package view.general;

import services.APIService;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnCancelListener;
import android.os.IBinder;
import android.widget.Button;


public class ExtendedFragment extends android.support.v4.app.Fragment {
	Context context;
	ProgressDialog pd;

	public void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
		pd = new ProgressDialog(context);
	};
	
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
}
