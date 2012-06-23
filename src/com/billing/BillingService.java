package com.billing;


import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.android.vending.billing.IMarketBillingService;

public class BillingService extends Service implements ServiceConnection {

	public static final String TAG = "ChessdiagsBilling";
	static IMarketBillingService mService;
	volatile boolean connecte = false;
	BillingServiceListener listener = null;


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if(intent == null || intent.getAction() == null) return;
		if (intent.getAction().equals(Consts.ACTION_GET_PURCHASE_INFORMATION)) {
			String notifyId = intent.getStringExtra(Consts.NOTIFICATION_ID);
			Bundle bundle = makeRequestBundle("GET_PURCHASE_INFORMATION");
			bundle.putLong(Consts.BILLING_REQUEST_NONCE, Security.generateNonce());
			bundle.putStringArray(Consts.BILLING_REQUEST_NOTIFY_IDS, new String[] {notifyId});
			try {
				Bundle response = mService.sendBillingRequest(bundle);
			}
			catch (RemoteException e) {

			}

		}
		else if (intent.getAction().equals(Consts.ACTION_PURCHASE_STATE_CHANGED)) {
			String signedData = intent.getStringExtra(Consts.INAPP_SIGNED_DATA);
			String signature = intent.getStringExtra(Consts.INAPP_SIGNATURE);

			//Un achat a été fait
			Toast.makeText(this, com.estragon.chessdiags2.R.string.thanksforsupport, 100).show();
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		Log.i(TAG, "MarketBillingService connected.");
		mService = IMarketBillingService.Stub.asInterface(service);
		Bundle request = makeRequestBundle("CHECK_BILLING_SUPPORTED");
		try {
			Bundle response = mService.sendBillingRequest(request);
			int statut = response.getInt(Consts.BILLING_RESPONSE_RESPONSE_CODE);
			setConnecte(true);
		}
		catch (RemoteException e) {

		}
		catch (Exception e) {
			
		}

	}

	public void setContext(Context context) {
		attachBaseContext(context);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		setConnecte(false);
	}

	protected Bundle makeRequestBundle(String method) {
		Bundle request = new Bundle();
		request.putString(Consts.BILLING_REQUEST_METHOD, method);
		request.putInt(Consts.BILLING_REQUEST_API_VERSION, 1);
		request.putString(Consts.BILLING_REQUEST_PACKAGE_NAME, getPackageName());
		return request;
	}


	public PendingIntent acheter() {
		Bundle request = makeRequestBundle("REQUEST_PURCHASE");
		request.putString(Consts.BILLING_REQUEST_ITEM_ID, "don");
		try {
			Bundle reponse = mService.sendBillingRequest(request);
			PendingIntent fun = reponse.getParcelable(Consts.BILLING_RESPONSE_PURCHASE_INTENT);
			return fun;
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	

	public void goBind() {
		try {
			boolean bindResult = bindService(
					new Intent("com.android.vending.billing.MarketBillingService.BIND"), this,
					Context.BIND_AUTO_CREATE);
			if (bindResult) {
				Log.i(TAG, "Service bind successful.");
			} else {
				Log.e(TAG, "Could not bind to the MarketBillingService.");
			}
		} catch (SecurityException e) {
			Log.e(TAG, "Security exception: " + e);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Unbinds from the MarketBillingService. Call this when the application
	 * terminates to avoid leaking a ServiceConnection.
	 */
	public void unbind() {
		try {
			unbindService(this);
		} catch (IllegalArgumentException e) {
			// This might happen if the service was disconnected
		}
	}
	
	public boolean isConnecte() {
		return connecte;
	}
	
	public void setListener(BillingServiceListener l) {
		listener = l;
	}
	
	public void removeListener() {
		listener = null;
	}
	
	public void setConnecte(boolean etat) {
		this.connecte = etat;
		if (listener != null) {
			if (etat) listener.connecte();
			else listener.deConnecte();
		}
	}
	
	public interface BillingServiceListener {
		public void connecte();
		public void deConnecte();
	}
}
