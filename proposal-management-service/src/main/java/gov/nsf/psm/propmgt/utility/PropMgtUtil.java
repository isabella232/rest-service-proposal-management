package gov.nsf.psm.propmgt.utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.springframework.util.StringUtils;

import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.BaseModel;
import gov.nsf.psm.foundation.model.Institution;
import gov.nsf.psm.foundation.model.PSMMessage;
import gov.nsf.psm.foundation.model.PSMMessageType;
import gov.nsf.psm.foundation.model.PSMRole;
import gov.nsf.psm.foundation.model.Personnel;
import gov.nsf.psm.foundation.model.Personnels;
import gov.nsf.psm.foundation.model.Section;
import gov.nsf.psm.foundation.model.login.InstitutionRole;
import gov.nsf.psm.foundation.model.login.User;
import gov.nsf.psm.foundation.model.lookup.DeadlineTypeLookUp;
import gov.nsf.psm.propmgt.common.Constants;
import gov.nsf.service.model.UdsAgencyIdentity;
import gov.nsf.service.model.UdsGetUserDataResponse;
import gov.nsf.service.model.UdsNsfIdUserData;
import gov.nsf.userdata.api.model.AgencyIdentity;
import gov.nsf.userdata.api.model.UserData;

public class PropMgtUtil {

	public static final String ORIG_FILENAME_REGEX = "(?i)^.*filename=\"([^\"]+)\".*$";
	public static final String NSFID = "nsfId";

	private PropMgtUtil() {

	}

	/**
	 * Retrieves the original file name from the request
	 *
	 * @param request
	 * @param name
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public static String getOrigFileName(Part part) throws IOException {
		String fileName = "";
		String disposition = part.getHeader("Content-Disposition");
		fileName = disposition.replaceFirst("(?i)^.*filename=\"([^\"]+)\".*$", "$1");

		return fileName;
	}

	public static Personnels convertUdsGetUserDataResponse(UdsGetUserDataResponse udsGetUserDataResponse,
			PSMRole psmRole) {
		Personnels personnels = new Personnels();
		UdsNsfIdUserData udsNsfIdUserData = udsGetUserDataResponse.getUserData();
		for (UdsAgencyIdentity udsAgencyIdentity : udsNsfIdUserData.getAgencyIdentities()) {
			List<String> agencyRoles = udsAgencyIdentity.getRgovRole();
			for (String role : agencyRoles) {
				if (role.equalsIgnoreCase(psmRole.getUserDataServiceRole())) {
					Personnel personnel = populatePersonnel(udsNsfIdUserData, udsAgencyIdentity);
					personnel.setPSMRole(psmRole);
					personnels.addPersonnel(personnel);
				}
			}
		}
		return personnels;
	}

	public static Personnels convertUdsGetUserDataResponse(UdsGetUserDataResponse udsGetUserDataResponse) {
		Personnels personnels = new Personnels();
		UdsNsfIdUserData udsNsfIdUserData = udsGetUserDataResponse.getUserData();
		for (UdsAgencyIdentity udsAgencyIdentity : udsNsfIdUserData.getAgencyIdentities()) {
			Personnel personnel = populatePersonnel(udsNsfIdUserData, udsAgencyIdentity);
			personnels.addPersonnel(personnel);
		}
		return personnels;
	}

	/**
	 * Convert a list of User Data (from UDS) to Personnel model
	 * 
	 * @param userDataList
	 * @return
	 */
	public static Personnels convertUserDataToPersonnel(List<UserData> userDataList) {
		Personnels personnels = new Personnels();

		for (UserData userData : userDataList) {
			for (AgencyIdentity agencyIdentity : userData.getAgencyIdentities()) {
				Personnel personnel = populatePersonnel(userData, agencyIdentity);
				personnels.addPersonnel(personnel);
			}
		}

		return personnels;
	}

	/**
	 * Convert a list of User Data (from UDS) to Personnel model with PSMRole filter
	 * 
	 * @param userDataList
	 * @param psmRole
	 * @return
	 */
	public static Personnels convertUserDataToPersonnel(List<UserData> userDataList, PSMRole psmRole) {
		Personnels personnels = new Personnels();

		for (UserData userData : userDataList) {
			for (AgencyIdentity agencyIdentity : userData.getAgencyIdentities()) {
				List<String> agencyRoles = agencyIdentity.getRgovRole();
				for (String role : agencyRoles) {
					if (role.equalsIgnoreCase(psmRole.getUserDataServiceRole())) {
						Personnel personnel = populatePersonnel(userData, agencyIdentity);
						personnel.setPSMRole(psmRole);
						personnels.addPersonnel(personnel);
					}
				}
			}
		}

		return personnels;
	}

