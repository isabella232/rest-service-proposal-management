package gov.nsf.psm.propmgt.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.nsf.psm.foundation.controller.PsmBaseController;
import gov.nsf.psm.foundation.ember.model.EmberModel;
import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.Directorate;
import gov.nsf.psm.foundation.model.Division;
import gov.nsf.psm.foundation.model.FundingOpportunity;
import gov.nsf.psm.foundation.model.ProgramElement;
import gov.nsf.psm.foundation.model.Section;
import gov.nsf.psm.foundation.model.lookup.CollaborativeTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.Country;
import gov.nsf.psm.foundation.model.lookup.DeadlineTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.InstitutionRoleTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.OtherPersonnelRoleTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.ProposalTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.SeniorPersonRoleTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.State;
import gov.nsf.psm.foundation.model.lookup.SubmissionTypeLookUp;
import gov.nsf.psm.propmgt.service.CachedDataService;
import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/v1")
public class ProposalManagementLookupController extends PsmBaseController {
	
	@Autowired
	CachedDataService cachedDataService;

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass()); 

	@ApiOperation(value = "Get all funding opportunity objects", notes = "This API returns the list of all funding opportunity objects", response = FundingOpportunity.class, responseContainer = "List")
	@RequestMapping(path = "/fundingOps", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getFundingOpportunities() throws CommonUtilException {
		
		List<FundingOpportunity> fundingOpsList = cachedDataService.getAllFundingOpportunities();
		return new EmberModel.Builder<>(FundingOpportunity.class, fundingOpsList).build();
	}
	

	/*@ApiOperation(value = "Get all funding opportunity objects", notes = "This API returns the list of all funding opportunity with exclusion objects", response = FundingOpportunity.class, responseContainer = "List")
	@RequestMapping(path = "/fundingOpsExclusion", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getFundingOpportunitiesExclusion() throws CommonUtilException {
		
		List<FundingOpportunity> fundingOpsList = cachedDataService.getAllFundingOpportunitiesExclusion();
		return new EmberModel.Builder<>(FundingOpportunity.class, fundingOpsList).build();
	}*/

	@ApiOperation(value = "Get all division objects", notes = "This API returns the list of all division objects", response = Division.class, responseContainer = "List")
	@RequestMapping(path = "/divisions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getDivisions() throws CommonUtilException {
		LOGGER.debug("ProposalManagementLookupController.getAllDivisions()");
		
		List<Division> divisionsList = cachedDataService.getAllDivisions();
		return new EmberModel.Builder<>(Division.class, divisionsList).build();
	}

	@ApiOperation(value = "Get all directorate objects", notes = "This API returns the list of all directorate objects", response = Directorate.class, responseContainer = "List")
	@RequestMapping(path = "/directorates", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getDirectorates() throws CommonUtilException {
		LOGGER.debug("ProposalManagementLookupController.getAllDirectorates()");
		
		List<Directorate> directoratesList = cachedDataService.getAllDirectorates();
		return new EmberModel.Builder<>(Directorate.class, directoratesList).build();
	}

	@ApiOperation(value = "Get all program element objects", notes = "This API returns the list of all program element objects", response = ProgramElement.class, responseContainer = "List")
	@RequestMapping(path = "/programElements", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getProgramElements() throws CommonUtilException {
		LOGGER.debug("ProposalManagementLookupController.getAllProgramElements()");
		
		List<ProgramElement> programElementsList = cachedDataService.getAllProgramElements();
		return new EmberModel.Builder<>(ProgramElement.class, programElementsList).build();
	}

	@ApiOperation(value = "Get all directorates objects for a given funding opportunity id", notes = "This API returns the list of program element objects", response = Directorate.class, responseContainer = "List")
	@RequestMapping(path = "/fundingOps/{fundingOpId}/directorates", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getDirectoratesByFundingOpId(@PathVariable String fundingOpId) throws CommonUtilException {
		LOGGER.debug("ProposalManagementLookupController.getDirectoratesByFundingOpId()");
		
		List<Directorate> directoratesList = cachedDataService.getDirectoratesByFundingOpId(fundingOpId);
		return new EmberModel.Builder<>(Directorate.class, directoratesList).build();
	}

	@ApiOperation(value = "Get a directorate object for a given funding opportunity id", notes = "This API returns a directorate object", response = Directorate.class)
	@RequestMapping(path = "/fundingOps/{fundingOpId}/directorates/{directorateId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getDirectorateByFundingOpId(@PathVariable String fundingOpId, @PathVariable String directorateId)
	        throws CommonUtilException {
		LOGGER.debug("ProposalManagementLookupController.getDirectorateByFundingOpId()");
		
		Directorate directorate = cachedDataService.getDirectorateByFundingOpId(fundingOpId, directorateId);
		return new EmberModel.Builder<>("directorate", directorate).build();
	}

	@ApiOperation(value = "Get all division objects for a given funding opportunity id and directorate id", notes = "This API returns the list of division objects for a given opportunity id and directorate id", response = Division.class, responseContainer = "List")
	@RequestMapping(path = "/fundingOps/{fundingOpId}/directorates/{directorateId}/divisions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getDivisionsByFundingOpIdAndDirectorateId(@PathVariable String fundingOpId,
	        @PathVariable String directorateId) throws CommonUtilException {
		LOGGER.debug("ProposalManagementLookupController.getDivisionsByFundingOpIdAndDirectorateId()");
		
		List<Division> divisionsList = cachedDataService.getDivisionsByFundingOpIdAndDirectorateId(fundingOpId, directorateId);
		return new EmberModel.Builder<>(Division.class, divisionsList).build();
	}

	@ApiOperation(value = "Get all program element objects for a given funding opportunity id and division id", notes = "This API returns a list of program element objects", response = ProgramElement.class, responseContainer = "List")
	@RequestMapping(path = "/fundingOps/{fundingOpId}/directorates/{directorateId}/divisions/{divisionId}/programElements", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getProgramElementsByDivisionId(@PathVariable String fundingOpId,
	        @PathVariable String directorateId, @PathVariable String divisionId) throws CommonUtilException {
		LOGGER.debug("ProposalManagementLookupController.getProgramElementsByDivisionId()");
		
		List<ProgramElement> programElementsList = cachedDataService
			        .getProgramElementsByDivisionId(fundingOpId, directorateId, divisionId);
		return new EmberModel.Builder<>(ProgramElement.class, programElementsList).build();
	}

	@ApiOperation(value = "Get a list of proposal types", notes = "This API returns a list of all proposal types", response = ProposalTypeLookUp.class, responseContainer = "List")
	@RequestMapping(path = "/proposalTypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getProposalTypes() throws CommonUtilException {
		LOGGER.debug("ProposalManagementLookupController.getAllProposalTypes()");
		
		List<ProposalTypeLookUp> propTypeList = cachedDataService.getProposalTypeLookUp();
		return new EmberModel.Builder<>(ProposalTypeLookUp.class, propTypeList).build();
	}

	@ApiOperation(value = "Get a list of submission types", notes = "This API returns a list of all submission types", response = SubmissionTypeLookUp.class, responseContainer = "List")
	@RequestMapping(path = "/submissionTypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getSubmissionTypes() throws CommonUtilException {
		LOGGER.debug("ProposalManagementLookupController.getAllSubmissionTypes()");
		
		List<SubmissionTypeLookUp> submTypeList = cachedDataService.getSubmissionTypeLookUp();
		return new EmberModel.Builder<>(SubmissionTypeLookUp.class, submTypeList).build();
	}

	@ApiOperation(value = "Get a list of collaboration types", notes = "This API returns a list of all collaboration types", response = CollaborativeTypeLookUp.class, responseContainer = "List")
	@RequestMapping(path = "/collaborativeTypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getCollaborativeTypes() throws CommonUtilException {
		LOGGER.debug("ProposalManagementLookupController.getAllCollaborativeTypes()");
		
		List<CollaborativeTypeLookUp> collabTypeList = cachedDataService.getCollabTypeLookUp();
		return new EmberModel.Builder<>(CollaborativeTypeLookUp.class, collabTypeList).build();
	}

	@ApiOperation(value = "Get a list of proposal section types", notes = "This API returns a list of all proposal section types", response = Section.class, responseContainer = "List")
	@RequestMapping(path = "/proposalSections", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getProposalSections() throws CommonUtilException {
		LOGGER.debug("ProposalManagementLookupController.getPropSections()");
		
		return new EmberModel.Builder<Section>("proposalSections", Section.getPropSectionMap()).build();
	}

	@ApiOperation(value = "Get a list of other personnel types", notes = "This API returns a list of all other personnel types", response = OtherPersonnelRoleTypeLookUp.class, responseContainer = "List")
	@RequestMapping(path = "/otherPersonnelRoleTypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getOtherPersonnelRoleTypes() throws CommonUtilException {
		LOGGER.debug("ProposalManagementLookupController.getSeniorPersonRoleTypes()");
		
		List<OtherPersonnelRoleTypeLookUp> otherPersonnelTypeList = cachedDataService.getOtherPersonnelRoleTypeLookup();
		return new EmberModel.Builder<OtherPersonnelRoleTypeLookUp>(OtherPersonnelRoleTypeLookUp.class, otherPersonnelTypeList).build();
	}

	@ApiOperation(value = "Get a list of institution role types", notes = "This API returns a list of all institution role types", response = InstitutionRoleTypeLookUp.class, responseContainer = "List")
	@RequestMapping(path = "/institutionRoleTypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getInstitutionRoleTypes() throws CommonUtilException {
		LOGGER.debug("ProposalManagementLookupController.getInstitutionRoleTypes()");
		
		List<InstitutionRoleTypeLookUp> instTypeList = cachedDataService.getInstitutionRoleTypeLookup();
		return new EmberModel.Builder<InstitutionRoleTypeLookUp>(InstitutionRoleTypeLookUp.class, instTypeList).build();
	}

	@ApiOperation(value = "Get a list of senior personnel role types", notes = "This API returns a list of all senior personnel role types", response = SeniorPersonRoleTypeLookUp.class, responseContainer = "List")
	@RequestMapping(path = "/seniorPersonnelRoleTypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getSeniorPersonRoleTypes() throws CommonUtilException {
		LOGGER.debug("ProposalManagementLookupController.getSeniorPersonRoleTypes()");
		
		List<SeniorPersonRoleTypeLookUp> seniorPersonTypeList = cachedDataService.getSeniorPersonRoleTypeLookup();
		return new EmberModel.Builder<SeniorPersonRoleTypeLookUp>(SeniorPersonRoleTypeLookUp.class, seniorPersonTypeList).build();
	}

	@ApiOperation(value = "Get all state objects", notes = "This API returns the list of all states  objects", response = FundingOpportunity.class, responseContainer = "List")
	@RequestMapping(path = "/states", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getStates() throws CommonUtilException {
		LOGGER.info("ProposalManagementLookupController.getStates()");
		
		List<State> states = cachedDataService.getStates();
		return new EmberModel.Builder<>(State.class, states).build();
	}
	
	@ApiOperation(value = "Get all country objects", notes = "This API returns the list of all country  objects", response = FundingOpportunity.class, responseContainer = "List")
	@RequestMapping(path = "/countries", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getCountries() throws CommonUtilException {
		LOGGER.info("ProposalManagementLookupController.getCountries()");
		
		List<Country> countries = cachedDataService.getCountries();
		return new EmberModel.Builder<>(Country.class, countries).build();
	}
	
	@ApiOperation(value = "Get all deadline types", notes = "This API returns the list of all deadline types", response = DeadlineTypeLookUp.class, responseContainer = "List")
	@RequestMapping(path = "/deadlineTypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getDeadlineTypes() throws CommonUtilException {
		LOGGER.info("ProposalManagementLookupController.getDeadlineTypes()");
		
		List<DeadlineTypeLookUp> deadlineTypes = cachedDataService.getDeadlineTypes();
		return new EmberModel.Builder<>(DeadlineTypeLookUp.class, deadlineTypes).build();
	}
}
