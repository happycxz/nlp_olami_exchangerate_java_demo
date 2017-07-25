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
