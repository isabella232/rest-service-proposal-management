package gov.nsf.psm.propmgt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "solicitationDataService")
public class SolicitationDataServiceConfig extends ExternalServiceConfig{
}
