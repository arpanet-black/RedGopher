package black.arpanet.gopher.files;

import static black.arpanet.util.logging.ArpanetLogUtil.d;
import static black.arpanet.util.logging.ArpanetLogUtil.i;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import black.arpanet.gopher.ConfigurationReader;
import black.arpanet.gopher.ContentMonitor;

public class FileContentMonitor extends ContentMonitor {
	
	private static final String CONTENT_MODE_PROP = "MODE";
	
	private static final Logger LOG = LogManager.getLogger(FileContentMonitor.class);

	private String contentDirectory;
	private ContentBuilder contentBuilder;

	@Override
	@SuppressWarnings("unchecked")	
	public boolean init(Map<String,Object> config) {
		d(LOG,"Initializing FileContentManager.");
		
		contentDirectory = config.get(ConfigurationReader.CONTENT_DIR).toString();
		
		Object tempParams = config.get(FileContentMonitor.class.getName());
		if(tempParams != null) {
			Map<String,String> params = (Map<String,String>)config.get(FileContentMonitor.class.getName());
			config.put(CONTENT_MODE_PROP, params.get(CONTENT_MODE_PROP));
		}
		
		contentBuilder = ContentBuilderFactory.getContentBuilder(config);
		
		return true;
	}

	@Override
	public boolean loadContent() {
		d(LOG,"Beginning FileContentManager.loadContent().");

		if(contentBuilder.initFromContentDirectory(contentDirectory)) {
			i(LOG,"RedGopher content initialized.");
		} else {
			i(LOG,String.format("RedGopher content could not be initialized!\nContent directory is: %s", contentDirectory));
		}
		
		return true;
	}

	@Override
	public boolean update() {
		//File content is static
		return false;
	}

}
