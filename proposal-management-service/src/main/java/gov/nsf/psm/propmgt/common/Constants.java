package gov.nsf.psm.propmgt.common;

public final class Constants {

    // Error Messages
    public static final String GET_ALL_FUNDING_OPPORTUNITY_ERROR = "Get All Funding Opportunity has encounterd an error while calling Solicitation Data Service";
    public static final String GET_ALL_DIVISIONS_ERROR = "Get All Divisions has encounterd an error while calling Solicitation Data Service";
    public static final String GET_ALL_DIRECTORATES_ERROR = "Get All Directorates has encounterd an error while calling Solicitation Data Service";
    public static final String GET_ALL_PROGRAM_ELEMENTS_ERROR = "Get All Program Elements has encounterd an error while calling Solicitation Data Service";
    public static final String GET_DIRECTORATES_FUNDING_OPPS_ERROR = "Get Directorates for a funding opportunity has encounterd an error while calling Solicitation Data Service";
    public static final String GET_DIVISIONS_BY_FUNDING_OPP_DIRECTORATE_ID_ERROR = "Get Divisions for a funding opportunity has encounterd an error while calling Solicitation Data Service";
    public static final String GET_PROGRAM_ELEMENTS_BY_DIVISION_ID_ERROR = "Get Program Elements for a funding opportunity along with division id has encounterd an error while calling Solicitation Data Service";
    public static final String GET_DIRECTORATE_BY_FUNDING_OPP_ID_ERROR = "Get Details of a Directorates by directorate id has encounterd an error while calling Solicitation Data Service";
    public static final String GET_ALL_STATES_ERROR = "Get All States has encountered an error while calling Solicitation Data Service";
    public static final String GET_ALL_COUNTRIES_ERROR = "Get All Countries has encounterd an error while calling Solicitation Data Service";
    public static final String GET_PROGRAM_DEADLINES_ERROR = "Get Program Deadlines  has encounterd an error while calling Solicitation Data Service";
    public static final String GET_POSTAL_CODE_VALIDATION_ERROR = "Postal Code validation has encounterd an error while calling Solicitation Data Service";
    public static final String UPLOAD_SECTION_SERVLET_EXCEPTION = "Servlet Exception error encountered while uploading file";
    public static final String UPLOAD_SECTION_IO_EXCEPTION = "IO Exception error encountered while uploading file";
    public static final String GET_FILE_IO_EXCEPTION = "IO Exception error encountered while retrieving file";
    public static final String GET_FILE_EXCEPTION = "Exception error encountered while retrieving file";
    public static final String GET_ELECTRONIC_SIGNATURE_ERROR = "Get Electronic Signature information failed while calling Proposal Data Service";
    public static final String SAVE_SUBMIT_PROPOSAL_WITH_ELECTRONIC_SIGNATURE_ERROR = "An error occurred submitting the Proposal ";
    public static final String GET_USER_BY_EMAIL_UDS = "Exception encountered retrieving user data by email through UDS";
    public static final String SAVE_PROPOSAL_UPDATE_JUSTIFICATIOIN_ERROR = "Save Proposal Update Justification failed while calling Proposal Data Service";
    public static final String GET_ELECTRONIC_CERTIFICATION_TEXT_ERROR = "Get Electronic Certification Text failed while calling Proposal Data Service";
    public static final String GET_PROPOSAL_ERROR = "Get Proposal failed while calling Proposal Data Service";
    public static final String GET_COVERSHEET_ERROR = "Get Coversheet is  failed while calling Proposal Data Service";
    public static final String GET_PRIMARY_AWARDEE_ORGANIZATION_ID_ERROR = "Get Primary Awardee Organization id is  failed while calling Proposal Data Service";
    public static final String GET_USER_DATA_FROM_UDS_ERROR = "Get User information from UDS Service is failed.";
    public static final String GET_INSTITUTION_DATA_FROM_REF_DATA_SERVICE_ERROR = "Get Institution Data from Reference Data Service is  failed while calling Proposal Data Service";
    public static final String GET_INSTITUTION_BUDGET_ERROR = "Get Institution Budget is  failed while calling Proposal Data Service";
    public static final String GET_PI_DETAILS_ERROR = "Get PI details is failed while calling Proposal Data Service";
    public static final String SAVE_SUBMIT_PROPOSAL_ERROR = "Save Submit Proposal is failed while calling Proposal Data Service";
    public static final String GET_PROPOSAL_UPDATE_JUSTIFICATIOIN_ERROR = "Get Proposal Update Justification failed while calling Proposal Data Service";

    public static final String GET_SECTION_DATA_ERROR = "Error encountered while getting section data.";

    public static final String GET_SECTION_DOCUMENT_ERROR_TEMPLATE = "Error while attempting to retrieve section document %s";

