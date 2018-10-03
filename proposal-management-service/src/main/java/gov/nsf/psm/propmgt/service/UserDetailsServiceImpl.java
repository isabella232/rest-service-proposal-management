package gov.nsf.psm.propmgt.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.Institution;
import gov.nsf.psm.foundation.model.PSMRole;
import gov.nsf.psm.foundation.model.Personnel;
import gov.nsf.psm.foundation.model.login.InstitutionRole;
import gov.nsf.psm.foundation.model.login.User;
import gov.nsf.psm.foundation.model.login.UserProfile;
import gov.nsf.psm.foundation.model.lookup.LoginUserRoleType;
import gov.nsf.psm.foundation.model.lookup.SeniorPersonRoleTypeLookUp;
import gov.nsf.psm.foundation.model.proposal.ProposalPackage;
import gov.nsf.psm.foundation.model.proposal.ProposalPermission;
import gov.nsf.psm.foundation.model.proposal.ProposalStatus;
import gov.nsf.psm.openam.model.OpenAMUserInfo;
import gov.nsf.psm.propmgt.common.Constants;
import gov.nsf.psm.propmgt.common.GappsStatuses;
import gov.nsf.psm.propmgt.common.PropMgtMessagesEnum;
import gov.nsf.psm.propmgt.utility.PropMgtUtil;
import gov.nsf.psm.proposaldata.ProposalDataServiceClient;
import gov.nsf.research.services.gapps.v1.GappsSearchRequest;
import gov.nsf.research.services.gapps.v1.GappsSearchResponse;
import gov.nsf.research.services.gapps.v1.GrantApplicationListRowLite;
import gov.nsf.research.services.gapps.v1.GrantApplicationRequest;
import gov.nsf.service.model.UdsAgencyIdentity;
import gov.nsf.service.model.UdsGetUserDataResponse;
import gov.nsf.service.model.UdsNsfIdUserData;

