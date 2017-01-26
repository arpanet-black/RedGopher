package black.arpanet.gopher.db;

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
		item.setDomainName(itemDomain);
		item.setPort(itemPort);
		item.setResourcePath(resourcePath);
		item.setParentPath(currentGopherDir);
		item.setPersistOverRestart(persistent);

		return item;
	}
	
	public static GopherItem buildTextFile(String displayText, String gopherPath, String resourcePath, String parentPath, String domainName, int port, boolean persistent) {

		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.TEXT_FILE, ServerResourceType.LOCAL_FILE);
		
		GopherItem item = new GopherItem();

		item.setResourceDescriptor(resourceDescriptor);
		item.setDisplayText(displayText);
		item.setDomainName(domainName);
		item.setPort(port);
		item.setResourcePath(resourcePath);
		item.setParentPath(parentPath);
		item.setPersistOverRestart(persistent);

		return item;
		
	}

	public static GopherItem buildBinaryFile(String displayText, String gopherPath, String resourcePath, String parentPath, String domainName, int port, boolean persistent) {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.BINARY_FILE, ServerResourceType.LOCAL_FILE);
		
		GopherItem item = new GopherItem();
		
		item.setResourceDescriptor(resourceDescriptor);
		
		item.setResourceDescriptor(resourceDescriptor);
		item.setDisplayText(displayText);
		item.setDomainName(domainName);
		item.setPort(port);
		item.setResourcePath(resourcePath);
		item.setParentPath(parentPath);
		item.setPersistOverRestart(persistent);
		
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
		
		GopherItem item = new GopherItem();
		
		item.setResourceDescriptor(resourceDescriptor);
		
		item.setResourceDescriptor(resourceDescriptor);
		item.setDisplayText(displayText);
		item.setDomainName(domainName);
		item.setPort(port);
		item.setResourcePath(resourcePath);
		item.setParentPath(parentPath);
		item.setPersistOverRestart(persistent);
		
		return item;
	}

	public static GopherItem buildHtmlFile(String displayText, String gopherPath, String resourcePath, String parentPath, String domainName, int port, boolean persistent) {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.HTML, ServerResourceType.LOCAL_FILE);
		
		GopherItem item = new GopherItem();
		
		item.setResourceDescriptor(resourceDescriptor);
		
		item.setResourceDescriptor(resourceDescriptor);
		item.setDisplayText(displayText);
		item.setDomainName(domainName);
		item.setPort(port);
		item.setResourcePath(resourcePath);
		item.setParentPath(parentPath);
		item.setPersistOverRestart(persistent);
		
		return item;
		
	}

	public static GopherItem buildRss2Feed() {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.DIRECTORY, ServerResourceType.RSS2_FEED);
		
		GopherItem item = new GopherItem();
		
		item.setResourceDescriptor(resourceDescriptor);
		
		//TODO: Set props
		
		return item;
		
	}

	public static GopherItem buildVirtualDirectory() {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.DIRECTORY, ServerResourceType.VIRTUAL_DIRECTORY);
		
		GopherItem item = new GopherItem();
		
		item.setResourceDescriptor(resourceDescriptor);
		
		//TODO: Set props
		
		return item;
		
	}

	public static GopherItem buildVirtualTextFile(String displayText, String gopherPath, String parentPath, String content, String domainName, int port, boolean persistent) {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.TEXT_FILE, ServerResourceType.VIRTUAL_FILE);
		
		GopherItem item = new GopherItem();
		
		item.setResourceDescriptor(resourceDescriptor);
		
		item.setResourceDescriptor(resourceDescriptor);
		item.setDisplayText(displayText);
		item.setDomainName(domainName);
		item.setPort(port);
		item.setParentPath(parentPath);
		item.setPersistOverRestart(persistent);
		item.setContent(content.getBytes());
		
		return item;
		
	}

	public static GopherItem buildVirtualBinaryFile(String displayText, String gopherPath, String parentPath, byte[] bytes, String domainName, int port, boolean persistent) {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.BINARY_FILE, ServerResourceType.VIRTUAL_FILE);
		
		GopherItem item = new GopherItem();
		
		item.setResourceDescriptor(resourceDescriptor);
		
		item.setResourceDescriptor(resourceDescriptor);
		item.setDisplayText(displayText);
		item.setDomainName(domainName);
		item.setPort(port);
		item.setParentPath(parentPath);
		item.setPersistOverRestart(persistent);
		item.setContent(bytes);
		
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
		
		GopherItem item = new GopherItem();
		
		item.setResourceDescriptor(resourceDescriptor);
		
		item.setResourceDescriptor(resourceDescriptor);
		item.setDisplayText(displayText);
		item.setDomainName(domainName);
		item.setPort(port);
		item.setParentPath(parentPath);
		item.setPersistOverRestart(persistent);
		item.setContent(bytes);
		
		return item;
		
	}

	public static GopherItem buildVirtualHtmlFile(String displayText, String gopherPath, String parentPath, byte[] bytes, String domainName, int port, boolean persistent) {
		ResourceDescriptor resourceDescriptor = RedGopherDbManager.findResourceDescriptor(GopherResourceType.HTML, ServerResourceType.VIRTUAL_FILE);
		
		GopherItem item = new GopherItem();
		
		item.setResourceDescriptor(resourceDescriptor);
		
		item.setResourceDescriptor(resourceDescriptor);
		item.setDisplayText(displayText);
		item.setDomainName(domainName);
		item.setPort(port);
		item.setParentPath(parentPath);
		item.setPersistOverRestart(persistent);
		item.setContent(bytes);
		
		return item;
		
	}
	
	public static GopherItem buildWithoutResourceDescriptor(String gopherPath, String resourcePath, String displayText, String domainName, int port, String parentPath, boolean persistOverRestart) {
		GopherItem gi = new GopherItem();
		
		gi.setGopherPath(gopherPath);
		gi.setResourcePath(resourcePath);;
		gi.setDisplayText(displayText);
		gi.setDomainName(domainName);
		gi.setPort(port);
		gi.setParentPath(parentPath);
		gi.setPersistOverRestart(persistOverRestart);
		
		return gi;
	}

}

