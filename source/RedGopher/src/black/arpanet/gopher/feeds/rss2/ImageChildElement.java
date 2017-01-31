package black.arpanet.gopher.feeds.rss2;

import static black.arpanet.util.logging.ArpanetLogUtil.t;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum ImageChildElement {

	URL,
	TITLE,
	LINK,
	WIDTH,
	HEIGHT,
	DESCRIPTION,
	UNKNOWN;

	private static final Logger LOG = LogManager.getLogger(ImageChildElement.class);

	public static ImageChildElement fromString(String str) {
		ImageChildElement ice = ImageChildElement.UNKNOWN;
		try {
			ice = ImageChildElement.valueOf(str.toUpperCase());
		} catch (IllegalArgumentException |  NullPointerException ex) {
			t(LOG, String.format("Unknown value: %s", str));
		}

		return ice;
	}

}
