package com.greenBank;

import android.os.Bundle;

import com.orange.labs.nfc.offhost.MainScreenActivity;
import com.orange.labs.nfc.offhost.Util;

public class greenCashUI extends MainScreenActivity {
	public String getAssociatedService(){
		return greenCashService.class.getCanonicalName();	
	}
	
	@Override
	public void onCreate(Bundle b) {
		Util.setTag("GreenCash");
		// TODO Auto-generated method stub
		super.onCreate(b);
		setServiceName("Green Cash", 0xEF44BB22);
	}
}