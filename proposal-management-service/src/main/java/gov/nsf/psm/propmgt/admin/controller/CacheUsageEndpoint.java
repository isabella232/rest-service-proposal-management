package gov.nsf.psm.propmgt.admin.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.stereotype.Component;

import net.sf.ehcache.CacheManager;

@Component
public class CacheUsageEndpoint extends AbstractEndpoint<Map<String, Object>> {

	public CacheUsageEndpoint() {
		super("cacheUsage", false);
	}

	@Override
	public Map<String, Object> invoke() {
		Map<String, Object> clearCache = new LinkedHashMap<>();
		CacheManager cacheManager = CacheManager.getInstance();
		String[] cacheNames = cacheManager.getCacheNames();
		for (String cacheName : cacheNames) {
			clearCache.put(cacheName, cacheManager.getEhcache(cacheName).getSize());
		}
		return clearCache;
	}
}
