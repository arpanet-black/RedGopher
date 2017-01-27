package black.arpanet.gopher;

import static black.arpanet.util.logging.ArpanetLogUtil.d;
import static black.arpanet.util.logging.ArpanetLogUtil.e;
import static black.arpanet.util.logging.ArpanetLogUtil.w;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class ContentMonitorFactory {
	
	private static final String CLASS_NAME_ELEMENT = "className";
	private static final Logger LOG = LogManager.getLogger(ContentMonitorFactory.class);
	
	public static Set<ContentMonitor> getContentMonitors(String configFile) {
		
		if(StringUtils.isBlank(configFile)) {
			throw new IllegalArgumentException("Config file cannot be empty");
		}
		
		Set<ContentMonitor> loaders = new HashSet<ContentMonitor>();
		
		SAXBuilder saxBuilder = new SAXBuilder();
		
		try(InputStream inStream = ContentMonitorFactory.class.getClassLoader().getResourceAsStream(configFile);) {
			
			Document d = saxBuilder.build(inStream);
			
			Element root = d.getRootElement();
			
			for(Element loader : root.getChildren()) {
				
				String className = loader.getChildText(CLASS_NAME_ELEMENT);
				try {
					Class<?> clazz = ContentMonitorFactory.class.getClassLoader().loadClass(className);
					
					ContentMonitor newLoader = (ContentMonitor)clazz.newInstance();
					
					loaders.add(newLoader);
					
					d(LOG, String.format("Content loader loaded: %s", className));
					
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					w(LOG, String.format("Could not load content loader class: %s", className), e);
				}
				
			}
			
		} catch (JDOMException | IOException e) {
			e(LOG, String.format("Excption encountered reading the content loader configuration file: %s", configFile), e);
		}
		
		return loaders;
	}
}
