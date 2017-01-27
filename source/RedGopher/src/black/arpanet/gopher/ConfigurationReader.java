package black.arpanet.gopher;

import static black.arpanet.util.logging.ArpanetLogUtil.t;
import static black.arpanet.util.logging.ArpanetLogUtil.e;
import static black.arpanet.util.logging.ArpanetLogUtil.w;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class ConfigurationReader {
	
	public static final String MONITOR_CLASSES = "MONITOR_CLASSES";
	public static final String DOMAIN_NAME = "DOMAIN_NAME";
	public static final String PORT = "PORT";
	public static final String CONTENT_DIR = "CONTENT_DIR";
	public static final String INIT_DB = "INIT_DB";
	public static final String GOPHERMAP_FILE_NAME = "GOPHERMAP_FILE_NAME";
	public static final String CORE_POOL_SIZE = "CORE_POOL_SIZE";
	public static final String MAX_POOL_SIZE = "MAX_POOL_SIZE";
	public static final String POOL_KEEP_ALIVE_TIME = "POOL_KEEP_ALIVE_TIME";
	
	public static final int DEFAULT_PORT = 70;
	public static final String DEFAULT_CONTENT_DIR = "/gopher";
	public static final boolean DEFAULT_INIT_DB = false;
	public static final String DEFAULT_GOPHERMAP_FILE_NAME = "gophermap.xml";
	public static final int DEFAULT_CORE_POOL_SIZE = 5;
	public static final int DEFAULT_MAX_POOL_SIZE = 50;
	public static final int DEFAULT_POOL_KEEP_ALIVE_TIME = 2000;
	
	private static final String SETTINGS_ELEMENT = "settings";
	private static final String CONTENT_ELEMENT = "content";
	private static final String DOMAIN_NAME_ELEMENT = "domainName";
	private static final String PORT_ELEMENT = "port";
	private static final String CONTENT_DIR_ELEMENT = "contentDirectory";
	private static final String INIT_DB_ELEMENT = "intializeDb";	
	private static final String GOPHERMAP_FILE_NAME_ELEMENT = "gophermapFileName";
	private static final String CORE_POOL_SIZE_ELEMENT = "coreThreadPoolSize";
	private static final String MAX_POOL_SIZE_ELEMENT = "maxThreadPoolSize";	
	private static final String POOL_KEEP_ALIVE_TIME_ELEMENT = "threadPoolKeepAliveTimeMs";
	private static final String CONTENT_MONITOR_ELEMENT = "monitor";
	private static final String CLASS_NAME_ELEMENT = "className";
	private static final String PARAM_ELEMENT = "param";
	private static final String KEY_ELEMENT = "key";
	private static final String VALUE_ELEMENT = "value";
	
	private static final Logger LOG = LogManager.getLogger(ConfigurationReader.class);

	public static Map<String,Object> readConfig(String configFile) {
		
		Map<String,Object> config = null;
		
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new File(configFile));
			Element root = doc.getRootElement();
			
			config = new HashMap<String,Object>();
			
			Element settings = root.getChild(SETTINGS_ELEMENT);
			
			if(settings != null) {
				
				//Domain name is required
				Element configEl = settings.getChild(DOMAIN_NAME_ELEMENT);
				
				if(configEl == null || StringUtils.isBlank(configEl.getValue())) {
					w(LOG, "Domain name is required in configuration.");
					return null;
				}
				
				config.put(DOMAIN_NAME, configEl.getValue());
				
				//Port is optional - default is 70
				configEl = settings.getChild(PORT_ELEMENT);
				
				if(configEl == null || StringUtils.isBlank(configEl.getValue())) {
					config.put(PORT, DEFAULT_PORT);
				} else {
					config.put(PORT, Integer.valueOf(configEl.getValue()));
				}
				
				//Content dir is optional - default is /gopher
				configEl = settings.getChild(CONTENT_DIR_ELEMENT);
				
				if(configEl == null || StringUtils.isBlank(configEl.getValue())) {
					config.put(CONTENT_DIR, DEFAULT_CONTENT_DIR);
				} else {
					config.put(CONTENT_DIR, configEl.getValue());
				}
				
				//Init db is optional - default is false
				configEl = settings.getChild(INIT_DB_ELEMENT);
				
				if(configEl == null || StringUtils.isBlank(configEl.getValue())) {
					config.put(INIT_DB, DEFAULT_INIT_DB);
				} else {
					config.put(INIT_DB, Boolean.valueOf(configEl.getValue()));
				}
				
				//Gophermap file name is optional - default is gophermap.xml
				configEl = settings.getChild(GOPHERMAP_FILE_NAME_ELEMENT);
				
				if(configEl == null || StringUtils.isBlank(configEl.getValue())) {
					config.put(GOPHERMAP_FILE_NAME, DEFAULT_GOPHERMAP_FILE_NAME);
				} else {
					config.put(GOPHERMAP_FILE_NAME, configEl.getValue());
				}
				
				//Core pool size is optional - default is 10
				configEl = settings.getChild(CORE_POOL_SIZE_ELEMENT);
				
				if(configEl == null || StringUtils.isBlank(configEl.getValue())) {
					config.put(CORE_POOL_SIZE, DEFAULT_CORE_POOL_SIZE);
				} else {
					config.put(CORE_POOL_SIZE, Integer.valueOf(configEl.getValue()));
				}
				
				//Max pool size is optional - default is 50
				configEl = settings.getChild(MAX_POOL_SIZE_ELEMENT);
				
				if(configEl == null || StringUtils.isBlank(configEl.getValue())) {
					config.put(MAX_POOL_SIZE, DEFAULT_MAX_POOL_SIZE);
				} else {
					config.put(MAX_POOL_SIZE, Integer.valueOf(configEl.getValue()));
				}
				
				//Thread pool keep alive time is optional - default is 2000
				configEl = settings.getChild(POOL_KEEP_ALIVE_TIME_ELEMENT);
				
				if(configEl == null || StringUtils.isBlank(configEl.getValue())) {
					config.put(POOL_KEEP_ALIVE_TIME, DEFAULT_POOL_KEEP_ALIVE_TIME);
				} else {
					config.put(POOL_KEEP_ALIVE_TIME, Integer.valueOf(configEl.getValue()));
				}
				
				if(LOG.isTraceEnabled()) {
					t(LOG, "");
					t(LOG, "-------------------------------");
					t(LOG, "Server Configuration Settings: ");
					for(String key : config.keySet()) {
						t(LOG, String.format("%s: %s", key, config.get(key)));
					}
					t(LOG, "-------------------------------");
					t(LOG, "");
				}

			} else {
				w(LOG, String.format("Settings element could not be retrieved from config file: %s", configFile));
			}
			
			
			Element content = root.getChild(CONTENT_ELEMENT);
			
			if(content != null) {
				List<Element> monitorElements = content.getChildren(CONTENT_MONITOR_ELEMENT);
				
				if(monitorElements != null) {
					List<String> monitorClassNames = new ArrayList<String>(monitorElements.size());
					
					for(Element el : monitorElements) {
						
						monitorClassNames.add(el.getChildText(CLASS_NAME_ELEMENT));
						
						List<Element> paramElements = el.getChildren(PARAM_ELEMENT);
						
						if(paramElements != null) {
							Map<String,String> params = new HashMap<String,String>(paramElements.size());
							
							for(Element paramEl : paramElements) {
								String key = paramEl.getChildText(KEY_ELEMENT);
								String value = paramEl.getChildText(VALUE_ELEMENT);
								params.put(key, value);
							}
							
							config.put(el.getValue(), params);
						}
						
					}
					
					config.put(MONITOR_CLASSES, monitorClassNames);
					
					if(LOG.isTraceEnabled()) {
						t(LOG, "");
						t(LOG, "-------------------------");
						t(LOG, "Server Content Monitors: ");
						for(String monitor : monitorClassNames) {
							t(LOG, monitor);
							
							@SuppressWarnings("unchecked")
							Map<String,String> params = (Map<String,String>)config.get(monitor);
							if(params != null) {
								t(LOG, " -- Monitor Params: ");
								for(String key : params.keySet()) {
									t(LOG, String.format(" -- %s: %s", key, params.get(key)));									
								}
							}
						}
						t(LOG, "-------------------------");
						t(LOG, "");
					}
				}
							
			} else {
				w(LOG, String.format("Content element could not be retrieved from config file: %s", configFile));
			}
			
		} catch (JDOMException | IOException ex) {
			e(LOG, String.format("Exception encountered reading configuration file: %s", configFile), ex);
		}
				
		return config;
	}
}

