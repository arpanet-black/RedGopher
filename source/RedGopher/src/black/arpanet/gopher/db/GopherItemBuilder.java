package black.arpanet.gopher.db;

import org.apache.commons.lang3.StringUtils;

import black.arpanet.gopher.GopherResourceType;
import black.arpanet.gopher.ServerResourceType;
import black.arpanet.gopher.db.entities.GopherItem;
import black.arpanet.gopher.db.entities.ResourceDescriptor;

public class GopherItemBuilder  {
	
	private static final String IMAGE_TYPE_GIF = "GIF";
	private static final String IMAGE_TYPE_PNG = "PNG";

	public static GopherItem buildInfo(String displayText, String currentGopherDir, boolean persistent) {
		
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.INFORMATION_TEXT, ServerResourceType.VIRTUAL_FILE);
		
		GopherItem item = new GopherItem();

		item.setResourceDescriptor(resourceDescriptor);
		item.setDisplayText(displayText);
		item.setParentPath(currentGopherDir);
		item.setPersistOverRestart(persistent);
				
		return item;
	}
	
	public static GopherItem buildError(String displayText, String currentGopherDir, boolean persistent) {

		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.ERROR, ServerResourceType.VIRTUAL_FILE);

		GopherItem item = new GopherItem();

		item.setResourceDescriptor(resourceDescriptor);
		item.setDisplayText(displayText);
		item.setParentPath(currentGopherDir);
		item.setPersistOverRestart(persistent);

		return item;
	}
	
	public static GopherItem buildDirectory(String displayText, String currentGopherDir, String itemDomain, int itemPort, String resourcePath, String gopherPath, boolean persistent) {

		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.DIRECTORY, ServerResourceType.LOCAL_DIRECTORY);

		GopherItem item = new GopherItem();

		item.setResourceDescriptor(resourceDescriptor);
		item.setDisplayText(displayText);
		item.setGopherPath(gopherPath);
		item.setResourcePath(resourcePath);
		item.setParentPath(currentGopherDir);
		item.setDomainName(itemDomain);
		item.setPort(itemPort);		
		item.setPersistOverRestart(persistent);

		return item;
	}
	
	public static GopherItem buildTextFile(String displayText, String gopherPath, String resourcePath, String parentPath, String domainName, int port, boolean persistent) {

		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.TEXT_FILE, ServerResourceType.LOCAL_FILE);
		
		GopherItem item = new GopherItem();

		item.setResourceDescriptor(resourceDescriptor);
		item.setDisplayText(displayText);
		item.setGopherPath(gopherPath);
		item.setResourcePath(resourcePath);
		item.setParentPath(parentPath);
		item.setDomainName(domainName);		
		item.setPort(port);
		item.setPersistOverRestart(persistent);

		return item;
		
	}

	public static GopherItem buildBinaryFile(String displayText, String gopherPath, String resourcePath, String parentPath, String domainName, int port, boolean persistent) {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.BINARY_FILE, ServerResourceType.LOCAL_FILE);
		
		GopherItem item = buildGopherItem(displayText, gopherPath, resourcePath, parentPath, domainName, port,
				persistent, resourceDescriptor);
		
		return item;
		
	}
	
	public static GopherItem buildBinaryArchive(String displayText, String gopherPath, String resourcePath, String parentPath, String domainName, int port, boolean persistent) {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.BIN_ARCHIVE, ServerResourceType.LOCAL_FILE);
		
		GopherItem item = buildGopherItem(displayText, gopherPath, resourcePath, parentPath, domainName, port,
				persistent, resourceDescriptor);
		
		return item;
		
	}

	public static GopherItem buildImage(String displayText, String gopherPath, String resourcePath, String parentPath, String imageType, String domainName, int port, boolean persistent) {
		
		GopherResourceType grt = GopherResourceType.IMAGE_FILE;
		
		if(imageType.equals(IMAGE_TYPE_GIF)) {
			grt = GopherResourceType.GIF_GRAPHICS_FILE;
		} else if(imageType.equals(IMAGE_TYPE_PNG)) {
			grt = GopherResourceType.PNG_IMAGE_FILE;
		}
		
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(grt, ServerResourceType.LOCAL_FILE);
		
		GopherItem item = buildGopherItem(displayText, gopherPath, resourcePath, parentPath, domainName, port,
				persistent, resourceDescriptor);
		
		return item;
	}

	public static GopherItem buildHtmlFile(String displayText, String gopherPath, String resourcePath, String parentPath, String domainName, int port, boolean persistent) {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.HTML, ServerResourceType.LOCAL_FILE);
		
		GopherItem item = buildGopherItem(displayText, gopherPath, resourcePath, parentPath, domainName, port,
				persistent, resourceDescriptor);
		
		return item;
		
	}
	
	public static GopherItem buildSearch(String displayText, String gopherPath, String resourcePath, String parentPath, String domainName, int port, boolean persistent) {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.INDEX_SEARCH_SERVER, ServerResourceType.SEARCH);
		
		GopherItem item = buildGopherItem(displayText, gopherPath, resourcePath, parentPath, domainName, port,
				persistent, resourceDescriptor);
		
		return item;
		
	}

	public static GopherItem buildRss2Link(String displayText, String gopherPath, String resourcePath, String parentPath, String domainName, int port, boolean persistent) {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.DIRECTORY, ServerResourceType.RSS2_LINK);
		
		GopherItem item = buildGopherItem(displayText, gopherPath, resourcePath, parentPath, domainName, port,
				persistent, resourceDescriptor);
		
		return item;
		
	}

	public static GopherItem buildVirtualDirectory() {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.DIRECTORY, ServerResourceType.VIRTUAL_DIRECTORY);
		
		GopherItem item = new GopherItem();
		
		item.setResourceDescriptor(resourceDescriptor);
		
		//TODO: Set props?
		
		return item;
		
	}

	public static GopherItem buildVirtualTextFile(String displayText, String gopherPath, String parentPath, String content, String domainName, int port, boolean persistent) {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.TEXT_FILE, ServerResourceType.VIRTUAL_FILE);
		
		GopherItem item = new GopherItem();
		
		item.setResourceDescriptor(resourceDescriptor);
		
		item.setResourceDescriptor(resourceDescriptor);
		item.setDisplayText(displayText);
		item.setGopherPath(gopherPath);
		item.setParentPath(parentPath);		
		item.setDomainName(domainName);
		item.setPort(port);		
		item.setPersistOverRestart(persistent);
		item.setContent(content.getBytes());
		
		return item;
		
	}

	public static GopherItem buildVirtualBinaryFile(String displayText, String gopherPath, String parentPath, byte[] bytes, String domainName, int port, boolean persistent) {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.BINARY_FILE, ServerResourceType.VIRTUAL_FILE);
		
		GopherItem item = buildVirtualGopherItem(displayText, gopherPath, parentPath, bytes, domainName, port, persistent,
				resourceDescriptor);
		
		return item;
		
	}
	
	public static GopherItem buildVirtualBinaryArchive(String displayText, String gopherPath, String parentPath, byte[] bytes, String domainName, int port, boolean persistent) {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.BIN_ARCHIVE, ServerResourceType.VIRTUAL_FILE);
		
		GopherItem item = buildVirtualGopherItem(displayText, gopherPath, parentPath, bytes, domainName, port, persistent,
				resourceDescriptor);
		
		return item;
		
	}

	public static GopherItem buildVirtualImage(String displayText, String gopherPath, String parentPath, String imageType, byte[] bytes, String domainName, int port, boolean persistent) {

		GopherResourceType grt = GopherResourceType.IMAGE_FILE;
		
		if(imageType.equals(IMAGE_TYPE_GIF)) {
			grt = GopherResourceType.GIF_GRAPHICS_FILE;
		} else if(imageType.equals(IMAGE_TYPE_PNG)) {
			grt = GopherResourceType.PNG_IMAGE_FILE;
		}
				
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(grt, ServerResourceType.VIRTUAL_FILE);
		
		GopherItem item = buildVirtualGopherItem(displayText, gopherPath, parentPath, bytes, domainName, port, persistent,
				resourceDescriptor);
		
		return item;
		
	}

	public static GopherItem buildVirtualHtmlFile(String displayText, String gopherPath, String parentPath, byte[] bytes, String domainName, int port, boolean persistent) {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.HTML, ServerResourceType.VIRTUAL_FILE);
		
		GopherItem item = buildVirtualGopherItem(displayText, gopherPath, parentPath, bytes, domainName, port, persistent,
				resourceDescriptor);
		
		return item;
		
	}
	
	public static GopherItem buildRss2Feed(String displayText, String gopherPath, String parentPath, byte[] bytes, String domainName, int port, boolean persistent, String ttl) {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.DIRECTORY, ServerResourceType.RSS2_FEED);
		
		if(StringUtils.isBlank(ttl)) {
			ttl = "0";
		}
		
		GopherItem item = buildVirtualGopherItem(displayText, gopherPath, parentPath, bytes, domainName, port, persistent,
				resourceDescriptor, Integer.valueOf(ttl));
		
		return item;
		
	}
	
	public static GopherItem buildRss2Item(String displayText, String gopherPath, String parentPath, byte[] bytes, String domainName, int port, boolean persistent) {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.TEXT_FILE, ServerResourceType.RSS2_ITEM);
		
		GopherItem item = buildVirtualGopherItem(displayText, gopherPath, parentPath, bytes, domainName, port, persistent,
				resourceDescriptor);
		
		return item;
		
	}
	
	public static GopherItem buildWithoutResourceDescriptor(String gopherPath, String resourcePath, String displayText, String domainName, int port, String parentPath, boolean persistOverRestart) {
		GopherItem item = new GopherItem();
		
		item.setDisplayText(displayText);
		item.setGopherPath(gopherPath);
		item.setResourcePath(resourcePath);;
		item.setParentPath(parentPath);
		item.setDomainName(domainName);
		item.setPort(port);		
		item.setPersistOverRestart(persistOverRestart);
		
		return item;
	}

	private static GopherItem buildGopherItem(String displayText, String gopherPath, String resourcePath,
			String parentPath, String domainName, int port, boolean persistent, ResourceDescriptor resourceDescriptor) {
		GopherItem item = new GopherItem();
		
		item.setResourceDescriptor(resourceDescriptor);
		
		item.setResourceDescriptor(resourceDescriptor);
		item.setDisplayText(displayText);
		item.setGopherPath(gopherPath);
		item.setResourcePath(resourcePath);
		item.setParentPath(parentPath);
		item.setDomainName(domainName);
		item.setPort(port);
		item.setPersistOverRestart(persistent);
		return item;
	}
	
	private static GopherItem buildVirtualGopherItem(String displayText, String gopherPath, String parentPath, byte[] bytes,
			String domainName, int port, boolean persistent, ResourceDescriptor resourceDescriptor) {
		return buildVirtualGopherItem(displayText, gopherPath, parentPath, bytes, domainName, port, persistent, resourceDescriptor, 0);
	}

	private static GopherItem buildVirtualGopherItem(String displayText, String gopherPath, String parentPath, byte[] bytes,
			String domainName, int port, boolean persistent, ResourceDescriptor resourceDescriptor, int ttl) {
		GopherItem item = new GopherItem();
		
		item.setResourceDescriptor(resourceDescriptor);
		
		item.setResourceDescriptor(resourceDescriptor);
		item.setDisplayText(displayText);
		item.setGopherPath(gopherPath);
		item.setParentPath(parentPath);
		item.setDomainName(domainName);
		item.setPort(port);
		item.setPersistOverRestart(persistent);
		item.setContent(bytes);
		item.setTtlMinutes(ttl);
		return item;
	}

}

