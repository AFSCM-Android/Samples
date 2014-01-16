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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class AidReceiver extends BroadcastReceiver {
	static int pushCount = 0;

	/*
	 * Broadcast receiver
	 */
	@Override
	public void onReceive(Context ctxt, Intent bcast) {
		byte[] aid;
		String textAid;

		Util.myLog("Receiver");

		/*
		 * This is used to discard initial notification of change received at
		 * boot by the current service selected on the tap&pay menu.
		 */
		if (bcast.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			Util.myLog("Boot complete");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				OrangeOffHostApduService.bootcomplete = true;
			}
		} else {
			/*
			 * TODO : this section of code does not handle GSMA-style
			 * transaction intents. Please do not consider as reference.
			 * 
			 * Transaction events intents are normally handled in an Activity
			 * registered for them in the manifest.
			 */
			aid = bcast.getByteArrayExtra("com.android.nfc_extras.extra.AID");
			textAid = bytesToHex(aid);
			Util.myLog("Receiver AID " + textAid);
			Intent intnt = new Intent();
			intnt.setClass(ctxt, ActivationActivity.class);
			intnt.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
			ctxt.startActivity(intnt);
		}
	}

	/*
	 * Simple code to turn AIDs byte arrays into strings, for the sake of
	 * logging.
	 */
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

}
