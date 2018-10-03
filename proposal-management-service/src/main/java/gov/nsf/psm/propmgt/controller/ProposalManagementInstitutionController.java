package gov.nsf.psm.propmgt.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.nsf.psm.foundation.controller.PsmBaseController;
import gov.nsf.psm.foundation.ember.model.EmberModel;
import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.Institution;
import gov.nsf.psm.propmgt.service.ExternalServices;
import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/v1")
public class ProposalManagementInstitutionController extends PsmBaseController {

	@Autowired
	ExternalServices externalServices;

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass()); 

	@ApiOperation(value = "Get a specific institution information", notes = "This API returns the a specific institution information")
	@RequestMapping(path = "/institution/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
 	public EmberModel getInstitutionById(@PathVariable String id) throws CommonUtilException {
		LOGGER.info("ProposalManagementInstitutionController.getInstitutionById()");
		
		Institution institution = externalServices.getInstitutionById(id);
		return new EmberModel.Builder<>("institution", institution).build();
	}

	@ApiOperation(value = "Search institutions", notes = "This API returns a list of institutions for the given search parameters")
	@RequestMapping(path = "/institutions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel searchInstitutions(@RequestParam Map<String, String> searchCriteria) throws CommonUtilException {
		LOGGER.info("ProposalManagementInstitutionController.searchInstitutions()");
		
		List<Institution> institutions = externalServices.getSearchInstitutions(searchCriteria);
		return new EmberModel.Builder<>(Institution.class, institutions).build();
	}

}
