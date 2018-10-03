package gov.nsf.psm.propmgt.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Joiner;

import gov.nsf.psm.foundation.model.PSMMessage;
import gov.nsf.psm.foundation.model.PSMMessageType;

public enum PropMgtMessagesEnum {

    /* INFO */
    PM_I_001("PM-I-001", PSMMessageType.INFORMATION, "Your file has been uploaded successfully."), 
    PM_I_002("PM-I-002", PSMMessageType.INFORMATION, "Your file has been deleted successfully."),
    PM_I_003("PM-I-003", PSMMessageType.INFORMATION, "A postdoctoral mentoring plan is now required since funds have been indicated in the budget for Postdoctoral Scholars. This section is now available from your required proposal sections."),

    /* WARNING */
    PM_W_200("PM-W-200", PSMMessageType.WARNING, "One or more COA template cells could not be saved. Look for differences between the preview and the COA template data, such as truncated or excluded data. If changes are needed to the COA template, delete the previously uploaded template file, and upload the revised COA template file.", new ArrayList<>()),

    /* ERROR 1-20 */
    PM_E_001("PM-E-001", PSMMessageType.ERROR, "The system has encountered an error. Please try again and if this issue persists, you may contact the Help Desk at 1-800-381-1532 or Rgov@nsf.gov."), 
    PM_E_002("PM-E-002", PSMMessageType.ERROR, "The system has encountered an error and was unable to upload your file. Please try again and if this issue persists, you may contact the Help Desk at 1-800-381-1532 or Rgov@nsf.gov."),
    PM_E_003("PM-E-003", PSMMessageType.ERROR, "Your file is an invalid file type. Only PDF documents are permitted."),
    PM_E_004("PM-E-004", PSMMessageType.ERROR, "Your file is an invalid file type. Only Microsoft Excel documents created with Microsoft Office are permitted."),
    PM_E_005("PM-E-005", PSMMessageType.ERROR, "The requested file could not be found."),
    PM_E_006("PM-E-006", PSMMessageType.ERROR, "No institution roles associated with user profile."),
    PM_E_007("PM-E-007", PSMMessageType.ERROR, "Error encountered while getting user profile."),
    PM_E_008("PM-E-008", PSMMessageType.ERROR, "User has no PI role."),
    PM_E_012("PM-E-012", PSMMessageType.ERROR, "Email service is not enabled."),
    PM_E_091("PM-E-091", PSMMessageType.ERROR, "You cannot submit this proposal file update/budget revision after reviewers have been assigned to the proposal. If only a budget revision is needed, it can still be prepared and submitted by returning to your submitted proposal and initiating a new budget revision."),
    PM_E_092("PM-E-092", PSMMessageType.ERROR, "You cannot submit this proposal file update/budget revision since a decision has been reached on this proposal."),
    PM_E_093("PM-E-093", PSMMessageType.ERROR, "You cannot submit this budget revision after a decision has been made."),
    PM_E_094("PM-E-094", PSMMessageType.ERROR, "You cannot submit this proposal file update/budget revision due to a change in the proposal status. If only a budget revision is needed, it can still be prepared and submitted by returning to your submitted proposal and initiating a new budget revision."),
    PM_E_100("PM-E-100", PSMMessageType.ERROR, "Postal Code entered is not valid within (state %s) selected", new ArrayList<>());
	
    private String id;
    private PSMMessageType msgType;
    private String msgText;
    private List<String> parameters;

    PropMgtMessagesEnum(String id, PSMMessageType msgType, String msgText) {
        this.id = id;
        this.msgType = msgType;
        this.msgText = msgText;
    }

    PropMgtMessagesEnum(String id, PSMMessageType msgType, String msgText, List<String> parameters) {
        this.id = id;
        this.msgType = msgType;
        this.msgText = msgText;
        this.parameters = parameters;
    }

    public PSMMessage getMessage() {
        PSMMessage message = new PSMMessage();
        message.setId(this.id);
        message.setType(this.msgType);
        message.setDescription(this.msgText);
        return message;
    }

    public PSMMessage getMessage(String p1, boolean addParam) {
        PSMMessage message = new PSMMessage();
        message.setId(this.id);
        message.setType(this.msgType);
        if (!addParam && this.parameters.indexOf(p1) > -1) {
            this.parameters.remove(this.parameters.indexOf(p1));
        }
        if (this.parameters.indexOf(p1) < 0)
            this.parameters.add(p1);
        sortMessages();
        message.setDescription(
                String.format(this.msgText, parameters.isEmpty() ? "" : Joiner.on(", ").join(this.parameters)));
        return message;
    }

    public PSMMessage getMessage(List<String> parameters) {
        PSMMessage message = new PSMMessage();
        message.setId(this.id);
        message.setType(this.msgType);
        this.parameters = parameters;
        sortMessages();
        message.setDescription(
                String.format(this.msgText, parameters.isEmpty() ? "" : Joiner.on(", ").join(this.parameters)));
        return message;
    }

    public void resetParameters() {
        this.parameters = new ArrayList<>();
    }
    
    private void sortMessages() {
        Collections.sort(this.parameters, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                String[] as = a.split(",");
                String[] bs = b.split(",");
                int result = Integer.valueOf(as[0]).compareTo(Integer.valueOf(bs[0]));
                if (result == 0)
                    result = Integer.valueOf(as[1]).compareTo(Integer.valueOf(bs[1]));
                return result;
            }
        });
    }
}
