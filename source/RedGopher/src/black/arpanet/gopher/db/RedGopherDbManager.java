package black.arpanet.gopher.db;

import static black.arpanet.util.logging.ArpanetLogUtil.d;
import static black.arpanet.util.logging.ArpanetLogUtil.i;
import static black.arpanet.util.logging.ArpanetLogUtil.t;
import static black.arpanet.util.logging.ArpanetLogUtil.w;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import black.arpanet.gopher.GopherResourceType;
import black.arpanet.gopher.ServerResourceType;
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
		EntityTransaction et = em.getTransaction();
		try {
			et.begin();
			em.createNamedQuery("ResourceDescriptor.deleteAll").executeUpdate();
			et.commit();
		} catch(Exception ex) {			
			et.rollback();
			w(LOG, "Exception encountered deleting all resource descriptors.",ex);
		}
	}

	public static void deleteAllServerFileTypes() {
		i(LOG,"Deleting all server file types.");
		EntityTransaction et = em.getTransaction();
		try {
			et.begin();
			em.createNamedQuery("ServerFileType.deleteAll").executeUpdate();
			et.commit();
		} catch(Exception ex) {			
			et.rollback();
			w(LOG, "Exception encountered deleting all server file types.",ex);
		}
	}

	public static void deleteVolatileItems() {
		i(LOG,"Deleting all volatile GopherItems.");
		EntityTransaction et = em.getTransaction();
		try {
			et.begin();
			em.createNamedQuery("GopherItem.deleteVolatileItems").executeUpdate();
			et.commit();
		} catch(Exception ex) {			
			et.rollback();
			w(LOG, "Exception encountered deleting all volatile gopher items.",ex);
		}
	}

	public static void deleteByParentPath(String parentPath) {
		i(LOG,String.format("Deleting all items under parent path: %s", parentPath));
		EntityTransaction et = em.getTransaction();
		try {
			et.begin();;
			Query deleteQuery = em.createNamedQuery("GopherItem.deleteByParentPath");
			deleteQuery.setParameter("path", parentPath);
			deleteQuery.executeUpdate();
			et.commit();
		} catch(Exception ex) {			
			et.rollback();
			w(LOG, String.format("Exception encountered deleting items by parent path: %s",parentPath),ex);
		}

	}

	public static GopherItem mergeGopherItem(GopherItem gi) {

		EntityTransaction et = em.getTransaction();
		try {
			//Item commit order needs to be maintained
			et.begin();

			if(gi.getCreationDate() == null) {
				gi.setCreationDate(new Date());
			}

			gi.setUpdateDate(new Date());

			gi = em.merge(gi);
			em.flush();
			et.commit();
		} catch(Exception ex) {			
			et.rollback();
			w(LOG, String.format("Exception encountered merging gopher item. ID # %s: %s",gi.getId(), gi.getDisplayText()),ex);
		}

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

	public static GopherItem findSingleItemByGopherPath(String gopherPath) {

		TypedQuery<GopherItem> q = em.createNamedQuery("GopherItem.findByGopherPath", GopherItem.class);
		q.setParameter("path", gopherPath);

		GopherItem gopherItem = null;

		try {
			gopherItem = q.getSingleResult();
		} catch(NoResultException ex) {
			w(LOG, String.format("No gopher item found for path: %s", gopherPath), ex);
		} catch(NonUniqueResultException ex) {
			w(LOG, String.format("Multiple gopher items found for path, but single result was expected: %s", gopherPath), ex);
		}

		return gopherItem;
	}

	public static void checkpointDb() {
		i(LOG,"Database checkpoint initiated.");
		EntityTransaction et = em.getTransaction();
		try {
			et.begin();
			em.createNativeQuery("CHECKPOINT").executeUpdate();
			et.commit();
		} catch(Exception ex) {			
			et.rollback();
			w(LOG,"Exception encountered executing database checkpoint.",ex);
		}
	}

	public static void shutdownDb() {
		w(LOG,"Database shutdown initiated!");
		EntityTransaction et = em.getTransaction();
		try {
			et.begin();
			em.createNativeQuery("SHUTDOWN").executeUpdate();
			et.commit();
		} catch(Exception ex) {			
			et.rollback();
			w(LOG, String.format("Exception encountered executing database shutdown."),ex);
		}
	}

}
