package gov.nsf.psm.propmgt.utility;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import java.text.ParseException;

import gov.nsf.psm.factmodel.PSMMessageType;
import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.PSMMessage;
import gov.nsf.psm.foundation.model.SectionStatus;
import gov.nsf.psm.foundation.model.proposal.ProposalRevisionType;
import gov.nsf.psm.foundation.model.proposal.ProposalStatus;
import gov.nsf.psm.propmgt.common.Constants;

public class SectionStatusUtils {
    
    private SectionStatusUtils() {
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

    public static boolean populateSectionUpdatedSeniorPersonnel(SectionStatus status) throws CommonUtilException {
        if (status.getLastUpdatedTmsp() != null && status.getStatusDate() != null 
                && !status.getRevisionType().equalsIgnoreCase(ProposalRevisionType.ORIGINAL_PROPOSAL) && !status.getStatusCode().equalsIgnoreCase(ProposalStatus.SUBMITTED_TO_NSF)) {
            SimpleDateFormat format = new SimpleDateFormat(Constants.STATUS_DATE_FORMAT);
            String dateOne = format.format(status.getLastUpdatedTmsp());
            String dateTwo = format.format(status.getStatusDate());
            try {
                return format.parse(dateOne).after(format.parse(dateTwo));
            } catch (ParseException e) {
                throw new CommonUtilException(e);
            }
        }
        return false;
    }
    
    public static boolean populateSectionUpdated(SectionStatus status) throws CommonUtilException {
        if (status.getLastUpdatedTmsp() != null && status.getStatusDate() != null 
                && !status.getRevisionType().equalsIgnoreCase(ProposalRevisionType.ORIGINAL_PROPOSAL)) {
            SimpleDateFormat format = new SimpleDateFormat(Constants.STATUS_DATE_FORMAT);
            String dateOne = format.format(status.getLastUpdatedTmsp());
            String dateTwo = format.format(status.getStatusDate());
            try {
                return format.parse(dateOne).after(format.parse(dateTwo));
            } catch (ParseException e) {
                throw new CommonUtilException(e);
            }
        }
        return false;
    }
    
    public static boolean populateSectionUpdatedOrigRevCreatedDate(SectionStatus status) throws CommonUtilException {
        if (status.getLastUpdatedTmsp() != null 
            && status.getOrigRevCreatedDate() != null
                && !status.getRevisionType().equalsIgnoreCase(ProposalRevisionType.ORIGINAL_PROPOSAL)) {
                    SimpleDateFormat format = new SimpleDateFormat(Constants.STATUS_DATE_FORMAT);
                    String dateOne = format.format(status.getLastUpdatedTmsp());
                    String dateTwo = format.format(status.getOrigRevCreatedDate());
                    try {
                        return format.parse(dateOne).after(format.parse(dateTwo));
                    } catch (ParseException e) {
                        throw new CommonUtilException(e);
                    }
        }
        return false;
    }


}
