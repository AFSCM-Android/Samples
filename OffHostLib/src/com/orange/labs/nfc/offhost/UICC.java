package com.orange.labs.nfc.offhost;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.SEService.CallBack;
import org.simalliance.openmobileapi.Session;

import com.orange.labs.nfc.offhost.exceptions.accessDeniedException;
import com.orange.labs.nfc.offhost.exceptions.cardNotPresentException;

import android.content.Context;

public class UICC implements CallBack{

	public SEService scardManager;
	private boolean isConnected = false;
	
	public UICC(Context ctx) {
		try {
			scardManager = new SEService(ctx,this);
		} catch (SecurityException e) {
			Util.myLog("Smartcard binding not allowed");
		} catch (Exception e) {
			Util.myLog("Exception : "+ e);
		}
	}
	
	public byte[] sendAPDU(byte aid[], byte[] capdu, String label) throws cardNotPresentException, accessDeniedException, NoSuchElementException {
		Reader uicc = null;
		Session s = null;
		Channel c = null;
		byte[] ret = null;

		if (isConnected) {
			
			// TODO : look for UICC in the reader list
			uicc = (scardManager.getReaders())[0];
			Util.myLog("Connected to "+ uicc.getName() );
			
			if (uicc.isSecureElementPresent() == false){
				throw new cardNotPresentException();
			}
			
			try {
				s = uicc.openSession();
				Util.myLog("[AID] " + Util.bytesToHex(aid));

				try {
					c = s.openLogicalChannel(aid);
					Util.myLog("[SELECT] OK");
					Util.myLog("APDU : " + Util.bytesToHex(capdu));

					ret = c.transmit(capdu);
					Util.myLog("<= " + Util.bytesToHex(ret));

				} catch (SecurityException e1) {
					throw new accessDeniedException();
				} catch (NoSuchElementException e) {
					throw e;
				} catch (Exception e1) {
								Util.myLog("[" + e1.getClass().getName() + "]\n\t" + e1.getMessage());
				} finally {
					if (c != null) {
						Util.myLog("Closing channel");
						c.close();
					}
				}
			} catch (IOException e2) {
				Util.myLog("[SESSION] KO \n"+ e2.getClass().getName() + "]\n\t"
						+ e2.getMessage());
			} finally {
				if (s != null) {
					Util.myLog("Closing session");					
					s.close();
				}
			}
		} else{
			Util.myLog("Service not connected");
		}
		
		return ret;
	}
	
	/* Function callback to handle SE Service connection */
	@Override
	public void serviceConnected(SEService service) {
		scardManager = service;
		try {
			if (service.isConnected()) {
				isConnected = true;
				Reader[] scardReaders = scardManager.getReaders();
				if (scardReaders != null) {
					Util.myLog("Identified readers:");
					for (Reader reader : scardReaders) {
						Util.myLog(reader.getName()
								+ " "
								+ (reader.isSecureElementPresent() ? "present"
										: "absent"));
					}
				} else
					Util.myLog("No reader detected");
			}
		} catch (Exception e) {
			Util.myLog("Exception: " + e.getClass().getName() + " "
					+ e.getMessage());
		}
	}

	public void dispose() {
		scardManager = null;
		isConnected = false;
	}
}
