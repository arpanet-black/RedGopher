package black.arpanet.gopher.files;

import static black.arpanet.gopher.util.RedGopherLogUtil.d;
import static black.arpanet.gopher.util.RedGopherLogUtil.e;
import static black.arpanet.gopher.util.RedGopherLogUtil.i;
import static black.arpanet.gopher.util.RedGopherLogUtil.w;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import black.arpanet.gopher.ContentManager;
import black.arpanet.gopher.db.GopherResourceType;
import black.arpanet.gopher.db.RedGopherDbManager;
import black.arpanet.gopher.db.ServerResourceType;
import black.arpanet.gopher.db.entities.GopherItem;
import black.arpanet.gopher.db.entities.ResourceDescriptor;
import black.arpanet.gopher.db.entities.ServerFileType;
import black.arpanet.gopher.util.DirectoryFilter;
import black.arpanet.gopher.util.GopherFileUtil;
import black.arpanet.gopher.util.GophermapFileNameFilter;

public class FileContentManager extends ContentManager {
	
	public static final String CONTENT_DIRECTORY_PROP = "content_directory";
	public static final String GOPHERMAP_PROP = "gophermap";
	public static final String CONTENT_MODE_PROP = "content_mode";
	public static final String DEFAULT_CONTENT_MODE = "GOPHERMAP";
	public static final String DEFAULT_CONTENT_DIRECTORY = "c:\\gopher";
	public static final String DEFAULT_GOPHERMAP_NAME = "gophermap.xml";
	public static final String DOMAIN_NAME_PROP = "domain_name";
	public static final String GOPHER_PORT_PROP = "gopher_port";
	
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
	private static final String RESOURCE_PATH_ELEMENT_NAME = "resourcePath";
	private static final String CONTENT_ELEMENT_NAME = "content";
	private static final String PERSISTENT_ELEMENT_NAME = "persistent";
	
	private static final Logger LOG = LogManager.getLogger(FileContentManager.class);
	
	private GophermapFileNameFilter gopherFnf;
	private DirectoryFilter dirF;
	private String domainName;
	private int port;
	private ContentMode contentMode;
	private String contentDirectory;
	private String gopherMapFileName;

	@Override
	public boolean init(Properties props) {
		//Load Gopher Items from content directory
		contentMode = ContentMode.valueOf(props.getProperty(CONTENT_MODE_PROP,DEFAULT_CONTENT_MODE));
		contentDirectory = props.getProperty(CONTENT_DIRECTORY_PROP, DEFAULT_CONTENT_DIRECTORY);
		gopherMapFileName = props.getProperty(GOPHERMAP_PROP, DEFAULT_GOPHERMAP_NAME);
		
		dirF = new DirectoryFilter();
		gopherFnf = new GophermapFileNameFilter(gopherMapFileName);
		domainName = props.getProperty(DOMAIN_NAME_PROP);
		port = Integer.valueOf(props.getProperty(GOPHER_PORT_PROP));

		return true;
	}

	@Override
	public boolean loadContent() {
		if(contentMode.equals(ContentMode.DIRECTORY)) {
			if(initFromContentDirectory(contentDirectory)) {
				i(LOG,"RedGopher content initialized from content directory.");
			} else {
				i(LOG,String.format("RedGopher content could not be initialized from content directory!\nDirectory is: %s", contentDirectory));
			}
		} else if(contentMode.equals(ContentMode.GOPHERMAP)) {
			if(initFromGophermap(contentDirectory, gopherMapFileName)) {
				i(LOG,"RedGopher content initialized from gophermap.");
			} else {
				i(LOG,String.format("RedGopher content could not be initialized from gophermap!\nDirectory is: %s", contentDirectory));
			}
		}
		
		return true;
	}

	@Override
	public boolean update() {
		//File content is static
		return false;
	}

	private boolean initFromContentDirectory(String contentDirectoryPath) {
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

	private boolean initFromGophermap(String contentDirectoryPath, String gopherMapFile) {
		i(LOG,"Initializing from Gophermap.");
		i(LOG,String.format("Content directory is: %s", contentDirectoryPath));
		i(LOG,String.format("Gophermap is: %s", gopherMapFile));

		File contentDirectory = new File(contentDirectoryPath);

		if(!contentDirectory.exists()) {
			i(LOG, String.format("Creating content directory: %s", contentDirectory.getAbsolutePath()));
			contentDirectory.mkdir();
		}	

		parseGopherDirectories(contentDirectory, "/");

		RedGopherDbManager.checkpointDb();

		return true;
	}

	private void parseGopherDirectories(File dir, String defaultDir) {

		//Parse the gophermap file from THIS directory
		for(File f :  dir.listFiles(gopherFnf)) {
			loadGophermap(f, defaultDir, domainName, port);
		}

		//Look in subdirectories
		for(File d : dir.listFiles(dirF)) {
			parseGopherDirectories(d, defaultDir + d.getName() + "/");
		}
	}

	private void loadContent(File contentFile, GopherItem parent) {

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
	
	private void loadGophermap(File f, String defaultDir, String defaultDomain, int defaultPort) {

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
					String resourcePath = el.getChild(RESOURCE_PATH_ELEMENT_NAME) != null ? el.getChild(RESOURCE_PATH_ELEMENT_NAME).getValue() : defaultDir;
					String gopherPath = el.getChild(GOPHER_PATH_ELEMENT_NAME) != null ? el.getChild(GOPHER_PATH_ELEMENT_NAME).getValue() : null;
					byte[] content = el.getChild(CONTENT_ELEMENT_NAME) != null ? el.getChild(CONTENT_ELEMENT_NAME).getValue().getBytes() : null;					
					boolean persistent = el.getChild(PERSISTENT_ELEMENT_NAME) != null ? Boolean.valueOf(el.getChild(PERSISTENT_ELEMENT_NAME).getValue()) : false;
					
					if(srt.equals(ServerResourceType.LOCAL_FILE)) {
						resourcePath = new File(f.getParent() + "/" + resourcePath).toURI().toString();
					}
					
					//If the gopherPath was not set for a local directory
					//then build the directory gopher name
					if(srt.equals(ServerResourceType.LOCAL_DIRECTORY)
							&& gopherPath == null) {
						resourcePath += "/";
						gopherPath = resourcePath;
					}
					
					//Parse element into an informational string
					RedGopherDbManager.createGopherItemWithTrans(grt, srt,
							gopherPath,
							resourcePath,
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
