package gov.nsf.psm.propmgt.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;

import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.Institution;
import gov.nsf.psm.foundation.model.login.User;
import gov.nsf.psm.foundation.model.login.UserProfile;

public interface UserDetailsService {
	
	/**
	 * Retrieves user details from User Data Service using the OAuth token.
	 * @param token
	 * @return
	 */
	public User getUserDetailsFromUDS(String token) throws CommonUtilException ;

	/**
	 * Retrieves roles, permissions and user details related to a specific proposal.
	 * @param nsfId
	 * @param propPrepId
	 * @param revId
	 * @return
	 */
	public UserProfile getUserProfile(String nsfId, String propPrepId, String revId) throws CommonUtilException;
	
	/**
	 * Retrieves User from UDS and converts to PSM User model. Returns null if the user is not found on UDS.
	 * @param nsfId
	 * @return
	 * @throws Exception
	 */
	public User getUDSUserByNSFId(String nsfId) throws CommonUtilException;
	
	/**
	 * Retrieves institutions the user has PI role
	 * @param nsfId
	 * @return
	 * @throws CommonUtilException
	 */
	public List<Institution> getInstitutionIdUserIsPI(String nsfId) throws CommonUtilException;
	
	/**
	 * Checks if user has proposal permission code
	 * @param nsfId
	 * @param propPrepId
	 * @param revId
	 * @param permissionCode
	 * @return
	 * @throws CommonUtilException
	 */
	public boolean userHasProposalPermission(String nsfId, String propPrepId, String revId, String permissionCode) throws CommonUtilException;

	/**
	 * Checks if user has role code.
	 * @param nsfId
	 * @param roleCode
	 * @return
	 * @throws CommonUtilException
	 */
	public boolean userHasRole(String nsfId, String roleCode) throws CommonUtilException;
	
	/**
	 * Validates User Token
	 * @param token
	 * @return
	 * @throws AccessDeniedException
	 * @throws CommonUtilException
	 */
	public boolean isValidUserToken(String token) throws CommonUtilException;
	
	/**
     * Gets user's SPO/AOR status
     * @param nsfId
     * @return
     * @throws CommonUtilException
     */
    public boolean userHasSPOAORRole(String nsfId) throws CommonUtilException;

	/**
	 * Retrieves user details from User Data Service using the nsfId.
	 * @param nsfId
	 * @return
	 */
	public User getUserDetailsByNsfId(String nsfId) throws CommonUtilException ;

    
}
