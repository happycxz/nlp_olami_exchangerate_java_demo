# nlp_olami_exchangerate_java_demo
a java demo of nature language process of exchange rate.


#自然语言理解 之 汇率换算
##用olami开放语义实现汇率换算功能


**>>>>>>>>>>>>>>>>>>>>>>>> 欢迎转载 <<<<<<<<<<<<<<<<<<<<<<<<**

**本文原地址:[http://blog.csdn.net/happycxz/article/details/73223916][csdn本文链接]**


###基础资源及工具
1. eclipse + windowbuilder插件
2. jdk 1.7
3. 免费开放语义接口平台 olami.ai
4. 参考开源代码 [https://github.com/codingmonster/olami-nlu-samples-java](https://github.com/codingmonster/olami-nlu-samples-java "olami date小应用").


###实现功能
通过java客户端界面，输入汇率换算相关句子，可以查到汇率和货币相关的结果。

###界面展示
![alt text](http://img.blog.csdn.net/20170614140819894?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaGFwcHljeHo=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center "APP界面")

APP内预置部分支持的说法，可点击“换一句”切换。点击“理解”即发送对应文本到olami开放语义服务器。
左下角是olami开放语义服务器返回的语义结构，右下角是我用对应拿到的语义，找了一个免费的汇率接口，获取到对应的数据展示出来。

###资源下载
点击 **[在CSDN免资源下载全部资料：源码、eclipse工程、可执行JAR包][eclipse源码工程 + jar包]** 。

点击 **[在CSDN免资源下载：可执行程序xx.jar][jar包]** 。

点击 **[通过百度云下载此DEMO全部资料][百度云]** 。

点击：**[github下载DEMO源码](https://github.com/happycxz/nlp_olami_exchangerate_java_demo)**

###源代码简介
1. CurrencyTable.java

    汇率货币代码、名称、别名，对应国家或地区名称、别名管理。主要是对currencyTable.csv配置文件中的配置数据解析。

------------------------------------------------------------------------------------------------
	package exchangerate;
	
	import java.io.BufferedReader;
	import java.io.FileNotFoundException;
	import java.io.IOException;
	import java.io.InputStream;
	import java.io.InputStreamReader;
	import java.util.Arrays;
	import java.util.HashMap;
	import java.util.HashSet;
	import java.util.Map;
	import java.util.Set;
	
	public class CurrencyTable {
	
		private static Map<String, Currency> codeMap = new HashMap<String, Currency>();
		private static Map<String, Currency> curMap = new HashMap<String, Currency>();
		private static Map<String, Currency> cntMap = new HashMap<String, Currency>();
	
		static {
			try {
				InputStream is = CurrencyTable.class.getResourceAsStream("/exchangerate/currencyTable.csv");
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "gbk"));
	
				String line;
				while ((line = reader.readLine()) != null) {
					String lineTrim = line.trim();
					if (lineTrim.startsWith("#") || lineTrim.isEmpty()) {
						continue;
					}
	
					String[] splitTmp = lineTrim.split(",");
	
					if (splitTmp.length != 3) {
						Utils.p("WARN: currencyTable.csv has invalid line:" + line);
						continue;
					}
	
					Currency cur = new Currency(splitTmp);
					//Utils.p("Currency found from currencyTable.csv :" + cur.getCurrencyCode());
					
					if (codeMap.containsKey(cur.getCurrencyCode())) {
						//发现重复CODE，合并
						codeMap.get(cur.getCurrencyCode()).mergeToThis(cur);
						Utils.p("CODE重复(将合并): " + cur.getCurrencyCode());
					} else {
						codeMap.put(cur.getCurrencyCode(), cur);
					}
					
					//更新 cur
					cur = codeMap.get(cur.getCurrencyCode());
					for (String info : cur.getAliasCurrencyNames()) {
						if (curMap.containsKey(info)) {
							//Utils.p("--> CURRENCY重复:" + info);
						} else {
							curMap.put(info, cur);
						}
					}
					for (String info : cur.getAliasCountryNames()) {
						if (cntMap.containsKey(info)) {
							//Utils.p("--> Country重复:" + info);
						} else {
							cntMap.put(info, cur);
						}
					}
				}
				reader.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			} finally {
	
			}
		}
	
		/**
		 * 货币名查代码
		 * @param currency
		 * @return
		 */
		public static String currency2code(String currency) {
			if (curMap.containsKey(currency) == false) {
				return "";
			}
	
			return curMap.get(currency).getCurrencyCode();
		}
		
		/**
		 * xx国的货币名
		 * @param country
		 * @return
		 */
		public static String getCurrencyFromCountry(String country) {
			if (cntMap.containsKey(country) == false) {
				return "";
			}
	
			return cntMap.get(country).getPreferredCurrencyName();
		}
		
		/**
		 * xx是什么国家的货币
		 * @param currency
		 * @return
		 */
		public static String getCountryFromCurrency(String currency) {
			if (curMap.containsKey(currency) == false) {
				return "";
			}
			
			return curMap.get(currency).getPreferredCountryName();
		}
		
		public static void main(String[] args) {
			Utils.p("done");
		}
	}
	
	class Currency {
		private String currencyCode = "";
	
		private String preferredCurrencyName = "";
		private Set<String> aliasCurrencyNames = new HashSet<String>();
	
		private String preferredCountryName = "";
		private Set<String> aliasCountryNames = new HashSet<String>();
	
		public Currency(String[] info) {
			if (info == null || info.length != 3) {
				return;
			}
	
			String[] curArray = info[1].trim().split("\\|");
			String[] cntArray = info[2].trim().split("\\|");
			if (curArray == null || cntArray == null || curArray.length == 0 || cntArray.length == 0) {
				return;
			}
	
			this.currencyCode = info[0].trim();
			this.preferredCurrencyName = curArray[0];
			this.aliasCurrencyNames = new HashSet<String>(Arrays.asList(curArray));
			this.preferredCountryName = cntArray[0];
			this.aliasCountryNames = new HashSet<String>(Arrays.asList(cntArray));
		}
	
		public void mergeToThis (Currency newCur) {
			this.aliasCountryNames.addAll(newCur.getAliasCountryNames());
			this.aliasCurrencyNames.addAll(newCur.getAliasCurrencyNames());
		}
		
		public String getPreferredCurrencyName() {
			return preferredCurrencyName;
		}
	
		public Set<String> getAliasCurrencyNames() {
			return aliasCurrencyNames;
		}
	
		public String getPreferredCountryName() {
			return preferredCountryName;
		}
	
		public Set<String> getAliasCountryNames() {
			return aliasCountryNames;
		}
	
		public String getCurrencyCode() {
			return currencyCode;
		}
	}

------------------------------------------------------------------------------------------------


2. ExHandler.java

	处理语义和结果的类。

------------------------------------------------------------------------------------------------
	package exchangerate;
	
	import java.text.SimpleDateFormat;
	import java.util.Calendar;
	import java.util.Date;
	
	import org.json.JSONArray;
	import org.json.JSONException;
	import org.json.JSONObject;
	
	public class ExHandler {
		private String input = "";
		
		private JSONObject nliResultJson = null;
		private JSONObject resultJson = null;
		
		//计算后的值，缓存用于界面显示
		private float calculatedSrc = 0;
		private float calculatedDst = 0;
		
		private static final SimpleDateFormat yyyymmdd_sd = new SimpleDateFormat("yyyyMMdd");
		
		protected DetailContext detailContext = new DetailContext(false);
		
		public ExHandler(String input) {
			this.input = input;
			this.nliResultJson = this.getNliResult();
		}
		
		private JSONObject getNliResult() {
			if (Utils.isEmpty(nliResultJson) == false) {
				return nliResultJson;
			}
	
			return new NLI(this.input).getNliResult();
		}
		
		public String getNliResultForShow() {
			JSONObject ret = getParsedSemantic();
			if (Utils.isEmpty(ret)) {
				ret = this.getNliResult();
			}
			
			return Utils.formatJson(ret);
		}
		
		public String getResultString() {
			if (this.resultJson == null) {
				this.resultJson = this.getResult();
			}
			
			if (false == "ok".equalsIgnoreCase(this.resultJson.optString("status"))) {
				return "出错！" + this.resultJson.optString("info");
			}
			
			return this.resultJson.optString("info");
		}
		
		private JSONObject getParsedSemantic() {
			return NLI.parseSemantic(this.getNliResult());
		}
		
		private JSONObject getResult() {
			if (Utils.isEmpty(this.getParsedSemantic())) {
				return errResult("获取语义失败！");
			}
			
			String modifier = getModifier();
			
			String srcNumStr = getSlotValue("src_number");
			String dstNumStr = getSlotValue("dst_number");
			String srcMoney = getSlotValue("src_money");
			String dstMoney = getSlotValue("dst_money");
			String srcPlace = getSlotValue("src_place");
			String dstPlace = getSlotValue("dst_place");
			
			String time = getSlotValue("time");
			
			// "100" to 100.0
			float srcNum = str2float(srcNumStr);
			float dstNum = str2float(dstNumStr);
			
			// "人民币" to "CNY"
			String srcMoneyCode = currency2code(srcMoney);
			String dstMoneyCode = currency2code(dstMoney);
			
			float rate = -1;
			
			if ("can".equalsIgnoreCase(modifier) && (Utils.isEmpty(dstMoney) == false || Utils.isEmpty(srcMoney) == false)) {
				//把 “你知道港元跟日元之间的汇率吗” 这种语义直接转成 “港元跟日元之间的汇率” 语义
				modifier = "query";
			}
			
			switch (modifier) {
			case "query" :
				if (Utils.isEmpty(srcPlace) == false || Utils.isEmpty(dstPlace) == false) {
					//查新加坡汇率
					//我要去德国，帮我查一下汇率
					String country = Utils.isEmpty(srcPlace) ? dstPlace : srcPlace;
					String dstM = CurrencyTable.getCurrencyFromCountry(country);
					String dstCode = currency2code(dstM);
					if (Utils.isEmpty(dstCode)) {
						return errResult("不支持【" + country + "】的货币汇率查询。");
					}
					
					if ("CNY".equalsIgnoreCase(dstCode)) {
						//return okResult("不出境不需要兑外汇。");
						dstCode = "USD";
						dstM = "美元";
					}
					
					rate = getRate("CNY", dstCode);
					
					detailContext = new DetailContext(true, 1, rate, "CNY", dstCode);
					return okResult("1人民币可兑换【" + rate + dstM + "】");
				}
				
	
				if (Utils.isEmpty(srcMoneyCode) == false) {
					String srcCode = srcMoneyCode;
					String dstCode = dstMoneyCode;
					String srcM = srcMoney;
					String dstM = dstMoney;
					if (Utils.isEmpty(dstMoneyCode) == false) {
						//美元兑换成人民币
					} else {
						//美元的汇率是多少
						if ("CNY".equalsIgnoreCase(srcCode)) {
							dstCode = "USD";
							dstM = "美元";
						} else {
							dstCode = "CNY";
							dstM = "人民币";
						}
					}
					
					rate = getRate(srcCode, dstCode);
					
					detailContext = new DetailContext(true, 1, rate, srcCode, dstCode);
					return okResult("1" + srcM + "可兑换【" + rate + dstM + "】");
				}
				
				if (Utils.isEmpty(time) == false) {
					//今日汇率
					rate = getDayRate("CNY", time);
					
					detailContext = new DetailContext(true, 1, rate, "USD", "CNY");
					return okResult("【" + time + "】美元兑人民币汇率【" + rate + "】");
				}
	
				rate = getRate("USD", "CNY");
				
				detailContext = new DetailContext(true, 1, rate, "USD", "CNY");
				return okResult("美元兑人民币汇率【" + rate + "】");
			case "query_change" : 
				if (Utils.isEmpty(time) == false) {
					//今天的汇率有什么变化
					rate = getDayRate("CNY", time);
					
					detailContext = new DetailContext(true, 1, rate, "USD", "CNY");
					return okResult("【" + time + "】美元兑人民币汇率【" + rate + "】");
				}
	
				rate = getRate("USD", "CNY");
				
				detailContext = new DetailContext(true, 1, rate, "USD", "CNY");
				return okResult("美元兑人民币汇率【" + rate + "】");
			case "use_place" :
			case "use_place_which" :
				//中国用什么货币
				if (Utils.isEmpty(srcPlace) == false) {
					String preferredCurrency = CurrencyTable.getCurrencyFromCountry(srcPlace);
					if (Utils.isEmpty(preferredCurrency) == false) {
						
						detailContext = new DetailContext(false);
						return okResult(srcPlace + "的货币是【" + preferredCurrency + "】");
					} else {
						
					}
				}
				
				//欧元在哪些国家使用
				if (Utils.isEmpty(srcMoney)) {
					return errResult("语义不完整！");
				}
				String preferredCountryName = CurrencyTable.getCountryFromCurrency(srcMoney);
				if (Utils.isEmpty(preferredCountryName) == false) {
					
					detailContext = new DetailContext(false);
					return okResult(srcMoney + "是【" + preferredCountryName + "】的货币");
				}
				
				return errResult("语义不完整！");
			case "use_place_unit" :
				//新加坡的货币是什么
				if (Utils.isEmpty(srcPlace)) {
					return errResult("不支持你说的这个地方。");
				}
				
				String preferredCurrency = CurrencyTable.getCurrencyFromCountry(srcPlace);
				if (Utils.isEmpty(preferredCurrency)) {
					return errResult("没有查到你说的【" + srcPlace + "】这个地方的货币。");
				}
				
				detailContext = new DetailContext(false);
				return okResult(srcPlace + "使用的货币是【" + preferredCurrency + "】");
			case "can" :
			case "can_type" :
				
				detailContext = new DetailContext(false);
				return okResult("全世界货币都支持，点击【换一句】试试吧！");
			case "convert" :
				// convert exchangerate semantics
				if (Utils.isEmpty(srcMoney) || Utils.isEmpty(dstMoney)) {
					return errResult("语义不完整！");
				}
				
				if (Utils.isEmpty(srcMoneyCode)) {
					return errResult("不支持【" + srcMoney + "】");
				}
				
				if (Utils.isEmpty(dstMoneyCode)) {
					return errResult("不支持【" + dstMoney + "】");
				}
				
				
				rate = getRate(srcMoneyCode, dstMoneyCode);
				if (Utils.isEmpty(srcNumStr) == false) {
					calculatedDst = rate * srcNum;
					
					detailContext = new DetailContext(true, srcNum, calculatedDst, srcMoneyCode, dstMoneyCode);
					return okResult(srcNumStr + srcMoney + "可兑换【" + calculatedDst + dstMoney + "】");
				}
				
				if (Utils.isEmpty(dstNumStr) == false) {
					calculatedSrc = dstNum / rate;
					
					detailContext = new DetailContext(true, calculatedSrc, dstNum, srcMoneyCode, dstMoneyCode);
					return okResult(dstNumStr + dstMoney + "用 【" + calculatedSrc + "" + srcMoney + "】可以兑换");
				}
				break;
			case "query_place" :
				//中国与各国货币的汇率表
			default:
				//errResult("对不起！此功能暂未支持！");
				rate = getRate("USD", "CNY");
				detailContext = new DetailContext(true, 1, rate, "USD", "CNY");
				return okResult("美元兑人民币：" + rate);
			}
			
			return errResult("未知错误！");
		}
		
		private static String currency2code(String currency) {
			return CurrencyTable.currency2code(currency);
		}
	
		private static float getRate(String srcCur, String dstCur) {
			return ExResult.getRate(srcCur, dstCur);
		}
		
		private static float getDayRate(String srcCur, String yyyymmdd) {
			 String today = yyyymmdd_sd.format(new Date());
			if (yyyymmdd.equalsIgnoreCase(today)) {
				if ("CNY".equalsIgnoreCase(srcCur)) {
					return ExResult.getRate("USD", "CNY");
				}
				
				return ExResult.getRate(srcCur, "CNY");
			}
			
			return ExResult.getHistoryRate(srcCur, yyyymmdd);
		}
		
		
		/**
		 * from number string to float.
		 * @param num
		 * @return
		 */
		private float str2float(String num) {
			if (num == null) {
				return -1.0F;
			}
			
			try {
				return Float.parseFloat(num);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return -2.0F;
			}
		}
		
		/**
		 * get slot value by name from slots json array.
		 * @param slotName
		 * @param slots
		 * @return
		 */
		private String getSlotValue(String slotName) {
			JSONObject jsonSemantic = this.getParsedSemantic();
			if (Utils.isEmpty(jsonSemantic)) {
				return "";
			}
			
			JSONArray slots = jsonSemantic.optJSONArray("slots");
			for (int i = 0; i < slots.length(); i++) {
				try {
					JSONObject slot = slots.getJSONObject(i);
					if (slot == null || slot.optString("name").equals(slotName) == false) {
						continue;
					}
					
					if (slot.has("num_detail")) {
						//如果是数值型slot，应该用推荐值，否则会有“一百”需要自己再转换
						return slot.optJSONObject("num_detail").optString("recommend_value");
					}
					
					if (slot.has("datetime")) {
						String dateTimeType = slot.optJSONObject("datetime").optString("type");
						long dayStartMs = 0;
						if ("time_recommend".equalsIgnoreCase(dateTimeType)) {
							//确切时间，取指定日
							dayStartMs = slot.optJSONObject("datetime").optJSONObject("data").optLong("start_time");
						} else {
							//非确切时间：时间段，或无效时间。取当天
							dayStartMs = Calendar.getInstance().getTimeInMillis();
						}
						
						//FIXME 这里将时间输出为：  今日|xxxxxx  ，使用time时再拆分，因为两个信息都需要用到
						//return slot.optString("value") + "|" + yyyymmdd.format(new Date(dayStartMs));
						
						return yyyymmdd_sd.format(new Date(dayStartMs));
					}
					
					return slot.optString("value");
				} catch (JSONException e) {
					e.printStackTrace();
					continue;
				}
			}
			
			return null;
		}
		
		/**
		 * 从modifier数组中获取第一个modifier
		 */
		private String getModifier() {
			JSONArray mods = this.getParsedSemantic().optJSONArray("modifier");
			if (Utils.isEmpty(mods)) {
				return "";
			}
			
			try {
				//TODO: may be array
				return mods.getString(0);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return "";
		}
		
		/**
		 * generate error json result.
		 * @param info
		 * @return
		 */
		private JSONObject errResult(String info) {
			
			detailContext = new DetailContext(false);
			
			JSONObject ret = new JSONObject();
			try {
				ret.put("status", "error");
				ret.put("info", info);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return ret;
		}
		
		private JSONObject okResult(String info) {
			JSONObject ret = new JSONObject();
			try {
				ret.put("status", "ok");
				ret.put("info", info);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return ret;
		}
		
		
	
		/**
		 * 界面显示用
		 */
		public String getSrcCurrency() {
			String srcMoney = getSlotValue("src_money");
			
			String ret = currency2code(srcMoney);
			//Utils.p(ret);
			return ret;
		}
		public String getDstCurrency() {
			String dstMoney = getSlotValue("dst_money");
			
			String ret = currency2code(dstMoney);
			//Utils.p(ret);
			return ret;
		}
		public String getSrcNum() {
			String ret = getSlotValue("src_number");
			if (Utils.isEmpty(ret)) {
				ret = Float.toString(calculatedSrc);
			}
			//Utils.p(ret);
			return ret;
		}
		public String getDstNum() {
			String ret = getSlotValue("dst_number");
			if (Utils.isEmpty(ret)) {
				ret = Float.toString(calculatedDst);
			}
			//Utils.p(ret);
			return ret;
		}
		
		
		public static void main(String[] args) {
	
		}
	
	}
	
	class DetailContext {
		
		public DetailContext(boolean isVisible) {
			this.isVisible = isVisible;
		}
		
		public DetailContext(boolean isVisible, float srcN, float dstN, String srcC, String dstC) {
			this.isVisible = isVisible;
			try {
				this.srcN = Float.toString(srcN);
			} catch (Exception e) {
			}
			try {
				this.dstN = Float.toString(dstN);
			} catch (Exception e) {
			}
			this.srcC = srcC;
			this.dstC = dstC;
		}
		
		boolean isVisible = false;
		String srcN = "";
		String dstN = "";
		String srcC = "";
		String dstC = "";
	}


------------------------------------------------------------------------------------------------

3. ExResult.java

	处理汇率API接口返回数据的类。

	这里面也包括从汇率API一次访问获取全世界各国货币名称和代码的数据，用于填充currencyTable.csv配置文件。

------------------------------------------------------------------------------------------------
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

------------------------------------------------------------------------------------------------

4. MainJFrame.java

	用windowbuilder插件画的一个简单界面。

------------------------------------------------------------------------------------------------
	package exchangerate;
	
	import java.awt.EventQueue;
	
	import javax.swing.JFrame;
	import javax.swing.JButton;
	import javax.swing.SwingConstants;
	import javax.swing.JTextField;
	import javax.swing.JLabel;
	import java.awt.event.ActionListener;
	import java.io.IOException;
	import java.net.URI;
	import java.net.URISyntaxException;
	import java.util.Vector;
	import java.awt.event.ActionEvent;
	import javax.swing.JPanel;
	import java.awt.Color;
	import java.awt.Desktop;
	
	import javax.swing.JTextArea;
	import javax.swing.JScrollPane;
	import javax.swing.border.TitledBorder;
	import javax.swing.UIManager;
	import java.awt.Font;
	
	public class MainJFrame {
	
		private JFrame frame;
		private JPanel resultPanel;
		private JTextField srcNum;
		private JLabel srcCur;
		private JLabel equals;
		private JTextField dstNum;
		private JLabel dstCur;
		private JPanel inputPanel;
		private JTextField input;
		private JTextField resultTxt;
		private JPanel commonConvert;
	
		private static Vector<String> sampleCorpus = new Vector<String>();
		private JButton button_1;
		static {
			sampleCorpus.add("帮我查一下汇率");
			sampleCorpus.add("今日汇率");
			sampleCorpus.add("今天的汇率有什么变化");
			
			sampleCorpus.add("美元的汇率是多少");
			sampleCorpus.add("查一下新加坡的汇率");
			sampleCorpus.add("我要去韩国玩，帮我看一下汇率信息");
			sampleCorpus.add("帮我查询一下中国与各国货币的汇率表");
			
			sampleCorpus.add("美元兑换成人民币");
			
			sampleCorpus.add("欧元是什么国家的货币");
			sampleCorpus.add("中国用什么货币");
			sampleCorpus.add("欧元在哪些国家使用");
			sampleCorpus.add("新加坡的货币是什么单位");
			
			sampleCorpus.add("你会不会查汇率啊");
			sampleCorpus.add("你能告诉我澳元的汇率吗");
			sampleCorpus.add("你知道港元跟日元之间的汇率吗");
			sampleCorpus.add("你能帮我计算什么币种的汇率");
			
			sampleCorpus.add("一百美元能换多少日元");
			sampleCorpus.add("10美元能换到60元人民币吗");
			sampleCorpus.add("多少美元可以兑换1000人民币");
			sampleCorpus.add("我要换一百美元需要多少人民币");
			
			//听不懂
			//sampleCorpus.add("调皮捣蛋听不懂");
		}
		public static String getRandomSampleCorpus() {
			int randomInt = (int)(Math.random() * sampleCorpus.size());
			Utils.p("random:" + randomInt + "/" + sampleCorpus.size());
			return sampleCorpus.get(randomInt);
		}
		
		/**
		 * Launch the application.
		 */
		public static void main(String[] args) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						MainJFrame window = new MainJFrame();
						window.frame.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	
		/**
		 * Create the application.
		 */
		public MainJFrame() {
			initialize();
		}
	
		/**
		 * Initialize the contents of the frame.
		 */
		private void initialize() {
			frame = new JFrame();
			frame.setTitle("自然语言理解试验小程序——汇率换算");
			frame.setBounds(100, 100, 915, 440);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().setLayout(null);
			
			JPanel semanticPanelBottom = new JPanel();
			semanticPanelBottom.setBounds(20, 77, 412, 293);
			semanticPanelBottom.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "  \u8BED \u4E49  ", TitledBorder.LEFT, TitledBorder.TOP, null, null));
			semanticPanelBottom.setToolTipText(" 123");
			frame.getContentPane().add(semanticPanelBottom);
			semanticPanelBottom.setLayout(null);
			
			JScrollPane semanticPanel = new JScrollPane();
			semanticPanel.setBounds(10, 26, 392, 250);
			semanticPanelBottom.add(semanticPanel);
			semanticPanel.setToolTipText("语义");
			
			final JTextArea semanticTxt = new JTextArea();
			semanticPanel.setViewportView(semanticTxt);
			semanticTxt.setColumns(20);
			semanticTxt.setRows(5);
			semanticTxt.setEditable(false);
			
			resultPanel = new JPanel();
			resultPanel.setBounds(442, 77, 428, 148);
			resultPanel.setBorder(new TitledBorder(null, "  \u7ED3 \u679C  ", TitledBorder.LEFT, TitledBorder.TOP, null, null));
			frame.getContentPane().add(resultPanel);
			resultPanel.setLayout(null);
			
			resultTxt = new JTextField();
			resultTxt.setFont(new Font("宋体", Font.PLAIN, 18));
			resultTxt.setBounds(30, 32, 374, 42);
			resultPanel.add(resultTxt);
			resultTxt.setColumns(10);
			
			commonConvert = new JPanel();
			commonConvert.setBounds(30, 96, 374, 42);
			resultPanel.add(commonConvert);
			commonConvert.setLayout(null);
			
			srcNum = new JTextField();
			srcNum.setBounds(10, 10, 66, 21);
			commonConvert.add(srcNum);
			srcNum.setHorizontalAlignment(SwingConstants.CENTER);
			srcNum.setEditable(false);
			srcNum.setColumns(10);
			srcNum.setBackground(Color.WHITE);
			
			srcCur = new JLabel("s");
			srcCur.setBounds(85, 10, 50, 21);
			commonConvert.add(srcCur);
			
			equals = new JLabel("=");
			equals.setBounds(135, 13, 23, 15);
			commonConvert.add(equals);
			equals.setHorizontalAlignment(SwingConstants.CENTER);
			
			dstNum = new JTextField();
			dstNum.setHorizontalAlignment(SwingConstants.CENTER);
			dstNum.setBounds(168, 10, 66, 21);
			commonConvert.add(dstNum);
			dstNum.setBackground(Color.WHITE);
			dstNum.setEditable(false);
			dstNum.setColumns(10);
			
			dstCur = new JLabel("d");
			dstCur.setBounds(240, 13, 49, 15);
			commonConvert.add(dstCur);
			
			inputPanel = new JPanel();
			inputPanel.setBounds(20, 10, 850, 57);
			frame.getContentPane().add(inputPanel);
			inputPanel.setLayout(null);
			
			JButton submit = new JButton("理解");
			submit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String inputStr = input.getText();
					ExHandler handler = new ExHandler(inputStr);
					semanticTxt.setText(handler.getNliResultForShow());
					resultTxt.setText(handler.getResultString());
					setCommonConvert(handler);
				}
			});
			submit.setBounds(109, 9, 70, 40);
			inputPanel.add(submit);
			
			input = new JTextField();
			input.setFont(new Font("宋体", Font.PLAIN, 18));
			input.setText(getRandomSampleCorpus());
			input.setColumns(10);
			input.setBounds(204, 8, 622, 39);
			inputPanel.add(input);
			
			JButton button = new JButton("换一句");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					input.setText(getRandomSampleCorpus());
				}
			});
			button.setBounds(20, 9, 79, 40);
			inputPanel.add(button);
			
			button_1 = new JButton("源码和功能说明");
			button_1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.browse(new URI("http://blog.csdn.net/happycxz/article/details/73223916"));
					} catch (IOException | URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			});
			button_1.setFont(new Font("宋体", Font.PLAIN, 20));
			button_1.setBounds(570, 271, 185, 57);
			frame.getContentPane().add(button_1);
		}
		
		private void setCommonConvert(ExHandler handler) {
			commonConvert.setVisible(handler.detailContext.isVisible);
			
			if (handler.detailContext.isVisible) {
				srcNum.setText(handler.detailContext.srcN);
				dstNum.setText(handler.detailContext.dstN);
				srcCur.setText("<html><font color=red>" + handler.detailContext.srcC + "</font></html>");
				dstCur.setText("<html><font color=red>" + handler.detailContext.dstC + "</font></html>");
			}
		}
	}


