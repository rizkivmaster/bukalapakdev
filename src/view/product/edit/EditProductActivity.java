package view.product.edit;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import listener.APIListener;
import model.business.Product;
import model.system.InternetTask;

import org.json.JSONException;
import org.json.JSONObject;

import services.APIService;
import view.general.ExtendedActivity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bukalapakmobile.R;

public class EditProductActivity extends ExtendedActivity {
	// /GROUP SELECTION
	private static final int GROUP_PICTURE_SELECTION = 1;
	private static final int GROUP_CATEGORY_SELECTION_NODE = 2;
	private static final int GROUP_CATEGORY_SELECTION_CHILD = 5;
	private static final int GROUP_CATEGORY_CANCEL = 3;
	private static final int GROUP_CATEGORY_BACK = 4;
	// /FOR TAKING PICTURE
	private static final int PICK_FROM_SOURCE = 1;
	private static final int PICK_CROP = 2;
	private static final int SELECT_IMAGE_CAMERA = 3;
	private static final int SELECT_IMAGE_GALLERY = 4;
	// for category
	LinkedList<JSONObject> cate_src;
	ArrayList<String> listKategori = new ArrayList<String>();

	private Context context;

	// String kategori;
	TextView kategori;
	Button kategori_edit;
	EditText namaBarang;
	EditText kondisi;
	EditText hargaBarang;
	EditText beratBarang;
	EditText stokBarang;
	EditText deskripsiBarang;
	RadioGroup kondisiGroup;
	RadioButton bekas;
	RadioButton baru;
	String username;
	String password;
	String[] list_kota;
	Button image_select;
	Button unggah;
	Button city_select;
	CheckBox nego;
	CheckBox kurirJNE;
	CheckBox kurirTIKI;
	CheckBox kurirPOS;
	LinearLayout len;
	LinearLayout listImages;
	ArrayList<String> img_ids;
	// Sementara
	// HashMap<String, String> attribs;
	// HashMap<String, View> specs;
	String product_id;
	HashMap<String, String> specs;
	String category_id;
	ImageView imgview;
	// ImageUploadAdapter imageAdapter;
	private Bitmap bitmap;
	// Session Manager Class
	// SessionManager session;
	private APIService api;
	private ServiceConnection mConnection;

