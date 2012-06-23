package com.estragon.chessdiags2;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import core.Problem;
import donnees.ListeProblemes;

public class ProblemListFragment extends ListFragment implements OnItemLongClickListener {

	private int index = 0;
	private ListItemSelectedListener selectedListener;
	int idSource;
	ListAdapter adapter;

	public ProblemListFragment(int idSource) {
		this.idSource = idSource;
	}

	

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		index = position;
		Log.e("Click : ",""+position);
		notifySelected((Problem)adapter.getItem(position));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setOnItemLongClickListener(this);
		getListView().setFastScrollEnabled(true);
		adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1,ListeProblemes.getProblemesFromSource(idSource));

		setListAdapter(adapter);
		
		if (savedInstanceState != null) {
			index = savedInstanceState.getInt("index", 0);
			notifySelected((Problem)adapter.getItem(index));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("index", index);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			selectedListener = (ListItemSelectedListener) activity;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}

	private void notifySelected(Problem probleme) {
		if (selectedListener != null) {
			selectedListener.onListItemSelected(probleme);
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		if (selectedListener != null) {
			selectedListener.onLongListItemSelected((Problem)adapter.getItem(arg2));
		}
		return true;
	}

	public interface ListItemSelectedListener {
		public void onListItemSelected(Problem problem);
		public void onLongListItemSelected(Problem problem);
	}



	
}
