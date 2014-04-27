package com.felina.android;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
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
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
		httpHost = new HttpHost("nl.ks07.co.uk", 5000);
//		httpHost = new HttpHost("ec2-54-194-186-121.eu-west-1.compute.amazonaws.com");
		context = c;
	}		

	public JSONObject execute(final HttpRequest req) {
		ExecutorService ex = Executors.newSingleThreadExecutor();

		Callable<JSONObject> callable = new Callable<JSONObject>() {
			
			@Override
			public JSONObject call() throws Exception {
				// TODO Auto-generated method stub
				JSONObject res = null;
				try {
				    HttpResponse response = mClient.execute(httpHost, req);
				    res = getJSON(response);
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
		
	    Future<JSONObject> future = ex.submit(callable);
	    JSONObject res = null;
		try {
			res = future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    ex.shutdown();
	    
	    return res;
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
		JSONObject res = execute(httpLoginCheck);
		Boolean b = false;
		try {
			if(res!= null)
			b = res.getBoolean("res");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(!b) {
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
			
		    res = execute(httpLogin);
		    try {
				b = res.getBoolean("res");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return b;
	}
	
	public Boolean login(String username, String password) {
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
		
	    JSONObject res = execute(httpLogin);
	    Boolean b = false;
	    try {
			b = res.getBoolean("res");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return b;
	}
	
	public Boolean register(String name, String username, String password) {
		ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(3);
		nameValuePair.add(new BasicNameValuePair("name", name));
		nameValuePair.add(new BasicNameValuePair("email", username));
	    nameValuePair.add(new BasicNameValuePair("pass", password));
	    HttpPost httpRegister = new HttpPost("/register");
	    
	    try {
		    httpRegister.setEntity(new UrlEncodedFormEntity(nameValuePair));
		} catch (UnsupportedEncodingException e) {
		    // writing error to Log
		    e.printStackTrace();
		}
	    
	    JSONObject res = execute(httpRegister);
	    Boolean b = false;
	    try {
			b = res.getBoolean("res");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return b;
	}
	
	public JSONArray getImageList() {
		final HttpGet httpGetImages = new HttpGet("/images");
		loginCheck();
		JSONObject res = execute(httpGetImages);
		JSONArray images = null;
		Boolean b = false;
		try {
			b = res.getBoolean("res");
			images = res.getJSONArray("images");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(b){
			return images;
		}
		
		return null;
//		bitmap = BitmapFactory.decodeStream((InputStream) response.getEntity().getContent());
	}
	
	public Bitmap getImage(final String id) {
		ExecutorService ex = Executors.newSingleThreadExecutor();

		Callable<Bitmap> callable = new Callable<Bitmap>() {
			
			@Override
			public Bitmap call() throws Exception {
				// TODO Auto-generated method stub
				Bitmap bitmap = null;
				try {
					loginCheck();
					HttpGet httpGetImage = new HttpGet("/img/"+id);
				    HttpResponse response = mClient.execute(httpHost, httpGetImage);
				    HttpEntity entity = response.getEntity();
				    System.out.println("entity is "+((entity==null)?"null":"not null"));
//				    BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
					bitmap = BitmapFactory.decodeStream(new FlushedInputStream(entity.getContent()));
				} catch (ClientProtocolException e) {
				    e.printStackTrace();
				} catch (IOException e) {
				    e.printStackTrace();
				}
				
				return bitmap;
			}
		};
		
	    Future<Bitmap> future = ex.submit(callable);
	    Bitmap b = null;
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
	
	private static JSONObject getJSON(HttpResponse response) {
		JSONTokener tokener = null;
		JSONObject json = null;
		String s="";
		try {
			s = EntityUtils.toString(response.getEntity());
			System.out.println("JSON: : " + s);
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
	
	static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
}
