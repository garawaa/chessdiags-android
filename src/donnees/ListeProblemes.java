package donnees;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.estragon.sql.DAO;

import core.Problem;

public class ListeProblemes extends ArrayList<Problem> implements Comparator<Problem> {
	
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
		ListeProblemes liste = getProblemesFromSource(source);
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
		ListeProblemes liste = new ListeProblemes();
		for (Problem problem : getListe()) {
			if (problem.getSource() == idSource) {
				liste.add(problem);
			}
		}
		Collections.sort(liste,liste);
		return liste;
	}
	
	@Override
	public int compare(Problem lhs, Problem rhs) {
		// TODO Auto-generated method stub
		if (lhs.getNbMoves() < rhs.getNbMoves())
			return -1;
		if (lhs.getNbMoves() == rhs.getNbMoves())
			return lhs.getNom().compareToIgnoreCase(rhs.getNom());
		else return 1;
	}
	
	public static void charger() {
		LISTE = DAO.loadProblemes();
	}
}
