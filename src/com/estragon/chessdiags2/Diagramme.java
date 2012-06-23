package com.estragon.chessdiags2;

import java.util.ArrayList;

import widgets.Board;
import widgets.Board.BoardListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.estragon.engine.Engine.EngineNotReadyException;
import com.estragon.sql.DAO;

import core.Partie;
import core.Partie.ParsingException;
import core.PartieListener;
import core.Problem;
import donnees.ListeProblemes;

public class Diagramme extends SherlockActivity implements PartieListener, BoardListener, OnGesturePerformedListener {

	private Board board = null;
	private TextView description;
	private TextView movesLeft;
	private Partie partie = null;
	private int caseChoisie = -1;
	private Problem problem;

	GestureLibrary gLib = null;
	
	MenuItem refresh;
	MenuItem share;
	MenuItem edit;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle == null || !bundle.containsKey("secondid") || !bundle.containsKey("source")) {
			finish();
			return;
		}
		int secondid = bundle.getInt("secondid");
		int source = bundle.getInt("source");

		problem = ListeProblemes.getListe().getProblem(secondid, source);
		if (problem == null)  {
			this.finish();
			return;
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		this.setContentView(R.layout.diagramme);



		board = (Board) this.findViewById(R.id.board);

		Object object = this.getLastNonConfigurationInstance();
		if (object != null) {
			Object[] sauvegarde = (Object[]) object;
			partie = (Partie) sauvegarde[0];
			setCaseChoisie((Integer) sauvegarde[1]);
		}
		else {
			try {
				partie = new Partie(problem);
			}
			catch (EngineNotReadyException e) {
				finish();
				return;
			}
			catch (ParsingException e) {
				Toast.makeText(this, R.string.invalidfen, Toast.LENGTH_LONG).show();
				finish();
				return;
			}
		}

		partie.addPartieListener(this);

		board.chargerPartie(partie);
		board.addListener(this);

		description = (TextView) this.findViewById(R.id.description);

		

		movesLeft = (TextView) this.findViewById(R.id.nbmoves);

		gLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
		gLib.load();

		GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
		gestures.addOnGesturePerformedListener(this);
		gestures.setGestureVisible(false);

		refreshTitle();
	}


	public void refreshTitle() {
		this.setTitle(problem.getNom());
		movesLeft.setText(partie.getNbMovesLeft()+" "+getString(R.string.movesleft));
		description.setText(problem.getDescription());
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		try {
			partie.removePartieListener(this);
		}
		catch (Exception e) {

		}
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		// TODO Auto-generated method stub
		share = menu.add("Share");
		share.setIcon(R.drawable.gd_action_bar_share)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		refresh = menu.add("Try again");
		refresh.setIcon(R.drawable.gd_action_bar_refresh)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		edit = menu.add("Edit");
		edit.setIcon(R.drawable.gd_action_bar_edit)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		edit.setVisible(problem.isEditable());
		return super.onCreateOptionsMenu(menu);
	}

	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == android.R.id.home) finish(); //home
		else if (item == refresh) {
			//Refresh button
			try {
				this.partie.importerProbleme(problem);
			}
			catch (Exception e) {

			}
			refreshTitle();
		}
		else if (item == edit && problem.isEditable()) {
			//Edit problem button
			Intent i = new Intent(this,NewProblem.class);
			i.putExtra("id", problem.getId());
			i.putExtra("source", problem.getSource());
			startActivity(i);
			finish(); // ? à voir
		}
		else if (item == share) {
			NewProblem.intentShare(this, problem);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		//Debug only
		//new AlertDialog.Builder(this).setMessage(this.partie.getPosition());
		return super.onKeyDown(keyCode, event);
	}


	@Override
	public Object onRetainNonConfigurationInstance() {
		// TODO Auto-generated method stub
		Object[] object = new Object[2];
		object[0] = partie;
		object[1] = caseChoisie;
		return object;
	}


	@Override
	public void positionChangee() {
		// TODO Auto-generated method stub
		this.runOnUiThread(new Runnable() {
			public void run() {
				majPosition();
			}
		});
	}

	public void majPosition() {
		board.majBoard();
		refreshTitle();
	}





	public void setCaseChoisie(int caseChoisie) {
		this.caseChoisie = caseChoisie;
		board.highLight(caseChoisie);
	}


	/*@Override
	public void onActionBarItemClicked(int position) {
		// TODO Auto-generated method stub
		if (position == -1) finish();
		else if (position == 0) {
			//Refresh button
			try {
				this.partie.importerProbleme(problem);
			}
			catch (Exception e) {

			}
			refreshTitle();
		}
		else if (position == 1 && problem.isEditable()) {
			//Edit problem button
			Intent i = new Intent(this,NewProblem.class);
			i.putExtra("id", problem.getId());
			i.putExtra("source", problem.getSource());
			startActivity(i);
			finish(); // ? à voir
		}
		else if (position == 2 || (position == 1 && !problem.isEditable())) {
			NewProblem.intentShare(this, problem);
		}
	}*/


	@Override
	public void caseClickee(int numCase) {
		// TODO Auto-generated method stub
		if (caseChoisie == numCase) {
			setCaseChoisie(-1);
		}
		else if (caseChoisie == -1) {
			setCaseChoisie(numCase);
		}
		else {
			this.partie.proposerCoup(caseChoisie,numCase);
			setCaseChoisie(-1);
		}
		board.majBoard();
	}


	@Override
	public void partieTerminee(final int resultat) {
		// TODO Auto-generated method stub
		this.runOnUiThread(new Runnable() {
			public void run() {
				if (resultat == PartieListener.WIN) {
					DAO.diagrammeResolu(problem);
					new AlertDialog.Builder(Diagramme.this).setMessage(getString(R.string.congratulations)).setNegativeButton(getString(R.string.ok), null).setPositiveButton(R.string.nextproblem, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							loadNextProblem(true);
						}
					}).show();

				}
				else {
					new AlertDialog.Builder(Diagramme.this).setMessage(R.string.fail).setPositiveButton(R.string.ok, null).setNegativeButton(R.string.tryagain, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							try {
								Diagramme.this.partie.importerProbleme(problem);
								refreshTitle();
							}
							catch (Exception e) {
								Log.e("Diagramme : ","",e);
							}
						}
					}).show();
				}
			}
		});
	}

	public void loadNextProblem(boolean sens) {
		Problem next = ListeProblemes.getListe().getNextProblem(problem.getSource(),problem.getId(),sens);
		if (next == null) {
			if (sens) Toast.makeText(Diagramme.this, R.string.nextproblemnotfound, Toast.LENGTH_LONG).show();
		}
		else {
			Diagramme.this.problem = next;
			try {
				Diagramme.this.partie.importerProbleme(problem);
				refreshTitle();
			}
			catch (Exception e) {
				Log.e("Diagramme : ","",e);
			}
		}
	}

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		// TODO Auto-generated method stub
		ArrayList<Prediction> predictions = gLib.recognize(gesture);

		// one prediction needed
		if (predictions.size() > 0) {
			Prediction prediction = predictions.get(0);
			// checking prediction
			if (prediction.score > 1.0) {
				loadNextProblem(prediction.name.equals("right"));
			}
				
		}
	}

}
