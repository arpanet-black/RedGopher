package black.arpanet.gopher.db;

import static black.arpanet.gopher.util.RedGopherLogUtil.i;
import static black.arpanet.gopher.util.RedGopherLogUtil.w;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import black.arpanet.gopher.GopherResourceType;
import black.arpanet.gopher.ServerResourceType;
import black.arpanet.gopher.db.entities.ResourceDescriptor;

public class RedGopherDbHelper {

	private static final Logger LOG = LogManager.getLogger(RedGopherDbHelper.class);
	
	public static void init() {
		RedGopherDbManager.deleteVolatileItems();
	}

	public static boolean initDbTables() {
		boolean success = false;

		try {
			RedGopherDbManager.deleteAllResourceDescriptors();
			RedGopherDbManager.deleteAllServerFileTypes();
			
			i(LOG,"Inserting resource descriptors.");
			ResourceDescriptor rd = null;
			//Directory
			rd = RedGopherDbManager.createResourceDescriptor(GopherResourceType.DIRECTORY, ServerResourceType.LOCAL_DIRECTORY, "Directory on the local filesystem.");
			
			//Use the special extension +DIR
			RedGopherDbManager.createServerFileType("+DIR", rd);
			
			//Files
			rd = RedGopherDbManager.createResourceDescriptor(GopherResourceType.TEXT_FILE, ServerResourceType.LOCAL_FILE, "Text file on the local filesystem.");
			
			//Use the special extension +FILE
			RedGopherDbManager.createServerFileType("+FILE", rd);
			
			//Create some other well-known types for efficiency
			RedGopherDbManager.createServerFileType("txt", rd);
			RedGopherDbManager.createServerFileType("sql", rd);
			RedGopherDbManager.createServerFileType("ascii", rd);
			RedGopherDbManager.createServerFileType("text", rd);
			RedGopherDbManager.createServerFileType("asm", rd);
			RedGopherDbManager.createServerFileType("s", rd);
			RedGopherDbManager.createServerFileType("bas", rd);
			RedGopherDbManager.createServerFileType("c", rd);
			
			//HTML Files
			rd = RedGopherDbManager.createResourceDescriptor(GopherResourceType.HTML, ServerResourceType.LOCAL_FILE, "Text file on the local filesystem.");
			
			//HTML File Extensions
			RedGopherDbManager.createServerFileType("htm", rd);
			RedGopherDbManager.createServerFileType("html", rd);
			
			//Image Files
			rd = RedGopherDbManager.createResourceDescriptor(GopherResourceType.IMAGE_FILE, ServerResourceType.LOCAL_FILE, "Image on the local filesystem.");
			
			//Associate with image file extensions
			RedGopherDbManager.createServerFileType("jpg", rd);
			RedGopherDbManager.createServerFileType("jpeg", rd);
			RedGopherDbManager.createServerFileType("bmp", rd);
			
			//PNG Files
			rd = RedGopherDbManager.createResourceDescriptor(GopherResourceType.PNG_IMAGE_FILE, ServerResourceType.LOCAL_FILE, "Image on the local filesystem.");
			RedGopherDbManager.createServerFileType("png", rd);
			
			//GIF images
			rd = RedGopherDbManager.createResourceDescriptor(GopherResourceType.GIF_GRAPHICS_FILE, ServerResourceType.LOCAL_FILE, "GIF image on the local filesystem.");
			
			//Associate with GIF file extension
			RedGopherDbManager.createServerFileType("gif", rd);
			
			//Binary Files
			rd = RedGopherDbManager.createResourceDescriptor(GopherResourceType.BINARY_FILE, ServerResourceType.LOCAL_FILE, "Binary file on the local filesystem.");
			
			//Binary file extensions
			RedGopherDbManager.createServerFileType("pdf", rd);
			RedGopherDbManager.createServerFileType("jar", rd);
			RedGopherDbManager.createServerFileType("iso", rd);
			RedGopherDbManager.createServerFileType("dsk", rd);
			RedGopherDbManager.createServerFileType("2mg", rd);
			RedGopherDbManager.createServerFileType("po", rd);
			
			//Archive Files
			rd = RedGopherDbManager.createResourceDescriptor(GopherResourceType.BIN_ARCHIVE, ServerResourceType.LOCAL_FILE, "Archive file on the local filesystem.");
			
			//Archive file extensions
			RedGopherDbManager.createServerFileType("zip", rd);
			RedGopherDbManager.createServerFileType("7z", rd);
			
			//Information Text elements
			RedGopherDbManager.createResourceDescriptor(GopherResourceType.INFORMATION_TEXT, ServerResourceType.VIRTUAL_FILE, "Information Text.");
			
			//Error message elements
			RedGopherDbManager.createResourceDescriptor(GopherResourceType.ERROR, ServerResourceType.VIRTUAL_FILE, "Error message.");
			
			//Resource for creating virtual directories
			RedGopherDbManager.createResourceDescriptor(GopherResourceType.DIRECTORY, ServerResourceType.VIRTUAL_DIRECTORY, "RSS2 xml feed.");
			
			//Virtual files
			RedGopherDbManager.createResourceDescriptor(GopherResourceType.TEXT_FILE, ServerResourceType.VIRTUAL_FILE, "Virtual Text file.");
			RedGopherDbManager.createResourceDescriptor(GopherResourceType.BINARY_FILE, ServerResourceType.VIRTUAL_FILE, "Virtual Binary file.");
			RedGopherDbManager.createResourceDescriptor(GopherResourceType.BIN_ARCHIVE, ServerResourceType.VIRTUAL_FILE, "Virtual Binary Archive file.");
			RedGopherDbManager.createResourceDescriptor(GopherResourceType.HTML, ServerResourceType.VIRTUAL_FILE, "Virtual HTML file.");
			RedGopherDbManager.createResourceDescriptor(GopherResourceType.IMAGE_FILE, ServerResourceType.VIRTUAL_FILE, "Virtual image file.");
			RedGopherDbManager.createResourceDescriptor(GopherResourceType.GIF_GRAPHICS_FILE, ServerResourceType.VIRTUAL_FILE, "Virtual GIF file.");
			RedGopherDbManager.createResourceDescriptor(GopherResourceType.PNG_IMAGE_FILE, ServerResourceType.VIRTUAL_FILE, "Virtual PNG file.");

			//Resources for RSS2 Feeds
			RedGopherDbManager.createResourceDescriptor(GopherResourceType.DIRECTORY, ServerResourceType.RSS2_FEED, "RSS2 xml feed.");
			RedGopherDbManager.createResourceDescriptor(GopherResourceType.TEXT_FILE, ServerResourceType.RSS2_ITEM, "RSS2 content item.");

			RedGopherDbManager.checkpointDb();

			success = true;
			
		} catch(Exception e) {
			w(LOG,"Exception encountered initializing database tables!",e);
			success = false;
		}

		return success;
	}
}
