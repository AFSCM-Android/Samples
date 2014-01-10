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
