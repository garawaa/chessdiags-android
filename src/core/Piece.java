package core;

public class Piece {
	int type;
	
	public Piece() {
		this.setVide();
	}
	
	public Piece(int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
	
	public boolean isVide() {
		return type == 0;
	}
	
	public void setVide() {
		this.setType(0);
	}
	
	public void setType(int type) {
		this.type = type;
	}

	
	public char getFEN() {
		char[] fen = new char[] {'x','P','N','B','R','Q','K','p','n','b','r','q','k'};
		try {
			return fen[this.getType()];
		}
		catch (Exception e) {
			return 'x';
		}
	}
	
}
