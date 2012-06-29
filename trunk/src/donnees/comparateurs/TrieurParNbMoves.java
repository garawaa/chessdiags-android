package donnees.comparateurs;

import core.Problem;

public class TrieurParNbMoves implements Trieur {

	@Override
	public int compare(Problem lhs, Problem rhs) {
		// TODO Auto-generated method stub
		if (lhs.getNbMoves() < rhs.getNbMoves())
			return -1;
		if (lhs.getNbMoves() == rhs.getNbMoves())
			return lhs.getNom().compareToIgnoreCase(rhs.getNom());
		else return 1;
	}
}
