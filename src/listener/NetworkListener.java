package listener;

import java.io.InputStream;

import org.json.JSONObject;

public interface NetworkListener {
	public abstract void onGivingResult(InputStream res);
}
