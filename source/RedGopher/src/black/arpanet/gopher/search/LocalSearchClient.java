package black.arpanet.gopher.search;

import static black.arpanet.util.logging.ArpanetLogUtil.t;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import black.arpanet.gopher.db.entities.GopherItem;

public class LocalSearchClient implements SearchClient {

	private static final Logger LOG = LogManager.getLogger(GoogleSearchClient.class);
	
	@Override
	public byte[] doSearch(GopherItem item, String gopherPath, String queryStr) {
		t(LOG, String.format("Performing LOCAL search: %s, %s", gopherPath, queryStr));
		return new byte[0];
	}


}
