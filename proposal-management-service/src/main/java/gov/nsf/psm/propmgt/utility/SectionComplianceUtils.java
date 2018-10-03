package gov.nsf.psm.propmgt.utility;

import java.util.List;

import gov.nsf.psm.factmodel.PSMMessageType;
import gov.nsf.psm.foundation.model.PSMMessage;

public class SectionComplianceUtils {
    
    private SectionComplianceUtils() {
        // Private constructor
    }
    
    public static long getErrorStatusCount(List<PSMMessage> msgs) {
        return msgs.stream()
                .filter(PSMMessage -> PSMMessageType.ERROR.getCode().equals(PSMMessage.getType().getCode())).count();
    }
    
    public static long getWarningStatusCount(List<PSMMessage> msgs) {
        return msgs.stream()
                .filter(PSMMessage -> PSMMessageType.WARNING.getCode().equals(PSMMessage.getType().getCode())).count();
    }

}
