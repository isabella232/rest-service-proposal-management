package gov.nsf.psm.propmgt.service;

import java.util.List;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import gov.mynsf.common.email.model.SendMetaData;
import gov.nsf.common.model.BaseError;
import gov.nsf.common.model.BaseResponseWrapper;
import gov.nsf.emailservice.api.model.EmailInfo;
import gov.nsf.emailservice.api.model.Letter;
import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.EmailMessage;
import gov.nsf.psm.foundation.model.EmailMessageRequest;
import gov.nsf.psm.foundation.model.EmailMessageType;
import gov.nsf.psm.foundation.model.PSMMessage;
import gov.nsf.psm.foundation.model.PSMMessageType;
import gov.nsf.psm.foundation.model.PSMRole;
import gov.nsf.psm.foundation.model.Personnel;
import gov.nsf.psm.foundation.model.lookup.SeniorPersonRoleTypeLookUp;
import gov.nsf.psm.foundation.model.proposal.ProposalPackage;
import gov.nsf.psm.propmgt.common.Constants;
import gov.nsf.psm.propmgt.common.EmailConstants;
import gov.nsf.psm.propmgt.common.EmailMessagesEnum;
import gov.nsf.psm.propmgt.common.PropMgtMessagesEnum;
import gov.nsf.psm.propmgt.utility.EmailUtils;
import gov.nsf.psm.proposaldata.ProposalDataServiceClient;
import gov.nsf.service.model.UdsGetUserDataResponse;
import gov.nsf.service.model.UdsNsfIdUserData;

/**
 * Service wraps all email notifications for PSM.
 * 
 * @author jharwood
 *
 */
@Component
public class EmailServiceImpl implements EmailService {
    
