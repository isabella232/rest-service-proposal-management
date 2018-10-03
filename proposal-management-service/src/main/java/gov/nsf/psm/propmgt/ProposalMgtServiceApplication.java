package gov.nsf.psm.propmgt;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class ProposalMgtServiceApplication extends SpringBootServletInitializer {

    private static final Logger PSM_LOGGER = LoggerFactory.getLogger(ProposalMgtServiceApplication.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ProposalMgtServiceApplication.class);
    }

    public static void main(String[] args) {
        PSM_LOGGER.debug("ProposalMgtServiceApplication.main");
        setEmbeddedContainerEnvironmentProperties();
        SpringApplication.run(ProposalMgtServiceApplication.class, args);
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        PSM_LOGGER.debug("ProposalMgtServiceApplication.onStartup");
        setExternalContainerEnvironmentProperties();
        super.onStartup(servletContext);
    }

    private static void setEmbeddedContainerEnvironmentProperties() {
        PSM_LOGGER.debug("ProposalMgtServiceApplication.setEmbeddedContainerEnvironment");
        setEnvironmentProperties();
        System.setProperty("server.context-path", "/proposalprepSvc");
    }

    private static void setExternalContainerEnvironmentProperties() {
        PSM_LOGGER.debug("ProposalMgtServiceApplication.setExternalContainterEnvironmentProperties");
        setEnvironmentProperties();
    }

    private static void setEnvironmentProperties() {
        PSM_LOGGER.debug("ProposalMgtServiceApplication.setExternalContainterEnvironmentProperties");
        System.setProperty("spring.config.name", "proposalprepSvc");
    }
    
}