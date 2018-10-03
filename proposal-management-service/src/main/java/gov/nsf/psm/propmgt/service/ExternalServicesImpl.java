package gov.nsf.psm.propmgt.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import gov.mynsf.common.email.model.SendMetaData;
import gov.nsf.common.exception.RollbackException;
import gov.nsf.common.model.BaseResponseWrapper;
import gov.nsf.components.rest.model.response.CollectionResponse;
import gov.nsf.components.rest.model.response.ModelResponse;
import gov.nsf.emailservice.api.model.Letter;
import gov.nsf.emailservice.client.EmailServiceClientImpl;
import gov.nsf.proposal.model.ProposalReviewWrapper;
import gov.nsf.proposal.service.client.ProposalReviewServiceRestfulClientImpl;
import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.Institution;
import gov.nsf.psm.foundation.model.InstitutionAddress;
import gov.nsf.psm.openam.OpenAMClientImpl;
import gov.nsf.psm.openam.exception.CommunicationException;
import gov.nsf.psm.openam.model.OpenAMUserInfo;
import gov.nsf.psm.propmgt.utility.MockUtility;
import gov.nsf.referencedataservice.client.InstitutionServiceClientImpl;
import gov.nsf.research.services.client.GappsServiceClientImpl;
import gov.nsf.research.services.gapps.v1.AgencyListResponse;
import gov.nsf.research.services.gapps.v1.GappsDetailRequest;
import gov.nsf.research.services.gapps.v1.GappsDetailResponse;
import gov.nsf.research.services.gapps.v1.GappsSearchRequest;
import gov.nsf.research.services.gapps.v1.GappsSearchResponse;
import gov.nsf.service.model.UdsGetUserDataRequest;
import gov.nsf.service.model.UdsGetUserDataResponse;
import gov.nsf.service.model.UdsRgovGroup;
import gov.nsf.userdata.api.model.UserData;
import gov.nsf.userdata.api.model.UserDataException;
import gov.nsf.userdata.api.model.UserDataResponseWrapper;
import gov.nsf.userdata.client.UserDataServiceClientImpl;

/**
 * Service contains methods that call services external to PSM.
 * 
 * @author carloilagan
 *
 */
public class ExternalServicesImpl implements ExternalServices {

	@Autowired
	InstitutionServiceClientImpl institutionServiceClientImpl;

	@Autowired
	UserDataServiceClientImpl udsRestClient;

	@Autowired
	gov.nsf.service.UserDataServiceClient udsSoapClient;

	@Autowired
	OpenAMClientImpl openAmClient;

	@Autowired
	GappsServiceClientImpl gappsServiceClient;

	@Autowired
	EmailServiceClientImpl emailServiceClient;

	@Autowired
	ProposalReviewServiceRestfulClientImpl proposalReviewerServiceClient;

