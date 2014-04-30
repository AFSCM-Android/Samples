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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.nfc.cardemulation.OffHostApduService;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

@TargetApi(19)
public class OrangeOffHostApduService extends OffHostApduService {
	static boolean bootcomplete = false;
	static String mServiceName;
	static Context mContext;
	private UICC mUICC;

	@TargetApi(19)
	@Override
	public IBinder onBind(Intent arg0) {
		Util.myLog("Bound");

		return null;
	}

	public void onCreate() {
		Uri paymentSettingUri = null;
		Util.myLog("Created");
		mContext = this;
		mServiceName = this.getClass().getCanonicalName();

		SettingsObserver mActObs = new SettingsObserver(mHandler);
		try {
			// Settings.Secure.NFC_PAYMENT_DEFAULT_COMPONENT not currently in
			// SDK
			paymentSettingUri = Settings.Secure
					.getUriFor((String) (Settings.Secure.class
							.getField("NFC_PAYMENT_DEFAULT_COMPONENT")
							.get(new Settings.Secure())));
			getContentResolver().registerContentObserver(paymentSettingUri,
					true, mActObs);
		} catch (Exception e) {
			Util.myLog("Could not retrieve secure setting to listen to : " + e);
		}

		if (paymentSettingUri != null) {
			mActObs.onChange(true, paymentSettingUri);
		}
	}

	final Handler mHandler = new Handler(Looper.getMainLooper());

	private final class SettingsObserver extends ContentObserver {
		public SettingsObserver(Handler handler) {
			super(handler);
		}

		@TargetApi(19)
		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);

			Util.myLog("onChange");

			if (OrangeOffHostApduService.bootcomplete) {

				NfcAdapter adapter = NfcAdapter.getDefaultAdapter(mContext);
				CardEmulation ce = CardEmulation.getInstance(adapter);

				Util.myLog("Setting change");

				/*
				 * String componentString = Settings.Secure.getString(
				 * mContext.getContentResolver(), //
				 * Settings.Secure.NFC_PAYMENT_DEFAULT_COMPONENT
				 * "nfc_payment_default_component" );
				 * Util.myLog("Active component is " + componentString);
				 */

				if (ce.isDefaultServiceForCategory(new ComponentName(mContext,
						mServiceName), "payment")) {
					Util.myLog("Activated");

					// TODO : read service status in UICC
					// If service is not enabled in the UICC, and if automatic
					// mode
					// then start PIN entry UI.
					SharedPreferences myPrefs = getSharedPreferences(
							MainScreenActivity.PREFS_NAME, 0);
					Boolean isAuto = myPrefs.getBoolean("automatic", false);

					if (isAuto) {
						try {
							Class classToInvestigate = Class
									.forName("org.simalliance.openmobileapi.Reader");

							mUICC = new UICC(mContext, getResources()
									.getString(R.string.AID));
							(new CheckActivationTask()).start();
						} catch (ClassNotFoundException e) {
							// Class not found!
							Util.myLog("UICC access not supported on this device"
									+ e);
						}
					}
				}
			} else {
				bootcomplete = true; // No longer discard further notifications
				Util.myLog("Ignoring startup setting change");
			}
		}
	}

	private class CheckActivationTask extends Thread {
		@Override
		public void run() {
			if (!mUICC.isActive()) {
				ActivationActivity.kick(mContext);
			}
		}
	}
}
