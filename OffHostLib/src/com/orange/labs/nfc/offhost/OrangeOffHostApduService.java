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
import android.database.ContentObserver;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.nfc.cardemulation.OffHostApduService;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

@TargetApi(19)
public class OrangeOffHostApduService extends OffHostApduService {
	static boolean bootcomplete = false;
	static String mServiceName;
	static Context mContext;

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

				if (ce.isDefaultServiceForCategory(new ComponentName(mContext,
						mServiceName
						), "payment")) {
					Util.myLog("Activated");

					// TODO : read service status in UICC
					// If service is not enabled in the UICC, and if automatic mode
					// then start PIN entry UI.
					if (MainScreenActivity.automatic){
						// if ( UICC_status_inactive )
						ActivationActivity.kick(mContext);
					    // }
					}
				}
			} else {
				bootcomplete = true; // No longer discard further notifications
				Util.myLog("Ignoring startup setting change");
			}
		}
	}

}
