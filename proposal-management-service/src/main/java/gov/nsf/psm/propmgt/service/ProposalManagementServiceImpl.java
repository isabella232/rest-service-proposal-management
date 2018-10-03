package gov.nsf.psm.propmgt.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.WordUtils;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpServerErrorException;

import com.google.common.base.Joiner;

import gov.nsf.proposal.model.ProposalReview;
import gov.nsf.proposal.model.ProposalReviewWrapper;
import gov.nsf.psm.compliancevalidation.ComplianceValidationServiceClient;
import gov.nsf.psm.documentcompliance.DocumentComplianceServiceClient;
import gov.nsf.psm.documentgeneration.DocumentGenerationServiceClient;
import gov.nsf.psm.factmodel.CoverSheetFactModel;
import gov.nsf.psm.factmodel.DeadlineFactModel;
import gov.nsf.psm.factmodel.DocumentFactModel;
import gov.nsf.psm.factmodel.FileFactModel;
import gov.nsf.psm.factmodel.InstitutionBudgetFactModel;
import gov.nsf.psm.factmodel.ProposalFactModel;
import gov.nsf.psm.factmodel.ProposalUpdateJustificationFactModel;
import gov.nsf.psm.filestorage.FileStorageServiceClient;
import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.BiographicalSketch;
import gov.nsf.psm.foundation.model.BudgetImpact;
import gov.nsf.psm.foundation.model.BudgetJustification;
import gov.nsf.psm.foundation.model.BudgetRevision;
import gov.nsf.psm.foundation.model.COA;
import gov.nsf.psm.foundation.model.COAResult;
import gov.nsf.psm.foundation.model.CurrentAndPendingSupport;
import gov.nsf.psm.foundation.model.EmailMessageRequest;
import gov.nsf.psm.foundation.model.EmailMessageType;
import gov.nsf.psm.foundation.model.FundingOpportunityParams;
import gov.nsf.psm.foundation.model.Institution;
import gov.nsf.psm.foundation.model.InstitutionAddress;
import gov.nsf.psm.foundation.model.OtherSuppDocs;
import gov.nsf.psm.foundation.model.PSMMessage;
import gov.nsf.psm.foundation.model.PSMMessageType;
import gov.nsf.psm.foundation.model.PSMMessages;
import gov.nsf.psm.foundation.model.PSMRole;
import gov.nsf.psm.foundation.model.PdfGenerationData;
import gov.nsf.psm.foundation.model.Personnel;
import gov.nsf.psm.foundation.model.PersonnelData;
import gov.nsf.psm.foundation.model.PersonnelParam;
import gov.nsf.psm.foundation.model.PersonnelSectionData;
import gov.nsf.psm.foundation.model.Personnels;
import gov.nsf.psm.foundation.model.Pi;
import gov.nsf.psm.foundation.model.ProjectSummary;
import gov.nsf.psm.foundation.model.ProposalElectronicSign;
import gov.nsf.psm.foundation.model.ProposalUpdateJustification;
import gov.nsf.psm.foundation.model.Section;
import gov.nsf.psm.foundation.model.SectionResponse;
import gov.nsf.psm.foundation.model.SectionStatus;
import gov.nsf.psm.foundation.model.SrPersonUploadData;
import gov.nsf.psm.foundation.model.UploadableProposalSection;
import gov.nsf.psm.foundation.model.WarnMsgs;
import gov.nsf.psm.foundation.model.budget.BudgetTotals;
import gov.nsf.psm.foundation.model.budget.InstitutionBudget;
import gov.nsf.psm.foundation.model.budget.OtherPersonnelCost;
import gov.nsf.psm.foundation.model.compliance.ComplianceConfig;
import gov.nsf.psm.foundation.model.compliance.ComplianceData;
import gov.nsf.psm.foundation.model.compliance.ComplianceModel;
import gov.nsf.psm.foundation.model.compliance.ComplianceResponse;
import gov.nsf.psm.foundation.model.compliance.doc.SectionModel;
import gov.nsf.psm.foundation.model.coversheet.CoverSheet;
import gov.nsf.psm.foundation.model.coversheet.PiCoPiInformation;
import gov.nsf.psm.foundation.model.coversheet.PrimaryPlaceOfPerformance;
import gov.nsf.psm.foundation.model.filestorage.DeleteFileResponse;
import gov.nsf.psm.foundation.model.filestorage.GetFileResponse;
import gov.nsf.psm.foundation.model.filestorage.UploadFileResponse;
import gov.nsf.psm.foundation.model.generation.GetGeneratedDocumentResponse;
import gov.nsf.psm.foundation.model.login.InstitutionRole;
import gov.nsf.psm.foundation.model.login.User;
import gov.nsf.psm.foundation.model.lookup.Deadline;
import gov.nsf.psm.foundation.model.lookup.DeadlineData;
import gov.nsf.psm.foundation.model.lookup.Deadlines;
import gov.nsf.psm.foundation.model.lookup.ElectronicCertificationText;
import gov.nsf.psm.foundation.model.lookup.SeniorPersonRoleTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.State;
import gov.nsf.psm.foundation.model.proposal.Proposal;
import gov.nsf.psm.foundation.model.proposal.ProposalCompleteTransfer;
import gov.nsf.psm.foundation.model.proposal.ProposalCopy;
import gov.nsf.psm.foundation.model.proposal.ProposalHeader;
import gov.nsf.psm.foundation.model.proposal.ProposalPackage;
import gov.nsf.psm.foundation.model.proposal.ProposalPersonnelSectionStatus;
import gov.nsf.psm.foundation.model.proposal.ProposalQueryParams;
import gov.nsf.psm.foundation.model.proposal.ProposalQueryResult;
import gov.nsf.psm.foundation.model.proposal.ProposalQueryRevisionResult;
import gov.nsf.psm.foundation.model.proposal.ProposalResponse;
import gov.nsf.psm.foundation.model.proposal.ProposalRevision;
import gov.nsf.psm.foundation.model.proposal.ProposalRevisionPersonnel;
import gov.nsf.psm.foundation.model.proposal.ProposalRevisionType;
import gov.nsf.psm.foundation.model.proposal.ProposalSection;
import gov.nsf.psm.foundation.model.proposal.ProposalSectionStatus;
import gov.nsf.psm.foundation.model.proposal.ProposalStatus;
import gov.nsf.psm.foundation.model.proposal.SectionCompliance;
import gov.nsf.psm.foundation.model.proposal.SectionComplianceStatus;
import gov.nsf.psm.foundation.model.proposal.SubmittedProposal;
import gov.nsf.psm.foundation.model.proposal.UOCInformation;
import gov.nsf.psm.propmgt.common.Constants;
import gov.nsf.psm.propmgt.common.GappsStatuses;
import gov.nsf.psm.propmgt.common.PropMgtMessagesEnum;
import gov.nsf.psm.propmgt.utility.CoverSheetFactModelUtility;
import gov.nsf.psm.propmgt.utility.DocumentUtils;
import gov.nsf.psm.propmgt.utility.EmailUtils;
import gov.nsf.psm.propmgt.utility.InstBudgetUtils;
import gov.nsf.psm.propmgt.utility.PropCOAUtils;
import gov.nsf.psm.propmgt.utility.PropCopyUtils;
import gov.nsf.psm.propmgt.utility.PropMgtUtil;
import gov.nsf.psm.propmgt.utility.PropQueryUtils;
import gov.nsf.psm.propmgt.utility.ProposalFactModelUtility;
import gov.nsf.psm.propmgt.utility.ProposalFileUtility;
import gov.nsf.psm.propmgt.utility.SectionStatusUtils;
import gov.nsf.psm.proposaldata.ProposalDataServiceClient;
import gov.nsf.psm.proposaltransfer.api.exception.ProposalTransferRequestException;
import gov.nsf.psm.proposaltransfer.api.model.ProposalTransferRequest;
import gov.nsf.psm.proposaltransfer.api.model.RequestParams;
import gov.nsf.psm.proposaltransfer.client.ProposalTransferServiceClient;
import gov.nsf.psm.solicitation.SolicitationDataServiceClient;
import gov.nsf.research.services.gapps.v1.GappsDetailRequest;
import gov.nsf.research.services.gapps.v1.GappsDetailResponse;
import gov.nsf.research.services.gapps.v1.GappsSearchRequest;
import gov.nsf.research.services.gapps.v1.GappsSearchResponse;
import gov.nsf.research.services.gapps.v1.GrantApplication;
import gov.nsf.research.services.gapps.v1.GrantApplicationListRowLite;
import gov.nsf.research.services.gapps.v1.GrantApplicationRequest;
import gov.nsf.research.services.gapps.v1.ProgramOfficer;
import gov.nsf.service.model.UdsAgencyIdentity;
import gov.nsf.service.model.UdsGetUserDataResponse;
import gov.nsf.service.model.UdsNsfIdUserData;
import gov.nsf.userdata.api.model.UserData;

public class ProposalManagementServiceImpl implements ProposalManagementService {

    @Autowired
    CachedDataService cachedDataService;

    @Autowired
    SolicitationDataServiceClient solicitationDataServiceclient;

    @Autowired
    ProposalDataServiceClient proposalDataServiceClient;

    @Autowired
    FileStorageServiceClient fileStorageServiceClient;

    @Autowired
    DocumentComplianceServiceClient documentComplianceServiceClient;

    @Autowired
    ComplianceValidationServiceClient complianceValidationServiceClient;

    @Autowired
    DocumentGenerationServiceClient documentGenerationServiceClient;

    @Autowired
    ExternalServices externalServices;

    @Autowired
    UserDetailsService userDatailsService;

    @Autowired
    ProposalFileUtility propFileUtil;

    @Autowired
    ProposalManagementForTransferService proposalManagementForTransferService;

    @Autowired
    EmailService emailService;

    @Autowired
    ProposalTransferServiceClient proposalTransferServiceClient;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private static final boolean BYPASS_TEXT_EXTRACTION = true;
    private static final String NOT_AVAILABLE = "Not Available";
    private static final String UNASSIGNED = "Unassigned";
    private static final String PM_I_001 = "PM-I-001";

    @Value("${propmgt.enable-proposal-data-transfer}")
    public Boolean enableProposalDataTransfer;

