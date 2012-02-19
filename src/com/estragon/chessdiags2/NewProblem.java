package com.estragon.chessdiags2;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBar.OnActionBarListener;
import greendroid.widget.ActionBarItem;

import java.util.ArrayList;

import widgets.Board;
import widgets.Board.BoardListener;
import widgets.Pieces;
import widgets.Pieces.PiecesListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import chesspresso.Chess;

import com.estragon.sockets.MultiRequete;
import com.estragon.sockets.RequeteUpload;

import core.Partie;
import core.PartieListener;
import core.Problem;
import core.Source;
import donnees.ListeProblemes;
import donnees.ListeSources;

public class NewProblem extends GDActivity implements BoardListener, PiecesListener, OnActionBarListener, PartieListener {

	Partie partie = new Partie();
	Board board;
	Pieces pieces;
	int typePieceChoisie = 0;
	Problem problem = null;
	ActionBarItem item = null;

	public static final int DIALOG_PROGRESS = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setActionBarContentView(R.layout.newproblem);
		this.getActionBar().addItem(ActionBarItem.Type.Add).setDrawable(R.drawable.gd_action_bar_refresh);
		this.getActionBar().addItem(ActionBarItem.Type.Add).setDrawable(R.drawable.gd_action_bar_edit);

		this.getActionBar().setOnActionBarListener(this);

		Object sauvegarde = this.getLastNonConfigurationInstance();
		if (sauvegarde != null) {
			Object[] donnees = (Object[]) sauvegarde;
			partie = (Partie) donnees[0];
			problem = (Problem) donnees[1];
			typePieceChoisie = (Integer) donnees[2];
		}
		else {
			//Chargement des données de l'intent :
			//Soit intent : id / source, soit position
			int id = getIntent().getIntExtra("id", -1);
			int source = getIntent().getIntExtra("source", -1);
			String position = getIntent().getStringExtra("position");
			if (id != -1 && source != -1) 
				problem = ListeProblemes.getListe().getProblem(id, source);
			else if (position != null) {
				try {
					partie.importerPosition(position);
				}
				catch (Exception e) {
					Log.e(ChessDiags.NOMLOG,"",e);
				}
			}
			if (problem != null) {
				try {
					partie.importerProbleme(problem);
				}
				catch (Exception e) {
					Log.e(ChessDiags.NOMLOG,"",e);
				}
			}
			else problem = new Problem(ListeProblemes.getListe().getMaxId(1), 1, partie.getPosition(), 0, false);
		}


		board = (Board) findViewById(R.id.board);
		board.addListener(this);



		checkShare();

		board.chargerPartie(partie);
		pieces = (Pieces) findViewById(R.id.pieces);
		pieces.addListener(this);
		pieces.highLight(typePieceChoisie);
		partie.addPartieListener(this);


