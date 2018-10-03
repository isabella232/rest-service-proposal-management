package gov.nsf.psm.propmgt.config;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.mynsf.common.restclient.authorization.BasicAuthentication;
import gov.mynsf.common.restclient.authorization.HttpAuthorization;
import gov.nsf.components.rest.template.SecureRestTemplate;
import gov.nsf.emailservice.client.EmailServiceClientImpl;
import gov.nsf.proposal.service.client.ProposalReviewServiceRestfulClientImpl;
import gov.nsf.psm.compliancevalidation.ComplianceValidationServiceClient;
import gov.nsf.psm.compliancevalidation.ComplianceValidationServiceClientImpl;
import gov.nsf.psm.documentcompliance.DocumentComplianceServiceClient;
import gov.nsf.psm.documentcompliance.DocumentComplianceServiceClientImpl;
import gov.nsf.psm.documentgeneration.DocumentGenerationServiceClient;
import gov.nsf.psm.documentgeneration.DocumentGenerationServiceClientImpl;
import gov.nsf.psm.filestorage.FileStorageServiceClient;
import gov.nsf.psm.filestorage.FileStorageServiceClientImpl;
import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.restclient.NsfRestTemplate;
import gov.nsf.psm.openam.OpenAMClientImpl;
import gov.nsf.psm.openam.exception.CommunicationException;
import gov.nsf.psm.openam.model.OpenAMConfiguration;
import gov.nsf.psm.propmgt.service.CachedDataService;
import gov.nsf.psm.propmgt.service.CachedDataServiceImpl;
import gov.nsf.psm.propmgt.service.ExternalServices;
import gov.nsf.psm.propmgt.service.ExternalServicesImpl;
import gov.nsf.psm.propmgt.service.ProposalManagementForTransferService;
import gov.nsf.psm.propmgt.service.ProposalManagementForTransferServiceImpl;
import gov.nsf.psm.propmgt.service.ProposalManagementService;
import gov.nsf.psm.propmgt.service.ProposalManagementServiceImpl;
import gov.nsf.psm.propmgt.service.UserDetailsService;
import gov.nsf.psm.propmgt.service.UserDetailsServiceImpl;
import gov.nsf.psm.propmgt.utility.ProposalFileUtility;
import gov.nsf.psm.proposaldata.ProposalDataServiceClient;
import gov.nsf.psm.proposaldata.ProposalDataServiceClientImpl;
import gov.nsf.psm.proposaltransfer.client.ProposalTransferServiceClientImpl;
import gov.nsf.psm.solicitation.SolicitationDataServiceClient;
import gov.nsf.psm.solicitation.SolicitationDataServiceClientImpl;
import gov.nsf.referencedataservice.client.InstitutionServiceClientImpl;
import gov.nsf.research.services.client.GappsServiceClientImpl;
import gov.nsf.service.UserDataServiceClient;
import gov.nsf.service.UserDataServiceClientImpl;
import gov.nsf.service.model.UdsServiceConnectionDetails;

@Configuration
@EnableConfigurationProperties({ SolicitationDataServiceConfig.class, ProposalDataServiceConfig.class,
		FileStorageServiceConfig.class, DocumentComplianceServiceConfig.class, ComplianceValidationServiceConfig.class,
		DocumentGenerationServiceConfig.class, IAMUserDataServiceConfig.class, IAMUserDataRestServiceConfig.class,
		ReferenceDataServiceConfig.class, OpenAmClientConfig.class, ProposalTransferServiceConfig.class,
		EmailServiceConfig.class, RGovGappsServiceConfig.class, ProposalReviewerServiceConfig.class })
@EnableAspectJAutoProxy
@EnableCaching
public class ProposalManagementServiceConfig {

    @Autowired
    private SolicitationDataServiceConfig solicitationDataServiceConfig;

    @Autowired
    private ProposalDataServiceConfig proposalDataServiceConfig;

