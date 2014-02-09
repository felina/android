package com.felina.android;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class HttpRequestClient {

	public static final String INPUT_USERNAME = "username";
	public static final String INPUT_PASSWORD = "password";
	public static final String PREFERENCE_NAME = "credentials";
	public static DefaultHttpClient mClient; 
	private HttpHost httpHost;
	private Context context;
	
	public HttpRequestClient(Context c) {
		HttpParams params = new BasicHttpParams();
	    params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		mClient = new DefaultHttpClient(params);
		httpHost = new HttpHost("ec2-54-194-186-121.eu-west-1.compute.amazonaws.com");
		context = c;
//		httpHost = new HttpHost("nl.ks07.co.uk", 5000);
	}		
		
	public Boolean execute(final HttpRequest req) {
		ExecutorService ex = Executors.newSingleThreadExecutor();

		Callable<Boolean> callable = new Callable<Boolean>() {
			
			@Override
			public Boolean call() throws Exception {
				// TODO Auto-generated method stub
				Boolean res = false;
				try {
				    HttpResponse response = mClient.execute(httpHost, req);
				    JSONObject json = getJSON(response);
				    try {
						res = json.getBoolean("res");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				    
				    //Log.d("Http Response:",  mClient.getCookieStore().getCookies().toString());
				} catch (ClientProtocolException e) {
				    // writing exception to log
				    e.printStackTrace();
				} catch (IOException e) {
				    // writing exception to log
				    e.printStackTrace();
				}
				
				return res;
			}
		};
		
	    Future<Boolean> future = ex.submit(callable);
	    Boolean b = false;
		try {
			b = future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    ex.shutdown();
	    
	    return b;
	}
	
	public Boolean executeUpload(final HttpRequest req) {
		ExecutorService ex = Executors.newSingleThreadExecutor();

		Callable<Boolean> callable = new Callable<Boolean>() {
			
			@Override
			public Boolean call() throws Exception {
				// TODO Auto-generated method stub
				Boolean res = false;
				try {
					loginCheck();
				    HttpResponse response = mClient.execute(httpHost, req);
				    JSONObject json = getJSON(response);
					Log.d("HttpRequest: ", json.toString());

//				    try {
////						res = json.getBoolean("res");
//						res = 0==json.getJSONObject("status").getInt("code");
//					} catch (JSONException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}				    
				    //Log.d("Http Response:",  mClient.getCookieStore().getCookies().toString());
				} catch (ClientProtocolException e) {
				    // writing exception to log
				    e.printStackTrace();
				} catch (IOException e) {
				    // writing exception to log
				    e.printStackTrace();
				}
				
				return res;
			}
		};
		
	    Future<Boolean> future = ex.submit(callable);
	    Boolean b = false;
		try {
			b = future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    ex.shutdown();
	    
	    return b;
	}

	public Boolean loginCheck() {
		HttpGet httpLoginCheck = new HttpGet("/logincheck");
		Boolean b = false;
		if(!execute(httpLoginCheck)) {
			SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE);
			String username = prefs.getString(INPUT_USERNAME, null);
			String password = prefs.getString(INPUT_PASSWORD, null);
			
			System.out.println("mClient: "+username+" "+password);
			ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
			nameValuePair.add(new BasicNameValuePair("email", username));
		    nameValuePair.add(new BasicNameValuePair("pass", password));
		    
		    HttpPost httpLogin = new HttpPost("/login");
			
		    try {
			    httpLogin.setEntity(new UrlEncodedFormEntity(nameValuePair));
			} catch (UnsupportedEncodingException e) {
			    // writing error to Log
			    e.printStackTrace();
			}
			
		    b = execute(httpLogin);
		}
		
		return b;
	}
	
	private static JSONObject getJSON(HttpResponse response) {
		 
		JSONTokener tokener = null;
		JSONObject json = null;
		String s="";
		try {
			s = EntityUtils.toString(response.getEntity());
			tokener = new JSONTokener(s);
			json = new JSONObject(tokener);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return json;
	}
}
