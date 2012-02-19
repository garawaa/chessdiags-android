package com.estragon.sockets;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONObject;

import android.content.Context;

public class MultiRequete {

	static MultiRequete requeteActive = null;
	ArrayList<Requete> requetes = new ArrayList<Requete>();
	
	public static MultiRequete getActive() {
		return requeteActive;
	}
	
	public MultiRequete() {
		
	}
	
	public void addRequete(Requete requete) {
		requetes.add(requete);
	}
	
	public void executer() {
		requeteActive = this;
		for (Requete requete : requetes) {
			requete.executer();
		}
	}
	
	public int getNbRequetes() {
		return requetes.size();
	}
	
	public int getAvancement() {
		int avancement = 0;
		for (Requete requete : requetes) {
			if (requete.getStatut() == Requete.ERREUR || requete.getStatut() == Requete.TRAITEMENT_TERMINE) avancement++;
		}
		return avancement;
	}
	
	public Iterator<Requete> getIterator() {
		return requetes.iterator();
	}
}
