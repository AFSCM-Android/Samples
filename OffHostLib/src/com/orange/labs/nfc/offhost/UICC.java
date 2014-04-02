package com.orange.labs.nfc.offhost;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.SEService.CallBack;
import org.simalliance.openmobileapi.Session;

import com.orange.labs.nfc.offhost.exceptions.WrongPinException;
import com.orange.labs.nfc.offhost.exceptions.accessDeniedException;
import com.orange.labs.nfc.offhost.exceptions.cardNotPresentException;

import android.content.Context;

public class UICC implements CallBack {

	byte[] verifyPIN_APDU = new byte[] { (byte) 0x00, (byte) 0x20, (byte) 0x00,
			(byte) 0x80, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
	byte[] activate_APDU = new byte[] { (byte) 0x80, (byte) 0xf0, (byte) 0x01,
			(byte) 0x00 };
	byte[] deactivate_APDU = new byte[] { (byte) 0x80, (byte) 0xf0,
			(byte) 0x00, (byte) 0x00 };
	byte[] get_status_APDU = new byte[] { (byte) 0x80, (byte) 0xf2,
			(byte) 0x00, (byte) 0x00, (byte) 0x00 };

	public SEService scardManager;
	private Lock mLock = new ReentrantLock();
	public volatile boolean isConnected = false;

	public UICC(Context ctx) {
		try {
			synchronized (mLock) {
				mLock.lock();
				Util.myLog("UICC after lock");
				scardManager = new SEService(ctx, this);
			}
		} catch (SecurityException e) {
			Util.myLog("Smartcard binding not allowed");
		} catch (Exception e) {
			Util.myLog("Exception : " + e);
		}
	}

	public byte[] sendAPDU(byte aid[], byte[] capdu, String label)
			throws cardNotPresentException, accessDeniedException,
			NoSuchElementException {
		Reader uicc = null;
		Session s = null;
		Channel c = null;
		byte[] ret = null;

		synchronized (mLock) {
			try {
				Util.myLog("before tryLock");
				if (mLock.tryLock(1000, TimeUnit.SECONDS)) {
					Util.myLog("after tryLock");

					if (isConnected) {

						mLock.unlock();
						Util.myLog("sendAPDU after unlock");

						
						// TODO : look for UICC in the reader list
						uicc = (scardManager.getReaders())[0];
						Util.myLog("Connected to " + uicc.getName());

						if (uicc.isSecureElementPresent() == false) {
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
								Util.myLog("[" + e1.getClass().getName()
										+ "]\n\t" + e1.getMessage());
							} finally {
								if (c != null) {
									Util.myLog("Closing channel");
									c.close();
								}
							}
						} catch (IOException e2) {
							Util.myLog("[SESSION] KO \n"
									+ e2.getClass().getName() + "]\n\t"
									+ e2.getMessage());
						} finally {
							if (s != null) {
								Util.myLog("Closing session");
								s.close();
							}
						}
					} else {
						Util.myLog("Service not connected");
					}
				}
			} catch (InterruptedException e) {
				Util.myLog("Interrupted !");
			}
		}

		return ret;
	}

	/* Function callback to handle SE Service connection */
	@Override
	public void serviceConnected(SEService service) {
		Util.myLog("serviceConnected");

		scardManager = service;
		try {
			if (service.isConnected()) {
				isConnected = true;
				mLock.unlock();
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

	public byte[] verifyPIN(byte[] aid, String pinValue)
			throws cardNotPresentException, accessDeniedException,
			NoSuchElementException, WrongPinException {
		byte[] tmpPIN = verifyPIN_APDU;
		System.arraycopy(pinValue.getBytes(), 0, tmpPIN, 5,
				pinValue.getBytes().length);

		byte[] rapdu = sendAPDU(aid, tmpPIN, "Verify PIN");
		if (rapdu[0] != (byte) 0x90 || rapdu[1] != (byte) 0) {
			throw new WrongPinException();
		}
		return rapdu;
	}

	public byte[] activate(byte[] aid, String pinValue)
			throws cardNotPresentException, accessDeniedException,
			NoSuchElementException, WrongPinException {
		verifyPIN(aid, pinValue);
		return sendAPDU(aid, activate_APDU, "Activate");
	}

	public byte[] deActivate(byte[] aid) throws cardNotPresentException,
			accessDeniedException, NoSuchElementException {
		return sendAPDU(aid, deactivate_APDU, "Deactivate");
	}

	public boolean isActive(byte[] aid) {
		byte[] response;

		try {
			response = sendAPDU(aid, get_status_APDU, "Get Status");
			if (response[0] == (byte) 0x01 && response[1] == (byte) 0x90
					&& response[2] == (byte) 0x00) {
				Util.myLog("cardlet is active");
				return true;
			}
		} catch (Exception e) {
			Util.myLog("Exception ! " + e);
		}
		Util.myLog("cardlet is not active");
		return false;
	}
}
