package black.arpanet.gopher.server;

import static black.arpanet.util.logging.ArpanetLogUtil.d;
import static black.arpanet.util.logging.ArpanetLogUtil.e;
import static black.arpanet.util.logging.ArpanetLogUtil.t;
import static black.arpanet.util.logging.ArpanetLogUtil.w;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import black.arpanet.gopher.GopherResourceType;
import black.arpanet.gopher.ServerResourceType;
import black.arpanet.gopher.db.RedGopherDbManager;
import black.arpanet.gopher.db.entities.GopherItem;
import black.arpanet.gopher.server.content.ContentHandlerFactory;

public class RedGopherSession extends Thread {

	private static final String SOCKET_REJECT_MESSAGE = "3Server at max capacity.\r\n.\r\n";
	private static final String NO_FILES_MESSAGE = "3No files available.\r\n.\r\n";
	private static final int BUFFER_SIZE = 1024;
	private static final Logger LOG = LogManager.getLogger(RedGopherSession.class);
	private static final String PATH_SEP = "/";

	protected Socket socket;

	public RedGopherSession(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {

		//Don't handle an invalid socket
		if(socket != null && !socket.isClosed()) {
			//Try with resources
			try(PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					InputStreamReader inReader = new InputStreamReader(socket.getInputStream()); ) {

				//Read the input and process
				byte[] buffer = handleGopherRequest(readInputLine(inReader));

				//Send the result back to the client
				socket.getOutputStream().write(buffer, 0, buffer.length);													

			} catch (Exception e) {
				e(LOG, "Exception encountrered accepting session.", e);
			} finally {
				//Gopher is stateless so go ahead and close
				IOUtils.closeQuietly(socket);
			}
		}			
	}

	//Read the input stream, including any CR, LF, or whitespace
	private String readInputLine(InputStreamReader inReader) throws IOException {
		StringBuilder sb = new StringBuilder();
		char[] cbuf = new char[BUFFER_SIZE];
		int numChars = 0;

		while((numChars = inReader.read(cbuf)) > -1) {
			sb.append(new String(cbuf,0,numChars));
			Arrays.fill(cbuf, (char)0x00);
			numChars = 0;

			if(sb.substring(sb.length()-2).equals(RedGopherServer.CRLF)) {
				break;
			}
		}

		return sb.toString();
	}

	//Where the magic happens
	public byte[] handleGopherRequest(String input) {
		t(LOG,"Input is: " + input.trim());
		
//		RedGopherDbManager.logAllItems();

		StringBuilder sb = new StringBuilder("[");
		for(int i = 0; i < input.length(); i++) {
			if(i > 0) {
				sb.append(", ");
			}
			sb.append((int)input.charAt(i));			
		}
		sb.append("]");
		t(LOG,sb.toString());

		return getContentForRequest(input);

	}

	//Load resource content
	private byte[] getContentForRequest(String input) {

		GopherItem item = null;
		
		if(input.equals(RedGopherServer.CRLF)) {
			//Top level items have a gopher path that starts at root
			//The content handler should add to this path so just
			//make sure it is not null
			item = new GopherItem();
			item.setResourceDescriptor(RedGopherDbManager.findResourceDescriptor(GopherResourceType.DIRECTORY, ServerResourceType.LOCAL_DIRECTORY));
			item.setGopherPath("");
		} else {
			item = RedGopherDbManager.findSingleItemByGopherPath(input.trim());
		}

		ServerResourceType serverResType = ServerResourceType.fromOrdinal(item.getResourceDescriptor().getServerResourceType()); 
			
		return ContentHandlerFactory.getHandler(serverResType).getContent(item);				
		
	}
	
	//Static method for handling sockets that could not be accepted
	//and given a session
	public static void rejectSession(Socket socket) {
		try(PrintWriter out = new PrintWriter(socket.getOutputStream(), true);) {
			out.println(SOCKET_REJECT_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(socket);
		}
	}
}
