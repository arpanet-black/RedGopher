package black.arpanet.gopher.feeds;

import static black.arpanet.util.logging.ArpanetLogUtil.e;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FeedClient {
	
	private static final String EXCEPTION_MESSAGE = "Exception occurred retrieving URL: %s";	
	private static final String CONTENT_TYPE_HEADER = "Content-Type";	
	private static final String CHARSET_TEXT = "charset";
	private static final String UTF8_CHARSET_NAME = "UTF-8";
	private static final Logger LOG = LogManager.getLogger(FeedClient.class);

	public static FeedResponse readFeed(String url) {
		
		FeedResponse feedResponse = new FeedResponse();
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet httpGetRequest = new HttpGet(url);
		
		try {		
			HttpResponse httpGetResponse = client.execute(httpGetRequest);
			feedResponse.setStatusCode(httpGetResponse.getStatusLine().getStatusCode());			
			feedResponse.setData(parseEntityData(httpGetResponse.getEntity(), getCharset(httpGetResponse)));
		} catch (ClientProtocolException cpe) {
			e(LOG,String.format(EXCEPTION_MESSAGE, url),cpe);
		} catch (IOException ioe) {
			e(LOG,String.format(EXCEPTION_MESSAGE, url),ioe);
		}
		
		return feedResponse;
	}

	private static Charset getCharset(HttpResponse httpGetResponse) {
		Header[] headers = httpGetResponse.getHeaders(CONTENT_TYPE_HEADER);
		
		if(headers == null || headers.length < 1) {
			return Charset.defaultCharset();
		}
		
		try {
			Charset cs = null;
			
			
			for(Header header : headers) {
				if(header.getValue().contains(CHARSET_TEXT)) {
					cs = Charset.forName(header.getValue().split(";")[1].split("=")[1]);
					break;
				}
			}
			
			
			if(cs != null) {
				return cs;
			}
		} catch(IllegalArgumentException iae) {}
		
		return Charset.forName(UTF8_CHARSET_NAME);
	}

	protected static String parseEntityData(HttpEntity entity, Charset charset) throws UnsupportedOperationException, IOException {
		String data;
		
		List<String> lines = IOUtils.readLines(entity.getContent(), charset);
		
		data = StringUtils.join(lines.toArray(), '\n');
		
		return data;
	}
}
