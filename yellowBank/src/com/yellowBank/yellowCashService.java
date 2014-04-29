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

import com.orange.labs.nfc.offhost.OrangeOffHostApduService;
import com.orange.labs.nfc.offhost.Util;

public class yellowCashService extends OrangeOffHostApduService {
	@Override
	public void onCreate() {
		Util.setTag("YellowCash");
		super.onCreate();
	}
}