    public static final String DOC_COMPLIANCE_PDF_SIZE = "The dimensions of your document exceed 8 1/2 inches by 11 inches.";
    public static final String DOC_COMPLIANCE_PDF_SIZE_ID = "DC-E-013";

    public static final String GET_SERVICE_IS_DOWN_ERROR = "*********************** THE FOLLOWING SERVICE APPEARS TO BE DOWN: %s **********************";
    public static final String PROPOSAL_STATUS_NOT_AVAILABLE = "The following propoal status not found in GAPPS: %s ";
    public static final String ENV_DEV = "DEV";
    public static final String ENV_INTG = "INTG";
    public static final String ENV_PROD = "PROD";

    public static final String PI_ROLE_CODE = "01";
    public static final String COPI_ROLE_CODE = "02";
    public static final String OTHER_SKP_ROLE_CODE = "03";
    public static final String OAU_ROLE_CODE = "04";
    public static final String SPO_ROLE_CODE = "05";
    public static final String AOR_ROLE_CODE = "06";

    public static final String ELECTRONIC_CERT_PROPOSAL_TYPE_CODE = "001";

    public static final String PI_ROLE_ABRV = "PI";
    public static final String COPI_ROLE_ABRV = "Co-PI";
    public static final String OAU_ROLE_ABRV = "OAU";
    public static final String SPO_ROLE_ABRV = "SPO";
    public static final String AOR_ROLE_ABRV = "AOR";
    public static final String OSP_ROLE_ABRV = "OSP";
    public static final String PROP_PERS_ID = "0";

    public static final String DT_FORMAT = "MM/dd/yyyy";

    public static final String PDF_EXT = ".pdf";

    public static final String ORIGINAL_FILENAME_METADATA_KEY = "origFileName";

    public static final String DUE_DATE_TYPE_TARGET_DATE = "1";
    public static final String DUE_DATE_TYPE_DEADLINE_DATE = "2";
    public static final String DUE_DATE_TYPE_ACCEPTED_ANYTIME_CODE = "3";
    public static final String DUE_DATE_TYPE_WINDOW_DATE = "4";
    public static final String DUE_DATE_PASSED = "Due Date Passed";
    public static final String DUE_DATE_PASSED_BUT_NOT_ASSIGNED_FOR_REVIEW = "Submitted to NSF (Due Date Passed But Prior to Reviewer Assignment)";
    public static final String DUE_DATE_PASSED_OR_ASSIGNED_FOR_REVIEW = "Submitted to NSF (Due Date Passed or Assigned for Review)";
    public static final String ASSIGNED_FOR_REVIEW = "Assigned for Review";
    public static final String NOT_ASSIGNED_FOR_REVIEW = "Not Assigned for Review";
    public static final String CANNOT_SUBMIT_ASSIGNED_FOR_REVIEW = "PFU/Budget Revision: Cannot Submit - Assigned For Review";
    public static final String CANNOT_SUBMIT_PFU_PROPOSAL_STATUS_CHANGED = "PFU/Budget Revision: Cannot Submit - Proposal Status Changed";
    public static final String CANNOT_SUBMIT_BREV_PROPOSAL_STATUS_CHANGED = "Budget Revision: Cannot Submit - Proposal Status Changed";

    public static final String PROPOSAL_QUERY_NO_DATES_MSG = "No dates available (cannot submit)";
    public static final String PROPOSAL_QUERY_NO_DATES_SELECTED_MSG = "None selected";

    public static final String MULTI_ORG_YES = "Y";
    public static final String MULTI_ORG_NO = "N";

    public static final String HEADING_PROJECT_OVERVIEW_TEXT = "Overview";
    public static final String HEADING_INTELLECTUAL_MERIT_TEXT = "Intellectual Merit";
    public static final String HEADING_BROADER_IMPACTS_TEXT = "Broader Impacts";

    public static final String DEFAULT_IP_ADDRESS = "1.1.1.1";

    public static final int MAX_REVN_NUM = 2;

    public static final String USA = "US";

    public static final String FL_TPI = "fastLaneProposalTempId";
    public static final String FL_TRANFER_STATUS = "requestStatus";
    public static final String FL_TRANFER_COMPLETED = "COMPLETED";

    // security
    public static final String AUTH_TOKEN_PREFIX = "Bearer ";
    protected static final String[] SITE_EXCEPTIONS = new String[] { "^v2/api-docs", "^swagger-resources/?.*",
            "^swagger-ui.html", "^webjars/springfox-swagger-ui/.*" };

    public static final String SIMPLE_DATE_FORMAT = "MM/dd/yyyy";
    public static final String STATUS_DATE_FORMAT = "MM/dd/yyyy h:mm:ss a z";

    private Constants() {
        super();
    }
}
