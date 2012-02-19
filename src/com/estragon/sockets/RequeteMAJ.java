package com.estragon.sockets;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.estragon.chessdiags2.Appli;
import com.estragon.chessdiags2.ChessDiags;
import com.estragon.chessdiags2.R;
import com.estragon.sql.DatabaseHelper2;
import com.j256.ormlite.dao.Dao;

import core.Problem;
import core.Source;
import donnees.ListeProblemes;

public class RequeteMAJ extends Requete implements IRequete {

	public static final int METHODE = Requete.POST;
	
	public RequeteMAJ(Source source) {
		super(METHODE,source.getUrl(),source);
	}

	@Override
	public void onFail(Exception e) {
		// TODO Auto-generated method stub
		this.message = Appli.getInstance().getString(R.string.error);
		super.onFail(e);
	}

	@Override
	public void onSuccess(final JSONObject data) {
		// TODO Auto-generated method stub
		Log.i(ChessDiags.NOMLOG,"Result of the update of source "+source.getId());
		final Handler handler = new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				// TODO Auto-generated method stub
				finRequete(data);
				return true;
			}
		});
		new Thread(new Runnable() {
			public void run() {
				importerJSON(source.getId(), data);
				handler.sendEmptyMessage(2);
			}
		}).start();
		this.message = Appli.getInstance().getString(R.string.ok);
	}
	
	private void finRequete(JSONObject data) {
		super.onSuccess(data);
	}
	
	public static void importerJSON(int source,JSONObject data) {
		StringBuilder liste = new StringBuilder("(-1");
		//SQLiteDatabase db = DatabaseHelper.getHelper().getWritableDatabase();
		long time = System.currentTimeMillis();
		//db.beginTransaction();
		try {
			final Dao<Problem, Integer> simpleDao = DatabaseHelper2.getHelper().getProblemDao();
			JSONArray problemes = data.getJSONArray("diags");
			final ArrayList<Problem> listeProblemes = new ArrayList<Problem>();
			for (int i = 0;i < problemes.length();i++) {
				JSONObject probleme = problemes.getJSONObject(i);
				Problem problem = new Problem(probleme);
				problem.setSource(source);
				//problem.sauvegarder();
				listeProblemes.add(problem);	
				liste.append(","+problem.getId());
			}
			liste.append(")");
			//db.setTransactionSuccessful();


			simpleDao.callBatchTasks(new Callable<Void>() {
				public Void call() throws Exception {
					Problem old = null;
					for (Problem problem : listeProblemes) {
						old = ListeProblemes.getListe().getProblem(problem.getId(), problem.getSource());
						if (old == null) {
							simpleDao.createOrUpdate(problem);
						}
					}
					return null;
				}
			});

			DatabaseHelper2.getHelper().getWritableDatabase().execSQL("DELETE FROM problem WHERE source = "+source+" AND id NOT IN "+liste);
		}
		catch (Exception e) {
			Log.e(ChessDiags.NOMLOG, "Erreur ", e);
		}
		finally {
			//db.endTransaction();
		}
		Log.e(ChessDiags.NOMLOG,(System.currentTimeMillis() - time)+" ms");
	}
	


}
