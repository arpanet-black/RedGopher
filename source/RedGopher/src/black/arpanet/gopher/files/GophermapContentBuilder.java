package black.arpanet.gopher.files;

import static black.arpanet.gopher.util.RedGopherLogUtil.d;
import static black.arpanet.gopher.util.RedGopherLogUtil.e;
import static black.arpanet.gopher.util.RedGopherLogUtil.i;
import static black.arpanet.gopher.util.RedGopherLogUtil.t;
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

import black.arpanet.gopher.db.GopherItemBuilder;
import black.arpanet.gopher.db.RedGopherDbManager;
import black.arpanet.gopher.db.entities.GopherItem;
import black.arpanet.gopher.util.DirectoryFilter;
import black.arpanet.gopher.util.GophermapFileNameFilter;

public class GophermapContentBuilder implements ContentBuilder {

	private static final String PATH_SEP = "/";
	
	private static final String INFO_ELEMENT_NAME = "info";
	private static final String ERROR_ELEMENT_NAME = "error";
	private static final String DIRECTORY_ELEMENT_NAME = "directory";
	private static final String TEXT_FILE_ELEMENT_NAME = "textFile";
	private static final String BINARY_FILE_ELEMENT_NAME = "binaryFile";
	private static final String BINARY_ARCHIVE_FILE_ELEMENT_NAME = "binaryArchiveFile";
	private static final String IMAGE_ELEMENT_NAME = "image";
	private static final String HTML_FILE_ELEMENT_NAME = "htmlFile";	
	private static final String RSS2FEED_ELEMENT_NAME = "rss2Feed";
	private static final String V_DIRECTORY_ELEMENT_NAME = "virtualDirectory";
	private static final String V_TEXT_FILE_ELEMENT_NAME = "virtualTextFile";
	private static final String V_BINARY_FILE_ELEMENT_NAME = "virtualBinaryFile";
	private static final String V_BINARY_ARCHIVE_FILE_ELEMENT_NAME = "virtualBinaryArchiveFile";
	private static final String V_IMAGE_ELEMENT_NAME = "virtualImage";
	private static final String V_HTML_FILE_ELEMENT_NAME = "virtualHtmlFile";
//	private static final String R_DIRECTORY_ELEMENT_NAME = "remoteDirectory";
//	private static final String R_TEXT_FILE_ELEMENT_NAME = "remoteTextFile";
//	private static final String R_BINARY_FILE_ELEMENT_NAME = "remoteBinaryFile";
//	private static final String R_IMAGE_ELEMENT_NAME = "remoteImage";
//	private static final String R_HTML_FILE_ELEMENT_NAME = "remoteHtmlFile";
	
	private static final String DISPLAY_TEXT_ELEMENT_NAME = "displayText";
	private static final String SERVER_ELEMENT_NAME = "server";
	private static final String PORT_ELEMENT_NAME = "port";
	private static final String GOPHER_PATH_ELEMENT_NAME = "gopherPath";
	private static final String PARENT_PATH_ELEMENT_NAME = "parentPath";
	private static final String RESOURCE_PATH_ELEMENT_NAME = "resourcePath";
	private static final String PERSISTENT_ELEMENT_NAME = "persistent";
	private static final String IMAGE_TYPE_ELEMENT_NAME = "imageType";
	private static final String TEXT_CONTENT_ELEMENT_NAME = "textContent";	
	private static final String ESCAPED_HTML_ELEMENT_NAME = "escapedHtml";
	private static final String BASE64_ENCODED_CONTENT_ELEMENT_NAME = "base64EncodedContent";	
	
	private static final String DEFAULT_IMAGE_TYPE = "OTHER";
	
	private static final Logger LOG = LogManager.getLogger(GophermapContentBuilder.class);
	
	private GophermapFileNameFilter gophermapFileNameFilter;
	private DirectoryFilter directoryFilter;
	private String gopherMapFile;
	private String domainName;
	private int port;
	
