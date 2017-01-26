package black.arpanet.gopher.feeds.rss2;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author nicholas.waun
 * @see https://validator.w3.org/feed/docs/rss2.html
 *
 */
public class Rss2Channel {

	//Required elements
	private String title;
	private String link;
	private String description;
	
	//Optional elements
	private Set<Rss2Item> items;
	private String language;
	private String copyright;
	private String managingEditor;
	private String webMaster;
	private String pubDate;
	private String lastBuildDate;
	private String category;
	private String generator;
	private String docs;	
	private String ttl;
	private Rss2Image image;
	
	//Ignored Elements
//	private String cloud;
//	private String textInput;
//	private String skipHours;
//	private String skipDays;
	
	public Rss2Channel() {
		this.items = new HashSet<Rss2Item>();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<Rss2Item> getItems() {
		return items;
	}

	public void setItems(Set<Rss2Item> items) {
		this.items = items;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public String getManagingEditor() {
		return managingEditor;
	}

	public void setManagingEditor(String managingEditor) {
		this.managingEditor = managingEditor;
	}

	public String getWebMaster() {
		return webMaster;
	}

	public void setWebMaster(String webMaster) {
		this.webMaster = webMaster;
	}

	public String getPubDate() {
		return pubDate;
	}

	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	public String getLastBuildDate() {
		return lastBuildDate;
	}

	public void setLastBuildDate(String lastBuildDate) {
		this.lastBuildDate = lastBuildDate;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getGenerator() {
		return generator;
	}

	public void setGenerator(String generator) {
		this.generator = generator;
	}

	public String getDocs() {
		return docs;
	}

	public void setDocs(String docs) {
		this.docs = docs;
	}

	public String getTtl() {
		return ttl;
	}

	public void setTtl(String ttl) {
		this.ttl = ttl;
	}

	public Rss2Image getImage() {
		return image;
	}

	public void setImage(Rss2Image image) {
		this.image = image;
	}	
	
	
}
