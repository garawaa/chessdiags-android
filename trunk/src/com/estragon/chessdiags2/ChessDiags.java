package com.estragon.chessdiags2;

import greendroid.app.GDListActivity;
import greendroid.widget.ActionBar.OnActionBarListener;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.json.JSONArray;
import org.json.JSONObject;

import ressources.Ressources;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.estragon.chessdiags2.ProblemAdapter.ProblemItem;
import com.estragon.engine.Engine;
import com.estragon.sockets.IRequete.RequeteAnnuleeException;
import com.estragon.sockets.IResultat;
import com.estragon.sockets.Requete;
import com.estragon.sockets.RequeteMAJ;
import com.estragon.sql.DAO;
import com.estragon.sql.DatabaseHelper;
import com.estragon.sql.DatabaseHelper2;
import com.estragon.sql.SimpleData;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import core.Problem;
import core.Source;
import donnees.ListeProblemes;
import donnees.ListeSources;

public class ChessDiags extends GDListActivity implements IResultat  {

	ProblemAdapter adapter;
	public static final String NOMLOG = "ChessDiags";
	private static Engine engine = null;
	ChessProgressDialog dialog;

	ActionBarItem refresh;
	ActionBarItem add;
	ActionBarItem settings;


	private DatabaseHelper2 databaseHelper = null;

	public static Engine getEngine() {
		return engine;
	}

	public void majListe() {
		adapter.charger();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	public void verifPremiereFois() {
		int version = this.getPreferences(MODE_PRIVATE).getInt("derniereVersion", 0);
		if (version == 0) new AlertDialog.Builder(this).setMessage(this.getString(R.string.welcome)).setTitle(R.string.welcometitle).setPositiveButton(android.R.string.ok, null).show();
		this.getPreferences(MODE_PRIVATE).edit().putInt("derniereVersion", ChessDiags.getVersionCode()).commit();
	}

	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		startActivity(new Intent(this, TestActivity.class));
		return super.onKeyUp(keyCode, event);
	}

	public static int getVersionCode() 
	{
		try {
			ComponentName comp = new ComponentName(Appli.getInstance().getApplicationContext(), ChessDiags.class);
			PackageInfo pinfo = Appli.getInstance().getApplicationContext().getPackageManager().getPackageInfo(comp.getPackageName(), 0);
			return pinfo.versionCode;
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			return 0;
		}
	}

	public static String getVersionName() 
	{
		try {
			ComponentName comp = new ComponentName(Appli.getInstance().getApplicationContext(), ChessDiags.class);
			PackageInfo pinfo = Appli.getInstance().getApplicationContext().getPackageManager().getPackageInfo(comp.getPackageName(), 0);
			return pinfo.versionName;
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			return "?";
		}
	}

	private DatabaseHelper2 getHelper() {
		if (databaseHelper == null) {
			databaseHelper =
					OpenHelperManager.getHelper(this, DatabaseHelper2.class);
		}
		return databaseHelper;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Requete.addListener(this);

		//Chargement du moteur de calcul, dans un thread séparé. Ne fait rien si il est déjà chargé.
		this.loadEngine();

		//Chargement des ressources (en async) 
		Ressources.charger();
		
		//Initiation du dialog de chargement
		dialog = new ChessProgressDialog(this);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE); //Pas de titre d'activity, l'action bar en contient déjà un
		this.getActionBar().removeViewAt(0); //Suppression du bouton home
		refresh = getActionBar().addItem(Type.Add).setDrawable(R.drawable.gd_action_bar_refresh);
		add = getActionBar().addItem(Type.Add).setDrawable(R.drawable.gd_action_bar_add);
		settings = getActionBar().addItem(Type.Settings).setDrawable(R.drawable.gd_action_bar_settings);

		this.getActionBar().setOnActionBarListener(new OnActionBarListener() {

			@Override
			public synchronized void onActionBarItemClicked(int position) {
				// TODO Auto-generated method stub
				if (position == 0) {
					//Update pour chaque repository
					try {
						dialog.dismiss();
					}
					catch (Exception e) {

					}
					dialog = new ChessProgressDialog(ChessDiags.this);
					dialog.show();
					dialog.setMessage(getString(R.string.updating));
					dialog.setMax(100);
					dialog.setProgress(0);
					for (Source source : ListeSources.getListe()) {
						if (source.getUrl() != null) {
							Log.i(ChessDiags.NOMLOG,"Updating source : "+source.getUrl()+" ("+source.getId()+")");
							if (dialog.getMax() == 100) dialog.setMax(1);
							else dialog.setMax(dialog.getMax()+1);
							//new RequeteMAJ(source.getUrl(),source.getId()).executer();
						}
					}
				}
				else if (position == 1) {
					Intent i = new Intent(ChessDiags.this,NewProblem.class);
					startActivity(i);
				}
				else if (position == 2) {
					Intent i = new Intent(ChessDiags.this,ChessPreferences.class);
					startActivity(i);
				}
			}

		});

		adapter = new ProblemAdapter(this,1);


		try {
			majListe();
		}
		catch (Exception e) {
			Log.e(ChessDiags.NOMLOG,"",e);
		}




