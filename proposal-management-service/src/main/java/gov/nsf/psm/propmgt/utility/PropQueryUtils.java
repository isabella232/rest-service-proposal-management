package gov.nsf.psm.propmgt.utility;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nsf.psm.foundation.exception.CommonUtilException;

public class PropQueryUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropQueryUtils.class);

	private PropQueryUtils() {
		// Private constructor
	}

	public static boolean isOnlySpoAor(Map<String, Boolean> spoAORMap, List<String> institutionIds) {
		if (institutionIds != null && !institutionIds.isEmpty()) {
			for (String instId : institutionIds) {
				if (spoAORMap != null && !spoAORMap.isEmpty() && spoAORMap.containsKey(instId)) {
					return spoAORMap.get(instId);
				}
			}
		}
		return false;
	}

	public static boolean isDueDatePassed(Date deadLineDate, String timeZone) throws CommonUtilException {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(deadLineDate);
			cal.set(Calendar.HOUR_OF_DAY, 17);
			cal.set(Calendar.MINUTE, 0);
			deadLineDate = cal.getTime();
			LOGGER.debug("deadline date " + cal.getTime() + " current time in institution " + timeZone + " "
					+ ProposalFactModelUtility.deriveCurrentDateTimefromInstitutionTimezone(timeZone));
			if (deadLineDate.before(ProposalFactModelUtility.deriveCurrentDateTimefromInstitutionTimezone(timeZone))) {
				return true;
			} else {
				return false;
			}
		} catch (ParseException pe) {
			throw new CommonUtilException("Parse Exception happened in PropQueryUtils.isDueDatePassed", pe);
		}
	}
}