    @Autowired
    private FileStorageServiceConfig fileStorageServiceConfig;

    @Autowired
    private DocumentComplianceServiceConfig docComplianceServiceConfig;

    @Autowired
    private ComplianceValidationServiceConfig complianceValidationServiceConfig;

    @Autowired
    private DocumentGenerationServiceConfig docGenerationServiceConfig;

    @Autowired
    private IAMUserDataServiceConfig iamUserDataServiceConfig;

    @Autowired
    private RGovGappsServiceConfig rgovGappsServiceConfig;
    
    @Autowired
    private IAMUserDataRestServiceConfig iamUserDataRestServiceConfig;

    @Autowired
    private ReferenceDataServiceConfig referenceDataServiceConfig;
    
    @Autowired
    private OpenAmClientConfig openAmClientConfig;

    @Autowired
    private ProposalTransferServiceConfig proposalTransferConfig;

    @Autowired
    private EmailServiceConfig emailServiceConfig;

    @Autowired
    private ProposalReviewerServiceConfig proposalReviewerServiceConfig;

    @Value("${propmgt.proposal-file-naming-template}")
    private String proposalFileNamingTemplate;

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonConverter.setObjectMapper(objectMapper);
        return jsonConverter;
    }

    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        RequestMappingHandlerMapping requestMappingHandlerMapping = new RequestMappingHandlerMapping();
        requestMappingHandlerMapping.setUseSuffixPatternMatch(false);
        return requestMappingHandlerMapping;
    }

    @Bean
    public ProposalManagementService proposalManagementService() {
        return new ProposalManagementServiceImpl();
    }

    @Bean
    public ProposalManagementForTransferService proposalManagementForTransferService() {
        return new ProposalManagementForTransferServiceImpl();
    }
    
    @Bean
    public CachedDataService cachedDataService() {
    	return new CachedDataServiceImpl();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
    	return new UserDetailsServiceImpl();
    }
    
    @Bean
    public ExternalServices externalServices() {
    	return new ExternalServicesImpl();
    }

    @Bean
    public SolicitationDataServiceClient solicitationDataServiceClient() {
        SolicitationDataServiceClientImpl client = new SolicitationDataServiceClientImpl();
        client.setServerURL(solicitationDataServiceConfig.getServerUrl());
        client.setUsername(solicitationDataServiceConfig.getUsername());
        client.setPassword(solicitationDataServiceConfig.getPassword());
        client.setAuthenticationRequired(solicitationDataServiceConfig.isAuthenticationRequired());
        client.setRequestTimeout(solicitationDataServiceConfig.getTimeout());
        return client;
    }

    @Bean
    public ProposalDataServiceClient proposalDataServiceClient() {
        ProposalDataServiceClientImpl client = new ProposalDataServiceClientImpl();
        client.setServerURL(proposalDataServiceConfig.getServerUrl());
        client.setUsername(proposalDataServiceConfig.getUsername());
        client.setPassword(proposalDataServiceConfig.getPassword());
        client.setAuthenticationRequired(proposalDataServiceConfig.isAuthenticationRequired());
        client.setRequestTimeout(proposalDataServiceConfig.getTimeout());
        return client;
    }

    @Bean
    public FileStorageServiceClient fileStorageServiceClient() {
        FileStorageServiceClientImpl client = new FileStorageServiceClientImpl();
        client.setServerURL(fileStorageServiceConfig.getServerUrl());
        client.setUsername(fileStorageServiceConfig.getUsername());
        client.setPassword(fileStorageServiceConfig.getPassword());
        client.setAuthenticationRequired(fileStorageServiceConfig.isAuthenticationRequired());
        client.setRequestTimeout(fileStorageServiceConfig.getTimeout());
        return client;
    }

    @Bean
    public DocumentComplianceServiceClient documentComplianceServiceClient() {
        DocumentComplianceServiceClientImpl client = new DocumentComplianceServiceClientImpl();
        client.setServerURL(docComplianceServiceConfig.getServerUrl());
        client.setUsername(docComplianceServiceConfig.getUsername());
        client.setPassword(docComplianceServiceConfig.getPassword());
        client.setAuthenticationRequired(docComplianceServiceConfig.isAuthenticationRequired());
        client.setRequestTimeout(docComplianceServiceConfig.getTimeout());
        return client;
    }

    @Bean
    public ComplianceValidationServiceClient complianceValidationServiceClient() {
        ComplianceValidationServiceClientImpl client = new ComplianceValidationServiceClientImpl();
        client.setServerURL(complianceValidationServiceConfig.getServerUrl());
        client.setUsername(complianceValidationServiceConfig.getUsername());
        client.setPassword(complianceValidationServiceConfig.getPassword());
        client.setAuthenticationRequired(complianceValidationServiceConfig.isAuthenticationRequired());
        client.setRequestTimeout(complianceValidationServiceConfig.getTimeout());
        return client;
    }

    @Bean
    public DocumentGenerationServiceClient documentGenerationServiceClient() {
        DocumentGenerationServiceClientImpl client = new DocumentGenerationServiceClientImpl();
        client.setServerURL(docGenerationServiceConfig.getServerUrl());
        client.setUsername(docGenerationServiceConfig.getUsername());
        client.setPassword(docGenerationServiceConfig.getPassword());
        client.setAuthenticationRequired(docGenerationServiceConfig.isAuthenticationRequired());
        client.setRequestTimeout(docGenerationServiceConfig.getTimeout());
        return client;
    }

    @Bean
    public UserDataServiceClient userDataServiceClient() {
        UdsServiceConnectionDetails udsServiceConnectionDetails = new UdsServiceConnectionDetails();
        udsServiceConnectionDetails.setUserName(iamUserDataServiceConfig.getUsername());
        udsServiceConnectionDetails.setPassword(iamUserDataServiceConfig.getPassword());
        udsServiceConnectionDetails.setEndpointUrl(iamUserDataServiceConfig.getServerUrl());
        return new UserDataServiceClientImpl(udsServiceConnectionDetails);
    }

	@Bean
	public GappsServiceClientImpl gappsServiceClientImpl() {
		return new GappsServiceClientImpl(rgovGappsServiceConfig.getServerUrl(), rgovGappsServiceConfig.getUsername(),
				rgovGappsServiceConfig.getPassword());
	}
    
    @Bean
    public InstitutionServiceClientImpl referenceDataServiceClient() {

        gov.nsf.components.rest.authorization.BasicAuthentication authorization =
                new gov.nsf.components.rest.authorization.BasicAuthentication(referenceDataServiceConfig.getUsername(), referenceDataServiceConfig.getPassword());

        InstitutionServiceClientImpl institutionServiceClientImpl = new InstitutionServiceClientImpl();
        institutionServiceClientImpl.setInstitutionServiceURL(referenceDataServiceConfig.getServerUrl());
        institutionServiceClientImpl.setAuthorization(authorization);
        institutionServiceClientImpl.setRestTemplate(new SecureRestTemplate(referenceDataServiceConfig.getTimeout(), 10, 10));

        return institutionServiceClientImpl;
    }
    
    @Bean
    public OpenAMClientImpl openAmClient() throws CommunicationException {
        OpenAMConfiguration openAMConfiguration = new OpenAMConfiguration();
        openAMConfiguration.setAuthenticationRequired(openAmClientConfig.isAuthenticationRequired());
        openAMConfiguration.setRequestTimeout(openAmClientConfig.getTimeout());
        openAMConfiguration.setServerURL(openAmClientConfig.getServerUrl());
        openAMConfiguration.setUserInfoEndpoint(openAmClientConfig.getUserInfoEndpoint());
        return new OpenAMClientImpl(openAMConfiguration);
    }

    @Bean
    public ProposalTransferServiceClientImpl proposalTransferServiceClient() throws CommunicationException {
        gov.nsf.components.rest.authorization.BasicAuthentication auth =
                new gov.nsf.components.rest.authorization.BasicAuthentication(proposalTransferConfig.getUsername(), proposalTransferConfig.getPassword());

        ProposalTransferServiceClientImpl proposalTransferServiceClient = new ProposalTransferServiceClientImpl();
        proposalTransferServiceClient.setProposalTransferServiceURL(proposalTransferConfig.getServerUrl());
        proposalTransferServiceClient.setAuthorization(auth);
        proposalTransferServiceClient.setRestTemplate(restTemplate());
        return proposalTransferServiceClient;
    }

    @Bean
    public EmailServiceClientImpl emailServiceClient() throws CommunicationException {
        EmailServiceClientImpl emailServiceClient = new EmailServiceClientImpl();
        emailServiceClient.setEmailServiceURL(emailServiceConfig.getServerUrl());
        emailServiceClient.setEmailServiceServiceUserName(emailServiceConfig.getUsername());
        emailServiceClient.setEmailServicePassword(emailServiceConfig.getPassword());
        emailServiceClient.setRequestTimeout(emailServiceConfig.getTimeout());
        emailServiceClient.setAuthenticationRequired(true);

        return emailServiceClient;
    }

    @Bean
    public ProposalFileUtility proposalFileUtility() {
        return new ProposalFileUtility(proposalFileNamingTemplate);
    }

	@Bean
	public gov.nsf.userdata.client.UserDataServiceClientImpl userDataRestServiceClient() {
		gov.nsf.userdata.client.UserDataServiceClientImpl userDataServiceClient = new gov.nsf.userdata.client.UserDataServiceClientImpl();

		userDataServiceClient.setUserDataServiceURL(iamUserDataRestServiceConfig.getServerUrl());
		userDataServiceClient.setAuthorization(basicAuth());
		userDataServiceClient.setRestTemplate(restTemplate());
		return userDataServiceClient;
	}

	@Bean
	public SecureRestTemplate restTemplate() {
		SecureRestTemplate restTemplate = new SecureRestTemplate(iamUserDataRestServiceConfig.getTimeout());
		return restTemplate;
	}

	@Bean
	public gov.nsf.components.rest.authorization.BasicAuthentication basicAuth() {
		gov.nsf.components.rest.authorization.BasicAuthentication basicAuth = new gov.nsf.components.rest.authorization.BasicAuthentication(
				iamUserDataRestServiceConfig.getUsername(), iamUserDataRestServiceConfig.getPassword());
		return basicAuth;
	}

	@Bean
    public VelocityEngine velocityEngine(){
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty("resource.loader", "class");
        engine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        return engine;
    }

	@Bean
	public ProposalReviewServiceRestfulClientImpl proposalReviewerServiceClient() throws CommunicationException {
		ProposalReviewServiceRestfulClientImpl proposalReviewerServiceClient = new ProposalReviewServiceRestfulClientImpl();
		proposalReviewerServiceClient.setProposalReviewServiceURL(proposalReviewerServiceConfig.getServerUrl());
		gov.nsf.components.rest.authorization.BasicAuthentication basicAuth = new gov.nsf.components.rest.authorization.BasicAuthentication(
				proposalReviewerServiceConfig.getUsername(), proposalReviewerServiceConfig.getPassword());
		proposalReviewerServiceClient.setAuthorization(basicAuth);
		try {
			proposalReviewerServiceClient.setRestTemplate(
					NsfRestTemplate.setupRestTemplate(proposalReviewerServiceConfig.isAuthenticationRequired(), proposalReviewerServiceConfig.getTimeout()));
		} catch (CommonUtilException e) {
			throw new CommunicationException(e);
		}
		return proposalReviewerServiceClient;
	}
}
