package gov.nsf.psm.propmgt.controller;

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
import gov.nsf.psm.foundation.model.login.User;
import gov.nsf.psm.foundation.model.login.UserProfile;
import gov.nsf.psm.foundation.model.proposal.ProposalPermission;
import gov.nsf.psm.propmgt.service.UserDetailsService;
import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/v1/user")
public class UserDetailsController extends PsmBaseController {

	@Autowired
	UserDetailsService userDetailsService;
	
	@RequestMapping(path = "/{token}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getUserDetails(@PathVariable String token) throws CommonUtilException {
		User user = userDetailsService.getUserDetailsFromUDS(token);
		return new EmberModel.Builder<>("UserData", user).build();
	}

	@ApiOperation(value = "Get a list of personnel permissions", notes = "This API retrieves a list of personnel permissions for a given NSF ID, proposal prep id, and proposal revision id", response = ProposalPermission.class)
	@RequestMapping(path = "/{nsfId}/permissions/{propPrepId}/{revId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getProposalPermissions(@PathVariable String nsfId, @PathVariable String propPrepId,
			@PathVariable String revId) throws CommonUtilException {

		UserProfile userProfile = userDetailsService.getUserProfile(nsfId, propPrepId, revId);
		return new EmberModel.Builder<>(UserProfile.getClassCamelCaseName(), userProfile).build();
	}

	@RequestMapping(path = "/user/{nsfId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public EmberModel getUserDetailsByNsfId(@PathVariable String nsfId) throws CommonUtilException {
		User user = userDetailsService.getUserDetailsByNsfId(nsfId);
		return new EmberModel.Builder<>("UserData", user).build();
	}
}
