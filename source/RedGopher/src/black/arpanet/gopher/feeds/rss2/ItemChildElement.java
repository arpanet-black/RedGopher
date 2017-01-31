package black.arpanet.gopher.feeds.rss2;

import static black.arpanet.util.logging.ArpanetLogUtil.t;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum ItemChildElement {

	DESCRIPTION,
	TITLE,
	LINK,
	AUTHOR,
	CATEGORY,
	COMMENTS,
	ENCLOSURE,
	GUID,
	PUBDATE,
	SOURCE,
	ENCODED,
	CREATOR,
	UNKNOWN;
	
	private static final Logger LOG = LogManager.getLogger(ItemChildElement.class);

	public static ItemChildElement fromString(String str) {
		ItemChildElement ice = ItemChildElement.UNKNOWN;
		try {
			ice = ItemChildElement.valueOf(str.toUpperCase());
		} catch (IllegalArgumentException |  NullPointerException ex) {
			t(LOG, String.format("Unknown value: %s", str));
		}

		return ice;
	}

}
