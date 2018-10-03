package gov.nsf.psm.propmgt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openAmClient")
public class OpenAmClientConfig extends ExternalServiceConfig {

	private String userInfoEndpoint;

	public String getUserInfoEndpoint() {
		return userInfoEndpoint;
	}

	public void setUserInfoEndpoint(String userInfoEndpoint) {
		this.userInfoEndpoint = userInfoEndpoint;
	}
}
