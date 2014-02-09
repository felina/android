package com.felina.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class MainActivity extends SherlockFragmentActivity implements ActionBar.TabListener {
	
	private static final int REQUEST_LOGIN = 1001;
	private SectionAdapter mAdapter;
	private ViewPager mViewPager;
	public static HttpRequestClient mClient ;
	public static final String INPUT_USERNAME = "username";
	public static final String INPUT_PASSWORD = "password";
	public static final String PREFERENCE_NAME = "credentials";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mClient = new HttpRequestClient(this);
        //Create the adapter to fragments for the app sections.
        mAdapter = new SectionAdapter(getSupportFragmentManager());

        if(!mClient.loginCheck()) {
        	System.out.println("LOGIN ACTIVITY");
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent, REQUEST_LOGIN);	
        }        
        
        // get the action bar. 
        final ActionBar actionBar = getSupportActionBar();
        
        actionBar.setHomeButtonEnabled(false);
        
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == REQUEST_LOGIN) {
    		switch(resultCode) {
    		
    		case RESULT_OK:
    		break;
    		
    		case RESULT_CANCELED:
    			finish();
    			break;
    		}
    	}
    }

	@Override
	public void onTabSelected(Tab tab,
			android.support.v4.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab,
			android.support.v4.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab,
			android.support.v4.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
	
	public static class SectionAdapter extends FragmentStatePagerAdapter {
			
		public SectionAdapter(FragmentManager fm) {
			super(fm);
		}
	
		@Override
		public Fragment getItem(int i) {
			switch(i) {
			
			case 0: return new GalleryFragment(); 
			
			default: return new DummyFragment();
			}
		}
		
		@Override
		public int getCount() {
			return 3;
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
