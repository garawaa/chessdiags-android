package com.estragon.chessdiags2;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import core.Source;
import donnees.ListeProblemes;
import donnees.ListeSources;

public class ChessdiagsPagerAdapter extends FragmentPagerAdapter {

    public ChessdiagsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
    	Source source = ListeSources.getListe().get(position);
    	return new ProblemListFragment(source.getId());
    }

    @Override
	public CharSequence getPageTitle(int position) {
		// TODO Auto-generated method stub
    	Source source = ListeSources.getListe().get(position);
    	int[] nb = ListeProblemes.getListe().getNbProblemes(source.getId());
    	return " "+ListeSources.getListe().get(position).getName()+" ("+nb[1]+"/"+nb[0]+") ";
	}

	@Override
    public int getCount() {
        return ListeSources.getListe().size();
    }
	
	
}
