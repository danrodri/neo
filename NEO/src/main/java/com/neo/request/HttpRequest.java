package com.neo.request;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpRequest {

	private static Logger logger = LogManager.getLogger();
	
	public static InputStream sendGet(String start, String end) {
		String url = "https://api.nasa.gov/neo/rest/v1/feed?start_date=" + start + "&end_date=" + end + "&api_key=DEMO_KEY";
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response1 = null;
		InputStream in = null;
		try {
			response1 = httpclient.execute(httpGet);

			System.out.println(response1.getStatusLine());
			HttpEntity entity1 = response1.getEntity();
			
			in = entity1.getContent();
			
		} catch (IOException e) {
			logger.error("IO exception trying to execute httpGet");
			return null;
		}
		return in;
	}

}