	public GophermapContentBuilder(String domainName, int port, String gopherMapFile) {
		this.gopherMapFile = gopherMapFile;
		this.domainName = domainName;
		this.port = port;
		directoryFilter = new DirectoryFilter();
		gophermapFileNameFilter = new GophermapFileNameFilter(gopherMapFile);
	}
	
	public boolean initFromContentDirectory(String contentDirectoryPath) {
		i(LOG,"Initializing from Gophermap.");
		i(LOG,String.format("Content directory is: %s", contentDirectoryPath));
		i(LOG,String.format("Gophermap is: %s", gopherMapFile));

		File contentDirectory = new File(contentDirectoryPath);

		if(!contentDirectory.exists()) {
			i(LOG, String.format("Creating content directory: %s", contentDirectory.getAbsolutePath()));
			contentDirectory.mkdir();
		}	

		parseGopherDirectories(contentDirectory, PATH_SEP);

		RedGopherDbManager.checkpointDb();

		return true;
	}
	
	private void parseGopherDirectories(File localDir, String currentGopherDir) {

		//Parse the gophermap file from THIS directory
		for(File gopherMap :  localDir.listFiles(gophermapFileNameFilter)) {
			loadGophermap(gopherMap, currentGopherDir);
		}

		//Look in subdirectories
		for(File localSubDir : localDir.listFiles(directoryFilter)) {
			String gopherSubDir = currentGopherDir + localSubDir.getName() + PATH_SEP;
			parseGopherDirectories(localSubDir, gopherSubDir);
		}
	}

	private void loadGophermap(File gopherMap, String currentGopherDir) {

		d(LOG, String.format("Parsing gophermap: %s", gopherMap.getAbsolutePath()));
		
		try(ByteArrayInputStream bais = new ByteArrayInputStream(Files.readAllBytes(gopherMap.toPath()));) {

			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(bais);
			Element root = doc.getRootElement();

 			for(Element el : root.getChildren()) {
 				t(LOG, "Parsing gophermap element: " + el.getName()); 
 				
 				GopherItem item = null;
 				boolean persistent = el.getChild(PERSISTENT_ELEMENT_NAME) != null ? Boolean.valueOf(el.getChild(PERSISTENT_ELEMENT_NAME).getValue()) : false;
 				String displayText = el.getChild(DISPLAY_TEXT_ELEMENT_NAME) != null ? el.getChild(DISPLAY_TEXT_ELEMENT_NAME).getValue() : "[" + el.getName() + "]";
 				
				if(el.getName().equals(INFO_ELEMENT_NAME)) {
					item = fromInfoElement(currentGopherDir, el, persistent);
				} else if(el.getName().equals(ERROR_ELEMENT_NAME)) {
					item = fromErrorElement(currentGopherDir, el, persistent);
				} else if(el.getName().equals(DIRECTORY_ELEMENT_NAME)) {					
					item = fromDirectoryElement(currentGopherDir, el, persistent, displayText);
				} else if(el.getName().equals(TEXT_FILE_ELEMENT_NAME)) {					
					item = fromTextFileElement(gopherMap, currentGopherDir, el, persistent, displayText);
				} else if(el.getName().equals(BINARY_FILE_ELEMENT_NAME)) {
					item = fromBinaryFileElement(gopherMap, currentGopherDir, el, persistent, displayText);
				}  else if(el.getName().equals(IMAGE_ELEMENT_NAME)) {
					item = fromImageElement(gopherMap, currentGopherDir, el, persistent, displayText);
				}  else if(el.getName().equals(HTML_FILE_ELEMENT_NAME)) {
					item = fromHtmlFileElement(gopherMap, currentGopherDir, el, persistent, displayText);
				}  else if(el.getName().equals(RSS2FEED_ELEMENT_NAME)) {
					item = fromRss2FeedElement(gopherMap, currentGopherDir, el, persistent, displayText);
				}  else if(el.getName().equals(V_DIRECTORY_ELEMENT_NAME)) {
					item = fromVirtualDirectoryElement(currentGopherDir, el, persistent, displayText);
				}  else if(el.getName().equals(V_TEXT_FILE_ELEMENT_NAME)) {
					item = fromVirtualTextFileElement(gopherMap, currentGopherDir, el, persistent, displayText);
				}  else if(el.getName().equals(V_BINARY_FILE_ELEMENT_NAME)) {
					item = fromVirtualBinaryFileElement(gopherMap, currentGopherDir, el, persistent, displayText);
				}  else if(el.getName().equals(V_IMAGE_ELEMENT_NAME)) {
					item = fromVirtualImageElement(gopherMap, currentGopherDir, el, persistent, displayText);
				}  else if(el.getName().equals(V_HTML_FILE_ELEMENT_NAME)) {
					item = fromVirtualHtmlFileElement(gopherMap, currentGopherDir, el, persistent, displayText);
				}  else {
					w(LOG, String.format("Unknown element encountered parsing gophermap xml file - Element: %s, File: %s", el.getName(), gopherMap.getAbsolutePath()));
				}
				
				if(item != null) {
					RedGopherDbManager.mergeGopherItem(item);
				} else {
					w(LOG, String.format("Gopher Item not persisted - Element: %s, File: %s", el.getName(), gopherMap.getAbsolutePath()));
				}
			}

		} catch (IOException | JDOMException ex) {
			e(LOG,String.format("Exception encountered parsing gophermap: ",gopherMap.getAbsolutePath()),ex);
		}
	}

