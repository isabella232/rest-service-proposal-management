package gov.nsf.psm.propmgt.common;

import java.util.HashMap;
import java.util.Map;

public class GappsStatuses {
	private static final Map<String, String> statues = new HashMap<String, String>();
	static {
		statues.put("03", "Pending");
		statues.put("04", "Pending");
		statues.put("05", "Pending");
		statues.put("06", "Pending");
		statues.put("07", "Pending");
		statues.put("08", "Pending");
		statues.put("09", "Pending");
		statues.put("0A", "Pending");
		statues.put("0B", "Pending");
		statues.put("0C", "Pending");
		statues.put("0D", "Pending");
		statues.put("0E", "Pending");
		statues.put("0F", "Pending");
		statues.put("0G", "Pending");
		statues.put("0H", "Pending");
		statues.put("0I", "Pending");
		statues.put("0J", "Pending");
		statues.put("0K", "Pending");
		statues.put("0L", "Pending");
		statues.put("0M", "Pending");
		statues.put("0N", "Pending");
		statues.put("0P", "Pending");
		statues.put("0Q", "Pending");
		statues.put("0R", "Pending");
		statues.put("0S", "Pending");
		statues.put("0T", "Pending");
		statues.put("0U", "Pending");
		statues.put("40", "Recommended");
		statues.put("10", "Declined");
		statues.put("80", "Awarded");
		statues.put("20", "Withdrawn");
		statues.put("21", "Withdrawn");
		statues.put("24", "Withdrawn");
		statues.put("25", "Withdrawn");
		statues.put("26", "Withdrawn");
		statues.put("27", "Withdrawn");
		statues.put("28", "Withdrawn");
		statues.put("45", "Withdrawn");
		statues.put("23", "Returned");
		statues.put("30", "Returned");
		statues.put("31", "Returned");
		statues.put("32", "Returned");
		statues.put("33", "Returned");
		statues.put("34", "Returned");
		statues.put("35", "Returned");
		statues.put("36", "Returned");
		statues.put("37", "Returned");
		statues.put("38", "Returned");
		statues.put("39", "Returned");
		statues.put("3A", "Returned");
		statues.put("51", "Returned");
		statues.put("52", "Returned");
		statues.put("53", "Returned");
		statues.put("54", "Returned");
		statues.put("55", "Returned");
		statues.put("56", "Returned");
		statues.put("57", "Returned");
		statues.put("42", "Discouraged");
		statues.put("41", "Encouraged");
		statues.put("43", "Invited");
		statues.put("44", "Not Invited");
	}

	private GappsStatuses() {

	}

	public static String getStatus(String statusCode) {
		String code = "00" + statusCode;
		code = code.substring(code.length() - 2);
		if (statues.containsKey(code)) {
			return statues.get(code);
		} else {
			return "";
		}
	}

}