------------------------------------------------------------------------------------------------

5. NLI.java

	调用OLAMI的开放语义NLI接口返回数据的处理。

------------------------------------------------------------------------------------------------
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

------------------------------------------------------------------------------------------------

6. Utils.java

	工程中用到的一些工具方法。

------------------------------------------------------------------------------------------------
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

------------------------------------------------------------------------------------------------

7. currencyTable.csv

	全世界各国货币名称和代码的数据配置文件。

------------------------------------------------------------------------------------------------
	##
	CNY,人民币|在岸人民币,中国|中华人民共和国
	USD,美元|美刀,美国
	HKD,港元,香港
	MOP,澳元|澳门元,澳门
	
	## ExResult.GetAllCurrencyInfo 【自动分析】 【正常数据】
	AED,迪拉姆,阿联酋
	AFN,尼,阿富汗
	ALL,列克,阿尔巴尼亚
	AMD,德拉姆,亚美尼亚
	ANG,荷兰盾,荷兰
	AOA,宽扎,安哥拉
	ARS,披索,阿根廷
	AUD,澳大利亚元,澳大利亚
	AWG,阿鲁巴盾,阿鲁巴
	AZN,新马纳特,阿塞拜疆
	BBD,巴巴多斯元,巴巴多斯
	BDT,塔卡,孟加拉国
	BGN,列瓦,保加利亚
	BHD,巴林第纳尔,巴林
	BIF,布隆迪法郎,布隆迪
	BMD,百慕大元,百慕大
	BND,文莱元,文莱
	BOB,诺,玻利维亚
	BRL,雷亚尔,巴西
	BSD,巴哈马元,巴哈马
	BTN,努扎姆,不丹
	BYR,白俄罗斯卢布,白俄罗斯
	BZD,伯利兹元,伯利兹
	CAD,加拿大元,加拿大
	CDF,刚果法郎,刚果
	CHF,瑞士法郎,瑞士
	CLP,智利比索,智利
	CNH,离岸人民币,中国离岸
	CNY,人民币,中国
	COP,哥伦比亚比索,哥伦比亚
	CRC,科朗,哥斯达黎加
	CUP,古巴比索,古巴
	CZK,捷克克朗,捷克
	DJF,吉布提法郎,吉布提
	DKK,丹麦克朗,丹麦
	DOP,多米尼加比索,多米尼加
	DZD,阿尔及利亚第纳尔,阿尔及利亚
	EGP,埃及镑,埃及
	ERN,里亚,厄立特
	ETB,比尔,埃塞俄比亚
	EUR,欧元,欧洲
	FJD,斐济元,斐济
	FKP,福克兰群岛镑,福克兰群岛
	GBP,英镑,英国
	GEL,拉里,格鲁吉亚
	GHS,塞地,加纳
	GIP,直布罗陀镑,直布罗陀
	GNF,几内亚法郎,几内亚
	GTQ,格查尔,危地马拉
	GYD,圭亚那元,圭亚那
	HKD,港币,香港
	HNL,伦皮拉,洪都拉斯
	HRK,库纳,克罗地亚
	HTG,古德,海地
	HUF,福林,匈牙利
	IDR,印度尼西亚盾,印度尼西亚
	ILS,谢克尔,以色列
	INR,印度卢比,印度
	IQD,伊拉克第纳尔,伊拉克
	IRR,伊朗里亚尔,伊朗
	ISK,冰岛克朗,冰岛
	JMD,牙买加元,牙买加
	JOD,约旦第纳尔,约旦
	JPY,日元,日本
	KES,肯尼亚先令,肯尼亚
	KGS,索姆,吉尔吉斯
	KHR,瑞尔,柬埔寨
	KMF,科摩罗法郎,科摩罗
	KPW,朝鲜元,朝鲜
	KRW,韩国元,韩国
	KWD,科威特第纳尔,科威特
	KYD,开曼群岛元,开曼群岛
	KZT,坚戈,哈萨克斯坦
	LAK,基普,老挝
	LBP,黎巴嫩镑,黎巴嫩
	LKR,斯里兰卡卢比,斯里兰卡
	LRD,利比里亚元,利比里亚
	LTL,立特,立陶宛
	LVL,拉特,拉脱维亚
	LYD,利比亚第纳尔,利比亚
	MAD,迪拉姆,摩洛哥
	MGA,阿里亚里,马达加斯加
	MKD,马其顿第纳尔,马其顿
	MMK,缅元,缅甸
	MNT,图格里克,蒙古
	MOP,澳门币,澳门
	MRO,乌吉亚,毛里塔尼亚
	MUR,毛里求斯卢比,毛里求斯
	MVR,拉菲亚,马尔代夫
	MXN,墨西哥比索,墨西哥
	MYR,林吉特,马来西亚
	NAD,纳米比亚元,纳米比亚
	NGN,尼日利亚第纳尔,尼日利亚
	NIO,科多巴,尼加拉瓜
	NOK,挪威克朗,挪威
	NPR,尼泊尔卢比,尼泊尔
	NZD,新西兰元,新西兰
	OMR,阿曼里亚尔,阿曼
	PAB,巴波亚,巴拿马
	PEN,新索尔,秘鲁
	PGK,基那基那,新几内亚
	PHP,菲律宾比索,菲律宾
	PKR,巴基斯坦卢比,巴基斯坦
	PLN,兹罗提,波兰
	PYG,瓜拉尼,巴拉圭
	QAR,卡塔尔里亚尔,卡塔尔
	RON,列伊,罗马尼亚
	RSD,塞尔维亚第纳尔,塞尔维亚
	RUB,俄罗斯卢布,俄罗斯
	RWF,卢旺达法郎,卢旺达
	SAR,沙特阿拉伯里亚尔,沙特阿拉伯
	SBD,所罗门群岛元,所罗门群岛
	SCR,塞舌尔卢比,塞舌尔
	SDG,苏丹镑,苏丹
	SEK,瑞典克朗,瑞典
	SGD,新加坡元,新加坡
	SHP,圣赫勒拿镑,圣赫勒拿
	SLL,利昂,塞拉利昂
	SOS,索马里先令,索马里
	SRD,苏里南元,苏里南
	SVC,科朗,萨尔瓦多
	SYP,叙利亚镑,叙利亚
	THB,泰铢,泰国
	TJS,索莫尼,塔吉克斯坦
	TMT,马纳特,土库曼斯坦
	TND,突尼斯第纳尔,突尼斯
	TRY,里拉,土耳其
	TTD,特立尼达和多巴哥元,特立尼达和多巴哥
	TWD,台币,台湾
	TZS,坦桑尼亚先令,坦桑尼亚
	UAH,格里夫纳,乌克兰
	UGX,乌干达先令,乌干达
	USD,美元,美国
	UYU,乌拉圭比索,乌拉圭
	UZS,索姆,乌兹别克斯坦
	VEF,玻利瓦尔,委内瑞拉
	VND,越南盾,越南
	VUV,瓦图,瓦努阿图
	WST,塔拉,萨摩亚
	XAF,中非法郎,中非
	XCD,东加勒比元,东加勒比
	XOF,西非法郎,西非
	XPF,太平洋法郎,太平洋
	YER,也门里亚尔,也门
	ZAR,兰特,南非
	ZMW,克瓦查,赞比亚
	ZWL,津巴布韦元,津巴布韦
	
	
	## ExResult.GetAllCurrencyInfo 【自动分析】 【判断不出来国家名，需要手工修正】
	BAM,波斯尼亚和黑塞哥维那可,波斯尼亚和黑塞哥维那可
	BWP,博茨瓦纳普拉,博茨瓦纳普拉
	CVE,佛得角埃斯库多,佛得角埃斯库多
	GMD,冈比亚达拉西,冈比亚达拉西
	LSL,莱索托洛提,莱索托洛提
	MDL,摩尔多瓦列伊,摩尔多瓦列伊
	MWK,马拉维克瓦查,马拉维克瓦查
	MZN,莫桑比克梅蒂卡尔,莫桑比克梅蒂卡尔
	STD,圣多美多布拉,圣多美多布拉
	SZL,斯威士兰里兰吉尼,斯威士兰里兰吉尼
	XDR,特别提款权（国际货币基金）,特别提款权（国际货币基金）