	private GopherItem fromInfoElement(String currentGopherDir, Element el, boolean persistent) {
		return GopherItemBuilder.buildInfo(el.getValue(), currentGopherDir, persistent);
	}

	private GopherItem fromErrorElement(String currentGopherDir, Element el, boolean persistent) {
		return GopherItemBuilder.buildError(el.getValue(), currentGopherDir, persistent);
	}

	private GopherItem fromDirectoryElement(String currentGopherDir, Element el, boolean persistent, String displayText) {
		
		String itemDomain = el.getChild(SERVER_ELEMENT_NAME) != null ? el.getChild(SERVER_ELEMENT_NAME).getValue() : domainName;
		int itemPort = el.getChild(PORT_ELEMENT_NAME) != null ? Integer.valueOf(el.getChild(PORT_ELEMENT_NAME).getValue()) : port;					
		String resourcePath = el.getChild(RESOURCE_PATH_ELEMENT_NAME) != null ? el.getChild(RESOURCE_PATH_ELEMENT_NAME).getValue() : currentGopherDir;
		String gopherPath = resourcePath;
		
		return GopherItemBuilder.buildDirectory(displayText, currentGopherDir,itemDomain, itemPort, resourcePath, gopherPath, persistent);
	}

	private GopherItem fromTextFileElement(File gopherMap, String currentGopherDir, Element el, boolean persistent, String displayText) {
		
		String itemDomain = el.getChild(SERVER_ELEMENT_NAME) != null ? el.getChild(SERVER_ELEMENT_NAME).getValue() : domainName;
		int itemPort = el.getChild(PORT_ELEMENT_NAME) != null ? Integer.valueOf(el.getChild(PORT_ELEMENT_NAME).getValue()) : port;
		String gopherPath = el.getChild(GOPHER_PATH_ELEMENT_NAME) != null ? el.getChild(GOPHER_PATH_ELEMENT_NAME).getValue() : null;
		String resourcePath = el.getChild(RESOURCE_PATH_ELEMENT_NAME) != null ? el.getChild(RESOURCE_PATH_ELEMENT_NAME).getValue() : currentGopherDir;
		String parentPath = el.getChild(PARENT_PATH_ELEMENT_NAME) != null ? el.getChild(PARENT_PATH_ELEMENT_NAME).getValue() : currentGopherDir;
		
		//Prepend current directory to resource path
		resourcePath = new File(gopherMap.getPath() + PATH_SEP + resourcePath).toURI().toString();
		
		return GopherItemBuilder.buildTextFile(displayText, gopherPath, resourcePath, parentPath, itemDomain, itemPort, persistent);
	}

