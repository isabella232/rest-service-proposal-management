package gov.nsf.psm.propmgt.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import gov.nsf.psm.propmgt.security.PSMOAuthAuthenticationFilter;
import gov.nsf.psm.propmgt.security.PSMOAuthAuthenticationFilterRegistration;
import gov.nsf.psm.propmgt.security.PSMOauthAuthenticationProvider;

@Configuration
@Order(1)
@EnableWebMvc
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ConditionalOnProperty(value = "propmgt.security.enable-secure-endpoints", havingValue = "true")
public class ProposalMgtSpringSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private AuthenticationManager customAuthenticationManager;

	@Autowired
	private PSMOauthAuthenticationProvider pSMOauthAuthenticationProvider;

	@Value("${propmgt.security.internal-user.username}")
	public String internalUsername;

	@Value("${propmgt.security.internal-user.password}")
	public String internalPassword;

	@Value("${propmgt.security.admin-user.username}")
	public String adminUsername;

	@Value("${propmgt.security.admin-user.password}")
	public String adminPassword;

	private static final String INTERNAL_USER_ROLE = "USER";

	private static final String ADMIN_USER_ROLE = "ACTUATOR";

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		/* oAuth */
		auth.authenticationProvider(this.pSMOauthAuthenticationProvider);
		/* Basic Auth */
		auth.inMemoryAuthentication().withUser(internalUsername).password(internalPassword).roles(INTERNAL_USER_ROLE);
		auth.inMemoryAuthentication().withUser(adminUsername).password(adminPassword).roles(ADMIN_USER_ROLE);
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/api/v1/proposal/document/**")
				// .antMatchers("/api/v1/submitted/**")
				.antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security",
						"/swagger-ui.html", "/webjars/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.addFilter(oauthAuthenticationFilter()).authorizeRequests()
				// .anyRequest().authenticated()
				.antMatchers("/api/**").authenticated() // oAuth

				.and().authorizeRequests().antMatchers("/internal/**").authenticated() // Basic
																						// Auth
				.and().httpBasic()

				.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().csrf()
				.disable();
	}

	@Bean
	PSMOAuthAuthenticationFilter oauthAuthenticationFilter() {
		PSMOAuthAuthenticationFilter oauthAuthenticationFilter = new PSMOAuthAuthenticationFilter();
		oauthAuthenticationFilter.setAuthenticationManager(customAuthenticationManager);
		return oauthAuthenticationFilter;
	}

	@Bean
	PSMOAuthAuthenticationFilterRegistration oauthAuthenticationFilterRegistration() throws Exception {
		PSMOAuthAuthenticationFilterRegistration oauthAuthenticationFilterRegistration = new PSMOAuthAuthenticationFilterRegistration(
				oauthAuthenticationFilter());
		return oauthAuthenticationFilterRegistration;
	}
}
