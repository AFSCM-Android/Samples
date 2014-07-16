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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public abstract class MainScreenActivity extends Activity {
	private static MainScreenActivity mContext;
	public static volatile boolean automatic = false;
	public static View payNowButton = null;
	public static String serviceName = "";
	public static int color = 0;
	protected static byte[] AID = null;
	public UICC mUICC;
	public static final String PREFS_NAME = "OrangeNfcConfig";

	private Handler uiHandler = new UIHandler();

	class UIHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			refreshUI();
		}
	}

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
	@TargetApi(Build.VERSION_CODES.KITKAT)
	boolean isRouted() {
		/*
		 * Query NFC adapter to see if current application is the default
		 * payment application in the tap & pay menu.
		 */
		NfcAdapter adapter = NfcAdapter.getDefaultAdapter(mContext);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			try {
				CardEmulation ce = CardEmulation.getInstance(adapter);

				return (ce.isDefaultServiceForCategory(new ComponentName(
						mContext, getAssociatedService()), "payment"));
			} catch (UnsupportedOperationException uoe) {
				// Device is Android 4.4 or greater but does not support HCE
				return (true);
			}
		}

		return true; // Assume we are routed on pre-kitkat releases.
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
				ActivationActivity.kick(mContext,
						ActivationActivity.TODO_PAY_NOW);
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
				Util.myLog("Automatic "
						+ (cb.isChecked() ? "checked" : "unchecked"));

				/*
				 * Save user preference into setting
				 */
				SharedPreferences settings = getSharedPreferences(
						MainScreenActivity.PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean("automatic", cb.isChecked());
				editor.commit();

				if (cb.isChecked()) {
					/*
					 * start PIN entry screen. If PIN matches then activate UICC
					 * payment instance.
					 */
					ActivationActivity.kick(mContext,
							ActivationActivity.TODO_ACTIVATE);
				} else {
					(new deactivateTask()).start();
				}
				if (payNowButton != null) {
					payNowButton.setVisibility((cb.isChecked() ? View.INVISIBLE
							: View.VISIBLE));
				}
			}
		});

		serviceName = getResources().getString(R.string.service_name);
		color = getResources().getColor(R.color.service_color);
		TextView title = (TextView) findViewById(R.id.servicename);
		title.setTextColor(color);
		title.setText(serviceName);

		/*
		 * Some code here is written in order to ignore notification of tap&pay
		 * change received at every boot by the currently selected application.
		 */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			OrangeOffHostApduService.bootcomplete = true;
		}

		/*
		 * UICC management : create a UICC object, and lauch a thread to read
		 * current cardlet activation status.
		 */

		try {
			Class classToInvestigate = Class
					.forName("org.simalliance.openmobileapi.Reader");

			mUICC = new UICC(this, getResources().getString(R.string.AID));
			(new GetStatusTask()).start();

		} catch (ClassNotFoundException e) {
			// Class not found!
			Util.myLog("UICC access not supported on this device" + e);
		} catch (Exception e) {
			// Unknown exception
			Util.myLog("Unknown error " + e);
		}
	}

	private class GetStatusTask extends Thread {
		@Override
		public void run() {
			try {
				Class classToInvestigate = Class
						.forName("org.simalliance.openmobileapi.Reader");

				automatic = mUICC.isActive();

				// Notify UI thread for update
				Message msg = Message.obtain(uiHandler);
				msg.obj = "update";
				uiHandler.sendMessage(msg);
			} catch (ClassNotFoundException e) {
				// Class not found!
				Util.myLog("UICC access not supported on this device" + e);
			}
		}
	}

	private class deactivateTask extends Thread {
		@Override
		public void run() {

			try {
				mUICC.deActivate();
				automatic = false;
			} catch (Exception e) {
				Util.myLog("Error trying to deactivate : " + e);
			}

			// Notify UI thread for update
			Message msg = Message.obtain(uiHandler);
			msg.obj = "update";
			uiHandler.sendMessage(msg);
		}
	}

	/*
	 * Performs activation check to display the right UI : if application not
	 * routed (according to tap&pay menu), then do not display configuration
	 * options and pay now button.
	 */
	protected void onResume() {
		super.onResume();
		(new GetStatusTask()).start();

		refreshUI();
	}

	void refreshUI() {

		View mLL = findViewById(R.id.warning_box);
		View pC = findViewById(R.id.payment_control);

		if (!isRouted()) {
			mLL.setVisibility(View.VISIBLE);
			pC.setVisibility(View.INVISIBLE);
		} else {
			mLL.setVisibility(View.INVISIBLE);
			pC.setVisibility(View.VISIBLE);
		}

		CheckBox cb = (CheckBox) findViewById(R.id.automatic);
		cb.setChecked(automatic);

		if (payNowButton != null) {
			payNowButton.setVisibility((cb.isChecked() ? View.INVISIBLE
					: View.VISIBLE));
		}

		TextView wl = (TextView) findViewById(R.id.warning_label);
		// TODO : use resource text string instead
		wl.setText(serviceName
				.concat(" is not currently set as your default payment application"));
	}

	@Override
	protected void onDestroy() {
		if (mUICC != null) {
			mUICC.dispose();
		}
		super.onDestroy();
	}
}
