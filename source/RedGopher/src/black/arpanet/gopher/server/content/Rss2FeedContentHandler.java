package black.arpanet.gopher.server.content;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import black.arpanet.gopher.db.GopherItemBuilder;
import black.arpanet.gopher.db.RedGopherDbManager;
import black.arpanet.gopher.db.entities.GopherItem;
import black.arpanet.gopher.feeds.FeedClient;
import black.arpanet.gopher.feeds.FeedResponse;
import black.arpanet.gopher.feeds.rss2.Rss2Channel;
import black.arpanet.gopher.feeds.rss2.Rss2Item;
import black.arpanet.gopher.feeds.rss2.Rss2Parser;

public class Rss2FeedContentHandler extends DirectoryContentHandler {
	
	private static final String TITLE_BANNER = "----- ";
	private static final String DIVIDER = "----------------------------------------";

	@Override
	public byte[] getContent(GopherItem item, String input) {
		
		//The actual feed content is stored using the link resource path as the gopher path
		//GopherItem rss2Feed = RedGopherDbManager.findSingleItemByGopherPath(item.getGopherPath());
		List<GopherItem> rss2Feed = RedGopherDbManager.findGopherItemsByParentPath(item.getGopherPath());
		
		if(rss2Feed == null || rss2Feed.size() < 1) {		
			createNewFeed(item);
		}
		
		byte[] response = super.getContent(item, input);

		return response;
	}
	
	private GopherItem createNewFeed(GopherItem item) {
		RedGopherDbManager.deleteByParentPath(item.getGopherPath() + "/");
		FeedResponse fr = FeedClient.readFeed(item.getResourcePath());
		Rss2Channel fc = Rss2Parser.parse(fr.getData());
		GopherItem feedItem = buildGopherItem(fc, item);
		return feedItem;
	}

	private GopherItem buildGopherItem(Rss2Channel fc, GopherItem feedLink) {
		
		String feedGopherPath = feedLink.getGopherPath() + "/";
		
		buildFeedHeader(fc, feedGopherPath, feedLink.isPersistOverRestart());
		
		for(Rss2Item rss2item : fc.getItems()) {
			String itemContent = buildItemContent(rss2item);
			String itemPath = feedGopherPath + rss2item.getTitle();
			
			GopherItem rss2ItemItem = GopherItemBuilder.buildRss2Item(rss2item.getTitle(), itemPath, feedGopherPath, itemContent.getBytes(),
					feedLink.getDomainName(), feedLink.getPort(), feedLink.isPersistOverRestart());
			
			RedGopherDbManager.mergeGopherItem(rss2ItemItem);
		}
		
		return null;
	}

	private void buildFeedHeader(Rss2Channel fc, String gopherDir, boolean persistent) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(TITLE_BANNER).append(fc.getTitle()).append(TITLE_BANNER);
		GopherItem infoItem = GopherItemBuilder.buildInfo(sb.toString(), gopherDir, persistent);
		RedGopherDbManager.mergeGopherItem(infoItem);
		
		infoItem = GopherItemBuilder.buildInfo("", gopherDir, persistent);
		RedGopherDbManager.mergeGopherItem(infoItem);
		
		infoItem = GopherItemBuilder.buildInfo(fc.getDescription(), gopherDir, persistent);
		RedGopherDbManager.mergeGopherItem(infoItem);
		
		infoItem = GopherItemBuilder.buildInfo("", gopherDir, persistent);
		RedGopherDbManager.mergeGopherItem(infoItem);
		
		if(StringUtils.isNotBlank(fc.getCopyright())) {
			infoItem = GopherItemBuilder.buildInfo(fc.getCopyright(), gopherDir, persistent);
			RedGopherDbManager.mergeGopherItem(infoItem);
		}
		
		infoItem = GopherItemBuilder.buildInfo(DIVIDER, gopherDir, persistent);
		RedGopherDbManager.mergeGopherItem(infoItem);
		
	}
	
	private String buildItemContent(Rss2Item rss2item) {
		StringBuilder sb = new StringBuilder();
		sb.append(rss2item.getTitle()).append("\n\n");
		sb.append(rss2item.getEncoded());
		return sb.toString();
	}


}