public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	CachedDataService cachedDataService;

	@Autowired
	ProposalDataServiceClient proposalDataServiceClient;

	@Autowired
	ExternalServices externalServices;

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Override
	public User getUserDetailsFromUDS(String authToken) throws CommonUtilException {
		LOGGER.info("UserDetailsServiceImpl.getUserDetailsFromUDS()");
		User user = new User();

		// Retrieve nsfId from OpenAM
		OpenAMUserInfo openAMUserInfo;
		try {
			openAMUserInfo = externalServices.getUserInfoForAccessToken(authToken);
		} catch (CommonUtilException e1) {
			LOGGER.error("No user details could be found with the associated token", e1);
			return user;
		}
		if (openAMUserInfo != null) {
			UdsGetUserDataResponse udsGetUserDataResponse = cachedDataService.getUserData(openAMUserInfo.getSub());
			user = PropMgtUtil.convertUdsGetUserDataResponseToUser(udsGetUserDataResponse.getUserData(),
					getLoginUserRoleMap());
		}
		LOGGER.info(user.toString());
		return user;
	}

	/*
	 * Retrieve the following components to determine user permissions, given
	 * proposal ids and user NSF ID - users' UDS profile(NSF ID) - proposal status
	 * code (proposal ID) - PSM Personnel record for user (NSF ID, proposal ID) If
	 * any of these components do not return a value, then return empty permissions
	 */

	@Override
	public UserProfile getUserProfile(String nsfId, String propPrepId, String revId) throws CommonUtilException {
		LOGGER.info("UserDetailsServiceImpl.getUserProfile()");
		UserProfile userProfile = new UserProfile();
		Set<PSMRole> userPSMRoleSet = new HashSet<PSMRole>();
		List<String> loginUserRoleCodes = new ArrayList<String>();
		Map<String, PSMRole> userPSMRoleMap = getLoginUserRoleMap();

		try {
			User udsUser = getUDSUserByNSFId(nsfId);
			LOGGER.debug("Retrieved UDS User: " + udsUser);
			String propStatusCode = getProposalStatus(propPrepId, revId);
			// TODO: Do we retrieve proposal institution from the PI?
			LOGGER.debug("Retrieved Proposal Status: " + propStatusCode);
			String propInstId = proposalDataServiceClient.getProposalPrep(propPrepId, revId).getPi().getInstitution()
					.getId();
			LOGGER.debug("Proposal Institution: " + propInstId);
			// user has no roles assigned in UDS
			if (CollectionUtils.isEmpty(udsUser.getInstitutionRoles())) {
				// return message(?)
				userProfile.getMessages().addMessage(PropMgtMessagesEnum.PM_E_006.getMessage());
				return userProfile;
			}

			// user is considered relevant to the proposal if they meet either
			// of the following criteria
			// 1. is a PI/co-PI/OAU listed on the proposal (for given
			// institution)
			// 2. is an AOR/SPO for the proposal's institution

			Personnel propPerson = getProposalPersonnelFromUDSUser(propPrepId, revId, udsUser.getNsfId());

			// the user exists as a senior key person on the proposal
			if (propPerson != null) {
				// determine if the PSM user has the assigned role for their
				// provided institution in UDS
				Optional<InstitutionRole> userUDSRolesForPersonInst = udsUser.getInstitutionRoles().stream().filter(
						instRole -> instRole.getInstitution().getId().equals(propPerson.getInstitution().getId()))
						.findFirst();
				// Found Institution roles for this user
				if (userUDSRolesForPersonInst.isPresent()) {
					InstitutionRole udsUserInstRole = userUDSRolesForPersonInst.get();
					if (udsUserInstRole != null && !CollectionUtils.isEmpty(udsUserInstRole.getRoles())) {
						PSMRole senrPersonPSMRole = constructPSMRoleFromPersonnelRoleCode(
								propPerson.getPSMRole().getCode());
						boolean psmRoleExistsInUDS = verifyPSMPersonnelRoleExistsInUDS(
								senrPersonPSMRole.getUserDataServiceRole(), udsUserInstRole.getRoles());
						if (psmRoleExistsInUDS) {
							PSMRole userSKPRole = userPSMRoleMap.get(senrPersonPSMRole.getAbbreviation());
							userPSMRoleSet.add(userSKPRole);
							loginUserRoleCodes.add(userSKPRole.getCode());
						}
					}
				}
			} else {
				LOGGER.info("propPerson is null");
			}

			// check whether user has AOR/SPO roles for the proposal's
			// institution
			Optional<InstitutionRole> userUDSRolesForPropInst = udsUser.getInstitutionRoles().stream()
					.filter(instRole -> instRole.getInstitution().getId().equals(propInstId)).findFirst();

			// Roles found for the proposal institution
			if (userUDSRolesForPropInst.isPresent()) {
				InstitutionRole UserPropInstPSMRoles = userUDSRolesForPropInst.get();
				if (UserPropInstPSMRoles != null && !CollectionUtils.isEmpty(UserPropInstPSMRoles.getRoles())) {

					boolean hasAORRoleInUDS = UserPropInstPSMRoles.getRoles().stream()
							.anyMatch(psmRole -> psmRole.getUserDataServiceRole().equals(Constants.AOR_ROLE_ABRV));
					boolean hasSPORoleInUDS = UserPropInstPSMRoles.getRoles().stream()
							.anyMatch(psmRole -> psmRole.getUserDataServiceRole().equals(Constants.SPO_ROLE_ABRV));

					if (hasAORRoleInUDS) {
						PSMRole userSKPRole = userPSMRoleMap.get(Constants.AOR_ROLE_ABRV);
						userPSMRoleSet.add(userSKPRole);
						loginUserRoleCodes.add(userSKPRole.getCode());
					}
					if (hasSPORoleInUDS) {
						PSMRole userSKPRole = userPSMRoleMap.get(Constants.SPO_ROLE_ABRV);
						userPSMRoleSet.add(userSKPRole);
						loginUserRoleCodes.add(userSKPRole.getCode());
					}
				}
			} else {
				LOGGER.info("Roles not found for institution - " + propInstId);
			}

			List<ProposalPermission> permissions = null;

			if (!loginUserRoleCodes.isEmpty()) {
				permissions = cachedDataService.getUserPermissions(propStatusCode,
						loginUserRoleCodes.toArray(new String[loginUserRoleCodes.size()]));
			} else {
				LOGGER.info("No loginUserRoleCodes found for institution - " + propInstId);
			}

			if (!CollectionUtils.isEmpty(permissions)) {
				userProfile.setPermissions(new HashSet<ProposalPermission>(permissions));
				userProfile.setRoles(userPSMRoleSet);
			} else {
				LOGGER.info("No permissions found for user " + nsfId + " in inst " + propInstId);
			}

		} catch (Exception e) {
			LOGGER.error(PropMgtMessagesEnum.PM_E_007.toString(), e);
			userProfile.getMessages().addMessage(PropMgtMessagesEnum.PM_E_007.getMessage());
			return userProfile;
		}

		return userProfile;
	}

	// TODO: remove from propmgt svc
	private Map<String, PSMRole> getLoginUserRoleMap() throws CommonUtilException {
		Map<String, PSMRole> loginUserRoleMap = new HashMap<String, PSMRole>();

		for (LoginUserRoleType role : cachedDataService.getLoginUserRoleTypeLookUp()) {
			// TODO: Not sure how to handle CO-PI/PI determination yet. Best
			// might be to have a login user role table which only contains PI
			// to resolve
			// UDS roles to Personnel roles
			loginUserRoleMap.put(role.getAbbreviation(),
					new PSMRole(role.getCode(), role.getDescription(), role.getAbbreviation(), role.getAbbreviation()));
		}

		return loginUserRoleMap;
	}

	private Personnel getProposalPersonnelFromUDSUser(String propPrepId, String revId, String udsNsfId)
			throws CommonUtilException {
		List<Personnel> personnel = proposalDataServiceClient.getPersonnels(propPrepId, revId);

		// there are no personnel on the proposal
		if (CollectionUtils.isEmpty(personnel)) {
			LOGGER.info("No personnel found on the proposal");
			// add message :- no personnel on proposal
			return null;
		}

		Optional<Personnel> psmUserStream = personnel.stream()
				.filter(p -> !p.getPSMRole().getCode().equals(Constants.OTHER_SKP_ROLE_CODE))
				.filter(p -> p.getNsfId().equals(udsNsfId)).findFirst();

		if (!psmUserStream.isPresent()) {
			LOGGER.info("User " + udsNsfId + " not found on proposal " + propPrepId + "|" + revId);
			// add message :- user not found in PSM for proposal
			return null;
		}

		return psmUserStream.get();
	}

	private PSMRole constructPSMRoleFromPersonnelRoleCode(String personnelRoleCode) throws CommonUtilException {
		// Use the proposal person's personnel role code to construct a PSMRole
		SeniorPersonRoleTypeLookUp seniorKeyPersonRole = cachedDataService.getSeniorPersonRoleTypeLookup().stream()
				.filter(roleType -> roleType.getCode().equals(personnelRoleCode)).findFirst().get();

		PSMRole psmUserRole = new PSMRole(seniorKeyPersonRole.getCode(), seniorKeyPersonRole.getDescription(),
				seniorKeyPersonRole.getAbbreviation(), seniorKeyPersonRole.getUserDataServiceRole());
		return psmUserRole;
	}

	/*
	 * Checks if (1) user has a Personnel role (PI, co-PI) that exists in UDS
	 * for the institution, or (2) user is listed as OAU on the proposal and has
	 * any role(s) for that institution
	 */
	private boolean verifyPSMPersonnelRoleExistsInUDS(String psmRoleUdsMatcher, List<PSMRole> userUDSInstPSMRoles) {
		if (userUDSInstPSMRoles.stream()
				.anyMatch(psmRole -> psmRole.getUserDataServiceRole().equals(psmRoleUdsMatcher))) {
			return true;
		} else if (psmRoleUdsMatcher.equals(Constants.OAU_ROLE_ABRV)
				&& CollectionUtils.isNotEmpty(userUDSInstPSMRoles)) {
			return true;
		}
		return false;
	}

	@Override
	public User getUDSUserByNSFId(String nsfId) throws CommonUtilException {
		UdsGetUserDataResponse udsGetUserDataResponse = cachedDataService.getUserData(nsfId);

		// doesn't exist in UDS
		if (udsGetUserDataResponse.getUserData() == null) {
			LOGGER.info("Did not find user " + nsfId + " from UDS.");
			return null;
		}

		return PropMgtUtil.convertUdsGetUserDataResponseToUser(udsGetUserDataResponse.getUserData(),
				getLoginUserRoleMap());
	}
	
	@Override
	public List<Institution> getInstitutionIdUserIsPI(String nsfId) throws CommonUtilException {
		User user = getUDSUserByNSFId(nsfId);
		List<Institution> piInstitutions = new ArrayList<Institution>();
		
		if(user != null) {
			for(InstitutionRole instRole : user.getInstitutionRoles()) {
				for (PSMRole psmRole : instRole.getRoles()) {
					if(PSMRole.PI_ROLE_ABRV.equals(psmRole.getAbbreviation())) {
						piInstitutions.add(instRole.getInstitution());
					}
				}
			}
		}

		return piInstitutions;
	}

	private String getProposalStatus(String propPrepId, String revId) throws CommonUtilException {
		ProposalPackage proposal = proposalDataServiceClient.getProposalPrep(propPrepId, revId);
		if (proposal.getProposalStatus().trim().equals(ProposalStatus.NOT_FORWARDED_TO_SPO)
				|| proposal.getProposalStatus().trim().equals(ProposalStatus.VIEW_ONLY_SPO_AOR)
				|| proposal.getProposalStatus().trim().equals(ProposalStatus.VIEW_EDIT_SPO)
				|| proposal.getProposalStatus().trim().equals(ProposalStatus.RETURN_TO_PI)
				|| proposal.getProposalStatus().trim().equals(ProposalStatus.SUBMITTED_ACCESS_FOR_AOR)
				|| proposal.getProposalStatus().trim().equals(ProposalStatus.CANNOT_SUBMIT_VIEW_ONLY)) {
			return proposal.getProposalStatus();
		}
		Map<String, GrantApplicationListRowLite> gappsResults = new HashMap<String, GrantApplicationListRowLite>();
		GrantApplicationListRowLite application = null;
		Map<String, String> nsfIds = new HashMap<String, String>();
		if (!StringUtils.isEmpty(proposal.getPi().getNsfId())) {
			nsfIds.put(proposal.getPi().getNsfId(), proposal.getPi().getNsfId());
		}
		if (!StringUtils.isEmpty(proposal.getLatestSubmittedPiNsfId())) {
			nsfIds.put(proposal.getLatestSubmittedPiNsfId(), proposal.getLatestSubmittedPiNsfId());
		}
		for (Map.Entry<String, String> entry :nsfIds.entrySet()){
			gappsResults.putAll(findGrantApplicationForANsfId(entry.getValue()));
		}
		if (gappsResults.containsKey(proposal.getNsfPropId())) {
			application = gappsResults.get(proposal.getNsfPropId());
		}
		if (application != null && (GappsStatuses.getStatus(application.getStatusCode())
				.equalsIgnoreCase(ProposalStatus.GAPPS_AWARDED)
				|| GappsStatuses.getStatus(application.getStatusCode()).equalsIgnoreCase(ProposalStatus.GAPPS_DECLINED)
				|| GappsStatuses.getStatus(application.getStatusCode()).equalsIgnoreCase(ProposalStatus.GAPPS_RETURNED)
				|| GappsStatuses.getStatus(application.getStatusCode())
						.equalsIgnoreCase(ProposalStatus.GAPPS_WITHDRAWN))) {
			return ProposalStatus.CANNOT_SUBMIT_VIEW_ONLY;
		}
		if (application != null
				&& ((ProposalStatus.PFU_NOT_FORWARDED_TO_SPO.equals(proposal.getProposalStatus().trim()))
						|| (ProposalStatus.PFU_VIEW_ONLY_SPO_AOR.equals(proposal.getProposalStatus().trim()))
						|| (ProposalStatus.PFU_VIEW_EDIT_SPO.equals(proposal.getProposalStatus().trim()))
						|| (ProposalStatus.PFU_RETURN_TO_PI.equals(proposal.getProposalStatus().trim()))
						|| (ProposalStatus.PFU_SUBMITTED_ACCESS_FOR_AOR.equals(proposal.getProposalStatus().trim())))
				&& (GappsStatuses.getStatus(application.getStatusCode())
						.equalsIgnoreCase(ProposalStatus.GAPPS_RECOMMENDED)
						|| GappsStatuses.getStatus(application.getStatusCode())
								.equalsIgnoreCase(ProposalStatus.GAPPS_PENDING))) {
			return ProposalStatus.CANNOT_SUBMIT_VIEW_ONLY;
		}
		return proposal.getProposalStatus();
	}

	@Override
	public boolean userHasProposalPermission(String nsfId, String propPrepId, String revId, String permissionCode)
			throws CommonUtilException {
		UserProfile userProfile = getUserProfile(nsfId, propPrepId, revId);

		for (ProposalPermission perm : userProfile.getPermissions()) {
			if (perm.getPermissionCode().equals(permissionCode)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean userHasRole(String nsfId, String roleCode) throws CommonUtilException {
		User user;

		try {
			user = getUDSUserByNSFId(nsfId);
		} catch (Exception e) {
			throw new CommonUtilException(e);
		}

		for (InstitutionRole instRole : user.getInstitutionRoles()) {
			for (PSMRole psmRoles : instRole.getRoles()) {
				if (psmRoles.getCode().equals(roleCode)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean userHasSPOAORRole(String nsfId) throws CommonUtilException {
		boolean isSPOAOR = false;
		UdsNsfIdUserData userData = cachedDataService.getUserData(nsfId).getUserData();
		for (UdsAgencyIdentity id : userData.getAgencyIdentities()) {
			for (String role : id.getRgovRole()) {
				switch (role) {
				case PSMRole.AOR_ROLE_ABRV:
					isSPOAOR = true;
					break;
				case PSMRole.SPO_ROLE_ABRV:
					isSPOAOR = true;
					break;
				default:
					break;
				}
			}
		}
		return isSPOAOR;
	}

	@Override
	public boolean isValidUserToken(String token) throws CommonUtilException {
		LOGGER.debug("isValidUserToken - token: " + token);
		OpenAMUserInfo userInfo = externalServices.getUserInfoForAccessToken(token);
		LOGGER.debug("User Info: " + userInfo);
		return userInfo == null ? false : true;
	}

    private Map<String, GrantApplicationListRowLite> findGrantApplicationForANsfId(String nsfId) {
        Map<String, GrantApplicationListRowLite> gappsResults = new HashMap<String, GrantApplicationListRowLite>();
        try {
            GrantApplicationRequest grantApplicationRequest = new GrantApplicationRequest();
            grantApplicationRequest.setAgency("ALL");
            GappsSearchRequest gappsSearchRequest = new GappsSearchRequest();
            gappsSearchRequest.setUserId(nsfId);
            gappsSearchRequest.setGrantApplicationRequest(grantApplicationRequest);
            GappsSearchResponse gappsSearchResponse = externalServices.findGrantApplications(gappsSearchRequest);
            for (GrantApplicationListRowLite application : gappsSearchResponse.getGrantApplications()) {
                gappsResults.put(application.getGrantApplicationId(), application);
            }
        } catch (Exception e) {
            LOGGER.error(String.format(Constants.GET_SERVICE_IS_DOWN_ERROR, "RGOV GAPPS"), e);
        }
        return gappsResults;
    }

	@Override
	public User getUserDetailsByNsfId(String nsfId) throws CommonUtilException {
		UdsGetUserDataResponse udsGetUserDataResponse = cachedDataService.getUserData(nsfId);
		return PropMgtUtil.convertUdsGetUserDataResponseToUser(udsGetUserDataResponse.getUserData(),
				getLoginUserRoleMap());
	}
}
