/*-------------------------------------------------------- 
 * Module Name : yellowBank
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
package com.yellowBank;

import android.os.Bundle;
import com.orange.labs.nfc.offhost.MainScreenActivity;
import com.orange.labs.nfc.offhost.Util;

public class yellowCashUI extends MainScreenActivity {

	static final byte[] mAID = {  (byte)0xA0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x04, (byte)0x10, (byte)0x10, (byte)0x11};

	public String getAssociatedService() {
		return yellowCashService.class.getCanonicalName();
	}

	@Override
	public void onCreate(Bundle b) {
		Util.setTag("YellowCash");
		setAID(mAID);
		super.onCreate(b);
		setServiceName("Yellow Cash", 0xEFEEDD22);
	}
}
