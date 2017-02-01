package black.arpanet.gopher.server.content;

import static black.arpanet.util.logging.ArpanetLogUtil.t;
import static black.arpanet.util.logging.ArpanetLogUtil.w;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;

import black.arpanet.gopher.db.GopherItemBuilder;
import black.arpanet.gopher.db.RedGopherDbManager;
import black.arpanet.gopher.db.entities.GopherItem;
import black.arpanet.gopher.feeds.FeedClient;
import black.arpanet.gopher.feeds.FeedResponse;
import black.arpanet.gopher.feeds.rss2.Rss2Channel;
import black.arpanet.gopher.feeds.rss2.Rss2Item;
import black.arpanet.gopher.feeds.rss2.Rss2Parser;
import black.arpanet.util.security.ArpanetHashUtil;
import black.arpanet.util.text.ArpanetStringUtil;

public class Rss2FeedContentHandler extends DirectoryContentHandler {
	//TODO: Make line length a configurable property
	//TODO: Make HTML or plain text a configurable option for rss
	//TODO: Make line length splitting a configurable property
	private static final int LINE_LENGTH = 40;
	//TODO: Make most of these configurable
	private static final String TITLE_BANNER = " ----- ";
	private static final String DIVIDER = "----------------------------------------";
//	private static final boolean LIMIT_TITLE_LENGTH = false;
	
	private static final String PATH_SEP = "/";
	private static final String TEXT_JOIN_STR = "\n";
//	private static final String GOPHER_JOIN_STR = RedGopherServer.CRLF + GopherResourceType.INFORMATION_TEXT.getTypeId();
	
	private static final Logger LOG = LogManager.getLogger(Rss2FeedContentHandler.class);

	@Override
	public byte[] getContent(GopherItem item, String input) {
		
		//The actual feed content is stored using the link resource path as the gopher path
		//GopherItem rss2Feed = RedGopherDbManager.findSingleItemByGopherPath(item.getGopherPath());
		List<GopherItem> rss2Feed = RedGopherDbManager.findGopherItemsByParentPath(item.getGopherPath() + PATH_SEP);
		
		//If there are no stored items for this feed
		if(rss2Feed == null || rss2Feed.size() < 1) {		
			createFeed(item);
		} else {
			//Else, perform an update
			updateFeed(item);
		}
		
		byte[] response = super.getContent(item, input);

		return response;
	}
	
	private void createFeed(GopherItem item) {
		RedGopherDbManager.deleteByParentPath(item.getGopherPath() + PATH_SEP);
		FeedResponse fr = FeedClient.readFeed(item.getResourcePath());
		Rss2Channel fc = Rss2Parser.parse(fr.getData());
		buildGopherItem(fc, item);		
	}
	
	private void updateFeed(GopherItem item) {
		
		Calendar updateTime = GregorianCalendar.getInstance();
		updateTime.setTime(item.getUpdateDate());
		updateTime.add(Calendar.MINUTE, item.getTtlMinutes());
		
		if(LOG.isTraceEnabled()) {
				
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			t(LOG, String.format("Feed TTL Minutes: %s", item.getTtlMinutes()));
			t(LOG, String.format("Current Time: %s", sdf.format(GregorianCalendar.getInstance().getTime())));
			t(LOG, String.format("Expected Feed Update Time: %s", sdf.format(updateTime.getTime())));
		
		}
		//If we have passed the TTL time for this feed
		//then go ahead and update
 		if(GregorianCalendar.getInstance().after(updateTime)) {
			createFeed(item);
		}
		
	}

