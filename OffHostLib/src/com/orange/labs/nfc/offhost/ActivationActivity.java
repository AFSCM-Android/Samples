/*-------------------------------------------------------- 
 * Module Name : OffHostLib
 * Version : 
 * 
 * Software Name : Android Sample NFC applications
 * Version : VersionNumber
 *
 * Copyright (c) 2014 Orange
 * This software is distributed under the Apache v2 license,
 * the text of which is available at http://www.apache.org/licenses/LICENSE-2.0
 * or see the "LICENSE" file for more details.
 *
 *--------------------------------------------------------
 * File Name   : ${file_name}
 *
 * Created     : Jan 10th, 2014
 * Author(s)   : Erwan Louët
 *
 * Description :
 * Sample payment application
 *
 *--------------------------------------------------------
 * ${Log}
 *
 */
package com.orange.labs.nfc.offhost;

import java.util.NoSuchElementException;

import com.orange.labs.nfc.offhost.exceptions.accessDeniedException;
import com.orange.labs.nfc.offhost.exceptions.cardNotPresentException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/*
 * This activity is created when the user needs to enter their PIN
 * code for cardlet activation.
 * 
 * This occurs in 3 situations mostly : 
 * - manual mode, user pushes the "pay now" button
 * - enable automatic mode
 * - service is selected in tap&pay menu (only if automatic mode is enabled) 
 */

public class ActivationActivity extends Activity {
	public UICC mUICC;
	public Context mContext;
	
	 static byte[] GET_STATUS_APDU = {(byte)0x80, (byte)0xF2, (byte)0x00, (byte)0x00, (byte)0x00 };
	 
	 @Override
     protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_activation);
		
		TextView label = (TextView)findViewById(R.id.enter_pin_label);
		label.setTextColor( MainScreenActivity.color);
		
		EditText pinEntry = (EditText)findViewById(R.id.PIN_entry);
		//pinEntry.requestFocus();
		pinEntry.setOnEditorActionListener( // setOnEditorActionListener

		new OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        boolean handled = false;
		        if (actionId == EditorInfo.IME_ACTION_DONE) {
		        	// TODO : do not leave this in production code !
		        	Util.myLog("PIN : " + v.getText());
		        	
		        	if (mUICC != null){
		        		try{
		        			byte[] response = mUICC.sendAPDU( MainScreenActivity.AID, GET_STATUS_APDU, "get status" );
		        		}catch (accessDeniedException e){
		        			makeToast("Access denied !");
		        		}catch (cardNotPresentException e){
		        			makeToast("No SIM inserted !");	
		        		}catch (NoSuchElementException e){
		        			makeToast("Cardlet not found !");
		        		}
		        	}
		        	handled = true;
		        	finish();
		        }
		        return handled;
		    }
		});


		mUICC = new UICC(this);
	}
	
	void makeToast(String message){
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast,
		                               (ViewGroup) findViewById(R.id.toast_layout_root));

		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(message);

		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	 }
	 
	 
	/* 
	 * Static method to kickstart the activity
	 */
	static void kick( Context ctx ){
		Intent intnt = new Intent();
		intnt.setClass(ctx, ActivationActivity.class);
		intnt.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intnt);

	}
}
