import java.util.List;
import java.util.Arrays;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

import com.securechat.authService.AuthService;
import com.securechat.serverhelpers.*;


public class MessageServer {
    private static final int MAX_COUNT = 1000;

    private AuthService authService;

    private final MessageDB messageDB;
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public MessageServer(int port, MessageDB messageDB, AuthService authService) {
        this.messageDB = messageDB;
        this.port = port;
        this.authService = authService;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"));
        running = true;
        while (running) {
            Socket client;
            try {
                client = serverSocket.accept();
            } catch (SocketException e) {
                if (!running) break;   // stop() closed the socket: normal shutdown
                throw e;
            }
            try {
                handleConnection(client);
            } catch (RuntimeException e) {
                System.err.println("Connection failed: " + e);
            }
        }
    }

    public void stop() throws IOException {
        running = false;
        if (serverSocket != null) serverSocket.close();
    }

    private void handleConnection(Socket client) {
        try (
            DataInputStream in = new DataInputStream(client.getInputStream());
            DataOutputStream out = new DataOutputStream(client.getOutputStream())){
            
            int firstType = in.readUnsignedByte();
            if (firstType != 0x02){
                MessageHelpers.writeError(out);
                return;
            }

            String username = authenticateAndGetUsername(in, out);
            if (username == null) {
                MessageHelpers.writeError(out);
                return;
            }
            MessageHelpers.writeSuccess(out);

            while (!client.isClosed()) {
                int msgType = in.readUnsignedByte();
                byte[] inPayload = MessageHelpers.readPayload(in);
                if (inPayload == null) {
                    MessageHelpers.writeError(out);
                    return;               // stream is out of sync: close the connection
                }

                try {
                    byte[] outPayload = fulfillRequest(msgType, inPayload);
                    if (outPayload == null) throw new Exception(); // Not a valid request 
                    MessageHelpers.sendMsg(0x00, outPayload, out);
                } catch (Exception e) {
                    MessageHelpers.writeError(out);
                }
            }

        } catch (EOFException e) {
            // client disconnected — normal, not an error
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } catch (Exception e){
            System.err.println("Critical failure: " + e.getMessage());
        }
    }


    private byte[] fulfillRequest(int msgType, byte[] inPayload) throws RuntimeException, IOException{
        byte[] outPayload = null;
        switch (msgType) {
            case 0x01:
                outPayload = new byte[0];
                break;
            case 0x03: 
                int ret1 = addMessagePayload(inPayload);
                if (ret1 != 0) return null;
                else outPayload = MessageCodec.createConfirmationPayload();  
                break;
            case 0x04:
                outPayload = MessageCodec.listMessagesToPayload(getNMessagesPayload(inPayload));
                break;
            case 0x05:
                outPayload = MessageCodec.listMessagesToPayload(getMessagesTimePayload(inPayload));
                break;
        
            default:
                break;
        }
        return outPayload;
    }


    private String authenticateAndGetUsername(DataInputStream in, DataOutputStream out){
        String username;
        try{
            int payloadLen = in.readInt();

            byte[] payload = new byte[payloadLen];
            in.readFully(payload);

            int idxCounter = 0;
            int usernameLen = ((payload[idxCounter++] & 0xff) << 8) | (payload[idxCounter++] & 0xff); // 2 first bytes are username len
            byte[] usernameBytes = Arrays.copyOfRange(payload, idxCounter, usernameLen+idxCounter);
            username = new String(usernameBytes, StandardCharsets.UTF_8);
            idxCounter += usernameLen;

            int passwordLen = ((payload[idxCounter++] & 0xff) << 8) | (payload[idxCounter++] & 0xff);
            byte[] passwordBytes = Arrays.copyOfRange(payload, idxCounter, idxCounter+passwordLen);
            String password = new String(passwordBytes, StandardCharsets.UTF_8);

            if (!authService.verifyLogin(username, password)){
                throw new Exception();
            }
        } catch (Exception e){
            return null;
        }
        return username;
    }



    private List<UserMessage> getNMessagesPayload(byte[] payload) throws RuntimeException{
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0, 2);
        int count = MessageCodec.readInt32At(payload, username.getEndPos());   // was "position"
        if (count < 1 || count > MAX_COUNT) {
            throw new IllegalArgumentException("Bad message count: " + count);
        }
        return messageDB.getNMessages(username.getEntry(), count);
    }

    private List<UserMessage> getMessagesTimePayload(byte[] payload) throws RuntimeException{
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0, 2);
        Timestamp startTime = MessageCodec.readTimestampAt(payload, username.getEndPos());
        Timestamp endTime = MessageCodec.readTimestampAt(payload, username.getEndPos() + 8);
        List<UserMessage> messages;

        messages = messageDB.getMessagesTime(username.getEntry(), 
                                             startTime, 
                                             endTime, 0);

        return messages;
    }

    private int addMessagePayload(byte[] payload){
        MsgBytes message = MessageCodec.readMessageBytesOffset(payload, 0, 4);
        long padStartOffset = MessageCodec.readLongAt(payload, message.getEndPos());
        MsgEntry username = MessageCodec.readMessageOffset(payload, message.getEndPos() + 8, 2);
        MsgEntry recipient = MessageCodec.readMessageOffset(payload, username.getEndPos(), 2);
        Timestamp timeBytes = MessageCodec.readTimestampAt(payload, recipient.getEndPos());

        UserMessage msg = new UserMessage(message.getEntry(), 
                                          padStartOffset, 
                                          username.getEntry(), 
                                          recipient.getEntry(), 
                                          timeBytes);

        try {
            messageDB.addMessage(msg);
        } catch (Exception e){
            return 1;
        }
        return 0;
    }
}
