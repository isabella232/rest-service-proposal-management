package gov.nsf.psm.propmgt.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;

import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.PSMRole;
import gov.nsf.psm.foundation.model.Personnel;
import gov.nsf.psm.foundation.model.SectionStatus;
import gov.nsf.psm.foundation.model.lookup.SeniorPersonRoleTypeLookUp;
import gov.nsf.psm.foundation.model.proposal.ProposalPackage;
import gov.nsf.psm.foundation.model.proposal.ProposalRevisionType;
import gov.nsf.psm.propmgt.common.Constants;

public class EmailUtils {
    
    private EmailUtils() {
        // Private constructor
    }
    
    public static String getEmailRecipientList(ProposalPackage pkg) {
        List<Personnel> recipients = pkg.getPersonnel();
        Set<String> toAddresses = recipients.stream().map(Personnel::getEmail).collect(Collectors.toSet());
        List<String> toAddressList = new ArrayList<>();
        toAddressList.addAll(toAddresses);
        String toAddressStr = Joiner.on(", ").join(toAddressList);
        return toAddressStr;
    }
    
    public static SeniorPersonRoleTypeLookUp getRole(List<SeniorPersonRoleTypeLookUp> lookups, String roleCode){
        return lookups.stream().filter(lookup -> lookup.getCode().trim().equals(roleCode)).collect(Collectors.toList()).get(0);
    }
    
    public static String getEmailSubjectRole(String role, String action, String proposalId) {
        StringBuilder subjBuilder = new StringBuilder();
        subjBuilder.append(role);
        subjBuilder.append(" " + action + " ");
        subjBuilder.append(proposalId);
        return subjBuilder.toString();
    }
    
    public static boolean isProdEnv(String ctx) {
        if(ctx == null || StringUtils.isEmpty(ctx)) {
            return false;
        } else {
            return ctx.equalsIgnoreCase(Constants.ENV_PROD);
        }
    }
    
    public static List<Personnel> sortPersonnelListByRoleCode(List<Personnel> list) {
        final class CustomComparator implements Comparator<Personnel> {
            @Override
            public int compare(Personnel p1, Personnel p2) {
                return p1.getPSMRole().getCode().compareTo(p2.getPSMRole().getCode());
            }
        }
        Collections.sort(list, new CustomComparator());
        return list;
    }
    
    public static String formatName(Personnel pers) {
        return pers.getFirstName() + (StringUtils.isEmpty(pers.getMiddleName()) ? " " : " " + pers.getMiddleName().charAt(0) + " ") + pers.getLastName();
    }
    
    public static boolean isAfterDeadlineDate(ProposalPackage prop) throws CommonUtilException {
            if (prop.getDeadline() != null && prop.getDeadline().getDeadlineDate() != null && !StringUtils.isEmpty(prop.getInstitution().getTimeZone())) {
                SimpleDateFormat format = new SimpleDateFormat(Constants.STATUS_DATE_FORMAT);
                Date dueDate = prop.getDeadline().getDeadlineDate();
                Calendar cal = Calendar.getInstance();
                cal.setTime(dueDate);
                cal.add(Calendar.HOUR_OF_DAY,17);
                TimeZone tz = TimeZone.getTimeZone(prop.getInstitution().getTimeZone());
                SimpleDateFormat tzFormat = new SimpleDateFormat(Constants.STATUS_DATE_FORMAT);
                tzFormat.setTimeZone(tz);
                Calendar calTz = Calendar.getInstance();
                calTz.setTime(new Date());
                String dateOne = tzFormat.format(calTz.getTime());
                String dateTwo = format.format(cal.getTime());
                try {
                    return format.parse(dateOne).after(format.parse(dateTwo));
                } catch (ParseException e) {
                    throw new CommonUtilException(e);
                }
            }
            return false;
    }
    
}