		setListAdapter(adapter);

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Object item = adapter.getItem(position);
				if (item instanceof ProblemItem) {
					ProblemItem problemItem = (ProblemItem) item;
					ouvrirProbleme(problemItem.getProblem());
				}

			}
		});

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				// TODO Auto-generated method stub
				Object item = adapter.getItem(position);
				if (item instanceof ProblemItem) {
					ProblemItem problemItem = (ProblemItem) item;
					Problem probleme = problemItem.getProblem();
					if (probleme.isDeletable()) {
						dialogSupprimerDiagramme(ChessDiags.this,probleme);
					}
				}
				return true;
			}
		});

		verifPremiereFois();

	}

	public void dialogSupprimerDiagramme(Context context,final Problem probleme) {
		new AlertDialog.Builder(context)
		.setMessage(getString(R.string.delete)+" "+probleme.getNom()+" ?")
		.setPositiveButton(getString(R.string.yes), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				DAO.deleteProblem(probleme.getSource(),probleme.getId());
				majListe();
			}
		})
		.setNegativeButton(getString(R.string.no), null)
		.show();
	}



	public void ouvrirProbleme (Problem p) {
		if (p == null) {
			Log.e(ChessDiags.NOMLOG,"The problem to open is null :(");
		}
		else ouvrirProbleme(p.getId(), p.getSource());
	}

	public void ouvrirProbleme(int id,int source) {
		final Intent i = new Intent(this,Diagramme.class);
		Bundle bundle = new Bundle();
		bundle.putInt("secondid", id);
		bundle.putInt("source", source);
		i.putExtras(bundle);
		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setMessage(getString(R.string.loadingchessengine));
		dialog.show();
		new Thread(new Runnable() {
			public void run() {
				Ressources.waitForLoad();
				engine.waitForLoad();
				runOnUiThread(new Runnable() {
					public void run() {
						dialog.dismiss();
						startActivity(i);
					}
				});
			}
		}).start();
	}

	/**
	 * Load the chess engine
	 */
	public void loadEngine() {
		new Thread(new Runnable() {
			public void run() {
				engine = new Engine();
				engine.load();
			}
		}).start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) { 
		super.onKeyDown(keyCode, event);
		return false; 
	}    

	public static void shouldMaj(boolean bool) {
		Appli.getInstance().getSharedPreferences("vars",Activity.MODE_PRIVATE).edit().putBoolean("shouldMAJListe",bool).commit();
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public synchronized void resultatRequete(final JSONObject data, Requete requeteSource) {
		// TODO Auto-generated method stub
		if (requeteSource instanceof RequeteMAJ) {
			//final int source = ((RequeteMAJ) requeteSource).getSource();
			final int source = 2;
			Log.i(ChessDiags.NOMLOG,"Result of the update of source "+source);
			new Thread(new Runnable() {
				public void run() {
					//String liste = "(-1";
					//SQLiteDatabase db = DatabaseHelper.getHelper().getWritableDatabase();
					long time = System.currentTimeMillis();
					//db.beginTransaction();
					try {
						final Dao<Problem, Integer> simpleDao = getHelper().getProblemDao();
						JSONArray problemes = data.getJSONArray("diags");
						final ArrayList<Problem> listeProblemes = new ArrayList<Problem>();
						for (int i = 0;i < problemes.length();i++) {
							JSONObject probleme = problemes.getJSONObject(i);
							Problem problem = new Problem(probleme);
							problem.setSource(source);
							//problem.sauvegarder();
							listeProblemes.add(problem);	
							//liste += ","+problem.getId();
						}
						//liste += ")";
						//db.setTransactionSuccessful();

						simpleDao.callBatchTasks(new Callable<Void>() {
							public Void call() throws Exception {
								Problem old = null;
								for (Problem problem : listeProblemes) {
									old = ListeProblemes.getListe().getProblem(problem.getId(), problem.getSource());
									if (old == null || !simpleDao.objectsEqual(problem, old))
											simpleDao.createOrUpdate(problem);
								}
								return null;
							}
						});

						//db.execSQL("DELETE FROM diagrammes WHERE source = "+source+" AND secondid NOT IN "+liste);
					}
					catch (final Exception e) {
						runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(ChessDiags.this, "Erreur "+e, Toast.LENGTH_LONG).show();
							}
						});
					}
					finally {
						//db.endTransaction();
					}
					Log.e(ChessDiags.NOMLOG,(System.currentTimeMillis() - time)+" ms");
					runOnUiThread(new Runnable() {
						public void run() {
							dialog.resultatRequete(ListeSources.getListe().getSourceById(source).getName(),getString(R.string.ok));
						}
					});
				}
			}).start();

		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		try {
			dialog.dismiss();
		}
		catch (Exception e) {

		}
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
		Requete.removeListener(this);
		super.onDestroy();
	}

	public synchronized void onFail(Exception e, Requete requeteSource) {
		if (requeteSource instanceof RequeteMAJ) {
			//int source = ((RequeteMAJ) requeteSource).getSource();
			int source = 2;
			dialog.resultatRequete(ListeSources.getListe().getSourceById(source).getName(),getString(R.string.error));
			if (e instanceof RequeteAnnuleeException) return; //Rien de spécial, juste l'annulation
		}
	}
}