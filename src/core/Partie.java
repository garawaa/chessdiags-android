package core;

import java.util.ArrayList;
import java.util.StringTokenizer;

import android.util.Log;
import chesspresso.Chess;
import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;
import chesspresso.position.FEN;
import chesspresso.position.Position;

import com.estragon.chessdiags2.ChessDiags;
import com.estragon.chessdiags2.Coup;
import com.estragon.chessdiags2.TestActivity;
import com.estragon.engine.AnalyseListener;
import com.estragon.engine.Engine;
import com.estragon.engine.Engine.EngineNotReadyException;

public class Partie implements  AnalyseListener {
	Position position = null;
	ArrayList<PartieListener> listeners = new ArrayList<PartieListener>();
	int couleurDefendue = Chess.WHITE;
	int nbMovesLeft = 2;
	int over;

	public Partie() {
		try {
			registerListener();
		}
		catch (Exception e) {
			Log.w(ChessDiags.NOMLOG,"Warning, engine listener registration failed");
		}
		try {
			importerPosition(FEN.START_POSITION);
		}
		catch (Exception e) {
			Log.e(ChessDiags.NOMLOG,"This should never happen !",e);
			System.exit(0);
		}
	}

	public Partie(Problem problem) throws ParsingException, EngineNotReadyException {
		registerListener();
		importerProbleme(problem);
	}

	public void registerListener() throws EngineNotReadyException {
		Engine.getEngine().setAnalyseListener(this);
	}
	
	
	public void setCouleurDefendue(int couleurDefendue) {
		this.couleurDefendue = couleurDefendue;
	}

	public boolean proposerCoup(int depart,int arrivee) {
		if (!isToPlay()) {
			Log.i(ChessDiags.NOMLOG,"It's not player's turn");
			return false;
		}
		if (isOver()) {
			Log.i(ChessDiags.NOMLOG,"Game is already over");
			return false;
		}
		short m = isLegal(depart, arrivee);
		if (m == 0) {
			Log.i(ChessDiags.NOMLOG,"Move is illegal");
			return false;
		}
		jouerCoup(m);
		if (!isOver()) this.analyse();
		return true;
	}

	public boolean getTrait() {
		return position.getToPlay() == Chess.WHITE;
	}

	public void jouerCoup(Coup c) {
		short m = isLegal(c.depart, c.arrivee);
		if (m == 0) {
			Log.i(ChessDiags.NOMLOG,"Invalid move : "+c.depart+"/"+c.arrivee);
		}
		else jouerCoup(m);
	}

	public void unCoupDeMoins() {
		nbMovesLeft = Math.max(nbMovesLeft - 1, 0);
	}

	private void jouerCoup(short m) {
		try {
			position.doMove(m);
			if (!isToPlay()) unCoupDeMoins();
			positionChangee();
		}
		catch (IllegalMoveException e) {
			//Should never happen
			e.printStackTrace();
		}

		if (position.isTerminal()) {
			if (!isToPlay() && position.isMate()) partieTerminee(PartieListener.WIN);
			else if (isToPlay()) partieTerminee(PartieListener.LOSE);
		}
		else {
			if (isToPlay() && nbMovesLeft <= 0) partieTerminee(PartieListener.MOVE_NUMBER_EXCEDED);
		}
	}

	public int getNbMovesLeft() {
		return nbMovesLeft;
	}

	public void setNbMovesLeft(int nbMovesLeft) {
		this.nbMovesLeft = nbMovesLeft;
	}

	public boolean isToPlay() {
		return position.getToPlay() == couleurDefendue;
	}

	public void broadcastResultat(int resultat) {
		for (PartieListener l : listeners) {
			l.partieTerminee(resultat);
		}
	}

	public void positionChangee() {
		for (PartieListener l : listeners) {
			l.positionChangee();
		}
	}

