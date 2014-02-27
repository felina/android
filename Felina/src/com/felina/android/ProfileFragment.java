package com.felina.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;


public class ProfileFragment extends SherlockFragment {
	
	ListView list;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.profile_fragment, container, false);
		ImageAdapter mImageAdapter = new ImageAdapter();
		list = (ListView) rootView.findViewById(R.id.imageList);
		list.setAdapter(mImageAdapter);
		HttpRequestClient mClient = new HttpRequestClient(getActivity());
		 mClient.getImageList();
		return rootView;
	}

	public class ImageAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		
		public ImageAdapter() {
			mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public Object getItem(int i) {
			return i;
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup container) {
			view = mInflater.inflate(R.layout.list_item, container, false);
			ImageView image = (ImageView) view.findViewById(R.id.listImage);
//			image.setImageBitmap();
			return view;
		}
		
	}
}
