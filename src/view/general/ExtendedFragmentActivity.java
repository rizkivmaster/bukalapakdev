package view.general;

import services.APIService;

import com.bukalapakmobile.R;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

public class ExtendedFragmentActivity extends FragmentActivity {
	protected APIService api;
	Button home;
	ProgressDialog pd;
	boolean isBound = false;
	private ServiceConnection mConnection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		pd = new ProgressDialog(this);

		mConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				api = ((APIService.MyBinder) arg1).getService();
				onServiceReady(api);
			}

			@Override
			public void onServiceDisconnected(ComponentName arg0) {
				onServiceLost();
			}
		};
		isBound =bindService(new Intent(this, APIService.class), mConnection,
				Context.BIND_AUTO_CREATE);
	}

	public void onServiceReady(APIService api) {
	}

	public void onServiceLost() {
	}
	public void setHomeButton(boolean b) {

		if (b) {
			home = (Button) findViewById(R.id.home_icon);
			home.setVisibility(Button.VISIBLE);
		} else
			home.setVisibility(Button.INVISIBLE);
	}
	public void onHomeButtonClick(View v)
	{
		finish();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(api!=null)		api.endForegroundProcess();
		unbindService(mConnection);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		unbindService(mConnection);
		if(api!=null)		api.endForegroundProcess();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mConnection != null)
			bindService(new Intent(this, APIService.class), mConnection,
					Context.BIND_AUTO_CREATE);
	}

	public void showProgressDialog(String t, String s, OnCancelListener cl) {
		pd.setTitle(t);
		pd.setMessage(s);
		if (cl != null) {
			pd.setOnCancelListener(cl);
			pd.setCancelable(true);
		} else {
			pd.setCancelable(false);
		}
		pd.setIndeterminate(true);
		pd.show();
	}

	public void clearProgressDialog() {
		pd.dismiss();
	}
	
	public APIService getApi()
	{
		return api; 
	}

}
