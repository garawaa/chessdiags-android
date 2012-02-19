package donnees;

import java.util.ArrayList;
import java.util.Collection;

import com.estragon.sql.DAO;

import core.Problem;
import core.Source;

public class ListeSources extends ArrayList<Source> {

	static ListeSources LISTE;
	public static boolean hasChanged = false;

	@Override
	public Source remove(int index) {
		// TODO Auto-generated method stub
		hasChanged = true;
		return super.remove(index);
	}

	@Override
	public boolean remove(Object object) {
		// TODO Auto-generated method stub
		hasChanged = true;
		return super.remove(object);
	}

	public synchronized static ListeSources getListe() {
		if (LISTE == null) {
			LISTE = DAO.loadSources();
		}
		return LISTE;
	}
	
	public ListeSources() {
		super();
	}
	
	public ListeSources(Collection<Source> sources) {
		super(sources);
	}
	
	public Source getSourceById(int id) {
		for (Source source : this) {
			if (source.getId() == id) return source;
		}
		return null;
	}
	
	public static void charger() {
		LISTE = DAO.loadSources();
	}
}
