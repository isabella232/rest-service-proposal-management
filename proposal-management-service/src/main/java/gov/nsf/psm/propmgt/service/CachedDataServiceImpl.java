package gov.nsf.psm.propmgt.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

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
import gov.nsf.psm.propmgt.common.Constants;
import gov.nsf.psm.proposaldata.ProposalDataServiceClient;

import gov.nsf.psm.solicitation.SolicitationDataServiceClient;
import gov.nsf.service.model.UdsGetUserDataResponse;

/**
 * Service contains methods that retrieve data that is cached in the services.
 * 
 * @author carloilagan
 *
 */

public class CachedDataServiceImpl implements CachedDataService {

    @Autowired
    SolicitationDataServiceClient solicitationDataServiceclient;

    @Autowired
    ProposalDataServiceClient proposalDataServiceClient;
    
    @Autowired
    ExternalServices externalServices;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass()); 
    
	@Override
	@Cacheable(value = "allFundingOpsCache", key = "#root.method.name")
	public List<FundingOpportunity> getAllFundingOpportunities() throws CommonUtilException {
		LOGGER.info("CachedDataServiceImpl.getAllFundingOpportunities()");
		try {
			// return
			// solicitationDataServiceclient.getAllFundingOpportunities();
			List<FundingOpportunity> fundingOpsSdsvc = solicitationDataServiceclient.getAllFundingOpportunities();

			List<FundingOpportunity> fundingOpsPdsvc = proposalDataServiceClient.getFundingOppExclusionList();

			for (FundingOpportunity fop : fundingOpsPdsvc) {

				fundingOpsSdsvc.remove(fop);
			}
			return fundingOpsSdsvc;
		} catch (Exception e) {
			throw new CommonUtilException(Constants.GET_ALL_FUNDING_OPPORTUNITY_ERROR, e);
		}
	}

    
