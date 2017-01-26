package black.arpanet.gopher.util;

import java.io.File;
import java.io.FileFilter;

public class DirectoryFilter implements FileFilter {

	@Override
	public boolean accept(File f) {
		return f.isDirectory();
	}

}
