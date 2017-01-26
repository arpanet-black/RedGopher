package black.arpanet.gopher.db;

import static black.arpanet.gopher.util.RedGopherLogUtil.d;
import static black.arpanet.gopher.util.RedGopherLogUtil.e;
import static black.arpanet.gopher.util.RedGopherLogUtil.w;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class GophermapDbLoader {

	private static final String INFO_ELEMENT_NAME = "info";
	private static final String ITEM_ELEMENT_NAME = "item";
	private static final String ERROR_ELEMENT_NAME = "error";
	private static final String GOPHER_TYPE_ELEMENT_NAME = "gopherType";
	private static final String RESOURCE_TYPE_ELEMENT_NAME = "resourceType";
	private static final String DISPLAY_TEXT_ELEMENT_NAME = "displayText";
	private static final String SERVER_ELEMENT_NAME = "server";
	private static final String PORT_ELEMENT_NAME = "port";
	private static final String GOPHER_PATH_ELEMENT_NAME = "gopherPath";
	private static final String PARENT_PATH_ELEMENT_NAME = "parentPath";
	private static final String LOCAL_PATH_ELEMENT_NAME = "localPath";
	private static final String CONTENT_ELEMENT_NAME = "content";
	private static final String PERSISTENT_ELEMENT_NAME = "persistent";
	
	private static final Logger LOG = LogManager.getLogger(GophermapDbLoader.class);

	public static void loadGophermap(File f, String defaultDir, String defaultDomain, int defaultPort) {

		d(LOG, String.format("Parsing gophermap: %s", f.getAbsolutePath()));
		SAXBuilder builder = new SAXBuilder();

		try(ByteArrayInputStream bais = new ByteArrayInputStream(Files.readAllBytes(f.toPath()));) {

			Document doc = builder.build(bais);
			Element root = doc.getRootElement();

 			for(Element el : root.getChildren()) {
 				d(LOG, "PARSING ELEMENT! " + el.getName());
				if(el.getName().equals(INFO_ELEMENT_NAME)) {
					//Parse element into an informational string
					RedGopherDbManager.createGopherItemWithTrans(GopherResourceType.INFORMATION_TEXT,
							ServerResourceType.VIRTUAL_FILE,
							null, null, el.getValue(), null,
							defaultDomain, defaultPort, false, defaultDir);
					
				} else if(el.getName().equals(ITEM_ELEMENT_NAME)) {					
					
					GopherResourceType grt = GopherResourceType.valueOf(el.getChild(GOPHER_TYPE_ELEMENT_NAME).getValue());
					ServerResourceType srt = ServerResourceType.valueOf(el.getChild(RESOURCE_TYPE_ELEMENT_NAME).getValue());
					String displayText = el.getChild(DISPLAY_TEXT_ELEMENT_NAME).getValue();
					String server = el.getChild(SERVER_ELEMENT_NAME) != null ? el.getChild(SERVER_ELEMENT_NAME).getValue() : defaultDomain;
					int port = el.getChild(PORT_ELEMENT_NAME) != null ? Integer.valueOf(el.getChild(PORT_ELEMENT_NAME).getValue()) : defaultPort;					
					String parentPath = el.getChild(PARENT_PATH_ELEMENT_NAME) != null ? el.getChild(PARENT_PATH_ELEMENT_NAME).getValue() : defaultDir;
					String localPath = el.getChild(LOCAL_PATH_ELEMENT_NAME) != null ? el.getChild(LOCAL_PATH_ELEMENT_NAME).getValue() : defaultDir;
					String gopherPath = el.getChild(GOPHER_PATH_ELEMENT_NAME) != null ? el.getChild(GOPHER_PATH_ELEMENT_NAME).getValue() : null;
					byte[] content = el.getChild(CONTENT_ELEMENT_NAME) != null ? el.getChild(CONTENT_ELEMENT_NAME).getValue().getBytes() : null;					
					boolean persistent = el.getChild(PERSISTENT_ELEMENT_NAME) != null ? Boolean.valueOf(el.getChild(PERSISTENT_ELEMENT_NAME).getValue()) : false;
					
					if(srt.equals(ServerResourceType.LOCAL_FILE)) {
						localPath = new File(f.getParent() + "/" + localPath).toURI().toString();
					}
					
					//If the gopherPath was not set for a local directory
					//then build the directory gopher name
					if(srt.equals(ServerResourceType.LOCAL_DIRECTORY)
							&& gopherPath == null) {
						localPath += "/";
						gopherPath = localPath;
					}
					
					//Parse element into an informational string
					RedGopherDbManager.createGopherItemWithTrans(grt, srt,
							gopherPath,
							localPath,
							displayText, content, 
							server, 
							port,
							persistent, 
							(StringUtils.isNotBlank(parentPath) ? parentPath : defaultDir));
					
				} else if(el.getName().equals(ERROR_ELEMENT_NAME)) {
					//Parse element into an error string
					RedGopherDbManager.createGopherItemWithTrans(GopherResourceType.ERROR,
							ServerResourceType.VIRTUAL_FILE,
							defaultDir, defaultDir, el.getValue(), null,
							defaultDomain, defaultPort, false, defaultDir);
				} else {
					w(LOG, String.format("Unknown element encountered parsing xml file - Element: %s, File: %s", el.getName(), f.getAbsolutePath()));
				}
			}

		} catch (IOException ioe) {
			e(LOG,String.format("IOException encountered parsing gophermap: ",f.getAbsolutePath()),ioe);
		} catch (JDOMException jdome) {
			e(LOG,String.format("JDOMException encountered parsing gophermap: ",f.getAbsolutePath()),jdome);
		}
	}
}

