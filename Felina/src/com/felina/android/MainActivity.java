package com.felina.android;

import java.io.File;

import android.app.Activity;
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

public class MainActivity extends SherlockFragmentActivity implements
		ActionBar.TabListener {

	private static final int REQUEST_LOGIN = 1001;
	private static final int REQUEST_IMAGE_CAPTURE = 1002;
	private SectionAdapter mAdapter;
	private ViewPager mViewPager;
	private HttpRequestClient mClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mClient = new HttpRequestClient(this);
		// Create the adapter to fragments for the app sections.
		mAdapter = new SectionAdapter(getSupportFragmentManager());

		if (!mClient.loginCheck()) {
			Intent loginIntent = new Intent(this, LoginActivity.class);
			startActivityForResult(loginIntent, REQUEST_LOGIN);
		}

		// get the action bar.
		final ActionBar actionBar = getSupportActionBar();

		actionBar.setHomeButtonEnabled(false);

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mAdapter);
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		for (int i = 0; i < mAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(mAdapter.getPageTitle(i)).setTabListener(this));
		}

		mViewPager.setCurrentItem(1);

	}

	// Logout function
	private void logout() {
		SharedPreferences prefs = getSharedPreferences(
				HttpRequestClient.PREFERENCE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(HttpRequestClient.INPUT_USERNAME, "");
		editor.putString(HttpRequestClient.INPUT_PASSWORD, "");
		editor.commit();
		Intent loginIntent = new Intent(this, LoginActivity.class);
		startActivity(loginIntent, null);
		finish();
	}

	// Takes the image from GalleryFragment and starts the media action scan
	private void addToGallery(final String mImagePath) {
		Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File f = new File(mImagePath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
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
		if (item.getItemId() == R.id.action_logout) {
			logout();
			return true;
		} else {
			return super.onOptionsItemSelected(item);

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("MainActivityFelina", "something happened");
		if (requestCode == REQUEST_LOGIN) {
			Log.d("MainActivityFelina", "login");
			switch (resultCode) {

			case RESULT_OK:
				break;

			case RESULT_CANCELED:
				finish();
				break;
			}
		} else if (requestCode == REQUEST_IMAGE_CAPTURE) {
			Log.d("MainActivityFelina", resultCode + " " + RESULT_CANCELED
					+ " " + RESULT_OK + " " + RESULT_FIRST_USER);
			if (resultCode == Activity.RESULT_OK) {
				System.out.println("allgood");
				File f = new File(GalleryFragment.mImagePath);
				if ((f.length()) == 0) {
					f.delete();
				}
				addToGallery(GalleryFragment.mImagePath);
			}
		} else {
			Log.d("MainActivityFelina", "something else " + requestCode);
			Log.d("MainActivityFelina", resultCode + " " + RESULT_CANCELED
					+ " " + RESULT_OK + " " + RESULT_FIRST_USER);

			super.onActivityResult(resultCode, resultCode, data);
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
			switch (i) {

			case 0:
				return new GalleryFragment();

				// Remember to uncomment before commit
				// case 2: return new ProfileFragment();

			default:
				return new DummyFragment();
			}
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return "Upload Images";
			case 1:
				return "News Feed";
			case 2:
				return "Profile";
			default:
				return super.getPageTitle(position);
			}
		}

	}

	public static class DummyFragment extends SherlockFragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.dummy_fragment,
					container, false);
			return rootView;
		}
	}
}
