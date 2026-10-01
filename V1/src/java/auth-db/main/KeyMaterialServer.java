
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
                writeError(out);
                return;
            }

            String username = authenticateAndGetUsername(in, out);
            if (username == null) {
                writeError(out);
                return;
            }
            writeSuccess(out);
            

            while (!client.isClosed()) {
                int msgType = in.readUnsignedByte();
                int payloadLen = in.readInt();
                byte[] inPayload = new byte[payloadLen];
                in.readFully(inPayload);

                try {
                    byte[] outPayload = fullfillRequest(msgType, inPayload);
                    if (outPayload == null) throw new Exception(); // Not a valid request 
                    sendMsg(0x00, outPayload, out);
                } catch (Exception e) {
                    writeError(out);
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

    private void sendMsg(int status, byte[] payload, DataOutputStream out) throws IOException{
        out.writeByte(status);
        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }

    private int storeKeyMaterialPayload(byte[] payload){
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0);
        MsgBytes keyMaterial = MessageCodec.readMessageBytesOffset(payload, username.end_pos);

        try {
            authService.storeEncryptedKeyMaterial(username.entry, keyMaterial.entry);
        } catch (Exception e){
            return 1;
        }
        return 0;
    }

    private byte[] getKeyMaterialPayload(byte[] payload){
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0);
        byte[] encryptedKey;
        try {
            encryptedKey = authService.getEncryptedKeyMaterial(username.entry);
        } catch (Exception e){
            return null;
        }
        return encryptedKey;
    }

    private int updatePadPositionPayload(byte[] payload){
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0);
        long position = MessageCodec.readLongAt(payload, username.end_pos);

        try {
            authService.updatePadPosition(username.entry, position);
        } catch (Exception e){
            return 1;
        }
        return 0;
    }

    private byte[] getPadPositionPayload(byte[] payload){
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0);
        long padPos;
        try {
            padPos = authService.getPadState(username.entry);
        } catch (Exception e){
            return null;
        }
        return MessageCodec.longToByteArray(padPos);
    }

    private byte[] getPadSizePayload(byte[] payload){
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0);
        long padSize;
        try {
            padSize = authService.getPadSize(username.entry);
        } catch (Exception e){
            return null;
        }
        return MessageCodec.longToByteArray(padSize);
    }

    private static final int writeError(DataOutputStream out){


        // exception can be passed as argument and be used to send specific error codes to the client 


        try {
            out.writeByte(0x01);
            out.writeInt(0);
            out.flush();
        } catch (Exception e){
            return 1;
        }
        return 0;
    }

    private static final int writeSuccess(DataOutputStream out){
        try {
            out.writeByte(0x00);
            out.writeInt(0);
            out.flush();
        } catch (Exception e){
            return 1;
        }
        return 0;
    }
}

class MsgEntry{
    String entry;
    int end_pos;
    MsgEntry(String entry, int end_pos){
        this.entry = entry;
        this.end_pos = end_pos;
    }
}

class MsgBytes{
    byte[] entry;
    int end_pos;
    MsgBytes(byte[] entry, int end_pos){
        this.entry = entry;
        this.end_pos = end_pos;
    }
}

class MessageCodec {
    static MsgEntry readMessageOffset(byte[] payload, int offset){
        int idxCounter = offset;
        int entryLen = ((payload[idxCounter++] & 0xff) << 8) | (payload[idxCounter++] & 0xff); // 2 first bytes are username len
        byte[] entryBytes = Arrays.copyOfRange(payload, idxCounter, entryLen+idxCounter);
        String entry = new String(entryBytes, StandardCharsets.UTF_8);
        return new MsgEntry(entry, idxCounter + entryLen);
    }

    static MsgBytes readMessageBytesOffset(byte[] payload, int offset){
        int idxCounter = offset;
        int entryLen = ((payload[idxCounter++] & 0xff) << 8) | (payload[idxCounter++] & 0xff);
        byte[] entryBytes = Arrays.copyOfRange(payload, idxCounter, entryLen + idxCounter);
        return new MsgBytes(entryBytes, idxCounter + entryLen);
    }

    static byte[] createConfirmationPayload(){
        byte[] msg = {0};
        return msg;
    }

    static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    static byte[] longToByteArray(long value) {
        return new byte[] {
                (byte)(value >>> 56),
                (byte)(value >>> 48),
                (byte)(value >>> 40),
                (byte)(value >>> 32),
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    static long readLongAt(byte[] buf, int offset) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (buf[offset + i] & 0xff);
        }
        return value;
    }

}