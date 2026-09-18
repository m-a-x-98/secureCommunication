
import java.io.*;
import java.net.*;

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
            handleConnection(client); // single-threaded for now — one C++ client at a time
        }
    }

    public void stop() throws IOException {
        running = false;
        if (serverSocket != null) serverSocket.close();
    }

    private void handleConnection(Socket client) {
        // loop: read one request, dispatch on message-type byte to the
        // matching AuthService method, write one response, repeat until
        // the client disconnects
    }
}