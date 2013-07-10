package view.product.list;


import java.util.ArrayList;

import listener.APIListener;
import model.business.Product;
import model.system.InternetTask;
import services.APIService;
import view.general.ExtendedFragment;
import view.general.ExtendedFragmentActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.bukalapakmobile.R;

 
/**
 * @author mwho
 *
 */
public class Tab_Dijual extends ExtendedFragment {
    /** (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
	Context context;
	LayoutInflater inflater;
	View mainView;
	APIService api;
	ViewGroup container;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		context = getActivity();
		api = ((ExtendedFragmentActivity)getActivity()).getApi();
    	this.inflater = inflater;
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }
        this.container = container;
        View myFragmentView = inflater.inflate(R.layout.tab_frag1_layout, container, false);
    	final ListView listview = (ListView) myFragmentView.findViewById(R.id.listView);
    	//append grid ke fragment
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

						ArrayList<Product> products = new ArrayList<Product>();
						products.addAll((ArrayList<Product>) res);
						LapakItemAdapter adapter = new LapakItemAdapter(context, api, products);
						listview.setAdapter(adapter);
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
					showProgressDialog("Produk Terjual", "Sedang mengambil...", new DialogInterface.OnCancelListener() {
						
						@Override
						public void onCancel(DialogInterface dialog) {
							// TODO Auto-generated method stub
							getActivity().finish();
							clearProgressDialog();
						}
					});
				}
			}, true, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return myFragmentView;
    }
}
