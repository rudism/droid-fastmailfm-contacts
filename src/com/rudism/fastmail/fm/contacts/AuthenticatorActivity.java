package com.rudism.fastmail.fm.contacts;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class AuthenticatorActivity extends ActionBarActivity {
	
	private AccountAuthenticatorResponse _accountAuthenticatorResponse = null;
	private Bundle _resultBundle = null;
	
	private Thread _authThread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authenticator_screen);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		_accountAuthenticatorResponse =
				getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
		if (_accountAuthenticatorResponse != null) {
			_accountAuthenticatorResponse.onRequestContinued();
		}
	}
	
	public final void setAccountAuthenticatorResult(Bundle result) {
		_resultBundle = result;
	}
	
	public void authenticate(final String email, final String password){
		DialogFragment dialog = new ProgressDialogFragment();
		dialog.show(getSupportFragmentManager(), "authenticating");
		_authThread = attemptAuth(email, password);
	}
	
	public void authenticationComplete(final Boolean success, final String email, final String password){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				DialogFragment dialog = (DialogFragment) getSupportFragmentManager().findFragmentByTag("authenticating");
				dialog.dismiss();
				if(!success){
					Toast failed = Toast.makeText(AuthenticatorActivity.this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT);
					failed.show();
				}
			}
		});
		
		if(success){
			Account account = new Account(email, FastmailAuthenticator.ACCOUNT_TYPE);
			AccountManager am = AccountManager.get(AuthenticatorActivity.this);
			CheckBox autosync = (CheckBox) AuthenticatorActivity.this.findViewById(R.id.checkboxAutoSync);
			Bundle userData = new Bundle();
			userData.putString("autosync", autosync.isChecked() ? "true" : "false");
			am.addAccountExplicitly(account, password, userData);
			ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
			if(autosync.isChecked()){
				ContentResolver.addPeriodicSync(account, ContactsContract.AUTHORITY, new Bundle(), 86400000);
			}
			final Intent intent = new Intent();
			intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, email);
			intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, FastmailAuthenticator.ACCOUNT_TYPE);
			intent.putExtra(AccountManager.KEY_AUTHTOKEN, password);
			setAccountAuthenticatorResult(intent.getExtras());
			setResult(RESULT_OK, intent);
			finish();
		}
	}
	
	private Thread attemptAuth(final String email, final String password){
		final Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				AuthenticatorActivity.this.authenticationComplete(LdapUtilities.authenticate(email,  password), email, password);
			}
		};
		final Thread thread = new Thread(){
			@Override
			public void run() {
				try {
					runnable.run();
				}
				finally {
					
				}
			};
		};
		thread.start();
		return thread;
	}
	
	public void finish() {
		if (_accountAuthenticatorResponse != null) {
			// send the result bundle back if set, otherwise send an error.
			if (_resultBundle != null) {
				_accountAuthenticatorResponse.onResult(_resultBundle);
			} else {
				_accountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED,
						"canceled");
			}
			_accountAuthenticatorResponse = null;
		}
		super.finish();
	}
	
	public static class ProgressDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
				.setTitle(getActivity().getString(R.string.authenticating_title))
				.setMessage(getActivity().getString(R.string.authenticating_message))
				.create();
		}
		@Override
		public boolean isCancelable() {
			return false;
		}
	}

	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			final View rootView = inflater.inflate(R.layout.fragment_authenticator_screen,
					container, false);
			
			Button cancelButton = (Button) rootView.findViewById(R.id.buttonCancel);
			cancelButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Activity activity = getActivity();
					activity.setResult(RESULT_CANCELED);
					activity.finish();
				}
			});
			
			Button addButton = (Button) rootView.findViewById(R.id.buttonAdd);
			addButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Context context = v.getContext();
					EditText inputEmail = (EditText) rootView.findViewById(R.id.inputEmail);
					EditText inputPassword = (EditText) rootView.findViewById(R.id.inputPassword);
					String email = inputEmail.getText().toString();
					String password = inputPassword.getText().toString();
					if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
						Toast error = Toast.makeText(context, context.getString(R.string.required_alert), Toast.LENGTH_SHORT);
						error.show();
					} else {
						AuthenticatorActivity activity = (AuthenticatorActivity) getActivity();
						activity.authenticate(email, password);
					}
				}
			});
			
			return rootView;
		}
	}
}
