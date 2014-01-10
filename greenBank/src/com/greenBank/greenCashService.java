package com.greenBank;

import com.orange.labs.nfc.offhost.OrangeOffHostApduService;
import com.orange.labs.nfc.offhost.Util;

public class greenCashService extends OrangeOffHostApduService {
	@Override
	public void onCreate() {
		Util.setTag("GreenCash");
		super.onCreate();
	}
}