	public void analyse() {
		try {
			Engine.getEngine().setPosition(this.getPosition());
			Engine.getEngine().analyse();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addPartieListener(PartieListener l) {
		listeners.add(l);
	}

	public boolean removePartieListener(PartieListener l) {
		return listeners.remove(l);
	}

	public void importerProbleme(Problem probleme) throws ParsingException {
		importerPosition(probleme.getPosition());
		setNbMovesLeft(probleme.getNbMoves());
		over = 0;
	}

	public int getRoques() {
		return position.getCastles();
	}

	public int getCoups() {
		return position.getPlyNumber();
	}

	public void setCoups(int coups) {
		position.setPlyNumber(coups);
	}

	public void setTrait(boolean trait) {
		if (trait) position.setToPlay(Chess.WHITE);
		else position.setToPlay(Chess.BLACK);
	}

	public void importerPosition(String positionOld) throws ParsingException {
		String position = positionOld.trim();
		StringTokenizer tokenizer = new StringTokenizer(position, " ");
		int nbChamps = tokenizer.countTokens();
		if (nbChamps == 1) position += " w KQkq - 0 0";
		else if (nbChamps == 2) position += " KQkq - 0 0";
		else if (nbChamps == 3) position += " - 0 0";
		else if (nbChamps == 4) position += " 0 0";
		else if (nbChamps == 5) position += " 0";
		try {
			this.position = new Position(position,false);
			checkCastles();
			couleurDefendue = this.position.getToPlay();
			this.positionChangee();
		}
		catch (Exception e) {
			Log.e(ChessDiags.NOMLOG,"Parsing error : "+position,e);
			throw new ParsingException();
		}
	}
	
	public class ParsingException extends Exception {
		
	}
	
	public void checkCastles() {
		this.position.setCastles(mixCastles(this.position.getCastles(),staticCastlesAnalysis(this.position)));
	}

	private static int mixCastles(int castle1,int castle2) {
		int castles = 0;
		if ((castle1 & Position.WHITE_SHORT_CASTLE) * (castle2 & Position.WHITE_SHORT_CASTLE) != 0) castles +=  Position.WHITE_SHORT_CASTLE;
		if ((castle1 & Position.WHITE_LONG_CASTLE) * (castle2 & Position.WHITE_LONG_CASTLE) != 0) castles +=  Position.WHITE_LONG_CASTLE;
		if ((castle1 & Position.BLACK_SHORT_CASTLE) * (castle2 & Position.BLACK_SHORT_CASTLE) != 0) castles += Position.BLACK_SHORT_CASTLE;
		if ((castle1 & Position.BLACK_LONG_CASTLE) * (castle2 & Position.BLACK_LONG_CASTLE) != 0) castles +=  Position.BLACK_LONG_CASTLE;
		return castles;
	}
	
	public String getPosition() {
		return this.getFEN();
	}

	public Position getPositionPieces() {
		return position;
	}

	public String getFEN() {
		return position.getFEN();
	}

	@Override
	public void bestMoveFound(Coup c) {
		// TODO Auto-generated method stub
		this.jouerCoup(c);
	}

	public void partieTerminee(int resultat) {
		// TODO Auto-generated method stub
		over = resultat;
		this.broadcastResultat(resultat);
	}

	public boolean isOver() {
		return over != 0;
	}

	/**
	 * Verifie si un coup est légal
	 * @param depart
	 * @param arrivee
	 * @return
	 */
	public short isLegal(int depart,int arrivee) {
		try {
			short[] coups = position.getAllMoves();
			short m = position.getMove(conversionCase(depart),conversionCase(arrivee),0);
			short m2 = Move.getEPMove(conversionCase(depart),conversionCase(arrivee));
			short m3 = position.getMove(conversionCase(depart),conversionCase(arrivee),Chess.QUEEN);
			for (short coup : coups) {
				if (m == coup) return m;
				if (m2 == coup) return m2;
				if (m3 == coup) return m3;
			}

			return 0;
		}
		catch (Exception e) {

			return 0;
		}
	}

	public static int conversionCase(int indexCase) {
		int oldY = (int) (indexCase / 8);
		int newY = 7 - oldY;
		int oldX = indexCase % 8;
		return newY * 8 + oldX;
	}
	
	public static int staticCastlesAnalysis(Position pos) {
		int roque1 = Position.WHITE_SHORT_CASTLE;
		int roque2 = Position.WHITE_LONG_CASTLE;
		int roque3 = Position.BLACK_SHORT_CASTLE;
		int roque4 = Position.BLACK_LONG_CASTLE;
		if (pos.getStone(Chess.A1) != Chess.WHITE_ROOK) roque2 = 0;
		if (pos.getStone(Chess.H1) != Chess.WHITE_ROOK) roque1 = 0;
		if (pos.getStone(Chess.A8) != Chess.BLACK_ROOK) roque4 = 0;
		if (pos.getStone(Chess.H8) != Chess.BLACK_ROOK) roque3 = 0;
		if (pos.getStone(Chess.E1) != Chess.WHITE_KING) {
			roque1 = 0;
			roque2 = 0;
		}
		if (pos.getStone(Chess.E8) != Chess.BLACK_KING) {
			roque3 = 0;
			roque4 = 0;
		}
		return roque1 + roque2 + roque3 + roque4;
	}
	
	public static int staticCastlesAnalysis(String fen) {
		return staticCastlesAnalysis(new Position(fen));
	}
	
	public int getCouleurDefendue() {
		return couleurDefendue;
	}


}
