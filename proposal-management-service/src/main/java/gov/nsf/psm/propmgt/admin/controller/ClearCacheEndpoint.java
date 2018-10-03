package gov.nsf.psm.propmgt.admin.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.stereotype.Component;

import net.sf.ehcache.CacheManager;

@Component
public class ClearCacheEndpoint extends AbstractEndpoint<Map<String, String>> {

	public ClearCacheEndpoint() {
		super("clearCache", false);
	}

	@Override
	public Map<String, String> invoke() {
		Map<String, String> clearCache = new LinkedHashMap<>();
		CacheManager cacheManager = CacheManager.getInstance();
		cacheManager.clearAll();
		clearCache.put("clearCache", "done");
		return clearCache;
	}
}
