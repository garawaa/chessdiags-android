package com.estragon.sockets;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import util.StreamUtils;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.estragon.chessdiags2.Appli;
import com.estragon.chessdiags2.ChessDiags;

import core.Source;

public abstract class Requete implements IRequete {
	public static final String BASEURL = "http://chessdiags.com/API/";
	public static final int EN_COURS = 0, ERREUR = 1, TRAITEMENT_ENCOURS = 2, TRAITEMENT_TERMINE = 3;
	public static int VERSION = ChessDiags.getVersionCode();
	private static CookieStore cookieStore = new BasicCookieStore();
	public static int GET = 1;
	public static int POST = 2;
	protected RequeteMachine machine = new RequeteMachine();
	private Exception erreur = null;
	protected HttpRequestBase requete = null;
	protected HttpResponse reponse = null;
	protected String resultat = null;
	List<NameValuePair> parametres = new ArrayList<NameValuePair>(); 
	boolean aborted = false;
	static ArrayList<IResultat> listeners = new ArrayList<IResultat>();
	int statut = EN_COURS;
	Source source;
	String message = "...";
	public static int SOCKET_TIMEOUT = 10000;
	public static int CONNECTION_TIMEOUT = 10000;

	protected void ajouterParametre(String name,String value) {
		parametres.add(new BasicNameValuePair(name, value));
	}
	
	public String getMessage() {
		return message;
	}

	public Requete(int methode,String url,Source source) {
		if (methode == GET) requete = new HttpGet(url);
		else requete = new HttpPost(url);

		this.source = source;
	}
	
	public Source getSource() {
		return source;
	}

	public Exception getErreur() {
		return erreur;
	}

	public boolean isAborted() {
		return aborted;
	}

	public boolean hasErreur() {
		return erreur != null;
	}


	public void executer() {
		if (PreferenceManager.getDefaultSharedPreferences(Appli.getInstance()).getBoolean("compression", true)) this.requete.addHeader("Accept-Encoding", "gzip");
		try {
			if (this.requete.getMethod().equals("POST")) ((HttpPost) (this.requete)).setEntity(new UrlEncodedFormEntity(parametres));
		}
		catch (UnsupportedEncodingException e) {

		}
		machine.execute(this);
	}

	public void go() {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSocketBufferSize(httpParams, SOCKET_TIMEOUT);
		HttpClient client = new DefaultHttpClient(httpParams);
		
		try {
			HttpContext localContext = new BasicHttpContext();
			localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
			reponse = client.execute(this.requete,localContext);
			if (reponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) throw new StatusCodeException(reponse.getStatusLine().getStatusCode());
			Header encoding = reponse.getFirstHeader("Content-Encoding");
			InputStream stream = null;
			if (encoding != null && encoding.toString().contains("gzip")) stream = new GZIPInputStream(reponse.getEntity().getContent());
			else stream = reponse.getEntity().getContent();
			resultat = StreamUtils.convertStreamToString(stream);
		} catch (Exception e) {
			this.erreur = e;
		}
		finally {
			client.getConnectionManager().shutdown();
		}
	}

	/**
	 * Lancé dans le thread graphique
	 */
	public void requeteComplete() {
		if (this.hasErreur()) {
			if (this.isAborted()) {
				//La requete a été annulé par l'utilisateur
				this.onFail(new RequeteAnnuleeException());
			}
			else 
				this.onFail(erreur);
		}
		else {
			try {
				Log.i(ChessDiags.NOMLOG,this.resultat);
				JSONObject lu = new JSONObject(this.resultat);
				statut = TRAITEMENT_ENCOURS;
				this.onSuccess(lu);
			} catch (JSONException e) {
				//Le parsing a raté ... FAIL
				this.onFail(e);
			}
		}
	}

	public void abort() {
		aborted = true;
		requete.abort();
	}
	
	public int getStatut() {
		return statut;
	}

	public static void addListener(IResultat listener) {
		listeners.add(listener);
	}

	public static void removeListener(IResultat listener) {
		listeners.remove(listener);
	}

	public void onFail(Exception e) {
		statut = ERREUR;
		Log.e(ChessDiags.NOMLOG,"Request error : ",e);
		for (IResultat resultat : listeners) {
			resultat.onFail(e, this);
		}
	}

	public void onSuccess(JSONObject data) {
		statut = TRAITEMENT_TERMINE;
		for (IResultat resultat : listeners) {
			resultat.resultatRequete(data, this);
		}
	}
	
	public class StatusCodeException extends Exception {
		private int code = 200;
		public StatusCodeException(int code) {
			this.code = code;
		}
		
		public int getErrorCode() {
			return code;
		}
	}

}
