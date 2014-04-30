package com.felina.android;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.felina.android.api.FelinaClient;
import com.loopj.android.http.JsonHttpResponseHandler;

public class ImageUploader {

	private FelinaClient fClient;
	private Notification.Builder mBuilder;
	private NotificationManager mManager;
	private ArrayList<File> mFiles;
	private int count=0;
	private int uploaded=1;
	private boolean[] selection;
	private CharSequence[] paths;
	private Context context;
	
	public ImageUploader(Context context, boolean[] selection, CharSequence[] paths) {
		this.context = context;
		this.selection = selection;
		this.paths = paths;
	}

	public void execute() {
		mFiles = new ArrayList<File>();
		for(int i = 0; i<selection.length; i++) {
			if(selection[i]) {
				File f = new File(paths[i].toString());
				mFiles.add(f);
				count++;
			}
		}
		
//		Intent i = new Intent(getApplicationContext(), MainActivity.class);
//		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
		
		//TODO older api compatibility
		mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new Notification.Builder(context);
		mBuilder.setOngoing(true)
				.setContentTitle("Image Upload")
//				.setContentIntent(pendingIntent)
				.setAutoCancel(true)
				.setContentText("Uploading "+uploaded+" out of "+count+" images.")
				.setSmallIcon(R.drawable.shutter)
				.setProgress(0, 0, true);
		
		mManager.notify(0, mBuilder.build());
		Log.d("UploadImageService:", "Sent notification");
		 		
		fClient = new FelinaClient(context);
		login(context, CredentialUtils.readEmail(context), CredentialUtils.readPassword(context), Constants.RETRY_LIMIT);

	}
	
	/**
	 * Login to the service.
	 * @param context
	 * @param email
	 * @param pass
	 * @param retry
	 */
    private void login(final Context context, final String email, final String pass, final int retry) {
		Log.d("UploadImageService:", "Login");
    	if(retry == 0) {
    		setError(uploaded, count);
    		return;
    	}
    	
    	try {
			fClient.login(email, pass, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(JSONObject response) {
					try {
						if (response.getBoolean("res")) {
							upload(context);
						} else {
							setError(uploaded, count);
						}
					} catch (JSONException e) {
						e.printStackTrace();
						login(context, email, pass, retry-1);
					}
				}
				
				@Override
				public void onFailure(Throwable e, JSONObject errorResponse) {
					login(context, email, pass, retry-1);
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * Uploads one more image from the stack if not empty.
     * @param context
     */
    private void upload(Context context) {
		Log.d("UploadImageService:", "upload");
    	if(mFiles.size()>0) {
    		startUpload(context, mFiles.remove(mFiles.size()-1), Constants.RETRY_LIMIT);
    	} else {
    		setSuccess(count);
    	}
    }
    
    /**
     * Uploads the file to the server.
     * @param context the context.
     * @param f the file to be uploaded.
     * @param retry the retry limit.
     */
    private void startUpload(final Context context, final File f, final int retry) {
		Log.d("UploadImageService:", "startUpload");
    	if(retry == 0) {
    		setError(uploaded, count);
    		return;
    	}
    	
    	fClient.postImg(f, "image/jpeg", "1", new JsonHttpResponseHandler(){
    		
    		@Override
    		public void onSuccess(int statusCode, JSONObject response) {
    			try {
					if (response.getBoolean("res")) {
						uploaded++;
						setProgress(uploaded, count);
						upload(context);
					} else {
						setError(uploaded, count);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					startUpload(context, f, retry-1);
				}
    		}
    		
    		@Override
    		public void onFailure(Throwable e, JSONObject errorResponse) {
    			e.printStackTrace();
    			try {
					Log.e("ImageUploader", errorResponse.toString(2));
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    			startUpload(context, f, retry-1);
    		}
    	});
    }
    
    /**
     * Sets the progress message on the image upload notification.
     * @param current The current image being uploaded.
     * @param total Total number of images.
     */
    private void setProgress(int current, int total) {
		Log.d("UploadImageService:", "SetProgress");
		mBuilder.setContentText("Uploading "+current+" out of "+total+" images.");
		mManager.notify(0, mBuilder.build());
    }
    
    /**
     * Sets the error message on the image upload notification.
     * @param current The image being uploaded.
     * @param total The total number of images.
     */
    private void setError(int current, int total) {
		Log.d("UploadImageService:", "SetError");
		mBuilder.setOngoing(false)
		.setContentText("Upload failed ["+(current-1)+"/"+total+"] images uploaded.")
		.setProgress(0, 0, false);
		mManager.notify(0, mBuilder.build());
    }
    
    /**
     * Set the success message on the image upload notification.
     * @param total The number of images uploaded.
     */
    private void setSuccess(int total) {
		Log.d("UploadImageService:", "SetSuccess");
		mBuilder.setOngoing(false)
		.setContentText("Upload successful ["+total+"/"+total+"] images uploaded.")
		.setProgress(0, 0, false);
		mManager.notify(0, mBuilder.build());
    }


}