------------------------------------------------------------------------------------------------

###olami平台NLI接口使用说明
1. 注册
2. 绑定手机
3. 登录
4. 选“自然语言语义互动系统”中的“进入NLI系统”
	NLI系统中，可以自己做语义模块，也可以导入已经有的语义模块。自己做语义模块相当于自己参与定制句型说法，刚接触这个，对规则不熟悉，暂时不考虑了。有兴趣的朋友可以尝试自己做语义模块。

5. 导入exchangerate语义模块

	这里用的汇率换算语义就是导入现成的exchangerate这个模块。

	附：[olami已有模块介绍](https://cn.olami.ai/wiki/?mp=nli&content=nli_module_list.html), 前面链接失效可以访问[olami官网](cn.olami.ai).
6. 进入刚刚导入的这个模块，点“发布”

	进入该模块后，能看到对应的语法信息，不需要关心，直接点“发布”，会显示“待发布的grammar（共xx个）： ”	.
7. 发布完成后，回到原来登录账号后的首页面
8. 点击“创建新应用”，应用名称、应用类型、应用描述可以简单填一下。
9. 配置该应用

	在刚刚创建的应用中，要先配置一下第5步和第6步导入的语义模块。
	在“配置模块”页面中，有“NLI模块”和“对话系统模块”。
	“NLI模块”是我们要配置的那种语义模块，勾选上刚刚那个exchangerate模块的名称，后面的优先级保持为默认值不需要修改，然后点“确定”。
	“对话系统模块”是另一种模式的模块，直接返回的是处理结果，而不是NLI模块里的那种语义，这里暂不深入研究，感兴趣的朋友可以配置一下试试，尝尝鲜。
10. 测试

	配置完应用后，就可以用这个应用后面的“测试”去试验一下配置的是否生效。点开“测试”后，输入一句话“1美元等于多少人民币”，正常返回NLI语义，证明配置成功。
11. 获取应用对应的API访问密钥

	在“我的应用”中，找到之前生成的那个应用，点击“查看Key”可以获取调用OLAMI开放语义平台的授权密钥，包括“App Key”和“App Secret”。
	这两个码，对应我写的demo小程序中的“NLI.java”中的“Appkey”和“Appsecret”。
	

---------------
**>>>>>>>>>>>>>>>>>>>>>>>> 欢迎转载 <<<<<<<<<<<<<<<<<<<<<<<<**

**本文原地址:[http://blog.csdn.net/happycxz/article/details/73223916][csdn本文链接]**


**本文相关资源链接：**

csdn此文链接:[http://blog.csdn.net/happycxz/article/details/73223916][csdn本文链接]

CSDN资源.此文对应全部资料：[http://download.csdn.net/download/happycxz/9871400][eclipse源码工程 + jar包]

CSDN资源.此文对应可运行JAR包：[http://download.csdn.net/download/happycxz/9871415][jar包]

oschina此文链接:[https://my.oschina.net/happycxz/blog/983179][oschina本文链接]

码云资源.此文对应源码:[http://git.oschina.net/happycxz/nlu_exchangerate_by_olami][码云工程]

百度云.此文对应全部资料：[http://pan.baidu.com/s/1mizCT7M][百度云]

github source：[https://github.com/happycxz/nlp_olami_exchangerate_java_demo](https://github.com/happycxz/nlp_olami_exchangerate_java_demo)

---------------
**优秀自然语言理解博客文章推荐：**

[根据OLAMI平台开发的日历Demo](http://blog.csdn.net/xinfinityx/article/details/72840977)

[用olami开放语义平台做汇率换算应用](http://blog.csdn.net/happycxz/article/details/73223916)

[自然语言处理-实际开发:用语义开放平台olami写一个翻译的应用](http://blog.csdn.net/u011211290/article/details/74330469)

[自定义java.awt.Canvas—趣味聊天](http://blog.csdn.net/u011827504/article/details/74332383)

[微信小程序+OLAMI自然语言API接口制作智能查询工具--快递、聊天、日历等](http://blog.csdn.net/huangmeimao/article/details/74923621)

[热门自然语言理解和语音API开发平台对比](http://blog.csdn.net/huangmeimao/article/details/74905918)

[使用OLAMI SDK和讯飞语音合成制作一个语音回复的短信小助手](http://blog.csdn.net/speeds3/article/details/75131125)

[微信小程序——智能小秘“遥知之”源码分享（语义理解基于olami）](http://blog.csdn.net/happycxz/article/details/75432928)

---------------
###写在最后
开放语义跟语音产品结合效果更好，如果有机会，我会将这个APP做成对接语音识别的，这样只需要对着话筒说话就可以，不用输入文字了。


[eclipse源码工程 + jar包]:http://download.csdn.net/download/happycxz/9871400
[jar包]:http://download.csdn.net/download/happycxz/9871415
[百度云]:http://pan.baidu.com/s/1mizCT7M

[码云工程]:http://git.oschina.net/happycxz/nlu_exchangerate_by_olami

[csdn本文链接]:http://blog.csdn.net/happycxz/article/details/73223916
[oschina本文链接]:https://my.oschina.net/happycxz/blog/983179



