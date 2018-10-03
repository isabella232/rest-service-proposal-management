package gov.nsf.psm.propmgt.admin.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.stereotype.Component;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import gov.nsf.proposal.model.ProposalReviewWrapper;
import gov.nsf.psm.foundation.model.Institution;
import gov.nsf.psm.propmgt.service.ExternalServices;
import gov.nsf.research.services.gapps.v1.AgencyListResponse;
import gov.nsf.service.model.UdsGetUserDataResponse;
import gov.nsf.userdata.api.model.UserData;

@Component
public class DependencyHealthEndpoint extends AbstractEndpoint<Map<String, String>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DependencyHealthEndpoint.class);
	
	@Autowired
	ExternalServices externalServices;

	public DependencyHealthEndpoint() {
		super("dependencyHealth", false);
	}

	@Override
	public Map<String, String> invoke() {
		Map<String, String> dependencyHealth = new LinkedHashMap<>();
		// Reference Data Service status check
		try {
			Monitor referenceDataService = MonitorFactory.start("referenceDataService");
			String institutionId = "0013128000";
			Institution institution = externalServices.getInstitutionById(institutionId);
			dependencyHealth.put("Reference Data Service", "UP");
			referenceDataService.stop();
			if (institution.getOrganizationName() != null) {
				dependencyHealth.put("Reference Data Service Response Time ", referenceDataService.getTotal() + "ms");
			}
			referenceDataService.reset();
		} catch (Exception e) {
			LOGGER.debug("Reference Data Service", e);
			dependencyHealth.put("Reference Data Service", "DOWN");
		}
		// UDS(SOAP) status check
		try {
			Monitor udsSoap = MonitorFactory.start("udsSoap");
			String nsfId = "000203143";
			UdsGetUserDataResponse udsGetUserDataResponse = externalServices.getUserData(nsfId);
			dependencyHealth.put("UDS(SOAP)", "UP");
			udsSoap.stop();
			if (udsGetUserDataResponse.getUserData().getFirstName() != null) {
				dependencyHealth.put("UDS(SOAP) Response Time ", udsSoap.getTotal() + "ms");
			}
			udsSoap.reset();
		} catch (Exception e) {
			LOGGER.debug("UDS(SOAP)", e);
			dependencyHealth.put("UDS(SOAP)", "DOWN");
		}
		// UDS(REST) status check
		try {
			Monitor udsRest = MonitorFactory.start("udsRet");
			String emailId = "yuanxie@ece.ucsb.edu";
			List<UserData> userDataList = externalServices.searchUDSbyEmailId(emailId);
			dependencyHealth.put("UDS(REST)", "UP");
			udsRest.stop();
			if (!userDataList.isEmpty()) {
				dependencyHealth.put("UDS(REST) Response Time ", udsRest.getTotal() + "ms");
			}
			udsRest.reset();
		} catch (Exception e) {
			LOGGER.debug("UDS(REST)", e);
			dependencyHealth.put("UDS(REST)", "DOWN");
		}
		// RGov Gapps Service status check
		try {
			Monitor gappsService = MonitorFactory.start("gappsService");
			String userId = "000085032";
			AgencyListResponse agencyListResponse = externalServices.getAgencyList(userId);
			dependencyHealth.put("RGov GAPPS Service(SOAP)", "UP");
			gappsService.stop();
			if (!agencyListResponse.getAgencyList().getAgency().isEmpty()) {
				dependencyHealth.put("RGov GAPPS Service(SOAP) Response Time ", gappsService.getTotal() + "ms");
			}
			gappsService.reset();
		} catch (Exception e) {
			LOGGER.debug("RGov GAPPS Service(SOAP)", e);
			dependencyHealth.put("RGov GAPPS Service(SOAP)", "DOWN");
		}
		// Proposal Reviewer Service status check
		try {
			Monitor propRevRet = MonitorFactory.start("propRevRet");
			String[] proposals = new String[] { "0000001", "000002" };
			ProposalReviewWrapper proposalReviewWrapper = externalServices.getProposalReviewers(proposals);
			dependencyHealth.put("Proposal Reviewer Service ", "UP");
			propRevRet.stop();
			if (!proposalReviewWrapper.getProposalReviews().isEmpty() || !proposalReviewWrapper.getErrors().isEmpty()) {
				dependencyHealth.put("Proposal Reviewer Service Response Time ", propRevRet.getTotal() + "ms");
			}
			propRevRet.reset();
		} catch (Exception e) {
			LOGGER.debug("Proposal Reviewer Service", e);
			dependencyHealth.put("Proposal Reviewer Service", "DOWN");
		}
		return dependencyHealth;
	}
}
