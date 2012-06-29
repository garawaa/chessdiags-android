package com.estragon.chessdiags2;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

public class HistoryListFragment extends ListFragment {

	HistoryAdapter adapter;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		getListView().setFastScrollEnabled(true);
		setListAdapter(adapter = new HistoryAdapter(getActivity()));
	}

	
}
