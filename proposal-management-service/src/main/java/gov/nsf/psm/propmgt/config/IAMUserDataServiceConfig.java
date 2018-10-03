package gov.nsf.psm.propmgt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iamUserDataService")
public class IAMUserDataServiceConfig extends ExternalServiceConfig {

}
