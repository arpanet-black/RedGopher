package black.arpanet.gopher.search;

import static black.arpanet.util.logging.ArpanetLogUtil.t;
import static black.arpanet.util.logging.ArpanetLogUtil.w;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SearchClientFactory {
	
	private static final Logger LOG = LogManager.getLogger(SearchClientFactory.class);
	
	private static Map<String,Class<? extends SearchClient>> searchClients = new HashMap<String,Class<? extends SearchClient>>();
	
	public static void registerClient(String resPath, Class<? extends SearchClient> searchClass) {
		t(LOG, String.format("Search client registered: %s, %s", resPath, searchClass.getName()));
		searchClients.put(resPath, searchClass);
	}

	public static SearchClient getSearchClientByResourcePath(String resPath) {
		
		Class<? extends SearchClient> searchClass = searchClients.get(resPath);
		SearchClient sc = null;
		
		if(searchClass != null) {
			t(LOG, String.format("Search client found! %s, %s", resPath, searchClass.getName()));
			try {
				sc = searchClass.newInstance();
			} catch (InstantiationException | IllegalAccessException ex) {
				w(LOG, String.format("Exception encountered loading search client! %s, %s", resPath, searchClass.getName()), ex);
			}
		} else {
			w(LOG, String.format("Search client not found! %s", resPath));
		}
		
		return sc;
	}
}
