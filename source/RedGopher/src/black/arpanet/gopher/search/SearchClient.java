package black.arpanet.gopher.search;

import black.arpanet.gopher.db.entities.GopherItem;

public interface SearchClient {

	public byte[] doSearch(GopherItem item, String gopherPath, String queryStr);
	
}
