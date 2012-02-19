package core;

import org.json.JSONException;
import org.json.JSONObject;

import android.widget.Toast;

import com.estragon.chessdiags2.Appli;
import com.estragon.chessdiags2.R;
import com.estragon.sql.DAO;
import com.j256.ormlite.field.DatabaseField;

import donnees.ListeProblemes;

public class Problem {

	
	@DatabaseField
	String position;
	@DatabaseField
	String nom = "";

	@DatabaseField
	String description = "";
	@DatabaseField
	int id = -1;
	@DatabaseField
	int source = 1;
	@DatabaseField
	int nbMoves = 2;
	@DatabaseField
	boolean resolu;
	@DatabaseField(allowGeneratedIdInsert=true,index = true,generatedId=true) 
	int internalId;
	
	boolean sauvegarde = false;

	public Problem(int id,int source,String position,int difficulty,boolean resolu) {
		this(id,source,position,difficulty,resolu,"","",2);
	}
	
	public Problem() {
		
	}
	
	public Problem(int id,int source,String position,int difficulty,boolean resolu,String nom,String description, int nbMoves) {
		this.id = id;
		this.source = source;
		this.position = position;
		this.resolu = resolu;
		setNom(nom);
		setDescription(description);
		setNbMoves(nbMoves);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		if (description == null) description = "";
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


	public int getNbMoves() {
		return nbMoves;
	}

	public void setNbMoves(int nbMoves) {
		this.nbMoves = nbMoves;
	}
	

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public boolean isResolu() {
		return resolu;
	}

	public void setResolu(boolean resolu) {
		this.resolu = resolu;
	}
	
	public Problem(JSONObject json) throws JSONException {
		this.id = json.getInt("id");
		this.position = json.getString("position");
		setDescription(json.getString("description"));
		setNom(json.getString("name"));
		setNbMoves(json.getInt("moves"));
	}
	
	public JSONObject getJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("position", position);
			json.put("description", description);
			json.put("name", getNom(true));
			json.put("moves", getNbMoves());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}
	
	public void setSauvegarde(boolean sauvegarde) {
		this.sauvegarde = sauvegarde;
	}

	public void sauvegarder() {
		if (ListeProblemes.getListe().getProblem(id, source) == null) {
			ListeProblemes.getListe().add(this);
		}
		try {
			sauvegarde = true;
			DAO.getProblemDao().createOrUpdate(this);
		}
		catch (Exception e) {
			
		}
	}
	
	public boolean isEditable() {
		return getSource() == 1;
	}
	
	public boolean isDeletable() {
		return getSource() == 1;
	}
	
	public boolean isSauvegarde() {
		return sauvegarde;
	}
	
	public String getNom() {
		return getNom(false);
	}
	
	public String getNom(boolean real) {
		if ((nom == null || nom.trim().equals("")) && !real) return Appli.getInstance().getString(R.string.unamedproblem);
		else if (real && nom == null) return "";
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
		if (nom == null) nom = "";
	}
	
}
