package black.arpanet.gopher.files;

import static black.arpanet.util.logging.ArpanetLogUtil.t;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import black.arpanet.gopher.ConfigurationReader;

public class ContentBuilderFactory {

	private static final String CONTENT_MODE_PROP = "MODE";
	private static final String DEFAULT_CONTENT_MODE = "GOPHERMAP";
	
	private static final Logger LOG = LogManager.getLogger(ContentBuilderFactory.class);

	public static ContentBuilder getContentBuilder(Map<String,Object> config) {		
		
		String configMode = config.get(CONTENT_MODE_PROP) != null ? config.get(CONTENT_MODE_PROP).toString() : null;
		
		ContentMode contentMode = null;
		if(StringUtils.isBlank(configMode)) {
			contentMode = ContentMode.valueOf(DEFAULT_CONTENT_MODE);
		} else {
			contentMode = ContentMode.valueOf(configMode);
		}
		
		t(LOG, String.format("Content mode is: %s", contentMode.toString()));
		
		String gopherMapFileName = config.get(ConfigurationReader.GOPHERMAP_FILE_NAME).toString();
		String domainName = config.get(ConfigurationReader.DOMAIN_NAME).toString();
		int port = (int)config.get(ConfigurationReader.PORT);
		
		ContentBuilder cb = null;
		
		switch(contentMode) {
		case DIRECTORY: cb = new DirectoryContentBuilder(domainName, port);
			break; 
		case GOPHERMAP: cb = new GophermapContentBuilder(domainName, port, gopherMapFileName);
			break;
		}
		
		t(LOG, String.format("Using %s content builder class.", cb.getClass().getSimpleName()));
		
		return cb;
	}
}
