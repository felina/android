package com.felina.android;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;

public class GalleryFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor>  {
	
	private static final int IMAGE_LOADER = 0;
	private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
	private GridView gallery;
	private int count;
	private Bitmap[] thumbnails;
	private String[] paths;
	private Boolean[] selection;
	private GalleryAdapter mAdapter;
//	private
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.gallery_fragment, container, false);
		
		mCallbacks = this;
		getSherlockActivity().getSupportLoaderManager().initLoader(IMAGE_LOADER, null, mCallbacks);

		gallery = (GridView) rootView.findViewById(R.id.imageGrid);
		mAdapter = new GalleryAdapter();
		gallery.setAdapter(mAdapter);
		return rootView;
	}
	

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		Uri mDataUrl = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		String[] mProjection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
		String mSortOrder = MediaStore.Images.Media._ID;
		
		switch (loaderID) {
        case IMAGE_LOADER:
            // Returns a new CursorLoader
            return new CursorLoader(
                        getActivity(),   // Parent activity context
                        mDataUrl,        // Table to query
                        mProjection,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        mSortOrder             // Default sort order
            		);
        default:
            // An invalid id was passed in
            return null;
		}
    }

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor mCursor) {
		// TODO Auto-generated method stub
		switch (loader.getId()) {
        case IMAGE_LOADER: 
        	processCursor(mCursor);
        	break;
		}
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private void processCursor(Cursor mCursor) {
		int idColIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
		count = mCursor.getCount();
		paths = new String[count];
		thumbnails = new Bitmap[count];
		selection = new Boolean[count];
		for (int i = 0; i < count; i++) {
			mCursor.moveToPosition(i);
			int id = mCursor.getInt(idColIndex);
			int dataColIndex = mCursor.getColumnIndex(MediaStore.Images.Media.DATA);
			thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(getActivity().getContentResolver(), 
					id, MediaStore.Images.Thumbnails.MINI_KIND, null);
			paths[i] = mCursor.getString(dataColIndex);
		}
		
	}
	
	public class GalleryAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		
		public GalleryAdapter() {
			mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return count;
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
		public View getView(int i, View view, ViewGroup parent) {
			ImageView imageView;
			CheckBox checkBox;
			view = mInflater.inflate(R.layout.gallery_item, null);
			imageView = (ImageView) view.findViewById(R.id.imageThumb);
			checkBox = (CheckBox) view.findViewById(R.id.checkBox);
			imageView.setImageBitmap(thumbnails[i]);
			return view;
		}
		
	}
}
