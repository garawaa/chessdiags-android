package com.billing;

import com.billing.BillingService.BillingServiceListener;
import com.estragon.chessdiags2.Appli;

import android.app.PendingIntent;
import android.content.Context;

public class Billing {

	public static BillingService mBillingService = new BillingService();

	public static void connexion(Context c,BillingServiceListener l) {
		mBillingService.setListener(l);
		if (isConnecte()) l.connecte();
		else {
			mBillingService.setContext(c);
			mBillingService.goBind();
		}
	}
	
	public static void deconnexion() {
		if (mBillingService != null) {
			mBillingService.unbind();
		}
	}
	
	public static PendingIntent acheter() {
		return mBillingService.acheter();
	}
	
	public static boolean isConnecte() {
		if (mBillingService == null) return false;
		return mBillingService.isConnecte();
	}

}