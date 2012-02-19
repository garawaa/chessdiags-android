package com.estragon.sockets;

import android.os.AsyncTask;
import android.util.Log;

public class RequeteMachine extends AsyncTask<Requete, Requete, String> {
	
	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Requete... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		values[0].requeteComplete();
	}

	@Override
	protected String doInBackground(Requete... params) {
		// TODO Auto-generated method stub
		params[0].go();
		this.publishProgress(params[0]);
		return null;
	}

}