/*	@Override
	public List<FundingOpportunity> getAllFundingOpportunitiesExclusion() throws CommonUtilException {
		System.out.println("CachedDataServiceImpl.getAllFundingOpportunitiesExclusion()");
		try {

			List<FundingOpportunity> fundingOpsSdsvc = solicitationDataServiceclient.getAllFundingOpportunities();

			List<FundingOpportunity> fundingOpsPdsvc = proposalDataServiceClient.getFundingOppExclusionList();

			for (FundingOpportunity fop : fundingOpsPdsvc) {

				fundingOpsSdsvc.remove(fop);
			}

			return fundingOpsSdsvc;
		} catch (Exception e) {
			throw new CommonUtilException(Constants.GET_ALL_FUNDING_OPPORTUNITY_ERROR, e);
		}
	}*/
    
    
    @Override
    @Cacheable(value = "allDivisionsCache", key = "#root.method.name")
    public List<Division> getAllDivisions() throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getAllDivisions()");
        try {
            return solicitationDataServiceclient.getAllDivisions();
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_ALL_DIVISIONS_ERROR, e);
        }
    }

    @Override
    @Cacheable(value = "allDirectoratesCache", key = "#root.method.name")
    public List<Directorate> getAllDirectorates() throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getAllDirectorates()");
        try {
            return solicitationDataServiceclient.getAllDirectorates();
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_ALL_DIRECTORATES_ERROR, e);
        }
    }

    @Override
    @Cacheable(value = "allProgElementsCache", key = "#root.method.name")
    public List<ProgramElement> getAllProgramElements() throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getAllProgramElements()");
        try {
            return solicitationDataServiceclient.getAllProgramElements();
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_ALL_PROGRAM_ELEMENTS_ERROR, e);
        }
    }

    @Override
    @Cacheable(value = "dirsByFundOpCache")
    public List<Directorate> getDirectoratesByFundingOpId(String fundingOpId) throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getDirectoratesByFundingOpId()");
        try {
            return solicitationDataServiceclient.getDirectoratesByFundingOpId(fundingOpId);
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_DIRECTORATES_FUNDING_OPPS_ERROR, e);
        }
    }

    @Override
    @Cacheable(value = "divsByFundOpCache")
    public List<Division> getDivisionsByFundingOpIdAndDirectorateId(String fundingOpId, String directorateId)
            throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getDivisionsByFundingOpIdAndDirectorateId()");
        try {
            return solicitationDataServiceclient.getDivisionsByFundingOpIdAndDirectorateId(fundingOpId, directorateId);
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_DIVISIONS_BY_FUNDING_OPP_DIRECTORATE_ID_ERROR, e);
        }
    }

    @Override
    @Cacheable(value = "progsByFundOpCache")
    public List<ProgramElement> getProgramElementsByDivisionId(String fundingOpId, String directorateId,
            String divisionId) throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getProgramElementsByDivisionId()");
        try {
            return solicitationDataServiceclient.getProgramElementsByDivisionId(fundingOpId, null, divisionId);
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_PROGRAM_ELEMENTS_BY_DIVISION_ID_ERROR, e);
        }
    }

    @Override
    @Cacheable(value = "dirByFundOpCache")
    public Directorate getDirectorateByFundingOpId(String fundingOpId, String directorateId)
            throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getDirectorateByFundingOpId()");
        try {
            return solicitationDataServiceclient.getDirectorateByFundingOpId(fundingOpId, directorateId);
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_DIRECTORATE_BY_FUNDING_OPP_ID_ERROR, e);
        }
    }
    
    @Override
    @Cacheable(value = "loginUserPermissionsCache")
    public List<ProposalPermission> getUserPermissions(String propStatusCode, String[] roleCodes) throws CommonUtilException {
    	LOGGER.debug("CachedDataServiceImpl.getUserPermissions()");
        return proposalDataServiceClient.getLoginUserPermissions(propStatusCode, roleCodes);
    }
    
    @Override
    @Cacheable(value = "countriesCache", key = "#root.method.name")
    public List<Country> getCountries() throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getCountries()");
        try {
            return solicitationDataServiceclient.getCountries();
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_ALL_COUNTRIES_ERROR, e);
        }
    }
    
    @Override
    @Cacheable(value = "proposalTypesCache", key = "#root.method.name")
    public List<ProposalTypeLookUp> getProposalTypeLookUp() throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getProposalTypeLookUp()");
        try {
            return proposalDataServiceClient.getProposalTypes();
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }
    
    @Override
    @Cacheable(value = "collabTypesCache", key = "#root.method.name")
    public List<CollaborativeTypeLookUp> getCollabTypeLookUp() throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getCollabTypeLookUp()");
        try {
            return proposalDataServiceClient.getCollaborativeTypes();
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }
    
    @Override
    @Cacheable(value = "othPersRoleTypesCache", key = "#root.method.name")
    public List<OtherPersonnelRoleTypeLookUp> getOtherPersonnelRoleTypeLookup() throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getOtherPersonnelRoleTypeLookup()");
        try {
            return proposalDataServiceClient.getOtherPersonnelTypes();
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }

    @Override
    @Cacheable(value = "instRoleTypesCache", key = "#root.method.name")
    public List<InstitutionRoleTypeLookUp> getInstitutionRoleTypeLookup() throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getInstitutionRoleTypeLookup()");
        try {
            return proposalDataServiceClient.getInstitutionTypes();
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }

    @Override
    @Cacheable(value = "loginUserRoleTypesCache", key = "#root.method.name")
    public List<LoginUserRoleType> getLoginUserRoleTypeLookUp() throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getLoginUserRoleTypeLookUp()");
        try {
            return proposalDataServiceClient.getLoginUserRoleTypes();
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }
    
    @Override
    @Cacheable(value = "submissionTypesCache", key = "#root.method.name")
    public List<SubmissionTypeLookUp> getSubmissionTypeLookUp() throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getSubmissionTypeLookUp()");
        try {
            return proposalDataServiceClient.getSubmissionTypes();
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }
	
    @Override
    @Cacheable(value = "senrPersRoleTypesCache", key = "#root.method.name")
    public List<SeniorPersonRoleTypeLookUp> getSeniorPersonRoleTypeLookup() throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getSeniorPersonRoleTypeLookup()");
        try {
            return proposalDataServiceClient.getSeniorPersonTypes();
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }
    
    @Override
    @Cacheable(value = "statesCache", key = "#root.method.name")
    public List<State> getStates() throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getStates()");
        try {
            return solicitationDataServiceclient.getStates();
        } catch (CommonUtilException e) {
            throw new CommonUtilException(Constants.GET_ALL_STATES_ERROR, e);
        }
    }

    @Override
    @Cacheable(value = "deadlineTypesCache", key = "#root.method.name")
    public List<DeadlineTypeLookUp> getDeadlineTypes() throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getDeadlineTypes()");
        try {
            return proposalDataServiceClient.getDeadlineTypes();
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }

	@Override
    @Cacheable(value = "udsUserDataCache")
	public UdsGetUserDataResponse getUserData(String nsfId) throws CommonUtilException {
        LOGGER.debug("CachedDataServiceImpl.getUserData()");
        try {
			return externalServices.getUserData(nsfId);
		} catch (Exception e) {
			LOGGER.error("Encountered error while searching by nsfId {} in UDS", nsfId, e);
			throw new CommonUtilException(e);
		}
	}
    
}
