package view.transaction;

import java.util.ArrayList;
import java.util.zip.Inflater;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bukalapakmobile.R;

import services.APIService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TransactionItem extends BaseAdapter {
	ArrayList<JSONObject> transactions;
	Context context;
	APIService api;
	LayoutInflater inflater;
	
	private static int ICON_OFF_ORDERED = R.drawable.payment_statuses_004;
	private static int ICON_OFF_PAID = R.drawable.payment_statuses_005;
	private static int ICON_OFF_SENT = R.drawable.payment_statuses_006;
	private static int ICON_OFF_ARRIVED = R.drawable.payment_statuses_008;
	
	private static int ICON_ON_ORDERED = R.drawable.payment_statuses_014;
	private static int ICON_ON_PAID = R.drawable.payment_statuses_015;
	private static int ICON_ON_SENT = R.drawable.payment_statuses_016;
	private static int ICON_ON_ARRIVED = R.drawable.payment_statuses_018;

	public TransactionItem(ArrayList<JSONObject> t, Context c, APIService a) {
		this.context = c;
		this.api = a;
		this.transactions = t;
		inflater = LayoutInflater.from(c);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.transactions.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return this.transactions.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		if (arg1 == null) {
			arg1 = inflater.inflate(R.layout.transaction_item, null);
		}
		TextView tProduct = (TextView) arg1.findViewById(R.id.product_trans);
		TextView tID = (TextView) arg1.findViewById(R.id.trans_id);
		TextView tTime = (TextView) arg1.findViewById(R.id.trans_date);
		ImageView icon_Ordered = (ImageView) arg1
				.findViewById(R.id.trans_ordered);
		ImageView icon_Paid = (ImageView) arg1.findViewById(R.id.trans_paid);
		ImageView icon_Sent = (ImageView) arg1.findViewById(R.id.trans_sent);
		ImageView icon_Arrived = (ImageView) arg1
				.findViewById(R.id.trans_arrived);
		Button btnSend = (Button) arg1.findViewById(R.id.trans_btnsend);

		JSONObject transaction = transactions.get(arg0);

		try {
			JSONObject product = transaction.getJSONObject("product");
			String listProduct = product.getString("name");
			tProduct.setText(listProduct);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
		}

		try {
			tID.setText(transaction.getString("transaction_id"));
			tTime.setText(transaction.getString("total_amount"));
			
			String status = transaction.getString("state");
			if(status.equals("ordered"))
			{
				icon_Ordered.setImageResource(ICON_ON_ORDERED);
				icon_Paid.setImageResource(ICON_OFF_PAID);
				icon_Sent.setImageResource(ICON_OFF_SENT);
				icon_Arrived.setImageResource(ICON_OFF_ARRIVED);
			}
			else if(status.equals("paid"))
			{
				icon_Ordered.setImageResource(ICON_ON_ORDERED);
				icon_Paid.setImageResource(ICON_ON_PAID);
				icon_Sent.setImageResource(ICON_OFF_SENT);
				icon_Arrived.setImageResource(ICON_OFF_ARRIVED);
			}
			else if(status.equals("sent"))
			{
				icon_Ordered.setImageResource(ICON_ON_ORDERED);
				icon_Paid.setImageResource(ICON_ON_PAID);
				icon_Sent.setImageResource(ICON_ON_SENT);
				icon_Arrived.setImageResource(ICON_OFF_ARRIVED);
			}
			else if(status.equals("arrived")){
				icon_Ordered.setImageResource(ICON_ON_ORDERED);
				icon_Paid.setImageResource(ICON_ON_PAID);
				icon_Sent.setImageResource(ICON_ON_SENT);
				icon_Arrived.setImageResource(ICON_ON_ARRIVED);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			JSONArray arr = transaction.getJSONArray("actions");
			for(int ii = 0 ; ii < arr.length();ii++)
			{
				String action = arr.getString(ii);
				if(action.equals("deliver"))
				{
					btnSend.setText("Kirim Barang");
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return arg1;
		
	}

}
