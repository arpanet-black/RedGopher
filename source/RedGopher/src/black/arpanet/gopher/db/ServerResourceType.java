package black.arpanet.gopher.db;

public enum ServerResourceType {

	LOCAL_FILE,
	LOCAL_DIRECTORY,
	VIRTUAL_FILE,
	VIRTUAL_DIRECTORY,
	RSS2_FEED,
	RSS2_ITEM;
	
	public static ServerResourceType fromOrdinal(int ordinal) {
		return ServerResourceType.values()[ordinal];
	}
	
}
