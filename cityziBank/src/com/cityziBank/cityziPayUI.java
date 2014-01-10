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
 * Author(s)   : Erwan Louët
 *
 * Description :
 * Sample payment application
 *
 *--------------------------------------------------------
 * ${Log}
 *
 */
package com.cityziBank;

import android.os.Bundle;

import com.orange.labs.nfc.offhost.MainScreenActivity;
import com.orange.labs.nfc.offhost.Util;

public class cityziPayUI extends MainScreenActivity {
	public String getAssociatedService(){
		return cityziPayService.class.getCanonicalName();	
	}
	
	@Override
	public void onCreate(Bundle b) {
		Util.setTag("CityziPay");
		// TODO Auto-generated method stub
		super.onCreate(b);
		setServiceName("Cityzi Pay", 0xEF999999);
	}
}
