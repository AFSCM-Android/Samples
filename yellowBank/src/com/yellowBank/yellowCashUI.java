package com.yellowBank;

import android.os.Bundle;

import com.orange.labs.nfc.offhost.MainScreenActivity;
import com.orange.labs.nfc.offhost.Util;

public class yellowCashUI extends MainScreenActivity {
	public String getAssociatedService(){
		return yellowCashService.class.getCanonicalName();	
	}
	
	@Override
	public void onCreate(Bundle b) {
		Util.setTag("YellowCash");
		super.onCreate(b);
		setServiceName("Yellow Cash", 0xEFEEDD22);
	}
}
