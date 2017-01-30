package black.arpanet.gopher.server.content;

import black.arpanet.gopher.db.entities.GopherItem;

public class Rss2ItemContentHandler implements ContentHandler {

	@Override
	public byte[] getContent(GopherItem item, String input) {
		return item.getContent();
	}

}
