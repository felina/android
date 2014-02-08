package com.felina.android;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class HttpRequestClient {

	private HttpClient mClient; 
	private HttpPost httpPost;
	private List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
	
	public HttpRequestClient() {
		mClient = new DefaultHttpClient();
		httpPost = new HttpPost("nl.ks07.co.uk/login");
	}
	
	private void execute() {
		nameValuePair.add(new BasicNameValuePair("mail", "test@example.com"));
	    nameValuePair.add(new BasicNameValuePair("pass", "word"));
	    
		try {
		    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
		} catch (UnsupportedEncodingException e) {
		    // writing error to Log
		    e.printStackTrace();
		}
		
		// Making HTTP Request
		try {
		    HttpResponse response = mClient.execute(httpPost);
		
		    // writing response to log
		    Log.d("Http Response:", response.toString());
		} catch (ClientProtocolException e) {
		    // writing exception to log
		    e.printStackTrace();
		} catch (IOException e) {
		    // writing exception to log
		    e.printStackTrace();
		
		}
	}
	
}
