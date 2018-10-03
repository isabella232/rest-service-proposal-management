package gov.nsf.psm.propmgt.utility;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.mynsf.common.email.model.SendMetaData;
import gov.nsf.emailservice.api.model.EmailInfo;
import gov.nsf.emailservice.api.model.Letter;
import gov.nsf.emailservice.api.service.EmailService;
import gov.nsf.psm.proposaltransfer.api.model.ProposalTransferRequest;

/**
 * ProposalTransferEmailUtility
 */
@Component
public class ProposalTransferEmailUtility {

    @Autowired
    private EmailService emailService;

    @Autowired
    private VelocityEngine velocityEngine;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProposalTransferEmailUtility.class);

    @Value("${propmgt.email.support.data-transfer.sender-email}")
    private String SENDER_EMAIL;
    
    @Value("${propmgt.email.support.data-transfer.to-recipients}")
    private String TO_RECIPIENTS;
    
    @Value("${propmgt.email.support.data-transfer.cc-recipients}")
    private String CC_RECIPIENTS;
    
	@Value("${propmgt.email.support.data-transfer.debug-recipients}")
    private String DEBUG_RECIPIENTS;

    private static final String COMMA_SEPARATOR = "\\s*;\\s*";
	
    private static final String FAILURE_EMAIL_TEMPLATE = "email/ProposalTransfer_FailureTemplate.vm";
    private static final String FAILURE_EMAIL_SUBJECT = "Attention: Failure in Processing Proposal Data Transfer Request on Submit for PSM Proposal %s / %s";

    private static final String SUCCESS_EMAIL_TEMPLATE = "email/ProposalTransfer_SuccessTemplate.vm";
    private static final String SUCCESS_EMAIL_SUBJECT = "PSM Proposal Transfer to FastLane Successful";

    public void sendSuccessEmail(ProposalTransferRequest proposalTransferRequest) {

        EmailInfo emailInfo = new EmailInfo();
        emailInfo.setFromAddress(SENDER_EMAIL);
        emailInfo.setToAddresses( Arrays.asList(TO_RECIPIENTS.split(COMMA_SEPARATOR)) );
        emailInfo.setCcAddresses( Arrays.asList(CC_RECIPIENTS.split(COMMA_SEPARATOR)) );
        emailInfo.setMailSubject(SUCCESS_EMAIL_SUBJECT);

        VelocityContext templateMap = new VelocityContext();
        templateMap.put("dateTool", new DateTool());
        templateMap.put("psmPropPrepId", proposalTransferRequest.getProposalPreparationId());
        templateMap.put("psmPropRevId", proposalTransferRequest.getProposalPreparationRevisionId());
        templateMap.put("revisionNumber", proposalTransferRequest.getRevisionNumber());
        templateMap.put("requestAction", proposalTransferRequest.getRequestAction().name() + " (" + proposalTransferRequest.getRequestAction().getTpiCode() + ")");
        templateMap.put("transferRequestId", proposalTransferRequest.getId());
        templateMap.put("flPropId", proposalTransferRequest.getFastLaneProposalId());
        templateMap.put("flTempPropId", proposalTransferRequest.getFastLaneProposalTempId());
        templateMap.put("user", proposalTransferRequest.getCreationUser());
        templateMap.put("timestamp", new Date());

        StringWriter emailBody = new StringWriter();
        try {
            velocityEngine.mergeTemplate(SUCCESS_EMAIL_TEMPLATE, "UTF-8", templateMap, emailBody);
            LOGGER.debug(emailBody.toString());
            Letter letter = new Letter();
            letter.setEmailInfo(emailInfo);
            letter.setEltrContent(emailBody.toString());

            SendMetaData sendMetaData = new SendMetaData();
            sendMetaData.setDebugRecipients( Arrays.asList(DEBUG_RECIPIENTS.split(COMMA_SEPARATOR)) );

            emailService.sendLetter(letter, sendMetaData);
        } catch (Exception ex) {
            LOGGER.error("Failed to send proposal transfer success email: ", ex);
        }

    }

    public void sendFailureEmail(ProposalTransferRequest proposalTransferRequest, Object failureReason) {

        EmailInfo emailInfo = new EmailInfo();
        emailInfo.setFromAddress(SENDER_EMAIL);
        emailInfo.setToAddresses( Arrays.asList(TO_RECIPIENTS.split(COMMA_SEPARATOR)) );
        emailInfo.setCcAddresses( Arrays.asList(CC_RECIPIENTS.split(COMMA_SEPARATOR)) );
        emailInfo.setMailSubject(String.format(FAILURE_EMAIL_SUBJECT, proposalTransferRequest.getProposalPreparationId(), proposalTransferRequest.getProposalPreparationRevisionId()));

        VelocityContext templateMap = new VelocityContext();
        templateMap.put("dateTool", new DateTool());
        templateMap.put("psmPropPrepId", proposalTransferRequest.getProposalPreparationId());
        templateMap.put("psmPropRevId", proposalTransferRequest.getProposalPreparationRevisionId());
        templateMap.put("transferRequestId", proposalTransferRequest.getId() != null ? proposalTransferRequest.getId() : "Not Applicable");
        templateMap.put("failureMessage", getFailureEmailBody(failureReason));
        templateMap.put("timestamp", proposalTransferRequest.getRequestStatusDate() != null ? proposalTransferRequest.getRequestStatusDate().getTime() : System.currentTimeMillis());
        templateMap.put("user", proposalTransferRequest.getCreationUser());
        StringWriter emailBody = new StringWriter();

        try {
            velocityEngine.mergeTemplate(FAILURE_EMAIL_TEMPLATE, "UTF-8", templateMap, emailBody);

            Letter letter = new Letter();
            letter.setEmailInfo(emailInfo);
            letter.setEltrContent(emailBody.toString());

            SendMetaData sendMetaData = new SendMetaData();
            sendMetaData.setDebugRecipients( Arrays.asList(DEBUG_RECIPIENTS.split(COMMA_SEPARATOR)) );

            emailService.sendLetter(letter, sendMetaData);
        } catch (Exception ex) {
            LOGGER.error("Failed to send proposal transfer success email: ", ex);
        }

    }

    public void sendFailureEmail(String psmPropId, String psmRevId, String userNsfId, Object failureReason) {

        EmailInfo emailInfo = new EmailInfo();
        emailInfo.setFromAddress(SENDER_EMAIL);
        emailInfo.setToAddresses( Arrays.asList(TO_RECIPIENTS.split(COMMA_SEPARATOR)) );
        emailInfo.setCcAddresses( Arrays.asList(CC_RECIPIENTS.split(COMMA_SEPARATOR)) );
        emailInfo.setMailSubject(String.format(FAILURE_EMAIL_SUBJECT, psmPropId, psmRevId));

        VelocityContext templateMap = new VelocityContext();
        templateMap.put("dateTool", new DateTool());
        templateMap.put("psmPropPrepId", psmPropId);
        templateMap.put("psmPropRevId", psmRevId);
        templateMap.put("transferRequestId", "Not Applicable");
        templateMap.put("failureMessage", getFailureEmailBody(failureReason));
        templateMap.put("timestamp", System.currentTimeMillis());
        templateMap.put("user", userNsfId);
        StringWriter emailBody = new StringWriter();

        try {
            velocityEngine.mergeTemplate(FAILURE_EMAIL_TEMPLATE, "UTF-8", templateMap, emailBody);

            Letter letter = new Letter();
            letter.setEmailInfo(emailInfo);
            letter.setEltrContent(emailBody.toString());

            SendMetaData sendMetaData = new SendMetaData();
            sendMetaData.setDebugRecipients( Arrays.asList(DEBUG_RECIPIENTS.split(COMMA_SEPARATOR)) );

            emailService.sendLetter(letter, sendMetaData);
        } catch (Exception ex) {
            LOGGER.error("Failed to send proposal transfer success email: ", ex);
        }

    }

    private String getFailureEmailBody(Object failureReason) {

        String failureMessage;

        try {
            failureMessage = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(failureReason);
        } catch (JsonProcessingException ex) {
            LOGGER.error("Failed to serialize failure reason: ", ex);
            failureMessage = "Failed to serialize failure reason: " + ex.getMessage();
        }

        return failureMessage;
    }

}
