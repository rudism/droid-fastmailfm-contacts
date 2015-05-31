package com.rudism.fastmail.fm.contacts;

public class FastmailContact {

	private final int _id;
	private final String _displayName;
	private final String _companyName;
	private final String _email;
	private final String _emailAlt;
	private final String _phoneHome;
	private final String _phoneMobile;
	private final String _phoneWork;
	private final String _notes;
	
	public FastmailContact(int id, String displayName, String companyName, String email, String emailAlt, String phoneHome, String phoneWork, String phoneMobile, String notes){
		_id = id;
		_displayName = displayName;
		_companyName = companyName;
		_email = email;
		_emailAlt = emailAlt;
		_phoneHome = phoneHome;
		_phoneMobile = phoneMobile;
		_phoneWork = phoneWork;
		_notes = notes;
	}
	
	public int getId(){
		return _id;
	}
	
	public String getDisplayName(){
		return _displayName;
	}
	
	public String getCompanyName(){
		return _companyName;
	}
	
	public String getEmail(){
		return _email;
	}
	
	public String getEmailAlt(){
		return _emailAlt;
	}
	
	public String getPhoneHome(){
		return _phoneHome;
	}
	
	public String getPhoneMobile(){
		return _phoneMobile;
	}
	
	public String getPhoneWork(){
		return _phoneWork;
	}
	
	public String getNotes(){
		return _notes;
	}
}
