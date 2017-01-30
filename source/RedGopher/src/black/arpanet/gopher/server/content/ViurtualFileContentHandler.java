package black.arpanet.gopher.server.content;

import black.arpanet.gopher.db.entities.GopherItem;

public class ViurtualFileContentHandler implements ContentHandler {

	@Override
	public byte[] getContent(GopherItem item) {
		return item.getContent();
	}

}
