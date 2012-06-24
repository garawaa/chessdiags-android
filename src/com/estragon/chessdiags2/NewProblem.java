package com.estragon.chessdiags2;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import widgets.Board;
import widgets.Board.BoardListener;
import widgets.Pieces;
import widgets.Pieces.PiecesListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import chesspresso.Chess;
import chesspresso.position.Position;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.estragon.sockets.MultiRequete;
import com.estragon.sockets.RequeteUpload;

import core.Partie;
import core.PartieListener;
import core.Problem;
import core.Source;
import donnees.ListeProblemes;
import donnees.ListeSources;

public class NewProblem extends SherlockActivity implements BoardListener, PiecesListener,  PartieListener {

	Partie partie = new Partie();
	Board board;
	Pieces pieces;
	int typePieceChoisie = 0;
	Problem problem = null;
	int sharingOption = SHARE_REPOSITORIES;

	public static final int DIALOG_PROGRESS = 1, DIALOG_CHOOSE_SHARE = 2, DIALOG_SHARE = 3;
	public static final int SHARE_REPOSITORIES = 0, SHARE_INTENT = 1;
	
	MenuItem refresh;
	MenuItem save;
	MenuItem share;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.newproblem);

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
			if (id != -1 && source != -1) {
				problem = ListeProblemes.getListe().getProblem(id, source);
				problem.setSauvegarde(true);
			}
			else if (position != null) {
				try {
					partie.importerPosition(position);
				}
				catch (Exception e) {
					Log.e("Chessdiags : ","",e);
				}
			}
			if (problem != null) {
				try {
					partie.importerProbleme(problem);
				}
				catch (Exception e) {
					Log.e("Chessdiags : ","",e);
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

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		refreshTitle();
	}

	
	
	
	public void refreshTitle() {
		this.setTitle(problem.getNom());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		save = menu.add(R.string.edit);
        save.setIcon(R.drawable.gd_action_bar_edit)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        refresh = menu.add(R.string.redraw);
        refresh.setIcon(R.drawable.gd_action_bar_refresh)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        share = menu.add(R.string.share);
        share.setIcon(R.drawable.gd_action_bar_share)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        checkShare();
		return super.onCreateOptionsMenu(menu);
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
		if (share == null) return;
		share.setVisible(problem.isSauvegarde());
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
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == android.R.id.home) finish(); //home
		else if (item == refresh) {
			try {
				partie.importerProbleme(problem);
			}
			catch (Exception e) {
				Log.e("Chessdiags","",e);
			}
		}
		else if (item == save) {
			if (partie.getPositionPieces().isLegal())
				dialogSauvegarder();
			else Toast.makeText(this, R.string.positioninvalide, Toast.LENGTH_LONG).show();
		}
		else if (item == share) {
			if (!problem.isSauvegarde()) Toast.makeText(this, R.string.youmustsaveproblemfirst, Toast.LENGTH_LONG).show();
			else {
				sauvegarderProbleme();
				showDialog(DIALOG_CHOOSE_SHARE);
			}
		}
		return super.onOptionsItemSelected(item);
	}




	public void dialogShare() {
		showDialog(DIALOG_SHARE);
		/*int nb = 0;
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
		.show();*/
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
				CheckBox box = (CheckBox) addView.findViewById(R.id.traitdialogsauvegarder);
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
		CheckBox box = (CheckBox) addView.findViewById(R.id.traitdialogsauvegarder);
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
	    case DIALOG_CHOOSE_SHARE:
	    	dialog = new AlertDialog.Builder(this).setTitle("Choose a sharing option").setPositiveButton(android.R.string.ok, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
					if (sharingOption == SHARE_INTENT) {
						intentShare(NewProblem.this,NewProblem.this.problem);
					}
					else {
						dialogShare();
					}
				}
			}).setNegativeButton(android.R.string.cancel, null).setSingleChoiceItems( R.array.shareoptions, 0, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					sharingOption = which;
				}
			}).create();
	    	break;
	    case DIALOG_SHARE:
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
				return null;
			}
			final CharSequence[] items = listeString.toArray(new CharSequence[]{});
			final boolean[] checked = new boolean[nb];
			for (int i = 0; i < checked.length; i++) {
				checked[i] = sources.get(i).isUploadSupported();
			}
			dialog = new AlertDialog.Builder( this )
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
						Log.i("Chessdiags","No source choosen");
						return;
					}
					MultiRequete multiRequete = new MultiRequete();
					for (boolean b : checked) {
						i++;
						if (!b) continue;
						Source source = sources.get(i);
						Log.i("Chessdiags","Upload to : "+source.getUrl()+" ("+source.getId()+")");
						multiRequete.addRequete(new RequeteUpload(problem,source));
					}
					multiRequete.executer();
					showDialog(DIALOG_PROGRESS);
				}
			})
			.create();
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
	
	public static void intentShare(Context context, Problem problem) {
		try {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			Position position = new Position(problem.getPosition());
			URI uri = new URI("http","www.chessdiags.com","/problem.php","fen="+position.getFEN().replace(" ", "_")+"&moves="+problem.getNbMoves(),null);
			intent.putExtra(Intent.EXTRA_TEXT, String.format(context.getString(R.string.sharemessage), position.getToPlay() == Chess.WHITE ? context.getString(R.string.white) : context.getString(R.string.black), problem.getNbMoves(),uri.toString()));
			intent.putExtra(Intent.EXTRA_TITLE, R.string.sharetitle);
			Intent intentChoisi = Intent.createChooser(intent, context.getString(R.string.sharethisproblem));
			if (intentChoisi != null) context.startActivity(intentChoisi);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		catch (ActivityNotFoundException e) {
			Toast.makeText(context, R.string.noactivitytohandleintent, Toast.LENGTH_LONG).show();
		}
	}


}
