/*-------------------------------------------------------- 
 * Module Name : OffHostLib
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
package com.orange.labs.nfc.offhost;

import java.util.NoSuchElementException;

import com.orange.labs.nfc.offhost.exceptions.WrongPinException;
import com.orange.labs.nfc.offhost.exceptions.accessDeniedException;
import com.orange.labs.nfc.offhost.exceptions.cardNotPresentException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/*
 * This activity is created when the user needs to enter their PIN
 * code for cardlet activation.
 * 
 * This occurs in 3 situations mostly : 
 * - manual mode, user pushes the "pay now" button
 * - enable automatic mode
 * - service is selected in tap&pay menu (only if automatic mode is enabled) 
 */

public class ActivationActivity extends Activity {
	public UICC mUICC;
	public Context mContext;

	static final int TODO_ACTIVATE = 0;
	static final int TODO_DEACTIVATE = 1;
	static final int TODO_VERIFY = 2;
	static final int TODO_CONFIRM = 3;
	static final int TODO_PAY_NOW = 4;

	static final String COUNTDOWN = "countdown";

	static final String PURPOSE_EXTRA = "whatfor";

	UIHandler uiHandler = new UIHandler();

	/*
	 * Assuming activation request by default. This happens to match the default
	 * layout values
	 */
	private int todo = TODO_ACTIVATE;
	static private CountDownThread cdt = null;

