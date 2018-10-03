package gov.nsf.psm.propmgt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "proposalReviewerServiceClient")
public class ProposalReviewerServiceConfig extends ExternalServiceConfig {
}
