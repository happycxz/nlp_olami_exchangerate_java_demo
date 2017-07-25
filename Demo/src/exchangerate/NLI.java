package exchangerate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NLI {

	private static final String url = "https://cn.olami.ai/cloudservice/api";
	private static final String Appkey = "892626f32837458b9f211a774e2e168a";
	private static final String Appsecret = "28aab610fc0040a29686330ff27ee8bb";
	private static final String api = "nli";
	
	private JSONObject nliResult = new JSONObject();
	
	/**
	 * 原始nli结果
	 * @return
	 */
	public JSONObject getNliResult() {
		return nliResult;
	}

	/**
	 * 解析之后的semantic
	 * @return
	 */
	public static JSONObject parseSemantic(JSONObject semanticJson) {
		JSONObject ret = new JSONObject();
		if (semanticJson == null) {
			return ret;
		}
		
		if ("error".equalsIgnoreCase(semanticJson.optString("status"))) {
			//错误
			return ret;
		}
		
		if (false == "ok".equalsIgnoreCase(semanticJson.optString("status"))) {
			//其它错误
			return ret;
		}
		
		try {
			JSONObject jsonTemp = semanticJson.getJSONObject("data");
			JSONArray jsonArrayTemp = jsonTemp.getJSONArray("nli");
			jsonTemp = jsonArrayTemp.getJSONObject(0);
			if (jsonTemp.has("semantic") == false) {
				// 听不懂
				Utils.p("NLI.getSemJson() 语义理解失败: " + jsonTemp);
				return ret;
			}
			jsonArrayTemp = jsonTemp.getJSONArray("semantic");
			ret = jsonArrayTemp.getJSONObject(0);
		} catch (JSONException e) {
			Utils.p("NLI.getSemJson() get semantic JSONException BY: " + semanticJson.toString());
		} catch (NullPointerException e2) {
			Utils.p("NLI.getSemJson() get semantic NullPointerException BY: " + semanticJson.toString());
		} catch (Exception e3) {
			Utils.p("NLI.getSemJson() get semantic Exception BY: " + semanticJson.toString());
		}
		return ret;
	}

	public NLI(String txt) {
		nliResult = getSemantic(txt);
	}
	
	public static void main(String[] args) {
		Utils.p(getSemantic("1美元等于多少法郎"));
	}
	
	private static String generateSign(long timestamp) {
		String sign = Appsecret + "api=" + api + "appkey=" + Appkey + "timestamp=" + timestamp + Appsecret;
		return Utils.MD5String(sign);
	}
	
	private static JSONObject getSemantic(String txt) {
		JSONObject ret = new JSONObject();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("appkey", Appkey));
		params.add(new BasicNameValuePair("api", api));
		
		long timestamp = Calendar.getInstance().getTimeInMillis();
		params.add(new BasicNameValuePair("timestamp", String.valueOf(timestamp)));
		params.add(new BasicNameValuePair("sign", generateSign(timestamp)));
		
		JSONObject request = new JSONObject();
		JSONObject data = new JSONObject();
		try {
			data.put("input_type", 0);
			data.put("text", txt);
			
			request.put("data_type", "stt");
			request.put("data", data);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return ret;
		}
		params.add(new BasicNameValuePair("rq", request.toString()));
		params.add(new BasicNameValuePair("cusid", Utils.getCustId()));

		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(url);
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			CloseableHttpResponse response = httpclient.execute(httppost);
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					String contnt = EntityUtils.toString(entity);
					//System.out.println("Response content: " + contnt);
					ret = new JSONObject(contnt);
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ret;
		} finally {
			try {
				httpclient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        
		return ret;
	}
	
}

class Semantic {

	public String modifier = "";
	public Map<String, String> slots = new HashMap<String, String>();
	
	public Semantic(JSONObject sem) {
		this.modifier = sem.optString("modifier");

		JSONArray slotsJson = sem.optJSONArray("slots");
		for (int i = 0; i < slotsJson.length(); i++) {
			try {
				JSONObject slot = slotsJson.getJSONObject(i);
				this.slots.put(slot.getString("name"), slot.getString("value"));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}
	
	public static void main(String[] args) {

	}
}