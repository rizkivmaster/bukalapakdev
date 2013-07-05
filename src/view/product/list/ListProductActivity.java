package view.product.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import listener.APIListener;
import model.business.Product;
import model.system.InternetTask;
import services.APIService;
import view.general.ExtendedActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bukalapakmobile.R;

public class ListProductActivity extends ExtendedActivity implements
		OnItemSelectedListener {
	Button btn_action;
	Spinner spinner_action;
	LapakItemAdapter adapter;
	ListView listview;
	Context context;
	String action;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_product_list_main);
		context = this;
		btn_action = (Button) findViewById(R.id.btn_action);
		spinner_action = (Spinner) findViewById(R.id.spinner_action);
		listview = (ListView) findViewById(R.id.listLapak);
		
		btn_action.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final Set<Product> s = adapter.getSelected();
				if(action==null)
				{
					return;
				}
				else if(s.isEmpty())
				{
					Toast.makeText(context, "Tidak ada barang yang dipilih", Toast.LENGTH_SHORT);
				}
				else if(action.equals("Hapus"))
				{
					DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					    @Override
					    public void onClick(DialogInterface dialog, int which) {
					        switch (which){
					        case DialogInterface.BUTTON_POSITIVE:
					            //Yes button clicked
					        	for (final Product p : s) {
									api.deleteProduct(new APIListener(){

										@Override
										public void onEnqueue(InternetTask task) {
											// TODO Auto-generated method stub
											
										}

										@Override
										public void onExecute(InternetTask task) {
											// TODO Auto-generated method stub
											
										}

										@Override
										public void onSuccess(Object res,
												Exception e, InternetTask task) {
											// TODO Auto-generated method stub
											adapter.productList.remove(p);
											adapter.notifyDataSetChanged();
										}

										@Override
										public void onHold(InternetTask task) {
											// TODO Auto-generated method stub
											
										}
										
									}, p.getId());
								}
					            break;

					        case DialogInterface.BUTTON_NEGATIVE:
					            //No button clicked
					            break;
					        }
					    }
					};

					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Hapus produk");
					builder.setMessage("Apakah anda yakin?").setPositiveButton("Ya", dialogClickListener)
					    .setNegativeButton("Tidak", dialogClickListener).show();
					
				}
				else if(action.equals("Set terjual"))
				{
					DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					    @Override
					    public void onClick(DialogInterface dialog, int which) {
					        switch (which){
					        case DialogInterface.BUTTON_POSITIVE:
					            //Yes button clicked
					        	for (final Product p : s) {
									api.setSoldProduct(new APIListener(){

										@Override
										public void onEnqueue(InternetTask task) {
											// TODO Auto-generated method stub
											
										}

										@Override
										public void onExecute(InternetTask task) {
											// TODO Auto-generated method stub
											
										}

										@Override
										public void onSuccess(Object res,
												Exception e, InternetTask task) {
											// TODO Auto-generated method stub
											adapter.productList.remove(p);
											adapter.notifyDataSetChanged();
										}

										@Override
										public void onHold(InternetTask task) {
											// TODO Auto-generated method stub
											
										}
										
									}, p.getId());
								}
					            break;

					        case DialogInterface.BUTTON_NEGATIVE:
					            //No button clicked
					            break;
					        }
					    }
					};

					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Set terjual");
					builder.setMessage("Apakah anda yakin?").setPositiveButton("Ya", dialogClickListener)
					    .setNegativeButton("Tidak", dialogClickListener).show();
				}
				else if(action.equals("Set tidak dijual"))
				{
					DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					    @Override
					    public void onClick(DialogInterface dialog, int which) {
					        switch (which){
					        case DialogInterface.BUTTON_POSITIVE:
					            //Yes button clicked
					        	for (final Product p : s) {
									api.relistProduct(new APIListener(){

										@Override
										public void onEnqueue(InternetTask task) {
											// TODO Auto-generated method stub
											
										}

										@Override
										public void onExecute(InternetTask task) {
											// TODO Auto-generated method stub
											
										}

										@Override
										public void onSuccess(Object res,
												Exception e, InternetTask task) {
											// TODO Auto-generated method stub
											adapter.productList.remove(p);
											adapter.notifyDataSetChanged();
										}

										@Override
										public void onHold(InternetTask task) {
											// TODO Auto-generated method stub
											
										}
										
									}, p.getId());
								}
					            break;

					        case DialogInterface.BUTTON_NEGATIVE:
					            //No button clicked
					            break;
					        }
					    }
					};

					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Set tidak dijual");
					builder.setMessage("Apakah anda yakin?").setPositiveButton("Ya", dialogClickListener)
					    .setNegativeButton("Tidak", dialogClickListener).show();
				}
			}
		});

		// Spinner click listener
		spinner_action.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				TextView tv = (TextView) arg1;
				action = tv.getText().toString();
			};

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});

		// Spinner Drop down elements
		List<String> categories = new ArrayList<String>();
		categories.add("Hapus");
		categories.add("Set terjual");
		categories.add("Set tidak dijual");

		// Creating adapter for spinner
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, categories);

		// Drop down layout style - list view with radio button
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// attaching data adapter to spinner
		spinner_action.setAdapter(dataAdapter);
	}

	@Override
	public void onServiceReady(final APIService api) {
		// TODO Auto-generated method stub
		super.onServiceReady(api);
		this.api = api;
		final ArrayList<Product> products = new ArrayList<Product>();
		adapter = new LapakItemAdapter(context, api, products);
		listview.setAdapter(adapter);
		try {
			api.listLapak(new APIListener() {

				@Override
				public void onSuccess(Object res, Exception e, InternetTask task) {
					// TODO Auto-generated method stub
					clearProgressDialog();
					if (e != null) {
						Toast.makeText(context, e.getMessage(),
								Toast.LENGTH_SHORT).show();
					} else {
						products.addAll((ArrayList<Product>) res);
						adapter.notifyDataSetChanged();
					}
				}

				@Override
				public void onHold(InternetTask task) {
					// TODO Auto-generated method stub
					clearProgressDialog();
					api.endForegroundProcess();
				}

				@Override
				public void onExecute(InternetTask task) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onEnqueue(final InternetTask task) {
					// TODO Auto-generated method stub
					showProgressDialog("My Lapak", "Sedang mengambil lapak...",
							new DialogInterface.OnCancelListener() {

								@Override
								public void onCancel(DialogInterface dialog) {
									// TODO Auto-generated method stub
									api.endForegroundProcess();
									finish();
								}
							});
				}
			}, true, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		// On selecting a spinner item
		String item = parent.getItemAtPosition(position).toString();

		// Showing selected spinner item
		Toast.makeText(parent.getContext(), "Selected: " + item,
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}
	
	
	public void refreshList()
	{
		
	}

}

