package black.arpanet.gopher;

import static black.arpanet.gopher.util.RedGopherLogUtil.i;
import static black.arpanet.gopher.util.RedGopherLogUtil.w;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import black.arpanet.gopher.db.RedGopherDbHelper;
import black.arpanet.gopher.feeds.FeedClient;
import black.arpanet.gopher.feeds.FeedResponse;
import black.arpanet.gopher.feeds.rss2.Rss2Channel;
import black.arpanet.gopher.feeds.rss2.Rss2Item;
import black.arpanet.gopher.feeds.rss2.Rss2Parser;
import black.arpanet.gopher.server.ContentMode;
import black.arpanet.gopher.server.RedGopherAdminServer;
import black.arpanet.gopher.server.RedGopherServer;
import black.arpanet.gopher.server.RedGopherServerProperties;

public class RedGopher {

	public static final String VERSION = "0.1a";

	public static final String CONFIG_FILE = "redgopher.conf";

	public static final String DOMAIN_NAME_PROP = "domain_name";
	public static final String GOPHER_PORT_PROP = "gopher_port";
	public static final String ADMIN_PORT_PROP = "admin_port";
	public static final String CORE_POOL_SIZE_PROP = "core_pool_size";
	public static final String MAX_POOL_SIZE_PROP = "max_pool_size";
	public static final String KEEP_ALIVE_TIME_MS_PROP = "keep_alive_time_ms";
	public static final String INIT_DB_DATA_PROP = "init_db_data";
	public static final String CONTENT_DIRECTORY_PROP = "content_directory";
	public static final String GOPHERMAP_PROP = "gophermap";
	public static final String CONTENT_MODE_PROP = "content_mode";
	
	public static final String DEFAULT_CONTENT_MODE = "GOPHERMAP";
	public static final String DEFAULT_CONTENT_DIRECTORY = "c:\\gopher";
	public static final String DEFAULT_GOPHERMAP_NAME = "gophermap.xml";
	
	public static Properties props;
	
	private static final String CORE_POOL_SIZE_DEFAULT = "10";
	private static final String MAX_POOL_SIZE_DEFAULT = "25";
	private static final String KEEP_ALIVE_TIME_MS_DEFAULT = "2000";
	
	private static final Logger LOG = LogManager.getLogger(RedGopher.class);
		
	
	private static RedGopherServer gopherServer;
	private static RedGopherAdminServer adminServer;

	public static void main(String[] args) {
		
		i(LOG,"Starting RedGopher...");

		loadProperties();	
		
		initDatabase();
		
		startFeedManager();
		
		startGopherServer();
		
		startAdminServer();		

		i(LOG,"RedGopher started.");
		
	}

	private static void initDatabase() {		
		
		String domainName = props.getProperty(DOMAIN_NAME_PROP);
		int port = Integer.valueOf(props.getProperty(GOPHER_PORT_PROP));
		
		RedGopherDbHelper.init(domainName, port);
		
		//Load up the resource tables with initial values
		boolean initDbData = Boolean.valueOf(props.getProperty(INIT_DB_DATA_PROP,"false"));
		
		if(initDbData) {
			i(LOG,"Initializing database tables...");
			if(RedGopherDbHelper.initDbTables()) {
				i(LOG,"Tables initialized.");
			} else {
				w(LOG,"Table initialization failed!");
			}
		}

		//Load Gopher Items from content directory
		ContentMode contentMode = ContentMode.valueOf(props.getProperty(CONTENT_MODE_PROP,DEFAULT_CONTENT_MODE));
		String contentDirectory = props.getProperty(CONTENT_DIRECTORY_PROP, DEFAULT_CONTENT_DIRECTORY);
		String gopherMapFileName = props.getProperty(GOPHERMAP_PROP, DEFAULT_GOPHERMAP_NAME);

		if(contentMode.equals(ContentMode.DIRECTORY)) {
			if(RedGopherDbHelper.initFromContentDirectory(contentDirectory)) {
				i(LOG,"RedGopher content initialized from content directory.");
			} else {
				i(LOG,String.format("RedGopher content could not be initialized from content directory!\nDirectory is: %s", contentDirectory));
			}
		} else if(contentMode.equals(ContentMode.GOPHERMAP)) {
			if(RedGopherDbHelper.initFromGophermap(contentDirectory, gopherMapFileName)) {
				i(LOG,"RedGopher content initialized from gophermap.");
			} else {
				i(LOG,String.format("RedGopher content could not be initialized from gophermap!\nDirectory is: %s", contentDirectory));
			}
		}

	}

	private static void startFeedManager() {
		String url = "http://www.vcfed.org/forum/external.php?type=RSS2&forumids=20";
		
		i(LOG, "Fetrching URL: " + url);
		FeedResponse fr = FeedClient.readFeed(url);
		
		i(LOG, "Status code: " + fr.getStatusCode());
//		d(LOG, "Data: " + fr.getData());		

		Rss2Channel channel = Rss2Parser.parse(fr.getData());
		Map<String, String> cprops;
		try {
			cprops = BeanUtils.describe(channel);
			for(String s : cprops.keySet()) {
				i(LOG, String.format("Channel: %s = %s", s, cprops.get(s)));				
			}
			
			for(Rss2Item item : channel.getItems()) {
				i(LOG, "\n");
				cprops = BeanUtils.describe(item);
				for(String s : cprops.keySet()) {
					i(LOG, String.format("Item: %s = %s", s, cprops.get(s)));				
				}
			}
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	private static void startGopherServer() {
		RedGopherServerProperties gopherServerProps = new RedGopherServerProperties();
		gopherServerProps.setPort(Integer.valueOf(props.getProperty(GOPHER_PORT_PROP)));
		gopherServerProps.setCorePoolSize(Integer.valueOf(props.getProperty(CORE_POOL_SIZE_PROP,CORE_POOL_SIZE_DEFAULT)));
		gopherServerProps.setMaxPoolSize(Integer.valueOf(props.getProperty(MAX_POOL_SIZE_PROP,MAX_POOL_SIZE_DEFAULT)));
		gopherServerProps.setKeepAliveTime(Long.valueOf(props.getProperty(KEEP_ALIVE_TIME_MS_PROP,KEEP_ALIVE_TIME_MS_DEFAULT)));
		gopherServerProps.setKeepAliveUnits(TimeUnit.MILLISECONDS);
		
		gopherServer = new RedGopherServer(gopherServerProps);
		
		gopherServer.start();
	}
	
	private static void startAdminServer() {
		RedGopherServerProperties adminServerProps = new RedGopherServerProperties();
		adminServerProps.setPort(Integer.valueOf(props.getProperty(ADMIN_PORT_PROP)));
		adminServerProps.setCorePoolSize(Integer.valueOf(props.getProperty(CORE_POOL_SIZE_PROP,CORE_POOL_SIZE_DEFAULT)));
		adminServerProps.setMaxPoolSize(Integer.valueOf(props.getProperty(MAX_POOL_SIZE_PROP,MAX_POOL_SIZE_DEFAULT)));
		adminServerProps.setKeepAliveTime(Long.valueOf(props.getProperty(KEEP_ALIVE_TIME_MS_PROP,KEEP_ALIVE_TIME_MS_DEFAULT)));
		adminServerProps.setKeepAliveUnits(TimeUnit.MILLISECONDS);
		
		adminServer = new RedGopherAdminServer(adminServerProps, gopherServer);
		
		adminServer.start();
	}

	private static void loadProperties() {

		try {
			
			props = new Properties();
			InputStream inStream = RedGopher.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
			props.load(inStream);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	




}
