package com.estragon.chessdiags2;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBar.OnActionBarListener;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.PageIndicator;

import java.util.HashMap;

import ressources.Ressources;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.estragon.chessdiags2.ProblemAdapter.ProblemItem;
import com.estragon.engine.Engine;
import com.estragon.engine.Engine.EngineNotReadyException;
import com.estragon.sockets.MultiRequete;
import com.estragon.sockets.RequeteMAJ;
import com.estragon.sql.DAO;

import core.Problem;
import core.Source;
import donnees.ListeProblemes;
import donnees.ListeSources;

public class TestActivity extends GDActivity implements OnPageChangeListener, OnDismissListener {

	private ViewPager awesomePager;
	private AwesomePagerAdapter awesomeAdapter;
	HashMap<Integer, ProblemAdapter> adapters = new HashMap<Integer, ProblemAdapter>();


	ActionBarItem refresh;
	ActionBarItem add;
	ActionBarItem settings;
	PageIndicator indicator;
	
	static final int DIALOG_LOADING = 0, DIALOG_PREMIERE_FOIS = 1, DIALOG_UPDATE = 2;



	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); 


		setActionBarContentView(R.layout.testactivity);

		setTitle(R.string.app_name);
		
		verifPremiereFois();

		//Chargement du moteur de calcul, dans un thread séparé. Ne fait rien si il est déjà chargé.
		Engine.loadEngine();


		this.getActionBar().removeViewAt(0); //Suppression du bouton home
		refresh = getActionBar().addItem(Type.Add).setDrawable(R.drawable.gd_action_bar_refresh);
		add = getActionBar().addItem(Type.Add).setDrawable(R.drawable.gd_action_bar_add);
		settings = getActionBar().addItem(Type.Settings).setDrawable(R.drawable.gd_action_bar_settings);

		awesomeAdapter = new AwesomePagerAdapter();
		awesomePager = (ViewPager) findViewById(R.id.pager);
		awesomePager.setAdapter(awesomeAdapter);
		awesomePager.setOnPageChangeListener(this);

		FrameLayout layout = (FrameLayout) findViewById(R.id.page_indicator_prev);
		indicator = new PageIndicator(this);
		indicator.setDotCount(awesomeAdapter.getCount());
		indicator.setGravity(Gravity.CENTER);
		layout.addView(indicator);

		this.getActionBar().setOnActionBarListener(new OnActionBarListener() {

			@Override
			public synchronized void onActionBarItemClicked(int position) {
				// TODO Auto-generated method stub
				if (position == 0) {
					lancerMAJ();
				}
				else if (position == 1) {
					Intent i = new Intent(TestActivity.this,NewProblem.class);
					startActivity(i);
				}
				else if (position == 2) {
					Intent i = new Intent(TestActivity.this,ChessPreferences.class);
					startActivity(i);
				}
			}

		});


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
	
	

	public void dialogSupprimerDiagramme(Context context,final Problem probleme) {
		new AlertDialog.Builder(context)
		.setMessage(getString(R.string.delete)+" "+probleme.getNom()+" ?")
		.setPositiveButton(getString(R.string.yes), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				DAO.deleteProblem(probleme.getSource(),probleme.getId());
				majCurrentTab();
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
		showDialog(DIALOG_LOADING);
		new Thread(new Runnable() {
			public void run() {
				Ressources.waitForLoad();
				Engine.waitForLoad();
				runOnUiThread(new Runnable() {
					public void run() {
						dismissDialog(DIALOG_LOADING);
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



	private class AwesomePagerAdapter extends PagerAdapter{


		@Override
		public int getCount() {
			indicator.setDotCount(ListeSources.getListe().size());
			return ListeSources.getListe().size();
		}

		/**
		 * Create the page for the given position.  The adapter is responsible
		 * for adding the view to the container given here, although it only
		 * must ensure this is done by the time it returns from
		 * {@link #finishUpdate()}.
		 *
		 * @param container The containing View in which the page will be shown.
		 * @param position The page position to be instantiated.
		 * @return Returns an Object representing the new page.  This does not
		 * need to be a View, but can be some other container of the page.
		 */
		@Override
		public Object instantiateItem(View collection, int position) {
			LayoutInflater inflater = (LayoutInflater) collection.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View view = inflater.inflate(R.layout.page, null);
			ListView liste = (ListView) view.findViewById(R.id.awesomelist);
			ProblemAdapter oldAdapter = adapters.get(position);
			if (oldAdapter == null) {
				ProblemAdapter adapter;
				adapter = new ProblemAdapter(liste.getContext(),ListeSources.getListe().get(position));
				adapter.charger();
				adapters.put(position, adapter);
			}
			adapters.get(position).notifyDataSetChanged();
			liste.setAdapter(adapters.get(position));

			final ProblemAdapter adapter = adapters.get(position);

			liste.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Object item = adapter.getItem(position);
					if (item instanceof ProblemItem) {
						ProblemItem problemItem = (ProblemItem) item;
						ouvrirProbleme(problemItem.getProblem());
					}

				}
			});

			liste.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int position, long id) {
					// TODO Auto-generated method stub
					Object item = adapter.getItem(position);
					if (item instanceof ProblemItem) {
						ProblemItem problemItem = (ProblemItem) item;
						Problem probleme = problemItem.getProblem();
						if (probleme.isDeletable()) {
							dialogSupprimerDiagramme(TestActivity.this,probleme);
						}
					}
					return true;
				}
			});



			((ViewPager) collection).addView(view,0);

			return liste;
		}


		/**
		 * Remove a page for the given position.  The adapter is responsible
		 * for removing the view from its container, although it only must ensure
		 * this is done by the time it returns from {@link #finishUpdate()}.
		 *
		 * @param container The containing View from which the page will be removed.
		 * @param position The page position to be removed.
		 * @param object The same object that was returned by
		 * {@link #instantiateItem(View, int)}.
		 */
		@Override
		public void destroyItem(View collection, int position, Object view) {
			((ViewPager) collection).removeView((View) view);
		}



		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view==object;
		}


		/**
		 * Called when the a change in the shown pages has been completed.  At this
		 * point you must ensure that all of the pages have actually been added or
		 * removed from the container as appropriate.
		 * @param container The containing View which is displaying this adapter's
		 * page views.
		 */
		@Override
		public void finishUpdate(View arg0) {}



		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {}

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
		majCurrentTab();
	}

	public void verifPremiereFois() {
		int version = this.getPreferences(MODE_PRIVATE).getInt("derniereVersion", 0);
		if (version == 0) {
			lancerMAJ();
			showDialog(DIALOG_PREMIERE_FOIS);
		}
		this.getPreferences(MODE_PRIVATE).edit().putInt("derniereVersion", ChessDiags.getVersionCode()).commit();
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

	void majIndicator() {
		indicator.setActiveDot(awesomePager.getCurrentItem());
	}

	void majCurrentTab() {
		try {
			majIndicator();
			adapters.get(awesomePager.getCurrentItem()).notifyDataSetChanged();
		}
		catch (Exception e) {
			Log.e(ChessDiags.NOMLOG,"",e);
		}
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		if (ListeSources.hasChanged) {
			adapters.clear();
			awesomePager.setAdapter(new AwesomePagerAdapter());
			ListeSources.hasChanged =false;
		}
		majCurrentTab();
		super.onRestart();
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
		adapters.clear();
		awesomePager.setAdapter(new AwesomePagerAdapter());
		majIndicator();
	}

	
	

}