	private void buildGopherItem(Rss2Channel fc, GopherItem feedLink) {
		
		String feedGopherPath = feedLink.getGopherPath() + PATH_SEP;
		
		buildFeedHeader(fc, feedGopherPath, feedLink.isPersistOverRestart());
		
		for(Rss2Item rss2item : fc.getItems()) {
			String itemContent = buildItemContent(rss2item);

			String itemPath = feedGopherPath + createItemId(rss2item.getTitle());
			
			String itemTitle = rss2item.getTitle();			
			
			//TODO: Make this optional?
//			if(LIMIT_TITLE_LENGTH && itemTitle.length() > LINE_LENGTH) {
//				itemTitle = itemTitle.substring(0, LINE_LENGTH);
//			}
			
			GopherItem rss2ItemItem = GopherItemBuilder.buildRss2Item(itemTitle, itemPath, feedGopherPath, itemContent.getBytes(),
					feedLink.getDomainName(), feedLink.getPort(), feedLink.isPersistOverRestart());
			
			RedGopherDbManager.mergeGopherItem(rss2ItemItem);
			
			//Add a description after the item
			//TODO: Make this optional?
//			GopherItem infoItem = GopherItemBuilder.buildInfo(ArpanetStringUtil.breakLines(Jsoup.parse(rss2item.getDescription()).text(), LINE_LENGTH, GOPHER_JOIN_STR), feedGopherPath, feedLink.isPersistOverRestart());
//			RedGopherDbManager.mergeGopherItem(infoItem);
		}
		
		buildFeedFooter(fc, feedGopherPath, feedLink.isPersistOverRestart());
		
		if(StringUtils.isNotBlank(fc.getTtl())) {
			feedLink.setTtlMinutes(Integer.valueOf(fc.getTtl()));
		}
				
	}

	private String createItemId(String itemStr) {
		
		String hashStr = "";
		
		try {
			hashStr = new String(Base64.encodeBase64(ArpanetHashUtil.hash(itemStr)));
		} catch (NoSuchAlgorithmException ex) {
			w(LOG, "Hash algorithm not found.", ex);
			hashStr = itemStr.toLowerCase();
		}
		
		return hashStr;
	}

	private void buildFeedHeader(Rss2Channel fc, String gopherDir, boolean persistent) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(TITLE_BANNER).append(fc.getTitle()).append(TITLE_BANNER);
		GopherItem infoItem = GopherItemBuilder.buildInfo(sb.toString(), gopherDir, persistent);
		RedGopherDbManager.mergeGopherItem(infoItem);
		
		if(StringUtils.isNotBlank(fc.getDescription())) {
			infoItem = GopherItemBuilder.buildInfo("", gopherDir, persistent);
			RedGopherDbManager.mergeGopherItem(infoItem);

			infoItem = GopherItemBuilder.buildInfo(fc.getDescription(), gopherDir, persistent);
			RedGopherDbManager.mergeGopherItem(infoItem);
		}
		
		infoItem = GopherItemBuilder.buildInfo("", gopherDir, persistent);
		RedGopherDbManager.mergeGopherItem(infoItem);
		
		infoItem = GopherItemBuilder.buildInfo(DIVIDER, gopherDir, persistent);
		RedGopherDbManager.mergeGopherItem(infoItem); 
		
	}
	
	private void buildFeedFooter(Rss2Channel fc, String gopherDir, boolean persistent) {
		
		GopherItem infoItem = GopherItemBuilder.buildInfo("", gopherDir, persistent);
		RedGopherDbManager.mergeGopherItem(infoItem);
		
		infoItem = GopherItemBuilder.buildInfo(DIVIDER, gopherDir, persistent);
		RedGopherDbManager.mergeGopherItem(infoItem); 
		
		if(StringUtils.isNotBlank(fc.getCopyright())) {
			infoItem = GopherItemBuilder.buildInfo("", gopherDir, persistent);
			RedGopherDbManager.mergeGopherItem(infoItem);
			//Remove weird copyright symbol
			String copyright = fc.getCopyright().replaceAll(String.valueOf((char)65533), "(c)");
			infoItem = GopherItemBuilder.buildInfo("Copyright: " + copyright, gopherDir, persistent);
			RedGopherDbManager.mergeGopherItem(infoItem);
		}
	}
	
	private String buildItemContent(Rss2Item rss2item) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("Title: \n").append(ArpanetStringUtil.breakLines(rss2item.getTitle(),LINE_LENGTH, TEXT_JOIN_STR));

		sb.append("\n\n").append("Description: \n").append(ArpanetStringUtil.breakLines(Jsoup.parse(rss2item.getDescription()).text(),LINE_LENGTH, TEXT_JOIN_STR));
		
		if(rss2item.getEncoded() != null && StringUtils.isNotBlank(rss2item.getEncoded())) {
			String contentText = Jsoup.parse(rss2item.getEncoded()).text();
			sb.append("\n\n").append("Content: \n").append(ArpanetStringUtil.breakLines(contentText,LINE_LENGTH, TEXT_JOIN_STR));
		}
		
		return sb.toString();
	}




}

