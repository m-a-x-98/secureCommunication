import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.securechat.serverhelpers.*;

public class KeyMaterialServer {
    private final AuthService authService;
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public KeyMaterialServer(AuthService authService, int port) {
        this.authService = authService;
        this.port = port;
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
                int payloadLen = in.readInt();
                byte[] inPayload = new byte[payloadLen];
                in.readFully(inPayload);

                try {
                    byte[] outPayload = fullfillRequest(msgType, inPayload);
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
        }
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

    private byte[] fullfillRequest(int msgType, byte[] inPayload) throws Exception{
        byte[] outPayload = null;
        switch (msgType) {
            case 0x01:
                outPayload = new byte[0];
                break;
            case 0x03: // case 0x02 already handled
                int ret1 = storeKeyMaterialPayload(inPayload);
                if (ret1 != 0) return null;
                else outPayload = MessageCodec.createConfirmationPayload();  
                break;
            case 0x04:
                outPayload = getKeyMaterialPayload(inPayload);
                break;
            case 0x05:
                int ret2 = updatePadPositionPayload(inPayload);
                if (ret2 != 0) return null;
                else outPayload = MessageCodec.createConfirmationPayload(); 
                break;
            case 0x06:
                outPayload = getPadPositionPayload(inPayload);
                break;
            case 0x07:
                outPayload = getPadSizePayload(inPayload);
                break;
        
            default:
                break;
        }
        return outPayload;
    }

    private int storeKeyMaterialPayload(byte[] payload){
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0);
        MsgBytes keyMaterial = MessageCodec.readMessageBytesOffset(payload, username.getEndPos());

        try {
            authService.storeEncryptedKeyMaterial(username.getEntry(), keyMaterial.getEntry());
        } catch (Exception e){
            return 1;
        }
        return 0;
    }

    private byte[] getKeyMaterialPayload(byte[] payload){
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0);
        byte[] encryptedKey;
        try {
            encryptedKey = authService.getEncryptedKeyMaterial(username.getEntry());
        } catch (Exception e){
            return null;
        }
        return encryptedKey;
    }

    private int updatePadPositionPayload(byte[] payload){
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0);
        long position = MessageCodec.readLongAt(payload, username.getEndPos());

        try {
            authService.updatePadPosition(username.getEntry(), position);
        } catch (Exception e){
            return 1;
        }
        return 0;
    }

    private byte[] getPadPositionPayload(byte[] payload){
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0);
        long padPos;
        try {
            padPos = authService.getPadState(username.getEntry());
        } catch (Exception e){
            return null;
        }
        return MessageCodec.longToByteArray(padPos);
    }

    private byte[] getPadSizePayload(byte[] payload){
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0);
        long padSize;
        try {
            padSize = authService.getPadSize(username.getEntry());
        } catch (Exception e){
            return null;
        }
        return MessageCodec.longToByteArray(padSize);
    }


}

