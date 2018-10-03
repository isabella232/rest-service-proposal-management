package gov.nsf.psm.propmgt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ProposalTransferServiceClientConfig
 */
@ConfigurationProperties(prefix = "emailServiceClient")
public class EmailServiceConfig extends ExternalServiceConfig{
}
