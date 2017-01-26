package black.arpanet.gopher.db;

import static black.arpanet.gopher.util.RedGopherLogUtil.i;
import static black.arpanet.gopher.util.RedGopherLogUtil.t;
import static black.arpanet.gopher.util.RedGopherLogUtil.d;
import static black.arpanet.gopher.util.RedGopherLogUtil.w;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import black.arpanet.gopher.db.entities.GopherItem;
import black.arpanet.gopher.db.entities.ResourceDescriptor;
import black.arpanet.gopher.db.entities.ServerFileType;

public class RedGopherDbManager {

	private static final String PERSISTENCE_UNIT_NAME = "RedGopher";	

	private static final Logger LOG = LogManager.getLogger(RedGopherDbManager.class);
	private static final EntityManagerFactory EM_FACTORY = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
	private static final EntityManager em = EM_FACTORY.createEntityManager();	

	public static ResourceDescriptor createResourceDescriptor(GopherResourceType grt, ServerResourceType srt, String description) {
		t(LOG,"In createResourceDescriptor(GopherResourceType,ServerResourceType,String).");
		ResourceDescriptor rd = new ResourceDescriptor();
		rd.setGopherResourceType(grt.ordinal());
		rd.setServerResourceType(srt.ordinal());
		rd.setResourceDescription(description);		
		rd = em.merge(rd);

		t(LOG,"Out createResourceDescriptor(GopherResourceType,ServerResourceType,String).");
		return rd;
	}
	
	public static ServerFileType createServerFileType(String fileExtension, ResourceDescriptor resourceDescriptor) {
		t(LOG,"In createServerFileType(String,ResourceDescriptor).");
		ServerFileType sft = new ServerFileType();
		sft.setFileExtension(fileExtension);
		sft.setResourceDescriptor(resourceDescriptor);
		sft = em.merge(sft);
		
		t(LOG,"Out createServerFileType(String,ResourceDescriptor).");
		return sft;
	}

	public static void deleteAllResourceDescriptors() {
		i(LOG,"Deleting all resource descriptors.");
		em.getTransaction().begin();
		em.createNamedQuery("ResourceDescriptor.deleteAll").executeUpdate();
		em.getTransaction().commit();
	}
	
	public static void deleteAllServerFileTypes() {
		i(LOG,"Deleting all server file types.");
		em.getTransaction().begin();
		em.createNamedQuery("ServerFileType.deleteAll").executeUpdate();
		em.getTransaction().commit();
	}

	public static void deleteVolatileItems() {
		i(LOG,"Deleting all volatile GopherItems.");
		em.getTransaction().begin();
		em.createNamedQuery("GopherItem.deleteVolatileItems").executeUpdate();
		em.getTransaction().commit();
	}

	public static GopherItem createGopherItem(ResourceDescriptor resourceDescriptor, String gopherPath, String resourceUri, String displayText, String domainName, int port, boolean persistOverRestart, String parentPath) {
		t(LOG,"In createGopherItem(1).");
		GopherItem gi = null;

		gi = new GopherItem();
		gi.setResourceDescriptor(resourceDescriptor);
		gi.setGopherPath(gopherPath);
		gi.setResourceUri(resourceUri);
		gi.setDisplayText(displayText);
		gi.setDomainName(domainName);
		gi.setPersistOverRestart(persistOverRestart);
		gi.setPort(port);
		gi.setParentPath(parentPath);

		gi = em.merge(gi);

		t(LOG,"Out createGopherItem(1).");
		return gi;
	}
	
	public static GopherItem createGopherItem(GopherResourceType gopherResourceType, ServerResourceType serverResourceType, String gopherPath, String resourceUri, String displayText, byte[] content, String domainName, int port, boolean persistOverRestart, String parentPath) {
		t(LOG,"In createGopherItem(2).");
		
		ResourceDescriptor resourceDescriptor = findResourceDescriptor(gopherResourceType, serverResourceType);
		
		GopherItem gi = null;

		gi = new GopherItem();
		gi.setResourceDescriptor(resourceDescriptor);
		gi.setGopherPath(gopherPath);
		gi.setResourceUri(resourceUri);
		gi.setDisplayText(displayText);
		gi.setDomainName(domainName);
		gi.setPersistOverRestart(persistOverRestart);
		gi.setPort(port);
		gi.setParentPath(parentPath);
		gi.setContent(content);

		gi = em.merge(gi);

		t(LOG,"Out createGopherItem(2).");
		return gi;
	}

