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
	
	private ListItemSelectedListener selectedListener;
	int idSource;
	ProblemAdapter adapter;

	public ProblemListFragment(int idSource) {
		this();
		this.idSource = idSource;
	}
	
	public ProblemListFragment() {
		
	}

	

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.e("Click : ",""+position);
		notifySelected((Problem)adapter.getItem(position));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			idSource = savedInstanceState.getInt("idSource",1);
		}
		
		getListView().setOnItemLongClickListener(this);
		getListView().setFastScrollEnabled(true);
		if (idSource == 1) {
			setEmptyText(getString(R.string.emptycreationsmessage));
		}
		
		
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("idSource", idSource);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		adapter = new ProblemAdapter(activity, idSource);
		setListAdapter(adapter);
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

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		adapter.notifyDataSetChanged();
		super.onResume();
	}

	


	
}
