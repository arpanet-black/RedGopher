package black.arpanet.gopher.db;

/*
0 Item is a file
1 Item is a directory
2 Item is a CSO phone-book server
3 Error
4 Item is a BinHexed Macintosh file.
5 Item is DOS binary archive of some sort.
Client must read until the TCP connection closes. Beware.
6 Item is a UNIX uuencoded file.
7 Item is an Index-Search server.
8 Item points to a text-based telnet session.
9 Item is a binary file!
+ Item is a redundant server
T Item points to a text-based tn3270 session.
g Item is a GIF format graphics file.
I Item is some kind of image file. Client decides how to display.
*/
public enum GopherResourceType {	
	
	TEXT_FILE("0"),
	DIRECTORY("1"),
	CSO_SEARCH("2"),
	ERROR("3"),
	BINHEX_ENCODED("4"),
	BIN_ARCHIVE("5"),
	UNIX_UUENCODED("6"),
	INDEX_SEARCH_SERVER("7"),
	TELNET("8"),
	BINARY_FILE("9"),
	REDUNDANT_SERVER("*"),
	TN3270_SESSION("T"),
	GIF_GRAPHICS_FILE("g"),
	PNG_IMAGE_FILE("p"),
	IMAGE_FILE("I"),
	INFORMATION_TEXT("i"),
	HTML("h"),
	AUDIO("s"),
	NONE("");
	
	private String typeId;
	
	private GopherResourceType(String typeId) {
		this.typeId = typeId;
	}
	
	public String getTypeId() {
		return this.typeId;
	}
	
	public static GopherResourceType fromOrdinal(int ordinal) {
		return GopherResourceType.values()[ordinal];
	}
	
}
