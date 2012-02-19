package com.estragon.sockets;

import org.json.JSONObject;

import android.content.Context;

public interface IResultat {
	public Context getContext();
	public void resultatRequete(JSONObject data, Requete source);
	public void onFail(Exception e, Requete source);
}
