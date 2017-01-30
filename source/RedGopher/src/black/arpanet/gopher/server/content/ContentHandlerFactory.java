package black.arpanet.gopher.server.content;

import static black.arpanet.util.logging.ArpanetLogUtil.w;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import black.arpanet.gopher.ServerResourceType;

public class ContentHandlerFactory {
	
	private static final Logger LOG = LogManager.getLogger(ContentHandlerFactory.class);

	public static ContentHandler getHandler(ServerResourceType srt) {
		
		switch(srt) {
		case LOCAL_DIRECTORY:
		case VIRTUAL_DIRECTORY:
			return new DirectoryContentHandler();
			
		case LOCAL_FILE:
			return new LocalFileContentHandler();
		
		case VIRTUAL_FILE: 
			return new ViurtualFileContentHandler();
			
		case SEARCH:
			return new SearchContentHandler();
			
		case RSS2_FEED:
		case RSS2_ITEM:
			return new Rss2ContentHandler();
			
		default: w(LOG, String.format("Could not find content handler for resource type: %s", srt.toString()));
			break;
		
		}
		
		return null;
		
	}
}
