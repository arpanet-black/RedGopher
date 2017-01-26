package black.arpanet.gopher.util;

import java.io.File;
import java.io.FilenameFilter;

public class GophermapFileNameFilter implements FilenameFilter {

	private String filename;
	
	public GophermapFileNameFilter(String filename) {
		this.filename = filename;
	}
	
	@Override
	public boolean accept(File f, String name) {
		return name.equalsIgnoreCase(filename);
	}

	public String getFilename() {
		return filename;
	}

}
