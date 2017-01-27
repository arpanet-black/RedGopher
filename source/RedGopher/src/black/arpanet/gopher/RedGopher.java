package black.arpanet.gopher;

import static black.arpanet.util.logging.ArpanetLogUtil.e;
import static black.arpanet.util.logging.ArpanetLogUtil.i;
import static black.arpanet.util.logging.ArpanetLogUtil.t;
import static black.arpanet.util.logging.ArpanetLogUtil.w;

import java.util.List;
import java.util.Map;
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

	public static final String CONFIG_FILE = "gopherserv.xml";
	public static final String CONFIG_FILE_ARG = "-cfg";

	public static final String DOMAIN_NAME_PROP = "domain_name";
	public static final String GOPHER_PORT_PROP = "gopher_port";
	public static final String ADMIN_PORT_PROP = "admin_port";
	public static final String CORE_POOL_SIZE_PROP = "core_pool_size";
	public static final String MAX_POOL_SIZE_PROP = "max_pool_size";
	public static final String KEEP_ALIVE_TIME_MS_PROP = "keep_alive_time_ms";
	public static final String INIT_DB_DATA_PROP = "init_db_data";
	public static final String CONTENT_MONITOR_CONFIG_PROP = "content_manager_config";

	public static final String DEFAULT_SERVER_CONFIG_FILE = "contentmanager.xml";
	public static final String DEFAULT_GOPHER_PORT = "70";
	public static final String DEFAULT_CORE_POOL_SIZE = "10";
	public static final String DEFAULT_MAX_POOL_SIZE = "25";
	public static final String DEFAULT_KEEP_ALIVE_TIME_MS = "2000";

	private static final Logger LOG = LogManager.getLogger(RedGopher.class);

//	private static Properties props;
	private static Map<String,Object> config;
	private static ThreadPoolExecutor tpe;
	private static RedGopherServer gopherServer;
	private static RedGopherAdminServer adminServer;

	public static void main(String[] args) {

		i(LOG,"Starting RedGopher...");

		loadConfig(args);

		initThreadPool();

		initDatabase();

		startContentMonitors();

		startGopherServer();

		startAdminServer();		

		i(LOG,"RedGopher started.");

		while(adminServer.isAlive()) {
			try{Thread.sleep(1000);}catch(Exception e){}
		}

		tpe.shutdown();

		w(LOG,"RedGopher stopped.");

	}

	private static void loadConfig(String[] args) {
		String configFile = CONFIG_FILE;

		//Parse configFile from Arguments
		if(args != null && args.length > 0) {
			for(int i = 0; i < args.length; i++) {
				if(args[i] != null && args[i].equals(CONFIG_FILE_ARG)) {
					if(i + 1 < args.length) {
						configFile = args[i+1];
					}
				}
			}
		}

		i(LOG, String.format("Using configuration: %s", configFile));

		config = ConfigurationReader.readConfig(configFile);

	}

	private static void initThreadPool() {
		int corePoolSize = (int)config.get(ConfigurationReader.CORE_POOL_SIZE);
		int maxPoolSize = (int)config.get(ConfigurationReader.MAX_POOL_SIZE);
		int keepAliveTimeMs = (int)config.get(ConfigurationReader.POOL_KEEP_ALIVE_TIME);
		tpe = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTimeMs, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(maxPoolSize));
	}

	private static void initDatabase() {		

		RedGopherDbHelper.init();

		//Load up the resource tables with initial values
		boolean initDbData = (boolean)config.get(ConfigurationReader.INIT_DB);

		if(initDbData) {
			i(LOG,"Initializing database tables...");
			if(RedGopherDbHelper.initDbTables()) {
				i(LOG,"Tables initialized.");
			} else {
				w(LOG,"Table initialization failed!");
			}
		}

	}

	private static void startContentMonitors() {

		@SuppressWarnings("unchecked")
		List<String> contentMonitorClasses = (List<String>)config.get(ConfigurationReader.MONITOR_CLASSES);

		for(String cmClass : contentMonitorClasses) {
			
			t(LOG, String.format("Loading content monitor class: %s", cmClass));
			
			Class<?> clazz;
			try {
				clazz = RedGopher.class.getClassLoader().loadClass(cmClass);
				ContentMonitor cm = (ContentMonitor)clazz.newInstance();
				
				i(LOG, String.format("Starting content monitor: %s", cm.getClass().getSimpleName()));
				
				if(cm.init(config) && cm.loadContent()) {			
					tpe.submit(cm);
				} else {
					w(LOG, String.format("Failed to start content monitor! Name: %s", cm.getClass().getSimpleName()));
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
				e(LOG, "Exception loading content monitors!", ex);
			}			
			
			
			
		}

	}

	private static void startGopherServer() {
		RedGopherServerProperties gopherServerProps = new RedGopherServerProperties();
		gopherServerProps.setPort((int)config.get(ConfigurationReader.PORT));
		gopherServerProps.setCorePoolSize((int)config.get(ConfigurationReader.CORE_POOL_SIZE));
		gopherServerProps.setMaxPoolSize((int)config.get(ConfigurationReader.MAX_POOL_SIZE));
		gopherServerProps.setKeepAliveTime((int)config.get(ConfigurationReader.POOL_KEEP_ALIVE_TIME));
		gopherServerProps.setKeepAliveUnits(TimeUnit.MILLISECONDS);

		gopherServer = new RedGopherServer(gopherServerProps);

		gopherServer.start();
	}

	private static void startAdminServer() {
		//XXX: This is likely going away
		RedGopherServerProperties adminServerProps = new RedGopherServerProperties();
		adminServerProps.setPort(10);
		adminServerProps.setCorePoolSize((int)config.get(ConfigurationReader.CORE_POOL_SIZE));
		adminServerProps.setMaxPoolSize((int)config.get(ConfigurationReader.MAX_POOL_SIZE));
		adminServerProps.setKeepAliveTime((int)config.get(ConfigurationReader.POOL_KEEP_ALIVE_TIME));
		adminServerProps.setKeepAliveUnits(TimeUnit.MILLISECONDS);

		adminServer = new RedGopherAdminServer(adminServerProps, gopherServer);

		adminServer.start();
	}

}
