package gov.nsf.psm.propmgt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "complianceValidationService")
public class ComplianceValidationServiceConfig extends ExternalServiceConfig {
}
