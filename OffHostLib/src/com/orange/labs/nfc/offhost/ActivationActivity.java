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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activation);
		
		TextView label = (TextView)findViewById(R.id.enter_pin_label);
		label.setTextColor( MainScreenActivity.color);
		
		/* 
		 * TODO : add buttons and manage display, etc...
		 */

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
