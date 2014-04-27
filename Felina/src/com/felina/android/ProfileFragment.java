package com.felina.android;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class ProfileFragment extends SherlockFragment {

	static public int CACHE_SIZE = 40;
	static public int CACHE_MARGIN = 10;
	public ListView list;
	public JSONArray images;
	private HashMap<String, Bitmap> imageMap;
	private HttpRequestClient mClient;
	private ArrayList<String> idList;
	private int lastVisibleItem;
	private String host = "http://ec2-54-194-186-121.eu-west-1.compute.amazonaws.com/img/";
	

	private OnScrollListener mScrollListener = new OnScrollListener() {
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			
			int size = images.length()-1;
			int start = (firstVisibleItem - CACHE_MARGIN > 0)? (firstVisibleItem - CACHE_MARGIN) : 0;
			int stop = (firstVisibleItem + visibleItemCount);
			stop = (stop + CACHE_MARGIN) < size ? (stop + CACHE_MARGIN) : size;
			int leftover = CACHE_SIZE - (stop - start);

			if (firstVisibleItem > lastVisibleItem) {
				//scroll up
				start = (start - leftover) > 0 ? (start - leftover) : 0;
				
				for(int i = stop; i<=start; i--)
					getImage(i);
			}
			else {
				//scroll down
				stop = (stop + leftover) < size ? (stop + leftover) : size;
				for(int i = start; i<=stop; i++)
					getImage(i);
			}
			
			lastVisibleItem = firstVisibleItem;
		}
	}; 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.profile_fragment, container, false);

		mClient = new HttpRequestClient(getActivity());
		images = mClient.getImageList();
		
		imageMap = new HashMap<String, Bitmap>();
		idList = new ArrayList<String>();
		
		for( int i = 0; i<(images.length()>10?10:images.length()); i++) {
			try {
				String id = images.getJSONObject(i).getString("imageid");
				idList.add(host+id);
//				addToMap(id);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		ImageAdapter mImageAdapter = new ImageAdapter();
				
		lastVisibleItem = 0;

		list = (ListView) rootView.findViewById(R.id.imageList);
		list.setAdapter(mImageAdapter);
//		list.setOnScrollListener(mScrollListener);
		return rootView;
	}

	public Bitmap addToMap(String id) {
		System.out.println("Profile Page: Downloading "+id );
		Bitmap b = mClient.getImage(id);
		System.out.println("Bitmap is "+((b==null)?"null":"not null"));
		if(imageMap.size()>=CACHE_SIZE){
			// prune the top of the list, oldest.
			imageMap.remove(idList.remove(0));
		}

		imageMap.put(id, b);
		idList.add(id);
		return b;
	}
	
	public Bitmap getImage(int i) {
		String id = null;
		try {
			id = images.getJSONObject(i).getString("imageid");
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(imageMap.containsKey(id)) {
			//to add the id to the bottom of the list
			idList.add(idList.remove(i));
			return imageMap.get(id);
		}
		
		return addToMap(id);
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
			public TextView text;
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

//			holder.text.setText("Item " + (position + 1));
			return view;
		}
	}
}
