package util;

public class Format {

	public static int fenToPiece(char lettre) {
		if (lettre == 'P') return (1);
		if (lettre == 'N') return(2);
		if (lettre == 'B') return (3);
		if (lettre == 'R') return(4);
		if (lettre == 'Q') return(5);
		if (lettre == 'K') return(6);
		if (lettre == 'p') return (7);
		if (lettre == 'n') return(8);
		if (lettre == 'b') return (9);
		if (lettre == 'r') return(10);
		if (lettre == 'q') return(11);
		if (lettre == 'k') return(12);
		return 0;
	}
	public static String lettretochiffre(char lettre) {
		if (lettre == 'a') return("7");
		if (lettre == 'b') return("8");
		if (lettre == 'c') return("9");
		if (lettre == 'd') return("10");
		if (lettre == 'e') return("11");
		if (lettre == 'f') return("12");
		return(Character.toString(lettre));
	}
}
