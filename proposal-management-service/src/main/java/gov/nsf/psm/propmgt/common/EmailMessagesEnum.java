package gov.nsf.psm.propmgt.common;

import gov.nsf.psm.foundation.model.PSMRole;

public enum EmailMessagesEnum {
   
    EMAIL_MSG_ROLE(new String[] {PSMRole.ROLE_PI, PSMRole.ROLE_CO_PI, PSMRole.ROLE_OAU});
    
    private String[] codes;

    EmailMessagesEnum(String[] codes) {
        this.codes = codes;
    }
    
    public String[] getCodes() {
        return this.codes;
    }
    
}
