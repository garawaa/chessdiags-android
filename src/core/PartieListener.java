package core;


public interface PartieListener {
	public static int WIN = 1, DRAW = 2, LOSE = 3, MOVE_NUMBER_EXCEDED = 4, UNKNOWN = 5;
	public void positionChangee();
	public void partieTerminee(int resultat);
}
