package black.arpanet.gopher.util;

import black.arpanet.gopher.db.entities.GopherItem;
import black.arpanet.gopher.db.entities.ResourceDescriptor;

public class GopherFileUtil {

	public static String buildGopherPath(String displayName, ResourceDescriptor resourceDescriptor, GopherItem parent) {
		return (parent == null ? "/" : parent.getGopherPath() + "/")  + displayName;
	}
}