    @Override
    public List<Deadline> getDueDates(String fundingOpId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.getDeadlineDates()");
        try {
            return solicitationDataServiceclient.getDueDates(fundingOpId);
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_PROGRAM_DEADLINES_ERROR, e);
        }
    }

    @Override
    public ProposalPackage getProposalForm(String propPrepId, String revId, String nsfId) throws CommonUtilException {
        ProposalPackage pkg = getProposal(propPrepId, revId);
        LOGGER.debug("Proposal " + pkg.getNsfPropId() + " available for revision = " + pkg.getIsAvailableForRevision());
        Map<String, GrantApplicationListRowLite> gappsResults = new HashMap<String, GrantApplicationListRowLite>();
        GrantApplicationListRowLite grantApplication = null;
        if (!StringUtils.isEmpty(pkg.getNsfPropId())) {
            LOGGER.debug(
                    "findGrantApplicationForANsfId, proposalNsfPropId: " + pkg.getNsfPropId() + " proposalPiNsfId: "
                            + (!StringUtils.isEmpty(pkg.getPi().getNsfId()) ? pkg.getPi().getNsfId() : "")
                            + " loggedUserNsfId: " + nsfId);
            Map<String, String> nsfIds = new HashMap<String, String>();
            if (!StringUtils.isEmpty(pkg.getPi().getNsfId())) {
                nsfIds.put(pkg.getPi().getNsfId(), pkg.getPi().getNsfId());
            }
            if (!StringUtils.isEmpty(pkg.getLatestSubmittedPiNsfId())) {
                nsfIds.put(pkg.getLatestSubmittedPiNsfId(), pkg.getLatestSubmittedPiNsfId());
            }
            for (Map.Entry<String, String> entry : nsfIds.entrySet()) {
                gappsResults.putAll(findGrantApplicationForANsfId(entry.getValue()));
            }
            if (gappsResults.containsKey(pkg.getNsfPropId())) {
                grantApplication = gappsResults.get(pkg.getNsfPropId());
            }
        }

        if (pkg.getProposalStatus().trim().equals(ProposalStatus.SUBMITTED_TO_NSF)) {
            if (!StringUtils.isEmpty(pkg.getNsfPropId())) {
                GrantApplication ga = getGrantApplicationDetailsFromGapps(
                        !StringUtils.isEmpty(pkg.getPi().getNsfId()) ? pkg.getPi().getNsfId() : nsfId,
                        pkg.getNsfPropId());
                if (ga != null) {
                    List<ProgramOfficer> gappsProgramOfficers = ga.getProgram().getProgramOfficers();
                    if (!gappsProgramOfficers.isEmpty()) {
                        ProgramOfficer programOfficer0 = gappsProgramOfficers.get(0);
                        if (!StringUtils.isEmpty(programOfficer0.getEmailAddress())
                                && !NOT_AVAILABLE.equalsIgnoreCase(programOfficer0.getEmailAddress())
                                && !UNASSIGNED.equalsIgnoreCase(programOfficer0.getEmailAddress())) {
                            pkg.setProgramOfficerEmail(programOfficer0.getEmailAddress());
                        }
                        if (!StringUtils.isEmpty(programOfficer0.getFullName())
                                && !NOT_AVAILABLE.equalsIgnoreCase(programOfficer0.getFullName())
                                && !UNASSIGNED.equalsIgnoreCase(programOfficer0.getFullName())) {
                            pkg.setProgramOfficerName(programOfficer0.getFullName());
                        }
                        if (!StringUtils.isEmpty(programOfficer0.getPhoneNumber())
                                && !NOT_AVAILABLE.equalsIgnoreCase(programOfficer0.getPhoneNumber())
                                && !UNASSIGNED.equalsIgnoreCase(programOfficer0.getPhoneNumber())) {
                            pkg.setProgramOfficerPhoneNumber(programOfficer0.getPhoneNumber());
                        }
                    }
                }
            }

            String proposalStatusDesc = pkg.getProposalStatusDesc();
            pkg.setProposalStatusDesc("");
            boolean isSPOAOR = userDatailsService.userHasSPOAORRole(nsfId);
            if (isSPOAOR && (pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_VIEW_EDIT_SPO)
                    || pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_SUBMITTED_ACCESS_FOR_AOR))) {
                pkg.setIsAvailableForRevision(true);
            }
            String revisionType = getRevisionTypeIfAvailableForRevision(grantApplication, pkg);
            if ((StringUtils.isEmpty(revisionType)) && (grantApplication != null)) {
                pkg.setProposalStatusDesc(GappsStatuses.getStatus(grantApplication.getStatusCode()));
            }
            if (revisionType.equals(ProposalRevisionType.BUDGET_REVISION) && (grantApplication != null)
                    && (GappsStatuses.getStatus(grantApplication.getStatusCode())
                            .equalsIgnoreCase(ProposalStatus.GAPPS_RECOMMENDED)
                            || GappsStatuses.getStatus(grantApplication.getStatusCode())
                                    .equalsIgnoreCase(ProposalStatus.GAPPS_PENDING))) {
                pkg.setProposalStatusDesc(GappsStatuses.getStatus(grantApplication.getStatusCode()));
            }
            if ((revisionType.equals(ProposalRevisionType.BUDGET_REVISION)
                    || revisionType.equals(ProposalRevisionType.PROPOSAL_FILE_UPDATE))
                    && pkg.getIsAvailableForRevision()) {
                pkg.setIsAvailableForRevision(true);
            } else {
                pkg.setIsAvailableForRevision(false);
            }
            if (revisionType.equals(ProposalRevisionType.PROPOSAL_FILE_UPDATE) && pkg.isHasPFU()) {
                ProposalRevision proposalRevision = proposalDataServiceClient.getProposalRevision(propPrepId,
                        pkg.getLatestPropRevId());
                if ((proposalRevision.getRevisionType().getType().equals(ProposalRevisionType.PROPOSAL_FILE_UPDATE))
                        && (proposalRevision.getProposalStatus().getStatusCode().trim()
                                .equals(ProposalStatus.MANUAL_PFU_REJECTED_VIEW_ONLY))) {
                    pkg.setIsAvailableForRevision(true);
                }
            }
            if (revisionType.equals(ProposalRevisionType.BUDGET_REVISION) && pkg.isHasPFU()) {
                ProposalRevision proposalRevision = proposalDataServiceClient.getProposalRevision(propPrepId,
                        pkg.getLatestPropRevId());
                if (proposalRevision.getRevisionType().getType().equals(ProposalRevisionType.PROPOSAL_FILE_UPDATE)) {
                    pkg.setIsAvailableForRevision(true);
                }
            }
			if (StringUtils.isEmpty(pkg.getProposalStatusDesc())) {
				if (revisionType.equals(ProposalRevisionType.BUDGET_REVISION)) {
					pkg.setProposalStatusDesc(Constants.DUE_DATE_PASSED_OR_ASSIGNED_FOR_REVIEW);
				} else if (revisionType.equals(ProposalRevisionType.PROPOSAL_FILE_UPDATE)) {
					if (!StringUtils.isEmpty(pkg.getDeadline().getDeadlineTypeCode()) && !pkg.getDeadline()
							.getDeadlineTypeCode().trim().equals(Constants.DUE_DATE_TYPE_ACCEPTED_ANYTIME_CODE)) {
						if (pkg.getDeadline().getDeadlineDate() != null && PropQueryUtils
								.isDueDatePassed(pkg.getDeadline().getDeadlineDate(), pkg.getTimeZone())) {
							pkg.setProposalStatusDesc(Constants.DUE_DATE_PASSED_BUT_NOT_ASSIGNED_FOR_REVIEW);
						}
					}
				}
			}
            if (StringUtils.isEmpty(pkg.getProposalStatusDesc())) {
                pkg.setProposalStatusDesc(proposalStatusDesc);
            }
        } else if ((pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_NOT_FORWARDED_TO_SPO))
                || (pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_VIEW_ONLY_SPO_AOR))
                || (pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_VIEW_EDIT_SPO))
                || (pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_RETURN_TO_PI))
                || (pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_SUBMITTED_ACCESS_FOR_AOR))
                || (pkg.getProposalStatus().trim().equals(ProposalStatus.BR_NOT_SHARED_WITH_SPO_AOR))
                || (pkg.getProposalStatus().trim().equals(ProposalStatus.BR_VIEW_ONLY_SPO_AOR))
                || (pkg.getProposalStatus().trim().equals(ProposalStatus.BR_VIEW_EDIT_SPO_AOR))
                || (pkg.getProposalStatus().trim().equals(ProposalStatus.BR_RETURN_TO_PI))
                || (pkg.getProposalStatus().trim().equals(ProposalStatus.BR_SUBMITTED_ACCESS_FOR_AOR))) {
            String revisionType = getRevisionTypeIfAvailableForRevision(grantApplication, pkg);
            if ((revisionType.equals(ProposalRevisionType.BUDGET_REVISION))
                    && ((pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_NOT_FORWARDED_TO_SPO))
                            || (pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_VIEW_ONLY_SPO_AOR))
                            || (pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_VIEW_EDIT_SPO))
                            || (pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_RETURN_TO_PI))
                            || (pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_SUBMITTED_ACCESS_FOR_AOR)))) {
                PSMMessages messages = new PSMMessages();
                if ((grantApplication != null) && (GappsStatuses.getStatus(grantApplication.getStatusCode())
                        .equalsIgnoreCase(ProposalStatus.GAPPS_RECOMMENDED)
                        || GappsStatuses.getStatus(grantApplication.getStatusCode())
                                .equalsIgnoreCase(ProposalStatus.GAPPS_PENDING))) {
                    messages.addMessage(PropMgtMessagesEnum.PM_E_094.getMessage());
                    pkg.setProposalStatusDesc(Constants.CANNOT_SUBMIT_PFU_PROPOSAL_STATUS_CHANGED);
                } else {
                    messages.addMessage(PropMgtMessagesEnum.PM_E_091.getMessage());
                    pkg.setProposalStatusDesc(Constants.CANNOT_SUBMIT_ASSIGNED_FOR_REVIEW);
                }
                pkg.setPsmMessages(messages);
            } else if (StringUtils.isEmpty(revisionType)) {
                PSMMessages messages = new PSMMessages();
                if ((pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_NOT_FORWARDED_TO_SPO))
                        || (pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_VIEW_ONLY_SPO_AOR))
                        || (pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_VIEW_EDIT_SPO))
                        || (pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_RETURN_TO_PI))
                        || (pkg.getProposalStatus().trim().equals(ProposalStatus.PFU_SUBMITTED_ACCESS_FOR_AOR))) {
                    messages.addMessage(PropMgtMessagesEnum.PM_E_092.getMessage());
                    pkg.setProposalStatusDesc(Constants.CANNOT_SUBMIT_PFU_PROPOSAL_STATUS_CHANGED);
                }
                if ((pkg.getProposalStatus().trim().equals(ProposalStatus.BR_NOT_SHARED_WITH_SPO_AOR))
                        || (pkg.getProposalStatus().trim().equals(ProposalStatus.BR_VIEW_ONLY_SPO_AOR))
                        || (pkg.getProposalStatus().trim().equals(ProposalStatus.BR_VIEW_EDIT_SPO_AOR))
                        || (pkg.getProposalStatus().trim().equals(ProposalStatus.BR_RETURN_TO_PI))
                        || (pkg.getProposalStatus().trim().equals(ProposalStatus.BR_SUBMITTED_ACCESS_FOR_AOR))) {
                    messages.addMessage(PropMgtMessagesEnum.PM_E_093.getMessage());
                    pkg.setProposalStatusDesc(Constants.CANNOT_SUBMIT_BREV_PROPOSAL_STATUS_CHANGED);
                }
                pkg.setPsmMessages(messages);
            }
        }

        if (pkg.getIsAvailableForRevision()) { // Only check if proposal is
                                               // already available for revision
            boolean isAvailableForRevision = isProposalTranferComplete(propPrepId, revId);
            if (isAvailableForRevision) {
                LOGGER.debug("ProposalManagementServiceImpl.getProposalForm()::Proposal is available for revision");
            } else {
                LOGGER.debug(
                        "ProposalManagementServiceImpl.getProposalForm()::Proposal is not available for revision because transfer is incomplete");
            }
            pkg.setIsAvailableForRevision(isAvailableForRevision); // Check for
                                                                   // transfer
        }

        return pkg;
    }

    @Override
    public ProposalPackage getProposal(String propPrepId, String revId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.getProposal()");
        try {
            // get proposal package from PSMDB
            ProposalPackage propPkgPD = proposalDataServiceClient.getProposalPrep(propPrepId, revId);

            if (propPkgPD == null || propPkgPD.getUocs() == null) {
                throw new CommonUtilException(
                        "Proposal Package returned by Proposal Data Service is either null or contains null UOCs.");
            }

            List<Deadline> dueDates = getDueDates(propPkgPD.getFundingOp().getFundingOpportunityId());

            if (!dueDates.isEmpty()) {
                propPkgPD.setDueDates(dueDates);
            }

            UOCInformation uocInfo = new UOCInformation();
            uocInfo.setFundingOp(propPkgPD.getFundingOp());
            uocInfo.setUocs(propPkgPD.getUocs());

            UOCInformation uOCInformation = solicitationDataServiceclient.getUOCDetails(uocInfo);

            if (uOCInformation == null) {
                throw new CommonUtilException("Proposal Package returned by Solicitation Data Service is null.");
            }

            if (uOCInformation.getFundingOp() != null) {
                propPkgPD.getFundingOp()
                        .setFundingOpportunityTitle(uOCInformation.getFundingOp().getFundingOpportunityTitle());
                propPkgPD.getFundingOp().setFundingOpportnityEffectiveDate(
                        uOCInformation.getFundingOp().getFundingOpportnityEffectiveDate());
                propPkgPD.getFundingOp().setFundingOpportnityExpirationDate(
                        uOCInformation.getFundingOp().getFundingOpportnityExpirationDate());
            }
            if (uOCInformation.getUocs() != null) {
                propPkgPD.setUocs(uOCInformation.getUocs());
            }

            Institution inst = externalServices.getInstitutionById(propPkgPD.getInstitution().getId());
            propPkgPD.getPi().setInstitution(inst);
            propPkgPD.setInstitution(inst);

            LOGGER.debug("ProposalManagementServiceImpl.getProposal(): propPkgPD - " + propPkgPD);

            return propPkgPD;
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }

    @Override
    public InstitutionBudget getInstitutionBudget(String propPrepId, String revisionId, Section section, String instId)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.getInstitutionBudget()");
        try {
            InstitutionBudget institutionBudget = proposalDataServiceClient.getInstitutionBudget(propPrepId, revisionId,
                    instId);

            Institution inst = externalServices.getInstitutionById(institutionBudget.getInstId());

            institutionBudget.setInstName(inst.getOrganizationName());

            return institutionBudget;

        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }

    @Override
    public InstitutionBudget getInstitutionBudgetForValidate(String propPrepId, String revisionId, Section section,
            String instId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.getInstitutionBudget()");
        try {
            InstitutionBudget institutionBudget = proposalDataServiceClient.getInstitutionBudget(propPrepId, revisionId,
                    instId);

            return institutionBudget;

        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }

    @Override
    public ProposalPackage saveProposal(ProposalPackage proposalPackage) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.saveProposal()");
        try {
            String instId = null;
            ProposalPackage pkg = new ProposalPackage();

            List<Institution> piInsts = userDatailsService.getInstitutionIdUserIsPI(proposalPackage.getPi().getNsfId());

            if (!piInsts.isEmpty()) {
                instId = piInsts.get(0).getId();

                Institution institution = externalServices.getInstitutionById(instId);
                proposalPackage.setInstitution(institution);
                proposalPackage.getPi().setInstitution(institution);

                List<Deadline> deadlines = solicitationDataServiceclient
                        .getDueDates(proposalPackage.getFundingOp().getFundingOpportunityId());

                if (!deadlines.isEmpty() && deadlines.get(0).getDeadlineTypeCode()
                        .equalsIgnoreCase(Constants.DUE_DATE_TYPE_ACCEPTED_ANYTIME_CODE)) {
                    Deadline deadline = new Deadline();
                    deadline.setDeadlineTypeCode(Constants.DUE_DATE_TYPE_ACCEPTED_ANYTIME_CODE);
                    proposalPackage.setDeadline(deadline);
                }

                pkg = proposalDataServiceClient.saveProposalPrep(proposalPackage);
                pkg.getPi().getInstitution().setOrganizationName(institution.getOrganizationName());

            } else {
                // user has no PI role
                PSMMessages msgs = new PSMMessages();
                msgs.addMessage(PropMgtMessagesEnum.PM_E_008.getMessage());
                pkg.setPsmMessages(msgs);
            }

            return pkg;
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }

    @Override
    public PSMMessages saveUploadedSection(String propPrepId, String revisionId, Section section, String instId,
            byte[] uploadedFile, Map<String, String> metaData) throws CommonUtilException {
        LOGGER.info("ProposalManagementServiceImpl.saveUploadedSection()");

        PSMMessages messages = new PSMMessages();
        List<String> keywordList = new ArrayList<>();
        UploadableProposalSection uploadableSection = new UploadableProposalSection();
        List<String> expectedMimeTypes = new ArrayList<>();
        expectedMimeTypes.add(ComplianceModel.MIME_TYPE_PDF);
        ComplianceConfig config = new ComplianceConfig();
        config.setMimeTypes(expectedMimeTypes);

        FileFactModel fileModel = DocumentUtils.getFileFactModel(metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY),
                uploadedFile.length);

        /* Document Compliance Check */

        // Check meta data
        ComplianceModel compModel = documentComplianceServiceClient
                .getMetadata(metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY), uploadedFile, config);
        ComplianceData results = new ComplianceData();

        if (!compModel.isCorrectMimeType()) {
            try {
                DocumentFactModel model = new DocumentFactModel();
                model.setPropPrepId(propPrepId);
                model.setPropRevId(revisionId);
                model.setMimeType(compModel.getMimeType());
                model.setTargetMimeType(ComplianceModel.MIME_TYPE_PDF);
                model.setSectionName(section.getName());
                model.setFile(fileModel);
                List<PSMMessage> msgs = new ArrayList<>();
                for (gov.nsf.psm.factmodel.PSMMessage cvMsg : complianceValidationServiceClient
                        .getDocumentUploadComplianceCheckFindings(model)) {
                    PSMMessage msg = new PSMMessage();
                    msg.setDescription(cvMsg.getDescription());
                    msg.setId(cvMsg.getId());
                    msg.setType(PSMMessageType.getMessageType(cvMsg.getType().getCode()));
                    msgs.add(msg);
                }
                results.setCompliant(false);
                messages.addMessagesList(msgs);
            } catch (CommonUtilException e) {
                LOGGER.error("", e);
            }
        } else {
            ComplianceData data = DocumentUtils.checkPDFDimensions(compModel.getDocModel());
            results.setMessages(new ArrayList<>());
            results.getMessages().addAll(data.getMessages());
            messages.addMessagesList(results.getMessages());
        }

        // If file can be processed, then check compliance
        if (results.isCompliant()) {
            compModel = documentComplianceServiceClient
                    .getComplianceModel(metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY), uploadedFile, config);
            try {
                DocumentFactModel model = compModel.getDocModel().getDocFactModel();
                model.setPropPrepId(propPrepId);
                model.setPropRevId(revisionId);
                model.setMimeType(compModel.getMimeType());
                model.setCorrectMimeType(compModel.isCorrectMimeType());
                model.setTargetMimeType(ComplianceModel.MIME_TYPE_PDF);
                model.setSectionName(section.getName());
                model.setFile(fileModel);
                List<PSMMessage> msgs = new ArrayList<>();
                for (gov.nsf.psm.factmodel.PSMMessage cvMsg : complianceValidationServiceClient
                        .getDocumentUploadComplianceCheckFindings(model)) {
                    PSMMessage msg = new PSMMessage();
                    msg.setDescription(cvMsg.getDescription());
                    msg.setId(cvMsg.getId());
                    msg.setType(PSMMessageType.getMessageType(cvMsg.getType().getCode()));
                    LOGGER.info("saveUploadedSection adding PCV msg to persist :: " + msg);
                    msgs.add(msg);
                }
                results.setMessages(msgs);
                results.setCompliant(!PropMgtUtil.hasErrorMessages(msgs));
            } catch (CommonUtilException e) {
                LOGGER.error("", e);
            }
            messages.addMessagesList(results.getMessages());
            ComplianceData data = DocumentUtils.checkPDFDimensions(compModel.getDocModel());
            messages.addMessagesList(data.getMessages());
            if (results.isCompliant()) {
                results.setCompliant(!PropMgtUtil.hasErrorMessages(data.getMessages()));
            }
        }

        // Set PSMMessages and Pass DataSvc to persist.
        LOGGER.debug("PSMMessages list size:: " + messages.getPsmMessages().size());
        uploadableSection.setPropMessages(messages.getPsmMessages());

        // If file is compliant, upload
        if (results.isCompliant()) {

            /* Upload Section to File Storage */
            String fileKey = propFileUtil.constructPSMFileKeyForSectionDocument(propPrepId, revisionId,
                    section.getCamelCaseName(), metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY));
            UploadFileResponse fsResp = fileStorageServiceClient.uploadFile(fileKey, uploadedFile);

            /* Save Section in DB */
            if (fsResp != null && fsResp.getUploadSuccessful()) {

                LOGGER.debug(fsResp.toString());

                SectionResponse secResp = null;

                // Check for text extraction
                switch (section) {
                case PROJ_SUMM:
                    results = DocumentUtils.checkPDFDimensions(compModel.getDocModel());
                    ProjectSummary summary = new ProjectSummary();
                    PropMgtUtil.setAuditFields(summary, metaData.get(PropMgtUtil.NSFID));

                    if (results.getSections() != null && !results.getSections().isEmpty()) {
                        extractProjectSummaryText(summary, results);
                    }

                    // Remove this workaround when text extraction becomes
                    // enabled
                    if (BYPASS_TEXT_EXTRACTION) {
                        summary.setBrodimpact(" ");
                        summary.setOverview(" ");
                        summary.setIntellmerit(" ");
                    }
                    // set warning message to persist
                    summary.setPropMessages(messages.getPsmMessages());
                    if (!StringUtils.isEmpty(summary.getOverview()) || !StringUtils.isEmpty(summary.getIntellmerit())
                            || !StringUtils.isEmpty(summary.getBrodimpact())) {
                        secResp = uploadProjectSummarySection(fsResp.getFilePath(),
                                metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY),
                                (int) compModel.getDocModel().getNoOfPages(), propPrepId, revisionId, summary);

                    } else {
                        secResp = new SectionResponse();
                        secResp.setSaveStatus(false);
                    }

                    break;
                case BUDGET_JUST:
                    BudgetJustification budgetJustification = new BudgetJustification();
                    PropMgtUtil.setAuditFields(budgetJustification, metaData.get(PropMgtUtil.NSFID));
                    secResp = uploadBudgetJustificationSection(fsResp.getFilePath(),
                            metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY),
                            (int) compModel.getDocModel().getNoOfPages(), propPrepId, revisionId, budgetJustification,
                            keywordList);
                    break;
                default:
                    PropMgtUtil.setAuditFields(uploadableSection, metaData.get(PropMgtUtil.NSFID));
                    secResp = uploadUploadableSection(fsResp.getFilePath(),
                            metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY),
                            (int) compModel.getDocModel().getNoOfPages(), propPrepId, revisionId, section,
                            uploadableSection);
                    break;
                }

                LOGGER.debug("Save status: " + secResp.getSaveStatus());

            } else {
                messages.addMessage(PropMgtMessagesEnum.PM_E_002.getMessage());
            }
        }
        return messages;
    }

    @Override
    public ComplianceResponse previewCOASection(byte[] uploadedFile, Map<String, String> metaData)
            throws CommonUtilException {
        LOGGER.info("ProposalManagementServiceImpl.previewCOASection()");

        ComplianceData results = new ComplianceData();
        ComplianceModel compModel = new ComplianceModel();
        ComplianceConfig config = new ComplianceConfig();
        PSMMessages messages = new PSMMessages();
        FileFactModel fileModel = null;

        try {
            fileModel = DocumentUtils.getFileFactModel(metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY),
                    uploadedFile.length);
        } catch (Exception e) {
            throw new CommonUtilException("Error encountered getting fact model for COA", e);
        }

        List<String> expectedMimeTypes = new ArrayList<>();
        expectedMimeTypes.add(ComplianceModel.MIME_TYPE_XLSX);
        config.setMimeTypes(expectedMimeTypes);
        config.setTablesOnly(true);

        /* Document Compliance Check */
        // Check meta data
        try {
            compModel = documentComplianceServiceClient
                    .getMetadata(metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY), uploadedFile, config);
        } catch (Exception e) {
            throw new CommonUtilException("Error encountered getting meta data for COA", e);
        }

        try {
            DocumentFactModel model = new DocumentFactModel();
            model.setMimeType(compModel.getMimeType());
            model.setCorrectMimeType(compModel.isCorrectMimeType());
            model.setTargetMimeType(ComplianceModel.MIME_TYPE_XLSX);
            model.setSectionName(Section.COA.getName());
            model.setFile(fileModel);
            List<PSMMessage> msgs = new ArrayList<>();
            for (gov.nsf.psm.factmodel.PSMMessage cvMsg : complianceValidationServiceClient
                    .getDocumentUploadComplianceCheckFindings(model)) {
                PSMMessage msg = new PSMMessage();
                msg.setDescription(cvMsg.getDescription());
                msg.setId(cvMsg.getId());
                msg.setType(PSMMessageType.getMessageType(cvMsg.getType().getCode()));
                msgs.add(msg);
            }
            results.setCompliant(msgs.isEmpty() || (!msgs.isEmpty() && PM_I_001.equalsIgnoreCase(msgs.get(0).getId())));
            messages.addMessagesList(msgs);
        } catch (CommonUtilException e) {
            throw new CommonUtilException("Error encountered checking compliance for COA", e);
        } catch (Exception e) {
            throw new CommonUtilException("Error encountered checking compliance for COA", e);
        }

        // If file can be processed, then check compliance
        if (results.isCompliant()) {
            try {
                compModel = documentComplianceServiceClient.getComplianceModel(
                        metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY), uploadedFile, config);
                compModel.setSsModel(PropCOAUtils.convertTableNames(compModel.getSsModel()));
            } catch (Exception e) {
                throw new CommonUtilException("Error encountered getting compliance model for COA", e);
            }

        }

        ComplianceResponse wrapper = new ComplianceResponse();
        wrapper.setModel(compModel);
        wrapper.setMessages(messages);
        return wrapper;
    }

    @Override
    public PSMMessages deleteUploadedCOASection(String propPrepId, String revisionId, String propPersId, String nsfId)
            throws CommonUtilException {
        PSMMessages psmMessages = new PSMMessages();

        COA coa = proposalDataServiceClient.getProposalCOA(Long.valueOf(revisionId), Long.valueOf(propPersId));

        boolean coaFilesDeleted = false;
        try {
            DeleteFileResponse fdRespExcel = fileStorageServiceClient.deleteFile(coa.getCoaExcelFilePath());
            DeleteFileResponse fdRespPdf = fileStorageServiceClient.deleteFile(coa.getCoaPdfFilePath());

            coaFilesDeleted = fdRespExcel.getDeleteSuccessful() && fdRespPdf.getDeleteSuccessful();
        } catch (Exception e) {
            throw new CommonUtilException("Error encountered in deleting COA from the file storage", e);
        }

        SectionResponse secResp = new SectionResponse();

        if (coaFilesDeleted) {
            try {
                secResp = proposalDataServiceClient.deleteProposalCOA(Long.valueOf(revisionId),
                        Long.valueOf(propPersId), nsfId);
            } catch (Exception e) {
                throw new CommonUtilException("Error encountered in deleting COA from the database", e);
            }
        }

        if (coaFilesDeleted && secResp.getSaveStatus()) {
            psmMessages.addMessage(PropMgtMessagesEnum.PM_I_002.getMessage());
        } else {
            psmMessages.addMessage(PropMgtMessagesEnum.PM_E_001.getMessage());
        }

        return psmMessages;

    }

    @Override
    public ByteArrayOutputStream getUploadedCOASectionFile(String propPrepId, String revisionId, String propPersId)
            throws CommonUtilException {
        COA coa;
        try {
            coa = proposalDataServiceClient.getProposalCOA(Long.valueOf(revisionId), Long.valueOf(propPersId));
            String fileKey = coa.getCoaPdfFilePath();
            GetFileResponse getFileResponse = fileStorageServiceClient.getFile(fileKey);
            if (getFileResponse.getGetSuccessful()) {
                byte[] getFile = getFileResponse.getFile();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(getFile.length);
                byteArrayOutputStream.write(getFile, 0, getFile.length);
                return byteArrayOutputStream;
            } else {
                return null;
            }
        } catch (NumberFormatException e) {
            throw new CommonUtilException("NumberFormatException encountered in getting proposal COA", e);
        } catch (Exception e) {
            throw new CommonUtilException("Error encountered in getting proposal COA", e);
        }
    }

    @Override
    public ComplianceResponse saveUploadedCOASection(String propPrepId, String revisionId, String propPersId,
            byte[] uploadedFile, Map<String, String> metaData) throws CommonUtilException {

        LOGGER.info("ProposalManagementServiceImpl.saveUploadedCOASection()");

        ComplianceData results = new ComplianceData();
        ComplianceModel compModel = new ComplianceModel();
        ComplianceConfig config = new ComplianceConfig();
        PSMMessages messages = new PSMMessages();

        FileFactModel fileModel = DocumentUtils.getFileFactModel(metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY),
                uploadedFile.length);

        List<String> expectedMimeTypes = new ArrayList<>();
        expectedMimeTypes.add(ComplianceModel.MIME_TYPE_XLSX);
        config.setMimeTypes(expectedMimeTypes);
        config.setTablesOnly(true);

        /* Document Compliance Check */

        // Check meta data
        compModel = documentComplianceServiceClient.getMetadata(metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY),
                uploadedFile, config);
        results = new ComplianceData();

        try {
            DocumentFactModel model = new DocumentFactModel();
            model.setPropPrepId(propPrepId);
            model.setPropRevId(revisionId);
            model.setMimeType(compModel.getMimeType());
            model.setCorrectMimeType(compModel.isCorrectMimeType());
            model.setTargetMimeType(ComplianceModel.MIME_TYPE_XLSX);
            model.setSectionName(Section.COA.getName());
            model.setFile(fileModel);

            List<PSMMessage> msgs = new ArrayList<>();
            for (gov.nsf.psm.factmodel.PSMMessage cvMsg : complianceValidationServiceClient
                    .getDocumentUploadComplianceCheckFindings(model)) {
                PSMMessage msg = new PSMMessage();
                msg.setDescription(cvMsg.getDescription());
                msg.setId(cvMsg.getId());
                msg.setType(PSMMessageType.getMessageType(cvMsg.getType().getCode()));
                msgs.add(msg);
            }
            results.setCompliant(msgs.isEmpty() || (!msgs.isEmpty() && PM_I_001.equalsIgnoreCase(msgs.get(0).getId())));
            messages.addMessagesList(msgs);
        } catch (CommonUtilException e) {
            throw new CommonUtilException("Error encountered in getting mime type for COA", e);
        }

        // If file can be processed, then check compliance
        if (results.isCompliant()) {
            compModel = documentComplianceServiceClient
                    .getComplianceModel(metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY), uploadedFile, config);

            /* Upload Section to File Storage */
            String fileKey = propFileUtil.constructPSMFileKeyForPersonnelDocument(propPrepId, revisionId,
                    Section.COA.getCamelCaseName(), propPersId, metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY));
            UploadFileResponse fsResp = fileStorageServiceClient.uploadFile(fileKey, uploadedFile);

            /* Save Section in DB */
            if (fsResp != null && fsResp.getUploadSuccessful()) {

                COA coa = new COA();
                COAResult coaResult = new COAResult();
                LOGGER.debug(fsResp.toString());
                try {
                    coaResult = PropCOAUtils.convertSpreadsheetModelToCOA(compModel.getSsModel(), fsResp,
                            metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY), Long.parseLong(revisionId),
                            Long.parseLong(propPersId));
                    coa = coaResult.getCoa();
                    if (!coaResult.getInvalidTypes().isEmpty()) {
                        messages.addMessage(PropMgtMessagesEnum.PM_W_200.getMessage());
                    }
                    results.setCompliant(true);
                } catch (CommonUtilException e) {
                    LOGGER.error("Error encountered in converting COA spreadsheet", e);
                    results.setCompliant(false);
                }

                if (results.isCompliant()) {
                    SectionResponse secResp = new SectionResponse();
                    compModel.setSsModel(PropCOAUtils.convertTableNames(compModel.getSsModel()));
                    try {
                        GetGeneratedDocumentResponse coaPDFResp = documentGenerationServiceClient.generateCOAPdf(coa);
                        fileKey = propFileUtil.constructPSMPersonnelFileKeyWithExt(propPrepId, revisionId,
                                Section.COA.getCamelCaseName(), propPersId, Constants.PDF_EXT);
                        UploadFileResponse saveCoaPdfResp = fileStorageServiceClient.uploadFile(fileKey,
                                coaPDFResp.getFile());
                        coa.setCoaPdfFileName(
                                FileUtils.removeExtension(metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY))
                                        + Constants.PDF_EXT);
                        coa.setCoaPdfFilePath(saveCoaPdfResp.getFilePath());
                        coa.setCoaPdfPageCount(coaPDFResp.getPageCount());
                    } catch (NullPointerException e) {
                        LOGGER.error("Error encountered in generating COA PDF", e);
                    }

                    try {
                        PropMgtUtil.setAuditFields(coa, metaData.get(PropMgtUtil.NSFID));
                        secResp = proposalDataServiceClient.updateProposalCOA(coa);
                    } catch (Exception e) {
                        throw new CommonUtilException(e);
                    }
                    LOGGER.debug("Save status: " + secResp.getSaveStatus());
                }

            } else {
                messages.addMessage(PropMgtMessagesEnum.PM_E_002.getMessage());
            }
        }

        ComplianceResponse wrapper = new ComplianceResponse();
        wrapper.setModel(compModel);
        wrapper.setMessages(messages);
        return wrapper;
    }

    private SectionResponse uploadProjectSummarySection(String filePath, String origFileName, int noOfPages,
            String propPrepId, String revisionId, ProjectSummary summary) throws CommonUtilException {
        summary.setFilePath(filePath);
        summary.setOrigFileName(origFileName);
        summary.setPageCount(noOfPages);
        LOGGER.debug(summary.toString());
        return proposalDataServiceClient.saveProjectSummary(summary, propPrepId, revisionId);
    }

    private static void extractProjectSummaryText(ProjectSummary summary, ComplianceData results) {
        for (Entry<SectionModel, StringBuilder> entry : results.getSections().entrySet()) {
            String matchVal = WordUtils.capitalizeFully(entry.getKey().getHeading().getValue().toLowerCase());
            if (entry.getValue() != null && !StringUtils.isEmpty(entry.getValue().toString().trim())) {
                if (matchVal.indexOf(Constants.HEADING_PROJECT_OVERVIEW_TEXT) > -1) {
                    summary.setOverview(entry.getValue().toString());
                } else if (matchVal.indexOf(Constants.HEADING_INTELLECTUAL_MERIT_TEXT) > -1) {
                    summary.setIntellmerit(entry.getValue().toString());
                } else if (matchVal.indexOf(Constants.HEADING_BROADER_IMPACTS_TEXT) > -1) {
                    summary.setBrodimpact(entry.getValue().toString());
                }
            }
        }
    }

    private SectionResponse uploadBudgetJustificationSection(String filePath, String origFileName, int noOfPages,
            String propPrepId, String revisionId, BudgetJustification budgetJustification, List<String> keywordList)
            throws CommonUtilException {
        budgetJustification.setFilePath(filePath);
        budgetJustification.setOrigFileName(origFileName);
        budgetJustification.setPageCount(noOfPages);
        budgetJustification.setProjDescText(Joiner.on("; ").join(keywordList));
        return proposalDataServiceClient.saveBudgetJustification(budgetJustification, propPrepId, revisionId);
    }

    private SectionResponse uploadSeniorPersonnelSection(String filePath, String origFileName, int noOfPages,
            String propPrepId, String propPersId, String revisionId, UploadableProposalSection uploadableSection,
            Section section) throws CommonUtilException {
        SectionResponse secResp = null;
        Map<String, Object> metaData = new HashMap<String, Object>();
        uploadableSection.setFilePath(filePath);
        uploadableSection.setOrigFileName(origFileName);
        uploadableSection.setPageCount(noOfPages);
        LOGGER.debug(uploadableSection.toString());
        secResp = proposalDataServiceClient.saveSeniorPersonnelDocuments(uploadableSection, propPrepId, revisionId,
                propPersId, section, metaData);
        return secResp;
    }

    private SectionResponse uploadUploadableSection(String filePath, String origFileName, int noOfPages,
            String propPrepId, String revisionId, Section section, UploadableProposalSection uploadableSection)
            throws CommonUtilException {
        uploadableSection.setFilePath(filePath);
        uploadableSection.setOrigFileName(origFileName);
        uploadableSection.setPageCount(noOfPages);
        Map<String, Object> metaDataObj = new HashMap<String, Object>();
        LOGGER.debug(uploadableSection.toString());
        return proposalDataServiceClient.saveSectionData(uploadableSection, propPrepId, revisionId, section,
                metaDataObj);
    }

    @Override
    public PSMMessages saveUploadedSeniorPersonnelDocument(String propPrepId, String revisionId, String propPersId,
            Section section, byte[] uploadedFile, Map<String, String> metaData) throws CommonUtilException {
        LOGGER.info("ProposalManagementServiceImpl.saveUploadedSeniorPersonnelDocument()");

        PSMMessages messages = new PSMMessages();
        UploadableProposalSection uploadableSection = new UploadableProposalSection();
        List<String> expectedMimeTypes = new ArrayList<>();
        expectedMimeTypes.add(ComplianceModel.MIME_TYPE_PDF);
        ComplianceConfig config = new ComplianceConfig();
        config.setMimeTypes(expectedMimeTypes);

        FileFactModel fileModel = DocumentUtils.getFileFactModel(metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY),
                uploadedFile.length);

        /* Document Compliance Check */
        ComplianceData results = new ComplianceData();

        // Check meta data
        ComplianceModel compModel = documentComplianceServiceClient
                .getMetadata(metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY), uploadedFile, config);

        if (!compModel.isCorrectMimeType()) {
            try {
                DocumentFactModel model = new DocumentFactModel();
                model.setPropPrepId(propPrepId);
                model.setPropRevId(revisionId);
                model.setMimeType(compModel.getMimeType());
                model.setCorrectMimeType(compModel.isCorrectMimeType());
                model.setTargetMimeType(ComplianceModel.MIME_TYPE_PDF);
                model.setFile(fileModel);
                model.setSectionName(section.getName());
                List<PSMMessage> msgs = new ArrayList<>();
                for (gov.nsf.psm.factmodel.PSMMessage cvMsg : complianceValidationServiceClient
                        .getDocumentUploadComplianceCheckFindings(model)) {
                    PSMMessage msg = new PSMMessage();
                    msg.setDescription(cvMsg.getDescription());
                    msg.setId(cvMsg.getId());
                    msg.setType(PSMMessageType.getMessageType(cvMsg.getType().getCode()));
                    LOGGER.info("saveUploadedSeniorPersonnelDocument adding PCV msg to persist :: " + msg);
                    msgs.add(msg);
                }
                results.setCompliant(false);
                messages.addMessagesList(msgs);
            } catch (CommonUtilException e) {
                throw new CommonUtilException("Error encountered while saving senior personnel document", e);
            }
        } else {
            ComplianceData data = DocumentUtils.checkPDFDimensions(compModel.getDocModel());
            results.setMessages(new ArrayList<>());
            results.getMessages().addAll(data.getMessages());
            messages.addMessagesList(results.getMessages());
        }

        // If file can be processed, then check compliance
        if (results.isCompliant()) {
            compModel = documentComplianceServiceClient
                    .getComplianceModel(metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY), uploadedFile, config);
            Personnel personnel = proposalDataServiceClient.getPersonnel(propPrepId, revisionId, propPersId);

            try {
                DocumentFactModel model = compModel.getDocModel().getDocFactModel();
                model.setPropPrepId(propPrepId);
                model.setPropRevId(revisionId);
                model.setMimeType(compModel.getMimeType());
                model.setCorrectMimeType(compModel.isCorrectMimeType());
                model.setTargetMimeType(ComplianceModel.MIME_TYPE_PDF);
                model.setSectionName(section.getName());
                model.setFile(fileModel);
                model.setPersonnelFirstName(personnel.getFirstName());
                model.setPersonnelLastName(personnel.getLastName());
                List<PSMMessage> msgs = new ArrayList<>();
                for (gov.nsf.psm.factmodel.PSMMessage cvMsg : complianceValidationServiceClient
                        .getDocumentUploadComplianceCheckFindings(model)) {
                    PSMMessage msg = new PSMMessage();
                    msg.setDescription(cvMsg.getDescription());
                    msg.setId(cvMsg.getId());
                    msg.setType(PSMMessageType.getMessageType(cvMsg.getType().getCode()));
                    LOGGER.info("saveUploadedSeniorPersonnelDocument adding PCV msg to persist (2):: " + msg);
                    msgs.add(msg);
                }
                results.setMessages(msgs);
                results.setCompliant(!PropMgtUtil.hasErrorMessages(msgs));
            } catch (CommonUtilException e) {
                throw new CommonUtilException("Error encountered checking compliance for senior personnel document", e);
            }
            messages.addMessagesList(results.getMessages());
            ComplianceData data = DocumentUtils.checkPDFDimensions(compModel.getDocModel());
            messages.addMessagesList(data.getMessages());
            if (results.isCompliant()) {
                results.setCompliant(!PropMgtUtil.hasErrorMessages(data.getMessages()));
            }
        }

        // Save Warning/Error messages to DB.
        LOGGER.debug("PSMMessages list size:: " + messages.getPsmMessages().size());
        uploadableSection.setPropMessages(messages.getPsmMessages());

        // If file is compliant, upload
        if (results.isCompliant()) {

            /* Upload Section to File Storage */
            String fileKey = propFileUtil.constructPSMFileKeyForPersonnelDocument(propPrepId, revisionId,
                    section.getCamelCaseName(), propPersId, metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY));
            UploadFileResponse fsResp = fileStorageServiceClient.uploadFile(fileKey, uploadedFile);

            /* Save Section in DB */
            if (fsResp != null && fsResp.getUploadSuccessful()) {

                LOGGER.debug(fsResp.toString());
                uploadableSection.setFilePath(fsResp.getFilePath());
                uploadableSection.setOrigFileName(metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY));
                uploadableSection.setPageCount((int) compModel.getDocModel().getNoOfPages());
                PropMgtUtil.setAuditFields(uploadableSection, metaData.get(PropMgtUtil.NSFID));
                Map<String, Object> metaDataObj = new HashMap<String, Object>();
                LOGGER.debug(uploadableSection.toString());
                SectionResponse secResp = proposalDataServiceClient.saveSeniorPersonnelDocuments(uploadableSection,
                        propPrepId, revisionId, propPersId, section, metaDataObj);

                addMessagesForUploadSectionResponse(secResp, messages);

            } else {
                messages.addMessage(PropMgtMessagesEnum.PM_E_002.getMessage());
            }
        }

        return messages;
    }

    private void addMessagesForUploadSectionResponse(SectionResponse secResp, PSMMessages messages) {
        if (!StringUtils.isEmpty(secResp))
            LOGGER.debug("SecResp:", secResp);
        if (secResp != null && secResp.getSaveStatus()) {
            messages.addMessage(PropMgtMessagesEnum.PM_I_001.getMessage());
        } else {
            messages.addMessage(PropMgtMessagesEnum.PM_E_002.getMessage());
        }
    }

    @Override
    public ProposalSection getSectionData(String propPrepId, String propRevId, Section section)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.getSectionData()");
        LOGGER.debug("ProposalManagementServiceImpl.getSectionData() :: " + propPrepId + "  " + propRevId + " "
                + section.toString());

        switch (section) {
        case PROJ_SUMM:
            return proposalDataServiceClient.getProjectSummary(propPrepId, propRevId);
        case REF_CITED:
            return proposalDataServiceClient.getReferencesCited(propPrepId, propRevId);
        case FER:
            return proposalDataServiceClient.getFacilitiesEquipment(propPrepId, propRevId);
        case PROJ_DESC:
            return proposalDataServiceClient.getProjectDescription(propPrepId, propRevId);
        case BUDGET_JUST:
            return proposalDataServiceClient.getBudgetJustification(propPrepId, propRevId);
        case SRL:
            return proposalDataServiceClient.getSuggestedReviewers(propPrepId, propRevId);
        case RNI:
            return proposalDataServiceClient.getReviewersNotInclude(propPrepId, propRevId);
        case OPBIO:
            return proposalDataServiceClient.getOthrPersBioInfo(propPrepId, propRevId);
        case DMP:
            return proposalDataServiceClient.getDataManagementPlan(propPrepId, propRevId);
        case PMP:
            return proposalDataServiceClient.getPostDocMentoringPlan(propPrepId, propRevId);
        case OSD:
            return proposalDataServiceClient.getOtherSuppDocs(propPrepId, propRevId);
        case BUDI:
            return proposalDataServiceClient.getBudgetImpact(propPrepId, propRevId);
        default:
            return new ProposalSection();
        }

    }

    @Override
    public ProposalSection getCOASectionData(String propRevId, String propPersId) throws CommonUtilException {
        UploadableProposalSection coaPayload = new UploadableProposalSection();
        COA coa = null;
        try {
            coa = proposalDataServiceClient.getProposalCOA(Long.valueOf(propRevId), Long.valueOf(propPersId));
        } catch (HttpServerErrorException e) {
            throw new CommonUtilException(e);
        }
        if (coa != null && coa.getCoaPdfFileName() != null && coa.getCoaPdfFilePath() != null) {
            coaPayload.setOrigFileName(coa.getCoaPdfFileName());
            coaPayload.setFilePath(coa.getCoaPdfFilePath());
        }
        return coaPayload;
    }

    @Override
    public Object getSeniorPersonnelSectionData(String propPrepId, String propRevId, String propPersId, Section section)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.getSeniorPersonnelSectionData()");

        switch (section) {
        case BIOSKETCH:
            return proposalDataServiceClient.getBiographicalSketch(propPrepId, propRevId, propPersId);
        case CURR_PEND_SUPP:
            return proposalDataServiceClient.getCurrentAndPendingSupport(propPrepId, propRevId, propPersId);
        default:
            return new ProposalSection();
        }

    }

    @Override
    public ByteArrayOutputStream getSectionFile(String propPrepId, String revisionId, Section section)
            throws CommonUtilException {
        Object sectionData = getSectionData(propPrepId, revisionId, section);
        if (sectionData instanceof UploadableProposalSection) {
            String fileKey = ((UploadableProposalSection) sectionData).getFilePath();
            if (fileKey == null) {
                throw new CommonUtilException(PropMgtMessagesEnum.PM_E_005.getMessage().getDescription());
            }
            GetFileResponse getFileResponse = fileStorageServiceClient.getFile(fileKey);

            if (getFileResponse.getGetSuccessful()) {
                byte[] getFile = getFileResponse.getFile();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(getFile.length);
                byteArrayOutputStream.write(getFile, 0, getFile.length);
                return byteArrayOutputStream;
            }
        }
        return null;
    }

    @Override
    public ByteArrayOutputStream getSeniorPersonnelSectionFile(String propPrepId, String revisionId, String propPersId,
            Section section) throws CommonUtilException {
        Object sectionData = getSeniorPersonnelSectionData(propPrepId, revisionId, propPersId, section);
        if (sectionData instanceof UploadableProposalSection) {
            String fileKey = ((UploadableProposalSection) sectionData).getFilePath();
            LOGGER.debug("fileKey :: " + fileKey);
            GetFileResponse getFileResponse = fileStorageServiceClient.getFile(fileKey);

            if (getFileResponse.getGetSuccessful()) {
                byte[] getFile = getFileResponse.getFile();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(getFile.length);
                byteArrayOutputStream.write(getFile, 0, getFile.length);
                return byteArrayOutputStream;
            }
        }

        return null;
    }

    @Override
    public PSMMessages deleteSectionFile(String propPrepId, String revisionId, Section section, String nsfId)
            throws CommonUtilException {
        ProposalSection sectionData = getSectionData(propPrepId, revisionId, section);
        PSMMessages psmMessages = new PSMMessages();
        try {
            if (sectionData != null && sectionData instanceof UploadableProposalSection) {
                String fileKey = ((UploadableProposalSection) sectionData).getFilePath();
                if (!StringUtils.isEmpty(fileKey)) {
                    SectionResponse sectionResponse = proposalDataServiceClient.deleteSectionData(propPrepId,
                            revisionId, section, nsfId);
                    DeleteFileResponse deleteFileResponse = fileStorageServiceClient.deleteFile(fileKey);
                    if (deleteFileResponse != null && sectionResponse != null) {
                        if (deleteFileResponse.getDeleteSuccessful() && sectionResponse.getSaveStatus()) {
                            psmMessages.addMessage(PropMgtMessagesEnum.PM_I_002.getMessage());
                        } else {
                            psmMessages.addMessage(PropMgtMessagesEnum.PM_E_001.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
        return psmMessages;
    }

    @Override
    public PSMMessages deleteSeniorPersonnelSectionFile(String propPrepId, String revisionId, String propPersId,
            Section section, String nsfId) throws CommonUtilException {
        Object sectionData = getSeniorPersonnelSectionData(propPrepId, revisionId, propPersId, section);
        PSMMessages psmMessages = new PSMMessages();

        if (sectionData instanceof UploadableProposalSection) {
            String fileKey = ((UploadableProposalSection) sectionData).getFilePath();
            SectionResponse sectionResponse = proposalDataServiceClient.deleteSeniorPersonnelDocuments(propPrepId,
                    revisionId, propPersId, section, nsfId);
            DeleteFileResponse deleteFileResponse = fileStorageServiceClient.deleteFile(fileKey);

            if (deleteFileResponse.getDeleteSuccessful() && sectionResponse.getSaveStatus()) {
                psmMessages.addMessage(PropMgtMessagesEnum.PM_I_002.getMessage());
            } else {
                psmMessages.addMessage(PropMgtMessagesEnum.PM_E_001.getMessage());
            }
        }
        return psmMessages;
    }

    @Override
    public SectionResponse validateInstitutionBudget(InstitutionBudget instBudget) throws CommonUtilException {
        LOGGER.info("ProposalManagementServiceImpl.validateInstitutionBudget()");
        SectionResponse response = new SectionResponse();
        InstitutionBudgetFactModel model = InstBudgetUtils.getFactModel(instBudget);
        List<gov.nsf.psm.factmodel.PSMMessage> msgs = new ArrayList<>();
        if (model != null) {
            try {
                msgs.addAll(complianceValidationServiceClient.getBudgetComplianceCheckFindings(model));
            } catch (CommonUtilException e) {
                throw new CommonUtilException("Error encountered on validating Institution Budget", e);
            }
        }

        List<PSMMessage> validationMsgs = new ArrayList<>();
        for (gov.nsf.psm.factmodel.PSMMessage msg : msgs) {
            PSMMessage message = new PSMMessage();
            message.setDescription(msg.getDescription());
            message.setId(msg.getId());
            message.setType(PSMMessageType.getMessageType(msg.getType().getCode()));
            message.setSectionCode(msg.getSectionCode());
            validationMsgs.add(message);
        }

        response.setMessages(validationMsgs);

        return response;
    }

    @Override
    public SectionResponse saveInstitutionBudget(InstitutionBudget instBudget) throws CommonUtilException {
        SectionResponse response = null;

        InstitutionBudgetFactModel model = InstBudgetUtils.getFactModel(instBudget);
        List<gov.nsf.psm.factmodel.PSMMessage> msgs = new ArrayList<>();
        if (model != null) {
            try {
                msgs.addAll(complianceValidationServiceClient.getBudgetComplianceCheckFindings(model));
            } catch (CommonUtilException e) {
                throw new CommonUtilException(e);
            }
        }

        List<PSMMessage> validationMsgs = new ArrayList<>();
        for (gov.nsf.psm.factmodel.PSMMessage msg : msgs) {
            PSMMessage message = new PSMMessage();
            message.setDescription(msg.getDescription());
            message.setId(msg.getId());
            message.setType(PSMMessageType.getMessageType(msg.getType().getCode()));
            validationMsgs.add(message);
        }

        if (!validationMsgs.isEmpty()) {
            instBudget.setPropMessages(validationMsgs);
        }

        try {
            response = proposalDataServiceClient.saveInstitutionBudget(instBudget);
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }

        response.setMessages(validationMsgs);
        executePostSaveActions(instBudget);

        return response;
    }

    private PSMMessages executePostSaveActions(InstitutionBudget instBudget) throws CommonUtilException {
        PSMMessages messages = new PSMMessages();

        // If trying to save budget w/o PostDoc Funding, delete PMP if it exists
        LOGGER.info("ProposalManagementServiceImpl.executePostSaveActions()");
        if (!budgetHasPostdocFunding(instBudget)) {
            LOGGER.debug("No Post-Doctoral Funding included on budget, deleting PMP");
            messages.addMessagesList(deleteSectionFile(instBudget.getPropPrepId(), instBudget.getPropRevId(),
                    Section.PMP, instBudget.getLastUpdatedUser()).getPsmMessages());
        }
        return messages;
    }

    @Override
    public Personnels getNsfIdDetailsFromUDS(String nsfId, String roleCode) throws CommonUtilException {
        PSMRole psmRole = new PSMRole();
        List<SeniorPersonRoleTypeLookUp> seniorPersonTypeList = cachedDataService.getSeniorPersonRoleTypeLookup();
        for (SeniorPersonRoleTypeLookUp seniorPersonRoleTypeLookup : seniorPersonTypeList) {
            if (seniorPersonRoleTypeLookup.getCode().equalsIgnoreCase(roleCode)) {
                psmRole.setCode(roleCode);
                psmRole.setDescription(seniorPersonRoleTypeLookup.getDescription());
                psmRole.setAbbreviation(seniorPersonRoleTypeLookup.getAbbreviation());
                psmRole.setUserDataServiceRole(seniorPersonRoleTypeLookup.getUserDataServiceRole());
            }
        }
        Personnels personnels = new Personnels();

        try {
            UdsGetUserDataResponse udsGetUserDataResponse = cachedDataService.getUserData(nsfId);
            personnels = PropMgtUtil.convertUdsGetUserDataResponse(udsGetUserDataResponse, psmRole);
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
        return personnels;
    }

    @Override
    public Personnels getNsfIdDetailsFromUDS(String nsfId) throws CommonUtilException {
        Personnels personnels = new Personnels();

        try {
            UdsGetUserDataResponse udsGetUserDataResponse = cachedDataService.getUserData(nsfId);
            personnels = PropMgtUtil.convertUdsGetUserDataResponse(udsGetUserDataResponse);
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
        return personnels;
    }

    @Override
    public Personnels getEmailIdDetailsFromUDS(String emailId) throws CommonUtilException {
        Personnels personnels = new Personnels();

        try {
            List<UserData> userDataList = externalServices.searchUDSbyEmailId(emailId);
            if (userDataList != null && !userDataList.isEmpty()) {
                personnels = PropMgtUtil.convertUserDataToPersonnel(userDataList);
            }
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_USER_BY_EMAIL_UDS, e);
        }

        return personnels;
    }

    @Override
    public Personnels getEmailIdDetailsFromUDS(String emailId, String roleCode) throws CommonUtilException {
        Personnels personnels = new Personnels();
        PSMRole psmRole = new PSMRole();

        List<SeniorPersonRoleTypeLookUp> seniorPersonTypeList = cachedDataService.getSeniorPersonRoleTypeLookup();
        for (SeniorPersonRoleTypeLookUp seniorPersonRoleTypeLookup : seniorPersonTypeList) {
            if (seniorPersonRoleTypeLookup.getCode().equalsIgnoreCase(roleCode)) {
                psmRole.setCode(roleCode);
                psmRole.setDescription(seniorPersonRoleTypeLookup.getDescription());
                psmRole.setAbbreviation(seniorPersonRoleTypeLookup.getAbbreviation());
                psmRole.setUserDataServiceRole(seniorPersonRoleTypeLookup.getUserDataServiceRole());
            }
        }

        try {
            List<UserData> userDataList = externalServices.searchUDSbyEmailId(emailId);
            if (userDataList != null && !userDataList.isEmpty()) {
                personnels = PropMgtUtil.convertUserDataToPersonnel(userDataList, psmRole);
            }
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_USER_BY_EMAIL_UDS, e);
        }

        return personnels;
    }

    @Override
    public List<Personnel> getPersonnels(String propPrepId, String revisionId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.getPersonnels()");
        List<Personnel> transformedPersonnels = new ArrayList<Personnel>();
        try {
            for (Personnel personnel : proposalDataServiceClient.getPersonnels(propPrepId, revisionId)) {
                transformedPersonnels.add(updatePersonnelWithInstitution(personnel));
            }
        } catch (CommonUtilException e) {
            throw new CommonUtilException(e);
        }
        return transformedPersonnels;
    }

    @Override
    public List<Personnel> getPersonnelsForValidate(String propPrepId, String revisionId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.getPersonnels()");
        List<Personnel> transformedPersonnels = new ArrayList<Personnel>();
        try {
            for (Personnel personnel : proposalDataServiceClient.getPersonnels(propPrepId, revisionId)) {
                transformedPersonnels.add(personnel);
            }
        } catch (CommonUtilException e) {
            throw new CommonUtilException(e);
        }
        return transformedPersonnels;
    }

    @Override
    public Personnel getPersonnel(String propPrepId, String revisionId, String propPersId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.getPersonnel()");
        try {
            return updatePersonnelWithInstitution(
                    proposalDataServiceClient.getPersonnel(propPrepId, revisionId, propPersId));
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }

    @Override
    public SectionResponse savePersonnel(Personnel personnel) throws CommonUtilException {
        LOGGER.info("ProposalManagementServiceImpl.savePersonnel(): propPrepId = " + personnel.getPropPrepId());
        Personnel pers = proposalDataServiceClient.savePersonnel(personnel);
        pers.setPropPrepId(personnel.getPropPrepId());
        SectionResponse sr = new SectionResponse();
        sr.setMessages(
                sendEmailMessagesPersonnel(pers, new EmailMessageType(EmailMessageType.EMAIL_MSG_TYPE_ROLE_ADD)));
        sr.setSaveStatus(pers != null);
        return sr;
    }

    @Override
    public SectionResponse deletePersonnel(String propPrepId, String revisionId, String propPersId)
            throws CommonUtilException {
        Personnel pers = proposalDataServiceClient.getPersonnel(propPrepId, revisionId, propPersId);
        pers.setPropPrepId(propPrepId);
        SectionResponse sr = proposalDataServiceClient.deletePersonnel(propPrepId, revisionId, propPersId);
        if (sr.getSaveStatus()) {
            sr.setMessages(
                    sendEmailMessagesPersonnel(pers, new EmailMessageType(EmailMessageType.EMAIL_MSG_TYPE_ROLE_REM)));
        }
        return sr;
    }

    @Override
    public SectionResponse replacePersonnel(String propPrepId, String propRevId, String oldPersId, String newPersId,
            String newRoleCode, String nsfId) throws CommonUtilException {
        LOGGER.info("ProposalManagementServiceImpl.replacePersonnel()");
        Personnel p = getPersonnel(propPrepId, propRevId, newPersId);

        Institution newinstitution = externalServices.getInstitutionById(p.getInstitution().getId());
        PersonnelParam personnel = new PersonnelParam();
        personnel.setPropPrepId(propPrepId);
        personnel.setPropRevId(propRevId);
        personnel.setOldPropPersId(oldPersId);
        personnel.setNewPropPersId(newPersId);
        personnel.setNewRoleCode(newRoleCode);
        personnel.setNewInstitution(newinstitution);
        personnel.setNsfId(nsfId);

        return proposalDataServiceClient.replacePersonnel(personnel);
    }

    @Override
    public ByteArrayOutputStream getProposalFile(String propPrepId, String revisionId) throws CommonUtilException {

        LOGGER.debug("ProposalManagementServiceImpl.getProposalFile() propPrepId : " + propPrepId + "  revisionId : "
                + revisionId);

        PdfGenerationData pdf = new PdfGenerationData();
        Map<Section, UploadableProposalSection> sectionPdMap = new LinkedHashMap<Section, UploadableProposalSection>();
        ProposalPackage prop = getProposal(propPrepId, revisionId);

        // Change the Revision Number for BREV
        if (ProposalRevisionType.BUDGET_REVISION.equalsIgnoreCase(prop.getPropPrepRevnTypeCode())) {
            prop.setRevNum(getRevNumberForRevisedBudget(propPrepId, revisionId));
        }
        pdf.setProp(prop);

        for (Section sec : getFullProposalPrintOrder()) {
            ByteArrayOutputStream baos = getSubmittedProposalPdf(null, null, sec.getCode(), propPrepId, revisionId);
            UploadableProposalSection ups = new UploadableProposalSection();
            ups.setFile(baos.toByteArray());
            sectionPdMap.put(sec, ups);
        }
        pdf.setSectionPdfMap(sectionPdMap);

        GetGeneratedDocumentResponse response = documentGenerationServiceClient.getProposalDocument(pdf);
        byte[] getProposalFile = response.getFile();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(getProposalFile.length);
        byteArrayOutputStream.write(getProposalFile, 0, getProposalFile.length);
        return byteArrayOutputStream;
    }

    private List<Section> getFullProposalPrintOrder() {
        List<Section> sectionOrderList = new ArrayList<Section>();
        sectionOrderList.add(Section.COVER_SHEET);
        sectionOrderList.add(Section.PROJ_SUMM);
        sectionOrderList.add(Section.TABLE_OF_CONTENTS);
        sectionOrderList.add(Section.PROJ_DESC);
        sectionOrderList.add(Section.REF_CITED);
        sectionOrderList.add(Section.BIOSKETCH);
        sectionOrderList.add(Section.OPBIO);
        sectionOrderList.add(Section.BUDGETS);
        sectionOrderList.add(Section.CURR_PEND_SUPP);
        sectionOrderList.add(Section.FER);
        // Other Supplementary Docs
        sectionOrderList.add(Section.DMP);
        sectionOrderList.add(Section.PMP);
        sectionOrderList.add(Section.OSD);
        // GOALI-Industrial PI Confirmation Letter
        sectionOrderList.add(Section.COA);
        // RAISE - Program
        // Deviation Authorization
        sectionOrderList.add(Section.SRL);
        sectionOrderList.add(Section.RNI);
        // Additional Single Copy Documents
        // Nature of Natural or Anthropogenic Event
        return sectionOrderList;
    }

    private List<Section> getPrintOrderForExternalView() {
        List<Section> sectionOrderList = new ArrayList<Section>();
        sectionOrderList.add(Section.COVER_SHEET);
        sectionOrderList.add(Section.PROJ_SUMM);
        sectionOrderList.add(Section.TABLE_OF_CONTENTS);
        sectionOrderList.add(Section.PROJ_DESC);
        sectionOrderList.add(Section.REF_CITED);
        sectionOrderList.add(Section.BIOSKETCH);
        sectionOrderList.add(Section.OPBIO);
        sectionOrderList.add(Section.BUDGETS);
        sectionOrderList.add(Section.CURR_PEND_SUPP);
        sectionOrderList.add(Section.FER);
        // Other Supplementary Docs
        sectionOrderList.add(Section.DMP);
        sectionOrderList.add(Section.PMP);
        sectionOrderList.add(Section.OSD);
        return sectionOrderList;
    }

    private static String formatPostalCode(String postalCode) {

        return String.valueOf(postalCode).replaceFirst("(\\d{5})(\\d+)", "$1-$2");

    }

    /**
     * Update Institution data on personnel object.
     *
     * @param personnel
     * @return
     * @throws CommonUtilException
     */
    private Personnel updatePersonnelWithInstitution(Personnel personnel) throws CommonUtilException {
        if (personnel.getInstitution().getId() != null && !personnel.getInstitution().getId().isEmpty()) {
            personnel.setInstitution(externalServices.getInstitutionById(personnel.getInstitution().getId()));
        }
        return personnel;
    }

    /**
     * This method updates the proposal title
     *
     * @param proposalHeader
     * @return sectionResponse
     * @throws CommonUtilException
     */

    @Override
    public SectionResponse updateProposal(ProposalHeader proposalHeader) throws CommonUtilException {
        return proposalDataServiceClient.updateProposal(proposalHeader);
    }

    private ProposalSectionStatus getSectionStatus(SectionStatus status, Section section, SectionStatus budgetStatus)
            throws CommonUtilException {

        try {
            ProposalSectionStatus propSectionStatus = new ProposalSectionStatus();

            // populate section data
            propSectionStatus.setCode(section.getCode());
            propSectionStatus.setName(section.getName());
            propSectionStatus.setCamelCaseName(section.getCamelCaseName());
            propSectionStatus.setEntryType(section.getEntryType().name());

            // load values from the database
            propSectionStatus.setComplianceStatus(SectionComplianceStatus.NOT_CHECKED.getStatus());
            propSectionStatus.setEnableAccess(true);

            SectionCompliance sectionCompliance = new SectionCompliance();

            // Handle unavailable docs
            if (status.getSectionExists()) {
                if (status.getEmptyDocumentCount() != null) {
                    sectionCompliance.setNoOfDocsUnavailable(status.getEmptyDocumentCount());
                }
                if (status.getMsgs() != null && !status.getMsgs().isEmpty()) {
                    sectionCompliance.setNoOfErrors(SectionStatusUtils.getErrorStatusCount(status.getMsgs()));
                    sectionCompliance.setNoOfWarnings(SectionStatusUtils.getWarningStatusCount(status.getMsgs()));
                }
                if (status.getLastUpdatedTmsp() != null) {
                    propSectionStatus.setLastUpdateDate(status.getLastUpdatedTmsp());
                    sectionCompliance.setFormChecked(true);
                }
            }

            // set whether form is required and check/set if enabled
            switch (section) {
            case PMP:
                // check if a PMP already uploaded for the proposal?
                propSectionStatus.setLastUpdateDate(status.getLastUpdatedTmsp());
                if (budgetStatus != null) {
                    boolean hasFunding = budgetStatus.getHasPostDoctoralFunding();
                    propSectionStatus.setEnableAccess(hasFunding);
                    if (!hasFunding) {
                        propSectionStatus.setLastUpdateDate(null);
                    }
                } else {
                    propSectionStatus.setLastUpdateDate(null);
                    propSectionStatus.setEnableAccess(false);
                }
                propSectionStatus.setRequired(true);
                propSectionStatus.setSectionUpdated(SectionStatusUtils.populateSectionUpdated(status));
                break;
            case SPD:
                boolean isUpdated = false;
                long unavailDocCount = status.getEmptyDocumentCount();
                isUpdated = SectionStatusUtils.populateSectionUpdated(status);
                if ((status.getSeniorPersonCount() * 3) > status.getDocumentCount()) {
                    unavailDocCount = status.getSeniorPersonCount() * 3;
                }
                if (status.getLastUpdatedTmsp() != null) {
                    propSectionStatus.setRequired(true);
                    propSectionStatus.setLastUpdateDate(status.getLastUpdatedTmsp());
                    propSectionStatus.setSectionUpdated(isUpdated);
                } else {
                    propSectionStatus.setRequired(true);
                }
                sectionCompliance.setNoOfDocsUnavailable(unavailDocCount);
                break;
            case BUDI:
                if (!ProposalRevisionType.ORIGINAL_PROPOSAL.equalsIgnoreCase(status.getRevisionType())) {
                    if (budgetStatus != null && (budgetStatus.getIsFormChecked()
                            || SectionStatusUtils.populateSectionUpdated(budgetStatus))) {
                        propSectionStatus.setRequired(true);
                        propSectionStatus.setEnableAccess(true);
                        propSectionStatus.setLastUpdateDate(status.getLastUpdatedTmsp());
                        propSectionStatus.setSectionUpdated(SectionStatusUtils.populateSectionUpdated(status));
                    } else if (status.getIsFormChecked() != null && !status.getIsFormChecked()) {
                        propSectionStatus.setRequired(false);
                        propSectionStatus.setEnableAccess(false);
                    }
                } else if (status.getIsFormChecked() != null && !status.getIsFormChecked()) {
                    propSectionStatus.setRequired(false);
                    propSectionStatus.setEnableAccess(false);
                }
                break;
            case COVER_SHEET:
                boolean updated = SectionStatusUtils.populateSectionUpdated(status);
                boolean origUpdated = SectionStatusUtils.populateSectionUpdatedOrigRevCreatedDate(status);
                if (updated || origUpdated) {
                    sectionCompliance.setFormChecked(true);
                    if (updated) {
                        propSectionStatus.setSectionUpdated(true);
                    }
                } else if (!status.getIsFormChecked()) {
                    sectionCompliance.setFormChecked(false);
                    propSectionStatus.setSectionUpdated(false);
                    propSectionStatus.setLastUpdateDate(null);
                }
                break;
            case BUDGETS:
                propSectionStatus.setSectionUpdated(status.getIsFormChecked());
                if (!status.getIsFormChecked()
                        && status.getRevisionType().equalsIgnoreCase(ProposalRevisionType.ORIGINAL_PROPOSAL)) {
                    propSectionStatus.setLastUpdateDate(null);
                    sectionCompliance.setFormChecked(false);
                }
                break;
            default:
                propSectionStatus.setRequired(true);
                if (status.getSectionExists()) {
                    propSectionStatus.setSectionUpdated(SectionStatusUtils.populateSectionUpdated(status));
                }
            }

            propSectionStatus.setSectionCompliance(sectionCompliance); // Set
                                                                       // compliance

            if (propSectionStatus.getLastUpdateDate() != null && sectionCompliance != null) {
                if ((sectionCompliance.getNoOfErrors() + sectionCompliance.getNoOfWarnings()) > 0) {
                    propSectionStatus.setComplianceStatus(SectionComplianceStatus.NONCOMPLIANT.getStatus());
                } else if (propSectionStatus.getSectionCompliance().getNoOfDocsUnavailable() < 1) {
                    propSectionStatus.setComplianceStatus(SectionComplianceStatus.COMPLIANT.getStatus());
                }
            }

            propSectionStatus.setSectionCompliance(sectionCompliance);

            return propSectionStatus;

        } catch (Exception e) {
            throw new CommonUtilException(e);
        }

    }

    private ProposalSectionStatus getPersonnelSectionStatus(String propPrepId, String revisionId, Section section,
            Date statusDate, String revTypeCode) throws CommonUtilException {

        ProposalSectionStatus propSectionStatus = new ProposalSectionStatus();

        // populate section data
        propSectionStatus.setCode(section.getCode());
        propSectionStatus.setName(section.getName());
        propSectionStatus.setCamelCaseName(section.getCamelCaseName());
        propSectionStatus.setEntryType(section.getEntryType().name());

        // load values from the database
        propSectionStatus.setComplianceStatus(SectionComplianceStatus.NOT_CHECKED.getStatus());
        propSectionStatus.setEnableAccess(true);

        // set whether form is required and check/set if enabled
        switch (section) {
        case BIOSKETCH:
            BiographicalSketch sketch = proposalDataServiceClient.getLatestBioSketch(propPrepId, revisionId);
            propSectionStatus.setSectionCompliance(
                    getSectionCompliance(propPrepId, sketch.getPerson().getPropPersId(), revisionId, section));
            propSectionStatus.setRequired(true);
            propSectionStatus.setLastUpdateDate(sketch.getLastUpdateDate());
            if (sketch.getLastUpdateDate() != null
                    && revTypeCode.equalsIgnoreCase(ProposalRevisionType.PROPOSAL_FILE_UPDATE)) {
                propSectionStatus.setSectionUpdated(sketch.getLastUpdateDate().after(statusDate));
            }
            break;
        case COA:
            COA coa = proposalDataServiceClient.getLatestProposalCOA(Long.valueOf(revisionId));
            propSectionStatus.setSectionCompliance(
                    getSectionCompliance(propPrepId, coa.getPerson().getPropPersId(), revisionId, section));
            propSectionStatus.setRequired(true);
            propSectionStatus.setLastUpdateDate(coa.getLastUpdatedTmsp());
            if (coa.getLastUpdatedTmsp() != null
                    && revTypeCode.equalsIgnoreCase(ProposalRevisionType.PROPOSAL_FILE_UPDATE)) {
                propSectionStatus.setSectionUpdated(coa.getLastUpdatedTmsp().after(statusDate));
            }
            break;
        case CURR_PEND_SUPP:
            CurrentAndPendingSupport cps = proposalDataServiceClient.getLatestCurrAndPendSupport(propPrepId,
                    revisionId);
            propSectionStatus.setSectionCompliance(
                    getSectionCompliance(propPrepId, cps.getPerson().getPropPersId(), revisionId, section));
            propSectionStatus.setRequired(true);
            propSectionStatus.setLastUpdateDate(cps.getLastUpdatedTmsp());
            if (cps.getLastUpdatedTmsp() != null
                    && revTypeCode.equalsIgnoreCase(ProposalRevisionType.PROPOSAL_FILE_UPDATE)) {
                propSectionStatus.setSectionUpdated(cps.getLastUpdatedTmsp().after(statusDate));
            }
            break;
        default:
            propSectionStatus.setRequired(false);
        }

        SectionCompliance sectionCompliance = propSectionStatus.getSectionCompliance();
        if (propSectionStatus.getLastUpdateDate() != null && sectionCompliance != null) {
            if (sectionCompliance.getNoOfErrors() + sectionCompliance.getNoOfWarnings()
                    + sectionCompliance.getNoOfDocsUnavailable() > 0) {
                propSectionStatus.setComplianceStatus(SectionComplianceStatus.NONCOMPLIANT.getStatus());
            } else {
                propSectionStatus.setComplianceStatus(SectionComplianceStatus.COMPLIANT.getStatus());
            }
        }

        return propSectionStatus;
    }

    /**
     * This method returns a proposal section with associated status information
     *
     * @param propPrepId
     * @param revisionId
     * @return Set<Section>
     */
    @Override
    public Map<Section, ProposalSectionStatus> getSingleSectionStatus(String propPrepId, String revisionId,
            String instId, String sectionCode) throws CommonUtilException {
        Section section = Section.getSection(sectionCode);
        Map<Section, ProposalSectionStatus> sectionMap = new HashMap<Section, ProposalSectionStatus>();
        Map<Section, SectionStatus> statuses = proposalDataServiceClient
                .getLatestSectionStatusData(propPrepId, revisionId, sectionCode).getSections();
        sectionMap.put(section, getSectionStatus(statuses.get(section), section, statuses.get(Section.BUDGETS)));
        return sectionMap;
    }

    /**
     * This method returns a proposal section with associated status information
     *
     * @param propPrepId
     * @param revisionId
     * @return Set<Section>
     */
    @Override
    public Map<Section, ProposalSectionStatus> getSinglePersonnelSectionStatus(String propPrepId, String revisionId,
            String propPersId, String sectionCode) throws CommonUtilException {
        Section section = Section.getSection(sectionCode);
        ProposalPackage pkg = proposalDataServiceClient.getProposalPrep(propPrepId, revisionId);
        Date propStatusDate = pkg.getCreateDate();
        Map<Section, ProposalSectionStatus> sectionMap = new HashMap<Section, ProposalSectionStatus>();
        sectionMap.put(section, getPersonnelSectionStatus(propPrepId, revisionId, section, propStatusDate,
                pkg.getPropPrepRevnTypeCode()));
        return sectionMap;
    }

    /**
     * This method returns a list of proposal sections with associated status
     * information
     *
     * @param propPrepId
     * @param revisionId
     * @return Set<Section>
     */
    @Override
    public Map<Section, ProposalSectionStatus> getAllSectionStatuses(String propPrepId, String revisionId)
            throws CommonUtilException {
        Map<Section, ProposalSectionStatus> statusMap = new HashMap<Section, ProposalSectionStatus>();
        Map<Section, SectionStatus> statuses = proposalDataServiceClient.getAllSectionStatusData(propPrepId, revisionId)
                .getSections();
        for (Section section : statuses.keySet()) {
            statusMap.put(section, getSectionStatus(statuses.get(section), section, statuses.get(Section.BUDGETS)));
        }
        return statusMap;
    }

    /**
     * This method returns a list of proposal sections with associated status
     * information
     *
     * @param propPrepId
     * @param revisionId
     * @return Set<Section>
     */
    @Override
    public Map<Section, ProposalSectionStatus> getAllPersonnelSectionStatuses(String propPrepId, String revisionId)
            throws CommonUtilException {
        Map<Section, ProposalSectionStatus> sectionMap = new HashMap<Section, ProposalSectionStatus>();
        ProposalPackage pkg = proposalDataServiceClient.getProposalPrep(propPrepId, revisionId);
        Date propCreateDate = pkg.getCreateDate();
        for (Section section : Section.getPersonnelSectionList()) {
            sectionMap.put(section, getPersonnelSectionStatus(propPrepId, revisionId, section, propCreateDate,
                    pkg.getPropPrepRevnTypeCode()));
        }
        return sectionMap;
    }

    private boolean budgetHasPostdocFunding(InstitutionBudget propBudget) throws CommonUtilException {
        // check if any of the budget periods contain postdoctorate funding
        if (CollectionUtils.isNotEmpty(propBudget.getBudgetRecordList())) {
            return propBudget.getBudgetRecordList().stream()
                    .anyMatch(budgetRecord -> budgetRecord.getOtherPersonnelList() != null && budgetRecord
                            .getOtherPersonnelList().stream()
                            .filter(othPersons -> othPersons.getOtherPersonTypeCode() != null && othPersons
                                    .getOtherPersonTypeCode().equals(OtherPersonnelCost.CODE_STUDENTS_POST_DOCTORAL))
                            .anyMatch(othPerson -> othPerson.getOtherPersonDollarAmount() != null
                                    && othPerson.getOtherPersonDollarAmount().doubleValue() > 0.0));
        }
        return false;
    }

    @Override
    public CoverSheet getCoverSheetForValidate(String propPrepId, String propRevId) throws CommonUtilException {

        LOGGER.debug("ProposalManagementServiceImpl.ValaidateCoverSheet");
        CoverSheet cv = proposalDataServiceClient.getCoverSheet(propPrepId, propRevId);
        cv.setPropPrepId(propPrepId);
        cv.setPropRevId(propRevId);

        // get Awardee organization Address
        String awdOrganizationId = proposalDataServiceClient.getPrimaryAwardeeOrganizationId(propPrepId, propRevId)
                .getAwdOrgId();
        if (!StringUtils.isEmpty(awdOrganizationId)) {
            Institution awdOrganization = externalServices.getInstitutionById(awdOrganizationId);
            if (awdOrganization != null) {
                if (awdOrganization.getAddress().getPostalCode() != null) {
                    awdOrganization.getAddress()
                            .setPostalCode(formatPostalCode(awdOrganization.getAddress().getPostalCode()));
                }
                cv.setAwdOrganization(awdOrganization);
                if (cv.getPpop() == null) {
                    LOGGER.debug(
                            "ProposalManagementServiceImpl.getCoverSheet..Populating PPOP object based on Awardee ID");
                    PrimaryPlaceOfPerformance ppop = populatePpop(awdOrganization);
                    cv.setPpop(ppop);

                }
            }
        }

        return cv;

    }

    @Override
    public CoverSheet getCoverSheet(String propPrepId, String propRevId) throws CommonUtilException {

        LOGGER.debug("ProposalManagementServiceImpl.getCoverSheet");
        CoverSheet cv = proposalDataServiceClient.getCoverSheet(propPrepId, propRevId);
        cv.setPropPrepId(propPrepId);
        cv.setPropRevId(propRevId);
        cv.setMultiOrg(Constants.MULTI_ORG_NO);

        // get Mutlti Org Flag
        List<Institution> instList = getUserOrganizations(propPrepId, propRevId);
        if (instList.size() > 1) {
            cv.setMultiOrg(Constants.MULTI_ORG_YES);
        }

        // get Awardee organization Address
        String awdOrganizationId = proposalDataServiceClient.getPrimaryAwardeeOrganizationId(propPrepId, propRevId)
                .getAwdOrgId();
        Institution awdOrganization = null;
        if (!StringUtils.isEmpty(awdOrganizationId)) {
            awdOrganization = externalServices.getInstitutionById(awdOrganizationId);
            if (awdOrganization != null) {
                if (awdOrganization.getAddress().getPostalCode() != null) {
                    awdOrganization.getAddress()
                            .setPostalCode(formatPostalCode(awdOrganization.getAddress().getPostalCode()));
                }
                cv.setAwdOrganization(awdOrganization);

                if (cv.getPpop() == null) {
                    LOGGER.debug(
                            "ProposalManagementServiceImpl.getCoverSheet..Populating PPOP object based on Awardee ID");
                    PrimaryPlaceOfPerformance ppop = populatePpop(awdOrganization);
                    cv.setPpop(ppop);

                }
            }

        }

        return cv;

    }

    private PrimaryPlaceOfPerformance populatePpop(Institution awdOrganization) {
        PrimaryPlaceOfPerformance ppop = new PrimaryPlaceOfPerformance();
        ppop.setStreetAddress(awdOrganization.getAddress().getStreetAddress());
        ppop.setStreetAddress2(awdOrganization.getAddress().getStreetAddress2());
        ppop.setCityName(awdOrganization.getAddress().getCityName());
        ppop.setCountryCode(awdOrganization.getAddress().getCountryCode());
        ppop.setOrganizationName(awdOrganization.getOrganizationName());
        ppop.setPostalCode(formatPostalCode(awdOrganization.getAddress().getPostalCode()));
        ppop.setStateCode(awdOrganization.getAddress().getStateCode());

        return ppop;

    }

    @Override
    public SectionResponse saveCoverSheet(CoverSheet coverSheet) throws CommonUtilException {
        CoverSheetFactModel model = CoverSheetFactModelUtility.getCoverSheetFactModel(coverSheet);

        SectionResponse response = null;
        String countryCode = "";

        List<gov.nsf.psm.factmodel.PSMMessage> msgs = new ArrayList<>();
        if (model != null) {
            try {

                countryCode = coverSheet.getPpop().getCountryCode();
                if (!StringUtils.isEmpty(countryCode) && (Constants.USA).equalsIgnoreCase(countryCode)) {
                    if (!StringUtils.isEmpty(coverSheet.getPpop().getStateCode())
                            && !StringUtils.isEmpty(coverSheet.getPpop().getPostalCode())) {
                        SectionResponse sectionResponse = solicitationDataServiceclient.isPostalCodeValid(
                                coverSheet.getPpop().getStateCode(), coverSheet.getPpop().getPostalCode());

                        model.setIsPostalCodeValid(sectionResponse.getIsPostalCodeValid());

                    }
                } else
                    model.setIsPostalCodeValid(false);

            } catch (CommonUtilException e) {
                throw new CommonUtilException(Constants.GET_POSTAL_CODE_VALIDATION_ERROR, e);
            }

            try {

                msgs.addAll(complianceValidationServiceClient.getCoverSheetComplianceCheckFindings(model));
            } catch (CommonUtilException e) {
                throw new CommonUtilException(e);
            }
        }
        LOGGER.debug("List<gov.nsf.psm.factmodel.PSMMessage> msgs :: " + msgs);
        List<PSMMessage> validationMsgs = new ArrayList<PSMMessage>();
        for (gov.nsf.psm.factmodel.PSMMessage msg : msgs) {

            LOGGER.debug("PSM Message" + msg.getDescription() + msg.getId());
            PSMMessage message = new PSMMessage();
            message.setDescription(msg.getDescription());
            message.setId(msg.getId());
            message.setType(PSMMessageType.getMessageType(msg.getType().getCode()));
            validationMsgs.add(message);
        }

        // Persist PSMMessages
        coverSheet.setPsmMsgList(validationMsgs);
        LOGGER.debug("After adding getCoverSheetComplianceCheckFinding Msgs to coverSheet :: " + coverSheet);

        /* If the country code is null or emtpty, do not save to the database */

        if (!StringUtils.isEmpty(countryCode)) {
            response = proposalDataServiceClient.saveCoverSheet(coverSheet);
        } else {
            response = new SectionResponse();
            response.setSaveStatus(false);

        }

        response.setMessages(validationMsgs);

        return response;
    }

    @Override
    public SectionResponse changeAwardeeOrganization(String propPrepId, String propRevId, String coverSheetId,
            Institution institution) throws CommonUtilException {
        return proposalDataServiceClient.changeAwardeeOrganization(propPrepId, propRevId, coverSheetId, institution);
    }

    @Override
    public List<Institution> getUserOrganizations(String propPrepId, String propRevId) throws CommonUtilException {

        String nsfId = null;
        CoverSheet cv = proposalDataServiceClient.getCoverSheet(propPrepId, propRevId);
        List<PiCoPiInformation> piCopiList = cv.getPiCopiList();

        for (PiCoPiInformation piCoPi : piCopiList) {
            if (piCoPi != null && piCoPi.getPersonRoleCode().equalsIgnoreCase(Constants.PI_ROLE_CODE)) {
                nsfId = piCoPi.getNsfId();
            }
        }

        // TODO: consider moving out as a utility on User Details Service
        List<Institution> instList = new ArrayList<Institution>();
        try {
            UdsGetUserDataResponse udsGetUserDataResponse = cachedDataService.getUserData(nsfId);
            for (UdsAgencyIdentity udsAgencyIdentity : udsGetUserDataResponse.getUserData().getAgencyIdentities()) {

                for (String udsRole : udsAgencyIdentity.getRgovRole()) {
                    if (Constants.PI_ROLE_ABRV.equalsIgnoreCase(udsRole)) {
                        Institution inst = new Institution();
                        inst = externalServices
                                .getInstitutionById(udsAgencyIdentity.getInstitutionIdentifier().getInstitID());
                        instList.add(inst);
                    }
                }

            }
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
        return instList;

    }

    @Override
    public List<State> getStates() throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.getStates()");
        try {
            return solicitationDataServiceclient.getStates();
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_ALL_STATES_ERROR, e);
        }
    }

    @Override
    public SectionResponse validateCoversheet(CoverSheet coverSheet) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.validateCoversheet()");
        CoverSheetFactModel model = CoverSheetFactModelUtility.getCoverSheetFactModel(coverSheet);
        LOGGER.debug("CoverShetFactModel after conversion -" + model.toString());

        SectionResponse response = new SectionResponse();
        String countryCode = "";

        List<gov.nsf.psm.factmodel.PSMMessage> msgs = new ArrayList<>();
        try {
            countryCode = coverSheet.getPpop().getCountryCode();
            if (!StringUtils.isEmpty(countryCode) && (Constants.USA).equalsIgnoreCase(countryCode)) {
                if (!StringUtils.isEmpty(coverSheet.getPpop().getStateCode())
                        && !StringUtils.isEmpty(coverSheet.getPpop().getPostalCode())) {
                    SectionResponse sectionResponse = solicitationDataServiceclient.isPostalCodeValid(
                            coverSheet.getPpop().getStateCode(), coverSheet.getPpop().getPostalCode());

                    model.setIsPostalCodeValid(sectionResponse.getIsPostalCodeValid());

                }
            } else {
                model.setIsPostalCodeValid(false);
            }
        } catch (CommonUtilException e) {
            throw new CommonUtilException(Constants.GET_POSTAL_CODE_VALIDATION_ERROR, e);
        }
        try {
            msgs.addAll(complianceValidationServiceClient.getCoverSheetComplianceCheckFindings(model));
        } catch (CommonUtilException e) {
            throw new CommonUtilException(e);
        }
        List<PSMMessage> validationMsgs = new ArrayList<>();
        for (gov.nsf.psm.factmodel.PSMMessage msg : msgs) {

            PSMMessage message = new PSMMessage();
            message.setDescription(msg.getDescription());
            message.setId(msg.getId());
            message.setType(PSMMessageType.getMessageType(msg.getType().getCode()));
            message.setSectionCode(msg.getSectionCode());
            validationMsgs.add(message);

        }

        response.setMessages(validationMsgs);
        return response;
    }

    @Override
    public ProposalPackage getProposalAccess(String propPrepId, String propRevId) throws CommonUtilException {
        return proposalDataServiceClient.getProposalAccess(propPrepId, propRevId);
    }

    @Override
    public SectionResponse setProposalAccess(ProposalPackage proposalPackage) throws CommonUtilException {
        return proposalDataServiceClient.setProposalAccess(proposalPackage);
    }

    @Override
    public List<ProposalQueryResult> getProposals(String nsfId, Boolean submitted) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.getProposals()");

        List<ProposalQueryResult> results = new ArrayList<>();
        List<ProposalQueryParams> paramList = new ArrayList<>();
        List<Proposal> propResults = new ArrayList<>();

        try {
            LOGGER.debug("ProposalManagementServiceImpl.getProposals()::NSF ID: " + nsfId);
            User user = userDatailsService.getUDSUserByNSFId(nsfId);
            if (user != null) {
                List<Personnel> proposalPersonsNonSPOAOR = new ArrayList<>();
                Personnel nsfPers = new Personnel();
                nsfPers.setEmail(user.getEmailId());
                nsfPers.setFirstName(user.getFirstName());
                nsfPers.setLastName(user.getLastName());
                nsfPers.setNsfId(nsfId);
                proposalPersonsNonSPOAOR.add(nsfPers);
                ProposalQueryParams nsfParams = new ProposalQueryParams();
                List<PSMRole> nsfRolesToExclude = new ArrayList<>();

                // Excluded roles
                PSMRole nsfPSMRole = new PSMRole();
                nsfPSMRole.setCode(PSMRole.ROLE_OSP);
                nsfRolesToExclude.add(nsfPSMRole);
                nsfParams.setRolesToExclude(nsfRolesToExclude);

                List<ProposalRevisionType> nsfPropRevnTypes = new ArrayList<>();
                List<ProposalStatus> nsfProposalStatuses = new ArrayList<>();

                if (submitted == null || !submitted) { // In-progress query

                    // Included statuses
                    ProposalStatus statusOne = new ProposalStatus();
                    statusOne.setStatusCode(ProposalStatus.NOT_FORWARDED_TO_SPO);
                    ProposalStatus statusTwo = new ProposalStatus();
                    statusTwo.setStatusCode(ProposalStatus.VIEW_ONLY_SPO_AOR);
                    ProposalStatus statusThree = new ProposalStatus();
                    statusThree.setStatusCode(ProposalStatus.VIEW_EDIT_SPO);
                    ProposalStatus statusFour = new ProposalStatus();
                    statusFour.setStatusCode(ProposalStatus.RETURN_TO_PI);
                    ProposalStatus statusFive = new ProposalStatus();
                    statusFive.setStatusCode(ProposalStatus.SUBMITTED_ACCESS_FOR_AOR);
                    nsfProposalStatuses.add(statusOne);
                    nsfProposalStatuses.add(statusTwo);
                    nsfProposalStatuses.add(statusThree);
                    nsfProposalStatuses.add(statusFour);
                    nsfProposalStatuses.add(statusFive);

                    // Included revision types
                    ProposalRevisionType typeOne = new ProposalRevisionType();
                    typeOne.setType(ProposalRevisionType.ORIGINAL_PROPOSAL);
                    nsfPropRevnTypes.add(typeOne);
                    nsfParams.setPropRevnTypes(nsfPropRevnTypes);

                    nsfParams.setIsSubmitted(false); // Set type of query

                    LOGGER.debug("ProposalManagementServiceImpl.getProposals()::Query type: IN-PROGRESS");

                } else { // Submitted query

                    // Included statuses
                    ProposalStatus statusOne = new ProposalStatus();
                    statusOne.setStatusCode(ProposalStatus.SUBMITTED_TO_NSF);
                    nsfProposalStatuses.add(statusOne);

                    // Included revision types
                    ProposalRevisionType typeOne = new ProposalRevisionType();
                    typeOne.setType(ProposalRevisionType.ORIGINAL_PROPOSAL);
                    nsfPropRevnTypes.add(typeOne);
                    ProposalRevisionType typeTwo = new ProposalRevisionType();
                    typeTwo.setType(ProposalRevisionType.PROPOSAL_FILE_UPDATE);
                    nsfPropRevnTypes.add(typeTwo);
                    ProposalRevisionType typeThree = new ProposalRevisionType();
                    typeThree.setType(ProposalRevisionType.BUDGET_REVISION);
                    nsfPropRevnTypes.add(typeThree);
                    ProposalRevisionType typeFour = new ProposalRevisionType();
                    typeFour.setType(ProposalRevisionType.BUDGET_UPDATE);
                    nsfPropRevnTypes.add(typeFour);
                    ProposalRevisionType typeFive = new ProposalRevisionType();
                    typeFive.setType(ProposalRevisionType.PROPOSAL_UPDATE);
                    nsfPropRevnTypes.add(typeFive);
                    nsfParams.setPropRevnTypes(nsfPropRevnTypes);

                    nsfParams.setIsSubmitted(true); // Set type of query

                    LOGGER.debug("ProposalManagementServiceImpl.getProposals()::Query type: SUBMITTED");
                }
                nsfParams.setPropStatus(nsfProposalStatuses);
                nsfParams.setPersonnel(proposalPersonsNonSPOAOR);
                nsfParams.setIsSPOAOR(false);
                paramList.add(nsfParams);
                LOGGER.debug("ProposalManagementServiceImpl.getProposals()::Query is by NSF ID");

                for (InstitutionRole role : user.getInstitutionRoles()) {
                    boolean isPI = false;
                    boolean isSPOAOR = false;
                    boolean isOAU = false;
                    List<Personnel> proposalPersonsSPOAOR = new ArrayList<>();
                    Personnel pers = new Personnel();
                    if (role.getRoles() != null) {
                        for (PSMRole psmRole : role.getRoles()) {

                            LOGGER.debug("ProposalManagementServiceImpl.getProposals()::User has role: "
                                    + psmRole.getAbbreviation());

                            switch (psmRole.getAbbreviation().trim()) {
                            case PSMRole.AOR_ROLE_ABRV:
                                isSPOAOR = true;
                                break;
                            case PSMRole.SPO_ROLE_ABRV:
                                isSPOAOR = true;
                                break;
                            case PSMRole.PI_ROLE_ABRV:
                                isPI = true;
                                break;
                            case PSMRole.COPI_ROLE_ABRV:
                                isPI = true;
                                break;
                            case PSMRole.OAU_ROLE_ABRV:
                                isOAU = true;
                                break;
                            default:
                                isOAU = true;
                                break;
                            }
                        }
                    } else {
                        LOGGER.debug(
                                "ProposalManagementServiceImpl.getProposals()::No roles where returned from the UDS for this user");
                    }

                    if (isSPOAOR && role.getInstitution() != null) { // Only
                                                                     // consider
                                                                     // institution
                                                                     // and role
                        // code
                        // for SPO/AOR
                        pers.setInstitution(role.getInstitution());
                        pers.setNsfId(nsfId);
                        if (!proposalPersonsSPOAOR.contains(pers)) {
                            proposalPersonsSPOAOR.add(pers);
                        }
                    }

                    LOGGER.debug("ProposalManagementServiceImpl.getProposals()::Institution ID: "
                            + role.getInstitution().getId());

                    if (isSPOAOR) { // Non-PI Query
                        ProposalQueryParams params = new ProposalQueryParams();
                        List<ProposalRevisionType> propRevnTypes = new ArrayList<>();
                        List<ProposalStatus> proposalStatuses = new ArrayList<>();
                        List<ProposalStatus> excludedStatuses = new ArrayList<>();
                        List<PSMRole> rolesToExclude = new ArrayList<>();

                        params.setCheckPermissions(true); // Make sure AOR/SPO
                                                          // permissions are
                                                          // enabled

                        // Excluded roles
                        PSMRole psmRole = new PSMRole();
                        psmRole.setCode(PSMRole.ROLE_OSP);
                        rolesToExclude.add(psmRole);
                        params.setRolesToExclude(rolesToExclude);

                        if (submitted == null || !submitted) { // In-progress
                                                               // query

                            // Included statuses
                            ProposalStatus statusOne = new ProposalStatus();
                            statusOne.setStatusCode(ProposalStatus.VIEW_ONLY_SPO_AOR);
                            ProposalStatus statusTwo = new ProposalStatus();
                            statusTwo.setStatusCode(ProposalStatus.VIEW_EDIT_SPO);
                            ProposalStatus statusThree = new ProposalStatus();
                            statusThree.setStatusCode(ProposalStatus.SUBMITTED_ACCESS_FOR_AOR);
                            proposalStatuses.add(statusOne);
                            proposalStatuses.add(statusTwo);
                            proposalStatuses.add(statusThree);
                            if (isSPOAOR && isPI || isSPOAOR && isOAU) {
                                ProposalStatus statusFour = new ProposalStatus();
                                statusFour.setStatusCode(ProposalStatus.NOT_FORWARDED_TO_SPO);
                                ProposalStatus statusFive = new ProposalStatus();
                                statusFive.setStatusCode(ProposalStatus.RETURN_TO_PI);
                                proposalStatuses.add(statusFour);
                                proposalStatuses.add(statusFive);
                            }

                            // Excluded statuses
                            ProposalStatus excludedStatusOne = new ProposalStatus();
                            excludedStatusOne.setStatusCode(ProposalStatus.NOT_FORWARDED_TO_SPO);
                            ProposalStatus excludedStatusTwo = new ProposalStatus();
                            excludedStatusTwo.setStatusCode(ProposalStatus.RETURN_TO_PI);
                            excludedStatuses.add(excludedStatusOne);
                            excludedStatuses.add(excludedStatusTwo);
                            params.setStatusesToExcludeAORSPO(excludedStatuses);

                            // Included revision types
                            ProposalRevisionType typeOne = new ProposalRevisionType();
                            typeOne.setType(ProposalRevisionType.ORIGINAL_PROPOSAL);
                            propRevnTypes.add(typeOne);
                            params.setPropRevnTypes(propRevnTypes);

                            params.setIsSubmitted(false); // Set type of query

                            LOGGER.debug("ProposalManagementServiceImpl.getProposals()::Query type: IN-PROGRESS");

                        } else { // Submitted query

                            // Included statuses
                            ProposalStatus statusOne = new ProposalStatus();
                            statusOne.setStatusCode(ProposalStatus.SUBMITTED_TO_NSF);
                            proposalStatuses.add(statusOne);

                            // Excluded statuses
                            ProposalStatus excludedStatusOne = new ProposalStatus();
                            excludedStatusOne.setStatusCode(ProposalStatus.PFU_NOT_FORWARDED_TO_SPO);
                            excludedStatuses.add(excludedStatusOne);
                            ProposalStatus excludedStatusTwo = new ProposalStatus();
                            excludedStatusTwo.setStatusCode(ProposalStatus.PFU_RETURN_TO_PI);
                            excludedStatuses.add(excludedStatusTwo);
                            ProposalStatus excludedStatusThree = new ProposalStatus();
                            excludedStatusThree.setStatusCode(ProposalStatus.BR_NOT_SHARED_WITH_SPO_AOR);
                            excludedStatuses.add(excludedStatusThree);
                            ProposalStatus excludedStatusFour = new ProposalStatus();
                            excludedStatusFour.setStatusCode(ProposalStatus.BR_RETURN_TO_PI);
                            excludedStatuses.add(excludedStatusFour);

                            // Included revision types
                            ProposalRevisionType typeOne = new ProposalRevisionType();
                            typeOne.setType(ProposalRevisionType.ORIGINAL_PROPOSAL);
                            propRevnTypes.add(typeOne);
                            ProposalRevisionType typeTwo = new ProposalRevisionType();
                            typeTwo.setType(ProposalRevisionType.PROPOSAL_FILE_UPDATE);
                            propRevnTypes.add(typeTwo);
                            ProposalRevisionType typeThree = new ProposalRevisionType();
                            typeThree.setType(ProposalRevisionType.BUDGET_REVISION);
                            propRevnTypes.add(typeThree);
                            ProposalRevisionType typeFour = new ProposalRevisionType();
                            typeFour.setType(ProposalRevisionType.BUDGET_UPDATE);
                            propRevnTypes.add(typeFour);
                            ProposalRevisionType typeFive = new ProposalRevisionType();
                            typeFive.setType(ProposalRevisionType.PROPOSAL_UPDATE);
                            propRevnTypes.add(typeFive);
                            params.setPropRevnTypes(propRevnTypes);

                            params.setIsSubmitted(true); // Set type of query

                            LOGGER.debug("ProposalManagementServiceImpl.getProposals()::Query type: SUBMITTED");

                        }

                        params.setPropStatus(proposalStatuses);
                        params.setStatusesToExcludeAORSPO(excludedStatuses);
                        params.setPersonnel(proposalPersonsSPOAOR);
                        params.setIsSPOAOR(true);
                        paramList.add(params);

                        LOGGER.debug("ProposalManagementServiceImpl.getProposals()::Query is by INSTITUTION ID");
                    }
                }
            }

            LOGGER.debug("ProposalManagementServiceImpl.getProposals()::Number of queries: " + paramList.size());

            propResults = proposalDataServiceClient.getProposals(paramList);

            LOGGER.debug("ProposalManagementServiceImpl.getProposals()::Number of proposals returned: "
                    + propResults.size());

            // Extract funding op ids
            DeadlineData data = null;
            data = getDeadlineDates(propResults);
            Map<String, Map<String, GrantApplicationListRowLite>> gappsResultsByPi = new HashMap<String, Map<String, GrantApplicationListRowLite>>();

            if (submitted) {
                Set<String> piNsfIds = propResults.stream().map(Proposal::getPiNsfId).collect(Collectors.toSet());
                for (String piNsfId : piNsfIds) {
                    if (!StringUtils.isEmpty(piNsfId)) {
                        Map<String, GrantApplicationListRowLite> piNsfIdGappsResults = findGrantApplicationForANsfId(
                                piNsfId);
                        gappsResultsByPi.put(piNsfId, piNsfIdGappsResults);
                    }
                }
            }

            Map<String, List<ProposalReview>> proposalReviewers = new HashMap<String, List<ProposalReview>>();
            if (submitted) {
                proposalReviewers = getProposalReviewers(propResults);
            }
            for (Proposal prop : propResults) {
                boolean isBudgetUpdatePossible = true;
                boolean isProposalFileUpdatePossible = true;
                boolean isProposalStatusChanged = false;
                ProposalQueryResult result = new ProposalQueryResult();
                result.setDeadlineTypeText(prop.getDeadlineTypeText());
                if (prop.getDeadlineTypeCode() != null && !StringUtils.isEmpty(prop.getDeadlineTypeCode())) {
                    result.setDeadlineTypeCode(prop.getDeadlineTypeCode().trim());
                } else if (!submitted) { // Deadline type is not provided
                    if (data != null) {
                        result = updateProposalQueryResult(prop, result, data);
                    }
                }
                result.setFundingOpId(prop.getFundingOpId());
                result.setLastUpdatedDate(prop.getLastUpdatedDate());
                result.setPropPrepId(prop.getPropPrepId());
                result.setPropRevId(prop.getPropRevId());
                result.setTitle(prop.getTitle());
                result.setIsReadOnly(prop.getIsReadOnly());
                result.setSubmDate(prop.getSubmDate());
                result.setDeadlineDate(prop.getDeadlineDate());
                result.setRevNo(prop.getRevNo());
                result.setNsfPropId(prop.getNsfPropId());
                result.setPiName(prop.getPiName());
                result.setPiLastName(prop.getPiLastName());
                result.setPiNsfId(prop.getPiNsfId());
                if (prop.getProposalStatus() != null) {
                    if (submitted && prop.getProposalStatus().getStatusCode().trim()
                            .equals(ProposalStatus.SUBMITTED_TO_NSF)) {
                        if (!StringUtils.isEmpty(prop.getProposalStatus().getStatusCode())) {
                            result.setProposalStatus(prop.getProposalStatus().getStatusCode().trim());
                        }
                        if (StringUtils.isEmpty(result.getProposalStatusDesc())) {
                            if (gappsResultsByPi.containsKey(result.getPiNsfId())) {
                                Map<String, GrantApplicationListRowLite> piNsfIdGappsResults = gappsResultsByPi
                                        .get(result.getPiNsfId());
                                if (piNsfIdGappsResults.containsKey(result.getNsfPropId())) {
                                    GrantApplicationListRowLite application = piNsfIdGappsResults
                                            .get(result.getNsfPropId());
                                    if (GappsStatuses.getStatus(application.getStatusCode())
                                            .equalsIgnoreCase(ProposalStatus.GAPPS_AWARDED)
                                            || GappsStatuses.getStatus(application.getStatusCode())
                                                    .equalsIgnoreCase(ProposalStatus.GAPPS_DECLINED)
                                            || GappsStatuses.getStatus(application.getStatusCode())
                                                    .equalsIgnoreCase(ProposalStatus.GAPPS_RETURNED)
                                            || GappsStatuses.getStatus(application.getStatusCode())
                                                    .equalsIgnoreCase(ProposalStatus.GAPPS_WITHDRAWN)) {
                                        result.setProposalStatusDesc(
                                                GappsStatuses.getStatus(application.getStatusCode()));
                                        isBudgetUpdatePossible = false;
                                        isProposalFileUpdatePossible = false;
                                        isProposalStatusChanged = true;
                                    }
                                    if (GappsStatuses.getStatus(application.getStatusCode())
                                            .equalsIgnoreCase(ProposalStatus.GAPPS_RECOMMENDED)
                                            || GappsStatuses.getStatus(application.getStatusCode())
                                                    .equalsIgnoreCase(ProposalStatus.GAPPS_PENDING)) {
                                        result.setProposalStatusDesc(
                                                GappsStatuses.getStatus(application.getStatusCode()));
                                        isProposalFileUpdatePossible = false;
                                        isProposalStatusChanged = true;
                                    }
                                }
                            }
                        }
                        if (StringUtils.isEmpty(result.getProposalStatusDesc())) {
                            if (proposalReviewers.containsKey(prop.getNsfPropId())) {
                                result.setProposalStatusDesc(Constants.DUE_DATE_PASSED_OR_ASSIGNED_FOR_REVIEW);
                                isProposalFileUpdatePossible = false;
                            }
                        }
                        if (StringUtils.isEmpty(result.getProposalStatusDesc())) {
                            if (!StringUtils.isEmpty(prop.getDeadlineTypeCode()) && prop.getDeadlineTypeCode().trim()
                                    .equals(Constants.DUE_DATE_TYPE_ACCEPTED_ANYTIME_CODE)) {
                                result.setProposalStatusDesc(prop.getProposalStatus().getStatusDesc());
                            }
                        }
						if (StringUtils.isEmpty(result.getProposalStatusDesc())) {
							if (prop.getDeadlineDate() != null
									&& PropQueryUtils.isDueDatePassed(prop.getDeadlineDate(), prop.getTimeZone())) {
								result.setProposalStatusDesc(Constants.DUE_DATE_PASSED_BUT_NOT_ASSIGNED_FOR_REVIEW);
							}
						}
						if (StringUtils.isEmpty(result.getProposalStatusDesc())) {
							result.setProposalStatusDesc(prop.getProposalStatus().getStatusDesc());
						}
                    } else {
                        if (!StringUtils.isEmpty(prop.getProposalStatus().getStatusDesc())) {
                            result.setProposalStatusDesc(prop.getProposalStatus().getStatusDesc());
                        }
                        if (!StringUtils.isEmpty(prop.getProposalStatus().getStatusCode())) {
                            result.setProposalStatus(prop.getProposalStatus().getStatusCode().trim());
                        }
                    }
                }
                result.setPersonnel(prop.getPersonnel());
                if (prop.getPfus() != null && !prop.getPfus().isEmpty()) {
                    ProposalRevision rev = prop.getPfus().get(prop.getPfus().size() - 1);
                    List<ProposalQueryRevisionResult> revs = new ArrayList<>();
                    String propLatestSubmittedRevId = prop.getPropRevId();
                    for (ProposalRevision revision : prop.getPfus()) {
                        ProposalQueryRevisionResult revResult = new ProposalQueryRevisionResult();
                        revResult.setLastUpdateDate(revision.getLastUpdatedTmsp());
                        revResult.setPersonnel(revision.getPersonnel());
                        revResult.setPiLastName(revision.getPiLastName());
                        revResult.setProposalStatus(revision.getProposalStatus());
                        revResult.setPropRevId(revision.getPropRevId());
                        if (revision.getProposalStatus() != null
                                && !StringUtils.isEmpty(revision.getProposalStatus().getStatusCode())
                                && revision.getProposalStatus().getStatusCode().trim()
                                        .equals(ProposalStatus.SUBMITTED_TO_NSF)) {
                            propLatestSubmittedRevId = revision.getPropRevId();
                        }
                        if (revision != null && revision.getProposalStatus() != null) {
                            if (!isProposalFileUpdatePossible) {
                                if (isBudgetUpdatePossible) {
                                    if ((ProposalStatus.PFU_NOT_FORWARDED_TO_SPO
                                            .equals(revision.getProposalStatus().getStatusCode().trim()))
                                            || (ProposalStatus.PFU_VIEW_ONLY_SPO_AOR
                                                    .equals(revision.getProposalStatus().getStatusCode().trim()))
                                            || (ProposalStatus.PFU_VIEW_EDIT_SPO
                                                    .equals(revision.getProposalStatus().getStatusCode().trim()))
                                            || (ProposalStatus.PFU_RETURN_TO_PI
                                                    .equals(revision.getProposalStatus().getStatusCode().trim()))
                                            || (ProposalStatus.PFU_SUBMITTED_ACCESS_FOR_AOR
                                                    .equals(revision.getProposalStatus().getStatusCode().trim()))) {
                                        revResult.setStatusError(true);
                                        if (isProposalStatusChanged) {
                                            revResult.setProposalStatusDesc(
                                                    Constants.CANNOT_SUBMIT_PFU_PROPOSAL_STATUS_CHANGED);
                                        } else {
                                            revResult
                                                    .setProposalStatusDesc(Constants.CANNOT_SUBMIT_ASSIGNED_FOR_REVIEW);
                                        }
                                    } else {
                                        revResult.setStatusError(false);
                                        revResult.setProposalStatusDesc(revision.getProposalStatus().getStatusDesc());
                                    }
                                } else {
                                    revResult.setStatusError(true);
                                    if ((ProposalStatus.PFU_NOT_FORWARDED_TO_SPO
                                            .equals(revision.getProposalStatus().getStatusCode().trim()))
                                            || (ProposalStatus.PFU_VIEW_ONLY_SPO_AOR
                                                    .equals(revision.getProposalStatus().getStatusCode().trim()))
                                            || (ProposalStatus.PFU_VIEW_EDIT_SPO
                                                    .equals(revision.getProposalStatus().getStatusCode().trim()))
                                            || (ProposalStatus.PFU_RETURN_TO_PI
                                                    .equals(revision.getProposalStatus().getStatusCode().trim()))
                                            || (ProposalStatus.PFU_SUBMITTED_ACCESS_FOR_AOR
                                                    .equals(revision.getProposalStatus().getStatusCode().trim()))) {
                                        revResult.setProposalStatusDesc(
                                                Constants.CANNOT_SUBMIT_PFU_PROPOSAL_STATUS_CHANGED);
                                    }
                                    if ((ProposalStatus.BR_NOT_SHARED_WITH_SPO_AOR
                                            .equals(revision.getProposalStatus().getStatusCode().trim()))
                                            || (ProposalStatus.BR_VIEW_ONLY_SPO_AOR
                                                    .equals(revision.getProposalStatus().getStatusCode().trim()))
                                            || (ProposalStatus.BR_VIEW_EDIT_SPO_AOR
                                                    .equals(revision.getProposalStatus().getStatusCode().trim()))
                                            || (ProposalStatus.BR_RETURN_TO_PI
                                                    .equals(revision.getProposalStatus().getStatusCode().trim()))
                                            || (ProposalStatus.BR_SUBMITTED_ACCESS_FOR_AOR
                                                    .equals(revision.getProposalStatus().getStatusCode().trim()))) {
                                        revResult.setProposalStatusDesc(
                                                Constants.CANNOT_SUBMIT_BREV_PROPOSAL_STATUS_CHANGED);
                                    }
                                }
                            } else {
                                revResult.setStatusError(false);
                                revResult.setProposalStatusDesc(revision.getProposalStatus().getStatusDesc());
                            }
                        }
                        revs.add(revResult);
                    }
                    if (propLatestSubmittedRevId == prop.getPropRevId()) {
                        result.setPfuProposals(revs);
                    } else {
                        result.setPropRevId(propLatestSubmittedRevId);
                    }
                    result.setPiName(rev.getPiName()); // Front End should
                                                       // display latest PI Name
                    result.setPiLastName(rev.getPiLastName());
                    result.setPiNsfId(rev.getPiNsfId());
                }
                results.add(result);
            }
            return results;
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }

    @Override
    public ByteArrayOutputStream getBudgetPdf(String propPrepId, String revisionId, String instId,
            boolean isPrintEntire) throws CommonUtilException {

        PdfGenerationData pdf = new PdfGenerationData();

        Institution awdOrganization = externalServices.getInstitutionById(instId);
        String orgName = awdOrganization.getOrganizationName();
        InstitutionBudget instBudget = proposalDataServiceClient.getInstitutionBudget(propPrepId, revisionId, instId);
        ProposalPackage prop = getProposal(propPrepId, revisionId);

        // Change the Revision Number for BREV
        if (ProposalRevisionType.BUDGET_REVISION.equalsIgnoreCase(prop.getPropPrepRevnTypeCode())) {
            prop.setRevNum(getRevNumberForRevisedBudget(propPrepId, revisionId));
        }
        pdf.setOrgName(orgName);
        pdf.setInstBudget(instBudget);
        pdf.setProp(prop);
        pdf.setPrintEntire(isPrintEntire);

        List<UploadableProposalSection> uploadedFileList = new ArrayList<UploadableProposalSection>();
        UploadableProposalSection sectionFile = (UploadableProposalSection) getSectionData(propPrepId, revisionId,
                Section.BUDGET_JUST);
        LOGGER.debug("******** Budget Justi file path : " + sectionFile.getFilePath());
        if (sectionFile.getFilePath() != null) {
            GetFileResponse resp = fileStorageServiceClient.getFile(sectionFile.getFilePath());
            sectionFile.setFile(resp.getFile());
            uploadedFileList.add(sectionFile);
        }

        if (!prop.getPropPrepRevnTypeCode().equalsIgnoreCase(ProposalRevisionType.ORIGINAL_PROPOSAL)) {
            sectionFile = (UploadableProposalSection) getSectionData(propPrepId, revisionId, Section.BUDI);
            LOGGER.debug("******** Budget Impact file path : " + sectionFile.getFilePath());
            if (sectionFile.getFilePath() != null) {
                GetFileResponse resp = fileStorageServiceClient.getFile(sectionFile.getFilePath());
                sectionFile.setFile(resp.getFile());
                uploadedFileList.add(sectionFile);
            }
        }
        pdf.setUploadedFileList(uploadedFileList);
        GetGeneratedDocumentResponse response = documentGenerationServiceClient.getBudgetPdf(pdf);
        byte[] file = response.getFile();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(file.length);
        baos.write(file, 0, file.length);
        return baos;
    }

    /**
     * This method will return concatenated Pdf for a given List of Files.
     * 
     * @param propPrepId
     * @param revisionId
     * @return
     * @throws CommonUtilException
     */
    private ByteArrayOutputStream getConcatenatedPdf(String propPrepId, String revisionId, String nsfPropId,
            String nsfTempPropId, Section section) throws CommonUtilException {

        PdfGenerationData pdf = new PdfGenerationData();
        List<UploadableProposalSection> uploadedFileList = new ArrayList<UploadableProposalSection>();
        Section section1 = null;
        Section section2 = null;
        if (section.equals(Section.SRL)) {
            section1 = section;
            if (nsfPropId != null || nsfTempPropId != null) {
                section2 = Section.RNI;
            }
        }

        if (section.equals(Section.OSD)) {
            section1 = section;
            if (nsfPropId != null || nsfTempPropId != null) {
                section2 = Section.OPBIO;
            }
        }
        pdf.setSection(section1);
        UploadableProposalSection srlFile = (UploadableProposalSection) getSectionData(propPrepId, revisionId,
                section1);
        if (srlFile.getFilePath() != null) {
            GetFileResponse resp = fileStorageServiceClient.getFile(srlFile.getFilePath());
            srlFile.setFile(resp.getFile());
            uploadedFileList.add(srlFile);
            pdf.setUploadedFileList(uploadedFileList);
        }

        if (section2 != null) {
            UploadableProposalSection rniFile = (UploadableProposalSection) getSectionData(propPrepId, revisionId,
                    section2);
            if (rniFile.getFilePath() != null) {
                GetFileResponse resp = fileStorageServiceClient.getFile(rniFile.getFilePath());
                rniFile.setFile(resp.getFile());
                uploadedFileList.add(rniFile);
                pdf.setUploadedFileList(uploadedFileList);
            }
        }

        GetGeneratedDocumentResponse response = documentGenerationServiceClient.getConcatenatedPdf(pdf);
        byte[] file = response.getFile();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(file.length);
        baos.write(file, 0, file.length);
        return baos;
    }

    @Override
    public PSMMessages validateEntireProposal(String propPrepId, String propRevId, String nsfId)
            throws CommonUtilException {
        try {
            LOGGER.debug("ProposalManagementServiceImpl.validateEntirePoroposal()");
            PSMMessages validationMsgs = new PSMMessages();

            /* Get CoverSheet Compliance Checks */
            CoverSheet coverSheet = getCoverSheetForValidate(propPrepId, propRevId);
            LOGGER.debug("CoverSheet PSM foundation -  " + coverSheet);

            LOGGER.debug("ProposalManagementServiceImpl.validateEntireProposal() List of PSMMessages :: "
                    + coverSheet.getPsmMsgList());
            SectionResponse coverValidationResponse = validateCoversheet(coverSheet);

            /* Get Budget Compliance Checks */
            String instId = coverSheet.getAwdOrganization().getId();
            Section section = Section.getSection(Section.BUDGETS.getCode());
            InstitutionBudget instBudget = getInstitutionBudgetForValidate(propPrepId, propRevId, section, instId);
            SectionResponse budgetValidationResponse = validateInstitutionBudget(instBudget);

            /* Get the Proposal Section Exits Compliance Checks */
            SectionResponse proposalRectionsRepsose = validateProposalSections(propPrepId, propRevId, nsfId, coverSheet,
                    instBudget);

            validationMsgs.addMessagesList(coverValidationResponse.getMessages());
            validationMsgs.addMessagesList(budgetValidationResponse.getMessages());
            validationMsgs.addMessagesList(proposalRectionsRepsose.getMessages());

            LOGGER.debug("******** Before Total Validation Msgs : " + validationMsgs.getPsmMessages().size());

            // pulling Bio Sketches & project description warning msgs.
            List<PSMMessage> list = proposalDataServiceClient.getOnlyProposalWarningMessages(propPrepId, propRevId)
                    .getPsmMsgList();
            LOGGER.debug("******** List of BIO & Proj Desc Warning Msgs : " + list.size());
            if (!list.isEmpty()) {
                validationMsgs.addMessagesList(list);
            }
            return validationMsgs;

        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }

    private String formatPIName(Pi pi) {
        if (pi.getFirstName() != null && pi.getLastName() != null && pi.getMiddleInitial() != null) {
            return pi.getFirstName() + ' ' + pi.getMiddleInitial() + ' ' + pi.getLastName();
        } else if (pi.getFirstName() != null && pi.getLastName() != null) {
            return pi.getFirstName() + ' ' + pi.getLastName();
        } else {
            return pi.getLastName();
        }

    }

    private void setPiCopiInformation(List<PiCoPiInformation> piCopiList) throws CommonUtilException {

        for (PiCoPiInformation piCoPi : piCopiList) {
            if (piCoPi != null) {
                Pi pi = new Pi();
                try {
                    pi = solicitationDataServiceclient.getPIDetails(piCoPi.getNsfId());
                } catch (Exception e) {
                    throw new CommonUtilException(Constants.GET_PI_DETAILS_ERROR, e);
                }
                piCoPi.setName(formatPIName(pi));
                piCoPi.setDegree(pi.getDegree());
                piCoPi.setDegreeYear(pi.getYearofDegree());
                piCoPi.setEmailAddress(pi.getEmail());
                piCoPi.setTelephoneNum(pi.getPhoneNumber());
                piCoPi.setDepartmentName(pi.getDepartmentName());
                piCoPi.setFaxNumber(pi.getFaxNumber());
                InstitutionAddress address = new InstitutionAddress();
                address.setStreetAddress(pi.getAddress().getStreetAddress());
                address.setStreetAddress2(pi.getAddress().getStreetAddress2());
                address.setCityName(pi.getAddress().getCityName());
                address.setStateCode(pi.getAddress().getStateCode());
                address.setCountryCode(pi.getAddress().getCountryCode());
                address.setPostalCode(pi.getAddress().getPostalCode());
                piCoPi.setAddress(address);
                LOGGER.debug(" setPiCopiInformation: Demog Info : " + piCoPi.toString());

            } else {
                LOGGER.info("Personnel does not exists ");
            }
        }
    }

    @Override
    public ByteArrayOutputStream getCoverSheetPdf(String propPrepId, String propRevId, boolean printPageNumbers)
            throws CommonUtilException {
        ProposalPackage prop = getProposal(propPrepId, propRevId);

        InstitutionBudget instBudget = getInstitutionBudget(propPrepId, propRevId, Section.BUDGETS,
                prop.getInstitution().getId());

        CoverSheet coverSheet = proposalDataServiceClient.getCoverSheet(prop.getPropPrepId(), prop.getPropRevId());

        List<PiCoPiInformation> piCopiList = coverSheet.getPiCopiList();
        setPiCopiInformation(piCopiList);

        PdfGenerationData pdf = new PdfGenerationData();
        pdf.setProp(prop);
        pdf.setCoverSheet(coverSheet);
        pdf.setInstBudget(instBudget);
        pdf.setPrintPageNumbers(printPageNumbers);
        GetGeneratedDocumentResponse response = documentGenerationServiceClient.generateCoverSheetPdf(pdf);
        byte[] file = response.getFile();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(file.length);
        baos.write(file, 0, file.length);
        return baos;
    }

    @Override
    public ByteArrayOutputStream getSubmittedProposalPdf(String nsfPropId, String nsfTempPropId, String sectionCode,
            String prepId, String revId) throws CommonUtilException {
        LOGGER.debug("********* ProposalManagementServiceImpl.getSubmittedProposalPdf() ***********");
        LOGGER.debug(
                "********* Geeting Section for: nsfPropId  : " + nsfPropId + "   nsfTempPropId : " + nsfTempPropId);
        ByteArrayOutputStream concatenatedPdf = new ByteArrayOutputStream();
        List<UploadableProposalSection> uploadedFileList = new ArrayList<UploadableProposalSection>();
        UploadableProposalSection ups = new UploadableProposalSection();
        PdfGenerationData pdf = new PdfGenerationData();
        boolean pageNos = false;
        boolean isBudgetSectionOnly = true;
        Section section = Section.getSection(sectionCode);
        pdf.setSection(section);
        LOGGER.debug("********* Geeting Section Pdf for : " + section);
        if (section != null) {
            String propPrepId = null;
            String revisionId = null;
            String instId = null;

            if (nsfPropId != null) {
                SubmittedProposal subProp = proposalDataServiceClient.getSubmittedProposal(nsfPropId);
                propPrepId = subProp.getPropPrepId();
                revisionId = subProp.getPropRevId();
                pageNos = true;
            } else if (nsfTempPropId != null) {
                ProposalPackage propPackage = getPrepRevIdWithNsfTemporaryProposalId(nsfTempPropId);
                propPrepId = propPackage.getPropPrepId();
                revisionId = propPackage.getPropRevId();
                pageNos = true;
                isBudgetSectionOnly = false;
            } else {
                propPrepId = prepId;
                revisionId = revId;
            }
            LOGGER.debug("********* propPrepId  : " + propPrepId + "   revisionId : " + revisionId);

            try {
                ProposalPackage prop = getProposal(propPrepId, revisionId);
                LOGGER.debug("********  RevisionType : " + prop.getPropPrepRevnTypeCode() + "  Submission Status : "
                        + prop.getProposalStatus());
                if (prop != null) {
                    instId = prop.getInstitution().getId();
                }
            } catch (Exception e) {
                LOGGER.error("getSubmittedProposalPDF", e);
            }

            switch (section) {
            case BIOSKETCH:
                List<BiographicalSketch> bioLikst = proposalDataServiceClient.getBioSketchesForProposal(revisionId);
                if (bioLikst != null && !bioLikst.isEmpty()) {
                    LOGGER.debug(
                            "ProposalManagementServiceImpl.getSeniorPersonnelsPdf() bioLikst : " + bioLikst.size());
                    for (BiographicalSketch bio : bioLikst) {
                        if (bio != null && bio.getFilePath() != null) {
                            UploadableProposalSection ups1 = new UploadableProposalSection();
                            GetFileResponse resp = fileStorageServiceClient.getFile(bio.getFilePath());
                            ups1.setFile(resp.getFile());
                            ups1.setFilePath(bio.getFilePath());
                            ups1.setPageCount(bio.getPageCount());
                            uploadedFileList.add(ups1);
                        }
                    }
                }
                break;
            case CURR_PEND_SUPP:

                List<CurrentAndPendingSupport> cpList = proposalDataServiceClient
                        .getCurrentAndPendingSupportForProposal(revisionId);
                if (cpList != null && !cpList.isEmpty()) {
                    for (CurrentAndPendingSupport cp : cpList) {
                        LOGGER.debug("ProposalManagementServiceImpl.getSeniorPersonnelsPdf() cp : " + cp);
                        if (cp != null && cp.getFilePath() != null) {
                            UploadableProposalSection ups1 = new UploadableProposalSection();
                            GetFileResponse resp = fileStorageServiceClient.getFile(cp.getFilePath());
                            ups1.setFile(resp.getFile());
                            ups1.setFilePath(cp.getFilePath());
                            ups1.setPageCount(cp.getPageCount());
                            uploadedFileList.add(ups1);
                        }
                    }
                }
                break;
            case COA:
                List<COA> coaList = proposalDataServiceClient.getCOAsForProposal(Long.valueOf(revisionId));
                if (coaList != null && !coaList.isEmpty())
                    for (COA coa : coaList) {
                        if (coa != null) {
                            String fileKey = coa.getCoaPdfFilePath().trim();
                            LOGGER.debug("ProposalManagementServiceImpl.getSeniorPersonnelsPdf() COA pdffile path : "
                                    + fileKey + " is Empty : " + org.apache.commons.lang.StringUtils.isEmpty(fileKey));
                            if (!org.apache.commons.lang.StringUtils.isEmpty(fileKey)) {
                                UploadableProposalSection ups1 = new UploadableProposalSection();
                                GetFileResponse fileRes = fileStorageServiceClient.getFile(fileKey);
                                ups1.setFile(fileRes.getFile());
                                ups1.setFilePath(fileKey);
                                ups1.setPageCount(coa.getPageCount());
                                uploadedFileList.add(ups1);
                            }
                        }
                    }

                break;

            case BUDGETS:
                if (instId != null) {
                    ByteArrayOutputStream budg = getBudgetPdf(propPrepId, revisionId, instId, isBudgetSectionOnly);
                    ups.setFile(budg.toByteArray());
                    uploadedFileList.add(ups);
                }
                break;

            case COVER_SHEET:
                ByteArrayOutputStream cv = getCoverSheetPdf(propPrepId, revisionId, pageNos);
                ups.setFile(cv.toByteArray());
                uploadedFileList.add(ups);
                break;

            case SRL:
                ByteArrayOutputStream concatenatedBaos = getConcatenatedPdf(propPrepId, revisionId, nsfPropId,
                        nsfTempPropId, Section.SRL);
                ups.setFile(concatenatedBaos.toByteArray());
                uploadedFileList.add(ups);
                break;

            case OSD:
                ByteArrayOutputStream osd = getConcatenatedPdf(propPrepId, revisionId, nsfPropId, nsfTempPropId,
                        Section.OSD);
                ups.setFile(osd.toByteArray());
                uploadedFileList.add(ups);
                break;

            default:
                // Pull other uploaded section file.
                Object sectionData = getSectionData(propPrepId, revisionId, section);
                LOGGER.debug("ProposalManagementServiceImpl.getSeniorPersonnelsPdf() :: " + sectionData);
                if (sectionData instanceof UploadableProposalSection) {
                    String fileKey = ((UploadableProposalSection) sectionData).getFilePath();
                    LOGGER.debug("ProposalManagementServiceImpl.getSeniorPersonnelsPdf() fileKey :: " + fileKey);

                    if (fileKey != null) {
                        GetFileResponse fileRes = fileStorageServiceClient.getFile(fileKey);
                        ups.setFile(fileRes.getFile());
                        uploadedFileList.add(ups);
                    }
                }
                break;
            }
            pdf.setUploadedFileList(uploadedFileList);
        }

        GetGeneratedDocumentResponse response = documentGenerationServiceClient.generateSeniorPersonnelsPdf(pdf);

        byte[] file = response.getFile();
        concatenatedPdf = new ByteArrayOutputStream(file.length);
        concatenatedPdf.write(file, 0, file.length);
        return concatenatedPdf;
    }

    /**
     * This method returns a list of proposal sections with associated status
     * information
     *
     * @param propPrepId
     * @param propRevId
     * @return Set<Section>
     */
    @Override
    public List<ProposalPersonnelSectionStatus> getAllSectionStatusesByPersonnel(String propPrepId, String propRevId)
            throws CommonUtilException {

        PersonnelData data = proposalDataServiceClient.getLatestPersonnelSectionStatusData(propPrepId, propRevId);
        return getProposalPersonnelStatusBySection(data);

    }

    /**
     * This method will return the all sections uploaded file paths
     *
     * @param data
     * @return
     */
    @Override
    public List<ProposalPersonnelSectionStatus> getProposalPersonnelStatusBySection(PersonnelData data)
            throws CommonUtilException {

        List<ProposalPersonnelSectionStatus> statuses = new ArrayList<>();

        try {

            for (PersonnelSectionData personnelData : data.getStatuses()) {

                for (SectionStatus sec : personnelData.getStatuses()) {

                    ProposalSectionStatus propSectionStatus = new ProposalSectionStatus();

                    // load values from the database
                    propSectionStatus.setComplianceStatus(SectionComplianceStatus.NOT_CHECKED.getStatus());
                    propSectionStatus.setEnableAccess(true);

                    // populate section data
                    propSectionStatus.setCode(sec.getSectionCode());
                    propSectionStatus.setName(Section.getSection(sec.getSectionCode()).getName());
                    propSectionStatus.setCamelCaseName(Section.getSection(sec.getSectionCode()).getCamelCaseName());
                    propSectionStatus.setEntryType(Section.getSection(sec.getSectionCode()).getEntryType().name());

                    // set section compliance
                    SectionCompliance sectionCompliance = new SectionCompliance();
                    if (sec.getWarningMsgs() != null && !sec.getWarningMsgs().isEmpty()) {
                        sectionCompliance.setNoOfWarnings(sec.getWarningMsgs().size());
                    }
                    if (sec.getErrorMsgs() != null && !sec.getErrorMsgs().isEmpty()) {
                        sectionCompliance.setNoOfErrors(sec.getErrorMsgs().size());
                    }
                    sectionCompliance.setNoOfDocsUnavailable(1);
                    propSectionStatus.setSectionCompliance(sectionCompliance);

                    // Check for documents
                    if (sec.getLastUpdatedTmsp() != null) {
                        propSectionStatus.setRequired(true);
                        propSectionStatus.setLastUpdateDate(sec.getLastUpdatedTmsp());
                        if (propSectionStatus.getLastUpdateDate() != null && sectionCompliance != null) {
                            if (sec.getFilePath() != null && !StringUtils.isEmpty(sec.getFilePath().trim())) {
                                if ((sectionCompliance.getNoOfErrors() + sectionCompliance.getNoOfWarnings()) > 0) {
                                    propSectionStatus
                                            .setComplianceStatus(SectionComplianceStatus.NONCOMPLIANT.getStatus());
                                } else {
                                    propSectionStatus
                                            .setComplianceStatus(SectionComplianceStatus.COMPLIANT.getStatus());
                                }
                                sectionCompliance.setNoOfDocsUnavailable(0);
                                propSectionStatus.setSectionCompliance(sectionCompliance);
                            }
                            propSectionStatus
                                    .setSectionUpdated(SectionStatusUtils.populateSectionUpdatedSeniorPersonnel(sec));
                        }
                        ProposalPersonnelSectionStatus status = new ProposalPersonnelSectionStatus(
                                personnelData.getPersonnel(), propSectionStatus);
                        if (!statuses.contains(status)) {
                            statuses.add(status);
                        }
                    } else {
                        ProposalPersonnelSectionStatus status = new ProposalPersonnelSectionStatus(
                                personnelData.getPersonnel(), propSectionStatus);
                        if (!statuses.contains(status)) {
                            statuses.add(status);
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new CommonUtilException(e);
        }

        return statuses;

    }

    /**
     * This method will return the all sections uploaded file paths
     *
     * @param propPrepId
     * @param propRevId
     * @return
     */

    @Override
    public Map<Section, UploadableProposalSection> getAllSectionsUploadedFilePath(String propPrepId, String propRevId,
            String propPersId) throws CommonUtilException {

        Map<Section, UploadableProposalSection> uploadedSectionFilePaths = new HashMap<Section, UploadableProposalSection>();

        // Non-personnel doc sections
        uploadedSectionFilePaths = getAllNonPersonnelSectionsUploadedFilePaths(propPrepId, propRevId);

        // Personnel doc sections
        if (!StringUtils.isEmpty(propPersId)) {
            uploadedSectionFilePaths
                    .putAll(getAllPersonnelSectionsUploadedFilePaths(propPrepId, propRevId, propPersId));
        }

        LOGGER.info("ProposalManagementServiceImpl.getAllSectionsUploadedFilePath() uploadedSectionFilePaths :: "
                + uploadedSectionFilePaths);

        return uploadedSectionFilePaths;
    }

    private Map<Section, UploadableProposalSection> getAllNonPersonnelSectionsUploadedFilePaths(String propPrepId,
            String propRevId) throws CommonUtilException {

        Map<Section, UploadableProposalSection> uploadedSectionFilePaths = new HashMap<Section, UploadableProposalSection>();

        UploadableProposalSection projSummSec = proposalDataServiceClient.getProjectSummary(propPrepId, propRevId);
        if (projSummSec != null && !StringUtils.isEmpty(projSummSec.getFilePath())) {
            UploadableProposalSection section = null;
            try {
                section = new UploadableProposalSection(projSummSec.getPageCount(), projSummSec.getFilePath());
                section.setOrigFileName(projSummSec.getOrigFileName());
            } catch (NullPointerException e) {
                throw new CommonUtilException(e);
            }
            if (section != null) {
                section.setSectionCode(Section.PROJ_SUMM.getCode());
                uploadedSectionFilePaths.put(Section.PROJ_SUMM, section);
            }
        }

        UploadableProposalSection recCitedSec = proposalDataServiceClient.getReferencesCited(propPrepId, propRevId);
        if (recCitedSec != null && !StringUtils.isEmpty(recCitedSec.getFilePath())) {
            UploadableProposalSection section = null;
            try {
                section = new UploadableProposalSection(recCitedSec.getPageCount(), recCitedSec.getFilePath());
                section.setOrigFileName(recCitedSec.getOrigFileName());
            } catch (NullPointerException e) {
                throw new CommonUtilException(e);
            }
            if (section != null) {
                section.setSectionCode(Section.REF_CITED.getCode());
                uploadedSectionFilePaths.put(Section.REF_CITED, section);
            }
        }

        UploadableProposalSection facEqupSec = proposalDataServiceClient.getFacilitiesEquipment(propPrepId, propRevId);
        if (facEqupSec != null && !StringUtils.isEmpty(facEqupSec.getFilePath())) {
            UploadableProposalSection section = null;
            try {
                section = new UploadableProposalSection(facEqupSec.getPageCount(), facEqupSec.getFilePath());
                section.setOrigFileName(facEqupSec.getOrigFileName());
            } catch (NullPointerException e) {
                throw new CommonUtilException(e);
            }
            if (section != null) {
                section.setSectionCode(Section.FER.getCode());
                uploadedSectionFilePaths.put(Section.FER, section);
            }
        }

        UploadableProposalSection projDescSec = proposalDataServiceClient.getProjectDescription(propPrepId, propRevId);
        if (projDescSec != null && !StringUtils.isEmpty(projDescSec.getFilePath())) {
            UploadableProposalSection section = null;
            try {
                section = new UploadableProposalSection(projDescSec.getPageCount(), projDescSec.getFilePath());
                section.setOrigFileName(projDescSec.getOrigFileName());
            } catch (NullPointerException e) {
                throw new CommonUtilException(e);
            }
            if (section != null) {
                section.setSectionCode(Section.PROJ_DESC.getCode());
                uploadedSectionFilePaths.put(Section.PROJ_DESC, section);
            }
        }

        UploadableProposalSection budgJustSec = proposalDataServiceClient.getBudgetJustification(propPrepId, propRevId);
        if (budgJustSec != null && !StringUtils.isEmpty(budgJustSec.getFilePath())) {
            UploadableProposalSection section = null;
            try {
                section = new UploadableProposalSection(budgJustSec.getPageCount(), budgJustSec.getFilePath());
                section.setOrigFileName(budgJustSec.getOrigFileName());
            } catch (NullPointerException e) {
                throw new CommonUtilException(e);
            }
            if (section != null) {
                section.setSectionCode(Section.BUDGET_JUST.getCode());
                uploadedSectionFilePaths.put(Section.BUDGET_JUST, section);
            }
        }

        BudgetImpact bi = proposalDataServiceClient.getBudgetImpact(propPrepId, propRevId);
        if (bi != null && !StringUtils.isEmpty(bi.getFilePath())) {
            UploadableProposalSection section = null;
            try {
                section = new UploadableProposalSection(bi.getPageCount(), bi.getFilePath());
                section.setOrigFileName(bi.getOrigFileName());
            } catch (NullPointerException e) {
                throw new CommonUtilException(e);
            }
            if (section != null) {
                section.setSectionCode(Section.BUDI.getCode());
                uploadedSectionFilePaths.put(Section.BUDI, section);
            }
        }

        UploadableProposalSection suggRevrSec = proposalDataServiceClient.getSuggestedReviewers(propPrepId, propRevId);
        if (suggRevrSec != null && !StringUtils.isEmpty(suggRevrSec.getFilePath())) {
            UploadableProposalSection section = null;
            try {
                section = new UploadableProposalSection(suggRevrSec.getPageCount(), suggRevrSec.getFilePath());
                section.setOrigFileName(suggRevrSec.getOrigFileName());
            } catch (NullPointerException e) {
                throw new CommonUtilException(e);
            }
            if (section != null) {
                section.setSectionCode(Section.SRL.getCode());
                uploadedSectionFilePaths.put(Section.SRL, section);
            }
        }

        UploadableProposalSection revrsNotInclSec = proposalDataServiceClient.getReviewersNotInclude(propPrepId,
                propRevId);
        if (revrsNotInclSec != null && !StringUtils.isEmpty(revrsNotInclSec.getFilePath())) {
            UploadableProposalSection section = null;
            try {
                section = new UploadableProposalSection(revrsNotInclSec.getPageCount(), revrsNotInclSec.getFilePath());
                section.setOrigFileName(revrsNotInclSec.getOrigFileName());
            } catch (NullPointerException e) {
                throw new CommonUtilException(e);
            }
            if (section != null) {
                section.setSectionCode(Section.RNI.getCode());
                uploadedSectionFilePaths.put(Section.RNI, section);
            }
        }

        UploadableProposalSection othrBioInfoSec = proposalDataServiceClient.getOthrPersBioInfo(propPrepId, propRevId);
        if (othrBioInfoSec != null && !StringUtils.isEmpty(othrBioInfoSec.getFilePath())) {
            UploadableProposalSection section = null;
            try {
                section = new UploadableProposalSection(othrBioInfoSec.getPageCount(), othrBioInfoSec.getFilePath());
                section.setOrigFileName(othrBioInfoSec.getOrigFileName());
            } catch (NullPointerException e) {
                throw new CommonUtilException(e);
            }
            if (section != null) {
                section.setSectionCode(Section.OPBIO.getCode());
                uploadedSectionFilePaths.put(Section.OPBIO, section);
            }
        }

        UploadableProposalSection mgmtPlanSec = proposalDataServiceClient.getDataManagementPlan(propPrepId, propRevId);
        if (mgmtPlanSec != null && !StringUtils.isEmpty(mgmtPlanSec.getFilePath())) {
            UploadableProposalSection section = null;
            try {
                section = new UploadableProposalSection(mgmtPlanSec.getPageCount(), mgmtPlanSec.getFilePath());
                section.setOrigFileName(mgmtPlanSec.getOrigFileName());
            } catch (NullPointerException e) {
                throw new CommonUtilException(e);
            }
            if (section != null) {
                section.setSectionCode(Section.DMP.getCode());
                uploadedSectionFilePaths.put(Section.DMP, section);
            }
        }

        UploadableProposalSection postDocMntSec = proposalDataServiceClient.getPostDocMentoringPlan(propPrepId,
                propRevId);
        if (postDocMntSec != null && !StringUtils.isEmpty(postDocMntSec.getFilePath())) {
            UploadableProposalSection section = null;
            try {
                section = new UploadableProposalSection(postDocMntSec.getPageCount(), postDocMntSec.getFilePath());
                section.setOrigFileName(postDocMntSec.getOrigFileName());
            } catch (NullPointerException e) {
                throw new CommonUtilException(e);
            }
            if (section != null) {
                section.setSectionCode(Section.PMP.getCode());
                uploadedSectionFilePaths.put(Section.PMP, section);
            }
        }

        OtherSuppDocs osd = proposalDataServiceClient.getOtherSuppDocs(propPrepId, propRevId);
        if (osd != null && !StringUtils.isEmpty(osd.getFilePath())) {
            UploadableProposalSection section = null;
            try {
                section = new UploadableProposalSection(osd.getPageCount(), osd.getFilePath());
                section.setOrigFileName(osd.getOrigFileName());
            } catch (NullPointerException e) {
                throw new CommonUtilException(e);
            }
            if (section != null) {
                section.setSectionCode(Section.OSD.getCode());
                uploadedSectionFilePaths.put(Section.OSD, section);
            }
        }

        return uploadedSectionFilePaths;

    }

    private Map<Section, UploadableProposalSection> getAllPersonnelSectionsUploadedFilePaths(String propPrepId,
            String propRevId, String propPersId) throws CommonUtilException {

        Map<Section, UploadableProposalSection> uploadedSectionFilePaths = new HashMap<Section, UploadableProposalSection>();

        COA coa = proposalDataServiceClient.getProposalCOA(Long.valueOf(propRevId), Long.valueOf(propPersId));
        if (coa != null && !StringUtils.isEmpty(coa.getCoaExcelFilePath())) {
            UploadableProposalSection section = null;
            try {
                section = new UploadableProposalSection(coa.getPageCount(), coa.getCoaExcelFilePath());
                section.setCoa(coa);
            } catch (NullPointerException e) {
                throw new CommonUtilException(e);
            }
            section.setSectionCode(Section.COA.getCode());
            uploadedSectionFilePaths.put(Section.COA, section);
        }

        UploadableProposalSection currPendSupport = proposalDataServiceClient.getCurrentAndPendingSupport(propPrepId,
                propRevId, propPersId);
        if (currPendSupport != null && !StringUtils.isEmpty(currPendSupport.getFilePath())) {
            UploadableProposalSection section = null;
            try {
                section = new UploadableProposalSection(currPendSupport.getPageCount(), currPendSupport.getFilePath());
                section.setOrigFileName(currPendSupport.getOrigFileName());
                section.setPropPersId(propPersId);
            } catch (NullPointerException e) {
                throw new CommonUtilException(e);
            }
            section.setSectionCode(Section.CURR_PEND_SUPP.getCode());
            uploadedSectionFilePaths.put(Section.CURR_PEND_SUPP, section);
        }

        BiographicalSketch sketch = proposalDataServiceClient.getBiographicalSketch(propPrepId, propRevId, propPersId);
        if (sketch != null && !StringUtils.isEmpty(sketch.getFilePath())) {
            UploadableProposalSection section = null;
            try {
                section = new UploadableProposalSection(sketch.getPageCount(), sketch.getFilePath());
                section.setOrigFileName(sketch.getOrigFileName());
                section.setPropPersId(propPersId);
            } catch (NullPointerException e) {
                throw new CommonUtilException(e);
            }
            section.setSectionCode(Section.BIOSKETCH.getCode());
            uploadedSectionFilePaths.put(Section.BIOSKETCH, section);
        }

        return uploadedSectionFilePaths;

    }

    @Override
    public SectionResponse validateProposalSections(String propPrepId, String propRevId, String nsfId,
            CoverSheet coverSheet, InstitutionBudget instBudget) throws CommonUtilException {
        try {

            LOGGER.info("ProposalManagementServiceImpl.validateProposalSections()");
            SectionResponse response = new SectionResponse();

            /* Get the Proposal Section Exits Compliance Checks */
            Map<Section, UploadableProposalSection> proposalSectionMap = getAllSectionsUploadedFilePath(propPrepId,
                    propRevId, null);

            LOGGER.debug("Proposal SectionMap : " + proposalSectionMap.toString());

            List<SrPersonUploadData> srPersUploadDataList = getSrPersonUploadData(propPrepId, propRevId);
            LOGGER.debug("Proposal SrPersonnelList : " + srPersUploadDataList);

            // Retrieve the PSM foundation Proposal Package
            ProposalPackage proposalPackage = getProposal(propPrepId, propRevId);

            ProposalFactModel model = ProposalFactModelUtility.getProposalFactModel(proposalSectionMap, proposalPackage,
                    srPersUploadDataList);

            LOGGER.debug("proposalTypeCode : " + proposalPackage.getProposalTypeCode()
                    + "  ProposalSubmissionTypeCode : " + proposalPackage.getSubmissionTypeCode());

            model.setProposalTypeCode(proposalPackage.getProposalTypeCode());
            model.setSubmissionTypeCode(proposalPackage.getSubmissionTypeCode());
            /**************
             * Should be retrieved from the database
             ******************/
            List<Deadline> dueDates = getDueDates(proposalPackage.getFundingOp().getFundingOpportunityId());

            DeadlineFactModel deadlineFactModel = model.getDeadlineFactModel();
            if (dueDates != null && !dueDates.isEmpty()) {
                // Dates Exist in the 13 month window
                deadlineFactModel.setDateExists13MonthWindow(true);

                // Check if the due date is an accepted anytime date
                if (dueDates.get(0).getDeadlineTypeCode()
                        .equalsIgnoreCase(Constants.DUE_DATE_TYPE_ACCEPTED_ANYTIME_CODE)) {

                    deadlineFactModel.setDueDateSelected(true);

                }
                // check if the due dates has been selected.
                else {

                    if (deadlineFactModel.getDeadlineDate() != null) {
                        deadlineFactModel.setDueDateSelected(true);

                    } else {
                        // No date has been selected by the user
                        deadlineFactModel.setDueDateSelected(false);
                    }
                }
            } else {
                // No Dates Exist in the 13 month window
                deadlineFactModel.setDateExists13MonthWindow(false);
            }

            CoverSheetFactModel coversheetFactmodel = CoverSheetFactModelUtility.getCoverSheetFactModel(coverSheet);
            InstitutionBudgetFactModel instBudgRecFactModel = InstBudgetUtils.getFactModel(instBudget);

            model.setCovrSheetFactModel(coversheetFactmodel);
            model.setInstBudgRecFactModel(instBudgRecFactModel);

            // If a PFU exists, //Retrieve the Proposal Update Justification

            ProposalUpdateJustificationFactModel propUpdJustFactModel = new ProposalUpdateJustificationFactModel();

            // identity revision type based on what's changed in the proposal.
            String revType = getRevisionType(propPrepId, propRevId);

            if (ProposalRevisionType.PROPOSAL_FILE_UPDATE.equalsIgnoreCase(revType)
                    || ProposalRevisionType.PROPOSAL_UPDATE.equalsIgnoreCase(revType)
                    || ProposalRevisionType.BUDGET_UPDATE.equalsIgnoreCase(revType)) {
                ProposalUpdateJustification propUpdateJustifciation = getProposalUpdateJustification(propPrepId,
                        propRevId);
                propUpdJustFactModel.setPropPrepId(propUpdateJustifciation.getPropPrepId());
                propUpdJustFactModel.setPropRevId(propUpdateJustifciation.getPropRevId());
                propUpdJustFactModel.setJustificationText(propUpdateJustifciation.getJustificationText());

            }
            // Retrieve the Reviewer Assignment Status and GAAPS Status for
            // Revision Types

            if (!StringUtils.isEmpty(proposalPackage.getNsfPropId())) {

                Map<String, List<ProposalReview>> proposalReviewers = new HashMap<String, List<ProposalReview>>();
                List<Proposal> propList = new ArrayList<Proposal>();
                Proposal proposal = new Proposal();
                proposal.setNsfPropId(proposalPackage.getNsfPropId());
                proposal.setFundingOpId(proposalPackage.getFundingOp().getFundingOpportunityId());
                proposal.setDeadlineDate(proposalPackage.getDeadline().getDeadlineDate());
                ProposalStatus proposalStatus = new ProposalStatus();
                proposalStatus.setStatusCode(proposalPackage.getProposalStatus());
                proposalStatus.setStatusDesc(proposalPackage.getProposalStatusDesc());
                proposal.setProposalStatus(proposalStatus);
                propList.add(proposal);
                proposalReviewers = getProposalReviewers(propList);
                if (proposalReviewers.containsKey(proposalPackage.getNsfPropId())) {
                    LOGGER.debug("Reviewers are assigned to the Proposal");
                    model.setReviewerAssignedStatus(Constants.ASSIGNED_FOR_REVIEW);

                } else {
                    LOGGER.debug("Reviewers are not assigned to the Proposal");
                    model.setReviewerAssignedStatus(Constants.NOT_ASSIGNED_FOR_REVIEW);
                }

            }

            Map<String, GrantApplicationListRowLite> gappsResults = new HashMap<String, GrantApplicationListRowLite>();
            GrantApplicationListRowLite grantApplication = null;
            if (!StringUtils.isEmpty(proposalPackage.getNsfPropId())) {
                LOGGER.debug("findGrantApplicationForANsfId, proposalNsfPropId: " + proposalPackage.getNsfPropId()
                        + " proposalPiNsfId: " + (!StringUtils.isEmpty(proposalPackage.getPi().getNsfId())
                                ? proposalPackage.getPi().getNsfId() : "")
                        + " loggedUserNsfId: " + nsfId);
                Map<String, String> nsfIds = new HashMap<String, String>();

                if (!StringUtils.isEmpty(proposalPackage.getPi().getNsfId())) {
                    nsfIds.put(proposalPackage.getPi().getNsfId(), proposalPackage.getPi().getNsfId());
                }
                if (!StringUtils.isEmpty(proposalPackage.getLatestSubmittedPiNsfId())) {
                    nsfIds.put(proposalPackage.getLatestSubmittedPiNsfId(),
                            proposalPackage.getLatestSubmittedPiNsfId());
                }
                for (Map.Entry<String, String> entry : nsfIds.entrySet()) {
                    gappsResults.putAll(findGrantApplicationForANsfId(entry.getValue()));
                }
                if (gappsResults.containsKey(proposalPackage.getNsfPropId())) {
                    grantApplication = gappsResults.get(proposalPackage.getNsfPropId());
                }
            }

            if (grantApplication != null) {
                model.setProposalStatus(GappsStatuses.getStatus(grantApplication.getStatusCode()));
            }

            model.setProposalUpdateJustFactModel(propUpdJustFactModel);
            model.setPropPrepRevnTypeCode(revType);

            LOGGER.debug("Proposal  Fact Model After  conversion" + model.toString());
            List<gov.nsf.psm.factmodel.PSMMessage> msgs = new ArrayList<>();
            if (model != null) {
                msgs.addAll(complianceValidationServiceClient.getProposalSectionsComplianceCheckFindings(model));
            }
            List<PSMMessage> validationMsgs = new ArrayList<>();
            for (gov.nsf.psm.factmodel.PSMMessage msg : msgs) {
                PSMMessage message = new PSMMessage();
                message.setDescription(msg.getDescription());
                message.setId(msg.getId());
                message.setType(PSMMessageType.getMessageType(msg.getType().getCode()));
                message.setSectionCode(msg.getSectionCode());
                validationMsgs.add(message);
            }

            response.setMessages(validationMsgs);

            return response;

        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }

    @Override
    public List<PSMMessage> getProposalWarningMessages(String propPrepId, String propRevId, String propPersId,
            String sectionCode) throws CommonUtilException {
        List<PSMMessage> psmMsgList = new ArrayList<PSMMessage>();
        try {
            if (propPrepId != null && propRevId != null && sectionCode != null) {
                WarnMsgs wmgs = proposalDataServiceClient.getProposalWarningMessages(propPrepId, propRevId,
                        (propPersId == null) ? Constants.PROP_PERS_ID : propPersId, sectionCode);
                LOGGER.debug("ProposalManagementServiceImpl.getProposalWarningMessages() Warning Msgs : " + wmgs);
                psmMsgList.addAll(wmgs.getPsmMsgList());
            }
        } catch (Exception e) {
            LOGGER.error("Error encountered on getProposalWarningMessages", e);
        }
        return psmMsgList;
    }

    @Override
    public ProposalResponse createProposalRevision(String propPrepId, String propRevId, String nsfPropId, String nsfId)
            throws CommonUtilException {
        ProposalResponse response = new ProposalResponse();
        boolean createRevision = true;
        PSMMessages msgs = new PSMMessages();
        Personnels pers = getNsfIdDetailsFromUDS(nsfId);
        if (pers == null || pers.getPersonnels().isEmpty()) {
            LOGGER.debug("No personnel found for proposal with NSF ID = " + nsfPropId);
            createRevision = false;
        }
        if (createRevision) {
            try {
                response = createPFURevision(propPrepId, propRevId, nsfPropId, pers.getPersonnels().get(0));
            } catch (CommonUtilException e) {
                throw new CommonUtilException(e);
            }
        } else {
            response.setSaveStatus(false);
            response.setMessages(msgs.getPsmMessages());
        }
        return response;
    }

    /*
     * 1) New ProposalRevision object with a new rev id and rev num (1 currently
     * byte>integer) 2) For each Section, create Section object 3) Insert header
     * 4) Insert sections 5) Insert warning messages
     */
    @Override
    public ProposalResponse createPFURevision(String propPrepId, String propRevId, String nsfPropId, Personnel person)
            throws CommonUtilException {

        PSMMessages msgs = new PSMMessages();
        ProposalCopy proposal = new ProposalCopy();
        ProposalResponse response = new ProposalResponse();

        ProposalPackage propPkg = proposalDataServiceClient.getProposalPrep(propPrepId, propRevId);
        Map<String, GrantApplicationListRowLite> gappsResults = new HashMap<String, GrantApplicationListRowLite>();
        GrantApplicationListRowLite grantApplication = null;
        if (!StringUtils.isEmpty(propPkg.getNsfPropId())) {
            LOGGER.debug(
                    "findGrantApplicationForANsfId, proposalNsfPropId: " + propPkg.getNsfPropId() + " proposalPiNsfId: "
                            + (!StringUtils.isEmpty(propPkg.getPi().getNsfId()) ? propPkg.getPi().getNsfId() : "")
                            + " loggedUserNsfId: " + person.getNsfId());
            Map<String, String> nsfIds = new HashMap<String, String>();
            if (!StringUtils.isEmpty(propPkg.getPi().getNsfId())) {
                nsfIds.put(propPkg.getPi().getNsfId(), propPkg.getPi().getNsfId());
            }
            if (!StringUtils.isEmpty(propPkg.getLatestSubmittedPiNsfId())) {
                nsfIds.put(propPkg.getLatestSubmittedPiNsfId(), propPkg.getLatestSubmittedPiNsfId());
            }
            for (Map.Entry<String, String> entry : nsfIds.entrySet()) {
                gappsResults.putAll(findGrantApplicationForANsfId(entry.getValue()));
            }
            if (gappsResults.containsKey(propPkg.getNsfPropId())) {
                grantApplication = gappsResults.get(propPkg.getNsfPropId());
            }
        }
        String revisionType = getRevisionTypeIfAvailableForRevision(grantApplication, propPkg);
        if (revisionType.equals(ProposalRevisionType.BUDGET_REVISION) && propPkg.isHasPFU()) {
            ProposalRevision proposalRevision = proposalDataServiceClient.getProposalRevision(propPkg.getPropPrepId(),
                    propPkg.getLatestPropRevId());
            if (proposalRevision.getRevisionType().getType().equals(ProposalRevisionType.PROPOSAL_FILE_UPDATE)) {
                propPkg.setPropPrepRevnTypeCode(proposalRevision.getRevisionType().getType());
                propPkg.setProposalStatus(ProposalStatus.CANNOT_SUBMIT_VIEW_ONLY);
                try {
                    proposalDataServiceClient.updateProposalRevisionStatus(proposalRevision.getPropRevId(), propPkg);
                } catch (CommonUtilException e) {
                    LOGGER.error("UpdateProposalStatus call failed for " + " revId " + proposalRevision.getPropRevId()
                            + " revisionType available " + revisionType, e);
                }
                propPkg = proposalDataServiceClient.getProposalPrep(propPrepId, propRevId);
            }
        }

        if (revisionType.equals(ProposalRevisionType.BUDGET_REVISION)) {
            proposal.setRevisionType(ProposalRevisionType.BUDGET_REVISION);
        } else if (revisionType.equals(ProposalRevisionType.PROPOSAL_FILE_UPDATE)) {
            proposal.setRevisionType(ProposalRevisionType.PROPOSAL_FILE_UPDATE);
        } else {
            PSMMessage psmMessage = new PSMMessage();
            psmMessage.setDescription("Proposal Status has changed please refresh your screen");
            msgs.addMessage(psmMessage);
        }
        proposal.setMaxRevisionNo(ProposalRevision.MAX_REV_NO_PFU);
        proposal.setNsfPropId(nsfPropId);
        proposal.setNsfId(person.getNsfId());

        ProposalPackage pkg = null;
        try {
            pkg = proposalDataServiceClient.createProposalRevision(proposal);
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }

        if (pkg != null) {
            if (pkg.getIsAvailableForRevision()) { // Create PFU section
                                                   // associations

                // Create non-personnel PFU sections
                msgs.addMessagesList(createNonPersonnelPFUSections(pkg, person).getPsmMessages());

                // Create personnel PFU sections
                for (ProposalRevisionPersonnel propRevPers : pkg.getPersonnelList()) {
                    msgs.addMessagesList(createPersonnelPFUSections(pkg, person, propRevPers.getPrevPersonnel(),
                            propRevPers.getRevPersonnel()).getPsmMessages());
                }

                response.setPropPrepId(pkg.getPropPrepId());
                response.setPropRevId(pkg.getPropRevId());
                proposalDataServiceClient.updateProposalRevisionCreateDate(pkg.getPropPrepId(), pkg.getPropRevId());

            } else {
                LOGGER.debug("PFU for proposal with NSF ID = " + nsfPropId + " already exists");
            }
        } else {
            LOGGER.debug("There was a problem creating a revision of proposal with NSF ID = " + nsfPropId);
        }

        response.setSaveStatus(msgs.getPsmMessages().isEmpty());
        response.setMessages(msgs.getPsmMessages());

        return response;

    }

    private PSMMessages createNonPersonnelPFUSections(ProposalPackage pkg, Personnel person)
            throws CommonUtilException {

        PSMMessages msgs = new PSMMessages();
        String propPrepId = pkg.getPropPrepId();
        String revId = pkg.getOrigPropRevId();

        Map<Section, UploadableProposalSection> sections = getAllNonPersonnelSectionsUploadedFilePaths(propPrepId,
                revId);

        for (Map.Entry<Section, UploadableProposalSection> entry : sections.entrySet()) {

            if (entry.getKey() != null) {
                UploadableProposalSection section = entry.getValue();
                GetFileResponse fileResponse = null;
                if (!StringUtils.isEmpty(section.getFilePath())) {
                    try {
                        LOGGER.info("File Path: " + section.getFilePath());
                        fileResponse = fileStorageServiceClient.getFile(section.getFilePath());
                    } catch (Exception e) {
                        LOGGER.error("createNonPersonnelPFUSections", e);
                    }
                }
                if (fileResponse != null && fileResponse.getFile().length > 0) {
                    Map<String, String> metaData = new LinkedHashMap<>();
                    metaData.put(Constants.ORIGINAL_FILENAME_METADATA_KEY, section.getOrigFileName());
                    if (person.getNsfId() != null) {
                        metaData.put(PropMgtUtil.NSFID, person.getNsfId());
                    } else {
                        metaData.put(PropMgtUtil.NSFID, "psm");
                    }

                    try {
                        String fileNameWithExt = section.getFilePath()
                                .substring(section.getFilePath().lastIndexOf("/") + 1, section.getFilePath().length());
                        String fileKey = propFileUtil.constructPSMFileKeyForSectionDocument(pkg.getPropPrepId(),
                                pkg.getPropRevId(), Section.getSection(section.getSectionCode()).getCamelCaseName(),
                                fileNameWithExt);
                        UploadFileResponse fsResp = fileStorageServiceClient.uploadFile(fileKey,
                                fileResponse.getFile());
                        if (fsResp != null && fsResp.getUploadSuccessful()) {
                            SectionResponse secResp = null;
                            Section sec = Section.getSection(section.getSectionCode());
                            switch (sec) {
                            case PROJ_SUMM:
                                ProjectSummary summary = new ProjectSummary();
                                PropMgtUtil.setAuditFields(summary, metaData.get(PropMgtUtil.NSFID));
                                summary.setBrodimpact(" ");
                                summary.setOverview(" ");
                                summary.setIntellmerit(" ");
                                summary.setLastUpdatedTmsp(pkg.getCreateDate());
                                List<PSMMessage> propMessages = getProposalWarningMessages(propPrepId, revId,
                                        Constants.PROP_PERS_ID, sec.getCode());
                                summary.setPropMessages(propMessages);
                                secResp = uploadProjectSummarySection(fsResp.getFilePath(),
                                        metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY), section.getPageCount(),
                                        pkg.getPropPrepId(), pkg.getPropRevId(), summary);
                                break;
                            case BUDGET_JUST:
                                BudgetJustification budgetJustification = new BudgetJustification();
                                budgetJustification.setLastUpdatedTmsp(pkg.getCreateDate());
                                PropMgtUtil.setAuditFields(budgetJustification, metaData.get(PropMgtUtil.NSFID));
                                List<String> keywordList = new ArrayList<>();
                                secResp = uploadBudgetJustificationSection(fsResp.getFilePath(),
                                        metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY), section.getPageCount(),
                                        pkg.getPropPrepId(), pkg.getPropRevId(), budgetJustification, keywordList);
                                break;
                            default:
                                PropMgtUtil.setAuditFields(section, metaData.get(PropMgtUtil.NSFID));
                                propMessages = getProposalWarningMessages(propPrepId, revId, Constants.PROP_PERS_ID,
                                        sec.getCode());
                                section.setPropMessages(propMessages);
                                section.setLastUpdatedTmsp(pkg.getCreateDate());
                                secResp = uploadUploadableSection(fsResp.getFilePath(),
                                        metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY), section.getPageCount(),
                                        pkg.getPropPrepId(), pkg.getPropRevId(), sec, section);
                                break;
                            }
                            if (secResp.getMessages() != null) {
                                msgs.addMessagesList(secResp.getMessages());
                            }
                        } else {
                            msgs.addMessage(PropMgtMessagesEnum.PM_E_002.getMessage());
                        }
                    } catch (Exception e) {
                        LOGGER.info(Arrays.toString(e.getStackTrace()));
                        throw new CommonUtilException(e);
                    }
                }
            }
        }

        return msgs;

    }

    private PSMMessages createPersonnelPFUSections(ProposalPackage pkg, Personnel person, Personnel pers,
            Personnel revPers) throws CommonUtilException {

        PSMMessages msgs = new PSMMessages();
        String propPrepId = pkg.getPropPrepId();
        String revId = pkg.getOrigPropRevId();

        Map<Section, UploadableProposalSection> sections = getAllPersonnelSectionsUploadedFilePaths(propPrepId, revId,
                pers.getPropPersId());

        for (Map.Entry<Section, UploadableProposalSection> entry : sections.entrySet()) {

            if (entry.getKey() != null) {
                UploadableProposalSection section = entry.getValue();
                GetFileResponse fileResponse = null;
                if (!StringUtils.isEmpty(section.getFilePath())) {
                    try {
                        LOGGER.debug("File Path: " + section.getFilePath());
                        fileResponse = fileStorageServiceClient.getFile(section.getFilePath());
                    } catch (Exception e) {
                        LOGGER.error("createPersonnelPFUSections", e.getMessage(), e);
                    }
                }
                if (fileResponse != null && fileResponse.getFile().length > 0) {
                    Map<String, String> metaData = new LinkedHashMap<>();
                    String newFileName = section.getOrigFileName();
                    metaData.put(Constants.ORIGINAL_FILENAME_METADATA_KEY, newFileName);
                    if (person.getNsfId() != null) {
                        metaData.put(PropMgtUtil.NSFID, person.getNsfId());
                    } else {
                        metaData.put(PropMgtUtil.NSFID, "psm");
                    }
                    String fileNameWithExt = section.getFilePath().substring(section.getFilePath().lastIndexOf("/") + 1,
                            section.getFilePath().length());
                    String fileKey = propFileUtil.constructPSMFileKeyForPersonnelDocument(pkg.getPropPrepId(),
                            pkg.getPropRevId(), Section.getSection(section.getSectionCode()).getCamelCaseName(),
                            revPers.getPropPersId(), fileNameWithExt);
                    UploadFileResponse fsResp = fileStorageServiceClient.uploadFile(fileKey, fileResponse.getFile());
                    if (fsResp != null && fsResp.getUploadSuccessful()) {
                        SectionResponse secResp = null;
                        Section sec = Section.getSection(section.getSectionCode());
                        LOGGER.debug("***** ProposalManagementServiceImpl.createPersonnelPFUSections()  -- PrepId : "
                                + propPrepId + " : RevId : " + revId + " : PersId : " + pers.getPropPersId()
                                + " : SecCode : " + sec.getCode());
                        List<PSMMessage> propMessages = getProposalWarningMessages(propPrepId, revId,
                                pers.getPropPersId(), sec.getCode());
                        switch (sec) {
                        case BIOSKETCH:
                            BiographicalSketch bioSketch = new BiographicalSketch();
                            bioSketch.setPropMessages(propMessages);
                            bioSketch.setLastUpdatedTmsp(pkg.getCreateDate());
                            PropMgtUtil.setAuditFields(bioSketch, metaData.get(PropMgtUtil.NSFID));
                            secResp = uploadSeniorPersonnelSection(fsResp.getFilePath(),
                                    metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY), section.getPageCount(),
                                    pkg.getPropPrepId(), revPers.getPropPersId(), pkg.getPropRevId(), bioSketch,
                                    Section.BIOSKETCH);
                            break;
                        case CURR_PEND_SUPP:
                            CurrentAndPendingSupport cps = new CurrentAndPendingSupport();
                            cps.setPropMessages(propMessages);
                            cps.setLastUpdatedTmsp(pkg.getCreateDate());
                            PropMgtUtil.setAuditFields(cps, metaData.get(PropMgtUtil.NSFID));
                            secResp = uploadSeniorPersonnelSection(fsResp.getFilePath(),
                                    metaData.get(Constants.ORIGINAL_FILENAME_METADATA_KEY), section.getPageCount(),
                                    pkg.getPropPrepId(), revPers.getPropPersId(), pkg.getPropRevId(), cps,
                                    Section.CURR_PEND_SUPP);
                            break;
                        case COA:
                            COA origCOA = entry.getValue().getCoa();
                            COA coa = PropCopyUtils.copyCOA(origCOA);
                            coa.setPropMessages(propMessages);
                            coa.setCoaExcelFilePath(fsResp.getFilePath());
                            String pdfFileNameWithExt = origCOA.getCoaPdfFilePath().substring(
                                    origCOA.getCoaPdfFilePath().lastIndexOf("/") + 1,
                                    origCOA.getCoaPdfFilePath().length());
                            String pdfFfileKey = propFileUtil.constructPSMFileKeyForPersonnelDocument(
                                    pkg.getPropPrepId(), pkg.getPropRevId(),
                                    Section.getSection(section.getSectionCode()).getCamelCaseName(),
                                    revPers.getPropPersId(), pdfFileNameWithExt);
                            GetFileResponse pdfFileResponse = fileStorageServiceClient
                                    .getFile(origCOA.getCoaPdfFilePath());
                            UploadFileResponse pdfFsResp = fileStorageServiceClient.uploadFile(pdfFfileKey,
                                    pdfFileResponse.getFile());
                            coa.setCoaPdfFilePath(pdfFsResp.getFilePath());
                            coa.setLastUpdatedTmsp(pkg.getCreateDate());
                            coa.setPropPrepRevId(Long.valueOf(pkg.getPropRevId()));
                            coa.setPropPersId(revPers.getPropPersId());
                            PropMgtUtil.setAuditFields(coa, metaData.get(PropMgtUtil.NSFID));
                            secResp = proposalDataServiceClient.updateProposalCOA(coa);
                            break;
                        default:
                            break;
                        }
                        if (secResp != null && secResp.getMessages() != null) {
                            msgs.addMessagesList(secResp.getMessages());
                        }
                    } else {
                        msgs.addMessage(PropMgtMessagesEnum.PM_E_002.getMessage());
                    }
                }
            }
        }

        return msgs;

    }

    private SectionCompliance getSectionCompliance(String propPrepId, String propPersId, String revisionId,
            Section section) throws CommonUtilException {

        // get warning/error messages
        List<PSMMessage> msgs = getProposalWarningMessages(propPrepId, revisionId, propPersId, section.getCode());
        SectionCompliance sectionCompliance = new SectionCompliance();
        if (msgs != null && !msgs.isEmpty()) {
            sectionCompliance.setNoOfErrors(SectionStatusUtils.getErrorStatusCount(msgs));
            sectionCompliance.setNoOfWarnings(SectionStatusUtils.getWarningStatusCount(msgs));
        }

        return sectionCompliance;

    }

    /**
     * This method returns Uploaded file paths/page counts for all Senior
     * Personals.
     *
     * @param propPrepId
     * @param propRevId
     * @return
     * @throws CommonUtilException
     */
    private List<SrPersonUploadData> getSrPersonUploadData(String propPrepId, String propRevId)
            throws CommonUtilException {
        List<SrPersonUploadData> srPersonUploadedDataList = new ArrayList<SrPersonUploadData>();

        List<Personnel> persList = getPersonnelsForValidate(propPrepId, propRevId);
        if (persList != null && !persList.isEmpty()) {
            for (Personnel pr : persList) {
                if (!"04".equals(pr.getPSMRole().getCode())) {
                    SrPersonUploadData srPerUploadData = new SrPersonUploadData();
                    // Pull Bio Sketches
                    BiographicalSketch bio = proposalDataServiceClient.getBiographicalSketch(propPrepId, propRevId,
                            pr.getPropPersId());
                    if (bio != null) {
                        bio.setPerson(pr);
                        srPerUploadData.setBioSketch(bio);
                    }

                    // pull Current & Pending
                    CurrentAndPendingSupport cp = proposalDataServiceClient.getCurrentAndPendingSupport(propPrepId,
                            propRevId, pr.getPropPersId());

                    if (cp != null) {
                        cp.setPerson(pr);
                        srPerUploadData.setCurrPendSupport(cp);
                    }

                    // COA
                    COA coa = proposalDataServiceClient.getProposalCOA(Long.valueOf(propRevId),
                            Long.valueOf(pr.getPropPersId()));
                    if (coa != null) {
                        coa.setPerson(pr);
                        srPerUploadData.setCoa(coa);
                    }
                    LOGGER.debug("srPersnData -" + srPerUploadData.toString());
                    srPersonUploadedDataList.add(srPerUploadData);
                }
            }
        }
        return srPersonUploadedDataList;
    }

    @Override
    public ProposalElectronicSign getAORSignature(String propPrepId, String propRevId) throws CommonUtilException {
        try {
            return proposalDataServiceClient.getAORSignature(propPrepId, propRevId);
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_ELECTRONIC_SIGNATURE_ERROR, e);
        }
    }

    @Override
    public SectionResponse submitProposal(ProposalElectronicSign proposalElectronicSign) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.submitProposal()");
        LOGGER.debug(
                "ProposalManagementServiceImpl.submitProposal():userIP:" + proposalElectronicSign.getUserIPAddress());

        /*
         * Steps:
         * 
         * 1. Submit Proposal (proposal data save) 2. Initiate Data Transfer
         * Request - convert to FL object - call pdt service 3. (depends on live
         * or batch) Retrieve and save nsf proposal id 4. (depends on live or
         * batch) Generate Submitted Static PDF and Upload to S3
         * 
         */
        Institution awardeeInstitution = null;
        final SectionResponse response;
        String revisionType = null;
        User user = null;
        try {
            user = userDatailsService.getUDSUserByNSFId(proposalElectronicSign.getUserNsfId());
            LOGGER.debug("submitProposal " + "-getUDSUserByNSFid" + " completed.");
            if (user != null) {
                proposalElectronicSign.setUserEmailAddress(user.getEmailId());
                proposalElectronicSign.setUserFirstName(user.getFirstName());
                proposalElectronicSign.setUserLastname(user.getLastName());
                proposalElectronicSign.setUserMiddleInit(user.getMiddleInitial());
                proposalElectronicSign.setUserPhoneNumber(user.getPhoneNumber());
            }

            proposalElectronicSign.setIpAddress(proposalElectronicSign.getUserIPAddress());
            proposalElectronicSign.setPiCopiList(getPiCoPiInformation(proposalElectronicSign.getPropPrepId(),
                    proposalElectronicSign.getPropRevId()));

            awardeeInstitution = externalServices.getInstitutionById(proposalElectronicSign.getInstitutionId());
            LOGGER.debug("submitProposal " + "-getInstitutionById" + " completed.");

            if (awardeeInstitution != null) {
                proposalElectronicSign.setAwardeeInstitution(awardeeInstitution);
            }
            // identity revision type based on what's changed in the proposal.
            revisionType = getRevisionType(proposalElectronicSign.getPropPrepId(),
                    proposalElectronicSign.getPropRevId());

            proposalElectronicSign.setPropPrepRevnTypeCode(revisionType);

            response = proposalDataServiceClient.submitProposal(proposalElectronicSign);
            LOGGER.debug("submitProposal " + "-submitProposal" + " completed.");
        } catch (Exception e) {
            throw new CommonUtilException(Constants.SAVE_SUBMIT_PROPOSAL_WITH_ELECTRONIC_SIGNATURE_ERROR, e);
        }

        // This happens in a separate thread
        Executors.newSingleThreadExecutor().submit(() -> {

            try {
                transferAndComplete(proposalElectronicSign.getPropPrepId(), proposalElectronicSign.getPropRevId(),
                        proposalElectronicSign.getUserNsfId());
            } catch (Exception ex) {
                LOGGER.error("Proposal Completion Failed: ", ex);
            }
        });

        return response;
    }

    private String getRevisionType(String propPrepId, String propRevId) throws CommonUtilException {
        String revisionType = null;

        ProposalRevision proposalRevision = proposalDataServiceClient.getProposalRevision(propPrepId, propRevId);

        if (proposalRevision != null) {
            revisionType = proposalRevision.getRevisionType().getType();
        }

        if (revisionType != null && !revisionType.equalsIgnoreCase(ProposalRevisionType.ORIGINAL_PROPOSAL)
                && !revisionType.equalsIgnoreCase(ProposalRevisionType.BUDGET_REVISION)) {
            boolean budgetUpdate = false;
            boolean proposalUpdate = false;
            Map<Section, ProposalSectionStatus> sectionStatusMap = getAllSectionStatuses(propPrepId, propRevId);
            for (Map.Entry<Section, ProposalSectionStatus> entry : sectionStatusMap.entrySet()) {
                if (entry.getValue().isSectionUpdated()) {
                    switch (entry.getKey()) {
                    case BUDGET_JUST:
                    case BUDI:
                    case BUDGETS:
                        LOGGER.debug("BudgetChanged " + propPrepId + "/" + propRevId + " " + entry.getKey());
                        budgetUpdate = true;
                        break;
                    case COVER_SHEET:
                    case BIOSKETCH:
                    case COA:
                    case CURR_PEND_SUPP:
                    case PROJ_SUMM:
                    case PROJ_DESC:
                    case RES_PRIOR_SUPP:
                    case REF_CITED:
                    case FER:
                    case DMP:
                    case COLLAB_PLAN:
                    case MGT_PLAN:
                    case PMP:
                    case DEV_AUTH:
                    case LOS:
                    case RIS:
                    case SRL:
                    case RNI:
                    case NNAE:
                    case OPBIO:
                    case SPD:
                    case OSD:
                        LOGGER.debug("ProposalFileChanged " + propPrepId + "/" + propRevId + " " + entry.getKey());
                        proposalUpdate = true;
                        break;
                    default:
                        LOGGER.debug("Not identified for ProposalFile or Budget changes " + propPrepId + "/" + propRevId
                                + " " + entry.getKey());
                        break;
                    }
                }
            }
            /* if personnel is updated, set proposal update to true */
            Boolean isPersonnelUpdated = proposalDataServiceClient.getPersonnelRevnUpdateStatus(propPrepId, propRevId);
            LOGGER.debug("isPersonnelUpdated " + propPrepId + "/" + propRevId + " " + isPersonnelUpdated);
            if (isPersonnelUpdated) {
                proposalUpdate = true;
            }

            if (budgetUpdate) {
                LOGGER.debug("budgetUpdate " + propPrepId + "/" + propRevId + " " + budgetUpdate);
                revisionType = ProposalRevisionType.BUDGET_UPDATE;
            }
            if (proposalUpdate) {
                LOGGER.debug("proposalUpdate " + propPrepId + "/" + propRevId + " " + budgetUpdate);
                revisionType = ProposalRevisionType.PROPOSAL_FILE_UPDATE;
            }
            if (budgetUpdate && proposalUpdate) {
                LOGGER.debug("proposalUpdate/budgetUpdate " + propPrepId + "/" + propRevId + " " + budgetUpdate + "/"
                        + proposalUpdate);
                revisionType = ProposalRevisionType.PROPOSAL_UPDATE;
            }
        }
        return revisionType;
    }

    @Override
    public SectionResponse transferAndComplete(String propPrepId, String propRevId, String userNsfId)
            throws CommonUtilException {

        if (!enableProposalDataTransfer) {
            return null;
        }
        ProposalCompleteTransfer proposalCompleteTransfer = new ProposalCompleteTransfer();
        try {
            ProposalTransferRequest transferRequest = proposalManagementForTransferService
                    .transferToFastLane(propPrepId, propRevId, userNsfId);

            proposalCompleteTransfer.setNsfPropId(transferRequest.getFastLaneProposalId());
            proposalCompleteTransfer.setTempPropId(transferRequest.getFastLaneProposalTempId());
            proposalCompleteTransfer.setLastUpdatedUser(userNsfId);

            return this.completeTransfer(propPrepId, propRevId, proposalCompleteTransfer);

        } catch (Exception ex) {
            throw new CommonUtilException("Proposal Transfer Completion Failed: ", ex);
        }
    }

    @Override
    public SectionResponse retryTransferAndComplete(String propPrepId, String propRevId, String userNsfId)
            throws CommonUtilException {
        if (!enableProposalDataTransfer) {
            return null;
        }

        ProposalCompleteTransfer proposalCompleteTransfer = new ProposalCompleteTransfer();
        try {
            ProposalTransferRequest transferRequest = proposalManagementForTransferService
                    .manuallyRetryTransfer(propPrepId, propRevId, userNsfId);

            proposalCompleteTransfer.setNsfPropId(transferRequest.getFastLaneProposalId());
            proposalCompleteTransfer.setTempPropId(transferRequest.getFastLaneProposalTempId());
            proposalCompleteTransfer.setLastUpdatedUser(userNsfId);

            return this.completeTransfer(propPrepId, propRevId, proposalCompleteTransfer);

        } catch (Exception ex) {
            throw new CommonUtilException("Proposal Transfer Completion Failed: ", ex);
        }
    }

    private List<PiCoPiInformation> getPiCoPiInformation(String propPrepId, String propRevId)
            throws CommonUtilException {
        CoverSheet cv = null;
        try {
            cv = proposalDataServiceClient.getCoverSheet(propPrepId, propRevId);

        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_COVERSHEET_ERROR, e);
        }
        List<PiCoPiInformation> piCopiList = cv.getPiCopiList();
        setPiCopiInformation(piCopiList);

        return piCopiList;

    }

    @Override
    public CoverSheet getCoverSheetForElectronicSign(String propPrepId, String propRevId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.getCoverSheetForElectronicSign()");
        CoverSheet cv = null;
        String awdOrganizationId = null;
        Institution awdOrganization = null;
        InstitutionBudget institutionBudget = null;
        try {
            cv = proposalDataServiceClient.getCoverSheet(propPrepId, propRevId);
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_COVERSHEET_ERROR, e);
        }
        cv.setPropPrepId(propPrepId);
        cv.setPropRevId(propRevId);

        try {
            awdOrganizationId = proposalDataServiceClient.getPrimaryAwardeeOrganizationId(propPrepId, propRevId)
                    .getAwdOrgId();
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_PRIMARY_AWARDEE_ORGANIZATION_ID_ERROR, e);
        }

        if (!StringUtils.isEmpty(awdOrganizationId)) {
            try {
                awdOrganization = externalServices.getInstitutionById(awdOrganizationId);
            } catch (Exception e) {
                throw new CommonUtilException(Constants.GET_INSTITUTION_DATA_FROM_REF_DATA_SERVICE_ERROR, e);
            }

            if (awdOrganization != null) {
                if (awdOrganization.getAddress().getPostalCode() != null) {
                    awdOrganization.getAddress()
                            .setPostalCode(formatPostalCode(awdOrganization.getAddress().getPostalCode()));
                }
                cv.setAwdOrganization(awdOrganization);
            }
        }

        List<PiCoPiInformation> piCopiList = cv.getPiCopiList();
        setPiCopiInformation(piCopiList);
        try {
            institutionBudget = proposalDataServiceClient.getInstitutionBudget(propPrepId, propRevId,
                    awdOrganizationId);
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_INSTITUTION_BUDGET_ERROR, e);
        }

        BudgetTotals bt = new BudgetTotals();
        String totalRequtestedDollars = formatDollorAmount(
                bt.getRequestedTotalAmountsForThisProposal(institutionBudget.getBudgetRecordList())
                        .getAmountOfThisRequest());
        cv.setTotalRequtestedDollars(totalRequtestedDollars);

        return cv;
    }

    public static String formatDollorAmount(BigDecimal amount) {
        NumberFormat nfm = NumberFormat.getNumberInstance(Locale.US);
        return nfm.format(amount.setScale(0, RoundingMode.HALF_UP));
    }

    private DeadlineData getDeadlineDates(List<Proposal> propResults) throws CommonUtilException {

        try {
            // Extract funding op ids
            Set<String> opIds = propResults.stream().map(Proposal::getFundingOpId).collect(Collectors.toSet());
            List<String> fundingOpportunityIds = new ArrayList<>();
            fundingOpportunityIds.addAll(opIds);
            FundingOpportunityParams params = new FundingOpportunityParams();
            params.setFundingOpportunityIds(fundingOpportunityIds);
            DeadlineData data = solicitationDataServiceclient.getDueDatesForFundingOps(params);
            if (data != null) {
                LOGGER.debug("Data was returned by the Solicitation Data Service for " + data.getDeadlineMap().size()
                        + " organizations");
            } else {
                LOGGER.debug("No data was returned by the Solicitation Data Service");
            }
            return data;

        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }

    private ProposalQueryResult updateProposalQueryResult(Proposal prop, ProposalQueryResult result, DeadlineData data)
            throws CommonUtilException {
        try {
            Deadlines deadlines = data.getDeadlineMap().get(prop.getFundingOpId());
            if (deadlines != null && !deadlines.getDeadlines().isEmpty()) {
                List<Deadline> dueDates = deadlines.getDeadlines();
                if (dueDates != null && !dueDates.isEmpty()) {
                    if (prop.getDeadlineDate() != null) {
                        if (!PropMgtUtil.isWithinWindowOfOpportunity(prop.getDeadlineDate())) {
                            result.setDeadlineTypeText(Constants.PROPOSAL_QUERY_NO_DATES_MSG);
                            result.setIsAvailableForRevision(false);
                        }
                    } else {
                        result.setDeadlineTypeText(Constants.PROPOSAL_QUERY_NO_DATES_SELECTED_MSG);
                        result.setIsAvailableForRevision(false);
                    }
                } else {
                    result.setDeadlineTypeText(Constants.PROPOSAL_QUERY_NO_DATES_MSG);
                    result.setIsAvailableForRevision(false);
                }
            } else {
                result.setDeadlineTypeText(Constants.PROPOSAL_QUERY_NO_DATES_MSG);
                result.setIsAvailableForRevision(false);
            }
            return result;
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }
    }

    @Override
    public ProposalPackage getProposal(String propPrepId, String propRevId, String nsfId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.getProposal()");

        ProposalPackage proposalPkg = null;
        User user = null;

        try {
            proposalPkg = getProposal(propPrepId, propRevId);
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_PROPOSAL_ERROR, e);
        }

        try {
            user = userDatailsService.getUDSUserByNSFId(nsfId);
            if (user != null) {

                proposalPkg.setAorName(PropMgtUtil.formatAORName(user));
                proposalPkg.setAorEmail(user.getEmailId());
                proposalPkg.setAorPhoneNumber(formatPhoneNumber(user));

                List<Personnel> personnelList = new ArrayList<>();
                Personnel personnel = new Personnel();
                personnel.setFirstName(user.getFirstName());
                personnel.setMiddleName(user.getMiddleInitial());
                personnel.setLastName(user.getLastName());
                personnelList.add(personnel);
                proposalPkg.setPersonnel(personnelList);

            }
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_USER_DATA_FROM_UDS_ERROR, e);
        }

        return proposalPkg;
    }

    private static String formatPhoneNumber(User user) {
        if (user.getPhoneNumber() != null) {
            return String.valueOf(user.getPhoneNumber()).replaceFirst("(\\d{3})(\\d{3})(\\d+)", "$1-$2-$3");
        } else
            return user.getPhoneNumber();

    }

    @Override
    public ElectronicCertificationText getElectronicCertificationText(String electronicCertTypeCode)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.getElectronicCertificationText()");

        try {
            return proposalDataServiceClient.getElectronicCertificationText(electronicCertTypeCode);
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_ELECTRONIC_CERTIFICATION_TEXT_ERROR, e);
        }

    }

    @Override
    public SectionResponse saveProposalUpdateJustification(ProposalUpdateJustification proposalUpdateJustification)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.saveProposalUpdateJustification()");

        try {
            SectionResponse response = new SectionResponse();

            ProposalUpdateJustificationFactModel propUpdJustFactModel = new ProposalUpdateJustificationFactModel();

            propUpdJustFactModel.setPropPrepId(proposalUpdateJustification.getPropPrepId());
            propUpdJustFactModel.setPropRevId(proposalUpdateJustification.getPropRevId());
            propUpdJustFactModel.setJustificationText(proposalUpdateJustification.getJustificationText());

            LOGGER.debug("Proposal Update Justification FactModel:" + propUpdJustFactModel.toString());
            List<gov.nsf.psm.factmodel.PSMMessage> msgs = new ArrayList<>();
            msgs.addAll(complianceValidationServiceClient
                    .getProposalFileUpdateComplianceCheckFindings(propUpdJustFactModel));
            List<PSMMessage> validationMsgs = new ArrayList<>();
            for (gov.nsf.psm.factmodel.PSMMessage msg : msgs) {
                PSMMessage message = new PSMMessage();
                message.setDescription(msg.getDescription());
                message.setId(msg.getId());
                message.setType(PSMMessageType.getMessageType(msg.getType().getCode()));
                message.setSectionCode(msg.getSectionCode());

                validationMsgs.add(message);
            }

            if (validationMsgs.isEmpty()) {
                response = proposalDataServiceClient.saveProposalUpdateJustification(proposalUpdateJustification);
            }
            response.setMessages(validationMsgs);
            return response;
        } catch (Exception e) {
            throw new CommonUtilException(Constants.SAVE_PROPOSAL_UPDATE_JUSTIFICATIOIN_ERROR, e);
        }

    }

    @Override
    public SectionResponse completeTransfer(String propPrepId, String revId,
            ProposalCompleteTransfer proposalCompleteTransfer) throws CommonUtilException {
        LOGGER.debug("completeTransfer prepId/revId/nsfId :" + propPrepId + "/" + revId + "/"
                + proposalCompleteTransfer.getNsfPropId());
        // Update NsfPropId
        ProposalPackage prop = getProposal(propPrepId, revId, proposalCompleteTransfer.getNsfPropId());
        if (ProposalRevisionType.ORIGINAL_PROPOSAL.equalsIgnoreCase(prop.getPropPrepRevnTypeCode().trim())) {
            proposalDataServiceClient.updateNsfPropId(propPrepId, revId, proposalCompleteTransfer);
            prop.setNsfPropId(proposalCompleteTransfer.getNsfPropId());
        }
        // create Complete Proposal PDF
        List<Section> secPrintOrderList = PropMgtUtil.getFullProposalPrintOrder();
        PdfGenerationData pdf = new PdfGenerationData();
        Map<Section, UploadableProposalSection> sectionPdMap = new LinkedHashMap<Section, UploadableProposalSection>();
        pdf.setProp(prop);
        for (Section sec : secPrintOrderList) {
            ByteArrayOutputStream baos = getSubmittedProposalPdf(null, null, sec.getCode(), propPrepId, revId);
            UploadableProposalSection ups = new UploadableProposalSection();
            ups.setFile(baos.toByteArray());
            sectionPdMap.put(sec, ups);
        }
        pdf.setSectionPdfMap(sectionPdMap);
        GetGeneratedDocumentResponse response = documentGenerationServiceClient.getProposalDocument(pdf);
        byte[] getProposalFile = response.getFile();
        // upload complete proposal PDF into S3
        String fileKey = propFileUtil.constructPSMFileKeyForSectionDocument(propPrepId, revId,
                Section.SUBMITTED_PROPOSAL.getCamelCaseName(), "generatedProposalFileName" + propPrepId + ".pdf");
        UploadFileResponse fsResp = fileStorageServiceClient.uploadFile(fileKey, getProposalFile);
        proposalCompleteTransfer.setS3FilePath(fsResp.getFilePath());
        LOGGER.debug("uploaded filePath " + fsResp.getFilePath());
        SectionResponse sectionResponse = proposalDataServiceClient.updateProposalStaticPathUrl(propPrepId, revId,
                proposalCompleteTransfer);
        LOGGER.debug("propPrepId: " + propPrepId + " propRevId: " + revId + " nsfPropId:"
                + proposalCompleteTransfer.getNsfPropId() + " updateStatus: " + sectionResponse.getSaveStatus());

        // Generate email
        if (sectionResponse.getSaveStatus()) {
            try {
                sendEmailMessagesSubmitProposal(prop);
            } catch (Exception e) {
                LOGGER.error("Email notifications could not be sent for submitted proposal with prop_prep_id = "
                        + prop.getPropPrepId(), e);
            }
        }

        return sectionResponse;
    }

    @Override
    public ProposalUpdateJustification getProposalUpdateJustification(String propPrepId, String propRevId)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.getProposalUpdateJustification()");
        try {
            return proposalDataServiceClient.getProposalUpdateJustification(propPrepId, propRevId);
        } catch (Exception e) {
            throw new CommonUtilException(Constants.GET_PROPOSAL_UPDATE_JUSTIFICATIOIN_ERROR, e);
        }
    }

    private List<PSMMessage> sendEmailMessagesPersonnel(Personnel personnel, EmailMessageType type)
            throws CommonUtilException {

        try {
            List<PSMMessage> msgs = new ArrayList<>();
            if (personnel != null) {
                PSMRole role = personnel.getPSMRole();
                if (role != null && (role.getCode().equalsIgnoreCase(PSMRole.ROLE_CO_PI)
                        || role.getCode().equalsIgnoreCase(PSMRole.ROLE_OAU)
                        || role.getCode().equalsIgnoreCase(PSMRole.ROLE_OSP))) {
                    EmailMessageRequest eMsgRequest = new EmailMessageRequest();
                    eMsgRequest.setPropPrepId(personnel.getPropPrepId());
                    eMsgRequest.setPropPrepRevnId(personnel.getPropRevnId());
                    List<Personnel> persons = new ArrayList<>();
                    persons.add(personnel);
                    eMsgRequest.setPersonnelList(persons);
                    eMsgRequest.setEmailMessageType(type);
                    return sendEmailMessages(eMsgRequest);
                }
            }
            return msgs;
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }

    }

    private List<PSMMessage> sendEmailMessagesSubmitProposal(ProposalPackage prop) throws CommonUtilException {

        try {
            List<PSMMessage> msgs = new ArrayList<>();
            if (prop != null) {
                ProposalElectronicSign sign = proposalDataServiceClient.getAORSignature(prop.getPropPrepId(),
                        prop.getPropRevId());
                Personnel personnel = new Personnel();
                personnel.setFirstName(sign.getUserFirstName());
                personnel.setMiddleName(sign.getUserMiddleInit());
                personnel.setLastName(sign.getUserLastname());
                personnel.setEmail(sign.getUserEmailAddress());
                List<Personnel> personnelList = new ArrayList<>();
                personnelList.add(personnel);
                EmailMessageRequest eMsgRequest = new EmailMessageRequest();
                eMsgRequest.setPropPrepId(prop.getPropPrepId());
                eMsgRequest.setPropPrepRevnId(prop.getPropRevId());
                eMsgRequest.setPersonnelList(personnelList);
                eMsgRequest.setInstitutionName(prop.getInstitution().getOrganizationName());
                if (!StringUtils.isEmpty(prop.getPropPrepRevnTypeCode())) {
                    switch (prop.getPropPrepRevnTypeCode()) {
                    case ProposalRevisionType.ORIGINAL_PROPOSAL:
                        eMsgRequest
                                .setEmailMessageType(new EmailMessageType(EmailMessageType.EMAIL_MSG_TYPE_SUBMIT_ORIG));
                        msgs = sendEmailMessages(eMsgRequest);
                        break;
                    case ProposalRevisionType.PROPOSAL_FILE_UPDATE:
                        if (prop.getInstitution() != null) {
                            prop.getInstitution().setTimeZone("EST"); // Temporary
                                                                      // fix
                                                                      // until
                                                                      // time
                                                                      // zone
                                                                      // issue
                                                                      // can be
                                                                      // ironed
                                                                      // out
                            LOGGER.info(
                                    "ProposalManagementServiceImpl.sendEmailMessagesSubmitProposal() :: Institution Time Zone = "
                                            + prop.getInstitution().getTimeZone());
                        } else {
                            LOGGER.info(
                                    "ProposalManagementServiceImpl.sendEmailMessagesSubmitProposal() :: Institution is unavailable");
                        }
                        if (!EmailUtils.isAfterDeadlineDate(prop)) {
                            eMsgRequest.setEmailMessageType(
                                    new EmailMessageType(EmailMessageType.EMAIL_MSG_TYPE_SUBMIT_PFU_AA));
                        } else {
                            String justificationText = getProposalUpdateJustification(prop.getPropPrepId(),
                                    prop.getPropRevId()).getJustificationText();
                            eMsgRequest.setJustificationText(justificationText);
                            eMsgRequest.setEmailMessageType(
                                    new EmailMessageType(EmailMessageType.EMAIL_MSG_TYPE_SUBMIT_PFU_MA));
                            msgs = sendEmailMessages(eMsgRequest);
                            eMsgRequest.setEmailMessageType(
                                    new EmailMessageType(EmailMessageType.EMAIL_MSG_TYPE_UPDATE_PFU_MA));
                            Personnel pi = new Personnel();
                            UdsNsfIdUserData userData = null;
                            UdsGetUserDataResponse userResponse = null;
                            try {
                                userResponse = cachedDataService.getUserData(prop.getPi().getNsfId());
                                if (userResponse != null && userResponse.getUserData() != null) {
                                    userData = userResponse.getUserData();
                                }
                                if (userData != null) {
                                    if (!StringUtils.isEmpty(userData.getFirstName())) {
                                        pi.setFirstName(userData.getFirstName());
                                    }
                                    if (!StringUtils.isEmpty(userData.getMiddleInitial())) {
                                        pi.setMiddleName(userData.getMiddleInitial());
                                    }
                                    if (!StringUtils.isEmpty(userData.getLastName())) {
                                        pi.setLastName(userData.getLastName());
                                    }
                                    if (!StringUtils.isEmpty(userData.getEmail())) {
                                        pi.setEmail(userData.getEmail());
                                    }
                                } else {
                                    pi.setFirstName(prop.getPi().getFirstName());
                                    pi.setMiddleName(prop.getPi().getMiddleName());
                                    pi.setLastName(prop.getPi().getLastName());
                                    pi.setEmail(prop.getPi().getEmail());
                                }
                            } catch (Exception e) {
                                throw new CommonUtilException(String.format(Constants.GET_SERVICE_IS_DOWN_ERROR, "UDS"),
                                        e);
                            }
                            if (userResponse == null) {
                                pi.setFirstName(prop.getPi().getFirstName());
                                pi.setMiddleName(prop.getPi().getMiddleName());
                                pi.setLastName(prop.getPi().getLastName());
                                pi.setEmail(prop.getPi().getEmail());
                            }
                            eMsgRequest.setPi(pi);
                            eMsgRequest.setAor(personnel);
                            eMsgRequest.setPersonnelList(personnelList);
                            GrantApplication ga = getGrantApplicationDetailsFromGapps(prop.getLatestSubmittedPiNsfId(),
                                    prop.getNsfPropId());
                            if (ga != null) {
                                List<ProgramOfficer> gappsProgramOfficers = ga.getProgram().getProgramOfficers();
                                if (!gappsProgramOfficers.isEmpty()) {
                                    ProgramOfficer programOfficer0 = gappsProgramOfficers.get(0);
                                    if (!StringUtils.isEmpty(programOfficer0.getEmailAddress())
                                            && !NOT_AVAILABLE.equalsIgnoreCase(programOfficer0.getEmailAddress())
                                            && !UNASSIGNED.equalsIgnoreCase(programOfficer0.getEmailAddress())) {
                                        prop.setProgramOfficerEmail(programOfficer0.getEmailAddress());
                                    }
                                    if (!StringUtils.isEmpty(programOfficer0.getFullName())
                                            && !NOT_AVAILABLE.equalsIgnoreCase(programOfficer0.getFullName())
                                            && !UNASSIGNED.equalsIgnoreCase(programOfficer0.getFullName())) {
                                        prop.setProgramOfficerName(programOfficer0.getFullName());
                                    }
                                    if (!StringUtils.isEmpty(programOfficer0.getPhoneNumber())
                                            && !NOT_AVAILABLE.equalsIgnoreCase(programOfficer0.getPhoneNumber())
                                            && !UNASSIGNED.equalsIgnoreCase(programOfficer0.getPhoneNumber())) {
                                        prop.setProgramOfficerPhoneNumber(programOfficer0.getPhoneNumber());
                                    }
                                } else {
                                    LOGGER.debug(
                                            "ProposalManagementServiceImpl.sendEmailMessagesSubmitProposal() :: No GA program officers are available");
                                }
                            }
                            if (!StringUtils.isEmpty(prop.getProgramOfficerEmail())) {
                                eMsgRequest.setPoEmailAddress(prop.getProgramOfficerEmail());
                            } else {
                                LOGGER.debug(
                                        "ProposalManagementServiceImpl.sendEmailMessagesSubmitProposal() :: No GA program officer email address was returned");
                            }
                            if (!StringUtils.isEmpty(prop.getProgramOfficerName())) {
                                eMsgRequest.setPoName(prop.getProgramOfficerName());
                            } else {
                                LOGGER.debug(
                                        "ProposalManagementServiceImpl.sendEmailMessagesSubmitProposal() :: No GA program officer name was returned");
                            }
                        }
                        msgs.addAll(sendEmailMessages(eMsgRequest));
                        break;
                    case ProposalRevisionType.BUDGET_REVISION:
                        eMsgRequest
                                .setEmailMessageType(new EmailMessageType(EmailMessageType.EMAIL_MSG_TYPE_SUBMIT_BR));
                        msgs = sendEmailMessages(eMsgRequest);
                        break;
                    default:
                        // Empty
                    }
                } else {
                    LOGGER.info("An email could not be sent because a revision type code as not returned for proposal "
                            + prop.getPropPrepId());
                }

                return msgs;
            }
            return msgs;
        } catch (Exception e) {
            throw new CommonUtilException(e);
        }

    }

    private List<PSMMessage> sendEmailMessages(EmailMessageRequest eMsgRequest) throws CommonUtilException {
        List<PSMMessage> msgs = new ArrayList<>();
        try {
            msgs = emailService.sendEmailMessages(eMsgRequest);
        } catch (Exception e) {
            LOGGER.error("WARNING: AN EMAIL FOR PROPOSAL REVISION WITH ID = " + eMsgRequest.getPropPrepRevnId()
                    + " COULD NOT BE SENT.  THE EMAIL SERVICE MAY BE UNAVAILABLE", e);
        }
        return msgs;
    }

    private Map<String, List<ProposalReview>> getProposalReviewers(List<Proposal> propResults)
            throws CommonUtilException {
        Map<String, List<ProposalReview>> proposalReviewers = new HashMap<String, List<ProposalReview>>();
        List<String> submittedProposalIds = new ArrayList<String>();
        for (Proposal prop : propResults) {
            if (!StringUtils.isEmpty(prop.getNsfPropId())) {
                submittedProposalIds.add(prop.getNsfPropId());
            }
        }
        ProposalReviewWrapper proposalReviewWrapper = externalServices
                .getProposalReviewers(submittedProposalIds.toArray(new String[submittedProposalIds.size()]));
        if (proposalReviewWrapper != null) {
            for (ProposalReview proposalReview : proposalReviewWrapper.getProposalReviews()) {
                if (!proposalReviewers.containsKey(proposalReview.getProposal())) {
                    proposalReviewers.put(proposalReview.getProposal(), new ArrayList<ProposalReview>());
                }
                proposalReviewers.get(proposalReview.getProposal()).add(proposalReview);
            }
        }
        return proposalReviewers;
    }

    private Map<String, GrantApplicationListRowLite> findGrantApplicationForANsfId(String nsfId)
            throws CommonUtilException {
        Map<String, GrantApplicationListRowLite> gappsResults = new HashMap<String, GrantApplicationListRowLite>();
        GrantApplicationRequest grantApplicationRequest = new GrantApplicationRequest();
        grantApplicationRequest.setAgency("ALL");
        GappsSearchRequest gappsSearchRequest = new GappsSearchRequest();
        gappsSearchRequest.setUserId(nsfId);
        gappsSearchRequest.setGrantApplicationRequest(grantApplicationRequest);
        GappsSearchResponse gappsSearchResponse = externalServices.findGrantApplications(gappsSearchRequest);
        for (GrantApplicationListRowLite application : gappsSearchResponse.getGrantApplications()) {
            gappsResults.put(application.getGrantApplicationId(), application);
        }
        return gappsResults;
    }

    private GrantApplication getGrantApplicationDetailsFromGapps(String nsfId, String applicationId) {
        GrantApplication application = null;
        try {
            GappsDetailRequest gappsDetailRequest = new GappsDetailRequest();
            gappsDetailRequest.setAgencyId("NSF");
            gappsDetailRequest.setApplicationId(applicationId);
            gappsDetailRequest.setUserId(nsfId);
            GappsDetailResponse gappsDetailResponse = externalServices.getGrantApplicationDetails(gappsDetailRequest);
            application = gappsDetailResponse.getGrantApplication();
        } catch (Exception e) {
            LOGGER.error(String.format(Constants.PROPOSAL_STATUS_NOT_AVAILABLE, nsfId + "/" + applicationId, e));
        }
        return application;
    }

    /**
     * This method is useful to pull the Prop Prep Id & Prop Rev Id based on
     * FastLane Tpi.
     * 
     * @param nsfTempPropId
     * @return
     * @throws CommonUtilException
     */
    private ProposalPackage getPrepRevIdWithNsfTemporaryProposalId(String nsfTempPropId) throws CommonUtilException {
        ProposalPackage propPackage = new ProposalPackage();
        Map<String, Object> searchCriteria = new HashMap<String, Object>();
        searchCriteria.put(Constants.FL_TPI, nsfTempPropId);
        searchCriteria.put(Constants.FL_TRANFER_STATUS, Constants.FL_TRANFER_COMPLETED);
        gov.nsf.components.rest.model.response.CollectionResponse<ProposalTransferRequest> tranReq = null;
        try {
            tranReq = proposalTransferServiceClient.getTransferRequests(searchCriteria, null);
        } catch (ProposalTransferRequestException ex) {
            throw new CommonUtilException("An error occurred looking up the PDT record", ex);
        }

        if (tranReq.hasData()) {
            ProposalTransferRequest transferRequest = tranReq.getData().iterator().next();
            propPackage.setPropPrepId(transferRequest.getProposalPreparationId());
            propPackage.setPropRevId(transferRequest.getProposalPreparationRevisionId());
        }
        return propPackage;
    }

    /**
     * This method will check wheather the proposal transfered successfully or
     * not.
     * 
     * @param prepId
     * @param revId
     * @return
     * @throws CommonUtilException
     */
    private boolean isProposalTranferComplete(String prepId, String revId) throws CommonUtilException {
        boolean status = false;

        LOGGER.debug("*********isProposalTranferComplete --> prepId : " + prepId + " revId :" + revId);
        Map<String, Object> searchCriteria = new HashMap<String, Object>();
        searchCriteria.put(RequestParams.PROPOSAL_PREPARATION_ID, prepId);
        searchCriteria.put(RequestParams.PROPOSAL_PREPARATION_REV_ID, revId);
        searchCriteria.put(RequestParams.REQUEST_STATUS, Constants.FL_TRANFER_COMPLETED);
        gov.nsf.components.rest.model.response.CollectionResponse<ProposalTransferRequest> tranReq = null;
        try {
            tranReq = proposalTransferServiceClient.getTransferRequests(searchCriteria, null);
            if (tranReq.hasData()) {
                ProposalTransferRequest transferRequest = tranReq.getData().iterator().next();
                LOGGER.debug("*********Transfer Status : " + transferRequest.toString());
                status = true;
            }
        } catch (ProposalTransferRequestException ex) {
            throw new CommonUtilException("An error occurred looking up the PDT record", ex);
        }
        return status;
    }

    @Override
    public ByteArrayOutputStream getEntirePdfForSubmittedProposal(String nsfTempPropId) throws CommonUtilException {
        LOGGER.debug(
                "ProposalManagementServiceImpl.getEntirePdfForSubmittedProposal()  nsfTempPropId : " + nsfTempPropId);
        ProposalPackage propPack = getPrepRevIdWithNsfTemporaryProposalId(nsfTempPropId);
        return getProposalFile(propPack.getPropPrepId(), propPack.getPropRevId());
    }

    @Override
    public ByteArrayOutputStream getSubmittedProposalPdfForExternalUsers(String nsfTempPropId)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceImpl.getSubmittedProposalPdfForExternalUsers()  nsfTempPropId : "
                + nsfTempPropId);
        ProposalPackage propPack = getPrepRevIdWithNsfTemporaryProposalId(nsfTempPropId);
        String propPrepId = propPack.getPropPrepId();
        String revisionId = propPack.getPropRevId();

        LOGGER.debug("ProposalManagementServiceImpl.getProposalFile() propPrepId : " + propPrepId + "  revisionId : "
                + revisionId);
        if (propPrepId != null && revisionId != null) {
            PdfGenerationData pdf = getPdfGenerationData(propPrepId, revisionId);
            GetGeneratedDocumentResponse response = documentGenerationServiceClient.getProposalDocument(pdf);
            byte[] getProposalFile = response.getFile();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(getProposalFile.length);
            byteArrayOutputStream.write(getProposalFile, 0, getProposalFile.length);
            return byteArrayOutputStream;
        }
        return new ByteArrayOutputStream();
    }

    @Override
    public ByteArrayOutputStream getProposalPdf(String nsfPropId) throws CommonUtilException {
        SubmittedProposal subProp = proposalDataServiceClient.getSubmittedProposal(nsfPropId);
        String staticPdfPath = subProp.getStaticPdfPath();
        LOGGER.debug("*********** staticPdfPath : " + subProp + " ******************");
        if (org.apache.commons.lang3.StringUtils.isNotBlank(staticPdfPath)) {
            GetFileResponse resp = fileStorageServiceClient.getFile(staticPdfPath);
            byte[] file = resp.getFile();
            ByteArrayOutputStream entirePdf = new ByteArrayOutputStream(file.length);
            entirePdf.write(file, 0, file.length);
            return entirePdf;

        }
        return new ByteArrayOutputStream();
    }

    private String getRevisionTypeIfAvailableForRevision(GrantApplicationListRowLite grantApplication,
            ProposalPackage propPkg) throws CommonUtilException {
        LOGGER.debug("getRevisionTypeIfAvailableForRevision executed");

        if (grantApplication != null && (GappsStatuses.getStatus(grantApplication.getStatusCode())
                .equalsIgnoreCase(ProposalStatus.GAPPS_AWARDED)
                || GappsStatuses.getStatus(grantApplication.getStatusCode())
                        .equalsIgnoreCase(ProposalStatus.GAPPS_DECLINED)
                || GappsStatuses.getStatus(grantApplication.getStatusCode())
                        .equalsIgnoreCase(ProposalStatus.GAPPS_RETURNED)
                || GappsStatuses.getStatus(grantApplication.getStatusCode())
                        .equalsIgnoreCase(ProposalStatus.GAPPS_WITHDRAWN))) {
            LOGGER.debug("getRevisionTypeIfAvailableForRevision grantApplication Status " + propPkg.getNsfPropId() + " "
                    + grantApplication.getStatusCode() + " "
                    + GappsStatuses.getStatus(grantApplication.getStatusCode()));
            return "";
        }

        if (grantApplication != null && (GappsStatuses.getStatus(grantApplication.getStatusCode())
                .equalsIgnoreCase(ProposalStatus.GAPPS_RECOMMENDED)
                || GappsStatuses.getStatus(grantApplication.getStatusCode())
                        .equalsIgnoreCase(ProposalStatus.GAPPS_PENDING))) {
            LOGGER.debug("getRevisionTypeIfAvailableForRevision grantApplication Status " + propPkg.getNsfPropId() + " "
                    + grantApplication.getStatusCode() + " "
                    + GappsStatuses.getStatus(grantApplication.getStatusCode()));
            return ProposalRevisionType.BUDGET_REVISION;
        }

        Map<String, List<ProposalReview>> proposalReviewers = new HashMap<String, List<ProposalReview>>();
        List<Proposal> propList = new ArrayList<Proposal>();
        Proposal proposal = new Proposal();
        proposal.setNsfPropId(propPkg.getNsfPropId());
        proposal.setFundingOpId(propPkg.getFundingOp().getFundingOpportunityId());
        proposal.setDeadlineDate(propPkg.getDeadline().getDeadlineDate());
        ProposalStatus proposalStatus = new ProposalStatus();
        proposalStatus.setStatusCode(propPkg.getProposalStatus());
        proposalStatus.setStatusDesc(propPkg.getProposalStatusDesc());
        proposal.setProposalStatus(proposalStatus);
        propList.add(proposal);
        proposalReviewers = getProposalReviewers(propList);
        if (proposalReviewers.containsKey(propPkg.getNsfPropId())) {
            LOGGER.debug("getRevisionTypeIfAvailableForRevision reviewers are assigned");
            return ProposalRevisionType.BUDGET_REVISION;
        }
        if (!proposalReviewers.containsKey(propPkg.getNsfPropId()) && (propPkg.getDeadline().getDeadlineTypeCode()
                .equalsIgnoreCase(Constants.DUE_DATE_TYPE_ACCEPTED_ANYTIME_CODE)
                || propPkg.getDeadline().getDeadlineTypeCode().equalsIgnoreCase(Constants.DUE_DATE_TYPE_TARGET_DATE))) {
            LOGGER.debug("getRevisionTypeIfAvailableForRevision reviewers are not assigned & deadlineTypeCode = "
                    + propPkg.getDeadline().getDeadlineTypeCode() + " proposal Pkg Status Desc"
                    + propPkg.getProposalStatusDesc());
            return ProposalRevisionType.PROPOSAL_FILE_UPDATE;
        }

        if (!proposalReviewers.containsKey(propPkg.getNsfPropId())
                && (!PropMgtUtil.isWithinWindowOfOpportunity(propPkg.getDeadline().getDeadlineDate()))) {
            // this is manual PFU
            return ProposalRevisionType.PROPOSAL_FILE_UPDATE;
        } else {
            return ProposalRevisionType.PROPOSAL_FILE_UPDATE;
        }
    }

    /**
     * This method will re-set revision number on Budget Pdf.
     * 
     * @param propPrepId
     * @param propRevId
     * @return
     * @throws CommonUtilException
     */
    @Override
    public int getRevNumberForRevisedBudget(String propPrepId, String propRevId) throws CommonUtilException {
        int revNum = 0;
        BudgetRevision brev = proposalDataServiceClient.getBudgetRevisions(propPrepId, propRevId);
        List<ProposalRevision> revList = brev.getRevList();
        LOGGER.debug("******** GetRevNumberForRevisedBudget : propPrepId :" + propPrepId + " propRevId : " + propRevId
                + " revList : " + revList);
        if (revList != null && !revList.isEmpty()) {
            LOGGER.debug("******** revList : " + revList.size());
            for (ProposalRevision rev : revList) {
                LOGGER.debug("******** Rev Id : " + rev.getPropRevId() + " Revision Num : " + rev.getRevNo());
                if (propRevId.equalsIgnoreCase(rev.getPropRevId().trim())) {
                    return rev.getRevNo();
                }
            }
        }
        return revNum;
    }

    @Override
    public ByteArrayOutputStream getProposalFileForExternalUsers(String propPrepId, String revisionId)
            throws CommonUtilException {
        LOGGER.debug("****** GetProposalFileForExternalUsers PropPrep Id : " + propPrepId + " PropRev Id  " + revisionId
                + "  ******");
        if (propPrepId != null && revisionId != null) {
            PdfGenerationData pdf = getPdfGenerationData(propPrepId, revisionId);
            GetGeneratedDocumentResponse response = documentGenerationServiceClient.getProposalDocument(pdf);
            byte[] getProposalFile = response.getFile();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(getProposalFile.length);
            byteArrayOutputStream.write(getProposalFile, 0, getProposalFile.length);
            return byteArrayOutputStream;
        }
        return new ByteArrayOutputStream();
    }

    /**
     * Pulls pdf data for all sections for PO/Reviewers View.
     * 
     * @param propPrepId
     * @param revisionId
     * @return
     * @throws CommonUtilException
     */
    private PdfGenerationData getPdfGenerationData(String propPrepId, String revisionId) throws CommonUtilException {
        PdfGenerationData pdf = new PdfGenerationData();
        Map<Section, UploadableProposalSection> sectionPdMap = new LinkedHashMap<Section, UploadableProposalSection>();
        ProposalPackage prop = getProposal(propPrepId, revisionId);

        // Change the Revision Number for BREV
        if (ProposalRevisionType.BUDGET_REVISION.equalsIgnoreCase(prop.getPropPrepRevnTypeCode())) {
            prop.setRevNum(getRevNumberForRevisedBudget(propPrepId, revisionId));
        }
        pdf.setProp(prop);

        for (Section sec : getPrintOrderForExternalView()) {

            if (!Section.COVER_SHEET.getCode().equalsIgnoreCase(sec.getCode())) {
                LOGGER.debug("@@ ******** Section : " + sec + " Code : " + sec.getCode());
                ByteArrayOutputStream baos = getSubmittedProposalPdf(null, null, sec.getCode(), propPrepId, revisionId);
                UploadableProposalSection ups = new UploadableProposalSection();
                ups.setFile(baos.toByteArray());
                sectionPdMap.put(sec, ups);
            } else {
                LOGGER.debug("@@ ******** ELSE Section : " + Section.COVER_SHEET + " Code : "
                        + Section.COVER_SHEET.getCode());
                ByteArrayOutputStream cs = getCoverSheetPdfForExternalView(propPrepId, revisionId, false);
                UploadableProposalSection ups = new UploadableProposalSection();
                ups.setFile(cs.toByteArray());
                sectionPdMap.put(sec, ups);
            }
        }
        pdf.setSectionPdfMap(sectionPdMap);

        return pdf;
    }

    @Override
    public ByteArrayOutputStream getProposalPdfForExternalUsers(String nsfPropId) throws CommonUtilException {
        SubmittedProposal subProp = proposalDataServiceClient.getSubmittedProposal(nsfPropId);
        String propPrepId = subProp.getPropPrepId();
        String revisionId = subProp.getPropRevId();
        LOGGER.debug("***** GetProposalPdfForExternalUsers : " + propPrepId + " PropRev Id  " + revisionId + "******");
        return getProposalFileForExternalUsers(propPrepId, revisionId);
    }

    /**
     * Creates PDF for PO/Reviewers view.
     * 
     * @param propPrepId
     * @param propRevId
     * @param printPageNumbers
     * @return
     * @throws CommonUtilException
     */
    private ByteArrayOutputStream getCoverSheetPdfForExternalView(String propPrepId, String propRevId,
            boolean printPageNumbers) throws CommonUtilException {
        ProposalPackage prop = getProposal(propPrepId, propRevId);

        InstitutionBudget instBudget = getInstitutionBudget(propPrepId, propRevId, Section.BUDGETS,
                prop.getInstitution().getId());

        CoverSheet coverSheet = proposalDataServiceClient.getCoverSheet(prop.getPropPrepId(), prop.getPropRevId());

        List<PiCoPiInformation> piCopiList = coverSheet.getPiCopiList();
        setPiCopiInformation(piCopiList);

        PdfGenerationData pdf = new PdfGenerationData();
        pdf.setProp(prop);
        pdf.setCoverSheet(coverSheet);
        pdf.setInstBudget(instBudget);
        pdf.setPrintPageNumbers(printPageNumbers);
        pdf.setExternalView(false);
        GetGeneratedDocumentResponse response = documentGenerationServiceClient.generateCoverSheetPdf(pdf);
        byte[] file = response.getFile();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(file.length);
        baos.write(file, 0, file.length);
        return baos;
    }
}