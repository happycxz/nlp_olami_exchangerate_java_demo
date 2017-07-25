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