	private GopherItem fromBinaryFileElement(File gopherMap, String currentGopherDir, Element el, boolean persistent, String displayText) {
		
		String itemDomain = el.getChild(SERVER_ELEMENT_NAME) != null ? el.getChild(SERVER_ELEMENT_NAME).getValue() : domainName;
		int itemPort = el.getChild(PORT_ELEMENT_NAME) != null ? Integer.valueOf(el.getChild(PORT_ELEMENT_NAME).getValue()) : port;
		String gopherPath = el.getChild(GOPHER_PATH_ELEMENT_NAME) != null ? el.getChild(GOPHER_PATH_ELEMENT_NAME).getValue() : null;
		String resourcePath = el.getChild(RESOURCE_PATH_ELEMENT_NAME) != null ? el.getChild(RESOURCE_PATH_ELEMENT_NAME).getValue() : currentGopherDir;					
		String parentPath = el.getChild(PARENT_PATH_ELEMENT_NAME) != null ? el.getChild(PARENT_PATH_ELEMENT_NAME).getValue() : currentGopherDir;
		
		//Prepend current directory to resource path
		resourcePath = new File(gopherMap.getPath() + PATH_SEP + resourcePath).toURI().toString();
				
		return GopherItemBuilder.buildBinaryFile(displayText, gopherPath, resourcePath, parentPath, itemDomain, itemPort, persistent);
	}
	
	private GopherItem fromImageElement(File gopherMap, String currentGopherDir, Element el, boolean persistent, String displayText) {
		
		String itemDomain = el.getChild(SERVER_ELEMENT_NAME) != null ? el.getChild(SERVER_ELEMENT_NAME).getValue() : domainName;
		int itemPort = el.getChild(PORT_ELEMENT_NAME) != null ? Integer.valueOf(el.getChild(PORT_ELEMENT_NAME).getValue()) : port;
		String gopherPath = el.getChild(GOPHER_PATH_ELEMENT_NAME) != null ? el.getChild(GOPHER_PATH_ELEMENT_NAME).getValue() : null;
		String resourcePath = el.getChild(RESOURCE_PATH_ELEMENT_NAME) != null ? el.getChild(RESOURCE_PATH_ELEMENT_NAME).getValue() : currentGopherDir;							
		String parentPath = el.getChild(PARENT_PATH_ELEMENT_NAME) != null ? el.getChild(PARENT_PATH_ELEMENT_NAME).getValue() : currentGopherDir;
		String imageType = el.getChild(IMAGE_TYPE_ELEMENT_NAME) != null ? el.getChild(IMAGE_TYPE_ELEMENT_NAME).getValue() : DEFAULT_IMAGE_TYPE;
		
		//Prepend current directory to resource path
		resourcePath = new File(gopherMap.getPath() + PATH_SEP + resourcePath).toURI().toString();
		
		return GopherItemBuilder.buildImage(displayText, gopherPath, resourcePath, parentPath, imageType, itemDomain, itemPort, persistent);
	}
	
	private GopherItem fromHtmlFileElement(File gopherMap, String currentGopherDir, Element el, boolean persistent, String displayText) {

		String itemDomain = el.getChild(SERVER_ELEMENT_NAME) != null ? el.getChild(SERVER_ELEMENT_NAME).getValue() : domainName;
		int itemPort = el.getChild(PORT_ELEMENT_NAME) != null ? Integer.valueOf(el.getChild(PORT_ELEMENT_NAME).getValue()) : port;
		String gopherPath = el.getChild(GOPHER_PATH_ELEMENT_NAME) != null ? el.getChild(GOPHER_PATH_ELEMENT_NAME).getValue() : null;
		String resourcePath = el.getChild(RESOURCE_PATH_ELEMENT_NAME) != null ? el.getChild(RESOURCE_PATH_ELEMENT_NAME).getValue() : currentGopherDir;					
		String parentPath = el.getChild(PARENT_PATH_ELEMENT_NAME) != null ? el.getChild(PARENT_PATH_ELEMENT_NAME).getValue() : currentGopherDir;
		
		//Prepend current directory to resource path
		resourcePath = new File(gopherMap.getPath() + PATH_SEP + resourcePath).toURI().toString();
		
		return GopherItemBuilder.buildHtmlFile(displayText, gopherPath, resourcePath, parentPath, itemDomain, itemPort, persistent);
	}
	
