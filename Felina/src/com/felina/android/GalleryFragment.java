package com.felina.android;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;

public class GalleryFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor>  {
	
	private static final int IMAGE_LOADER = 0;
	private static final String IMAGE = "image";
	private static final String CHECKBOX = "checkbox";
	public static final String EXTRA_SELECTION = "extra_selection";
	public static final String EXTRA_PATHS = "extra_paths";
	private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
	private GridView gallery;
	private int count;
	private Bitmap[] thumbnails;
	private String[] paths;
	private boolean[] selection;
	private GalleryAdapter mAdapter;	
	private int selectedCount;
	private Button uploadBtn;
	private ImageView cameraBtn;
	private Button selectBtn;
	public static String mImagePath;
	
	private OnClickListener mBtnListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.cameraBtn:
				takePicture();
				break;
				
			case R.id.uploadBtn:
				uploadImages();
				break;
				
			case R.id.selectBtn:
			}
			
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.gallery_fragment, container, false);
		
		mCallbacks = this;
		getSherlockActivity().getSupportLoaderManager().initLoader(IMAGE_LOADER, null, mCallbacks);

		gallery = (GridView) rootView.findViewById(R.id.imageGrid);
		uploadBtn = (Button) rootView.findViewById(R.id.uploadBtn);
		cameraBtn = (ImageView) rootView.findViewById(R.id.cameraBtn);
		selectBtn = (Button) rootView.findViewById(R.id.selectBtn);

		uploadBtn.setOnClickListener(mBtnListener);
		cameraBtn.setOnClickListener(mBtnListener);
		selectBtn.setOnClickListener(mBtnListener);

		uploadBtn.setEnabled(false);
		
		selectedCount = 0;
		
		mAdapter = new GalleryAdapter();
		gallery.setAdapter(mAdapter);
				
		return rootView;
	}
	

	   public void takePicture() {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			
			if(intent.resolveActivity(getActivity().getPackageManager()) != null) {
				File f = null;
				try {
					f = createFile();
				} catch (IOException e) {
					System.out.println(e);
					System.exit(0);
				}
				
				if (f != null) {
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
					getActivity().startActivityForResult(intent, Constants.REQUEST_IMAGE_CAPTURE);
				}
			}
		}
	   

	private File createFile() throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = new File(dir, timeStamp+".jpg");
		mImagePath = image.getAbsolutePath();
		return image;
	}

	private void uploadImages() {
		ImageUploader mUploader = new ImageUploader(getActivity(), selection, paths);
		mUploader.execute();
	}
	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("CAMERARESULT: ", "got something "+requestCode );		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		Uri mDataUrl = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		String[] mProjection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
		String mSortOrder = MediaStore.Images.Media.DATE_TAKEN;
		
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
		final Cursor c = mCursor;
		switch (loader.getId()) {
        case IMAGE_LOADER: 
        	new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
		        	processCursor(c);
				}
			}).start();
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
		selection = new boolean[count];
		for (int i = 0; i < count; i++) {
			mCursor.moveToPosition(i);
			int id = mCursor.getInt(idColIndex);
			int dataColIndex = mCursor.getColumnIndex(MediaStore.Images.Media.DATA);
			thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(getActivity().getContentResolver(), 
					id, MediaStore.Images.Thumbnails.MINI_KIND, null);
			paths[i] = mCursor.getString(dataColIndex);
			selection[i] = false;
		}
		
		gallery.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	
	public class GalleryAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private OnClickListener mListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int id = v.getId();
				if(v.getTag().equals(IMAGE)) {
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.parse("file://" + paths[id]), "image/*");
					startActivity(intent);
				}
				else if(v.getTag().equals(CHECKBOX)) {
					CheckBox checkBox = (CheckBox) v;
					if(selection[id]) {
						selection[id] = false;
						checkBox.setChecked(false);
						selectedCount--;
					}
					else {
						selection[id] = true;
						checkBox.setChecked(true);
						selectedCount++;
					}
					uploadBtn.setEnabled(selectedCount>0);
				}	
			}
		};
		
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
			imageView.setOnClickListener(mListener);
			imageView.setId(i);
			imageView.setTag(IMAGE);
			
			checkBox.setChecked(selection[i]);
			checkBox.setOnClickListener(mListener);
			checkBox.setTag(CHECKBOX);
			checkBox.setId(i);
			return view;
		}
		
	}
}
