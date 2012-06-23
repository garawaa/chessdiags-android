package com.estragon.sockets;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.estragon.chessdiags2.Appli;
import com.estragon.chessdiags2.R;
import com.estragon.sql.DAO;

import core.Problem;
import core.Source;


public class RequeteUpload extends Requete implements IRequete {

	public static final int METHODE = Requete.POST;
	public static final int UPLOAD_OK = 1, DEJA_PRESENT = 2, REFUSE = 3;
	
	public RequeteUpload(Problem problem,Source source) {
		super(METHODE,source.getUrl(),source);
		JSONObject json = new JSONObject();
		JSONArray diags = new JSONArray();
		diags.put(problem.getJSON());
		try {
			json.put("diags", diags);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		this.ajouterParametre("diags", json.toString());
	}
	
	@Override
	public void onFail(Exception e) {
		// TODO Auto-generated method stub
		this.message = Appli.getInstance().getString(R.string.error);
		DAO.setUploadSupported(source.getId(),false);
		source.setUploadSupported(false);
		super.onFail(e);
	}

	@Override
	public void onSuccess(final JSONObject data) {
		// TODO Auto-generated method stub
		DAO.setUploadSupported(source.getId(),true);
		source.setUploadSupported(true);
		Log.i("Chessdiags","Résultat upload "+source);
		this.message = Appli.getInstance().getString(R.string.invalidresponse);
		try {
			JSONArray array = data.getJSONArray("results");
			int result = -1;
			//Les réponses peuvent être multiples, mais dans notre cas on a envoyé qu'un seul problème
			for (int i = 0; i < array.length(); i++) {
				int resultatUpload = array.getJSONObject(i).getInt("result");
				result = resultatUpload;
			}
			if (result == UPLOAD_OK) message = Appli.getInstance().getString(R.string.ok);
			else if (result == DEJA_PRESENT) message = Appli.getInstance().getString(R.string.problemalreadysubmitted);
			else if (result == REFUSE) message = Appli.getInstance().getString(R.string.problemrefused);
			
		}
		catch (Exception e) {
			Log.e("Chessdiags","",e);
		}
		super.onSuccess(data);
	}

	

}
