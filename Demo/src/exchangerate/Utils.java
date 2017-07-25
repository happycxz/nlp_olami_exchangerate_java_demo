package exchangerate;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


public class Utils {
	public static String localIP = getLocalIp();
	public static String localMac = getLocalMac();
	private static CloseableHttpClient httpClient = HttpClients.createDefault();
	
	private static String getLocalIp() {
		String ret = "default_ip";
		Enumeration<?> allNetInterfaces;
		try {
			allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
			return ret;
		}
		
		InetAddress ip = null;
		while(allNetInterfaces.hasMoreElements()) {
			NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
			//System.out.println(netInterface.getName());
			Enumeration<?> addresses = netInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				ip = (InetAddress) addresses.nextElement();
				if (ip != null && ip instanceof Inet4Address) {
					String ret1 = ip.getHostAddress();
					if ("127.0.0.1".equals(ret1)) {
						continue;
					}
					ret = ret1;
					//System.out.println("本机的IP = " + ip.getHostAddress());
				}
			}
		}
		return ret;
	}
	
	private static String getLocalMac() {
		String ret = "default_cust";
		InetAddress ia;
		try {
			ia = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return ret;
		}
		
		//获取网卡，获取地址
		byte[] mac;
		try {
			mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
		} catch (SocketException e) {
			e.printStackTrace();
			return ret;
		}
		//System.out.println("mac数组长度："+mac.length);
		StringBuffer sb = new StringBuffer("");
		for(int i=0; i<mac.length; i++) {
			if(i!=0) {
				sb.append("-");
			}
			//字节转换为整数
			int temp = mac[i]&0xff;
			String str = Integer.toHexString(temp);
			//System.out.println("每8位:"+str);
			if(str.length()==1) {
				sb.append("0"+str);
			}else {
				sb.append(str);
			}
		}
		ret = sb.toString().toUpperCase();
		//System.out.println("本机MAC地址:"+ ret);
		return ret;
	}
	
	public static String MD5String(String str) {
		try {
			MessageDigest msgDigest = MessageDigest.getInstance("MD5");
			msgDigest.reset();
			msgDigest.update(str.getBytes("UTF-8"));
			byte[] byteArrary = msgDigest.digest();
			StringBuffer md5StrBuff = new StringBuffer();
			for (int i = 0; i < byteArrary.length; i++) {
				String tmp = Integer.toHexString(0xFF & byteArrary[i]);
				if (tmp.length() == 1) {
					md5StrBuff.append(0).append(tmp);
				} else {
					md5StrBuff.append(tmp);
				}
			}
			return md5StrBuff.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getCustId() {
		return localMac;
	}
	
	public static void p(Object in) {
		System.out.println(in);
	}

	private static HttpEntity ParseResponse(HttpResponse response) {
		HttpEntity httpEntity = response.getEntity();
		if (httpEntity != null) {
			if (response.getStatusLine().getStatusCode() != 200) {
				try {
					InputStream is = httpEntity.getContent();
					if (is != null)
						is.close();
					return null;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			Header ceheader = httpEntity.getContentEncoding();
			if (ceheader != null) {
				HeaderElement[] codecs = ceheader.getElements();
				for (int i = 0; i < codecs.length; i++) {
					if (codecs[i].getName().equalsIgnoreCase("gzip")) {
						return new GzipDecompressingEntity(httpEntity);
					}
				}
			}
		}
		return httpEntity;
	}

	private static HttpEntity httpGet(final HttpClient httpClient, final String url) {
		HttpGet getReq = new HttpGet(url);
		// add gzip support
		getReq.setHeader("Accept-Encoding", "gzip");
		// getReq.setRequestHeader("Accept-Encoding", "gzip, deflate");
		try {
			getReq.setHeader("Connection", "close");
			HttpResponse response = httpClient.execute(getReq);
			return ParseResponse(response);

		} catch (Exception e) {
			e.printStackTrace();
			getReq.releaseConnection();
			return null;
		}
	}

	public static boolean isEmpty(Object in) {
		if (in == null) {
			return true;
		}
		
		if (in instanceof JSONObject) {
			if (((JSONObject)in).length() == 0) {
				return true;
			}
		} else if (in instanceof String) {
			if (((String) in).isEmpty()) {
				return true;
			}
		}
		
		return false;
	}
	
	public static String httpGet(final String url, final String defaultCharset) {
		HttpEntity entity = httpGet(httpClient, url);
		if (entity == null)
			return "";

		try {
			String sResult = EntityUtils.toString(entity, defaultCharset);
			return sResult;
		} catch (ParseException | IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	private static String appendJson(String str, int count) {
        String retStr = System.getProperty("line.separator", "\n");
        for (int i = 0; i < count; i++) {
            retStr += str;
        }
        return retStr;
    }
	
	/**
	 * 将json转成便于阅读的格式
	 * @param oldJson
	 * @return
	 */
	public static String formatJson(JSONObject old) {
		int i = 0;
		String space = "  ";
		String formatJson = "";
		int indentCount = 0;
		Boolean isStr = false;
		String currChar = "";

		String oldJson = old.toString();
		
		for (i = 0; i < oldJson.length(); i++) {
			currChar = oldJson.substring(i, i + 1);
			switch (currChar) {
			case "{":
			case "[":
				if (!isStr) {
					indentCount++;
					formatJson += currChar + appendJson(space, indentCount);
				} else {
					formatJson += currChar;
				}
				break;
			case "}":
			case "]":
				if (!isStr) {
					indentCount--;
					formatJson += appendJson(space, indentCount) + currChar;
				} else {
					formatJson += currChar;
				}
				break;
			case ",":
				if (!isStr) {
					formatJson += "," + appendJson(space, indentCount);
				} else {
					formatJson += currChar;
				}
				break;
			case ":":
				if (!isStr) {
					formatJson += ": ";
				} else {
					formatJson += currChar;
				}
				break;
			case " ":
			case "\n":
			case "\t":
				if (isStr) {
					formatJson += currChar;
				}
				break;
			case "\"":
				if (i > 0 && !oldJson.substring(i - 1, i).equals("\\")) {
					isStr = !isStr;
				}
				formatJson += currChar;
				break;
			default:
				formatJson += currChar;
				break;
			}
		}
		return formatJson;
	}
	
	public static void main(String[] args) {
		System.out.println("result ip is:" + localIP);
		System.out.println("result mac is:" + localMac);
	}
}
