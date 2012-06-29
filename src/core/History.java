package core;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "history")
public class History {

	@DatabaseField(index = true,id=true) 
	public int problemInternalId;
	
	@DatabaseField
	public Date date;
	
	public History() {
		
	}

	public int getProblemInternalId() {
		return problemInternalId;
	}

	public void setProblemInternalId(int problemInternalId) {
		this.problemInternalId = problemInternalId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public String toString() {
		return "STUB : "+problemInternalId+"/"+date;
	}
	
	
}
