package gov.nsf.psm.propmgt.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.client.HttpClientErrorException;

import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.openam.model.OpenAMUserInfo;
import gov.nsf.psm.propmgt.common.Constants;
import gov.nsf.psm.propmgt.service.ExternalServices;

public class PSMOAuthAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {
    
	@Autowired
    ExternalServices externalServices;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
    	logger.debug("OAuthAuthenticationFilter executed");
        HttpServletRequest httpRequest = this.getAsHttpRequest(request);
        String authToken = extractAuthTokenFromRequest(httpRequest);
        
        logger.debug("URL = " + httpRequest.getRequestURL());
        
        if (authToken != null) {
            try {
                OpenAMUserInfo openAMUserInfo = externalServices.getUserInfoForAccessToken(authToken);
                PSMPreAuthenticatedAuthenticationToken authentication = new PSMPreAuthenticatedAuthenticationToken(
                        openAMUserInfo.getSub(), authToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (HttpClientErrorException | AccessDeniedException | CommonUtilException e) {
            	logger.error("PSMOAuthAuthenticationFilter",e);
                PSMPreAuthenticatedAuthenticationToken authentication = new PSMPreAuthenticatedAuthenticationToken(
                        null, null);
                authentication.setAuthenticated(false);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }

    private HttpServletRequest getAsHttpRequest(ServletRequest request) {
        if (!(request instanceof HttpServletRequest)) {
            throw new RuntimeException("Expecting an HTTP request");
        }
        return (HttpServletRequest) request;
    }

    private String extractAuthTokenFromRequest(HttpServletRequest httpRequest) {
        /* Get token from header */
        String authToken = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if(authToken != null && authToken.substring(0, 7).equals(Constants.AUTH_TOKEN_PREFIX)) {
            return authToken.substring(7);
        }
        return authToken;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }
}
