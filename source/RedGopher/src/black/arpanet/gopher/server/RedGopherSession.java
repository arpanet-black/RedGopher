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
	//TODO: Break this out into a "handler" interface and implementation
	private byte[] getContentForRequest(String input) {

		List<GopherItem> items = null;

		if(input.equals(RedGopherServer.CRLF)) {
			items = RedGopherDbManager.findTopLevelGopherItems();

			if(items == null || items.size() < 1) {
				return NO_FILES_MESSAGE.getBytes();
			}
		} else {
			items = RedGopherDbManager.findGopherItemsByGopherPath(input.trim());
		}

		return buildGopherResponse(items);
	}

	//Build a response based on the items being sent back
	private byte[] buildGopherResponse(List<GopherItem> items) {

		if(items.size() == 1) {
			GopherItem item = items.get(0);

			GopherResourceType gopherResType = GopherResourceType.fromOrdinal(item.getResourceDescriptor().getGopherResourceType());

			if(gopherResType.equals(GopherResourceType.DIRECTORY)) {
				items = RedGopherDbManager.findGopherItemsByParentPath(item.getGopherPath() + PATH_SEP);
			} else {

				ServerResourceType serverResType = ServerResourceType.fromOrdinal(item.getResourceDescriptor().getServerResourceType()); 

				switch(serverResType) {				
				case LOCAL_FILE: return loadLocalFile(item);
				case RSS2_FEED:
					break;
				case RSS2_ITEM:
					break;
				case VIRTUAL_DIRECTORY:
					break;
				case VIRTUAL_FILE: return item.getContent();
				default: w(LOG, "Unsupported serverResourceType: " + serverResType.toString());
					break;

				}

			}
		}

		return buildMenu(items).getBytes();
	}

	//Load a file from the local file system
	private byte[] loadLocalFile(GopherItem i) {
		byte[] bytes = null;

		try {
			URI resUri = new URI(i.getResourcePath());
			d(LOG, "Reading local file URI: " + resUri);					
			File resource = new File(resUri);
			return Files.readAllBytes(resource.toPath());

		} catch (URISyntaxException urie) {
			e(LOG, String.format("Exception attempting to obtain resource URI for GopherItem ID: %s", i.getId()), urie);
			urie.printStackTrace();
		} catch (FileNotFoundException fnfe) {
			e(LOG, String.format("Exception attempting to open GopherItem resource URI for reading. ID: %s, URI: %s", i.getId(), i.getResourcePath()), fnfe);
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			e(LOG, String.format("Exception attempting to read from file. Gopher ID: %s, URI: %s", i.getId(), i.getResourcePath()), ioe);
			ioe.printStackTrace();
		}
		
		return bytes;
	}

	//Build a menu from the content items
	private String buildMenu(List<GopherItem> items) {
		StringBuilder sb = new StringBuilder();

		for(GopherItem gi : items) {
			//File Type - Display Text - Selector String - Domain Name - Port - CRLF
			String gopherTypeId = GopherResourceType.values()[gi.getResourceDescriptor().getGopherResourceType()].getTypeId();
			sb.append(String.format("%s%s%s%s%s%s%s%s%s%s", 
					gopherTypeId,
					gi.getDisplayText(), RedGopherServer.TAB,
					gi.getGopherPath(), RedGopherServer.TAB,
					gi.getDomainName(), RedGopherServer.TAB,
					gi.getPort(), RedGopherServer.TAB,
					RedGopherServer.CRLF));
		}

		sb.append(".").append(RedGopherServer.CRLF);


		return sb.toString();
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
