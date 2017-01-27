package black.arpanet.gopher.server;

import static black.arpanet.util.logging.ArpanetLogUtil.t;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;

public class RedGopherAdminSession extends Thread {
	
	private static final Logger LOG = LogManager.getLogger(RedGopherAdminServer.class);

	protected Socket socket;

	public RedGopherAdminSession(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {

		//Don't handle an invalid socket
		if(socket != null && !socket.isClosed()) {
			//Try with resources
			try(PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); ) {

				//If we got a value, process it
				String outputLine = handleAdminRequest(in.readLine());

				//Send the result back to the client
				out.println(outputLine);	
				
			} catch (IOException e) {
				//Print any errors
				e.printStackTrace();
			} finally {
				//Go ahead and close the session
				IOUtils.closeQuietly(socket);
			}
		}			
	}

	//Where the magic happens
	public String handleAdminRequest(String input) {
		t(LOG,"Input is: " + input.trim());

		StringBuilder chars = new StringBuilder("Characters: [");
		for(int i = 0; i < input.length(); i++) {
			if(i > 0) {
				chars.append(", ");
			}
			chars.append((int)input.charAt(i));			
		}
		chars.append("]");
		t(LOG,chars.toString());

		//File Type - Display Text - Selector String - Domain Name - Port - CRLF
		return  "OK";
	}

	//Static method for handling sockets that could not be accepted
	//and given a session
	public static void rejectSession(Socket socket) {

	}

}
