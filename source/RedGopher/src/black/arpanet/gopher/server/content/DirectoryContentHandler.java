package black.arpanet.gopher.server.content;

import java.util.List;

import black.arpanet.gopher.GopherResourceType;
import black.arpanet.gopher.db.RedGopherDbManager;
import black.arpanet.gopher.db.entities.GopherItem;
import black.arpanet.gopher.server.RedGopherServer;

public class DirectoryContentHandler implements ContentHandler {
	
	private static final String PATH_SEP = "/";

	@Override
	public byte[] getContent(GopherItem item) {
		List<GopherItem> items = RedGopherDbManager.findGopherItemsByParentPath(item.getGopherPath() + PATH_SEP);
		return buildMenu(items).getBytes();
	}
	
	//Build a menu from the content items
	private String buildMenu(List<GopherItem> items) {
		StringBuilder sb = new StringBuilder();

		for(GopherItem gi : items) {
			//File Type - Display Text - Selector String - Domain Name - Port - CRLF
			String gopherTypeId = GopherResourceType.values()[gi.getResourceDescriptor().getGopherResourceType()].getTypeId();
			sb.append(String.format("%s%s%s%s%s%s%s%s%s%s", 
					gopherTypeId,
					gi.getDisplayText(), RedGopherServer.TAB,
					gi.getGopherPath(), RedGopherServer.TAB,
					gi.getDomainName(), RedGopherServer.TAB,
					gi.getPort(), RedGopherServer.TAB,
					RedGopherServer.CRLF));
		}

		sb.append(".").append(RedGopherServer.CRLF);


		return sb.toString();
	}

}
