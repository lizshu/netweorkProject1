import java.io.*;
import java.net.*;
import java.util.*;

final class HttpRequest implements Runnable
{
    //returning carriage return (CR) and a line feed (LF)
    final static String CRLF = "\r\n";
    Socket socket;

    // My Constructor
    public HttpRequest(Socket socket) throws Exception{
        this.socket = socket;
    }

    // Implement the run() method of the Runnable interface.
    //Within run(), we explicitly catch and handle exceptions with a try/catch block.
    public void run(){
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception{
        // Get reference to socket's input and output streams.
        InputStream inStream = socket.getInputStream();
        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());

        // Set up input stream filters.
        BufferedReader br = new BufferedReader(new InputStreamReader(inStream));//reads the input data

        // Get request line of HTTP request message.
        String requestLine = br.readLine();// get /path/file.html version of http

        // Extract filename from the request line.
        StringTokenizer tokens = new StringTokenizer(requestLine);// this is a input method with deliminators
        tokens.nextToken(); // skip over the method, which should be "GET"
        String fileName = tokens.nextToken();

        // Prepend a "." so that file request is within the current directory.
        fileName = "." + fileName;

        //Open the requested file.
        FileInputStream fis = null;
        boolean fileExists = true;
        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            fileExists = false;
        }

        //Construct the response message.
        String statusLine = null;

        if (fileExists) {
            statusLine = "HTTP/1.1 200 OK" + CRLF; //common success message
        }
        else {
            statusLine = "HTTP/1.1 404 Not Found" + CRLF; //common error message
        }

        //Send the status line.
        outStream.writeBytes(statusLine);
        //Send a blank line to indicate the end of the header lines.
        outStream.writeBytes(CRLF);

        //Send the entity body.
        if (!fileExists) {
            fis = new FileInputStream("404.html");
        }
        sendBytes(fis, outStream);
        fis.close();

        // Close streams and socket.
        outStream.close();
        br.close();
        socket.close();
    }

    //set up input output streams
    private static void sendBytes(FileInputStream fis, OutputStream outStream) throws Exception{
        // Construct a 1K buffer to hold bytes on their way to the socket.
        byte[] buffer = new byte[1024];
        int bytes = 0;

        // Copy requested file into the socket's output stream.
        while((bytes = fis.read(buffer)) != -1 )// read() returns minus one, indicating that the end of the file
        {
            outStream.write(buffer, 0, bytes);
        }}
}