	protected CharSequence[] kota;
	protected ArrayList<CharSequence> selectedKota;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.view_product_edit_main);
		// setHomeButton(true);
		// imageAdapter = new ImageUploadAdapter(getApplicationContext());
		len = (LinearLayout) findViewById(R.id.listSpecs);
		listImages = (LinearLayout) findViewById(R.id.listImages);

		// untuk multiplechoice
		list_kota = getResources().getStringArray(R.array.daftar_kota);
		kota = (CharSequence[]) list_kota;
		selectedKota = new ArrayList<CharSequence>();

		// session = new SessionManager(getApplicationContext());
		//
		// TextView lblName = (TextView) findViewById(R.id.lblName);
		// TextView lblEmail = (TextView) findViewById(R.id.lblEmail);

		kategori = (TextView) findViewById(R.id.edit_kategori_text);
		// kategori_edit = (Button) findViewById(R.id.edit_kategori_button);
		namaBarang = (EditText) findViewById(R.id.edit_namabarang_edittext);
		bekas = (RadioButton) findViewById(R.id.edit_radioBekas);
		baru = (RadioButton) findViewById(R.id.edit_radioBaru);
		kondisiGroup = (RadioGroup) findViewById(R.id.edit_radioKondisi);
		hargaBarang = (EditText) findViewById(R.id.edit_hargabarang_edittext);
		nego = (CheckBox) findViewById(R.id.edit_checkBisaNego);
		beratBarang = (EditText) findViewById(R.id.edit_perkiraanberat_edittext);
		stokBarang = (EditText) findViewById(R.id.edit_stok_edittext);
		kurirJNE = (CheckBox) findViewById(R.id.edit_checkJasaKurirJNE);
		kurirTIKI = (CheckBox) findViewById(R.id.edit_checkJasaKurirTIKI);
		kurirPOS = (CheckBox) findViewById(R.id.edit_checkJasaKurirPos);
		deskripsiBarang = (EditText) findViewById(R.id.edit_deskripsi_edittext);
		image_select = (Button) findViewById(R.id.edit_photo_button);
		imgview = (ImageView) findViewById(R.id.photo_image);
		unggah = (Button) findViewById(R.id.edit_product_upload_save_button);
		city_select = (Button) findViewById(R.id.edit_pilihdelivery_button);
		registerForContextMenu(image_select);
		// registerForContextMenu(kategori_edit);

		// kategori_edit.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// api.listCategory(new APIListener() {
		//
		// @Override
		// public void onSuccess(Object res, Exception e,
		// InternetTask task) {
		// // TODO Auto-generated method stub
		// clearProgressDialog();
		// if (e != null) {
		// Toast.makeText(context, e.getMessage(),
		// Toast.LENGTH_SHORT).show();
		// } else {
		// cate_src.add((JSONObject) res);
		// openContextMenu(kategori_edit);
		// }
		// }
		//
		// @Override
		// public void onHold(InternetTask task) {
		// // TODO Auto-generated method stub
		// finish();
		// clearProgressDialog();
		// }
		//
		// @Override
		// public void onExecute(InternetTask task) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onEnqueue(InternetTask task) {
		// // TODO Auto-generated method stub
		// showProgressDialog("Kategori",
		// "Sedang mengambil kategori....",
		// new OnCancelListener() {
		//
		// @Override
		// public void onCancel(DialogInterface dialog) {
		// // TODO Auto-generated method stub
		// finish();
		// }
		// });
		// }
		// });
		//
		// }
		// });

		// image_select.setOnClickListener(new View.OnClickListener() {
		//
		// public void onClick(View v) {
		// try {
		// openContextMenu(image_select);
		// } catch (ActivityNotFoundException e) {
		// // Do nothing for now
		// }
		// }
		// });

		city_select.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				switch (view.getId()) {
				case R.id.pilihdelivery_button:
					showSelectCityDialog();
					break;

				default:
					break;
				}
			}
		});
		unggah.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				JSONObject obj = null;
				try {
					obj = compileInfo();
				} catch (JSONException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
//				String imgstr = img_ids.get(0);
//				for (int ii = 1; ii < img_ids.size(); ii++) {
//					imgstr += "," + img_ids.get(ii);
//				}
				// try {
				// // obj.put("images", imgstr);
				// } catch (JSONException e1) {
				// // TODO Auto-generated catch block
				// e1.printStackTrace();
				// }
				try {
					try {
						api.updateProduct(new APIListener() {

							@Override
							public void onSuccess(Object res, Exception e,
									InternetTask task) {
								// TODO Auto-generated method stub
								clearProgressDialog();
								Toast.makeText(EditProductActivity.this,
										"Berhasil dikirim", Toast.LENGTH_SHORT)
										.show();
								finish();
							}

							@Override
							public void onHold(InternetTask task) {
								// TODO Auto-generated method stub
								clearProgressDialog();
								Toast.makeText(EditProductActivity.this,
										"Gagal dikirim", Toast.LENGTH_SHORT)
										.show();
							}

							@Override
							public void onExecute(InternetTask task) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onEnqueue(InternetTask task) {
								// TODO Auto-generated method stub
								showProgressDialog("Edit Produk",
										"Sedang mengunggah produk", null);
							}
						}, obj, product_id);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onServiceReady(APIService api) {
		// TODO Auto-generated method stub
		super.onServiceReady(api);
		this.api = api;
		cate_src = new LinkedList<JSONObject>();
		img_ids = new ArrayList<String>();
		// attribs = new HashMap<String, String>();
		// specs = new HashMap<String, View>();

		product_id = getIntent().getExtras().getString("id");

		try {
			api.listLapak(new APIListener() {

				@Override
				public void onSuccess(Object res, Exception e, InternetTask task) {
					// TODO Auto-generated method stub
					clearProgressDialog();
					ArrayList<Product> prods = (ArrayList<Product>) res;
					for (Product product : prods) {
						if (product.getId().equals(product_id))
							try {
								{
									specs = product.getSpecs();
									decompileInfo(product);
								}
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
					}
				}

				@Override
				public void onHold(InternetTask task) {
					// TODO Auto-generated method stub
					clearProgressDialog();
					finish();
				}

				@Override
				public void onExecute(InternetTask task) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onEnqueue(final InternetTask task) {
					// TODO Auto-generated method stub
					showProgressDialog("Edit Produk",
							"Sedang mengambil info produk",
							new DialogInterface.OnCancelListener() {

								@Override
								public void onCancel(DialogInterface dialog) {
									// TODO Auto-generated method stub
									task.cancelProcess();
									finish();
								}
							});
				}
			}, false, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//
	// @Override
	// public void onCreateContextMenu(ContextMenu menu, View v,
	// ContextMenuInfo menuInfo) {
	// // TODO Auto-generated method stub
	// super.onCreateContextMenu(menu, v, menuInfo);
	// if (v.getId() == R.id.photo_button) {
	// menu.setHeaderTitle("Pilih sumber");
	// menu.add(GROUP_PICTURE_SELECTION, SELECT_IMAGE_GALLERY, 0, "Galeri");
	// menu.add(GROUP_PICTURE_SELECTION, SELECT_IMAGE_CAMERA, 1, "Kamera");
	// } else if (v == kategori) {
	// menu.setHeaderTitle("Pilih kategori :");
	// @SuppressWarnings("unchecked")
	// Iterator<String> iter = cate_src.getLast().keys();
	// while (iter.hasNext()) {
	// String key = iter.next();
	// try {
	// cate_src.getLast().getJSONObject(key);
	// menu.add(GROUP_CATEGORY_SELECTION_NODE, ContextMenu.NONE,
	// ContextMenu.NONE, key);
	// } catch (JSONException e) {
	// // TODO Auto-generated catch block
	// String name = null;
	// try {
	// name = cate_src.getLast().getString(key);
	// } catch (JSONException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// }
	// menu.add(GROUP_CATEGORY_SELECTION_CHILD,
	// Integer.parseInt(key), ContextMenu.NONE, name);
	// }
	// }
	// if (cate_src.size() > 1)
	// menu.add(GROUP_CATEGORY_BACK, ContextMenu.NONE,
	// ContextMenu.NONE, "Kembali");
	// menu.add(GROUP_CATEGORY_CANCEL, ContextMenu.NONE, ContextMenu.NONE,
	// "Batalkan");
	// }
	// }
	//
	// @Override
	// public boolean onContextItemSelected(MenuItem item) {
	// // TODO Auto-generated method stub
	// // TODO Auto-generated method stub
	// int groupCode = item.getGroupId();
	// int idCode = item.getItemId();
	// switch (groupCode) {
	// case GROUP_PICTURE_SELECTION:
	// switch (idCode) {
	// case SELECT_IMAGE_CAMERA:
	// Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	// intent.putExtra("return-data", true);
	// startActivityForResult(intent, PICK_FROM_SOURCE);
	// break;
	// case SELECT_IMAGE_GALLERY:
	// Intent intent2 = new Intent();
	// intent2.setType("image/*");
	// intent2.setAction(Intent.ACTION_GET_CONTENT);
	// intent2.putExtra("return-data", true);
	// startActivityForResult(
	// Intent.createChooser(intent2, "Complete action using"),
	// PICK_FROM_SOURCE);
	// break;
	// }
	// break;
	// case GROUP_CATEGORY_SELECTION_NODE:
	// try {
	// listKategori.add((String) item.getTitle());
	// cate_src.add(cate_src.getLast().getJSONObject(
	// (String) item.getTitle()));
	// } catch (JSONException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// closeContextMenu();
	// openContextMenu(kategori);
	// break;
	// case GROUP_CATEGORY_SELECTION_CHILD:
	//
	// try {
	// String name = cate_src.getLast().getString(
	// Integer.toString(idCode));
	// category_id = Integer.toString(idCode);
	// listKategori.add((String) item.getTitle());
	// kategori.setText(name);
	// } catch (JSONException e3) {
	// // TODO Auto-generated catch block
	// e3.printStackTrace();
	// }
	// api.listAttributes(new APIListener() {
	// @Override
	// public void onSuccess(Object res, Exception e, InternetTask task) {
	// clearProgressDialog();
	// if (e != null) {
	// Toast.makeText(context, e.getMessage(),
	// Toast.LENGTH_SHORT).show();
	// } else {
	// JSONObject src = (JSONObject) res;
	// Iterator<String> iter = src.keys();
	// while (iter.hasNext()) {
	// try {
	// final String key = iter.next();
	// String field;
	// String disp;
	// field = src.getJSONObject(key).getString(
	// "fieldName");
	// disp = src.getJSONObject(key).getString(
	// "displayName");
	// attribs.put(field, disp);
	//
	// JSONArray values = src.getJSONObject(key)
	// .getJSONArray("options");
	//
	// LayoutInflater inflater = LayoutInflater
	// .from(getApplicationContext());
	// View view = inflater.inflate(
	// R.layout.view_product_upload_field,
	// null);
	// TextView tx = (TextView) view
	// .findViewById(R.id.field_text);
	// tx.setText(disp);
	// View vw = null;
	// if (values.length() == 0) {
	// EditText et = (EditText) view
	// .findViewById(R.id.field_edittext);
	// et.setVisibility(EditText.VISIBLE);
	// vw = et;
	// } else {
	//
	// final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
	// getApplicationContext(),
	// android.R.layout.simple_spinner_item);
	// for (int ii = 0; ii < values.length(); ii++) {
	// try {
	// adapter.add(values.getString(ii));
	// } catch (JSONException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// }
	// }
	// adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	// Spinner spin = (Spinner) view
	// .findViewById(R.id.field_spinner);
	// spin.setAdapter(adapter);
	// spin.setVisibility(Spinner.VISIBLE);
	// vw = spin;
	// }
	// tx.setTextColor(Color.BLACK);
	// len.addView(view);
	// specs.put(field, vw);
	// } catch (JSONException e2) {
	// // TODO Auto-generated catch block
	// e2.printStackTrace();
	// }
	// }
	//
	// len.requestLayout();
	// }
	// }
	//
	// @Override
	// public void onHold(InternetTask task) {
	// // TODO Auto-generated method stub
	// clearProgressDialog();
	// finish();
	// }
	//
	// @Override
	// public void onExecute(InternetTask task) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onEnqueue(InternetTask task) {
	// // TODO Auto-generated method stub
	// showProgressDialog("Isian Produk",
	// "Sedang memuat isian produk",
	// new OnCancelListener() {
	//
	// @Override
	// public void onCancel(DialogInterface dialog) {
	// // TODO Auto-generated method stub
	// finish();
	// }
	// });
	// }
	// }, "" + idCode);
	//
	// break;
	// case GROUP_CATEGORY_BACK:
	// cate_src.removeLast();
	// listKategori.remove(listKategori.size() - 1);
	// closeContextMenu();
	// openContextMenu(kategori);
	// break;
	// case GROUP_CATEGORY_CANCEL:
	// finish();
	// break;
	// }
	// return super.onContextItemSelected(item);
	// }
	//

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == PICK_FROM_SOURCE) {
			if (data != null) {
				Uri picUri = data.getData();
				performCrop(picUri);
			}
		} else if (requestCode == PICK_CROP) {
			if (data != null) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					bitmap = extras.getParcelable("data");
					if (bitmap.getHeight() < 300 || bitmap.getWidth() < 300)
						Toast.makeText(getApplicationContext(),
								"Gambar kurang dari ukuran minimal",
								Toast.LENGTH_SHORT).show();
					else {
						try {
							final String s = "image";
							addImage(bitmap, s);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private void performCrop(Uri uri) {
		try {
			// call the standard crop action intent (the user device may not
			// support it)
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			// indicate image type and Uri
			cropIntent.setDataAndType(uri, "image/*");
			// set crop properties
			cropIntent.putExtra("crop", "true");
			// indicate aspect of desired crop
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			// indicate output X and Y
			cropIntent.putExtra("outputX", 300);
			cropIntent.putExtra("outputY", 300);
			// retrieve data on return
			cropIntent.putExtra("return-data", true);
			// start the activity - we handle returning in onActivityResult
			startActivityForResult(cropIntent, PICK_CROP);
		} catch (ActivityNotFoundException anfe) {
			// display an error message
			String errorMessage = "Whoops - your device doesn't support the crop action!";
			Toast toast = Toast
					.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	protected void showSelectCityDialog() {
		final boolean[] checkedColours = new boolean[kota.length];
		int count = kota.length;

		for (int i = 0; i < count; i++)
			checkedColours[i] = selectedKota.contains(kota[i]);

		DialogInterface.OnMultiChoiceClickListener coloursDialogListener = new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which,
					boolean isChecked) {
				ListView dialogListView = ((AlertDialog) dialog).getListView();
				if (isChecked) {

					if (which == 0) {
						if (dialogListView != null) {
							selectedKota.clear();
							// Log.i(tagName,
							// "Dialog List Item Count"+dialogListView.getCount());
							for (int position = 0; position < dialogListView
									.getCount(); position++) {
								if (position > 0) {
									// Log.i(tagName,
									// "Iterating for Adding Positions : "+position);
									// Check items, disable and make them
									// unclickable
									dialogListView.setItemChecked(position,
											true);
									selectedKota.add(kota[position]);
								}
							}
						}

					} else {
						// ini seharusnya bikin si check all unchecked
						checkedColours[0] = false;
						dialogListView.setItemChecked(0, false);
						selectedKota.add(kota[which]);
					}
				}

				else
					selectedKota.remove(kota[which]);

				// onChangeSelectedColours();
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pilih Kota");
		builder.setMultiChoiceItems(kota, checkedColours, coloursDialogListener);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// SHOULD NOW WORK
				Toast.makeText(EditProductActivity.this, "HAHAI",
						Toast.LENGTH_SHORT).show();
				onChangeSelectedCity();
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	protected void onChangeSelectedCity() {
		StringBuilder stringBuilder = new StringBuilder();

		for (CharSequence colour : selectedKota)
			stringBuilder.append(colour + ",");

		// ambilnya di string builder, masukin arraylist aja ya nanti
		city_select.setText(stringBuilder.toString());
	}

	private void addImage(Bitmap b, String g) throws Exception {
		final String s = g;
		LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
		final View view = inflater.inflate(R.layout.view_product_upload_image,
				null);
		ImageView imgview = (ImageView) view.findViewById(R.id.thumb);
		imgview.setImageBitmap(b);
		final Button btn = (Button) view.findViewById(R.id.controlBtn);
		// final TextView imgname = (TextView) view.findViewById(R.id.imgname);
		final ProgressBar progress = (ProgressBar) view
				.findViewById(R.id.progress);
		final ImageView imgOk = (ImageView) view.findViewById(R.id.finishIcon);
		api.createImage(new APIListener() {

			@Override
			public void onSuccess(Object res, Exception e, InternetTask task) {
				// TODO Auto-generated method stub
				progress.setVisibility(ProgressBar.GONE);
				imgOk.setVisibility(ImageView.VISIBLE);
				String obj = (String) res;
				final String id = obj;
				img_ids.add(id);
				btn.setText("HAPUS");
				btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						listImages.removeView(view);
						img_ids.remove(id);
					}
				});

			}

			@Override
			public void onHold(InternetTask task) {
				// TODO Auto-generated method stub
				imgOk.setImageResource(R.drawable.clear_button_image);
				imgOk.setVisibility(ImageView.VISIBLE);
				progress.setVisibility(ProgressBar.GONE);
			}

			@Override
			public void onExecute(InternetTask task) {
				// TODO Auto-generated method stub
				progress.setVisibility(ProgressBar.VISIBLE);
			}

			@Override
			public void onEnqueue(final InternetTask task) {
				// TODO Auto-generated method stub
				btn.setText("BATAL");
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						task.cancelProcess();
					}
				});
			}
		}, b);
		listImages.addView(view);
		listImages.requestLayout();
		listImages.invalidate();
	}

	private JSONObject compileInfo() throws JSONException {
		JSONObject product = new JSONObject();
//		product.put("category_id", category_id);
		product.put("name", namaBarang.getText().toString());
		product.put("price", hargaBarang.getText().toString());
//		product.put("weight", beratBarang.getText().toString());
		product.put("stock", stokBarang.getText().toString());
		product.put("description_bb", deskripsiBarang.getText().toString());
		if (bekas.isChecked())
			product.put("new", "false");
		if (baru.isChecked())
			product.put("new", "true");
		if (nego.isChecked())
			product.put("negotiable", "true");
		else
			product.put("negotiable", "false");

		// JSONObject attrib_det = new JSONObject();
		// for (String k : attribs.keySet()) {
		// View v = specs.get(k);
		// if (v instanceof EditText) {
		// EditText e = (EditText) v;
		// attrib_det.put(k, e.getText().toString());
		// } else if (v instanceof Spinner) {
		// Spinner s = (Spinner) v;
		// attrib_det.put(k, s.getSelectedItem().toString());
		// }
		//
		// }
		// for (String key : specs.keySet()) {
		// attrib_det.put(key, specs.get(key));
		// }
		JSONObject result = new JSONObject();
		result.put("product", product);
		// result.put("product_detail_attribute", attrib_det);
		return result;
	}

	private void decompileInfo(Product p) throws JSONException {
		// product.put("category_id", category_id);
		category_id = p.getCategory();
		kategori.setText(p.getCategory());
		// product.put("name", namaBarang.getText().toString());
		namaBarang.setText(p.getName());
		// product.put("price", hargaBarang.getText().toString());
		hargaBarang.setText(p.getPrice() + "");
		// product.put("weight", beratBarang.getText().toString());
		// beratBarang.setText(p.get)
		// product.put("stock", stokBarang.getText().toString());
		stokBarang.setText(p.getStock() + "");
		// product.put("description_bb", deskripsiBarang.getText().toString());
		deskripsiBarang.setText(p.getDescription());
		if (p.getCondition().equals("new")) {
			baru.setChecked(true);
		} else if (p.getCondition().equals("bekas")) {
			bekas.setChecked(true);
		}

		nego.setChecked(p.isNego());
		// HashMap<String,String> spec = p.getSpecs();
		// LayoutInflater inflater = LayoutInflater
		// .from(getApplicationContext());

		// for (String key : spec.keySet()) {
		// View view = inflater.inflate(
		// R.layout.view_product_upload_field,
		// null);
		// TextView tx = (TextView) view
		// .findViewById(R.id.field_text);
		// tx.setText(key);
		// View v =
		// if (v instanceof EditText) {
		// TextView tv
		// EditText e = (EditText) v;
		// e.sette
		// attrib_det.put(key, e.getText().toString());
		// } else if (v instanceof Spinner) {
		// Spinner s = (Spinner) v;
		// attrib_det.put(k, s.getSelectedItem().toString());
		// }
		//
		// }

		// if (bekas.isChecked())
		// product.put("new", "false");
		// if (baru.isChecked())
		// product.put("new", "true");
		// if (nego.isChecked())
		// product.put("negotiable", "true");
		// else
		// product.put("negotiable", "false");

		// JSONObject attrib_det = new JSONObject();
		// for (String k : attribs.keySet()) {
		// View v = specs.get(k);
		// if (v instanceof EditText) {
		// EditText e = (EditText) v;
		// attrib_det.put(k, e.getText().toString());
		// } else if (v instanceof Spinner) {
		// Spinner s = (Spinner) v;
		// attrib_det.put(k, s.getSelectedItem().toString());
		// }
		//
		// }
		// JSONObject result = new JSONObject();
		// result.put("product", product);
		// result.put("product_detail_attribute", attrib_det);
		// return result;
	}
}
