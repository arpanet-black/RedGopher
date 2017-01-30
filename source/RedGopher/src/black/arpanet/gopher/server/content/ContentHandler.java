package black.arpanet.gopher.server.content;

import black.arpanet.gopher.db.entities.GopherItem;

public interface ContentHandler {

	public byte[] getContent(GopherItem item);
}
