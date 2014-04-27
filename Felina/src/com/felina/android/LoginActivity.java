package com.felina.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {

	private EditText userBox;
	private EditText passBox;
	private EditText nameBox;
	private TextView errTxt;
	private Button registerBtn;
	private Button loginBtn;
	private ProgressDialog dialog;
	private String login;
	private String register;
	private String new_account;
 	private HttpRequestClient mClient;

	private OnClickListener mListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			String text;
			
			switch(v.getId()) {
			
			case R.id.loginBtn:

				String username = userBox.getText().toString();
				String password = passBox.getText().toString();
				String name = nameBox.getText().toString();
				text = loginBtn.getText().toString();

				if(text.equals(login)) {
					
					dialog.show();
					
				    Boolean b = mClient.login(username, password);
					
				    dialog.dismiss();
					
				    if(b){
				    	savePrefs(username, password);
						setResult(RESULT_OK);
						finish();
					}
					else {
						errTxt.setVisibility(View.VISIBLE);
					}
				}
				
				else if (text.equals(register)) {
					
					dialog.show();
					
				    Boolean b = mClient.register(name, username, password) ;
					
				    dialog.dismiss();
				    
				    if(b){
				    	savePrefs(username, password);
						setResult(RESULT_OK);
						finish();
					}
				    
				}
					
				break;
			
			case R.id.registerBtn:
				
				text = registerBtn.getText().toString();
				
				if(text.equals(new_account)) {
					errTxt.setVisibility(View.INVISIBLE);
					nameBox.setVisibility(View.VISIBLE);
					loginBtn.setText(register);
					registerBtn.setText(login);
				}
				
				else if(text.equals(login)) {
					nameBox.setVisibility(View.INVISIBLE);
					loginBtn.setText(login);
					registerBtn.setText(new_account);
				}
					
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		mClient = new HttpRequestClient(this);
		
		userBox = (EditText) findViewById(R.id.usernameBox);
		passBox = (EditText) findViewById(R.id.passwordBox);
		nameBox = (EditText) findViewById(R.id.nameBox);
		errTxt = (TextView) findViewById(R.id.errorTxt);
		registerBtn = (Button) findViewById(R.id.registerBtn);
		loginBtn = (Button) findViewById(R.id.loginBtn);
		
		login = getResources().getString(R.string.login);
		register = getResources().getString(R.string.register);
		new_account = getResources().getString(R.string.new_account);
		
		loginBtn.setText(login);
		registerBtn.setText(new_account);
		
		dialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
		
		loginBtn.setOnClickListener(mListener);
		registerBtn.setOnClickListener(mListener);
	}
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	private void savePrefs(String username, String password) {
		System.out.println("Save prefs");
		SharedPreferences prefs = getSharedPreferences(HttpRequestClient.PREFERENCE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(HttpRequestClient.INPUT_USERNAME, username);
		editor.putString(HttpRequestClient.INPUT_PASSWORD, password);
		editor.commit();
		
		System.out.println(prefs.getString("username", null)+"  "+prefs.getString("pass", null));
	}

}