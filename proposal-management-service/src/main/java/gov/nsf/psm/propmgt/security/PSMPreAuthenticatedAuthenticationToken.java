package gov.nsf.psm.propmgt.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class PSMPreAuthenticatedAuthenticationToken extends PreAuthenticatedAuthenticationToken {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PSMPreAuthenticatedAuthenticationToken(Object aPrincipal, Object aCredentials) {
        super(aPrincipal, aCredentials);
    }

    public PSMPreAuthenticatedAuthenticationToken(Object aPrincipal, Object aCredentials,
            Collection<? extends GrantedAuthority> anAuthorities) {
        super(aPrincipal, aCredentials, anAuthorities);
    }

}
