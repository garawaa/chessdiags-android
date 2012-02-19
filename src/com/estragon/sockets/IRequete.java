package com.estragon.sockets;

import org.json.JSONException;
import org.json.JSONObject;


public interface IRequete {
	public void onSuccess(JSONObject data);
	public void onFail(Exception e);
	
	public class RequeteAnnuleeException extends Exception {
		
	}
}
