package gov.nsf.psm.propmgt.service;

import java.util.List;

import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.Directorate;
import gov.nsf.psm.foundation.model.Division;
import gov.nsf.psm.foundation.model.FundingOpportunity;
import gov.nsf.psm.foundation.model.ProgramElement;
import gov.nsf.psm.foundation.model.lookup.CollaborativeTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.Country;
import gov.nsf.psm.foundation.model.lookup.DeadlineTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.InstitutionRoleTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.LoginUserRoleType;
import gov.nsf.psm.foundation.model.lookup.OtherPersonnelRoleTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.ProposalTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.SeniorPersonRoleTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.State;
import gov.nsf.psm.foundation.model.lookup.SubmissionTypeLookUp;
import gov.nsf.psm.foundation.model.proposal.ProposalPermission;
import gov.nsf.service.model.UdsGetUserDataResponse;


public interface CachedDataService {
	
	public List<FundingOpportunity> getAllFundingOpportunities() throws CommonUtilException;
	
	//public List<FundingOpportunity> getAllFundingOpportunitiesExclusion() throws CommonUtilException;
	
	public List<Division> getAllDivisions() throws CommonUtilException;
	
	public List<Directorate> getAllDirectorates() throws CommonUtilException;
	
	public List<ProgramElement> getAllProgramElements() throws CommonUtilException;
	
	public List<Directorate> getDirectoratesByFundingOpId(String fundingOpId) throws CommonUtilException;
	
	public List<Division> getDivisionsByFundingOpIdAndDirectorateId(String fundingOpId, String directorateId)
            throws CommonUtilException;
	
	public List<ProgramElement> getProgramElementsByDivisionId(String fundingOpId, String directorateId,
            String divisionId) throws CommonUtilException;
	
	public Directorate getDirectorateByFundingOpId(String fundingOpId, String directorateId)
            throws CommonUtilException;
	
	public List<ProposalPermission> getUserPermissions(String propStatusCode, String[] roleCodes)
            throws CommonUtilException;
	
	 public List<Country> getCountries() throws CommonUtilException;
	 
	 public List<DeadlineTypeLookUp> getDeadlineTypes() throws CommonUtilException;
	 
	 public List<ProposalTypeLookUp> getProposalTypeLookUp() throws CommonUtilException;
	 
	 public List<CollaborativeTypeLookUp> getCollabTypeLookUp() throws CommonUtilException;
	 
	 public List<OtherPersonnelRoleTypeLookUp> getOtherPersonnelRoleTypeLookup() throws CommonUtilException;
	 
	 public List<InstitutionRoleTypeLookUp> getInstitutionRoleTypeLookup() throws CommonUtilException;
	 
	 public List<LoginUserRoleType> getLoginUserRoleTypeLookUp() throws CommonUtilException;
	 
	 public List<SubmissionTypeLookUp> getSubmissionTypeLookUp() throws CommonUtilException;
	 
	 public List<SeniorPersonRoleTypeLookUp> getSeniorPersonRoleTypeLookup() throws CommonUtilException;
	 
	 public List<State> getStates() throws CommonUtilException;
	 
	 public UdsGetUserDataResponse getUserData(String nsfId) throws CommonUtilException;
	
}
