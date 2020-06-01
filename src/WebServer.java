import java.net.*;
import java.io.*;

public class WebServer {
    private static final int SOCKET_PORT = 80; //port in which server is listening
    private BufferedReader in; //input that listens to client
    private PrintWriter textOut; //for outputting header
    private BufferedOutputStream dataOut; //for outputting body
    private Socket socket;

    public WebServer(int port) {
        try {
            //Starts web server
            ServerSocket server = new ServerSocket(port);
            System.out.println("Web Server started");
            System.out.println("listening for connections on port " + port);

            while (true) { //listens for connection until program closes

                socket = server.accept(); //waits until someone connects
                System.out.println("Connection opened from: " + socket);

                // takes input and output from the client socket
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                textOut = new PrintWriter(socket.getOutputStream());
                dataOut = new BufferedOutputStream(socket.getOutputStream());

                //gives connection to connection handler and then comes back to line 20 to listen further connection
                ConnectionHandler connectionHandler = new ConnectionHandler(socket, in, textOut, dataOut);
                connectionHandler.start(); //starts new thread
            }
        } catch (IOException e) {
            System.err.println("Connection error");
            // close connection
            try {
                socket.close();
                in.close();
                textOut.close();
                dataOut.close();
            } catch (IOException ex) {
                System.err.println("Closing connection error");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        //writes port on port.txt
        // index.html needs port to send post request
        FileWriter fileWriter = new FileWriter("PageContents/port.txt");
        fileWriter.write(Integer.toString(SOCKET_PORT));
        fileWriter.close();

        new WebServer(SOCKET_PORT);

    }
}
