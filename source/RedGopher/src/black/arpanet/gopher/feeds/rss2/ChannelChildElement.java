package black.arpanet.gopher.feeds.rss2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static black.arpanet.util.logging.ArpanetLogUtil.t;

public enum ChannelChildElement {

	TITLE,
	LINK,
	DESCRIPTION,
	ITEM,
	LANGUAGE,
	COPYRIGHT,
	MANAGINGEDITOR,
	WEBMASTER,
	PUBDATE,
	LASTBUILDDATE,
	CATEGORY,
	GENERATOR,
	DOCS,	
	TTL,
	IMAGE,
	CLOUD,
	TEXTINPUT,
	SKIPHOURS,
	SKIPDAYS,
	UNKNOWN;
	
	private static final Logger LOG = LogManager.getLogger(ChannelChildElement.class);
	
	public static ChannelChildElement fromString(String str) {
		ChannelChildElement cce = ChannelChildElement.UNKNOWN;
		try {
			cce = ChannelChildElement.valueOf(str.toUpperCase());
		} catch (IllegalArgumentException |  NullPointerException ex) {
			t(LOG, String.format("Unknown value: %s", str));
		}
		
		return cce;
	}
	
}
