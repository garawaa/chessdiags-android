package core;
public class Coup {
	public int depart;
	public int arrivee;
	
	public Coup(int depart,int arrivee) {
		this.depart = depart;
		this.arrivee = arrivee;
	}
	
	public String toString() {
		return depart+"/"+arrivee;
	}
}
