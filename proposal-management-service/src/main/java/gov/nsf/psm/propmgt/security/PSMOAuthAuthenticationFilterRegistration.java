package gov.nsf.psm.propmgt.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;

public class PSMOAuthAuthenticationFilterRegistration extends FilterRegistrationBean {

    public PSMOAuthAuthenticationFilterRegistration(PSMOAuthAuthenticationFilter oauthAuthenticationFilter) {
        super.setFilter(oauthAuthenticationFilter);
        super.setEnabled(false);
    }

}
