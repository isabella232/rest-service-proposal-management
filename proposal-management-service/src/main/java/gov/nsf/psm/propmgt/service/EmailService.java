package gov.nsf.psm.propmgt.service;

import java.util.List;

import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.EmailMessage;
import gov.nsf.psm.foundation.model.EmailMessageRequest;
import gov.nsf.psm.foundation.model.PSMMessage;

public interface EmailService {
    
    public List<PSMMessage> formatEmailMessageRole(EmailMessage eMsg) throws CommonUtilException;
    
    public List<PSMMessage> formatEmailMessageSubmit(EmailMessage eMsg) throws CommonUtilException;
    
    public List<PSMMessage> sendEmailMessages(EmailMessageRequest eMsgRequest) throws CommonUtilException;

}
