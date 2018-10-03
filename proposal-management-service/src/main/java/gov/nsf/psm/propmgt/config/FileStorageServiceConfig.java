package gov.nsf.psm.propmgt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fileStorageService")
public class FileStorageServiceConfig extends ExternalServiceConfig {

}