	/**
	 * Populate Personnel model from User Data model.
	 * 
	 * @param userData
	 * @param agencyIdentity
	 * @return
	 */
	private static Personnel populatePersonnel(UserData userData, AgencyIdentity agencyIdentity) {
		Personnel personnel = new Personnel();

		personnel.setEmail(userData.getEmail().replaceAll("(?<=.{1}).(?=.*@)", "*"));
		personnel.setFirstName(userData.getFirstName());
		personnel.setLastName(userData.getLastName());
		personnel.setMiddleName(userData.getMiddleInitial());
		personnel.setNsfId(userData.getUserID());
		personnel.setPhoneNumber(userData.getPhoneNumber());

		Institution institution = new Institution();
		institution.setId(agencyIdentity.getInstitutionIdentifier().getInstitID());
		institution.setOrganizationName(agencyIdentity.getInstitutionName());
		institution.setDunsNumber(agencyIdentity.getInstitutionIdentifier().getDUNSNumber());
		personnel.setInstitution(institution);

		return personnel;
	}

	public static boolean hasErrorMessages(List<PSMMessage> msgList) {
		long errCount = msgList.stream().filter(PSMMessage -> PSMMessageType.ERROR.equals(PSMMessage.getType()))
				.count();
		return errCount > 0;
	}

	private static Personnel populatePersonnel(UdsNsfIdUserData udsNsfIdUserData, UdsAgencyIdentity udsAgencyIdentity) {
		Personnel personnel = new Personnel();
		personnel.setEmail(udsNsfIdUserData.getEmail().replaceAll("(?<=.{1}).(?=.*@)", "*"));
		personnel.setFirstName(udsNsfIdUserData.getFirstName());
		personnel.setLastName(udsNsfIdUserData.getLastName());
		personnel.setMiddleName(udsNsfIdUserData.getMiddleInitial());
		personnel.setNsfId(udsNsfIdUserData.getUserID());
		personnel.setPhoneNumber(udsNsfIdUserData.getPhoneNumber());
		Institution institution = new Institution();
		institution.setId(udsAgencyIdentity.getInstitutionIdentifier().getInstitID());
		institution.setOrganizationName(udsAgencyIdentity.getInstitutionName());
		institution.setDunsNumber(udsAgencyIdentity.getInstitutionIdentifier().getDunsNumber());
		personnel.setInstitution(institution);
		return personnel;
	}

	public static User convertUdsGetUserDataResponseToUser(UdsNsfIdUserData userNsfIdUserData,
			Map<String, PSMRole> psmRoles) {
		User user = new User();

		// add inst roles
		List<InstitutionRole> institutionRoles = new ArrayList<InstitutionRole>();
		// cycle through the user's institutions		
		for (UdsAgencyIdentity udsAgencyIdentity : userNsfIdUserData.getAgencyIdentities()) {
			InstitutionRole instRole = new InstitutionRole();

			Institution inst = new Institution();
			inst.setId(udsAgencyIdentity.getInstitutionIdentifier().getInstitID());
			inst.setOrganizationName(udsAgencyIdentity.getInstitutionName());
			inst.setDunsNumber(udsAgencyIdentity.getInstitutionIdentifier().getDunsNumber());
			// capture only PSM-relevant roles for each institution
			List<PSMRole> psmRoleList = new ArrayList<PSMRole>();
			boolean hasNonPSMRoles = false;
			
			for (String udsRole : udsAgencyIdentity.getRgovRole()) {
				if (psmRoles.containsKey(udsRole)) {
					psmRoleList.add(psmRoles.get(udsRole));
				} else {
					hasNonPSMRoles = true;
				}
			}
			// if user has any other roles, then add OAU role
			if(hasNonPSMRoles) {
				psmRoleList.add( psmRoles.get(PSMRole.OAU_ROLE_ABRV ) );
			}
			
			instRole.setInstitution(inst);
			instRole.setRoles(psmRoleList);
			institutionRoles.add(instRole);
		}

		// add user data
		user.setFirstName(userNsfIdUserData.getFirstName());
		user.setLastName(userNsfIdUserData.getLastName());
		user.setMiddleInitial(userNsfIdUserData.getMiddleInitial());
		user.setNsfId(userNsfIdUserData.getUserID());
		user.setEmailId(userNsfIdUserData.getEmail());
		user.setPhoneNumber(userNsfIdUserData.getPhoneNumber());		
		user.setInstitutionRoles(institutionRoles);
		
		return user;
	}
	
