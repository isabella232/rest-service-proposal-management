package gov.nsf.psm.propmgt.service;

import java.util.List;
import java.util.Map;

import gov.mynsf.common.email.model.SendMetaData;
import gov.nsf.common.model.BaseResponseWrapper;
import gov.nsf.emailservice.api.model.Letter;
import gov.nsf.proposal.model.ProposalReviewWrapper;
import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.Institution;
import gov.nsf.psm.openam.model.OpenAMUserInfo;
import gov.nsf.research.services.gapps.v1.AgencyListResponse;
import gov.nsf.research.services.gapps.v1.GappsDetailRequest;
import gov.nsf.research.services.gapps.v1.GappsDetailResponse;
import gov.nsf.research.services.gapps.v1.GappsSearchRequest;
import gov.nsf.research.services.gapps.v1.GappsSearchResponse;
import gov.nsf.service.model.UdsGetUserDataResponse;
import gov.nsf.userdata.api.model.UserData;

public interface ExternalServices {

	public Institution getInstitutionById(String id) throws CommonUtilException;
	
	public List<Institution> getSearchInstitutions(Map<String, String> searchCriteria) throws CommonUtilException;
	
	public List<UserData> searchUDSbyEmailId(String emailId) throws CommonUtilException;
	
	public OpenAMUserInfo getUserInfoForAccessToken(String authToken) throws CommonUtilException;
	
	public UdsGetUserDataResponse getUserData(String nsfId) throws CommonUtilException;
	
	public AgencyListResponse getAgencyList(String userId) throws CommonUtilException;
	
	public GappsDetailResponse getGrantApplicationDetails(GappsDetailRequest gappsSearchRequest) throws CommonUtilException;
	
	public GappsSearchResponse findGrantApplications(GappsSearchRequest gappsSearchRequest) throws CommonUtilException;
	
	public BaseResponseWrapper sendEmailMessage(Letter ltr, SendMetaData smData) throws CommonUtilException;
	
	public ProposalReviewWrapper getProposalReviewers(String[] proposals) throws CommonUtilException;
	
}
