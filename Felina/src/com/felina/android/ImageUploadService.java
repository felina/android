package com.felina.android;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

public class ImageUploadService extends IntentService {

	public ImageUploadService() {
		super("ImageUploadService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Bundle data = intent.getExtras();
		boolean[] selection = data.getBooleanArray(GalleryFragment.EXTRA_SELECTION);
		CharSequence[] paths = data.getCharSequenceArray(GalleryFragment.EXTRA_PATHS);
		
		for(int i=0; i<selection.length; i++) {
			if(selection[i]) {
				
			}
		}
		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		for(int i = 0; i<selection.length; i++) {
			if(selection[i]) {
				File f = new File(paths[i].toString());
				FileBody fb = new FileBody(f);
				builder.addPart("file"+i, fb);
			}
		}
		
		HttpEntity fileEntity = builder.build();
		HttpPost httpUpload = new HttpPost("/upload/img");
		httpUpload.setEntity(fileEntity);
		HttpRequestClient mClient = new HttpRequestClient(this);
		mClient.executeUpload(httpUpload);
	}

}