	/**
	 * Add PI role to other inst if user has no PI role on other inst
	 * 
	 * @param inputInstRole
	 * @param psmRoles
	 * @return
	 */
	private static List<InstitutionRole> addPIRoleToAllInst(List<InstitutionRole> inputInstRole, Map<String, PSMRole> psmRoles){
		if(inputInstRole.isEmpty()) {
			return inputInstRole;
		}
		
		List<InstitutionRole> outputInstRole = inputInstRole;
		for(InstitutionRole instRole : outputInstRole) {
			Map<String, PSMRole> roleMap = convertRolesToMap( instRole.getRoles() );
			if(!roleMap.containsKey(PSMRole.PI_ROLE_ABRV)) {
				instRole.getRoles().add( psmRoles.get(PSMRole.PI_ROLE_ABRV ) );
			}
		}
		return outputInstRole;
	}
	
	/**
	 * Converts role list to map
	 *  
	 * @param psmRoleList
	 * @return
	 */
	private static Map<String, PSMRole> convertRolesToMap(List<PSMRole> psmRoleList) {
		Map<String, PSMRole> roleMap = new HashMap<String, PSMRole>();
		for(PSMRole role : psmRoleList) {
			roleMap.put(role.getAbbreviation(), role);
		}
		return roleMap;
	}

	/**
	 * Retrieves Proposal Section Map
	 *
	 * @return
	 */
	public static Map<Section, Map<String, String>> getPropSectionMap() throws CommonUtilException {

		List<Section> secList = Section.getSectionList();
		Map<Section, Map<String, String>> allSectionMap = new EnumMap<Section, Map<String, String>>(Section.class);

		for (Section section : secList) {
			Map<String, String> sectionMap = new HashMap<String, String>();
			sectionMap.put("code", section.getCode());
			sectionMap.put("name", section.getName());
			sectionMap.put("camelCaseName", section.getCamelCaseName());
			sectionMap.put("entryType", section.getEntryType().toString());
			allSectionMap.put(section, sectionMap);
		}
		return allSectionMap;
	}

	public static void setAuditFields(BaseModel model, String nsfId) {

		if (nsfId != null) {
			model.setLastUpdatedUser(nsfId);
		} else {
			model.setLastUpdatedUser("psm");
		}

	}
	

	public static boolean isWithinWindowOfOpportunity(Date date) {

		Date today = new Date();

		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(today);
		gc.add(GregorianCalendar.MONTH, -1);
		Date lowerEnd = gc.getTime();

		gc.setTime(lowerEnd);
		gc.add(GregorianCalendar.MONTH, 13);
		Date upperEnd = gc.getTime();

		return !(date.before(lowerEnd) || date.after(upperEnd));

	}

	public static boolean isInProgress(int revNum) {
		if (revNum < Constants.MAX_REVN_NUM) {
			return true;
		} else {
			return false;
		}
	}

	public static List<Section> getFullProposalPrintOrder() {
		List<Section> sectionOrderList = new ArrayList<Section>();
		sectionOrderList.add(Section.COVER_SHEET);
		sectionOrderList.add(Section.PROJ_SUMM);
		sectionOrderList.add(Section.TABLE_OF_CONTENTS);
		sectionOrderList.add(Section.PROJ_DESC);
		sectionOrderList.add(Section.REF_CITED);
		sectionOrderList.add(Section.BIOSKETCH);
		sectionOrderList.add(Section.OPBIO);
		sectionOrderList.add(Section.BUDGETS);// inlcuding budget justification
		sectionOrderList.add(Section.CURR_PEND_SUPP);
		sectionOrderList.add(Section.FER);
		// Other Supplementary Docs
		sectionOrderList.add(Section.DMP);
		sectionOrderList.add(Section.PMP);
		sectionOrderList.add(Section.OSD);
		// GOALI-Industrial PI Confirmation Letter
		sectionOrderList.add(Section.COA);
		// RAISE - Program
		// Deviation Authorization
		sectionOrderList.add(Section.SRL);
		sectionOrderList.add(Section.RNI);
		// Additional Single Copy Documents
		// Nature of Natural or Anthropogenic Event
		return sectionOrderList;
	}

	public static String getUserIPAddress(HttpServletRequest request) throws CommonUtilException {

		String userIP = null;

		if (request != null) {
			userIP = request.getHeader("X-FORWARDED-FOR");
			if (userIP == null || "".equals(userIP)) {
				userIP = request.getRemoteAddr();
			}
		}

		return userIP;
	}
	
	public static String formatAORName(User user) {
        if (user.getFirstName() != null && user.getLastName() != null && user.getMiddleInitial() != null) {
            return user.getFirstName() + ' ' + user.getMiddleInitial() + ' ' + user.getLastName();
        } else if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + ' ' + user.getLastName();
        } else {
            return user.getLastName();
        }
    }

}
