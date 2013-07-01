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
import java.util.PriorityQueue;

import listener.APIListener;
import listener.AppListener;
import listener.NetworkListener;
import model.business.Product;
import model.system.InternetTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

public class APIService extends Service {
	private String url = "https://api.bukalapak.com/v1/";
	// SHARED PREFERENCE
	SharedPreferences pref;
	Editor editor;
	int PRIVATE_MODE = 0;
	private static final String IS_LOGIN = "IsLoggedIn";
	private static final String PREF_NAME = "BukaLapak";
	public static final String KEY_ID = "id";
	public static final String KEY_TOKEN = "token";

	static String userid;
	static String token;
	private final IBinder mBinder = new MyBinder();
	private PriorityQueue<InternetTask> backgroundTasks;
	private InternetTask foregroundTask;
	private UploadStatus uploadStatus = UploadStatus.IDLE;

	@Override
	public void onCreate() {
		super.onCreate();

		backgroundTasks = new PriorityQueue<InternetTask>();
		// inisiasi foreground
		foregroundTask = null;
		if(hasActiveLogin())
		{
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
			processBackgroundTask();
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if(hasActiveLogin())
			saveBackGroundProcess();
		super.onDestroy();
	}

	private void executeBackgroundTask(InternetTask task) {
		backgroundTasks.add(task);
		task.tellEnqueued();
		if (uploadStatus != UploadStatus.BUSY)
			processBackgroundTask();
	}

	private void executeForegroundTask(InternetTask task) {

		if (foregroundTask != null && foregroundTask != task) {
			foregroundTask.cancelProcess();
		}
		foregroundTask = task;
		task.tellEnqueued();
		new ForegroundPostData().execute();
	}

	private void processBackgroundTask() {
		new BackgroundPostData().execute();
	}
	
	public void continueRemainingProcess()
	{
		if(uploadStatus == UploadStatus.ONHOLD)
		{
			processBackgroundTask();
		}
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

	private boolean hasActiveLogin() {
		// return (userid != null && token != null);
		return pref.getBoolean(IS_LOGIN, false);
	}

	private void getActiveLogin() {
		pref = getApplicationContext().getSharedPreferences(PREF_NAME,
				PRIVATE_MODE);
		editor = pref.edit();
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
	
	public boolean isActive()
	{
		return userid!=null && token!=null;
	}
	public void saveBackGroundProcess() {
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
	private void loadBackGroundProcess()
			throws StreamCorruptedException, IOException,
			ClassNotFoundException {
		FileInputStream fin;
		fin = new FileInputStream(userid);
		ObjectInputStream iis = new ObjectInputStream(fin);
		@SuppressWarnings("unchecked")
		PriorityQueue<InternetTask> result = (PriorityQueue<InternetTask>) iis
				.readObject();
		if(result!=null && !result.isEmpty()) backgroundTasks = result;
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
			public void onGivingResult(JSONObject res) {
				String status = null;
				try {
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
		backgroundTasks.clear();
		if (foregroundTask != null)
			foregroundTask.cancelProcess();
		// session.logoutUser();
	}

	// //////////////////////////////CATEGORY API//////////////////////////

	// /////////////CATEGORY PART/////////
	public void listCategory(APIListener l) {
		HttpUriRequest req = get(userid, token, "categories.json");
		final InternetTask task = new InternetTask(req,
				APIPriority.FOREGROUND_TASK, l);
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(JSONObject res) {
				try {
					if (res.getString("status").equals("OK"))
						task.tellResult(res.getJSONObject("categories"), null);
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

	// ///////////ATTRIBUTE PART///////////
	public void listAttributes(APIListener l, String id) {
		HttpUriRequest req = get(userid, token, "categories/" + id
				+ "/attributes.json");
		final InternetTask task = new InternetTask(req,
				APIPriority.FOREGROUND_TASK, l);
		task.setNetworkListener(new NetworkListener() {
			@Override
			public void onGivingResult(JSONObject res) {
				try {
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
			public void onGivingResult(JSONObject res) {
				try {
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
			public void onGivingResult(JSONObject res) {
				try {
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
			public void onGivingResult(JSONObject res) {
				try {
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
			public void onGivingResult(JSONObject res) {
				try {
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
			public void onGivingResult(JSONObject res) {
				try {
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
			public void onGivingResult(JSONObject res) {
				try {
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
			public void onGivingResult(JSONObject res) {
				try {
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
			public void onGivingResult(JSONObject res) {
				// TODO Auto-generated method stub
				ArrayList<Product> result = new ArrayList<Product>();
				try {
					if (res.getString("status").equals("OK")) {
						JSONArray products = res.getJSONArray("products");
						for (int ii = 0; ii < products.length(); ii++) {
							JSONObject p = products.getJSONObject(ii);
							String id = p.getString("id");
							Product product = new Product(id);
							product.setName(p.getString("name"));
							product.setPrice(Integer.parseInt(p
									.getString("price")));
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
			public void onGivingResult(JSONObject res) {
				// TODO Auto-generated method stub
				ArrayList<Product> result = new ArrayList<Product>();
				try {
					if (res.getString("status").equals("OK")) {
						JSONArray products = res.getJSONArray("products");
						for (int ii = 0; ii < products.length(); ii++) {
							JSONObject p = products.getJSONObject(ii);
							String id = p.getString("id");
							Product product = new Product(id);
							product.setName(p.getString("name"));
							product.setPrice(Integer.parseInt(p
									.getString("price")));
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
			public void onGivingResult(JSONObject res) {
				// TODO Auto-generated method stub
				try {
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
			public void onGivingResult(JSONObject res) {
				// TODO Auto-generated method stub
				try {
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

	// /////////////////////////////CONFIRM
	// SHIPPING//////////////////////////////////////
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
			public void onGivingResult(JSONObject res) {
				try {
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

	// //////////////////////////////////////SWISS ARMY KNIFE
	// TOOLS/////////////////////
	private String parse(InputStream is) {
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
		return response;
	}

	private String getB64Auth(String login, String pass) {
		String source = login + ":" + pass;
		String ret = "Basic "
				+ Base64.encodeToString(source.getBytes(), Base64.URL_SAFE
						| Base64.NO_WRAP);
		return ret;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	public class MyBinder extends Binder {
		public APIService getService() {
			return APIService.this;
		}
	}

	// //////////////////////////BACKGROUND PUSH/////////////////////////////
	class BackgroundPostData extends AsyncTask<String, String, JSONObject> {
		InternetTask task = null;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if (!backgroundTasks.isEmpty()) {
				task = backgroundTasks.peek();
				ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo i = conMgr.getActiveNetworkInfo();
				if (i == null) {
					uploadStatus = UploadStatus.ONHOLD;
					task.tellHold();
				} else if (i.isConnected()) {
					uploadStatus = UploadStatus.IDLE;
					task.tellStart();
				} else if (i.isAvailable()) {
					uploadStatus = UploadStatus.ONHOLD;
					task.tellHold();
				}
			}
		}

		@Override
		protected JSONObject doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			if (task != null && uploadStatus == UploadStatus.IDLE) {
				uploadStatus = UploadStatus.BUSY;
				task.setAppListener(new AppListener() {
					@Override
					public void onCancel() {
						backgroundTasks.remove(this);
					}
				});
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpResponse response = null;
				try {
					response = httpclient.execute(task.getRequest());
					String temp = parse(response.getEntity().getContent());
					JSONObject result = new JSONObject(temp);
					return result;
				} catch (ClientProtocolException e) {
					uploadStatus = UploadStatus.ONHOLD;
					Log.e("Error API", e.getMessage());
				} catch (IOException e) {
					uploadStatus = UploadStatus.ONHOLD;
					Log.e("Error API", e.getMessage());
				} catch (JSONException e) {
					uploadStatus = UploadStatus.ONHOLD;
					Log.e("Error API", e.getMessage());
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result != null) {
				task.tellFinish(result);
				backgroundTasks.poll();
				uploadStatus = UploadStatus.IDLE;
			} else {
				if (uploadStatus == UploadStatus.ONHOLD)
					task.tellHold();
			}
			if (!backgroundTasks.isEmpty() && uploadStatus == UploadStatus.IDLE) {
				new BackgroundPostData().execute();
			}
		}

	}

	// ////////////////////////FOREGROUND PUSH//////////////////////////
	class ForegroundPostData extends
			AsyncTask<JSONObject, JSONObject, JSONObject> {

		boolean readyToPush = false;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if (foregroundTask != null) {
				ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo i = conMgr.getActiveNetworkInfo();
				if (i == null) {
					foregroundTask.tellHold();
					readyToPush = false;
				} else if (i.isConnected()) {
					foregroundTask.tellStart();
					readyToPush = true;
				} else if (i.isAvailable()) {
					foregroundTask.tellHold();
					readyToPush = false;
				}
			}

		}

		@Override
		protected JSONObject doInBackground(JSONObject... params) {
			JSONObject result = null;
			if (readyToPush) {
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpResponse response = null;
				try {
					response = httpclient.execute(foregroundTask.getRequest());
					String temp = parse(response.getEntity().getContent());
					result = new JSONObject(temp);
				} catch (ClientProtocolException e) {
					Log.e("Error API", e.getMessage());
				} catch (IOException e) {
					Log.e("Error API", e.getMessage());
				} catch (JSONException e) {
					Log.e("Error API", e.getMessage());
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result != null) {
				foregroundTask.tellFinish(result);
			} else {
				foregroundTask.tellHold();
			}
		}
	}
}