	private GopherItem fromRss2FeedElement(File gopherMap, String currentGopherDir, Element el, boolean persistent, String displayText) {
		
		String itemDomain = el.getChild(SERVER_ELEMENT_NAME) != null ? el.getChild(SERVER_ELEMENT_NAME).getValue() : domainName;
		int itemPort = el.getChild(PORT_ELEMENT_NAME) != null ? Integer.valueOf(el.getChild(PORT_ELEMENT_NAME).getValue()) : port;
		String gopherPath = el.getChild(GOPHER_PATH_ELEMENT_NAME) != null ? el.getChild(GOPHER_PATH_ELEMENT_NAME).getValue() : null;
		String resourcePath = el.getChild(RESOURCE_PATH_ELEMENT_NAME) != null ? el.getChild(RESOURCE_PATH_ELEMENT_NAME).getValue() : currentGopherDir;					
		String parentPath = el.getChild(PARENT_PATH_ELEMENT_NAME) != null ? el.getChild(PARENT_PATH_ELEMENT_NAME).getValue() : currentGopherDir;
		
		//Prepend current directory to resource path
		resourcePath = new File(gopherMap.getPath() + PATH_SEP + resourcePath).toURI().toString();
				
		return null; //GopherItemBuilder.buildHtmlFile(displayText, gopherPath, resourcePath, parentPath, itemDomain, itemPort, persistent);
	}
	
	private GopherItem fromVirtualDirectoryElement(String currentGopherDir, Element el, boolean persistent, String displayText) {
		GopherItem item = null;
//		String displayText = el.getChild(DISPLAY_TEXT_ELEMENT_NAME).getValue();
//		String resourcePath = el.getChild(RESOURCE_PATH_ELEMENT_NAME) != null ? el.getChild(RESOURCE_PATH_ELEMENT_NAME).getValue() : currentGopherDir;
//		String gopherPath = resourcePath;
//		item = GopherItemBuilder.buildVirtualDirectory(displayText, currentGopherDir, resourcePath, gopherPath, domainName, port, persistent);
		return item;
	}

	private GopherItem fromVirtualTextFileElement(File gopherMap, String currentGopherDir, Element el, boolean persistent, String displayText) {
		
		String gopherPath = el.getChild(GOPHER_PATH_ELEMENT_NAME) != null ? el.getChild(GOPHER_PATH_ELEMENT_NAME).getValue() : null;		
		String parentPath = el.getChild(PARENT_PATH_ELEMENT_NAME) != null ? el.getChild(PARENT_PATH_ELEMENT_NAME).getValue() : currentGopherDir;
		String content = el.getChild(TEXT_CONTENT_ELEMENT_NAME) != null ? el.getChild(TEXT_CONTENT_ELEMENT_NAME).getValue() : "";
		
		return GopherItemBuilder.buildVirtualTextFile(displayText, gopherPath, parentPath, content, domainName, port, persistent);
	}

	private GopherItem fromVirtualBinaryFileElement(File gopherMap, String currentGopherDir, Element el, boolean persistent, String displayText) {
				
		String gopherPath = el.getChild(GOPHER_PATH_ELEMENT_NAME) != null ? el.getChild(GOPHER_PATH_ELEMENT_NAME).getValue() : null;				
		String parentPath = el.getChild(PARENT_PATH_ELEMENT_NAME) != null ? el.getChild(PARENT_PATH_ELEMENT_NAME).getValue() : currentGopherDir;
		String base64Content = el.getChild(BASE64_ENCODED_CONTENT_ELEMENT_NAME) != null ? el.getChild(BASE64_ENCODED_CONTENT_ELEMENT_NAME).getValue() : "";
		
		byte[] bytes = null;
		
		if(StringUtils.isNotBlank(base64Content)) {
			bytes = org.apache.commons.codec.binary.Base64.decodeBase64(base64Content);
		}
		
		return GopherItemBuilder.buildVirtualBinaryFile(displayText, gopherPath, parentPath, bytes, domainName, port, persistent);
	}
	