	//Keep transactional to preserve order
	public static GopherItem createGopherItemWithTrans(GopherResourceType gopherResourceType, ServerResourceType serverResourceType, String gopherPath, String resourceUri, String displayText, byte[] content, String domainName, int port, boolean persistOverRestart, String parentPath) {
		em.getTransaction().begin();
		GopherItem gi = createGopherItem(gopherResourceType, serverResourceType, gopherPath, resourceUri, displayText, content, domainName, port, persistOverRestart, parentPath);
		em.flush();
		em.getTransaction().commit();
		
		return gi;
	}

	public static ResourceDescriptor findResourceDescriptor(GopherResourceType grt, ServerResourceType srt) {

		TypedQuery<ResourceDescriptor> q = em.createNamedQuery("ResourceDescriptor.findByResourceTypes", ResourceDescriptor.class);

		q.setParameter("grt", grt.ordinal());
		q.setParameter("srt", srt.ordinal());

		ResourceDescriptor rd = q.getSingleResult();

		return rd;
	}
	
	public static ServerFileType findServerFileTypeByExt(String extension) {

		TypedQuery<ServerFileType> q = em.createNamedQuery("ServerFileType.findByFileExtension", ServerFileType.class);

		q.setParameter("ext", extension);

		ServerFileType sft = q.getSingleResult();

		return sft;
	}

	public static List<GopherItem> findAllGopherItems() {

		TypedQuery<GopherItem> q = em.createNamedQuery("GopherItem.findAll", GopherItem.class);

		List<GopherItem> gopherItems = q.getResultList();

		return gopherItems;
	}

	public static List<GopherItem> findTopLevelGopherItems() {

		TypedQuery<GopherItem> q = em.createNamedQuery("GopherItem.findTopLevelItems", GopherItem.class);

		List<GopherItem> gopherItems = q.getResultList();

		return gopherItems;
	}

	public static List<GopherItem> findGopherItemsByGopherPath(String gopherPath) {

		TypedQuery<GopherItem> q = em.createNamedQuery("GopherItem.findByGopherPath", GopherItem.class);
		q.setParameter("path", gopherPath);

		List<GopherItem> gopherItems = q.getResultList();

		if(gopherItems.size() == 1) {
			GopherItem item = gopherItems.get(0);

			if(GopherResourceType.values()[item.getResourceDescriptor().getGopherResourceType()] == GopherResourceType.DIRECTORY) {
				TypedQuery<GopherItem> q2 = em.createNamedQuery("GopherItem.findByParentPath", GopherItem.class);
				q2.setParameter("path", gopherPath);
				gopherItems = q.getResultList();
			}

		}  else if(gopherItems.size() < 1) {
			w(LOG, String.format("No entries returned for gopher path. Path: %s", gopherPath));
		} else {
			w(LOG, String.format("Multiple entries returned for single gopher path. Path: %s", gopherPath));
		}

		return gopherItems;
	}

	public static List<GopherItem> findGopherItemsByParentPath(String parentPath) {

		TypedQuery<GopherItem> q = em.createNamedQuery("GopherItem.findByParentPath", GopherItem.class);
		q.setParameter("path", parentPath);

		List<GopherItem> gopherItems = q.getResultList();

		if(gopherItems.size() == 1) {
			GopherItem item = gopherItems.get(0);

			if(GopherResourceType.values()[item.getResourceDescriptor().getGopherResourceType()] == GopherResourceType.DIRECTORY) {
				TypedQuery<GopherItem> q2 = em.createNamedQuery("GopherItem.findByParentPath", GopherItem.class);
				q2.setParameter("path", parentPath);
				gopherItems = q.getResultList();
			}

		} else if(gopherItems.size() < 1) {
			w(LOG, String.format("No entries returned for parent path. Path: %s", parentPath));
		} else {
			d(LOG, String.format("Multiple entries returned for single parent path. Path: %s", parentPath));
		}

		return gopherItems;
	}


	public static void checkpointDb() {
		i(LOG,"Database checkpoint initiated.");
		em.getTransaction().begin();
		em.createNativeQuery("CHECKPOINT").executeUpdate();
		em.getTransaction().commit();
	}

	public static void shutdownDb() {
		w(LOG,"Database shutdown initiated!");
		em.getTransaction().begin();
		em.createNativeQuery("SHUTDOWN").executeUpdate();
		em.getTransaction().commit();
	}

}
