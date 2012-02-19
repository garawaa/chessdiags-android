package com.billing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.billing.Consts.ResponseCode;

public class BillingReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
        if (Consts.ACTION_PURCHASE_STATE_CHANGED.equals(action)) {
            String signedData = intent.getStringExtra(Consts.INAPP_SIGNED_DATA);
            String signature = intent.getStringExtra(Consts.INAPP_SIGNATURE);
            purchaseStateChanged(context, signedData, signature);
        } else if (Consts.ACTION_NOTIFY.equals(action)) {
            String notifyId = intent.getStringExtra(Consts.NOTIFICATION_ID);
            notify(context, notifyId);
        } else if (Consts.ACTION_RESPONSE_CODE.equals(action)) {
            long requestId = intent.getLongExtra(Consts.INAPP_REQUEST_ID, -1);
            int responseCodeIndex = intent.getIntExtra(Consts.INAPP_RESPONSE_CODE,
                    ResponseCode.RESULT_ERROR.ordinal());
            checkResponseCode(context, requestId, responseCodeIndex);
        } else {
            Log.w("Market", "unexpected action: " + action);
        }
	}
	
    /**
     * This is called when Android Market sends information about a purchase state
     * change. The signedData parameter is a plaintext JSON string that is
     * signed by the server with the developer's private key. The signature
     * for the signed data is passed in the signature parameter.
     * @param context the context
     * @param signedData the (unencrypted) JSON string
     * @param signature the signature for the signedData
     */
    private void purchaseStateChanged(Context context, String signedData, String signature) {
        Intent intent = new Intent(Consts.ACTION_PURCHASE_STATE_CHANGED);
        intent.setClass(context, BillingService.class);
        intent.putExtra(Consts.INAPP_SIGNED_DATA, signedData);
        intent.putExtra(Consts.INAPP_SIGNATURE, signature);
        context.startService(intent);
    }

    /**
     * This is called when Android Market sends a "notify" message  indicating that transaction
     * information is available. The request includes a nonce (random number used once) that
     * we generate and Android Market signs and sends back to us with the purchase state and
     * other transaction details. This BroadcastReceiver cannot bind to the
     * MarketBillingService directly so it starts the {@link BillingService}, which does the
     * actual work of sending the message.
     *
     * @param context the context
     * @param notifyId the notification ID
     */
    private void notify(Context context, String notifyId) {
        Intent intent = new Intent(Consts.ACTION_GET_PURCHASE_INFORMATION);
        intent.setClass(context, BillingService.class);
        intent.putExtra(Consts.NOTIFICATION_ID, notifyId);
        context.startService(intent);
        
    }

    /**
     * This is called when Android Market sends a server response code. The BillingService can
     * then report the status of the response if desired.
     *
     * @param context the context
     * @param requestId the request ID that corresponds to a previous request
     * @param responseCodeIndex the ResponseCode ordinal value for the request
     */
    private void checkResponseCode(Context context, long requestId, int responseCodeIndex) {
        Intent intent = new Intent(Consts.ACTION_RESPONSE_CODE);
        intent.setClass(context, BillingService.class);
        intent.putExtra(Consts.INAPP_REQUEST_ID, requestId);
        intent.putExtra(Consts.INAPP_RESPONSE_CODE, responseCodeIndex);
        context.startService(intent);
    }

}