	private GopherItem fromVirtualImageElement(File gopherMap, String currentGopherDir, Element el, boolean persistent, String displayText) {

		String itemDomain = el.getChild(SERVER_ELEMENT_NAME) != null ? el.getChild(SERVER_ELEMENT_NAME).getValue() : domainName;
		int itemPort = el.getChild(PORT_ELEMENT_NAME) != null ? Integer.valueOf(el.getChild(PORT_ELEMENT_NAME).getValue()) : port;
		String gopherPath = el.getChild(GOPHER_PATH_ELEMENT_NAME) != null ? el.getChild(GOPHER_PATH_ELEMENT_NAME).getValue() : null;
		String parentPath = el.getChild(PARENT_PATH_ELEMENT_NAME) != null ? el.getChild(PARENT_PATH_ELEMENT_NAME).getValue() : currentGopherDir;
		String imageType = el.getChild(IMAGE_TYPE_ELEMENT_NAME) != null ? el.getChild(IMAGE_TYPE_ELEMENT_NAME).getValue() : DEFAULT_IMAGE_TYPE;
				
		byte[] bytes = null;
		
		if(el.getChild(BASE64_ENCODED_CONTENT_ELEMENT_NAME) != null ) {
			String base64Content = el.getChild(BASE64_ENCODED_CONTENT_ELEMENT_NAME).getValue();
			if(StringUtils.isNotBlank(base64Content)) {
				bytes = org.apache.commons.codec.binary.Base64.decodeBase64(base64Content);
			}
			
		} else if(el.getChild(ESCAPED_HTML_ELEMENT_NAME) != null ) {
			String escapedContent = el.getChild(BASE64_ENCODED_CONTENT_ELEMENT_NAME).getValue();
			if(StringUtils.isNotBlank(escapedContent)) {
				bytes = escapedContent.getBytes();
			}
		}
		
		return GopherItemBuilder.buildVirtualImage(displayText, gopherPath, parentPath, imageType, bytes, itemDomain, itemPort, persistent);
	}
	
	private GopherItem fromVirtualHtmlFileElement(File gopherMap, String currentGopherDir, Element el, boolean persistent, String displayText) {
		
		String itemDomain = el.getChild(SERVER_ELEMENT_NAME) != null ? el.getChild(SERVER_ELEMENT_NAME).getValue() : domainName;
		int itemPort = el.getChild(PORT_ELEMENT_NAME) != null ? Integer.valueOf(el.getChild(PORT_ELEMENT_NAME).getValue()) : port;
		String gopherPath = el.getChild(GOPHER_PATH_ELEMENT_NAME) != null ? el.getChild(GOPHER_PATH_ELEMENT_NAME).getValue() : null;	
		String parentPath = el.getChild(PARENT_PATH_ELEMENT_NAME) != null ? el.getChild(PARENT_PATH_ELEMENT_NAME).getValue() : currentGopherDir;
				
		byte[] bytes = null;
		
		if(el.getChild(BASE64_ENCODED_CONTENT_ELEMENT_NAME) != null ) {
			String base64Content = el.getChild(BASE64_ENCODED_CONTENT_ELEMENT_NAME).getValue();
			if(StringUtils.isNotBlank(base64Content)) {
				bytes = org.apache.commons.codec.binary.Base64.decodeBase64(base64Content);
			}
			
		} else if(el.getChild(ESCAPED_HTML_ELEMENT_NAME) != null ) {
			String escapedContent = el.getChild(BASE64_ENCODED_CONTENT_ELEMENT_NAME).getValue();
			if(StringUtils.isNotBlank(escapedContent)) {
				bytes = escapedContent.getBytes();
			}
		}

		return GopherItemBuilder.buildVirtualHtmlFile(displayText, gopherPath, parentPath, bytes, itemDomain, itemPort, persistent);
	}
}