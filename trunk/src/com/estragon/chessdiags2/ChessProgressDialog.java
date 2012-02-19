package com.estragon.chessdiags2;

import java.util.Iterator;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.Toast;

import com.estragon.sockets.IResultat;
import com.estragon.sockets.MultiRequete;
import com.estragon.sockets.Requete;

public class ChessProgressDialog extends ProgressDialog implements IResultat {

	private CharSequence message = "";
	static Bundle data = new Bundle();




	public ChessProgressDialog(Context context) {
		super(context);
		setCancelable(false);
		setCanceledOnTouchOutside(false);
		
		
		OnClickListener listener = null;
		this.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.ok), listener);


		setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog,
					int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
	
	    
	    maj();
	    Requete.addListener(this);
	}

	public boolean onSearchRequested() {
		return false;
	}
	
	public void maj() {
		MultiRequete requete = MultiRequete.getActive();
		if (requete == null) {
			dismiss();
			return;
		}
		setMax(requete.getNbRequetes());
		setProgress(requete.getAvancement());
		verifButtonState();
		refreshMessage();
	}
	
	public void refreshMessage() {
		String message = "";
		MultiRequete requeteMulti = MultiRequete.getActive();
		if (requeteMulti == null) {
			dismiss();
			return;
		}
		Iterator<Requete> iterator = requeteMulti.getIterator();
		while (iterator.hasNext()) {
			Requete requete = iterator.next();
			message += requete.getSource().getName()+" "+requete.getMessage()+System.getProperty("line.separator");
		}
		setMessage(message);
	}
	

	public void setMessage(CharSequence message) {
		this.message = message;
		super.setMessage(message);

	}

	public void verifButtonState() {
		try {
			this.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(this.getProgress() == this.getMax());
		}
		catch (NullPointerException e) {

		}
	}

	public CharSequence getMessage() {
		return message;
	}

	public void appendMessage(CharSequence message) {
		this.setMessage(getMessage()+message.toString());
	}

	public void resultatRequete(String nom, String msg) {
		this.appendMessage(System.getProperty("line.separator")+nom+" "+msg);
		this.setProgress(getProgress()+1);
	}

	@Override
	public void setProgress(int value) {
		// TODO Auto-generated method stub
		super.setProgress(value);
		verifButtonState();
	}

	@Override
	public void setMax(int max) {
		// TODO Auto-generated method stub
		super.setMax(max);
		verifButtonState();
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		maj();
		super.onStart();
	}

	
	@Override
	public void resultatRequete(JSONObject data, Requete source) {
		// TODO Auto-generated method stub
		maj();
	}

	@Override
	public void onFail(Exception e, Requete source) {
		// TODO Auto-generated method stub
		maj();
	}
}
