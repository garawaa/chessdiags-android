package com.estragon.chessdiags2;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import core.History;
import core.Problem;
import donnees.ListeHistory;
import donnees.ListeProblemes;

public class HistoryAdapter extends ArrayAdapter<History>  {

	public HistoryAdapter(Context context) {
		super(context, android.R.layout.simple_list_item_1);
		charger();
	}
	
	public void charger() {
		Log.i("Chessdiags", "Mise à jour de l'historique");
		setNotifyOnChange(false);
		clear();
		ListeHistory liste = ListeHistory.getListe();
		for (History history : liste) {
			add(history);
		}
	}

}
