package com.felina.android;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.felina.android.api.FelinaClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

public class ProfileFragment extends SherlockFragment {

	public ListView list;
	private ArrayList<String> idList;
	private ArrayList<String> idStack;
	private ArrayList<Bitmap> imageList;
	private static FelinaClient fClient;
	private ImageAdapter mImageAdapter;
	private Point size;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.profile_fragment, container, false);
		Log.d("ProfileFragment", "onCreate");
		if(MainActivity.fClient != null) {
			fClient = MainActivity.fClient;
		} else {
			fClient = new FelinaClient(getActivity());
		}
		
		WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		size = new Point();
		display.getSize(size);
		mImageAdapter = new ImageAdapter();
		list = (ListView) rootView.findViewById(R.id.imageList);
		idList = new ArrayList<String>();
		idStack = new ArrayList<String>();
		imageList = new ArrayList<Bitmap>();
		list.setAdapter(mImageAdapter);
		getImageList(Constants.RETRY_LIMIT);				
		return rootView;
	}

	/**
	 * Downloads the list of image id's belonging to the user.
	 * @param retry
	 */
	private void getImageList(final int retry) {
		Log.d("ProfileFragment", "getImageList "+retry);
		if(retry==0) {
			Log.d("ProfileFragment", "failed");
			return;
		}
		
		fClient.getImageList(new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(JSONObject response) {
				Log.d("ProfileFragment", "some response");
				try {
					if (response.getBoolean("res")) {
						Log.d("ProfileFragment", "got list");
						JSONArray images = response.getJSONArray("images");
						for( int i = 0; i<images.length(); i++) {
							String id = images.getJSONObject(i).getString("imageid");
							idList.add(id);
							idStack.add(id);
							Log.d("ProfileFragment", id);
						}
						mImageAdapter.notifyDataSetChanged();
						startImageDownload(getActivity());
						//startImageDownload(getActivity());
						//startImageDownload(getActivity());
						Log.d("ProfileFragment", "done images");
					}
					else {
						Log.d("ProfileFragment", "not list");
						getImageList(retry-1);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					getImageList(retry-1);
				}
 			}
			
			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				Log.d("ProfileFragment", "some response-");
				e.printStackTrace();
				getImageList(retry-1);
			}
			
		});
		Log.d("ProfileFragment", "returning");
	}	
	
	/**
	 * Calculates the required scaling size for bitmap decoding
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return the scaled size
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		//Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
	
	/**
	 * Decodes the bitmap from file, scaled to required size.
	 * @param f
	 * @param reqWidth
	 * @param reqHeight
	 * @return the scaled bitmap.
	 */
	public static Bitmap decodeSampledBitmapFromResource(File f, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(f.getAbsolutePath(), options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(f.getAbsolutePath(), options);
	}
	
	/**
	 * Starts downloading a new image if there is an id left in the stack
	 * @param context
	 */
	private void startImageDownload(Context context) {
		Log.d("ProfileFragment", "startDownload");
				synchronized (idStack) {
			if(!idStack.isEmpty()) {
				getImage(context, idStack.remove(idStack.size()-1), Constants.RETRY_LIMIT);
			}
		}
	}
	
	/**
	 * Downloads the image specified by the id, stores it and notifies the UI.
	 * @param context
	 * @param id
	 * @param retry
	 */
	private void getImage(final Context context, final String id, final int retry) {
		Log.d("ProfileFragment", "getImage "+retry);
		if(retry == 0) {
			return;
		}
		
		fClient.getImage(id, new FileAsyncHttpResponseHandler(context){
			
			@Override
			public void onSuccess(File file) {
				Log.d("ProfileFragment", "got image");
				if(file!=null) {
					Log.d("ProfileFragment", "not null");
					imageList.add(decodeSampledBitmapFromResource(file, 300, 500));
					Log.d("ProfileFragment", ""+size.x);
					mImageAdapter.notifyDataSetChanged();
					startImageDownload(context);
				}
			}
			
			@Override
			public void onFailure(Throwable e, File response) {
				e.printStackTrace();
				getImage(context, id, retry-1);
			}
			
		});
	}
	
//	public class ImageAdapter extends BaseAdapter {
//		private LayoutInflater mInflater;
//		
//		public ImageAdapter() {
//			mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		}
//		
//		@Override
//		public int getCount() {
//			return images.length();
//		}
//
//		@Override
//		public Object getItem(int i) {
//			return i;
//		}
//
//		@Override
//		public long getItemId(int i) {
//			return i;
//		}
//
//		@Override
//		public View getView(int i, View view, ViewGroup container) {
////			view = mInflater.inflate(R.layout.list_item, container, false);
////			ImageView image = (ImageView) view.findViewById(R.id.listImage);
////			Bitmap src = getImage(i);
////			if(src!=null) {
////				Bitmap b = Bitmap.createScaledBitmap(src, 200, 200, true);
////				image.setImageBitmap(b);
////			}
////			else{
////				image.setImageResource(R.drawable.ic_launcher);
////			}
////			return view;
//		}
//		
//	}
	
	class ImageAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		
		public ImageAdapter() {
			mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		private class ViewHolder {
//			public TextView text;
			public ImageView image;
		}

		@Override
		public int getCount() {
			return idList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ViewHolder holder;
			if (convertView == null) {
				view = mInflater.inflate(R.layout.list_item, parent, false);
				holder = new ViewHolder();
//				holder.text = (TextView) view.findViewById(R.id.text);
				holder.image = (ImageView) view.findViewById(R.id.listImage);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}
			if(position < imageList.size() && imageList.get(position)!=null){
				holder.image.setImageBitmap(imageList.get(position));
			} else {
				holder.image.setImageResource(R.drawable.ic_loading_sun);
			}
//			holder.text.setText("Item " + (position + 1));
			return view;
		}
	}
}
