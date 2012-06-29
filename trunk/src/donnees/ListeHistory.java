package donnees;

import java.util.ArrayList;

import android.util.Log;

import com.estragon.sql.DAO;

import core.History;

public class ListeHistory extends ArrayList<History> {

	public static ListeHistory getListe() {
		ListeHistory liste = new ListeHistory();
		try {
			liste.addAll(DAO.getHistoryDao().queryForAll());
		}
		catch (Exception e) {
			Log.e("Chessdiags","",e);
		}
		return liste;
	}

}
