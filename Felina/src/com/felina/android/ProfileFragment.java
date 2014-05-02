package com.felina.android;

import java.io.File;
import java.util.ArrayList;

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

public class ProfileFragment extends SherlockFragment {

	public ListView list;
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
		imageList = new ArrayList<Bitmap>();
		list.setAdapter(mImageAdapter);
		startImageDownload(getActivity());
		return rootView;
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
				synchronized (MainActivity.idStack) {
			if(!MainActivity.idStack.isEmpty()) {
				getImage(context, MainActivity.idStack.remove(MainActivity.idStack.size()-1), Constants.RETRY_LIMIT);
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
			return MainActivity.idList.size();
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
