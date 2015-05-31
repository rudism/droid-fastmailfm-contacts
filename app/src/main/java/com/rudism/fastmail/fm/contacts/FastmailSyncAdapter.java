package com.rudism.fastmail.fm.contacts;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.unboundid.ldap.sdk.LDAPException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Build.VERSION;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;

public class FastmailSyncAdapter extends AbstractThreadedSyncAdapter {

	private Context _context;
	
	public FastmailSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		_context = context;
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		List<FastmailContact> contacts;
		String password = null;
		AccountManager am = AccountManager.get(_context);
		try {
			password = am.blockingGetAuthToken(account, "password", true);
			contacts = LdapUtilities.getContacts(account, password);
			
			ContentResolver resolver = _context.getContentResolver();
			Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon()
					.appendQueryParameter(RawContacts.ACCOUNT_NAME, account.name)
					.appendQueryParameter(RawContacts.ACCOUNT_TYPE, FastmailAuthenticator.ACCOUNT_TYPE)
					.build();

			// preserve favorites
			HashSet<Integer> favs = new HashSet<Integer>();
			Cursor curs = resolver.query(rawContactUri, null, null, null, null);
			if(curs.getCount() > 0){
				while(curs.moveToNext()){
					int sid = curs.getInt(curs.getColumnIndex(RawContacts.SOURCE_ID));
					int isFav = curs.getInt(curs.getColumnIndex(RawContacts.STARRED));
					if(isFav == 1){
						favs.add(sid);
					}
				}
			}
			
			resolver.delete(rawContactUri, null, null);
			
			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			
			for(FastmailContact contact : contacts){
				ContentProviderOperation.Builder builder =
						ContentProviderOperation.newInsert(RawContacts.CONTENT_URI.buildUpon()
								.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build())
								.withYieldAllowed(true);
				builder.withValue(RawContacts.SOURCE_ID, contact.getId());
				if(favs.contains(contact.getId())){
					builder.withValue(RawContacts.STARRED, 1);
				}
				builder.withValue(RawContacts.ACCOUNT_NAME, account.name);
				builder.withValue(RawContacts.ACCOUNT_TYPE, FastmailAuthenticator.ACCOUNT_TYPE);
				int idref = operations.size();
				operations.add(builder.build());
				
				if(!TextUtils.isEmpty(contact.getDisplayName())){
					ContentProviderOperation.Builder name =
							ContentProviderOperation.newInsert(Data.CONTENT_URI.buildUpon()
									.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build())
									.withYieldAllowed(true);
					name.withValueBackReference(Data.RAW_CONTACT_ID, idref);
					name.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getDisplayName());
					name.withValue(ContactsContract.CommonDataKinds.StructuredName.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
					operations.add(name.build());
				}
				
				if(!TextUtils.isEmpty(contact.getCompanyName())){
					ContentProviderOperation.Builder company =
							ContentProviderOperation.newInsert(Data.CONTENT_URI.buildUpon()
										.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build())
										.withYieldAllowed(true);
					company.withValueBackReference(Data.RAW_CONTACT_ID, idref);
					company.withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, contact.getCompanyName());
					company.withValue(ContactsContract.CommonDataKinds.Organization.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
					operations.add(company.build());
				}
				
				if(!TextUtils.isEmpty(contact.getEmail())){
					ContentProviderOperation.Builder email =
							ContentProviderOperation.newInsert(Data.CONTENT_URI.buildUpon()
										.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build())
										.withYieldAllowed(true);
					email.withValueBackReference(Data.RAW_CONTACT_ID, idref);
					email.withValue(ContactsContract.CommonDataKinds.Email.DATA, contact.getEmail());
					email.withValue(ContactsContract.CommonDataKinds.Email.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
					operations.add(email.build());
				}
				
				if(!TextUtils.isEmpty(contact.getEmailAlt())){
					ContentProviderOperation.Builder email =
							ContentProviderOperation.newInsert(Data.CONTENT_URI.buildUpon()
										.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build())
										.withYieldAllowed(true);
					email.withValueBackReference(Data.RAW_CONTACT_ID, idref);
					email.withValue(ContactsContract.CommonDataKinds.Email.DATA, contact.getEmailAlt());
					email.withValue(ContactsContract.CommonDataKinds.Email.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
					operations.add(email.build());
				}
					
				if(!TextUtils.isEmpty(contact.getPhoneHome())){
					ContentProviderOperation.Builder phone =
							ContentProviderOperation.newInsert(Data.CONTENT_URI.buildUpon()
										.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build())
										.withYieldAllowed(true);
					phone.withValueBackReference(Data.RAW_CONTACT_ID, idref);
					phone.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getPhoneHome());
					phone.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
					phone.withValue(ContactsContract.CommonDataKinds.Phone.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
					operations.add(phone.build());
				}
				
				if(!TextUtils.isEmpty(contact.getPhoneMobile())){
					ContentProviderOperation.Builder phone =
							ContentProviderOperation.newInsert(Data.CONTENT_URI.buildUpon()
										.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build())
										.withYieldAllowed(true);
					phone.withValueBackReference(Data.RAW_CONTACT_ID, idref);
					phone.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getPhoneMobile());
					phone.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
					phone.withValue(ContactsContract.CommonDataKinds.Phone.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
					operations.add(phone.build());
				}
				
				if(!TextUtils.isEmpty(contact.getPhoneWork())){
					ContentProviderOperation.Builder phone =
							ContentProviderOperation.newInsert(Data.CONTENT_URI.buildUpon()
										.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build())
										.withYieldAllowed(true);
					phone.withValueBackReference(Data.RAW_CONTACT_ID, idref);
					phone.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getPhoneWork());
					phone.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
					phone.withValue(ContactsContract.CommonDataKinds.Phone.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
					operations.add(phone.build());
				}
				
				if(!TextUtils.isEmpty(contact.getNotes())){
					ContentProviderOperation.Builder notes =
							ContentProviderOperation.newInsert(Data.CONTENT_URI.buildUpon()
										.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build())
										.withYieldAllowed(true);
					notes.withValueBackReference(Data.RAW_CONTACT_ID, idref);
					notes.withValue(ContactsContract.CommonDataKinds.Note.NOTE, contact.getNotes());
					notes.withValue(ContactsContract.CommonDataKinds.Note.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE);
					operations.add(notes.build());
				}
			}
			
			provider.applyBatch(operations);
		} catch (AuthenticatorException | LDAPException | GeneralSecurityException e) {
			e.printStackTrace();
			syncResult.stats.numAuthExceptions++;
		} catch (OperationCanceledException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			syncResult.stats.numIoExceptions++;
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
