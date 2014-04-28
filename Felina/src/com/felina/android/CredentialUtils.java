package com.felina.android;

import com.felina.android.Constants.Extra;

import android.content.Context;
import android.content.SharedPreferences;

public class CredentialUtils {
	/**
	 * reads the previously saved email from the preference file
	 * @param context 
	 * @return the email read
	 */
	public static synchronized String readEmail(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Extra.CREDENTIAL_PREFS, Context.MODE_PRIVATE);
		return prefs.getString(Extra.CREDENTIAL_EMAIL, null);
	}
	
	/**
	 * writes the email to the preference file
	 * @param context
	 * @param email
	 */
	public static synchronized void writeEmail(Context context, String email) {
		SharedPreferences prefs = context.getSharedPreferences(Extra.CREDENTIAL_PREFS, Context.MODE_PRIVATE);
		prefs.edit().putString(Extra.CREDENTIAL_EMAIL, email).commit();
	}
	
	/**
	 * reads the previously saved password from the preference file
	 * @param context 
	 * @return the password read
	 */
	public static synchronized String readPassword(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Extra.CREDENTIAL_PREFS, Context.MODE_PRIVATE);
		return prefs.getString(Extra.CREDENTIAL_PASSWORD, null);
	}
	
	/**
	 * writes the password to the preference file
	 * @param context
	 * @param password
	 */
	public static synchronized void writePassword(Context context, String pass) {
		SharedPreferences prefs = context.getSharedPreferences(Extra.CREDENTIAL_PREFS, Context.MODE_PRIVATE);
		prefs.edit().putString(Extra.CREDENTIAL_PASSWORD, pass).commit();
	}
	
	/**
	 * reads the previously saved password from the preference file
	 * @param context 
	 * @return the password read
	 */
	public static synchronized String readName(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Extra.CREDENTIAL_PREFS, Context.MODE_PRIVATE);
		return prefs.getString(Extra.CREDENTIAL_NAME, null);
	}
	
	/**
	 * writes the password to the preference file
	 * @param context
	 * @param password
	 */
	public static synchronized void writeName(Context context, String name) {
		SharedPreferences prefs = context.getSharedPreferences(Extra.CREDENTIAL_PREFS, Context.MODE_PRIVATE);
		prefs.edit().putString(Extra.CREDENTIAL_NAME, name).commit();
	}
}