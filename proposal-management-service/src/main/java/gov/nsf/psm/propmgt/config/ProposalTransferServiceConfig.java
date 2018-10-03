package gov.nsf.psm.propmgt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ProposalTransferServiceClientConfig
 */
@ConfigurationProperties(prefix = "proposalTransferService")
public class ProposalTransferServiceConfig extends ExternalServiceConfig{
}
