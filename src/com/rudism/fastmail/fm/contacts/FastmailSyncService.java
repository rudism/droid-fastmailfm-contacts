package com.rudism.fastmail.fm.contacts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class FastmailSyncService extends Service {

	private static FastmailSyncAdapter _syncAdapter = null;
	private static final Object _syncAdapterLock = new Object();
	
	@Override
	public void onCreate(){
		synchronized(_syncAdapterLock){
			if(_syncAdapter == null){
				_syncAdapter = new FastmailSyncAdapter(getApplicationContext(), true);
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return _syncAdapter.getSyncAdapterBinder();
	}

}
