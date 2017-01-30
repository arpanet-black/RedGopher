package black.arpanet.gopher.server.content;

import static black.arpanet.util.logging.ArpanetLogUtil.d;
import static black.arpanet.util.logging.ArpanetLogUtil.e;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import black.arpanet.gopher.db.entities.GopherItem;

public class LocalFileContentHandler implements ContentHandler {
	
	private static final Logger LOG = LogManager.getLogger(LocalFileContentHandler.class);

	@Override
	public byte[] getContent(GopherItem item, String input) {
		byte[] bytes = null;

		try {
			URI resUri = new URI(item.getResourcePath());
			d(LOG, "Reading local file URI: " + resUri);					
			File resource = new File(resUri);
			return Files.readAllBytes(resource.toPath());

		} catch (URISyntaxException urie) {
			e(LOG, String.format("Exception attempting to obtain resource URI for GopherItem ID: %s", item.getId()), urie);
			urie.printStackTrace();
		} catch (FileNotFoundException fnfe) {
			e(LOG, String.format("Exception attempting to open GopherItem resource URI for reading. ID: %s, URI: %s", item.getId(), item.getResourcePath()), fnfe);
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			e(LOG, String.format("Exception attempting to read from file. Gopher ID: %s, URI: %s", item.getId(), item.getResourcePath()), ioe);
			ioe.printStackTrace();
		}
		
		return bytes;
	}

}
