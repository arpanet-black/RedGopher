package black.arpanet.gopher;

import static black.arpanet.util.ArpanetLogUtil.d;
import static black.arpanet.util.ArpanetLogUtil.e;
import static black.arpanet.util.ArpanetLogUtil.w;

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

public class ContentManagerFactory {
	
	private static final String CLASS_NAME_ELEMENT = "className";
	private static final Logger LOG = LogManager.getLogger(ContentManagerFactory.class);
	
	public static Set<ContentManager> getContentManagers(String configFile) {
		
		if(StringUtils.isBlank(configFile)) {
			throw new IllegalArgumentException("Config file cannot be empty");
		}
		
		Set<ContentManager> managers = new HashSet<ContentManager>();
		
		SAXBuilder saxBuilder = new SAXBuilder();
		
		try(InputStream inStream = ContentManagerFactory.class.getClassLoader().getResourceAsStream(configFile);) {
			
			Document d = saxBuilder.build(inStream);
			
			Element root = d.getRootElement();
			
			for(Element manager : root.getChildren()) {
				
				String className = manager.getChildText(CLASS_NAME_ELEMENT);
				try {
					Class<?> clazz = ContentManagerFactory.class.getClassLoader().loadClass(className);
					
					ContentManager newManager = (ContentManager)clazz.newInstance();
					
					managers.add(newManager);
					
					d(LOG, String.format("Content manager loaded: %s", className));
					
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					w(LOG, String.format("Could not load content manager class: %s", className), e);
				}
				
			}
			
		} catch (JDOMException | IOException e) {
			e(LOG, String.format("Excption encountered reading the content manager configuration file: %s", configFile), e);
		}
		
		return managers;
	}
}
