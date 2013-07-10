package view.product.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import listener.APIListener;
import model.business.Product;
import model.system.InternetTask;
import services.APIService;
import view.product.edit.EditProductActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bukalapakmobile.R;

public class LapakItemAdapter extends BaseAdapter {
	Context context;
	APIService api;
	ArrayList<Product> productList;
	HashMap<String,Bitmap> bitmaps;
	HashMap<String,Boolean> checks;
	LayoutInflater inflater;

	public LapakItemAdapter(Context c, APIService a, ArrayList<Product> list) {
		// TODO Auto-generated constructor stub
		context = c;
		api = a;
		productList = list;
		inflater = LayoutInflater.from(context);
		checks = new HashMap<String, Boolean>();
		bitmaps = new HashMap<String, Bitmap>();
	}

	public HashSet<Product> getSelected() {
		HashSet<Product> selectedProduct;
		selectedProduct = new HashSet<Product>();
		for (int ii = 0; ii < productList.size(); ii++) {
			if (checks.get(productList.get(ii).getId())!=null&&checks.get(productList.get(ii).getId())==true)
				selectedProduct.add(productList.get(ii));
		}
		return selectedProduct;
	}

	public void deleteProduct(Product prod) {
		this.productList.remove(prod);
		this.checks.remove(prod.getId());
		this.bitmaps.remove(prod.getId());
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return productList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return productList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		final Product p = productList.get(arg0);
		if (arg1 == null) {
			arg1 = inflater.inflate(R.layout.view_product_list_item, null);

		}

		final ImageView prodIcon = (ImageView) arg1
				.findViewById(R.id.image_item);
		if (bitmaps.containsKey(p.getId()))
			prodIcon.setImageBitmap(bitmaps.get(p.getId()));
		else {
			String imgLink = p.getImages().get(0);
			try {
				try {
					api.retrieveImage(new APIListener() {

						@Override
						public void onSuccess(Object res, Exception e,
								InternetTask task) {
							// TODO Auto-generated method stub
							bitmaps.put(p.getId(),(Bitmap) res);
							notifyDataSetChanged();
						}

						@Override
						public void onHold(InternetTask task) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onExecute(InternetTask task) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onEnqueue(InternetTask task) {
							// TODO Auto-generated method stub

						}
					}, imgLink);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		TextView prodName = (TextView) arg1.findViewById(R.id.title);
		prodName.setText(p.getName());

		TextView prodPrice = (TextView) arg1.findViewById(R.id.price);
		prodPrice.setText("Rp " + p.getPrice());

		TextView prodStock = (TextView) arg1.findViewById(R.id.stock);
		prodStock.setText(p.getStock() + " pcs");

		TextView prodStatus = (TextView) arg1.findViewById(R.id.status);
		if (p.isPayment_ready()) {
			prodStatus.setText("dijual");
			prodStatus.setBackgroundColor(Color.GREEN);
		} else {
			prodStatus.setText("terjual");
			prodStatus.setBackgroundColor(Color.RED);
		}
		
		Button btn_edit = (Button) arg1.findViewById(R.id.btn_edit);
		btn_edit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(context,EditProductActivity.class);
				intent.putExtra("id",p.getId());
				context.startActivity(intent);
			}
		});

		CheckBox prodCheck = (CheckBox) arg1.findViewById(R.id.check);

		prodCheck.setChecked(checks.get(p.getId())!=null&&checks.get(p.getId()));
		prodCheck.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked())
					checks.put(p.getId(), true);
				else
					checks.put(p.getId(), false);

			}
		});
		return arg1;
	}

}
