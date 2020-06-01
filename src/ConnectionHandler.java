import java.io.*;
import java.net.Socket;
import java.util.Date;

class ConnectionHandler extends Thread {
    private final String NOT_IMPLEMENTED = "PageContents/not_implemented.html";
    private final String NOT_FOUND = "PageContents/not_found.html";
    private final String HOME_PAGE = "PageContents/index.html";
    private BufferedReader reader;
    private PrintWriter writer;
    private BufferedOutputStream dataWriter;
    private Socket s;

    public ConnectionHandler(Socket s, BufferedReader reader, PrintWriter writer, BufferedOutputStream dataWriter) {
        this.s = s;
        this.reader = reader;
        this.writer = writer;
        this.dataWriter = dataWriter;
    }

    @Override
    public void run() {
        try {
            // reads first line of request from client:
            String request = reader.readLine();
            if (request == null)
                return;
            // extracts method and file url:
            String method = request.split(" ")[0];
            String fileRequested = request.split(" ")[1];
            System.out.println(fileRequested + " from: " + s);

            boolean methodIsNotGET = !method.equals("GET");
            if (methodIsNotGET) {
                sendResponse(NOT_IMPLEMENTED);
            } else {
                if (fileRequested.equals("/")) {
                    sendResponse(HOME_PAGE);
                } else {
                    File file = new File("PageContents" + fileRequested);
                    if (file.isFile())
                        sendResponse("PageContents" + fileRequested);
                    else
                        sendResponse(NOT_FOUND);
                }
            }
        } catch (IOException e) {
            System.err.println("probably socket closed abruptly! " + s);
        }
        try {
            // closing resources 
            this.reader.close();
            this.writer.close();
            this.dataWriter.close();

        } catch (IOException e) {
            System.err.println("An error occurred during closing IO sockets");
        }

        System.out.println("Connection closed: " + s);
    }

    private void sendResponse(String fileName) {
        //reads body file
        File file = new File(fileName);
        int fileLength = (int) file.length();
        byte[] fileData = readFileData(file, fileLength);

        sendHeader(fileName, fileLength);
        sendBody(fileData, fileLength);
    }

    private void sendBody(byte[] fileData, int fileLength) {
        try {
            dataWriter.write(fileData, 0, fileLength);
            dataWriter.flush();
        } catch (IOException e) {
            System.err.println("body sending error");
        }
    }

    private void sendHeader(String fileName, int fileLength) {

        if (fileName.equals(NOT_FOUND)) {
            writer.println("HTTP/1.1 404 File Not Found");
        } else {
            writer.println("HTTP/1.1 200 OK");
        }


        writer.println("Server: Java Web Server from Soroush Mehraban : 1.0");
        writer.println("Date: " + new Date());
        writer.println("Content-type: text/html");
        writer.println("Content-length: " + fileLength);
        writer.println(); // blank line between headers and content
        writer.flush(); // flush character output stream buffer
    }

    private byte[] readFileData(File file, int fileLength) {
        byte[] fileData = new byte[fileLength];
        try {
            try (FileInputStream inputFile = new FileInputStream(file)) {
                inputFile.read(fileData);
            }
        } catch (Exception e) {
            System.err.println("File reading error: " + file.getName());
        }
        return fileData;
    }
} 