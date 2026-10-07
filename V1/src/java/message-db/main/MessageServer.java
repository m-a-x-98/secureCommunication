
import java.io.*;
import java.net.*;

import com.securechat.serverhelpers.*;

public class MessageServer {
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public MessageServer(int port) {
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


    private byte[] fullfillRequest(int msgType, byte[] inPayload) throws Exception{
        byte[] outPayload = null;
        switch (msgType) {
            case 0x01:
                outPayload = new byte[0];
                break;
            case 0x03: // case 0x02 already handled
                // int ret1 = storeKeyMaterialPayload(inPayload);
                // if (ret1 != 0) return null;
                // else outPayload = MessageCodec.createConfirmationPayload();  
                break;
            case 0x04:
                break;
            case 0x05:

                break;
            case 0x06:
                // outPayload = getPadPositionPayload(inPayload);
                break;
            case 0x07:
                break;
        
            default:
                break;
        }
        return outPayload;
    }
}