	private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);
	
	private static final String PSM_PROP_ADD_PERS_TEMPLATE = "email/PSM_PropAddPersTemplate.vm";
	private static final String PSM_PROP_REM_PERS_TEMPLATE = "email/PSM_PropRemPersTemplate.vm";
	private static final String PSM_PROP_ORIG_SUBMIT_TEMPLATE = "email/PSM_PropORIGSubmitTemplate.vm";
	private static final String PSM_PROP_PFU_AA_SUBMIT_TEMPLATE = "email/PSM_PropPFUAASubmitTemplate.vm";
	private static final String PSM_PROP_PFU_MA_SUBMIT_TEMPLATE = "email/PSM_PropPFUMASubmitTemplate.vm";
	private static final String PSM_PROP_PFU_MA_UPDATE_TEMPLATE = "email/PSM_PropPFUMAUpdateTemplate.vm";
	private static final String PSM_PROP_BR_SUBMIT_TEMPLATE = "email/PSM_PropBRSubmitTemplate.vm";
	
    @Autowired
    CachedDataService cachedDataService;
	
	@Autowired
	private ExternalServices extSvc;
	
	@Autowired
    private VelocityEngine velocityEngine;
	
	@Autowired
    ProposalDataServiceClient proposalDataServiceClient;
	
	@Value("#{environment['spring.profiles.active']}")
    private String serverCtx;
	
	@Value("${propmgt.email.recipients.list.default}")
	private String defaultRecipients;
	
	@Value("${propmgt.email.recipients.list.prod-test}")
    private String prodTestEmailRecipients;
	
	@Value("${propmgt.email.recipients.list.bcc}")
    private String bccRecipients;
	
	@Value("${propmgt.email.recipients.list.cc.manual}")
    private String ccManualRecipients;
	
	@Value("${propmgt.email.debug}")
    private Boolean isDebugMode;
	
    @Value("${propmgt.email.enable}")
    private Boolean isEnabled;
	
    @Override
	public List<PSMMessage> sendEmailMessages(EmailMessageRequest eMsgRequest) throws CommonUtilException {
	    
	    try {
	        if(isEnabled != null) {
	            if(isEnabled) {
	                LOGGER.debug("Mail service is enabled.");
	            } else {
	                LOGGER.debug("Mail service is not enabled.");
	                List<PSMMessage> msgs = new ArrayList<>();
	                msgs.add(PropMgtMessagesEnum.PM_E_012.getMessage());
	                return new ArrayList<PSMMessage>();
	            }
	        } else {
	            LOGGER.debug("Mail service is not enabled because 'propmgt.email.enable' parameter does not exist.");
                List<PSMMessage> msgs = new ArrayList<>();
                msgs.add(PropMgtMessagesEnum.PM_E_012.getMessage());
                return new ArrayList<PSMMessage>();
	        }
	        
	        if(serverCtx != null) {
	            LOGGER.debug("Current environment: " + serverCtx.toUpperCase());
	        } else {
	            LOGGER.debug("Current environment could not be found");
	        }
    	    
	        EmailMessage eMsg = new EmailMessage();
	        eMsg.setPropPrepId(eMsgRequest.getPropPrepId());
	        eMsg.setPropPrepRevnId(eMsgRequest.getPropPrepRevnId());
    	    eMsg.setFromAddress(EmailConstants.EMAIL_DEFAULT_SENDER_ADDR);
    	    eMsg.setPersonnelList(eMsgRequest.getPersonnelList());
    	    eMsg.setDebugMode(isDebugMode);
    	    eMsg.setEmailMessageType(eMsgRequest.getEmailMessageType());
    	    
    	    if(!StringUtils.isEmpty(bccRecipients)) {
    	        eMsg.setBccAddresses(Arrays.asList(bccRecipients.split("\\s*;\\s*")));
    	    }
    	    
    	    List<PSMMessage> msgs = new ArrayList<>();
    	    
    	    ProposalPackage pkg = proposalDataServiceClient.getProposalPrep(eMsg.getPropPrepId(), eMsg.getPropPrepRevnId());
    	    eMsg.setDueDate(pkg.getDeadline().getDeadlineDate());
    	    
    	    String emailType = eMsgRequest.getEmailMessageType().getEmailMessageType();
    	    
            if(isDebugMode) {
                if(!EmailUtils.isProdEnv(serverCtx)) {
                    if(!StringUtils.isEmpty(defaultRecipients)) {
                        List<String> recipients = Arrays.asList(defaultRecipients.split("\\s*;\\s*"));
                        eMsg.setToAddresses(recipients);
                        if(emailType.equalsIgnoreCase(EmailMessageType.EMAIL_MSG_TYPE_UPDATE_PFU_MA)) {
                            if(!StringUtils.isEmpty(eMsgRequest.getPoName()) 
                                && !StringUtils.isEmpty(eMsgRequest.getPoEmailAddress())) {
                                     List<String> debugRecipients = new ArrayList<>();
                                     debugRecipients.add(eMsgRequest.getPoEmailAddress() + " (PO=" + eMsgRequest.getPoName() + ")");
                                     eMsg.setDebugRecipients(debugRecipients);
                            } else {
                                LOGGER.debug("PO Name and email address are unavailable");
                                List<String> debugRecipients = new ArrayList<>();
                                List<String> defaultRecipients = Arrays.asList(ccManualRecipients.split("\\s*;\\s*"));
                                if(!defaultRecipients.isEmpty()) {
                                    debugRecipients.add(defaultRecipients.get(0) + " (PSM Operations)");
                                } else {
                                    debugRecipients.add("Unavailable");
                                }
                                eMsg.setDebugRecipients(debugRecipients);
                            }
                        } else {
                            eMsg.setDebugRecipients(getEmailRecipientInfoForRevision(eMsg.getPropPrepId(), eMsg.getPropPrepRevnId(), EmailMessagesEnum.EMAIL_MSG_ROLE.getCodes(), pkg.getPersonnel(), true));
                        }
                    } else {
                        LOGGER.debug("Default recipients have not been set for the following environment: " + serverCtx.toUpperCase());
                    }
                } else {
                    if(!StringUtils.isEmpty(prodTestEmailRecipients)) {
                        List<String> recipients = Arrays.asList(prodTestEmailRecipients.split("\\s*;\\s*"));
                        eMsg.setToAddresses(recipients);
                        LOGGER.debug("The following recipients are being used in a production email test: " + prodTestEmailRecipients);
                    } else {
                        LOGGER.debug("Please specify the recipients to be used in a production email test");
                    }
                }
            } else if(EmailUtils.isProdEnv(serverCtx)) {
                if(!emailType.equalsIgnoreCase(EmailMessageType.EMAIL_MSG_TYPE_UPDATE_PFU_MA)) {
                    eMsg.setToAddresses(getEmailRecipientInfoForRevision(eMsg.getPropPrepId(), eMsg.getPropPrepRevnId(), EmailMessagesEnum.EMAIL_MSG_ROLE.getCodes(), pkg.getPersonnel(), false));
                } else {
                    if(!StringUtils.isEmpty(eMsgRequest.getPoEmailAddress())) {
                          List<String> toAddresses = new ArrayList<>();
                          toAddresses.add(eMsgRequest.getPoEmailAddress());
                          eMsg.setToAddresses(toAddresses);
                    } else {
                        List<String> recipients = Arrays.asList(ccManualRecipients.split("\\s*;\\s*"));
                        eMsg.setToAddresses(recipients);
                    }
                }
            }
            
            eMsg.setEnv(serverCtx);
            eMsg.setPropNsfId(pkg.getNsfPropId());
            eMsg.setProposalTitle(pkg.getProposalTitle());
    	    
            switch(emailType) {
                case EmailMessageType.EMAIL_MSG_TYPE_ROLE_ADD:
                    eMsg.setTemplateName(PSM_PROP_ADD_PERS_TEMPLATE);
                    msgs = formatEmailMessageRole(eMsg);
                    break;
                case EmailMessageType.EMAIL_MSG_TYPE_ROLE_REM:
                    eMsg.setTemplateName(PSM_PROP_REM_PERS_TEMPLATE);
                    msgs = formatEmailMessageRole(eMsg);
                    break;
                case EmailMessageType.EMAIL_MSG_TYPE_SUBMIT_ORIG:
                    eMsg.setTemplateName(PSM_PROP_ORIG_SUBMIT_TEMPLATE);
                    eMsg.setActionDate(pkg.getSubmissionDate());
                    eMsg.setInstitutionName(eMsgRequest.getInstitutionName());
                    eMsg.setSubject("Proposal submitted");
                    msgs = formatEmailMessageSubmit(eMsg);
                    break;
                case EmailMessageType.EMAIL_MSG_TYPE_SUBMIT_PFU_AA:
                    eMsg.setTemplateName(PSM_PROP_PFU_AA_SUBMIT_TEMPLATE);
                    eMsg.setActionDate(pkg.getRevisionStatusDate());
                    eMsg.setInstitutionName(eMsgRequest.getInstitutionName());
                    eMsg.setSubject("Proposal File Update/Budget Revision submitted and accepted");
                    msgs = formatEmailMessageSubmit(eMsg);
                    break;
                case EmailMessageType.EMAIL_MSG_TYPE_SUBMIT_PFU_MA:
                    eMsg.setTemplateName(PSM_PROP_PFU_MA_SUBMIT_TEMPLATE);
                    eMsg.setActionDate(pkg.getRevisionStatusDate());
                    eMsg.setInstitutionName(eMsgRequest.getInstitutionName());
                    eMsg.setSubject("Proposal File Update/Budget Revision submission needs PO approval");
                    eMsg.setJustificationText(eMsgRequest.getJustificationText());
                    if(!StringUtils.isEmpty(ccManualRecipients)) {
                        eMsg.setCcAddresses(Arrays.asList(ccManualRecipients.split("\\s*;\\s*")));
                    }
                    msgs = formatEmailMessageSubmit(eMsg);
                    break;
                case EmailMessageType.EMAIL_MSG_TYPE_UPDATE_PFU_MA:
                    eMsg.setTemplateName(PSM_PROP_PFU_MA_UPDATE_TEMPLATE);
                    eMsg.setActionDate(pkg.getRevisionStatusDate());
                    eMsg.setInstitutionName(eMsgRequest.getInstitutionName());
                    eMsg.setSubject("Proposal File Update Request for %s/%s has been submitted");
                    eMsg.setJustificationText(eMsgRequest.getJustificationText());
                    eMsg.setAor(eMsgRequest.getAor());
                    eMsg.setPi(eMsgRequest.getPi());
                    if(!StringUtils.isEmpty(ccManualRecipients)) {
                        eMsg.setCcAddresses(Arrays.asList(ccManualRecipients.split("\\s*;\\s*")));
                    }
                    msgs = formatEmailMessageUpdate(eMsg);
                    break;
                case EmailMessageType.EMAIL_MSG_TYPE_SUBMIT_BR:
                    eMsg.setTemplateName(PSM_PROP_BR_SUBMIT_TEMPLATE);
                    eMsg.setActionDate(pkg.getRevisionStatusDate());
                    eMsg.setInstitutionName(eMsgRequest.getInstitutionName());
                    eMsg.setSubject("Budget Revision submitted and accepted");
                    msgs = formatEmailMessageSubmit(eMsg);
                    break;
                default:
                    //
            }
            
            return msgs;
            
	    } catch (Exception e) {
	        throw new CommonUtilException(e);
	    }
	}

	@Override
	public List<PSMMessage> formatEmailMessageRole(EmailMessage eMsg) throws CommonUtilException {

		VelocityContext templateMap = new VelocityContext();
		List<PSMMessage> msgs = new ArrayList<>();

		String propStatus = "in-progress";
		String propIdTitle = "Temporary Proposal ID";
		String propId = eMsg.getPropPrepId();
		if (!StringUtils.isEmpty(eMsg.getPropNsfId())) {
			propId = eMsg.getPropNsfId();
			propStatus = "submitted";
			propIdTitle = "Proposal ID";
		}

		Date deletionDate = new Date(); // Get current date since Sybase does
										// not have a way to query record
										// deletion dates
		SimpleDateFormat dateFormat = new SimpleDateFormat(EmailConstants.STATUS_DATE_FORMAT);

		for (Personnel pers : eMsg.getPersonnelList()) {
			SeniorPersonRoleTypeLookUp psmRole = EmailUtils.getRole(proposalDataServiceClient.getSeniorPersonTypes(),
					pers.getPSMRole().getCode().trim());
			String role = psmRole.getDescription()
					+ (!psmRole.getCode().equals(PSMRole.ROLE_OSP) ? " (" + psmRole.getAbbreviation() + ")" : "");

			switch (eMsg.getEmailMessageType().getEmailMessageType()) {
			case EmailMessageType.EMAIL_MSG_TYPE_ROLE_ADD:
				eMsg.setSubject(EmailUtils.getEmailSubjectRole(role, "added to Proposal", propId));
				templateMap.put(EmailConstants.EMAIL_BODY_NAME, EmailUtils.formatName(pers));
				if (psmRole.getCode().equals(PSMRole.ROLE_OSP)) {
					templateMap.put(EmailConstants.EMAIL_BODY_FIRST_LINE_INSERT, "has been added as " + role);
				} else if (psmRole.getCode().equals(PSMRole.ROLE_OAU)) {
					templateMap.put(EmailConstants.EMAIL_BODY_FIRST_LINE_INSERT, "has been added as an " + role);
				} else {
					templateMap.put(EmailConstants.EMAIL_BODY_FIRST_LINE_INSERT, "has been added as a " + role);
				}
				templateMap.put(EmailConstants.EMAIL_BODY_DATE_TIME, dateFormat.format(pers.getLastUpdatedTmsp()));
				break;
			case EmailMessageType.EMAIL_MSG_TYPE_ROLE_REM:
				eMsg.setSubject(EmailUtils.getEmailSubjectRole(role, "removed from Proposal", propId));
				templateMap.put(EmailConstants.EMAIL_BODY_NAME, EmailUtils.formatName(pers));
				if (psmRole.getCode().equals(PSMRole.ROLE_OSP)) {
					templateMap.put(EmailConstants.EMAIL_BODY_FIRST_LINE_INSERT, role);
				} else {
					templateMap.put(EmailConstants.EMAIL_BODY_FIRST_LINE_INSERT, role);
				}
				templateMap.put(EmailConstants.EMAIL_BODY_DATE_TIME, dateFormat.format(deletionDate));
				break;
			default:
				//
			}

			templateMap.put(EmailConstants.EMAIL_BODY_PROP_ID_TITLE, propIdTitle);
			templateMap.put(EmailConstants.EMAIL_BODY_PROP_ID, propId);
			templateMap.put(EmailConstants.EMAIL_BODY_PROP_TITLE, eMsg.getProposalTitle());
			templateMap.put(EmailConstants.EMAIL_BODY_PROP_STATUS, propStatus);
			templateMap.put(EmailConstants.EMAIL_DISCLAIMER_INSERT, EmailConstants.EMAIL_DISCLAIMER_TXT);

			StringWriter stringWriter = new StringWriter();
			velocityEngine.mergeTemplate(eMsg.getTemplateName(), EmailConstants.EMAIL_DEFAULT_ENCODING, templateMap,
					stringWriter);
			eMsg.setMsgText(stringWriter.toString());
			msgs.addAll(processEmailMessage(eMsg));
		}

		return msgs;
	}
	 
    @Override
	public List<PSMMessage> formatEmailMessageSubmit(EmailMessage eMsg) throws CommonUtilException {
        
        try {
            
            VelocityContext templateMap = new VelocityContext();
            List<PSMMessage> msgs = new ArrayList<>();
           
            SimpleDateFormat dueDateFormat = new SimpleDateFormat(EmailConstants.SIMPLE_DATE_FORMAT);
            SimpleDateFormat actionDateFormat = new SimpleDateFormat(EmailConstants.STATUS_DATE_FORMAT);
            
            for(Personnel pers : eMsg.getPersonnelList()) {
                
                templateMap.put(EmailConstants.EMAIL_BODY_NAME, EmailUtils.formatName(pers));
                templateMap.put(EmailConstants.EMAIL_BODY_INST_NAME, eMsg.getInstitutionName());
                if(eMsg.getTemplateName().equalsIgnoreCase(PSM_PROP_ORIG_SUBMIT_TEMPLATE)) {
                    templateMap.put(EmailConstants.EMAIL_BODY_PROP_ID_TEMP, eMsg.getPropPrepId());
                }
                if(!StringUtils.isEmpty(eMsg.getPropNsfId())) {
                    templateMap.put(EmailConstants.EMAIL_BODY_PROP_ID, eMsg.getPropNsfId());
                } else {
                    templateMap.put(EmailConstants.EMAIL_BODY_PROP_ID, "Not yet available");
                }
                templateMap.put(EmailConstants.EMAIL_BODY_PROP_TITLE, eMsg.getProposalTitle());
                templateMap.put(EmailConstants.EMAIL_BODY_DATE_TIME, actionDateFormat.format(eMsg.getActionDate()));
                templateMap.put(EmailConstants.EMAIL_DISCLAIMER_INSERT, EmailConstants.EMAIL_DISCLAIMER_TXT);
                if(eMsg.getTemplateName().equalsIgnoreCase(PSM_PROP_PFU_MA_SUBMIT_TEMPLATE)) {
                    templateMap.put(EmailConstants.EMAIL_BODY_JUSTIFICATION_TEXT, eMsg.getJustificationText());
                    templateMap.put(EmailConstants.EMAIL_BODY_DUE_DATE, dueDateFormat.format(eMsg.getDueDate()));
                }
                StringWriter stringWriter = new StringWriter();
                velocityEngine.mergeTemplate(eMsg.getTemplateName(), EmailConstants.EMAIL_DEFAULT_ENCODING, templateMap, stringWriter);
                eMsg.setMsgText(stringWriter.toString());
                msgs.addAll(processEmailMessage(eMsg));
            }
            
            return msgs;
            
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }
    
    public List<PSMMessage> formatEmailMessageUpdate(EmailMessage eMsg) throws CommonUtilException {
        
        try {
            
            VelocityContext templateMap = new VelocityContext();
            List<PSMMessage> msgs = new ArrayList<>();
           
            SimpleDateFormat actionDateFormat = new SimpleDateFormat(EmailConstants.STATUS_DATE_FORMAT);
                
            String piName = EmailUtils.formatName(eMsg.getPi());
            templateMap.put(EmailConstants.EMAIL_BODY_PI_NAME, piName);
            templateMap.put(EmailConstants.EMAIL_BODY_PI_EMAIL, eMsg.getPi().getEmail());
            String aorName = EmailUtils.formatName(eMsg.getAor());
            templateMap.put(EmailConstants.EMAIL_BODY_AOR_NAME, aorName);
            templateMap.put(EmailConstants.EMAIL_BODY_AOR_EMAIL, eMsg.getAor().getEmail());
            templateMap.put(EmailConstants.EMAIL_BODY_INST_NAME, eMsg.getInstitutionName());
            templateMap.put(EmailConstants.EMAIL_BODY_PROP_ID, eMsg.getPropNsfId());
            templateMap.put(EmailConstants.EMAIL_BODY_PROP_TITLE, eMsg.getProposalTitle());
            templateMap.put(EmailConstants.EMAIL_BODY_DATE_TIME, actionDateFormat.format(eMsg.getActionDate()));
            templateMap.put(EmailConstants.EMAIL_DISCLAIMER_INSERT, EmailConstants.EMAIL_DISCLAIMER_TXT);
            templateMap.put(EmailConstants.EMAIL_BODY_JUSTIFICATION_TEXT, eMsg.getJustificationText());
            
            StringWriter stringWriter = new StringWriter();
            velocityEngine.mergeTemplate(eMsg.getTemplateName(), EmailConstants.EMAIL_DEFAULT_ENCODING, templateMap, stringWriter);
            eMsg.setMsgText(stringWriter.toString());
            eMsg.setSubject(String.format(eMsg.getSubject(), eMsg.getPropNsfId(), eMsg.getPi().getLastName()));
            msgs.addAll(processEmailMessage(eMsg));
            
            return msgs;
            
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }
	
	private List<PSMMessage> processEmailMessage(EmailMessage eMsg) throws CommonUtilException {       
	    
	    try {
	        
    	    List<PSMMessage> msgs = new ArrayList<>();
    	    Letter ltr = new Letter();
            ltr.setPlainText(false);
            
            if(!EmailUtils.isProdEnv(serverCtx) && isDebugMode) {
                StringBuilder sb = new StringBuilder(eMsg.getMsgText());
                sb.append("<br><br>[TESTING - " + eMsg.getEnv().toUpperCase() + "]<br>");
                sb.append("[From: " + eMsg.getFromAddress() + "]<br>");
                sb.append("[To: " + (eMsg.getDebugRecipients().isEmpty()?"N/A":Joiner.on("; ").join(eMsg.getDebugRecipients())) + "]<br>");
                eMsg.setMsgText(sb.toString());
            }
            
            ltr.setEltrContent(eMsg.getMsgText());
            EmailInfo emailInfo = new EmailInfo();
            LOGGER.debug("Email from address: " + eMsg.getFromAddress());
            LOGGER.debug("Email to addresses: " + (eMsg.getToAddresses() != null?Joiner.on("; ").join(eMsg.getToAddresses()):""));
            if(eMsg.getBccAddresses() != null && !eMsg.getBccAddresses().isEmpty()) {
                LOGGER.debug("Email bcc addresses: " + (eMsg.getBccAddresses() != null?Joiner.on("; ").join(eMsg.getBccAddresses()):""));
                emailInfo.setBccAddresses(eMsg.getBccAddresses());
            }
            emailInfo.setFromAddress(eMsg.getFromAddress());
            if(isDebugMode && !EmailUtils.isProdEnv(serverCtx) && !eMsg.getDebugRecipients().isEmpty()) {
                emailInfo.setToAddresses(eMsg.getDebugRecipients());
	        } else {
	            emailInfo.setToAddresses(eMsg.getToAddresses());
	        }
            emailInfo.setMailSubject(eMsg.getSubject());
            if(eMsg.getCcAddresses() != null && !eMsg.getCcAddresses().isEmpty()) {
                LOGGER.debug("Email cc addresses: " + (eMsg.getCcAddresses() != null?Joiner.on("; ").join(eMsg.getCcAddresses()):""));
                emailInfo.setCcAddresses(eMsg.getCcAddresses());
            }
            ltr.setEmailInfo(emailInfo);
            SendMetaData smData = new SendMetaData();
            if(eMsg.isDebugMode()) {
                smData.setDebugRecipients(eMsg.getToAddresses());
            }
            if(eMsg.getBccAddresses() != null && !eMsg.getBccAddresses().isEmpty()) {
                smData.setDefaultBccRecipients(eMsg.getBccAddresses());
            }
            
    	    BaseResponseWrapper wrapper = extSvc.sendEmailMessage(ltr, smData);
    	    
    	    if(wrapper.getErrors() != null) {
        	    for(BaseError err : wrapper.getErrors()) {
                    PSMMessage msg = new PSMMessage();
                    msg.setId(err.getFieldId());
                    int idx1 = err.getDetail().indexOf(": ");
                    int idx2 = err.getDetail().indexOf("at gov");
                    if(idx1 > -1 && idx2 > -1) {  
                        msg.setDescription(err.getDetail().substring(idx1+2, idx2).trim());
                    } else {
                        msg.setDescription(err.getDetail());
                    }
                    LOGGER.debug("Email service generated the following error: " + msg.getDescription());
                    msg.setType(PSMMessageType.ERROR);
                    msgs.add(msg);
                }
    	    }
    	    
    	    if(msgs.isEmpty()) {
    	        LOGGER.debug("Email message sent successfully for " + emailInfo.getFromAddress());
    	    }
    	    
            for(BaseError err : wrapper.getWarnings()) {
                PSMMessage msg = new PSMMessage();
                msg.setId(err.getFieldId());
                msg.setDescription(err.getDetail());
                LOGGER.debug("Email service generated the following warning: " + msg.getDescription());
                msg.setType(PSMMessageType.WARNING);
                msgs.add(msg);
            }
            
            return msgs;
            
	    } catch (Exception e) {
	        throw new CommonUtilException(e);
	    }
	}
	
	private List<String> getEmailRecipientInfoForRevision(String propPrepId, String propRevId, String[] roles, List<Personnel> personnel, boolean showInfo) throws CommonUtilException {
	    try {
    	    List<String> recipients = new ArrayList<>();
    	    List<Personnel> persons = null;
    	    if(personnel == null || personnel.isEmpty()) {
    	        persons = proposalDataServiceClient.getPersonnels(propPrepId, propRevId);
    	    } else {
    	        persons = personnel;
    	    }
    	    persons = EmailUtils.sortPersonnelListByRoleCode(persons);
    	    for(Personnel person : persons) { // Get all personnel who should receive emails
	            if(person.getPSMRole() != null && person.getPSMRole().getCode() != null
	                && Arrays.asList(roles).contains(person.getPSMRole().getCode().trim())) {
	                    String emailAddress = getEmailAddressInfo(person.getNsfId()).trim();
    	                if(!StringUtils.isEmpty(emailAddress)) {
    	                    String emailAddressUDS = getEmailAddressInfo(person.getNsfId().trim());
    	                    if(!showInfo) {
    	                        recipients.add(emailAddressUDS);
    	                    } else {
    	                        recipients.add(emailAddressUDS + " (" + EmailUtils.getRole(cachedDataService.getSeniorPersonRoleTypeLookup(), person.getPSMRole().getCode().trim()).getAbbreviation() + "=" + EmailUtils.formatName(person) + ")");
    	                    } 
    	                    LOGGER.debug("An email address has been added for " + EmailUtils.formatName(person) + " (" + EmailUtils.getRole(cachedDataService.getSeniorPersonRoleTypeLookup(), person.getPSMRole().getCode().trim()).getAbbreviation() + ")");
    	                } else {
    	                    LOGGER.debug("An email address does not exist for " + EmailUtils.formatName(person) + " (" + EmailUtils.getRole(cachedDataService.getSeniorPersonRoleTypeLookup(), person.getPSMRole().getCode().trim()).getAbbreviation() + ")");
    	                }
	            } else {
	                LOGGER.debug("A proposal role is not assigned to " + EmailUtils.formatName(person) + " (" + EmailUtils.getRole(cachedDataService.getSeniorPersonRoleTypeLookup(), person.getPSMRole().getCode().trim()).getAbbreviation() + ")");
	            }
    	    }
    	    return recipients;
	    } catch (Exception e) {
            throw new CommonUtilException(e);
        } 
	}
	
	private String getEmailAddressInfo(String nsfId) throws CommonUtilException {
	    
	    UdsNsfIdUserData userData = null;
        UdsGetUserDataResponse userResponse = null;
        
	    try {
            userResponse = cachedDataService.getUserData(nsfId);
        } catch (Exception e) {
            throw new CommonUtilException(String.format(Constants.GET_SERVICE_IS_DOWN_ERROR, "UDS"), e);
        }

        if (userResponse != null) {
            userData = userResponse.getUserData();
        }
        
        if(userData != null) {
            return userData.getEmail();
        } else {
            return "";
        }
	}
	
}
