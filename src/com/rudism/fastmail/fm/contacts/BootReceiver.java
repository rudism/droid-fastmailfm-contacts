package com.rudism.fastmail.fm.contacts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		AccountManager am = AccountManager.get(context);
		Account[] accounts = am.getAccountsByType(FastmailAuthenticator.ACCOUNT_TYPE);
		for(Account account : accounts){
			if(ContentResolver.getSyncAutomatically(account, ContactsContract.AUTHORITY)){
				ContentResolver.requestSync(account, ContactsContract.AUTHORITY, new Bundle());
				if(am.getUserData(account, "autosync").equals("true")){
					ContentResolver.addPeriodicSync(account, ContactsContract.AUTHORITY, new Bundle(), 86400000);
				}
			}
		}
	}
}