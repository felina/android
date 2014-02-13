package com.felina.android;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ImageUploadService extends IntentService {

	public ImageUploadService() {
		super("ImageUploadService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle data = intent.getExtras();
		boolean[] selection = data.getBooleanArray(GalleryFragment.EXTRA_SELECTION);
		CharSequence[] paths = data.getCharSequenceArray(GalleryFragment.EXTRA_PATHS);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		int count=0;
		for(int i = 0; i<selection.length; i++) {
			if(selection[i]) {
				File f = new File(paths[i].toString());
				FileBody fb = new FileBody(f);
				builder.addPart("file"+i, fb);
				count++;
			}
		}
		
		Intent i = new Intent(getApplicationContext(), MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
		
		//TODO older api compatibility
		NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder mBuilder = new Notification.Builder(getApplicationContext());
		mBuilder.setOngoing(true)
				.setContentTitle("Image Upload")
				.setContentIntent(pendingIntent)
				.setAutoCancel(true)
				.setContentText("Uploading "+count+" images.")
				.setSmallIcon(R.drawable.shutter)
				.setProgress(0, 0, true);
		
		mManager.notify(0, mBuilder.build());
		Log.d("UploadImageService:", "Sent notification");
		 
		HttpEntity fileEntity = builder.build();
		HttpPost httpUpload = new HttpPost("/upload/img");
		httpUpload.setEntity(fileEntity);
		HttpRequestClient mClient = new HttpRequestClient(this);
		mClient.executeUpload(httpUpload);
		
		mBuilder.setOngoing(false)
				.setContentText("Upload complete")
				.setProgress(0, 0, false);
		mManager.notify(0, mBuilder.build());
	}

}
