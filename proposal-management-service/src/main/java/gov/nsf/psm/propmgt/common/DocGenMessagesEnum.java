package gov.nsf.psm.propmgt.common;

import gov.nsf.psm.foundation.model.PSMMessage;
import gov.nsf.psm.foundation.model.PSMMessageType;

public enum DocGenMessagesEnum {

    /* ERROR 1-2 */
    DG_E_001("DG-E-001", PSMMessageType.ERROR,"The system has encountered an error. Please try again and if this issue persists, you may contact the Help Desk at 1-800-673-6188 or helpdesk@nsf.gov."),
    DG_E_002("DG-E-002", PSMMessageType.ERROR,"The system has encountered an error and was unable to generate one or more sections in this proposal file. Please try again and if this issue persists, you may contact the Help Desk at 1-800-673-6188 or helpdesk@nsf.gov.");

    
    private String id;
    private PSMMessageType msgType;
    private String msgText;

    DocGenMessagesEnum(String id, PSMMessageType msgType, String msgText) {
        this.id = id;
        this.msgType = msgType;
        this.msgText = msgText;
    }

    public PSMMessage getMessage() {
        PSMMessage message = new PSMMessage();
        message.setId(this.id);
        message.setType(this.msgType);
        message.setDescription(this.msgText);
        return message;
    }
    
}
