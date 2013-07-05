//APIAPIAPI
package services;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import listener.APIListener;
import listener.AppListener;
import listener.NetworkListener;
import model.business.Product;
import model.system.InternetTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;

public class APIService extends Service {

	private String url = "https://api.bukalapak.com/v1/";
	static String userid;
	static String token;
	private final IBinder mBinder = new MyBinder();
	private PriorityQueue<InternetTask> backgroundTasks;
	private PriorityQueue<InternetTask> foregroundTasks;
	private UploadStatus backgroundStatus;
	private UploadStatus foregroundStatus;
	private NetworkStatus networkStatus;

	@Override
	public void onCreate() {
		super.onCreate();
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
		pref = getApplicationContext().getSharedPreferences(PREF_NAME,PRIVATE_MODE);
		editor = pref.edit();
		backgroundTasks = new PriorityQueue<InternetTask>();
		foregroundTasks = new PriorityQueue<InternetTask>();
		if (hasActiveLogin()) {
			getActiveLogin();
			try {
				loadBackGroundProcess();
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			kickBackgroundTask();
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (hasActiveLogin())
			saveBackGroundProcess();
	}

	private HttpGet requestBlank(String url) throws Exception {
		HttpGet result = new HttpGet(url);
		return result;
	}

	private HttpPost post(String user, String pass, String suburl)
			throws Exception {
		HttpPost result = new HttpPost(url + suburl);
		result.setHeader("Authorization", getB64Auth(user, pass));
		result.setHeader("Content-Type", "application/json");
		return result;
	}

	private HttpPost postWithJSON(String user, String pass, String suburl,
			JSONObject json) throws UnsupportedEncodingException {
		HttpPost result = new HttpPost(url + suburl);
		result.setHeader("Authorization", getB64Auth(user, pass));
		result.setHeader("Content-Type", "application/json");
		result.setHeader("Accept", "application/json");
		if (json != null) {
			StringEntity se = new StringEntity(json.toString());
			result.setEntity(se);
		}
		return result;
	}

	private HttpPost postWithImage(String user, String pass, String suburl,
			Bitmap image) {
		HttpPost result = new HttpPost(url + suburl);
		result.setHeader("Authorization", getB64Auth(user, pass));
		MultipartEntity entity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		if (image != null) {

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			image.compress(CompressFormat.JPEG, 100, stream);
			InputStream is = new ByteArrayInputStream(stream.toByteArray());
			entity.addPart("file", new InputStreamBody(is, "file"));
			result.setEntity(entity);
		}
		return result;
	}

	private HttpGet get(String user, String pass, String suburl) {
		HttpGet result = new HttpGet(url + suburl);
		result.setHeader("Authorization", getB64Auth(user, pass));
		result.setHeader("Content-Type", "application/json");
		return result;
	}

	private HttpDelete delete(String user, String pass, String suburl) {
		HttpDelete result = new HttpDelete(url + suburl);
		result.setHeader("Authorization", getB64Auth(user, pass));
		result.setHeader("Content-Type", "application/json");
		return result;
	}

	private HttpPut put(String user, String pass, String suburl) {
		HttpPut result = new HttpPut(url + suburl);
		result.setHeader("Authorization", getB64Auth(user, pass));
		result.setHeader("Content-Type", "application/json");
		return result;
	}

	private HttpPut putWithJSON(String user, String pass, String suburl,
			JSONObject json) throws UnsupportedEncodingException {
		HttpPut result = new HttpPut(url + suburl);
		result.setHeader("Authorization", getB64Auth(user, pass));
		result.setHeader("Content-Type", "application/json");
		result.setHeader("Accept", "application/json");
		if (json != null) {
			StringEntity se = new StringEntity(json.toString());
			result.setEntity(se);
		}
		return result;
	}

	// ////////////////////////// SHARED PREFERENCE
	// PART/////////////////////////////////
	Editor editor;
	SharedPreferences pref;
	private static int PRIVATE_MODE = 0;
	private static final String IS_LOGIN = "IsLoggedIn";
	private static final String PREF_NAME = "BukaLapak";
	private static final String KEY_ID = "id";
	private static final String KEY_TOKEN = "token";

	private boolean hasActiveLogin() {
		return pref.getBoolean(IS_LOGIN, false);
	}

	private void getActiveLogin() {
		userid = pref.getString(KEY_ID, null);
		token = pref.getString(KEY_TOKEN, null);
	}

	private void setActiveLogin(String userid, String token) {
		// Storing login value as TRUE
		editor.putBoolean(IS_LOGIN, true);
		// Storing id in pref
		editor.putString(KEY_ID, userid);
		// Storing token in pref
		editor.putString(KEY_TOKEN, token);
		// commit changes
		editor.commit();
	}

	private void removeActiveLogin() {
		// Clearing all data from Shared Preferences
		editor.clear();
		editor.commit();
	}

	public boolean isActive() {
		return userid != null && token != null;
	}

	// /////////////////////////////AUTHENTICATION API//////////////////////
	// ///LOGIN//////
	public void retrieveNewAccess(String user, String pwd, APIListener l)
			throws Exception {
		// buat request baru
		HttpUriRequest req = post(user, pwd, "authenticate.json");
		// bundle internet tasj
		final InternetTask task = new InternetTask(req,
				APIPriority.FOREGROUND_TASK, l);
		// set network listenernya
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(InputStream r) {
				String status = null;
				try {
					JSONObject res = convertToJSON(r);
					status = res.getString("status");
					if (status.equals("OK")) {
						userid = res.getString("user_id");
						token = res.getString("token");
						setActiveLogin(userid, token);
						task.tellResult(null, null);
					} else {
						task.tellResult(res.getString("message"),
								new Exception(res.getString("message")));
					}
				} catch (JSONException e) {
					task.tellResult("Error! We are in maintenance.", e);
					Log.e("error", e.getMessage());
				}
			}
		});
		executeForegroundTask(task);
	}

