import java.util.List;
import java.util.Arrays;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

import com.securechat.authService.AuthService;
import com.securechat.serverhelpers.*;

public class MessageServer {
    private AuthService authService;
    private static final int MAX_PAYLOAD = 16 * 1024 * 1024;

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
            Socket client = serverSocket.accept();
            handleConnection(client); // single-threaded for now, one C++ client at a time
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
                int payloadLen = Math.min(in.readInt(), MAX_PAYLOAD);
                if (payloadLen < 0) continue;
                byte[] inPayload = new byte[payloadLen];
                in.readFully(inPayload);

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
                outPayload = listMessagesToPayload(getNMessagesPayload(inPayload));
                break;
            case 0x05:
                outPayload = listMessagesToPayload(getMessagesTimePayload(inPayload));
                break;
        
            default:
                break;
        }
        return outPayload;
    }

    private byte[] listMessagesToPayload(List<UserMessage> messages) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(buffer);

        out.writeInt(messages.size());                          // [4 count]
        for (UserMessage msg : messages) {
            out.writeInt(msg.message.length);                   // [4 size]
            out.write(msg.message);                             // [ciphertext]
            out.writeLong(msg.padStartOffset);                  // [8 pad start]
            writeName(out, msg.user);                           // [2 size][sender]
            writeName(out, msg.recipient);                      // [2 size][recipient]
            out.writeLong(msg.time.getTime());                  // [8 epoch ms]

            if (buffer.size() > MAX_PAYLOAD) {                  // clamp the response size
                throw new IllegalArgumentException("Response too large");
            }
        }
        return buffer.toByteArray();
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

    private static void writeName(DataOutputStream out, String name) throws IOException {
        byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 0xFFFF) {                            // clamp to the 2-byte field
            throw new IllegalArgumentException("Name too long");
        }
        out.writeShort(bytes.length);
        out.write(bytes);
    }

    private List<UserMessage> getNMessagesPayload(byte[] payload) throws RuntimeException{
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0, 2);
        int position = MessageCodec.readInt32At(payload, username.getEndPos());
        List<UserMessage> messages;

        messages = messageDB.getNMessages(username.getEntry(), position);
        
        return messages;
    }

    private List<UserMessage> getMessagesTimePayload(byte[] payload) throws RuntimeException{
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0, 2);
        Timestamp startTime = MessageCodec.readTimestampAt(payload, username.getEndPos());
        Timestamp endTime = MessageCodec.readTimestampAt(payload, 8);
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
