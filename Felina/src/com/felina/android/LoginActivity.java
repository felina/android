package com.felina.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.felina.android.api.FelinaClient;
import com.loopj.android.http.JsonHttpResponseHandler;

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
 	private static FelinaClient fClient;

	private OnClickListener mListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			String text;
			
			switch(v.getId()) {
			
			case R.id.loginBtn:

				String email = userBox.getText().toString();
				String pass = passBox.getText().toString();
				String name = nameBox.getText().toString();
				text = loginBtn.getText().toString();

				if(text.equals(login)) {
					dialog.show();
				    login(getApplicationContext(), email, pass, Constants.RETRY_LIMIT);
				}
				
				else if (text.equals(register)) {
					dialog.show();
				    register(getApplicationContext(), email, pass, name, Constants.RETRY_LIMIT);
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
		
		fClient = new FelinaClient(this);
		
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
	
    private void login(final Context context, final String email, final String pass, final int retry) {
    	
    	if(retry == 0) {
    		return;
    	}
    	
    	fClient.login(email, pass, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(JSONObject response) {
				dialog.dismiss();
				try {
					if (response.getBoolean("res")) {
				    	CredentialUtils.writeEmail(context, email);
				    	CredentialUtils.writePassword(context, pass);
				    	CredentialUtils.writeName(context, response.getJSONObject("user").getString("name"));
						setResult(RESULT_OK);
						finish();
					} else {
						errTxt.setVisibility(View.VISIBLE);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					login(context, email, pass, retry-1);
				}
			}
			
			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				login(context, email, pass, retry-1);
			}
		});
    }
    
    private void register(final Context context, final String email, final String pass, final String name, final int retry) {
    	
    	if(retry == 0) {
    		return;
    	}
    	
    	fClient.register(email, pass, name, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(JSONObject response) {
				dialog.dismiss();
				try {
					if (response.getBoolean("res")) {
				    	CredentialUtils.writeEmail(context, email);
				    	CredentialUtils.writePassword(context, pass);
				    	CredentialUtils.writeName(context, name);
						setResult(RESULT_OK);
						finish();
					} else {
						errTxt.setVisibility(View.VISIBLE);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					login(context, email, pass, retry-1);
				}
			}
			
			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				login(context, email, pass, retry-1);
			}
		});
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

}