	// ////LOGOUT////////////
	public void removeRecentAccess() {
		APIService.userid = null;
		APIService.token = null;
		removeActiveLogin();
		endBackgroundProcess();
		endForegroundProcess();
	}

	// //////////////////////////////CATEGORY API//////////////////////////

	// /////////////CATEGORY PART/////////
	public void listCategory(APIListener l) {
		HttpUriRequest req = get(userid, token, "categories.json");
		final InternetTask task = new InternetTask(req,
				APIPriority.FOREGROUND_TASK, l);
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(InputStream r) {
				try {
					JSONObject res = convertToJSON(r);
					if (res.getString("status").equals("OK"))
						task.tellResult(res.getJSONObject("categories"), null);
					else
						task.tellResult(null,
								new Exception(res.getString("message")));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					task.tellResult(null,
							e);
				}
			}
		});
		executeForegroundTask(task);
	}

	// ///////////ATTRIBUTE PART///////////
	public void listAttributes(APIListener l, String id) {
		HttpUriRequest req = get(userid, token, "categories/" + id
				+ "/attributes.json");
		final InternetTask task = new InternetTask(req,
				APIPriority.FOREGROUND_TASK, l);
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(InputStream r) {
				try {
					JSONObject res = convertToJSON(r);
					if (res.getString("status").equals("OK"))
						task.tellResult(res.getJSONObject("attributes"), null);
					else
						task.tellResult(null,
								new Exception(res.getString("message")));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					task.tellResult(null, e);
				}
			}
		});
		executeForegroundTask(task);
	}

	// ///////////////////////////IMAGE API////////////////////////////////
	// ////////UPLOAD IMAGE///////////////////
	public InternetTask createImage(APIListener l, Bitmap image)
			throws Exception {
		HttpUriRequest req = postWithImage(userid, token, "images.json", image);
		final InternetTask task = new InternetTask(req,
				APIPriority.UPLOAD_IMAGE_ACTION, l);
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(InputStream r) {
				try {
					JSONObject res = convertToJSON(r);
					if (res.getString("status").equals("OK")) {
						task.tellResult(res.getString("id"), null);
					} else {
						task.tellResult(null,
								new Exception(res.getString("message")));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		executeBackgroundTask(task);
		return task;
	}

	public InternetTask retrieveImage(APIListener l, String url)
			throws Exception {
		HttpUriRequest req = requestBlank(url);
		final InternetTask task = new InternetTask(req,
				APIPriority.FOREGROUND_TASK, l);
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(InputStream r) {
				Bitmap b = BitmapFactory.decodeStream(r);
					task.tellResult(b, null);
			}
		});
		executeForegroundTask(task);
		return task;
	}

	// ///////////////////////RETRIEVE IMAGE//////////////////////////////////

	// ///////////////////////////PRODUCT API//////////////////////////////////
	// ////////////////////UPLOAD PRODUCT///////////////////
	public void createProduct(APIListener l, JSONObject json)
			throws UnsupportedEncodingException {
		HttpUriRequest req = postWithJSON(userid, token, "products.json", json);
		final InternetTask task = new InternetTask(req,
				APIPriority.UPLOAD_PRODUCT_ACTION, l);
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(InputStream r) {
				try {
					JSONObject res = convertToJSON(r);
					if (res.getString("status").equals("OK")) {
						task.tellResult(res.getString("id"), null);
					} else {
						task.tellResult(null,
								new Exception(res.getString("message")));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					task.tellResult(null, e);
				}
			}
		});
		executeBackgroundTask(task);
	}

	// /////////////////////UPDATE PRODUCT///////////////
	public void updateProduct(APIListener l, JSONObject json, String id)
			throws UnsupportedEncodingException {
		HttpUriRequest req = putWithJSON(userid, token, "products/" + id
				+ ".json", json);
		final InternetTask task = new InternetTask(req,
				APIPriority.UPDATE_PRODUCT_ACTION, l);
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(InputStream r) {
				try {
					JSONObject res = convertToJSON(r);
					if (res.getString("status").equals("OK")) {
						task.tellResult(res.getJSONObject("product"), null);
					} else {
						task.tellResult(null,
								new Exception(res.getString("message")));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					task.tellResult(null, e);
				}
			}
		});
		executeBackgroundTask(task);
	}

	// /////////////////////READ PRODUCT////////////////////
	public void readProduct(APIListener l, String id) {
		HttpUriRequest req = get(userid, token, "products/" + id + ".json");
		final InternetTask task = new InternetTask(req,
				APIPriority.FOREGROUND_TASK, l);
		task.setNetworkListener(new NetworkListener() {

			@Override
			public void onGivingResult(InputStream r) {
				try {
					JSONObject res = convertToJSON(r);
					if (res.getString("status").equals("OK")) {
						JSONObject p = res.getJSONObject("product");
						String id = p.getString("id");
						Product product = new Product(id);
						product.setName(p.getString("name"));
						product.setPrice(Integer.parseInt(p.getString("price")));
						task.tellResult(product, null);
					} else {
						task.tellResult(null,
								new Exception(res.getString("message")));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					task.tellResult(null, e);
				}
			}
		});
		executeForegroundTask(task);
	}

	// /////////////////////SET SOLD/////////////////////////
	public void setSoldProduct(APIListener l, String id) {
		HttpUriRequest req = put(userid, token, "products/" + id + "/sold.json");
		final InternetTask task = new InternetTask(req,
				APIPriority.RELIST_PRODUCT_ACTION, l);
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(InputStream r) {
				try {
					JSONObject res = convertToJSON(r);
					if (res.getString("status").equals("OK")) {
						task.tellResult(res.getString("id"), null);
					} else {
						task.tellResult(null,
								new Exception(res.getString("message")));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					task.tellResult(null, e);
				}
			}
		});
		executeBackgroundTask(task);
	}

	// ////////////////RELIST PRODUCT//////////////////////
	public void relistProduct(APIListener l, String id) {
		HttpUriRequest req = put(userid, token, "products/" + id
				+ "/relist.json");
		final InternetTask task = new InternetTask(req,
				APIPriority.RELIST_PRODUCT_ACTION, l);
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(InputStream r) {
				try {
					JSONObject res = convertToJSON(r);
					if (res.getString("status").equals("OK")) {
						task.tellResult(res.getString("id"), null);
					} else {
						task.tellResult(null,
								new Exception(res.getString("message")));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					task.tellResult(null, e);
				}
			}
		});
		executeBackgroundTask(task);
	}

	// /////////////////////DELETEPRODUCT////////////////////
	public void deleteProduct(APIListener l, String id) {
		HttpUriRequest req = delete(userid, token, "products/" + id + ".json");
		final InternetTask task = new InternetTask(req,
				APIPriority.DELETE_PRODUCT_ACTION, l);
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(InputStream r) {
				try {
					JSONObject res = convertToJSON(r);
					if (res.getString("status").equals("OK")) {
						task.tellResult(res.getString("id"), null);
					} else {
						task.tellResult(null,
								new Exception(res.getString("message")));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					task.tellResult(null, e);
				}
			}
		});
		executeBackgroundTask(task);
	}

	// /////////////////////////LIST LAPAK///////////////////
	public void listLapak(APIListener l, boolean available, boolean sold)
			throws Exception {
		String params = "";
		if (available && sold)
			params += "?available=true&sold=true";
		else if (available)
			params += "?available=true";
		else if (sold)
			params += "?sold=true";
		HttpUriRequest req = get(userid, token, "products/mylapak.json"
				+ params);
		final InternetTask task = new InternetTask(req,
				APIPriority.FOREGROUND_TASK, l);
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(InputStream r) {
				// TODO Auto-generated method stub
				ArrayList<Product> result = new ArrayList<Product>();
				try {
					JSONObject res = convertToJSON(r);
					if (res.getString("status").equals("OK")) {
						JSONArray products = res.getJSONArray("products");
						for (int ii = 0; ii < products.length(); ii++) {
							JSONObject p = products.getJSONObject(ii);
							String id = p.getString("id");
							Product product = new Product(id);
							product.setCategory(p.getString("name"));
							JSONArray catlist = p.getJSONArray("category_structure");
							List<String> temp = new ArrayList<String>(); 
							for(int jj = 0; jj < catlist.length();jj++)
							{
								temp.add(catlist.getString(jj));
							}
							product.setCategory_structure(temp);
							product.setName(p.getString("name"));
							product.setCity(p.getString("city"));
							product.setProvince(p.getString("province"));
							product.setPrice(Integer.parseInt(p
									.getString("price")));
							List<String> temp2 = new ArrayList<String>(); 
							JSONArray imglist = p.getJSONArray("images");
							for(int jj = 0; jj < imglist.length();jj++)
							{
								temp2.add(imglist.getString(jj));
							}
							product.setImages(temp2);
							product.setUrl(p.getString("url"));
							product.setDescription(p.getString("desc"));
							product.setCondition(p.getString("condition"));
							product.setNego(Boolean.parseBoolean(p.getString("nego")));
							product.setSeller_name(p.getString("seller_name"));
							product.setPayment_ready(Boolean.parseBoolean(p.getString("payment_ready")));
							JSONObject specs = p.getJSONObject("specs");
							@SuppressWarnings("unchecked")
							Iterator<String> iter = specs.keys();
							HashMap<String,String> temp3 = new HashMap<String, String>();
							while(iter.hasNext())
							{
								String key = iter.next();
								temp3.put(key, specs.getString(key));
							}
							product.setSpecs(temp3);
							result.add(product);
						}
						task.tellResult(result, null);
					} else {
						task.tellResult(null,
								new Exception(res.getString("message")));
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					task.tellResult(null, e);
					Log.e("error", e.getMessage());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					task.tellResult(null, e);
					Log.e("error", e.getMessage());
				}
			}
		});
		executeForegroundTask(task);
	}

	// ///////////////////LIST PRODUCT///////////////////////////////////
	public void listProduct(APIListener l, int page, int per_page, String q)
			throws Exception {
		if (page <= 0)
			page = 1;
		if (per_page <= 0)
			page = 10;
		String params = "?page=" + page + "&per_page=" + per_page;
		if (q != null)
			params += "&q=" + q;
		HttpUriRequest req = get(userid, token, "products.json" + params);
		final InternetTask task = new InternetTask(req,
				APIPriority.FOREGROUND_TASK, l);
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(InputStream r) {
				// TODO Auto-generated method stub
				ArrayList<Product> result = new ArrayList<Product>();
				try {
					JSONObject res = convertToJSON(r);
					if (res.getString("status").equals("OK")) {
						JSONArray products = res.getJSONArray("products");
						for (int ii = 0; ii < products.length(); ii++) {
							JSONObject p = products.getJSONObject(ii);
							String id = p.getString("id");
							Product product = new Product(id);
							product.setName(p.getString("name"));
							product.setPrice(Integer.parseInt(p
									.getString("price")));
							product.setStock(Integer.parseInt(p
									.getString("stock")));
							result.add(product);
						}
						task.tellResult(result, null);
					} else {
						task.tellResult(null,
								new Exception(res.getString("message")));
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					task.tellResult(null, e);
					Log.e("error", e.getMessage());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					task.tellResult(null, e);
					Log.e("error", e.getMessage());
				}
			}
		});
		executeForegroundTask(task);
	}

	// /////////////////////////////////TRANSACTION
	// API//////////////////////////////
	public void listTransaction(APIListener l, int page, int per_page)
			throws Exception {
		if (page <= 0)
			page = 1;
		if (per_page <= 0)
			page = 10;
		String params = "?page=" + page + "&per_page=" + per_page;
		HttpUriRequest req = get(userid, token, "transactions.json" + params);
		final InternetTask task = new InternetTask(req,
				APIPriority.FOREGROUND_TASK, l);
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(InputStream r) {
				// TODO Auto-generated method stub
				try {
					JSONObject res = convertToJSON(r);
					if (res.getString("status").equals("OK")) {
						task.tellResult(res.getJSONObject("transactions"), null);
					} else {
						task.tellResult(null,
								new Exception(res.getString("message")));
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					task.tellResult(null, e);
					Log.e("error", e.getMessage());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					task.tellResult(null, e);
					Log.e("error", e.getMessage());
				}
			}
		});
		executeForegroundTask(task);
	}

	// //////////////////////GET TRANSACTION///////////////////////////////
	public void getTransaction(APIListener l, String id) throws Exception {
		HttpUriRequest req = get(userid, token, "transactions.json/" + id
				+ ".json");
		final InternetTask task = new InternetTask(req,
				APIPriority.FOREGROUND_TASK, l);
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(InputStream r) {
				// TODO Auto-generated method stub
				try {
					JSONObject res = convertToJSON(r);
					if (res.getString("status").equals("OK")) {
						task.tellResult(res.getJSONObject("transactions"), null);
					} else {
						task.tellResult(null,
								new Exception(res.getString("message")));
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					task.tellResult(null, e);
					Log.e("error", e.getMessage());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					task.tellResult(null, e);
					Log.e("error", e.getMessage());
				}
			}
		});
		executeForegroundTask(task);
	}

	// //////CONFIRM SHIPPING//////////////////////////////////////
	public void confirmShipping(APIListener l, String trans_id,
			String ship_code, String new_courier) throws JSONException,
			UnsupportedEncodingException {
		JSONObject confirmationObj = new JSONObject();
		confirmationObj.put("transaction_id", trans_id);
		confirmationObj.put("shipping_code", ship_code);
		confirmationObj.put("new_courier", new_courier);
		JSONObject json = new JSONObject();
		json.put("payment_shipping", confirmationObj);
		HttpUriRequest req = postWithJSON(userid, token,
				"transactions/confirm_shipping.json", json);
		final InternetTask task = new InternetTask(req,
				APIPriority.CONFIRM_SHIPPING_ACTION, l);
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(InputStream r) {
				try {
					JSONObject res = convertToJSON(r);
					if (res.getString("status").equals("OK")) {
						task.tellResult(res.getString("id"), null);
					} else {
						task.tellResult(null,
								new Exception(res.getString("message")));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					task.tellResult(null, e);
				}
			}
		});
		executeBackgroundTask(task);
	}

	// ////////////////SWISS ARMY KNIFE TOOLS/////////////////////
	private Bitmap convertToBitmap(InputStream is) {
		return BitmapFactory.decodeStream(is);
	}

	private JSONObject convertToJSON(InputStream is) throws JSONException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(
					new InputStreamReader(is, "iso-8859-1"), 8);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String response = sb.toString();
		return new JSONObject(response);
	}

	private String getB64Auth(String login, String pass) {
		String source = login + ":" + pass;
		String ret = "Basic "
				+ Base64.encodeToString(source.getBytes(), Base64.URL_SAFE
						| Base64.NO_WRAP);
		return ret;
	}

	// //////////////////////////PUBLIC RELATION : BINDING///////////////
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	//
	public class MyBinder extends Binder {
		public APIService getService() {
			return APIService.this;
		}
	}

	// ///////////////////////////PROCESS CONTROL////////////////////////////

	private void executeBackgroundTask(InternetTask task) {
		backgroundTasks.add(task);
		task.tellEnqueued();
		kickBackgroundTask();
	}

	private void executeForegroundTask(InternetTask task) {
		foregroundTasks.add(task);
		task.tellEnqueued();
		kickForegroundTask();
	}

	public void continueRemainingProcess() {
		kickBackgroundTask();
	}

	private void kickBackgroundTask() {
		if (backgroundStatus != UploadStatus.BUSY)
			new BackgroundPushData().execute();
	}

	private void kickForegroundTask() {
		if (foregroundStatus != UploadStatus.BUSY)
			new ForegroundPushData().execute();
	}

	// //////////////////////////BACKGROUND PUSH/////////////////////////////
	class BackgroundPushData extends AsyncTask<String, String, InputStream> {
		InternetTask task = null;
		Exception ex = null;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if (!backgroundTasks.isEmpty()) {
				task = backgroundTasks.peek();
				task.tellStart();
				networkStatusGovernor();
			}
		}

		@Override
		protected InputStream doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			if (task != null) {
				if (networkStatus == NetworkStatus.OK) {
					backgroundStatus = UploadStatus.BUSY;
					task.setAppListener(new AppListener() {
						@Override
						public void onCancel() {
							backgroundTasks.remove(task);
						}
					});
					try {
//						HttpParams httpParameters = new BasicHttpParams();
//						int timeoutConnection = 3000;
//						HttpConnectionParams.setConnectionTimeout(
//								httpParameters, timeoutConnection);
//						int timeoutSocket = 5000;
//						HttpConnectionParams.setSoTimeout(httpParameters,
//								timeoutSocket);
//						DefaultHttpClient httpclient = new DefaultHttpClient(
//								httpParameters);
						DefaultHttpClient httpclient = new DefaultHttpClient();
						HttpResponse response = httpclient.execute(task
								.getRequest());
						return response.getEntity().getContent();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						ex = new Exception("Kesalahan pada jaringan");
						return null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						ex = new Exception("Kesalahan pada jaringan");
						return null;
					}
				} else {
					ex = new Exception("Kesalahan pada jaringan");
					return null;
				}
			} else {
				return null;
			}
		}

		@Override
		protected void onPostExecute(InputStream result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result != null) {
				task.tellFinish(result);
				backgroundTasks.poll();
				backgroundStatus = UploadStatus.IDLE;
				// ///GO TO NEXT QUEUE
				if (!backgroundTasks.isEmpty()) {
					new BackgroundPushData().execute();
				}
			} else {
				if (ex != null) {
					task.tellResult(null, ex);
					backgroundStatus = UploadStatus.ONHOLD;
				} else {
					backgroundStatus = UploadStatus.IDLE;
				}
			}

		}
	}

	private void saveBackGroundProcess() {
		FileOutputStream fout;
		try {
			fout = new FileOutputStream(userid);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(backgroundTasks);
			oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void loadBackGroundProcess() throws StreamCorruptedException,
			IOException, ClassNotFoundException {
		FileInputStream fin;
		fin = new FileInputStream(userid);
		ObjectInputStream iis = new ObjectInputStream(fin);
		@SuppressWarnings("unchecked")
		PriorityQueue<InternetTask> result = (PriorityQueue<InternetTask>) iis
				.readObject();
		if (result != null && !result.isEmpty())
			backgroundTasks.addAll(result);
	}

	private void endBackgroundProcess() {
		if (!backgroundTasks.isEmpty()) {
			backgroundTasks.peek().cancelProcess();
			backgroundTasks.clear();
		}
	}

	// //////////////////////////FOREGROUND PROCESS
	// WORKSPACE////////////////////////

	class ForegroundPushData extends AsyncTask<String, String, InputStream> {
		InternetTask task = null;
		Exception ex = null;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if (!foregroundTasks.isEmpty()) {
				task = foregroundTasks.peek();
				task.tellStart();
				networkStatusGovernor();
			}
		}

		@Override
		protected InputStream doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			if (task != null) {
				if (networkStatus == NetworkStatus.OK) {
					foregroundStatus = UploadStatus.BUSY;
					task.setAppListener(new AppListener() {
						@Override
						public void onCancel() {
							foregroundTasks.remove(task);
						}
					});
					try {
//						HttpParams httpParameters = new BasicHttpParams();
//						int timeoutConnection = 3000;
//						HttpConnectionParams.setConnectionTimeout(
//								httpParameters, timeoutConnection);
//						int timeoutSocket = 5000;
//						HttpConnectionParams.setSoTimeout(httpParameters,
//								timeoutSocket);
//						DefaultHttpClient httpclient = new DefaultHttpClient(
//								httpParameters);
						DefaultHttpClient httpclient = new DefaultHttpClient();
						HttpResponse response = httpclient.execute(task
								.getRequest());
						return response.getEntity().getContent();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						ex = new Exception("Kesalahan pada jaringan");
						return null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						ex = new Exception("Kesalahan pada jaringan");
						return null;
					}
				} else {
					ex = new Exception("Kesalahan pada jaringan");
					return null;
				}
			} else {
				return null;
			}
		}

		@Override
		protected void onPostExecute(InputStream result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result != null) {
				task.tellFinish(result);
				foregroundTasks.poll();
				foregroundStatus = UploadStatus.IDLE;
				// ///GO TO NEXT QUEUE
				if (!foregroundTasks.isEmpty()) {
					new ForegroundPushData().execute();
				}
			} else {
				if (ex != null) {
					task.tellResult(null, ex);
					foregroundStatus = UploadStatus.ONHOLD;
					endForegroundProcess();
				} else {
					foregroundStatus = UploadStatus.IDLE;
				}
			}

		}
	}

	public void endForegroundProcess() {
		if (!foregroundTasks.isEmpty()) {
			foregroundTasks.peek();
			foregroundTasks.clear();
		}
	}

	public void startForegrounfProcess() {
		if (!foregroundTasks.isEmpty()) {
			foregroundTasks.peek();
			foregroundTasks.clear();
		}
	}

	// //////////////////////////NETWORK TOOLS///////////////////////////////
	private void networkStatusGovernor() {
		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo i = conMgr.getActiveNetworkInfo();
		if (i == null) {
			networkStatus = NetworkStatus.NOTOK;
		} else if (i.isConnected()) {
			networkStatus = NetworkStatus.OK;
		} else if (i.isAvailable()) {
			networkStatus = NetworkStatus.NOTOK;
		}
	}

}
