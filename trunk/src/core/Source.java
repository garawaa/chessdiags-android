package core;

import com.j256.ormlite.field.DatabaseField;

public class Source {

	@DatabaseField(index = true, allowGeneratedIdInsert=true,generatedId=true)
	Integer id = null;
	@DatabaseField
	String name;
	@DatabaseField(uniqueIndex=true)
	String url;
	@DatabaseField
	boolean uploadSupported = true;
	
	public Source() {
		
	}
	
	public boolean isUploadSupported() {
		return uploadSupported;
	}

	public void setUploadSupported(boolean upload) {
		this.uploadSupported = upload;
	}
	
	public Source(int id,String name,String url, boolean uploadSupported) {
		this.id = id;
		this.name = name;
		this.url = url;
		this.uploadSupported = uploadSupported;
	}
	
	public Source(String name,String url,boolean uploadSupported) {
		this.name = name;
		this.url = url;
		this.uploadSupported = uploadSupported;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	
}
