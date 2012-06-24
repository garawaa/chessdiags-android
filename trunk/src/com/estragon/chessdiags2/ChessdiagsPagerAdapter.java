package com.estragon.chessdiags2;

import java.util.HashMap;
import java.util.Map;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;
import core.Source;
import donnees.ListeProblemes;
import donnees.ListeSources;

public class ChessdiagsPagerAdapter extends FragmentStatePagerAdapter {

	private Map<Integer, ListFragment> lists = new HashMap<Integer, ListFragment>();
	
    public ChessdiagsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
    	Source source = ListeSources.getListe().get(position);
    	lists.put(position, new ProblemListFragment(source.getId()));
    	return lists.get(position);
    }

    @Override
	public CharSequence getPageTitle(int position) {
		// TODO Auto-generated method stub
    	Source source = ListeSources.getListe().get(position);
    	int[] nb = ListeProblemes.getListe().getNbProblemes(source.getId());
    	return " "+ListeSources.getListe().get(position).getName()+" ("+nb[1]+"/"+nb[0]+") ";
	}

    
    
	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		for (ListFragment fragment : lists.values()) {
			ArrayAdapter adapter = ((ArrayAdapter) fragment.getListAdapter());
			adapter.notifyDataSetChanged();
		}
		super.notifyDataSetChanged();
	}

	@Override
    public int getCount() {
        return ListeSources.getListe().size();
    }
	
	
}
