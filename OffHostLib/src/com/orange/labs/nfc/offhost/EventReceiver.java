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

public class EventReceiver extends BroadcastReceiver {
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
		} 
	}
}
