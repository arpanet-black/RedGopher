package black.arpanet.gopher.db.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="resource_descriptor")
@NamedQueries({
	@NamedQuery(name="ResourceDescriptor.findByResourceTypes", query="select rd from ResourceDescriptor rd where rd.gopherResourceType = :grt and rd.serverResourceType = :srt"),
	@NamedQuery(name="ResourceDescriptor.deleteAll", query="delete from ResourceDescriptor"),
})
public class ResourceDescriptor {

	@Id
	@Column(name="id")
	@GeneratedValue
	private int id;
	
	@Column(name="gopher_resource_type")
	private int gopherResourceType;
	
	@Column(name="server_resource_type")
	private int serverResourceType;
	
	@Column(name="resource_description")
	private String resourceDescription;
	
	@OneToMany
	private List<ServerFileType> fileTypes;

	public ResourceDescriptor() {
		this.fileTypes = new ArrayList<ServerFileType>();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getGopherResourceType() {
		return gopherResourceType;
	}

	public void setGopherResourceType(int gopherResourceType) {
		this.gopherResourceType = gopherResourceType;
	}

	public int getServerResourceType() {
		return serverResourceType;
	}

	public void setServerResourceType(int serverResourceType) {
		this.serverResourceType = serverResourceType;
	}

	public String getResourceDescription() {
		return resourceDescription;
	}

	public void setResourceDescription(String resourceDescription) {
		this.resourceDescription = resourceDescription;
	}

	
}
