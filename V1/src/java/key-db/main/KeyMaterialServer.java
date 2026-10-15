import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.securechat.serverhelpers.*;

public class KeyMaterialServer {
    private final KeysDB keysDB;
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public KeyMaterialServer(KeysDB keysDB, int port) {
        this.keysDB = keysDB;
        this.port = port;
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
        try{
            int payloadLen = in.readInt();

            byte[] payload = new byte[payloadLen];
            in.readFully(payload);

            MsgEntry username = MessageCodec.readMessageOffset(payload, 0, 2);
            MsgEntry password = MessageCodec.readMessageOffset(payload, username.getEndPos(), 2);

            return keysDB.verifyLogin(username.getEntry(), password.getEntry())
                ? username.getEntry()
                : null;
        } catch (Exception e){
            return null;
        }
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
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0, 2);
        MsgBytes keyMaterial = MessageCodec.readMessageBytesOffset(payload, username.getEndPos(), 8);

        try {
            keysDB.storeEncryptedKeyMaterial(username.getEntry(), keyMaterial.getEntry());
        } catch (Exception e){
            return 1;
        }
        return 0;
    }

    private byte[] getKeyMaterialPayload(byte[] payload){
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0, 2);
        byte[] encryptedKey;
        try {
            encryptedKey = keysDB.getEncryptedKeyMaterial(username.getEntry());
        } catch (Exception e){
            return null;
        }
        return encryptedKey;
    }

    private int updatePadPositionPayload(byte[] payload){
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0, 2);
        long position = MessageCodec.readLongAt(payload, username.getEndPos());

        try {
            keysDB.updatePadPosition(username.getEntry(), position);
        } catch (Exception e){
            return 1;
        }
        return 0;
    }

    private byte[] getPadPositionPayload(byte[] payload){
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0, 2);
        long padPos;
        try {
            padPos = keysDB.getPadState(username.getEntry());
        } catch (Exception e){
            return null;
        }
        return MessageCodec.longToByteArray(padPos);
    }

    private byte[] getPadSizePayload(byte[] payload){
        MsgEntry username = MessageCodec.readMessageOffset(payload, 0, 2);
        long padSize;
        try {
            padSize = keysDB.getPadSize(username.getEntry());
        } catch (Exception e){
            return null;
        }
        return MessageCodec.longToByteArray(padSize);
    }


}

