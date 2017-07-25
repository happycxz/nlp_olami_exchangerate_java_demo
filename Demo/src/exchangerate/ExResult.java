package exchangerate;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExResult {
	private static final String url = "http://api.k780.com/?";
	private static final String key = "25496";
	// private static final String secret = "8b14fe0d046c6c655ea5b4fafc68dc68";
	private static final String sign = "2cb32e44026b72ed60b5b6e5a55f8db8";

	// 接口有访问次数限制，本地做个缓存
	private static HashMap<String, K780Result> cacheResult = new HashMap<String, K780Result>();

	private static String generateUrl(String srcCur, String dstOrDate, boolean isHistory) {
		StringBuffer paras = new StringBuffer(url);
		if (isHistory == false) {
			paras.append("app=finance.rate&");
			paras.append("scur=" + srcCur);
			paras.append("&");
			paras.append("tcur=" + dstOrDate);
		} else {
			paras.append("app=finance.rate_history&");
			paras.append("curno=" + srcCur);
			paras.append("&");
			paras.append("date=" + dstOrDate);
		}
		paras.append("&appkey=" + key + "&sign=" + sign);
		return paras.toString();
	}
	
	public static float getRate(String srcCur, String dstCur) {
		String cacheKey = srcCur + "|" + dstCur;
		if (cacheResult.containsKey(cacheKey)) {
			return cacheResult.get(cacheKey).rate;
		}

		String url = generateUrl(srcCur, dstCur, false);
		String resultStr = Utils.httpGet(url, "UTF-8");
		// {"success":"1","result":{"status":"ALREADY","scur":"USD","tcur":"CNY","ratenm":"美元/人民币","rate":"6.8894","update":"2017-05-22
		// 10:08:04"}}
		K780Result result = new K780Result(resultStr, false);
		if (result.isSuccess == false) {
			return -1.0F;
		}

		cacheResult.put(cacheKey, result);
		return result.rate;
	}

	public static float getHistoryRate(String srcCur, String date_yyyymmdd) {
		String cacheKey = srcCur + "|" + date_yyyymmdd;
		if (cacheResult.containsKey(cacheKey)) {
			return cacheResult.get(cacheKey).rate;
		}

		String url = generateUrl(srcCur, date_yyyymmdd, true);
		String resultStr = Utils.httpGet(url, "UTF-8");
		// {"success":"1","result":[{"curno":"CNY","days":"2015-12-12","rate":"6.4581","uptime":"2015-12-12 23:59:59"}]}
		K780Result result = new K780Result(resultStr, true);
		if (result.isSuccess == false) {
			return -1.0F;
		}
		
		cacheResult.put(cacheKey, result);
		return result.rate;
	}
	
	/**
	 * 一次运行，获取全部货币名称和代码，分析国家或地区名字，用于补充 currencyTable.csv 中的数据
	 */
	private static void GetAllCurrencyInfo() {
		// String appendKeySign =
		// "&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4";
		String url = "http://api.k780.com/?app=finance.rate_curlist" + "&appkey=" + key + "&sign=" + sign;
		String resultStr = Utils.httpGet(url, "UTF-8");

		String[] otherCountryNames = { "阿联酋", "阿尔巴尼亚", "亚美尼亚", "安哥拉", "阿根廷", "阿塞拜疆", "孟加拉国", "保加利亚", "巴西", "不丹",
				"哥斯达黎加", "厄立特", "埃塞俄比亚", "格鲁吉亚", "加纳", "危地马拉", "洪都拉斯", "克罗地亚", "海地", "匈牙利", "以色列", "吉尔吉斯", "柬埔寨",
				"哈萨克斯坦", "老挝", "立陶宛", "拉脱维亚", "摩洛哥", "马达加斯加", "蒙古	", "马尔代夫", "马来西亚", "尼加拉瓜", "巴拿马", "秘鲁", "新几内亚",
				"波兰", "巴拉圭", "罗马尼亚", "塞拉利昂", "萨尔瓦多", "塔吉克斯坦", "土库曼斯坦", "土耳其", "乌克兰", "乌兹别克斯坦", "委内瑞拉", "南非", "赞比亚",
				"萨摩亚", "瓦努阿图", "蒙古", "阿富汗", "玻利维亚", "毛里塔尼亚" };

		try {
			if (Utils.isEmpty(resultStr)) {
				Utils.p("获取数据失败，接口超时或异常，无数据返回！");
				return;
			}
			JSONObject result = new JSONObject(resultStr);
			if (false == result.optString("success").equals("1")) {
				Utils.p("获取数据失败，检查接口及密钥是否正确，结果：" + resultStr);
				return;
			}

			JSONArray dataList = result.optJSONArray("result");
			Utils.p("## ExResult.GetAllCurrencyInfo 【自动分析】 【正常数据】");
			String otherList = "";
			String singleCharCountryList = "";
			for (int i = 0; i < dataList.length(); i++) {
				JSONObject data = dataList.getJSONObject(i);
				String code = data.optString("curno");
				String name = data.optString("curnm");
				String country = "";

				if ("TOP".equalsIgnoreCase(code)) {
					// 无效记录
					continue;
				}
				if ("CLF".equalsIgnoreCase(code)) {
					// 智利比索 CLP
					continue;
				}

				if (name.endsWith("盾") || name.endsWith("镑") || name.endsWith("元") || name.endsWith("铢")) {
					country = name.substring(0, name.length() - 1);
				} else if (name.endsWith("币")) {
					if (name.contains("离岸人民币")) {
						country = "中国离岸";
					} else if (name.contains("人民币")) {
						country = "中国";
					} else {
						country = name.substring(0, name.length() - 1);
					}
				} else if (name.endsWith("比索") || name.endsWith("法郎") || name.endsWith("卢布") || name.endsWith("卢比")
						|| name.endsWith("先令") || name.endsWith("克朗")) {
					country = name.substring(0, name.length() - 2);
				} else if (name.endsWith("里亚尔") || name.endsWith("第纳尔")) {
					country = name.substring(0, name.length() - 3);
				} else {
					boolean isFound = false;
					for (String countryName : otherCountryNames) {
						if (name.startsWith(countryName)) {
							country = countryName;
							name = name.substring(countryName.length());
							isFound = true;
							break;
						}
					}
					if (isFound == false) {
						otherList += code + "," + name + "," + name + "\n";
						continue;
					}
				}

				if (country.length() == 1) {
					switch (country) {
					case "欧":
						country = "欧洲";
						break;
					case "英":
						country = "英国";
						break;
					case "港":
						country = "香港";
						break;
					case "日":
						country = "日本";
						break;
					case "缅":
						country = "缅甸";
						break;
					case "台":
						country = "台湾";
						break;
					case "美":
						country = "美国";
						break;
					case "泰":
						country = "泰国";
						break;
					default:
						break;
					}
				}

				if (country.length() == 1) {
					singleCharCountryList += code + "," + name + "," + country + "\n";
					continue;
				}

				String line = code + "," + name;
				if (country.isEmpty() == false) {
					line += "," + country;
				}
				Utils.p(line);
			}
			if (singleCharCountryList.isEmpty() == false) {
				Utils.p("\n\n## ExResult.GetAllCurrencyInfo 【自动分析】 【单字国家名，需要手工补齐国家名】");
				Utils.p(singleCharCountryList);
			}
			if (otherList.isEmpty() == false) {
				Utils.p("\n\n## ExResult.GetAllCurrencyInfo 【自动分析】 【判断不出来国家名，需要手工修正】");
				Utils.p(otherList);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// float rate = getRate("USD","CNY");
		// Utils.p(rate);
		// 运行一次，获取货币清单，填充 currencyTable.csv 
		GetAllCurrencyInfo();
	}
}

class K780Result {
	protected boolean isSuccess = false;
	
	String rateStr = "";
	
	String status = "";
	String scur = "";
	String tcur = "";
	String ratenm = "";
	String update = "";
	
	String curno = "";
	String days = "";

	protected float rate = -1.0F;

	public K780Result(String resultStr, boolean isHistory) {
		try {
			Utils.p("new K780Result(): " + resultStr);
			JSONObject jsonRet = new JSONObject(resultStr);
			if (jsonRet == null || jsonRet.isNull("success")) {
				return;
			}

			if ("1".equalsIgnoreCase(jsonRet.optString("success")) == false) {
				return;
			}

			if (isHistory == false) {
				JSONObject jResult = jsonRet.getJSONObject("result");
				this.status = jResult.getString("status");
				this.scur = jResult.getString("scur");
				this.tcur = jResult.getString("tcur");
				this.ratenm = jResult.getString("ratenm");
				this.rateStr = jResult.getString("rate");
				this.update = jResult.getString("update");
			} else {
				JSONObject jResult = jsonRet.getJSONArray("result").getJSONObject(0);
				this.curno = jResult.getString("curno");
				this.days = jResult.getString("days");
				this.rateStr = jResult.getString("rate");
			}
			
			this.rate = Float.parseFloat(this.rateStr);
			this.isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
