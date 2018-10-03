package gov.nsf.psm.propmgt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "userdataRestClient")
public class IAMUserDataRestServiceConfig extends ExternalServiceConfig {

}
