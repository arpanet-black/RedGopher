package black.arpanet.gopher.files;

import static black.arpanet.gopher.util.RedGopherLogUtil.t;

import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ContentBuilderFactory {

	private static final String GOPHERMAP_PROP = "gophermap";
	private static final String CONTENT_MODE_PROP = "content_mode";
	private static final String DEFAULT_CONTENT_MODE = "GOPHERMAP";
	private static final String DEFAULT_GOPHERMAP_NAME = "gophermap.xml";
	private static final String DOMAIN_NAME_PROP = "domain_name";
	private static final String GOPHER_PORT_PROP = "gopher_port";
	
	private static final Logger LOG = LogManager.getLogger(ContentBuilderFactory.class);

	public static ContentBuilder getContentBuilder(Properties props) {		
		
		ContentMode contentMode = ContentMode.valueOf(props.getProperty(CONTENT_MODE_PROP,DEFAULT_CONTENT_MODE));
		
		t(LOG, String.format("Content mode is: %s", contentMode.toString()));
		
		String gopherMapFileName = props.getProperty(GOPHERMAP_PROP, DEFAULT_GOPHERMAP_NAME);
		String domainName = props.getProperty(DOMAIN_NAME_PROP);
		int port = Integer.valueOf(props.getProperty(GOPHER_PORT_PROP));
		
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
