package black.arpanet.gopher.server.content;

import static black.arpanet.util.logging.ArpanetLogUtil.d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import black.arpanet.gopher.db.entities.GopherItem;

public class SearchContentHandler implements ContentHandler {

	private static final Logger LOG = LogManager.getLogger(SearchContentHandler.class);
	
	@Override
	public byte[] getContent(GopherItem item, String input) {
		
		String[] inputItems = input.trim().split("\t");
		String searchServer = item.getResourcePath();
		
		d(LOG, String.format("Searching %s with string %s", searchServer, inputItems[1]));
		
		return null;
	}

}
