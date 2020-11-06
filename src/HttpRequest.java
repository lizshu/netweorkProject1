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

        //reads input data
        BufferedReader br = new BufferedReader(new InputStreamReader(inStream));

        // Get request line of HTTP request message. (/path/file.html version of http)
        String requestLine = br.readLine();

        // Extract filename from the request line.
        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken(); // skip over the method, which should be "GET"
        String fileName = tokens.nextToken();

        //add "." to declare that file is in current directory.
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

        /* I really struggled to implement cookies, so I ended up deleting what I had.
        This is where the cookie information would go.
            There would be a setPath that required "/els141" to be within the provided URL
            There would be a setMaxTime=27000 that would tell when to kill the cookie
            The cookie would be called numVisited and the value for that cookie would be iterated whenever a valid URL is called
            Then, whenever https://eecslab-10:50064/els141/visit.html was called, the html file would be run and we'd add a
                reference to the value within the cookie which would be displayed on the page
            Which would result in a page that contained the text on the visit.html page followed by the number of times
                that valid URLs have been visited
         */

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

        //Added to give more information when call fails/succeeds
        System.out.println("*****");
        System.out.println(fileName);//print out file request to console
        System.out.println("*****");
        // Get and display the header lines.
        String headerLine = null;
        while ((headerLine = br.readLine()).length() != 0) {
            System.out.println(headerLine);
        }

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