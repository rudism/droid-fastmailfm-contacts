package com.rudism.fastmail.fm.contacts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class FastmailAuthenticatorService extends Service {

	private FastmailAuthenticator _authenticator;
	
	@Override
	public void onCreate(){
		_authenticator = new FastmailAuthenticator(this);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return _authenticator.getIBinder();
	}

}
