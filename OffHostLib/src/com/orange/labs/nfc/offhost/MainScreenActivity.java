/*-------------------------------------------------------- 
 * Module Name : 
 * Version : 
 * 
 * Software Name : Android NFC Handset Test Suite
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
 * Author(s)   : Erwan Lou�t
 *
 * Description :
 * Sample payment application
 *
 *--------------------------------------------------------
 * ${Log}
 *
 */
package com.orange.labs.nfc.offhost;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public abstract class MainScreenActivity extends Activity {
	private static MainScreenActivity mContext;
	public static boolean automatic = false;
	public static View payNowButton = null;
	public static String serviceName = "";
	public static int color = 0;

	/*
	 * Hook to link to associated service. This allows to perfome default
	 * payment query and provide UI accordingly. To be implemented by
	 * application using the lib.
	 */
	public abstract String getAssociatedService();

	/*
	 * Find out if current application is selected as default payment in the
	 * tap&pay menu
	 */
	boolean isRouted() {
		/*
		 * Query NFC adapter to see if current application is the default
		 * payment application in the tap & pay menu.
		 */
		NfcAdapter adapter = NfcAdapter.getDefaultAdapter(mContext);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			CardEmulation ce = CardEmulation.getInstance(adapter);

			return (ce.isDefaultServiceForCategory(new ComponentName(mContext,
					getAssociatedService()), "payment"));
		}
		
		return true;   // Assume we are routed on pre-kitkat releases.
	}

	/*
	 * Ask user to set current application as default tap&pay setting This does
	 * not involve visiting the settings menu, but brings up a dialog.
	 */
	@TargetApi(19)
	void setDefaultPaymentSetting() {
		if (!mContext.isRouted()) {
			Intent activate = new Intent();
			activate.setAction(android.nfc.cardemulation.CardEmulation.ACTION_CHANGE_DEFAULT);
			activate.putExtra(
					android.nfc.cardemulation.CardEmulation.EXTRA_SERVICE_COMPONENT,
					new ComponentName(mContext, getAssociatedService()));
			activate.putExtra(
					android.nfc.cardemulation.CardEmulation.EXTRA_CATEGORY,
					"payment");
			mContext.startActivity(activate);
		}
	}

	/*
	 * Activity creation. Standard processing.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		View setDefaultButton;
		View automaticButton;
		mContext = this;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);

		/*
		 * This button appears when current service is not selected in tap&pay
		 * menu, in order to invite user to change setting.
		 */
		setDefaultButton = findViewById(R.id.button1);
		setDefaultButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mContext.setDefaultPaymentSetting();
			}
		});

		/*
		 * Handle manual mode with this button
		 */
		payNowButton = findViewById(R.id.pay_now);
		payNowButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				setDefaultPaymentSetting();
				ActivationActivity.kick(mContext);
			}
		});

		/*
		 * This check box is used for automatic mode
		 */
		automaticButton = findViewById(R.id.automatic);
		automaticButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				CheckBox cb = (CheckBox) view;
				Util.myLog("Check " + cb.isChecked());

				automatic = cb.isChecked();
				// TODO : save this to file system

				if (automatic) {
					/*
					 * start PIN entry screen. If PIN matches then activate UICC
					 * payment instance.
					 */
					ActivationActivity.kick(mContext);
				}
				if (payNowButton != null) {
					payNowButton.setVisibility((cb.isChecked() ? View.INVISIBLE
							: View.VISIBLE));
				}
			}
		});

		/* 
		 * Some code here is written in order to ignore notification 
		 * of tap&pay change received at every boot by the currently 
		 * selected application.
		 */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			OrangeOffHostApduService.bootcomplete = true;
		}
	}

	/*
	 * Performs activation check to display the right UI : if application not
	 * routed (according to tap&pay menu), then do not display configuration
	 * options and pay now button.
	 */
	protected void onResume() {
		super.onResume();

		View mLL = findViewById(R.id.warning_box);
		View pC = findViewById(R.id.payment_control);

		if (!isRouted()) {
			mLL.setVisibility(View.VISIBLE);
			pC.setVisibility(View.INVISIBLE);
		} else {
			mLL.setVisibility(View.INVISIBLE);
			pC.setVisibility(View.VISIBLE);
		}

		TextView wl = (TextView) findViewById(R.id.warning_label);
		// TODO : use resource text string instead
		wl.setText(serviceName
				.concat(" is not currently set as your default payment application"));
	}

	/* Simple function to customise UI */
	public void setServiceName(String sn, int cv) {
		serviceName = sn;
		color = cv;
		TextView title = (TextView) findViewById(R.id.servicename);
		title.setTextColor(cv);
		title.setText(sn);
	}

}