	class UIHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.getData().getInt(COUNTDOWN) > 0) {
				TextView tv = (TextView) findViewById(R.id.seconds);
				tv.setText(msg.getData().getInt(COUNTDOWN)
						+ " seconds remaining");
			} else {
				if (todo == TODO_PAY_NOW) {
					try {
						mUICC.deActivate();
						finish();
					} catch (Exception e) {
						Util.myLog("Deactivation failed for some reason");
					}
				}
			}
		}
	}

	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);

		checkNfcPush(intent);

		if (todo == TODO_PAY_NOW) {
			findViewById(R.id.countdown).setVisibility(View.INVISIBLE);

			if (cdt != null) {
				cdt.interrupt();
				cdt = null;
			}
			
			try {
				mUICC.deActivate();
			} catch (Exception e){
				Util.myLog("Deactivation failed for some reason.");
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_activation);

		todo = getIntent().getIntExtra(PURPOSE_EXTRA, TODO_ACTIVATE);

		TextView label = (TextView) findViewById(R.id.enter_pin_label);
		label.setTextColor(MainScreenActivity.color);

		label = (TextView) findViewById(R.id.please_tap_label);
		label.setTextColor(MainScreenActivity.color);

		
		checkNfcPush(getIntent());

		EditText pinEntry = (EditText) findViewById(R.id.PIN_entry);
		pinEntry.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					// remove keyboard
					InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

					// TODO : do not leave this in production code !
					Util.myLog("PIN : " + v.getText());

					if (mUICC != null) {
						try {
							byte[] response;

							switch (todo) {
							case TODO_VERIFY:
								response = mUICC.verifyPIN( v.getText()
												.toString());
								// Assuming PIN is correct from here (otherwise
								// exception)
								startCountDown();
								break;
							case TODO_ACTIVATE:
								MainScreenActivity.automatic = false;
								response = mUICC.activate( v.getText()
												.toString());

								if (response[0] == (byte) 0x90
										&& response[1] == (byte) 0) {
									makeToast("Activated");
									MainScreenActivity.automatic = true;
								} else {
									makeToast("Activation failed");
								}
								break;
							case TODO_PAY_NOW:
								response = mUICC.activate( v.getText()
												.toString());

								if ( ( response[0] == (byte) 0x90
										&& response[1] == (byte) 0) 
										||  ( response[0] == (byte) 0x69
												&& response[1] == (byte)0x85) ) {
									startCountDown();
								} else {
									makeToast("Activation failed");
								}
								startCountDown();
								break;
							}

						} catch (WrongPinException e) {
							makeToast("Wrong PIN");
						} catch (accessDeniedException e) {
							makeToast("Access denied !");
						} catch (cardNotPresentException e) {
							makeToast("No SIM inserted !");
						} catch (NoSuchElementException e) {
							makeToast("Cardlet not found !");
						}
					}
					handled = true;
					if (todo != TODO_PAY_NOW  && todo != TODO_VERIFY)
						finish();
				}
				return handled;
			}
		});

		try {
			// Detect devices not supporting the API with this call...
			Class classToInvestigate = Class
					.forName("org.simalliance.openmobileapi.Reader");
			mUICC = new UICC(this, getResources().getString(R.string.AID));
			// TODO : define AID
		} catch (ClassNotFoundException e) {
			// ... and treat appropriately 
			Util.myLog("UICC access not supported on this device" + e);
		} catch (Exception e) {
			// Unknown exception
			Util.myLog("Unknown error " + e);
		}
	}

	protected void startCountDown() {
		findViewById(R.id.enter_pin_label).setVisibility(View.INVISIBLE);
		findViewById(R.id.PIN_entry).setVisibility(View.INVISIBLE);
		findViewById(R.id.countdown).setVisibility(View.VISIBLE);

		cdt = new CountDownThread();
		cdt.start();
	}

	class CountDownThread extends Thread {
		public void run() {
			int i = 30;
			while (i >= 0) {
				try {
					sleep(1000);
					Message msg = Message.obtain(uiHandler);
					Bundle b = new Bundle();
					b.putInt(COUNTDOWN, i);
					msg.setData(b);
					uiHandler.sendMessage(msg);
				} catch (InterruptedException e) {
					return;
				}
				i--;
			}
		}
	}

	void checkNfcPush(Intent i) {
		String nfcaction = i.getAction();

		// Handle legacy intent as well as GSMA standard type intent
		if (nfcaction != null) {
			if (nfcaction.equals(PaymentConstants.TRANSACTION_EVENT_LEGACY)) {
				updateUI(i
						.getByteArrayExtra(PaymentConstants.TRANSACTION_EVENT_EXTRA_DATA_LEGACY));
			} else if (nfcaction
					.equals(PaymentConstants.TRANSACTION_EVENT_GSMA)) {
				updateUI(i
						.getByteArrayExtra(PaymentConstants.TRANSACTION_EVENT_EXTRA_DATA_GSMA));
			}
		}
	}

	private void updateUI(byte[] payload) {
		if (todo == TODO_PAY_NOW || payload.length == 2) {
			// push is caused by second TAP
			todo = TODO_CONFIRM;

			// Remove PIN entry stuff
			findViewById(R.id.PIN_entry).setVisibility(View.INVISIBLE);
			findViewById(R.id.enter_pin_label).setVisibility(View.INVISIBLE);

			// Remove countdown
			findViewById(R.id.countdown).setVisibility(View.INVISIBLE);
			findViewById(R.id.seconds).setVisibility(View.INVISIBLE);
			findViewById(R.id.please_tap_label).setVisibility(View.INVISIBLE);

			// Show confirmation message and exit button
			findViewById(R.id.confirmation_button).setVisibility(View.VISIBLE);
			findViewById(R.id.confirmation_button).setOnClickListener(
					new OnClickListener() {
						@Override
						public void onClick(View view) {
							finish();
						}
					});

			TextView tv = (TextView) findViewById(R.id.confirmation);
			tv.setVisibility(View.VISIBLE);

			if (payload[1] == 1) {
				// Successfull payment
				tv.setTextColor(Color.GREEN);
				tv.setText("Payment authorised");
			} else {
				tv.setTextColor(Color.RED);
				tv.setText("Payment not authorised");
			}
		} else {
			// push is caused by first TAP
			todo = TODO_VERIFY;

			TextView amountTv = (TextView) findViewById(R.id.enter_pin_label);
			String amount = "0";

			if (payload.length > 0) {
				amount = new String(payload, 1, payload.length - 1);
			}
			// Display payment form
			amountTv.setText("Please enter PIN to confirm payment of " + amount);
		}
	}

	void makeToast(String message) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast,
				(ViewGroup) findViewById(R.id.toast_layout_root));

		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(message);

		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}

	/*
	 * Static method to kickstart the activity
	 */
	static void kick(Context ctx, int purpose) {
		Intent intnt = new Intent();
		intnt.setClass(ctx, ActivationActivity.class);
		intnt.putExtra(PURPOSE_EXTRA, purpose);
		intnt.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intnt);
	}

	static void kick(Context ctx) {
		kick(ctx, TODO_ACTIVATE);
	}
}
