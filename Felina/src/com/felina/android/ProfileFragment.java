package com.felina.android;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
	private ArrayList<Bitmap> imageList;
	private static FelinaClient fClient;
	private ImageAdapter mImageAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.profile_fragment, container, false);

		if(MainActivity.fClient != null) {
			fClient = MainActivity.fClient;
		} else {
			fClient = new FelinaClient(getActivity());
		}
		
		mImageAdapter = new ImageAdapter();
		list = (ListView) rootView.findViewById(R.id.imageList);
		idList = new ArrayList<String>();
		imageList = new ArrayList<Bitmap>();
		list.setAdapter(mImageAdapter);
		getImageList(Constants.RETRY_LIMIT);				
		return rootView;
	}

	private void getImageList(final int retry) {
		if(retry==0) {
			Log.d("ProfileFragment", "failed");
			return;
		}
		
		fClient.getImageList(new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(JSONObject response) {
				try {
					if (response.getBoolean("res")) {
						Log.d("ProfileFragment", "got list");
						JSONArray images = response.getJSONArray("images");
						for( int i = 0; i<images.length(); i++) {
							String id = images.getJSONObject(i).getString("imageid");
							getImage(getActivity(), id, Constants.RETRY_LIMIT);
							Log.d("ProfileFragment", id);
						}
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
				e.printStackTrace();
				getImageList(retry-1);
			}
			
		});
	}
	
	private void getImage(final Context context, final String id, final int retry) {
		if(retry == 0) {
			return;
		}
		
		fClient.getImage(id, new FileAsyncHttpResponseHandler(context){
			
			@Override
			public void onSuccess(File file) {
				if(file!=null) {
					imageList.add(BitmapFactory.decodeFile(file.getAbsolutePath()));	
					mImageAdapter.notifyDataSetChanged();
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
			if(position < imageList.size()) {
				if(imageList.get(position)!=null){
					holder.image.setImageBitmap(imageList.get(position));
				} else {
					holder.image.setImageResource(R.drawable.ic_loading_sun);
				}
			}
//			holder.text.setText("Item " + (position + 1));
			return view;
		}
	}
}
