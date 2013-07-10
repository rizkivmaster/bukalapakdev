package view.transaction; 

import java.util.ArrayList;

import org.json.JSONObject;

import listener.APIListener;
import model.system.InternetTask;
import services.APIService;
import view.general.ExtendedActivity;

import com.bukalapakmobile.R;
import com.bukalapakmobile.R.layout;
import com.bukalapakmobile.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class TransactionActivity extends ExtendedActivity {
	Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transaction_list);
		context = getApplicationContext();
	}
	
	@Override
	public void onServiceReady(final APIService api) {
		// TODO Auto-generated method stub
		super.onServiceReady(api);
		try {
			api.listTransaction(new APIListener() {
				
				@Override
				public void onSuccess(Object res, Exception e, InternetTask task) {
					// TODO Auto-generated method stub
					clearProgressDialog();
					if(e!=null)
					{
						Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
					}
					else
					{
						ArrayList<JSONObject> arr = (ArrayList<JSONObject>) res;
						ListView mainList = (ListView) findViewById(R.id.list);
						TransactionItem adapter = new TransactionItem(arr, context, api);
						mainList.setAdapter(adapter);
					}
					
				}
				
				@Override
				public void onHold(InternetTask task) {
					// TODO Auto-generated method stub
					clearProgressDialog();
				}
				
				@Override
				public void onExecute(InternetTask task) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onEnqueue(InternetTask task) {
					// TODO Auto-generated method stub
					showProgressDialog("Transaksi", "Sedang menunggu transaksi", null);
				}
			}, 1, 10);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_transaction, menu);
		return true;
	}

}
