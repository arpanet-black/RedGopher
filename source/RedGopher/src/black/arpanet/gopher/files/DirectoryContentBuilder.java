package black.arpanet.gopher.files;

import static black.arpanet.util.logging.ArpanetLogUtil.d;
import static black.arpanet.util.logging.ArpanetLogUtil.i;
import static black.arpanet.util.logging.ArpanetLogUtil.w;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import black.arpanet.gopher.db.GopherItemBuilder;
import black.arpanet.gopher.db.RedGopherDbManager;
import black.arpanet.gopher.db.entities.GopherItem;
import black.arpanet.gopher.db.entities.ResourceDescriptor;
import black.arpanet.gopher.db.entities.ServerFileType;
import black.arpanet.gopher.util.GopherFileUtil;

public class DirectoryContentBuilder implements ContentBuilder {
	
	private static final String PATH_SEP = "/";
	
	private static final Logger LOG = LogManager.getLogger(DirectoryContentBuilder.class);
	
	private String domainName;
	private int port;
	
	public DirectoryContentBuilder(String domainName, int port) {
		this.domainName = domainName;
		this.port = port;
	}


	public boolean initFromContentDirectory(String contentDirectoryPath) {
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
		root.setGopherPath(PATH_SEP);
		for(File f : contentDirectory.listFiles()) {
			loadContent(f, root);
		}

		RedGopherDbManager.checkpointDb();

		return true;
	}
	

	private void loadContent(File contentFile, GopherItem parent) {

		if(contentFile == null) return;

		d(LOG,String.format("Parsing content %s : %s", contentFile.isDirectory() ? "directory" : "file", contentFile.getName()));

		//Get descriptor for local directories using special file ext
		ResourceDescriptor localDirectoryDescriptor = RedGopherDbManager.findServerFileTypeByExt("+DIR").getResourceDescriptor();

		if(contentFile.isDirectory()) {

			String displayName = contentFile.getName();
			String gopherPath = GopherFileUtil.buildGopherPath(displayName, localDirectoryDescriptor, parent);	
			GopherItem thisItem = GopherItemBuilder.buildDirectory(displayName, (parent == null ? null : parent.getGopherPath()), domainName, port, contentFile.toURI().toASCIIString(), gopherPath, false);
			thisItem = RedGopherDbManager.mergeGopherItem(thisItem);
			
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
				GopherItem gi = GopherItemBuilder.buildWithoutResourceDescriptor(gopherPath, contentFile.toURI().toASCIIString(),
						displayName, domainName, port, (parent == null ? null : parent.getGopherPath()), false);
				gi.setResourceDescriptor(resourceDescriptor);
				gi = RedGopherDbManager.mergeGopherItem(gi);
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
