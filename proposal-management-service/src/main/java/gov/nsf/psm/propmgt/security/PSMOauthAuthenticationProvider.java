package gov.nsf.psm.propmgt.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/***
 * 
 * @author 
 *
 */
@Component
public class PSMOauthAuthenticationProvider implements AuthenticationProvider {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            PSMPreAuthenticatedAuthenticationToken preAuthenticatedToken  = 
                    new PSMPreAuthenticatedAuthenticationToken("", authentication.getCredentials().toString());
            return preAuthenticatedToken;
        } catch (Exception ex) {
        	LOGGER.error("PSMOauthAuthenticationProvider",ex);
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PSMPreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
