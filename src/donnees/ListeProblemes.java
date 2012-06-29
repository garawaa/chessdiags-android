package donnees;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import android.preference.PreferenceManager;
import android.util.Log;

import com.estragon.chessdiags2.Appli;
import com.estragon.sql.DAO;

import core.Problem;
import donnees.comparateurs.Trieur;
import donnees.comparateurs.TrieurParNbMoves;

public class ListeProblemes extends ArrayList<Problem> {
	
	static ListeProblemes LISTE;
	
	public synchronized static ListeProblemes getListe() {
		if (LISTE == null) LISTE = DAO.loadProblemes();
		return LISTE;
	}
	
	public ListeProblemes() {
		super();
	}
	
	public ListeProblemes(Collection<Problem> problemes) {
		super(problemes);
	}
	
	public Problem getProblem(int id,int source) {
		for (Problem p : this) {
			if (p.getId() == id && p.getSource() == source) return p;
		}
		return null;
	}
	
	public int[] getNbProblemes(int id) {
		int[] result = new int[2];
		for (Problem p : this) {
			if (p.getSource() == id) {
				if (p.isResolu()) result[1]++;
				result[0]++;
			}
		}
		return result;
	}
	
	public int getMaxId(int source) {
		int max = 1;
		for (Problem p : this) {
			if (p.getSource() == source) max = Math.max(max, p.getId() + 1);
		}
		return max;
	}
	
	public Problem getNextProblem(int source, int id,boolean sens) {
		ListeProblemes liste = getProblemesFromSource(source,id);
		Problem previous = null;
		for (Problem p : liste) {
			if (p.getId() == id) {
				if (!sens) return previous;
			}
			if (sens && previous != null && previous.getId() == id) {
				return p;
			}
			previous = p;
		}
		return null;
	}
	
	public Problem getRandomProblem() {
		int random = (int) (Math.random() * this.size());
		return get(random);
	}
	
	public static ListeProblemes getProblemesFromSource(int idSource) {
		return getProblemesFromSource(idSource, -1);
	}
	
	public static ListeProblemes getProblemesFromSource(int idSource,int idAForcer) {
		boolean hideSolved = PreferenceManager.getDefaultSharedPreferences(Appli.getInstance()).getBoolean("hidesolved", false);
		ListeProblemes liste = new ListeProblemes();
		for (Problem problem : getListe()) {
			if (problem.getSource() == idSource) {
				if (!(hideSolved && problem.isResolu()) || problem.getId() == idAForcer) 
					liste.add(problem);
			}
		}
		int trieur = Trieur.TRI_NBMOVES;
		try {
			trieur = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(Appli.getInstance()).getString("trieur", ""+Trieur.TRI_NBMOVES));
		}
		catch (NumberFormatException e) {
			Log.e("Chessdiags","Erreur de parsing du trieur",e);
		}
		if (trieur == Trieur.TRI_NBMOVES) Collections.sort(liste,new TrieurParNbMoves());
		return liste;
	}
	
	
	public static void charger() {
		LISTE = DAO.loadProblemes();
	}
	
	
}
