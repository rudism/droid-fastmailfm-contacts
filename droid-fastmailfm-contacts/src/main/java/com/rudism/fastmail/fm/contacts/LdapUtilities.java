package com.rudism.fastmail.fm.contacts;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.util.Log;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

public class LdapUtilities {

	public static Boolean authenticate(String email, String password){
		SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
		LDAPConnection connection;
		try {
			connection = new LDAPConnection(
					sslUtil.createSSLSocketFactory(),
					"ldap.messagingengine.com", 636,
					"cn=" + email + ",dc=User", password);
		} catch (LDAPException | GeneralSecurityException e) {
			e.printStackTrace();
			return false;
		}
		Boolean success = connection.isConnected();
		if(success){
			connection.close();
		}
		return success;
	}
	
	public static List<FastmailContact> getContacts(Account account, String password) throws LDAPException, GeneralSecurityException{
		SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
		LDAPConnection connection;
		connection = new LDAPConnection(
				sslUtil.createSSLSocketFactory(),
				"ldap.messagingengine.com", 636,
				"cn=" + account.name + ",dc=User", password);
		SearchRequest search = new SearchRequest("dc=AddressBook", SearchScope.BASE, Filter.create("(objectClass=*)"));
		SearchResult result = connection.search(search);
		List<FastmailContact> contacts = new ArrayList<FastmailContact>();
		for(SearchResultEntry entry : result.getSearchEntries()){
			int id = entry.getAttributeValueAsInteger("uid");
			String displayName = entry.getAttributeValue("displayName");
			String companyName = entry.getAttributeValue("o");
			String email = entry.getAttributeValue("mail");
			String emailAlt = entry.getAttributeValue("otherMailbox");
			String phoneHome = entry.getAttributeValue("homePhone");
			String phoneMobile = entry.getAttributeValue("mobile");
			String phoneWork = entry.getAttributeValue("telephoneNumber");
			String notes = entry.getAttributeValue("description");
			
			FastmailContact contact = new FastmailContact(id, displayName, companyName, email, emailAlt, phoneHome, phoneWork, phoneMobile, notes);
			contacts.add(contact);
		}
		connection.close();
		return contacts;
	}
}
