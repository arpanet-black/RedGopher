package black.arpanet.gopher.db;

import static black.arpanet.gopher.util.RedGopherLogUtil.d;
import static black.arpanet.gopher.util.RedGopherLogUtil.i;
import static black.arpanet.gopher.util.RedGopherLogUtil.w;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import black.arpanet.gopher.db.entities.GopherItem;
import black.arpanet.gopher.db.entities.ResourceDescriptor;
import black.arpanet.gopher.db.entities.ServerFileType;
import black.arpanet.gopher.util.DirectoryFilter;
import black.arpanet.gopher.util.GopherFileUtil;
import black.arpanet.gopher.util.GophermapFileNameFilter;

public class RedGopherDbHelper {

	private static final Logger LOG = LogManager.getLogger(RedGopherDbHelper.class);
	private static String domainName;
	private static int port;
	private static GophermapFileNameFilter gopherFnf;
	private static DirectoryFilter dirF = new DirectoryFilter();
	
	public static void init(String serverDomainName, int serverPort) {
		domainName = serverDomainName;
		port = serverPort;
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
			RedGopherDbManager.createResourceDescriptor(GopherResourceType.TEXT_FILE, ServerResourceType.VIRTUAL_FILE, "Virtual file.");

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

	public static boolean initFromContentDirectory(String contentDirectoryPath) {
		i(LOG,"Initializing from content directory.");
		i(LOG,String.format("Content directory is: %s", contentDirectoryPath));		

		File contentDirectory = new File(contentDirectoryPath);
		
		if(!contentDirectory.exists()) {
			i(LOG, String.format("Creating content directory: %s", contentDirectory.getAbsolutePath()));
			contentDirectory.mkdir();
		}

		//Load all of the files in the content directory into the gopher database
		if(contentDirectory.listFiles().length < 1) {
			w(LOG,"No files found in content directory");
			return false;
		}

		//Iterate here so that the content directory
		//itself is not the top-level resource.
		GopherItem root = new GopherItem();
		root.setGopherPath("/");
		for(File f : contentDirectory.listFiles()) {
			loadContent(f, root);
		}

		RedGopherDbManager.checkpointDb();

		return true;
	}
	
	public static boolean initFromGophermap(String contentDirectoryPath, String gopherMapFile) {
		i(LOG,"Initializing from Gophermap.");
		i(LOG,String.format("Content directory is: %s", contentDirectoryPath));
		i(LOG,String.format("Gophermap is: %s", gopherMapFile));
		gopherFnf = new GophermapFileNameFilter(gopherMapFile);
		
		File contentDirectory = new File(contentDirectoryPath);
		
		if(!contentDirectory.exists()) {
			i(LOG, String.format("Creating content directory: %s", contentDirectory.getAbsolutePath()));
			contentDirectory.mkdir();
		}	
		
		parseGopherDirectories(contentDirectory, "/");
		
		RedGopherDbManager.checkpointDb();
		
		return true;
	}

	private static void parseGopherDirectories(File dir, String defaultDir) {
		
		//Parse the gophermap file from THIS directory
		for(File f :  dir.listFiles(gopherFnf)) {
			GophermapDbLoader.loadGophermap(f, defaultDir, domainName, port);
		}
		
		//Look in subdirectories
		for(File d : dir.listFiles(dirF)) {
			parseGopherDirectories(d, defaultDir + d.getName() + "/");
		}
	}

	private static void loadContent(File contentFile, GopherItem parent) {

		if(contentFile == null) return;

		d(LOG,String.format("Parsing content %s : %s", contentFile.isDirectory() ? "directory" : "file", contentFile.getName()));
		
		//Get descriptor for local directories using special file ext
		ResourceDescriptor localDirectoryDescriptor = RedGopherDbManager.findServerFileTypeByExt("+DIR").getResourceDescriptor();

		if(contentFile.isDirectory()) {

			String displayName = contentFile.getName();
			String gopherPath = GopherFileUtil.buildGopherPath(displayName, localDirectoryDescriptor, parent);			
			GopherItem thisItem = RedGopherDbManager.createGopherItem(localDirectoryDescriptor, gopherPath, contentFile.toURI().toASCIIString(),
					displayName, domainName, port, false, (parent == null ? null : parent.getGopherPath()));
			
			//Load directory contents
			for(File f : contentFile.listFiles()) {
				loadContent(f, thisItem);
			}

		} else if(contentFile.isFile()) {

			ResourceDescriptor resourceDescriptor = getDescriptorForFileType(contentFile);

			if(resourceDescriptor == null) {
				w(LOG,String.format("(Skipping) Could not find appropriate resource descriptor for file: %s", contentFile.getName()));
			} else {
				String displayName = contentFile.getName();
				String gopherPath = GopherFileUtil.buildGopherPath(displayName, resourceDescriptor, parent);
				RedGopherDbManager.createGopherItem(resourceDescriptor, gopherPath, contentFile.toURI().toASCIIString(),
						displayName, domainName, port, false, (parent == null ? null : parent.getGopherPath()));
			}
		}


	}

	private static ResourceDescriptor getDescriptorForFileType(File contentFile) {
		ResourceDescriptor rd = null;

		String extension = FilenameUtils.getExtension(contentFile.getName());

		if(StringUtils.isBlank(extension)) {
			return null;
		}

		extension = extension.toLowerCase();
		
		ServerFileType sft = RedGopherDbManager.findServerFileTypeByExt(extension);
		
		//If file type was not found then just use the generic file descriptor
		if(sft == null) {
			sft = RedGopherDbManager.findServerFileTypeByExt("+FILE");
		}
		
		rd = sft.getResourceDescriptor();

		return rd;
	}
}
