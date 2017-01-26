package black.arpanet.gopher.db.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name="server_file_type")
@NamedQueries({
	@NamedQuery(name="ServerFileType.deleteAll", query="delete from ServerFileType sft"),
	@NamedQuery(name="ServerFileType.findByFileExtension", query="select sft from ServerFileType sft where sft.fileExtension = :ext")
})
public class ServerFileType {

	@Id
	@GeneratedValue
	private int id;
	
	@Column(name="file_extension")
	private String fileExtension;
	
	@ManyToOne
	private ResourceDescriptor resourceDescriptor;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public ResourceDescriptor getResourceDescriptor() {
		return resourceDescriptor;
	}

	public void setResourceDescriptor(ResourceDescriptor resourceDescriptor) {
		this.resourceDescriptor = resourceDescriptor;
	}
	
	
}
