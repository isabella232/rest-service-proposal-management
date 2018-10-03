package gov.nsf.psm.propmgt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "referenceDataService")
public class ReferenceDataServiceConfig extends ExternalServiceConfig {
}
