package com.orange.labs.nfc.offhost;

public interface PaymentConstants {
	static final String TRANSACTION_EVENT_LEGACY 							= "android.nfc.action.TRANSACTION_DETECTED";
    static final String TRANSACTION_EVENT_EXTRA_DATA_LEGACY 				= "android.nfc.extra.DATA";
	static final String TRANSACTION_EVENT_GSMA						= "com.gsma.services.nfc.action.TRANSACTION_EVENT";
    static final String TRANSACTION_EVENT_EXTRA_DATA_GSMA 			= "com.gsma.services.nfc.extra.DATA";
}
