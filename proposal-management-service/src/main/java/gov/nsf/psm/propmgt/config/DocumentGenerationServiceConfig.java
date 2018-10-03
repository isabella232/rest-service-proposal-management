package gov.nsf.psm.propmgt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="documentGenerationService")
public class DocumentGenerationServiceConfig extends ExternalServiceConfig {

}
