package donnees;

import java.util.ArrayList;
import java.util.Collection;

import com.estragon.sql.DAO;

import core.Problem;

public class ListeProblemes extends ArrayList<Problem>  {
	
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
		Problem problem = null;
		for (Problem p : this) {
			if (sens && p.getSource() == source && p.getId() > id) return p;
			else if (!sens && p.getSource() == source && p.getId() == id) return problem;
			else if (p.getSource() == source) problem = p;
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
		return liste;
	}
	
	public static void charger() {
		LISTE = DAO.loadProblemes();
	}
}
