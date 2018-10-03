package gov.nsf.psm.propmgt;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.nsf.psm.foundation.exception.CommonUtilException;

@RestController
public class F5Controller {

	private static final Logger logger = LoggerFactory.getLogger(F5Controller.class);

	@RequestMapping(value = "/f5health", method = RequestMethod.GET)
	@ResponseBody
	public String whatIsMyStatus() throws CommonUtilException {
		logger.debug("F5Contoller.whatIsMyStatus");
		return "UP";
	}

	@RequestMapping(value = "/network", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, String> getNetwork(HttpServletRequest request) throws CommonUtilException {
		return getNetworkParams(request);
	}

	private static Map<String, String> getNetworkParams(HttpServletRequest request) throws CommonUtilException {
		InetAddress ip;
		String hostname;
		String userIP;

		Map<String, String> networkParams = new HashMap<String, String>();

		try {
			ip = InetAddress.getLocalHost();
			hostname = ip.getHostName();
			userIP = request.getRemoteAddr();
			networkParams.put("IP", ip.toString());
			networkParams.put("HostName", hostname);
			networkParams.put("User IP: ", userIP);
		} catch (UnknownHostException e) {
			throw new CommonUtilException(e);
		}

		return networkParams;
	}

}
