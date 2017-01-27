package black.arpanet.gopher;

import static black.arpanet.util.logging.ArpanetLogUtil.e;
import static black.arpanet.util.logging.ArpanetLogUtil.i;
import static black.arpanet.util.logging.ArpanetLogUtil.w;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;

import black.arpanet.gopher.db.RedGopherDbHelper;
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
	public static final String CONTENT_MANAGER_CONFIG_PROP = "content_manager_config";
	
	public static final String DEFAULT_CONTENT_MANAGER_FILE = "contentmanager.xml";
	public static final String DEFAULT_GOPHER_PORT = "70";
	public static final String DEFAULT_CORE_POOL_SIZE = "10";
	public static final String DEFAULT_MAX_POOL_SIZE = "25";
	public static final String DEFAULT_KEEP_ALIVE_TIME_MS = "2000";
	
	private static final Logger LOG = LogManager.getLogger(RedGopher.class);
	
	private static Properties props;
	private static ThreadPoolExecutor tpe;
	private static RedGopherServer gopherServer;
	private static RedGopherAdminServer adminServer;

	public static void main(String[] args) {
		
		i(LOG,"Starting RedGopher...");

		loadProperties();	
		
		initThreadPool();
		
		initDatabase();
		
		startContentManagers();
		
		startGopherServer();
		
		startAdminServer();		

		i(LOG,"RedGopher started.");
		
	}

	private static void initThreadPool() {
		int corePoolSize = Integer.valueOf(props.getProperty(CORE_POOL_SIZE_PROP,DEFAULT_CORE_POOL_SIZE));
		int maxPoolSize = Integer.valueOf(props.getProperty(MAX_POOL_SIZE_PROP,DEFAULT_MAX_POOL_SIZE));
		int keepAliveTimeMs = Integer.valueOf(props.getProperty(KEEP_ALIVE_TIME_MS_PROP,DEFAULT_KEEP_ALIVE_TIME_MS));
		tpe = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTimeMs, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(maxPoolSize));
	}

	private static void initDatabase() {		
		
		RedGopherDbHelper.init();
		
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

	}

	private static void startContentManagers() {
		
		String contentManagerFile = props.getProperty(CONTENT_MANAGER_CONFIG_PROP,DEFAULT_CONTENT_MANAGER_FILE);
		
		for(ContentManager cm : ContentManagerFactory.getContentManagers(contentManagerFile)) {
			i(LOG, String.format("Starting content manager: %s", cm.getClass().getSimpleName()));
			if(cm.init(props) && cm.loadContent()) {			
				tpe.submit(cm);
			} else {
				e(LOG, String.format("Failed to start content manager! Name: %s", cm.getClass().getSimpleName()));
			}
		}
		
	}

	private static void startGopherServer() {
		RedGopherServerProperties gopherServerProps = new RedGopherServerProperties();
		gopherServerProps.setPort(Integer.valueOf(props.getProperty(GOPHER_PORT_PROP)));
		gopherServerProps.setCorePoolSize(Integer.valueOf(props.getProperty(CORE_POOL_SIZE_PROP,DEFAULT_CORE_POOL_SIZE)));
		gopherServerProps.setMaxPoolSize(Integer.valueOf(props.getProperty(MAX_POOL_SIZE_PROP,DEFAULT_MAX_POOL_SIZE)));
		gopherServerProps.setKeepAliveTime(Long.valueOf(props.getProperty(KEEP_ALIVE_TIME_MS_PROP,DEFAULT_KEEP_ALIVE_TIME_MS)));
		gopherServerProps.setKeepAliveUnits(TimeUnit.MILLISECONDS);
		
		gopherServer = new RedGopherServer(gopherServerProps);
		
		gopherServer.start();
	}
	
	private static void startAdminServer() {
		RedGopherServerProperties adminServerProps = new RedGopherServerProperties();
		adminServerProps.setPort(Integer.valueOf(props.getProperty(ADMIN_PORT_PROP)));
		adminServerProps.setCorePoolSize(Integer.valueOf(props.getProperty(CORE_POOL_SIZE_PROP,DEFAULT_CORE_POOL_SIZE)));
		adminServerProps.setMaxPoolSize(Integer.valueOf(props.getProperty(MAX_POOL_SIZE_PROP,DEFAULT_MAX_POOL_SIZE)));
		adminServerProps.setKeepAliveTime(Long.valueOf(props.getProperty(KEEP_ALIVE_TIME_MS_PROP,DEFAULT_KEEP_ALIVE_TIME_MS)));
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
