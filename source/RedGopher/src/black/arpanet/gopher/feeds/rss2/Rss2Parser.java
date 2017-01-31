package black.arpanet.gopher.feeds.rss2;

import static black.arpanet.util.logging.ArpanetLogUtil.dot;
import static black.arpanet.util.logging.ArpanetLogUtil.e;
import static black.arpanet.util.logging.ArpanetLogUtil.t;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class Rss2Parser {
	
	private static final String CHANNEL_ELEMENT_NAME = "channel";
	private static final Logger LOG = LogManager.getLogger(Rss2Parser.class);

	public static Rss2Channel parse(String data) {
		t(LOG, "In: Rss2Channel.parse(data)");
		
		Rss2Channel channel = new Rss2Channel();
		
		SAXBuilder builder = new SAXBuilder();
		try {		
			
			dot(LOG, String.format("Parsing XML: %s", data), "Parsing XML.");
			
			ByteArrayInputStream dataInput = new ByteArrayInputStream(data.getBytes());
			InputStreamReader utfInStream = new InputStreamReader(dataInput, "UTF-8");
			Document doc = builder.build(utfInStream);
			Element root = doc.getRootElement();
			Element channelElement = root.getChild(CHANNEL_ELEMENT_NAME);
			
			for(Element element : channelElement.getChildren()) {
				switch(ChannelChildElement.fromString(element.getName())) {
				case CATEGORY: channel.setCategory(element.getValue());
					break;
				case COPYRIGHT: channel.setCopyright(element.getValue());
					break;
				case DESCRIPTION: channel.setDescription(element.getValue());
					break;
				case DOCS: channel.setDocs(element.getValue());
					break;
				case GENERATOR: channel.setGenerator(element.getValue());
					break;
				case IMAGE: channel.setImage(parseRss2Image(element));
					break;
				case ITEM: channel.getItems().add(parseRss2Item(element));
					break;
				case LANGUAGE: channel.setLanguage(element.getValue());
					break;
				case LASTBUILDDATE: channel.setLastBuildDate(element.getValue());
					break;
				case LINK: channel.setLink(element.getValue());
					break;
				case MANAGINGEDITOR: channel.setManagingEditor(element.getValue());
					break;
				case PUBDATE: channel.setPubDate(element.getValue());
					break;
				case TITLE: channel.setTitle(element.getValue());
					break;
				case TTL: channel.setTtl(element.getValue());
					break;
				case WEBMASTER: channel.setWebMaster(element.getValue());
					break;
				default: //Do nothing
					break;
				};
			}
			
		} catch (JDOMException jdome) {
			e(LOG,jdome.getMessage(),jdome);
		} catch (IOException ioe) {
			e(LOG,ioe.getMessage(),ioe);
		}
		
		t(LOG, "Out: Rss2Channel.parse(data)");
		return channel;
	}

	private static Rss2Image parseRss2Image(Element imageElement) {
		Rss2Image image = new Rss2Image();
		

		for(Element el : imageElement.getChildren()) {
			switch(ImageChildElement.fromString(el.getName())) {
			case DESCRIPTION: image.setDescription(el.getValue());
				break;
			case HEIGHT: image.setHeight(el.getValue());
				break;
			case LINK: image.setLink(el.getValue());
				break;
			case TITLE: image.setTitle(el.getValue());
				break;
			case URL: image.setUrl(el.getValue());
				break;
			case WIDTH: image.setWidth(el.getValue());
				break;
			default: //Do nothing
				break;
				
			}
		}
		
		return image; 
	}

	private static Rss2Item parseRss2Item(Element itemElement) {
		Rss2Item item = new Rss2Item();
		
		for(Element el : itemElement.getChildren()) {
			switch(ItemChildElement.fromString(el.getName())) {
			case AUTHOR: item.setAuthor(el.getValue());
				break;
			case CATEGORY: item.setCategory(el.getValue());
				break;
			case COMMENTS: item.setComments(el.getValue());
				break;
			case DESCRIPTION: item.setDescription(el.getValue());
				break;
			case ENCLOSURE: item.setEnclosure(parseRss2Enclosure(el));
				break;
			case GUID: item.setGuid(el.getValue());
				break;
			case LINK: item.setLink(el.getValue());
				break;
			case PUBDATE: item.setPubDate(el.getValue());
				break;
			case SOURCE: item.setSource(el.getValue());
				break;
			case TITLE: item.setTitle(el.getValue());
				break;
			case ENCODED: item.setEncoded(el.getValue());
				break;
			case CREATOR: item.setCreator(el.getValue());
				break;
			default: //Do nothing
				break;
			
			}
		}
		
		return item;
	}

	private static Rss2Enclosure parseRss2Enclosure(Element enclosureElement) {
		Rss2Enclosure enclosure = new Rss2Enclosure();
		
		for(Element el : enclosureElement.getChildren()) {
			switch(EnclosureChildElement.fromString(el.getName())) {
			case LENGTH: enclosure.setLength(el.getValue());
				break;
			case TYPE: enclosure.setType(el.getValue());
				break;
			case URL: enclosure.setUrl(el.getValue());
				break;
			default: //Do nothing
				break;
			
			}
		}
		
		return enclosure;
	}

}