	@Value("${propmgt.override-dependencies.inst-reference-data}") 
	Boolean overrideInstRefData;

	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalServicesImpl.class);

	@Override
	public Institution getInstitutionById(String id) throws CommonUtilException {
		Institution psmInstitution = new Institution();

		if (overrideInstRefData) {
			return MockUtility.getMockInstitution();
		} else {
			try {
				ModelResponse<gov.nsf.referencedataservice.api.model.Institution> modelResponse = institutionServiceClientImpl
						.getInstitution(id);
				if (modelResponse != null) { // && modelResponse.getData() !=
												// null
					psmInstitution = convertInstitution(modelResponse.getData());
				} else {
					LOGGER.error("Returned null while searching by institutionId {} in RefDataSvc", id);
					throw new CommonUtilException(
							"Returned null while searching by institutionId " + id + " in RefDataSvc");
				}
			} catch (RollbackException e) {
				LOGGER.error("Encountered Rollback Exception while searching by institutionId {} in RefDataSvc", id, e);
				throw new CommonUtilException(e);
			} catch (Exception e) {
				LOGGER.error("Encountered error while searching by institutionId {} in RefDataSvc", id, e);
				throw new CommonUtilException(e);
			}
		}

		return psmInstitution;
	}

	@Override
	public List<Institution> getSearchInstitutions(Map<String, String> searchCriteria) throws CommonUtilException {
		List<Institution> psmInstitutions = new ArrayList<Institution>();
		try {
			CollectionResponse<gov.nsf.referencedataservice.api.model.Institution> collectionResponse = getInstById(
					searchCriteria);
			for (gov.nsf.referencedataservice.api.model.Institution institution : collectionResponse.getData()) {
				psmInstitutions.add(convertInstitution(institution));
			}
		} catch (RollbackException e) {
			throw new CommonUtilException(e);
		}
		return psmInstitutions;
	}

	private CollectionResponse<gov.nsf.referencedataservice.api.model.Institution> getInstById(
			Map<String, String> searchCriteria) throws RollbackException {
		return institutionServiceClientImpl.searchInstitutions(searchCriteria);
	}

	protected static Institution convertInstitution(gov.nsf.referencedataservice.api.model.Institution institution) {
		Institution psmInstitution = new Institution();
		InstitutionAddress psmInstitutionAddress = new InstitutionAddress();
		psmInstitution.setId(institution.getId());
		psmInstitution.setDunsNumber(institution.getDunsId());
		psmInstitution.setSamRegistrationStatus(institution.getSamRegistrationStatus());
		psmInstitution.setOrganizationName(institution.getInstName());
		psmInstitution.setTaxId(formatTIN(institution.getTaxId()));
		if (institution.getTimeZone() != null) {
			psmInstitution.setTimeZone(institution.getTimeZone().getTimeZoneName() + " ("
					+ institution.getTimeZone().getTimeZoneOffset() + ")");
		}
		psmInstitution.setAddressType(institution.getAddressType());
		psmInstitutionAddress.setStreetAddress(institution.getAddress().getStreet1());
		psmInstitutionAddress.setStreetAddress2(institution.getAddress().getStreet2());
		psmInstitutionAddress.setCityName(institution.getAddress().getCity());
		psmInstitutionAddress.setStateCode(institution.getAddress().getState());
		psmInstitutionAddress.setCountryCode(institution.getAddress().getCountry());
		psmInstitutionAddress.setPostalCode(institution.getAddress().getZipCode());
		psmInstitution.setAddress(psmInstitutionAddress);

		LOGGER.debug("Converted Institution: " + psmInstitution.toString());
		return psmInstitution;
	}

	// TODO: move to util class?
	private static String formatTIN(String tin) {
		if (tin != null) {
			return String.valueOf(tin).replaceFirst("(\\d{3})(\\d{2})(\\d+)", "$1-$2-$3");
		} else
			return tin;

	}

	@Override
	public List<UserData> searchUDSbyEmailId(String emailId) throws CommonUtilException {
		UserDataResponseWrapper udsResponseWrapper = new UserDataResponseWrapper();
		try {
			udsResponseWrapper = udsRestClient.search(Collections.singletonMap("email", emailId));
			if (!udsResponseWrapper.getErrors().isEmpty()) {
				LOGGER.info("Error return calling UDS: " + udsResponseWrapper.toString());
				return new ArrayList<UserData>();
			}
		} catch (UserDataException e) {
			LOGGER.error("Encountered error while searching by email {} in UDS", emailId, e);
		}
		return udsResponseWrapper.getUsers();
	}

	@Override
	public UdsGetUserDataResponse getUserData(String nsfId) throws CommonUtilException {
		LOGGER.info("getUserData started: ");
		UdsGetUserDataRequest udsGetUserDataRequest = new UdsGetUserDataRequest();

		UdsGetUserDataResponse udsGetUserDataResponse;
		udsGetUserDataRequest.setUserID(nsfId);
		udsGetUserDataRequest.setGroup(UdsRgovGroup.NSF);

		try {
			udsGetUserDataResponse = udsSoapClient.getUserData(udsGetUserDataRequest);
			LOGGER.debug("UDS (SOAP) Response: " + udsGetUserDataResponse.toString());
		} catch (Exception e) {
			LOGGER.error("Encountered error while searching by nsfId {} in UDS", nsfId, e);
			throw new CommonUtilException(e);
		}
		return udsGetUserDataResponse;
	}

	@Override
	public OpenAMUserInfo getUserInfoForAccessToken(String authToken) throws CommonUtilException {
		try {
			return openAmClient.getUserInfoForAccessToken(authToken);
		} catch (CommunicationException e) {
			throw new CommonUtilException("Error encountered calling OpenAM", e);
		}
	}

	@Override
	public AgencyListResponse getAgencyList(String userId) throws CommonUtilException {
		try {
			return gappsServiceClient.getAgencyList(userId);
		} catch (Exception e) {
			throw new CommonUtilException("Error encountered calling Gapps Service", e);
		}
	}

	@Override
	public GappsDetailResponse getGrantApplicationDetails(GappsDetailRequest gappsSearchRequest)
			throws CommonUtilException {
		try {
			return gappsServiceClient.getGrantApplicationDetails(gappsSearchRequest);
		} catch (Exception e) {
			throw new CommonUtilException("Error encountered calling Gapps Service "
					+ gappsSearchRequest.getApplicationId() + " " + gappsSearchRequest.getUserId(), e);
		}
	}

	@Override
	public GappsSearchResponse findGrantApplications(GappsSearchRequest gappsSearchRequest) throws CommonUtilException {
		try {
			return gappsServiceClient.findGrantApplications(gappsSearchRequest);
		} catch (Exception e) {
			throw new CommonUtilException("Error encountered calling Gapps Service " + gappsSearchRequest.getUserId(),
					e);
		}
	}

	@Override
	public BaseResponseWrapper sendEmailMessage(Letter ltr, SendMetaData smData) throws CommonUtilException {
		try {
			return emailServiceClient.sendLetter(ltr, smData);
		} catch (Exception e) {
			throw new CommonUtilException("Error encountered using NSF email service", e);
		}
	}

	@Override
	public ProposalReviewWrapper getProposalReviewers(String[] proposals) throws CommonUtilException {
		try {
			return proposalReviewerServiceClient.getRevPropRecords(proposals);
		} catch (Exception e) {
			throw new CommonUtilException("Error encountered calling Proposal Reviewer Service", e);
		}
	}

}
