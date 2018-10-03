package gov.nsf.psm.propmgt.admin.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.stereotype.Component;

@Component
public class BuildInfoEndpoint extends AbstractEndpoint<Map<String, String>> {

	@Value("${buildinfo.project-name}")
	private String projectName;

	@Value("${buildinfo.artifact-version}")
	private String artifactVersion;

	@Value("${buildinfo.java-version}")
	private String javaVersion;

	@Value("${buildinfo.build-date}")
	private String buildDate;

	public BuildInfoEndpoint() {
		super("buildInfo", false);
	}

	private static final Logger logger = LoggerFactory.getLogger(BuildInfoEndpoint.class);

	@Override
	public Map<String, String> invoke() {
		Map<String, String> buildInfo = new HashMap<>();
		buildInfo.put("PROJECT_NAME", projectName);
		buildInfo.put("ARTIFACT_VERSION", artifactVersion);
		buildInfo.put("JAVA_VERSION", javaVersion);
		buildInfo.put("BUILD_DATE", buildDate);
		logger.info("BuildInfo: " + buildInfo.toString());
		return buildInfo;
	}
}
