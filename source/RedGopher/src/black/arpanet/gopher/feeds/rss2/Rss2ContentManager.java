package black.arpanet.gopher.feeds.rss2;

import java.util.Properties;

import black.arpanet.gopher.ContentManager;

public class Rss2ContentManager extends ContentManager {

	@Override
	public boolean init(Properties properties) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean loadContent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean update() {
		// TODO Auto-generated method stub
		return false;
	}
	
//	private static void startContentManagers() {
//		String url = "http://www.vcfed.org/forum/external.php?type=RSS2&forumids=20";
//		
//		i(LOG, "Fetrching URL: " + url);
//		FeedResponse fr = FeedClient.readFeed(url);
//		
//		i(LOG, "Status code: " + fr.getStatusCode());
////		d(LOG, "Data: " + fr.getData());		
//
//		Rss2Channel channel = Rss2Parser.parse(fr.getData());
//		Map<String, String> cprops;
//		try {
//			cprops = BeanUtils.describe(channel);
//			for(String s : cprops.keySet()) {
//				i(LOG, String.format("Channel: %s = %s", s, cprops.get(s)));				
//			}
//			
//			for(Rss2Item item : channel.getItems()) {
//				i(LOG, "\n");
//				cprops = BeanUtils.describe(item);
//				for(String s : cprops.keySet()) {
//					i(LOG, String.format("Item: %s = %s", s, cprops.get(s)));				
//				}
//			}
//		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//		
//	}
}
