package gov.nsf.psm.propmgt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "documentComplianceService")
public class DocumentComplianceServiceConfig extends ExternalServiceConfig {
}
