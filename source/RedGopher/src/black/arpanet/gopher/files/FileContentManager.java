package black.arpanet.gopher.files;

import static black.arpanet.util.ArpanetLogUtil.d;
import static black.arpanet.util.ArpanetLogUtil.i;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import black.arpanet.gopher.ContentManager;

public class FileContentManager extends ContentManager {
	
	private static final String CONTENT_DIRECTORY_PROP = "content_directory";
	private static final String DEFAULT_CONTENT_DIRECTORY = "c:\\gopher";
	
	private static final Logger LOG = LogManager.getLogger(FileContentManager.class);

	private String contentDirectory;
	private ContentBuilder contentBuilder;

	@Override
	public boolean init(Properties props) {
		d(LOG,"Initializing FileContentManager.");
		
		contentDirectory = props.getProperty(CONTENT_DIRECTORY_PROP, DEFAULT_CONTENT_DIRECTORY);
		contentBuilder = ContentBuilderFactory.getContentBuilder(props);
		
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
