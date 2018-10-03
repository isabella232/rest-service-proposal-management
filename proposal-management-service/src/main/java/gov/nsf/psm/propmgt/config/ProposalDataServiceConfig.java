package gov.nsf.psm.propmgt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "proposalDataService")
public class ProposalDataServiceConfig extends ExternalServiceConfig {
}
