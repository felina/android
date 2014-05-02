package com.felina.android;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.felina.android.api.FelinaClient;
import com.loopj.android.http.JsonHttpResponseHandler;

public class MainActivity extends SherlockFragmentActivity implements ActionBar.TabListener {
	
	private SectionAdapter mAdapter;
	private ViewPager mViewPager;
	public static ArrayList<String> idList;
	public static ArrayList<String> idStack;
//	private HttpRequestClient mClient;
	public static FelinaClient fClient;
	private static String EMAIL;
	private static String PASS;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if(fClient == null) {
        	fClient = new FelinaClient(this);
        }
       
		idList = new ArrayList<String>();
		idStack = new ArrayList<String>();
		
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new SectionAdapter(getSupportFragmentManager());
       
        
//        mClient = new HttpRequestClient(this);
        //Create the adapter to fragments for the app sections.

//        if(!mClient.loginCheck()) {
//            Intent loginIntent = new Intent(this, LoginActivity.class);
//            startActivityForResult(loginIntent, REQUEST_LOGIN);	
//        }
        
        EMAIL = CredentialUtils.readEmail(this);
        PASS = CredentialUtils.readPassword(this);
        
       login(this, EMAIL, PASS, Constants.RETRY_LIMIT); 
       
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
		
		fClient.getImageList(new JsonHttpResponseHandler() {
			
			@Override
			public void onSuccess(int statuscode, JSONObject response) {
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
//						startImageDownload(getActivity());
						setup();
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
    
    private void login(final Context context, final String email, final String pass, final int retry) {
    	
    	if(retry == 0) {
			Log.d("MainActivity", "Login failed");
    		return;
    	}
    	
    	try {
			fClient.login(email, pass, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(JSONObject response) {
					try {
						Log.d("MainActivity", "Login resp: "+response.toString(4));
						if (response.getBoolean("res")) {
							getImageList(Constants.RETRY_LIMIT);
						} else {
				            Intent loginIntent = new Intent(context, LoginActivity.class);
				            startActivityForResult(loginIntent, Constants.REQUEST_LOGIN);
						}
					} catch (JSONException e) {
						e.printStackTrace();
						login(context, email, pass, retry-1);
					}
				}
				
				@Override
				public void onFailure(Throwable e, JSONObject errorResponse) {
					Log.d("MainActivity", "Login failed");
					login(context, email, pass, retry-1);
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void setup() {
    	Log.d("MainActivityFelina","setup happened");
        final ActionBar actionBar = getSupportActionBar();
        
        actionBar.setHomeButtonEnabled(false);
        
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
        	@Override
        	public void onPageSelected(int position) {
        		actionBar.setSelectedNavigationItem(position);
        	}
        });

        for (int i = 0; i < mAdapter.getCount(); i++) {
    	   actionBar.addTab(
    			   actionBar.newTab()
    			   			.setText(mAdapter.getPageTitle(i))
    			   			.setTabListener(this));
       }
       
		mViewPager.setCurrentItem(1);
    }
    
    private void logout() {
    	SharedPreferences prefs = getSharedPreferences(HttpRequestClient.PREFERENCE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(HttpRequestClient.INPUT_USERNAME, "");
		editor.putString(HttpRequestClient.INPUT_PASSWORD, "");
		editor.commit();
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent, null);
        finish();
    }
	
	private void addToGallery(final String mImagePath) {
//		System.out.println("ok");
//		Uri content = Uri.fromFile(file);
//		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, content);
//		sendBroadcast(intent);
		MediaScannerConnection.scanFile(this, new String[]{mImagePath}, null, 
				new MediaScannerConnection.OnScanCompletedListener() {
			
			@Override
			public void onScanCompleted(String path, Uri uri) {
				if(mImagePath.equals(path)) {
					Log.d("MediaScanner: ", "Scan completed");
				}
			}
		});		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if(item.getItemId()==R.id.action_logout){
			logout();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
	
		}
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d("MainActivityFelina","something happened");
    	if(requestCode == Constants.REQUEST_LOGIN) {
        	Log.d("MainActivityFelina","login");
    		switch(resultCode) {
    		
    		case RESULT_OK:
    			setup();
    		break;
    		
    		case RESULT_CANCELED:
    			finish();
    			break;
    		}
    	}
    	else if ( requestCode == Constants.REQUEST_IMAGE_CAPTURE ) {
        	Log.d("MainActivityFelina", resultCode+" "+RESULT_CANCELED+" "+RESULT_OK+" "+RESULT_FIRST_USER);
    		if( resultCode == Activity.RESULT_OK ) {
				System.out.println("allgood");
				File f = new File(GalleryFragment.mImagePath);
	            if((f.length())==0) {
	                f.delete();
	            } 
	            addToGallery(GalleryFragment.mImagePath);
    		}
    	}
    	else {    		
        	Log.d("MainActivityFelina","something else "+requestCode);
        	Log.d("MainActivityFelina", resultCode+" "+RESULT_CANCELED+" "+RESULT_OK+" "+RESULT_FIRST_USER);
        	
        	super.onActivityResult(resultCode, resultCode, data);
    	}
    }

	@Override
	public void onTabSelected(Tab tab,
			android.support.v4.app.FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab,
			android.support.v4.app.FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab,
			android.support.v4.app.FragmentTransaction ft) {		
	}
	
	public static class SectionAdapter extends FragmentStatePagerAdapter {
				
		public SectionAdapter(FragmentManager fm) {
			super(fm);
		}
	
		@Override
		public Fragment getItem(int i) {
			switch(i) {
			
			case 0: return new GalleryFragment(); 
			
			case 1: return new ProfileFragment();
			
			default: return new DummyFragment();
 			}
		}
		
		@Override
		public int getCount() {
			return 2;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0: return "Upload Images";
			case 1: return "News Feed";
			case 2: return "Profile";
			default: return super.getPageTitle(position);
			}
		}
		
	}

	public static class DummyFragment extends SherlockFragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.dummy_fragment, container, false);
			return rootView;
		}
	}
}
