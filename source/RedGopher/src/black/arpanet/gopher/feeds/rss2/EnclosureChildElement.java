package black.arpanet.gopher.feeds.rss2;

import static black.arpanet.util.logging.ArpanetLogUtil.t;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum EnclosureChildElement {

	URL,
	LENGTH,
	TYPE,
	UNKNOWN;

	private static final Logger LOG = LogManager.getLogger(EnclosureChildElement.class);
	
	public static EnclosureChildElement fromString(String str) {
		EnclosureChildElement ece = EnclosureChildElement.UNKNOWN;
		try {
			ece = EnclosureChildElement.valueOf(str.toUpperCase());
		} catch (IllegalArgumentException |  NullPointerException ex) {
			t(LOG, String.format("Unknown value: %s", str));
		}
		
		return ece;
	}
}
