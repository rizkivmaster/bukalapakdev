package view.product.list;

import java.util.ArrayList;
import java.util.HashSet;

import listener.APIListener;
import model.business.Product;
import model.system.InternetTask;
import services.APIService;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.bukalapakmobile.R;

public class LapakItemAdapter extends BaseAdapter {
	Context context;
	APIService api;
	ArrayList<Product> productList;
	HashSet<Product> selectedProduct;

	public LapakItemAdapter(Context c, APIService a, ArrayList<Product> list) {
		// TODO Auto-generated constructor stub
		context = c;
		api = a;
		productList = list;
		selectedProduct = new HashSet<Product>();
	}

	public HashSet<Product> getSelected() {
		return selectedProduct;
	}

	public void deleteProduct(Product prod) {
		this.productList.remove(prod);
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
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		final Product p = productList.get(arg0);
		if (arg1 == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			arg1 = inflater.inflate(R.layout.view_product_list_item, null);

			String imgLink = p.getImages().get(0);
			final ImageView prodIcon = (ImageView) arg1
					.findViewById(R.id.image_item);
			try {
				api.retrieveImage(new APIListener() {

					@Override
					public void onSuccess(Object res, Exception e,
							InternetTask task) {
						// TODO Auto-generated method stub
						prodIcon.setImageBitmap((Bitmap) res);
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

			CheckBox prodCheck = (CheckBox) arg1.findViewById(R.id.check);
			prodCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					// TODO Auto-generated method stub
					if (isChecked) {
						selectedProduct.add(p);
					} else {
						selectedProduct.remove(p);
					}
				}
			});

		}
		return arg1;
	}

}