		refreshTitle();
	}


	public void refreshTitle() {
		this.setTitle(problem.getNom());
	}


	@Override
	public Object onRetainNonConfigurationInstance() {
		// TODO Auto-generated method stub
		Object[] object = new Object[3];
		object[0] = partie;
		object[1] = problem;
		object[2] = typePieceChoisie;
		return object;
	}



	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	public void checkShare() {
		if (problem.isSauvegarde() && item == null) item = this.getActionBar().addItem(ActionBarItem.Type.Share).setDrawable(R.drawable.gd_action_bar_share);
	}

	@Override
	public void caseClickee(int indexCase) {
		// TODO Auto-generated method stub
		int[] conversion = new int[] {0,Chess.WHITE_PAWN,Chess.WHITE_KNIGHT,Chess.WHITE_BISHOP,Chess.WHITE_ROOK,Chess.WHITE_QUEEN,Chess.WHITE_KING,Chess.BLACK_PAWN,Chess.BLACK_KNIGHT,Chess.BLACK_BISHOP,Chess.BLACK_ROOK,Chess.BLACK_QUEEN,Chess.BLACK_KING};
		int indexCaseVrai = Partie.conversionCase(indexCase);
		if (partie.getPositionPieces().getStone(indexCaseVrai) == conversion[typePieceChoisie])
			partie.getPositionPieces().setStone(indexCaseVrai, 0);
		else partie.getPositionPieces().setStone(indexCaseVrai, conversion[typePieceChoisie]);
		board.majBoard();
	}

	@Override
	public void pieceChoisie(int type) {
		// TODO Auto-generated method stub
		typePieceChoisie = type;
	}

	@Override
	public void onActionBarItemClicked(int position) {
		// TODO Auto-generated method stub
		if (position == -1) finish(); //home
		else if (position == 0) {
			try {
				partie.importerProbleme(problem);
			}
			catch (Exception e) {
				Log.e(ChessDiags.NOMLOG,"",e);
			}
		}
		else if (position == 1) {
			if (partie.getPositionPieces().isLegal())
				dialogSauvegarder();
			else Toast.makeText(this, R.string.positioninvalide, Toast.LENGTH_LONG).show();
		}
		else if (position == 2) {
			if (!problem.isSauvegarde()) Toast.makeText(this, R.string.youmustsaveproblemfirst, Toast.LENGTH_LONG).show();
			else {
				sauvegarderProbleme();
				dialogShare();
			}
		}
	}

	public void dialogShare() {
		int nb = 0;
		ArrayList<CharSequence> listeString = new ArrayList<CharSequence>();
		final ArrayList<Source> sources = new ArrayList<Source>();
		for (Source source : ListeSources.getListe()) {
			if (source.getId() > 1) {
				listeString.add(source.getName());
				sources.add(source);
				nb++;
			}
		}
		if (nb == 0) {
			Toast.makeText(this, R.string.norepositoryfound, Toast.LENGTH_LONG);
			return;
		}
		final CharSequence[] items = listeString.toArray(new CharSequence[]{});
		final boolean[] checked = new boolean[nb];
		for (int i = 0; i < checked.length; i++) {
			checked[i] = sources.get(i).isUploadSupported();
		}
		new AlertDialog.Builder( this )
		.setTitle(R.string.uploadto)
		.setMultiChoiceItems(items,checked,new OnMultiChoiceClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				// TODO Auto-generated method stub
				checked[which] = isChecked;
			}
		})
		.setPositiveButton(R.string.ok, new OnClickListener() {


			@Override
			public void onClick(DialogInterface dialog2, int which) {
				// TODO Auto-generated method stub

				int i = -1;
				boolean zeroSources = true;
				for (boolean b : checked) {
					if (b) zeroSources = false;
				}
				if (zeroSources) {
					Log.i(ChessDiags.NOMLOG,"No source choosen");
					return;
				}
				MultiRequete multiRequete = new MultiRequete();
				for (boolean b : checked) {
					i++;
					if (!b) continue;
					Source source = sources.get(i);
					Log.i(ChessDiags.NOMLOG,"Upload to : "+source.getUrl()+" ("+source.getId()+")");
					multiRequete.addRequete(new RequeteUpload(problem,source));
				}
				multiRequete.executer();
				showDialog(DIALOG_PROGRESS);
			}
		})
		.show();
	}

	@Override
	public void positionChangee() {
		// TODO Auto-generated method stub
		board.invalidate();
	}


	public void sauvegarderProbleme() {
		partie.checkCastles(); //make sure static castles analysis is taken into account
		problem.setPosition(partie.getPosition());
		problem.sauvegarder();
		checkShare();
		refreshTitle();
		Toast.makeText(this, R.string.problemsaved, Toast.LENGTH_SHORT).show();
	}

	public void dialogSauvegarder() {
		LayoutInflater inflater=LayoutInflater.from(this);
		final View addView=inflater.inflate(R.layout.dialogsauvegarder, null);
		new AlertDialog.Builder(this)
		.setTitle(R.string.savecreation)
		.setView(addView)
		.setPositiveButton(R.string.save,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int whichButton) {
				CheckBox box = (CheckBox) addView.findViewById(R.id.trait);
				boolean trait = box.isChecked();
				partie.setTrait(trait);
				problem.setPosition(partie.getFEN());
				TextView text = (TextView) addView.findViewById(R.id.description);
				problem.setDescription(text.getText().toString());
				TextView nom = (TextView) addView.findViewById(R.id.nom);
				problem.setNom(nom.getText().toString());
				final EditText nbMoves = (EditText) addView.findViewById(R.id.nbCoups);
				try {
					problem.setNbMoves(Integer.parseInt(nbMoves.getText().toString()));
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				sauvegarderProbleme();
			}
		})
		.setNegativeButton(R.string.cancel,null)
		.show();
		Button boutonPlus = (Button) addView.findViewById(R.id.boutonplus);
		final EditText nbMoves = (EditText) addView.findViewById(R.id.nbCoups);
		Button boutonMoins = (Button) addView.findViewById(R.id.boutonmoins);
		boutonPlus.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int i = 1;
				try {
					i = Integer.parseInt(nbMoves.getText().toString());
					if (i>=999) return;
				}
				catch (Exception e) {

				}
				nbMoves.setText((i+1)+"");
			}
		});
		boutonMoins.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int i = 1;
				try {
					i = Integer.parseInt(nbMoves.getText().toString());
					if (i <= 1) return;
				}
				catch (Exception e) {

				}
				nbMoves.setText((i-1)+"");
			}
		});
		nbMoves.setText(""+problem.getNbMoves());
		CheckBox box = (CheckBox) addView.findViewById(R.id.trait);
		box.setChecked(partie.getTrait());
		TextView nom = (TextView) addView.findViewById(R.id.nom);
		nom.setText(problem.getNom());
		TextView description = (TextView) addView.findViewById(R.id.description);
		description.setText(problem.getDescription());

	}

	protected Dialog onCreateDialog(int id) {
	    Dialog dialog;
	    ChessProgressDialog dialog2;
	    switch(id) {
	    case DIALOG_PROGRESS:
			dialog2 = new ChessProgressDialog(this);
			dialog2.setTitle(R.string.sending);
			dialog = dialog2;
			break;
	    default:
	        dialog = null;
	    }
	    return dialog; 
	}
	
	

	@Override
	public void partieTerminee(int resultat) {
		// TODO Auto-generated method stub
		board.invalidate();
	}


}
