package black.arpanet.gopher.db.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="gopher_item")
@NamedQueries({
	@NamedQuery(name="GopherItem.findAll", query="select gi from GopherItem gi order by gi.creationDate ASC"),
	@NamedQuery(name="GopherItem.findTopLevelItems", query="select gi from GopherItem gi where gi.parentPath = '/' order by gi.creationDate ASC"),
	@NamedQuery(name="GopherItem.findByGopherPath", query="select gi from GopherItem gi where gi.gopherPath = :path order by gi.creationDate ASC"),
	@NamedQuery(name="GopherItem.findByParentPath", query="select gi from GopherItem gi where gi.parentPath = :path order by gi.creationDate ASC"),
	@NamedQuery(name="GopherItem.deleteVolatileItems", query="delete from GopherItem gi where gi.persistOverRestart = false")
})
//File Type - Display Text - Selector String - Domain Name - Port - CRLF
public class GopherItem {

	@Id
	@Column(name="id")
	@GeneratedValue
	private int id;
	
	@Column(name="display_text")
	private String displayText;
	
	@Column(name="parent_path")
	private String parentPath;
	
	@Column(name="gopher_path")
	private String gopherPath;
	
	@Column(name="resource_path")
	private String resourcePath;
	
	@Column(name="domain_name")
	private String domainName;
	
	@Column(name="port")
	private int port;
	
	@Column(name="persist_over_restart")
	private boolean persistOverRestart = true;
	
	@Lob
	@Column(name="content")
	private byte[] content;
	
	@Column(name="creation_date")
	private Date creationDate;
	
	@OneToOne
	@Column(name="resource_descriptor")
	private ResourceDescriptor resourceDescriptor;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDisplayText() {
		return displayText;
	}

	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}

	public ResourceDescriptor getResourceDescriptor() {
		return resourceDescriptor;
	}

	public void setResourceDescriptor(ResourceDescriptor resourceDescriptor) {
		this.resourceDescriptor = resourceDescriptor;
	}
	
	public String getParentPath() {
		return parentPath;
	}

	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}

	public String getGopherPath() {
		return gopherPath;
	}

	public void setGopherPath(String gopherPath) {
		this.gopherPath = gopherPath;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isPersistOverRestart() {
		return persistOverRestart;
	}

	public void setPersistOverRestart(boolean persistOverRestart) {
		this.persistOverRestart = persistOverRestart;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

}
