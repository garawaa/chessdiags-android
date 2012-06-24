package com.estragon.chessdiags2;

import java.util.HashMap;

import ressources.Ressources;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.estragon.chessdiags2.ProblemListFragment.ListItemSelectedListener;
import com.estragon.engine.Engine;
import com.estragon.sockets.MultiRequete;
import com.estragon.sockets.RequeteMAJ;
import com.estragon.sql.DAO;
import com.viewpagerindicator.TitlePageIndicator;

import core.Problem;
import core.Source;
import donnees.ListeProblemes;
import donnees.ListeSources;

public class TestActivity extends SherlockFragmentActivity implements OnDismissListener, ListItemSelectedListener, OnPageChangeListener {

	private ViewPager viewPager;
	private PagerAdapter adapter;
	HashMap<Integer, ProblemAdapter> adapters = new HashMap<Integer, ProblemAdapter>();

	static final int DIALOG_LOADING = 0, DIALOG_PREMIERE_FOIS = 1, DIALOG_UPDATE = 2;

	MenuItem add;
	MenuItem refresh;
	MenuItem settings;
	MenuItem sort;
	TitlePageIndicator pageIndicator;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.testactivity);

		setTitle(R.string.app_name);

		verifPremiereFois();

		//Chargement du moteur de calcul, dans un thread séparé. Ne fait rien si il est déjà chargé.
		Engine.loadEngine();

		adapter = new ChessdiagsPagerAdapter(getSupportFragmentManager());
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(this);
		
		
		pageIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
		pageIndicator.setViewPager(viewPager);
	}

	public static int getVersionCode() 
	{
		try {
			ComponentName comp = new ComponentName(Appli.getInstance().getApplicationContext(), TestActivity.class);
			PackageInfo pinfo = Appli.getInstance().getApplicationContext().getPackageManager().getPackageInfo(comp.getPackageName(), 0);
			return pinfo.versionCode;
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			return 0;
		}
	}

	public static String getVersionName() 
	{
		try {
			ComponentName comp = new ComponentName(Appli.getInstance().getApplicationContext(), TestActivity.class);
			PackageInfo pinfo = Appli.getInstance().getApplicationContext().getPackageManager().getPackageInfo(comp.getPackageName(), 0);
			return pinfo.versionName;
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			return "?";
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		add = menu.add("New problem");
		add.setIcon(R.drawable.gd_action_bar_add)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		refresh = menu.add("Update");
		refresh.setIcon(R.drawable.gd_action_bar_refresh)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		/*sort = menu.add("Sort");
		sort.setIcon(R.drawable.gd_action_bar_sort_by_size)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);*/
		settings = menu.add("Settings");
		settings.setIcon(R.drawable.gd_action_bar_settings)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == refresh) {
			lancerMAJ();
		}
		else if (item == add) {
			Intent i = new Intent(TestActivity.this,NewProblem.class);
			startActivity(i);
		}
		else if (item == settings) {
			Intent i = new Intent(TestActivity.this,ChessPreferences.class);
			startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}



	public void lancerMAJ() {
		MultiRequete maj = new MultiRequete();
		for (Source source : ListeSources.getListe()) {
			if (source.getUrl() != null) maj.addRequete(new RequeteMAJ(source));
		}
		if (maj.getNbRequetes() != 0) {
			maj.executer();
			showDialog(DIALOG_UPDATE);			
		}
	}



	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		notifyChanges();
		super.onResume();
	}

	private void notifyChanges() {
		adapter.notifyDataSetChanged();
		pageIndicator.notifyDataSetChanged();
	}


	public void dialogSupprimerDiagramme(final Problem probleme) {
		new AlertDialog.Builder(this)
		.setMessage(getString(R.string.delete)+" "+probleme.getNom()+" ?")
		.setPositiveButton(getString(R.string.yes), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				DAO.deleteProblem(probleme.getSource(),probleme.getId());
				notifyChanges();
			}
		})
		.setNegativeButton(getString(R.string.no), null)
		.show();
	}



	public void ouvrirProbleme (Problem p) {
		if (p == null) {
			Log.e("Chessdiags","The problem to open is null :(");
		}
		else ouvrirProbleme(p.getId(), p.getSource());
	}

	public void ouvrirProbleme(int id,int source) {
		final Intent i = new Intent(this,Diagramme.class);
		Bundle bundle = new Bundle();
		bundle.putInt("secondid", id);
		bundle.putInt("source", source);
		i.putExtras(bundle);
		showDialog(DIALOG_LOADING);
		new Thread(new Runnable() {
			public void run() {
				Ressources.waitForLoad();
				Engine.waitForLoad();
				runOnUiThread(new Runnable() {
					public void run() {
						try {
							dismissDialog(DIALOG_LOADING);
						}
						catch (Exception e) {

						}
						startActivity(i);
					}
				});
			}
		}).start();
	}



	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	public void verifPremiereFois() {
		int version = this.getPreferences(MODE_PRIVATE).getInt("derniereVersion", 0);
		if (version == 0) {
			lancerMAJ();
			showDialog(DIALOG_PREMIERE_FOIS);
		}
		this.getPreferences(MODE_PRIVATE).edit().putInt("derniereVersion", 1).commit();
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		ChessProgressDialog dialog2;
		switch(id) {
		case 1:
			dialog = new AlertDialog.Builder(this).setMessage(this.getString(R.string.welcome)).setTitle(R.string.welcometitle).setPositiveButton(android.R.string.ok, null).create();
			break;
		case 2:
			dialog2 = new ChessProgressDialog(TestActivity.this);
			dialog2.setTitle(R.string.updating);
			dialog = dialog2;
			break;
		case DIALOG_LOADING:
			final ProgressDialog dialog3 = new ProgressDialog(this);
			dialog3.setMessage(getString(R.string.loadingchessengine));
			dialog3.setTitle(R.string.loading);
			dialog = dialog3;
			break;
		default:
			dialog = null;
		}
		return dialog;

	}



	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		// TODO Auto-generated method stub
		if (id == DIALOG_UPDATE) {
			dialog.setOnDismissListener(TestActivity.this);
			((ChessProgressDialog) dialog).maj();
		}
		super.onPrepareDialog(id, dialog);
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// TODO Auto-generated method stub
		try {
			removeDialog(DIALOG_UPDATE);
		}
		catch (Exception e) {

		}
		ListeProblemes.charger();
		ListeSources.charger();
		notifyChanges();
	}


	protected void onRestart() {
		notifyChanges();
		super.onRestart();
	}

	@Override
	public void onListItemSelected(Problem problem) {
		// TODO Auto-generated method stub
		ouvrirProbleme(problem);
	}



	@Override
	public void onLongListItemSelected(Problem problem) {
		// TODO Auto-generated method stub
		if (problem.getSource() == 1) {
			dialogSupprimerDiagramme(problem);
		}
	}



	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		
	}


	



}