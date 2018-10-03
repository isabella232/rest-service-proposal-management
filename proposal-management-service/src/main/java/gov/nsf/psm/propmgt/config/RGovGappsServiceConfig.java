package gov.nsf.psm.propmgt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rgovGappsService")
public class RGovGappsServiceConfig extends ExternalServiceConfig {
}
