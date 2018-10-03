package gov.nsf.psm.propmgt.common;

public class EmailConstants {
    
    public static final String EMAIL_HEADER_TO = "to";
    public static final String EMAIL_HEADER_FROM = "from";
    public static final String EMAIL_HEADER_DATE = "date";
    public static final String EMAIL_HEADER_SUBJ = "subject";
    public static final String EMAIL_BODY_NAME = "nameReference";
    public static final String EMAIL_BODY_ROLE = "role";
    public static final String EMAIL_BODY_PROP_ID = "proposalId";
    public static final String EMAIL_BODY_PROP_ID_TEMP = "tempProposalId";
    public static final String EMAIL_BODY_INST_NAME = "instName";
    public static final String EMAIL_BODY_PROP_TITLE = "proposalTitle";
    public static final String EMAIL_BODY_DATE_TIME = "dateTimeAdded";
    public static final String EMAIL_DEFAULT_ENCODING = "UTF-8";
    public static final String EMAIL_DEFAULT_SENDER_ADDR = "proposalprep@research.gov";
    public static final String EMAIL_SUBJECT = "subject";
    public static final String EMAIL_BODY_PROP_STATUS = "propStatus";
    public static final String EMAIL_BODY_PROP_ID_TITLE = "proposalIdTitle";
    public static final String EMAIL_BODY_FIRST_LINE_INSERT = "firstLineInsert";
    public static final String EMAIL_BODY_JUSTIFICATION_TEXT = "justificationText";
    public static final String EMAIL_BODY_PI_NAME = "piName";
    public static final String EMAIL_BODY_PI_EMAIL = "piEmail";
    public static final String EMAIL_BODY_AOR_NAME = "aorName";
    public static final String EMAIL_BODY_AOR_EMAIL = "aorEmail";
    public static final String EMAIL_BODY_DUE_DATE = "dueDate";
    public static final String EMAIL_DISCLAIMER_INSERT ="disclaimer";
    public static final String STATUS_DATE_FORMAT = "MM/dd/yyyy h:mm a z";
    public static final String SIMPLE_DATE_FORMAT = "MM/dd/yyyy";
    public static final String EMAIL_DISCLAIMER_TXT = "<div class='box'>\n" + 
            "            Need Help?<br>\n" + 
            "            You can find helpful Research.gov information by clicking Help in the top right-hand corner of Research.gov.<br>\n" + 
            "            For additional assistance, please contact the NSF Help Desk at <a href='dial: 1-800-381-1532'>1-800-381-1532</a> or <a href='mailto: Rgov@nsf.gov'>Rgov@nsf.gov</a>.<br>\n" + 
            "            Please DO NOT REPLY TO THIS MESSAGE, as this email was sent from an address that cannot accept incoming messages.\n" + 
            "         </div>";
    
    private EmailConstants() {
        super();
    }

}
