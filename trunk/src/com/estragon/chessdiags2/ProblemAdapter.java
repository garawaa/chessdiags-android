package com.estragon.chessdiags2;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import core.Problem;
import donnees.ListeProblemes;



public class ProblemAdapter extends ArrayAdapter<Problem> {

	int idSource;
	
	public ProblemAdapter(Context context,int idSource) {
		super(context, android.R.layout.simple_list_item_1);
		this.idSource = idSource;
		charger();
	}

	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		charger();
		super.notifyDataSetChanged();
	}
	
	private void charger() {
		setNotifyOnChange(false);
		clear();
		addAll(ListeProblemes.getProblemesFromSource(idSource));
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//TODO : optimiser avec convertView
		Problem problem = getItem(position);
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.problemitem, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		TextView subtitle = (TextView) rowView.findViewById(R.id.subtitle);
		textView.setText(getItem(position).getNom());
		if (problem.isResolu()) imageView.setImageResource(android.R.drawable.star_big_on);
		else imageView.setImageResource(android.R.drawable.star_big_off);
		subtitle.setText("Mate in "+problem.getNbMoves());
		return rowView;
	}
	
/*
	Source source;
	boolean empty = false;

	public ProblemAdapter(Context context, int idSource) {
		super(context);
		source = ListeSources.getListe().getSourceById(idSource);
	}

	public ProblemAdapter(Context context, Source source) {
		super(context);
		this.source = source;
	}
	
	public Source getSource() {
		return source;
	}

	public void charger() {
		this.clear();
		charger(source);
	}

	public void charger(Source source) {
		this.setNotifyOnChange(false);
		this.clear();
		this.ajouterSource(source);
		empty = true;
		for (Problem p : ListeProblemes.getListe()) {
			if (p.getSource() == source.getId()) {
				this.ajouterProbleme(p);
				empty = false;
			}
		}
		
		if (empty && source.getId() == 1) add(new LongTextItem(Appli.getInstance().getString(R.string.emptycreationsmessage)));
		super.notifyDataSetChanged();
	}

	public void ajouterSource(Source source) {
		int[] nbProblemes = ListeProblemes.getListe().getNbProblemes(source.getId());
		this.add(new SeparatorItem(source.getName()+" ("+nbProblemes[1]+"/"+nbProblemes[0]+")"));
	}

	public void insererSource(Source source) {
		int[] nbProblemes = ListeProblemes.getListe().getNbProblemes(source.getId());
		this.insert(new SeparatorItem(source.getName()+" ("+nbProblemes[1]+"/"+nbProblemes[0]+")"),0);
	}

	public void ajouterProbleme(Problem p) {
		this.add(new ProblemItem(p));
	}

	public void chargerSeparateurs(int idDebut,int idFin) {
		if (idDebut == idFin) return;
		for (Source source : ListeSources.getListe()) {
			if (source.getId() <= idDebut) continue;
			if (source.getId() > idFin) return;
			this.ajouterSource(source);
		}
	}

	public class ProblemItem extends ThumbnailItem {

		Problem problem;

		public ProblemItem(Problem p) {
			this.problem = p;
			init();
		}

		public void init() {
			if (problem.isResolu()) this.drawableId = android.R.drawable.btn_star_big_on;
			else this.drawableId = android.R.drawable.btn_star_big_off;
			String text = "";
			//text = "["+p.getId()+"] ";
			if (problem.getNom() != null) text +=  problem.getNom()+" ";
			this.text = Character.toUpperCase(text.charAt(0))+text.substring(1);
			String sousTitre = getContext().getString(R.string.matein,problem.getNbMoves());
			this.subtitle = sousTitre;
			
		}

		@Override
		public ItemView newView(Context context, ViewGroup parent) {
			// TODO Auto-generated method stub
			init();
			return super.newView(context, parent);
		}



		public Problem getProblem() {
			return problem;
		}
	}

	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		this.remove((Item) getItem(0));
		this.insererSource(source);
		int nbProblemes = ListeProblemes.getListe().getNbProblemes(source.getId())[0];
		if (nbProblemes != (this.getCount() - 1) || (nbProblemes == 1 && empty)) {
			charger();
		}
		super.notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetInvalidated() {
		// TODO Auto-generated method stub
		super.notifyDataSetInvalidated();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) return super.getView(position, convertView, parent);
		Item item = (Item) getItem(position);
		ItemView view = item.newView(parent.getContext(), parent);
		view.prepareItemView();
		view.setObject(item);
		return (View) view;
	}
*/


}
