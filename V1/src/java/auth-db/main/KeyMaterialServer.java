
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
                
                break;
            case 0x04:
                
                break;
            case 0x05:
                
                break;
            case 0x06:
                
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

    private int writeError(DataOutputStream out){


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
}