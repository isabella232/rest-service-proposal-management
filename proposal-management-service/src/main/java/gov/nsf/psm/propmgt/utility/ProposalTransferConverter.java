package gov.nsf.psm.propmgt.utility;

import gov.nsf.psm.foundation.model.*;
import gov.nsf.psm.foundation.model.budget.BudgetRecord;
import gov.nsf.psm.foundation.model.budget.OtherPersonnelCost;
import gov.nsf.psm.foundation.model.budget.SeniorPersonnelCost;
import gov.nsf.psm.foundation.model.coversheet.*;
import gov.nsf.psm.foundation.model.lookup.ElectronicCertificationText;
import gov.nsf.psm.foundation.model.proposal.ProposalTransfer;
import gov.nsf.psm.foundation.model.proposal.ProposalTransferHeader;
import gov.nsf.psm.proposaltransfer.api.model.*;
import gov.nsf.psm.proposaltransfer.api.model.proposal.AdditionalDocument;
import gov.nsf.psm.proposaltransfer.api.model.proposal.ElectronicSignature;
import gov.nsf.psm.proposaltransfer.api.model.proposal.FileReference;
import gov.nsf.psm.proposaltransfer.api.model.proposal.MonthDistribution;
import gov.nsf.psm.proposaltransfer.api.model.proposal.budget.AnnualBudget;
import gov.nsf.psm.proposaltransfer.api.model.proposal.budget.ProjectBudget;
import gov.nsf.psm.proposaltransfer.api.model.proposal.budget.cost.groups.*;
import gov.nsf.psm.proposaltransfer.api.model.proposal.budget.cost.units.Cost;
import gov.nsf.psm.proposaltransfer.api.model.proposal.budget.cost.units.ItemCost;
import gov.nsf.psm.proposaltransfer.api.model.proposal.budget.cost.units.PersonnelCost;
import gov.nsf.psm.proposaltransfer.api.model.proposal.budget.cost.units.ProratedItemCost;
import gov.nsf.psm.proposaltransfer.api.model.proposal.coversheet.AwardeeOrganization;
import gov.nsf.psm.proposaltransfer.api.model.proposal.coversheet.PrimaryPerformancePlace;
import gov.nsf.psm.proposaltransfer.api.model.proposal.personnel.PrincipleInvestigator;
import gov.nsf.psm.proposaltransfer.api.model.proposal.personnel.ProjectPersonnel;
import gov.nsf.psm.proposaltransfer.api.model.proposal.personnel.Support;
import gov.nsf.referencedataservice.api.model.Address;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ProposalTransferRequestConverter
 */
@Component
public class ProposalTransferConverter {

    private static final Map<String, ProposalType> PROP_TYPE_MAP = new HashMap<String, ProposalType>();
    private static final Map<String, CollaborationType> COLLAB_TYPE_MAP = new HashMap<String, CollaborationType>();
    private static final Map<String, SubmissionType> SUBMISSION_TYPE_MAP = new HashMap<String, SubmissionType>();
    private static final Map<String, RequestAction> REQUEST_ACTION_MAP = new HashMap<String, RequestAction>();
    private static final String PSM_APPLICATION_ID = "53";

    static {
        COLLAB_TYPE_MAP.put("Non-Collaborative", CollaborationType.NON_COLLABORATIVE);
        PROP_TYPE_MAP.put("Research", ProposalType.RESEARCH);
        PROP_TYPE_MAP.put("Early-concept Grants for Exploratory Research (EAGER)", ProposalType.EAGER);
        SUBMISSION_TYPE_MAP.put("Full Proposal", SubmissionType.FULL_PROPOSAL);

        REQUEST_ACTION_MAP.put("ORIG", RequestAction.SUBMIT);
        REQUEST_ACTION_MAP.put("PFUD", RequestAction.PFU);
        REQUEST_ACTION_MAP.put("BUPD", RequestAction.BUDGET_UPDATE);
        REQUEST_ACTION_MAP.put("BREV", RequestAction.BUDGET_REVISION);
    }

    public ProposalTransferRequest convertToProposalTransferRequest(ProposalTransfer proposalTransfer) {
        if (proposalTransfer == null) {
            throw new IllegalArgumentException("ProposalTransfer object cannot be null (did ProposalDataService lookup fail?)");
        }

        ProposalTransferRequest proposalTransferRequest = new ProposalTransferRequest();
        proposalTransferRequest.setApplicationId(PSM_APPLICATION_ID);
        proposalTransferRequest.setProposalPreparationId(sanitizeString(proposalTransfer.getProposalHeader().getPropPrepId()));
        proposalTransferRequest.setProposalPreparationRevisionId(sanitizeString(proposalTransfer.getProposalHeader().getPropRevId()));
        proposalTransferRequest.setRevisionNumber(Integer.toString(proposalTransfer.getProposalHeader().getRevisionNumber()));
        proposalTransferRequest.setProposalType(PROP_TYPE_MAP.get(sanitizeString(proposalTransfer.getProposalHeader().getProposalType())));
        proposalTransferRequest.setSubmissionType(SUBMISSION_TYPE_MAP.get(sanitizeString(proposalTransfer.getProposalHeader().getSubmissionType())));
        proposalTransferRequest.setCollaborationType(COLLAB_TYPE_MAP.get(sanitizeString(proposalTransfer.getProposalHeader().getCollabType())));
        proposalTransferRequest.setFastLaneProposal(convertToFastLaneProposal(proposalTransfer));
        proposalTransferRequest.setRequestAction(REQUEST_ACTION_MAP.get(sanitizeString(proposalTransfer.getProposalHeader().getRevnType())));
        proposalTransferRequest.setCreationUser(sanitizeString(proposalTransfer.getElectronicSignature().getUserNsfId()));
        proposalTransferRequest.setActiveUser(sanitizeString(proposalTransfer.getElectronicSignature().getUserNsfId()));

        return proposalTransferRequest;
    }

    public ProposalTransferRequest convertToProposalTransferRequest(ProposalTransfer proposalTransfer, RequestAction requestAction) {
        if (proposalTransfer == null) {
            throw new IllegalArgumentException("ProposalTransfer object cannot be null (did ProposalDataService lookup fail?)");
        }

        ProposalTransferRequest proposalTransferRequest = convertToProposalTransferRequest(proposalTransfer);
        proposalTransferRequest.setRequestAction(requestAction);

        return proposalTransferRequest;
    }

    protected static FastLaneProposal convertToFastLaneProposal(ProposalTransfer proposalTransfer) {

        FastLaneProposal fastLaneProposal = new FastLaneProposal();
        fastLaneProposal.setProposalControlInfo(convertProposalControlInfo(proposalTransfer.getProposalHeader(), proposalTransfer.getUpdateJustification()));
        fastLaneProposal.setProjectSummary(convertProjectSummary(proposalTransfer.getProjectSummary()));
        fastLaneProposal.setProjectDescription(convertProjectDescription(proposalTransfer.getProjectDescription()));
        if (proposalTransfer.getPostDocMentPlan() != null && StringUtils.isNotEmpty(proposalTransfer.getPostDocMentPlan().getFilePath())) {
            fastLaneProposal.setMentoringPlan(convertMentoringPlan(proposalTransfer.getPostDocMentPlan()));
        }
        fastLaneProposal.setDataManagementPlan(convertDataManagementPlan(proposalTransfer.getDataManagementPlan()));
        fastLaneProposal.setFacilitiesEquipmentAndOtherResources(convertFacilitiesAndEquipment(proposalTransfer.getFacilitiesEquipment()));
        fastLaneProposal.setReferencesCited(convertReferencesCited(proposalTransfer.getReferencesCited()));
        fastLaneProposal.setProjectBudget(convertBudget(proposalTransfer.getBudget(), proposalTransfer.getBudgetJustification()));
        fastLaneProposal.setCoverSheet(convertCoversheet(proposalTransfer.getCoverSheet(), proposalTransfer.getProposalHeader(), proposalTransfer.getElectronicSignature(), proposalTransfer.getProposalHeader().getTotalRqstDolAmt()));
        fastLaneProposal.setProjectPersonnel(convertPersonnel(proposalTransfer.getPersonnels(), proposalTransfer.getBioSketcheList(), proposalTransfer.getCurrPendSuppList(), proposalTransfer.getCoaList(), proposalTransfer.getPiCoPIDemographicInfoList(), proposalTransfer.getProposalHeader().isPICoPIUpdated()));
        fastLaneProposal.setSingleCopyDocuments(getSingleCopyDocuments(proposalTransfer));
        fastLaneProposal.setSupplementaryDocuments(getSupplementaryDocuments(proposalTransfer));
        fastLaneProposal.setElectronicSignature(getElectronicSignature(proposalTransfer.getElectronicSignature(), proposalTransfer.getElectonicCertificationText(), proposalTransfer.getProposalHeader()));
        fastLaneProposal.setSuggestedReviewers(convertSuggestedReviewers(proposalTransfer.getListOfSuggReviewers()));
        fastLaneProposal.setReviewersNotToInclude(convertReviewersNotToInclude(proposalTransfer.getListOfReviewersNotToInclude()));

        return fastLaneProposal;
    }

    private static ProposalControlInfo convertProposalControlInfo(ProposalTransferHeader proposalHeader, ProposalUpdateJustification proposalUpdateJustification) {
        ProposalControlInfo proposalControlInfo = new ProposalControlInfo();
        proposalControlInfo.setPappgVersion(sanitizeString(proposalHeader.getGpgVersion()));
        proposalControlInfo.setProposalRevisionCreateDate(proposalHeader.getRevnCreateDate());
        proposalControlInfo.setProposalRevisionSubmitDate(proposalHeader.getProposalSubmitDate());
        proposalControlInfo.setProposalRevisionJustification(proposalUpdateJustification != null ? sanitizeString(proposalUpdateJustification.getJustificationText()) : null);
        proposalControlInfo.setTpiStatusCode(proposalHeader.getRevnType().equals("BREV") ? "40" : "83");
        proposalControlInfo.setProgramOfficerSignature("AUTOMATIC");

        return proposalControlInfo;
    }

    private static ElectronicSignature getElectronicSignature(ProposalElectronicSign proposalElectronicSign, ElectronicCertificationText electonicCertificationText, ProposalTransferHeader proposalHeader) {

        ElectronicSignature electronicSignature = new ElectronicSignature();

        electronicSignature.setDateSigned(proposalElectronicSign.getElecSignDate());
        electronicSignature.setInstitutionId(sanitizeString(proposalElectronicSign.getInstitutionId()));
        electronicSignature.setIpAddress(sanitizeString(proposalElectronicSign.getIpAddress()));
        electronicSignature.setUserFirstName(sanitizeString(proposalElectronicSign.getUserFirstName()));
        electronicSignature.setUserMiddleInit(sanitizeString(proposalElectronicSign.getUserMiddleInit()));
        electronicSignature.setUserLastName(sanitizeString(proposalElectronicSign.getUserLastname()));
        electronicSignature.setUserEmailAddress(sanitizeString(proposalElectronicSign.getUserEmailAddress()));
        electronicSignature.setUserFaxNumber(sanitizeString(proposalElectronicSign.getUserFaxNumber()));
        electronicSignature.setUserPhoneNumber(sanitizeString(proposalElectronicSign.getUserPhoneNumber()));
        electronicSignature.setUserNsfId(sanitizeString(proposalElectronicSign.getUserNsfId()));
        electronicSignature.setLastUpdatedTimestamp(proposalElectronicSign.getLastUpdatedTmsp());

        return electronicSignature;
    }

    private static List<AdditionalDocument> getSupplementaryDocuments(ProposalTransfer proposalTransfer) {
        List<AdditionalDocument> supplementaryDocuments = new ArrayList<AdditionalDocument>();

        if (proposalTransfer.getOtherPersonnelInfoBio() != null && StringUtils.isNotEmpty(proposalTransfer.getOtherPersonnelInfoBio().getFilePath())) {
            supplementaryDocuments.add(convertOtherPersonnelInfoBio(proposalTransfer.getOtherPersonnelInfoBio()));
        }

        supplementaryDocuments.addAll(convertOtherSupplementaryDocs(proposalTransfer.getOtherSupplementaryDocs()));

        return supplementaryDocuments;
    }

    private static List<AdditionalDocument> convertOtherSupplementaryDocs(List<OtherSuppDocs> otherSupplementaryDocs) {
        List<AdditionalDocument> otherDocs = new ArrayList<AdditionalDocument>();

        for (OtherSuppDocs document : otherSupplementaryDocs) {
            if (document != null && StringUtils.isNotEmpty(document.getFilePath())) {
                FileReference fileReference = new FileReference(extractFolderPath(document.getFilePath()), document.getFilePath(), document.getPageCount(), document.getLastUpdatedTmsp());
                otherDocs.add(new AdditionalDocument(fileReference, sanitizeString(document.getOtherSuppDocTxt())));
            }
        }
        return otherDocs;
    }

    private static AdditionalDocument convertOtherPersonnelInfoBio(OthrPersBioInfo otherPersonnelInfoBio) {
        if (otherPersonnelInfoBio == null) {
            throw new IllegalArgumentException("OthrPersBioInfo parameter cannot be null");
        }

        return new AdditionalDocument(
                new FileReference(extractFolderPath(otherPersonnelInfoBio.getFilePath()), otherPersonnelInfoBio.getFilePath(), otherPersonnelInfoBio.getPageCount(), otherPersonnelInfoBio.getLastUpdatedTmsp()),
                sanitizeString(otherPersonnelInfoBio.getOthrPersBioInfoDocText())
        );
    }

    private static List<AdditionalDocument> getSingleCopyDocuments(ProposalTransfer proposalTransfer) {
        List<AdditionalDocument> singleCopyDocuments = new ArrayList<AdditionalDocument>();

        return singleCopyDocuments;
    }

    private static AdditionalDocument convertReviewersNotToInclude(ReviewersNotInclude reviewersNotToInclude) {
        if (reviewersNotToInclude == null || StringUtils.isEmpty(reviewersNotToInclude.getFilePath())) {
            return null;
        }

        return new AdditionalDocument(
                new FileReference(extractFolderPath(reviewersNotToInclude.getFilePath()), reviewersNotToInclude.getFilePath(), reviewersNotToInclude.getPageCount(), reviewersNotToInclude.getLastUpdatedTmsp()),
                sanitizeString("File upload from PSM")
        );
    }

    private static AdditionalDocument convertSuggestedReviewers(SuggestedReviewer suggestedReviewers) {
        if (suggestedReviewers == null || StringUtils.isEmpty(suggestedReviewers.getFilePath())) {
            return null;
        }

        return new AdditionalDocument(
                new FileReference(extractFolderPath(suggestedReviewers.getFilePath()), suggestedReviewers.getFilePath(), suggestedReviewers.getPageCount(), suggestedReviewers.getLastUpdatedTmsp()),
                sanitizeString("File upload from PSM")
        );
    }

    private static ProjectBudget convertBudget(List<gov.nsf.psm.foundation.model.budget.InstitutionBudget> budget, BudgetJustification budgetJustification) {


        List<gov.nsf.psm.proposaltransfer.api.model.proposal.budget.InstitutionBudget> institutionBudgets = budget
                .stream()
                .map(institutionBudget -> convertInstitutionBudget(institutionBudget, budgetJustification))
                .collect(Collectors.toList());


        return new ProjectBudget(institutionBudgets);
    }

    private static gov.nsf.psm.proposaltransfer.api.model.proposal.budget.InstitutionBudget convertInstitutionBudget(gov.nsf.psm.foundation.model.budget.InstitutionBudget institutionBudget, BudgetJustification budgetJustification) {

        List<AnnualBudget> annualBudgets = institutionBudget.getBudgetRecordList()
                .stream()
                .map(budgetRecord -> convertBudgetRecord(budgetRecord, budgetJustification, institutionBudget.getInstId(), institutionBudget.getLastUpdtTmsp()))
                .collect(Collectors.toList());

        return new gov.nsf.psm.proposaltransfer.api.model.proposal.budget.InstitutionBudget(annualBudgets);
    }

    private static AnnualBudget convertBudgetRecord(BudgetRecord budgetRecord, BudgetJustification budgetJustification, String institutionId, Date lastUpdatedTimestamp) {

        if (budgetRecord == null || budgetJustification == null || StringUtils.isEmpty(institutionId)) {
            throw new IllegalArgumentException("BudgetRecord, BudgetJustification, and InstitutionId parameters cannot be null");
        }

        AnnualBudget annualBudget = new AnnualBudget();

        annualBudget.setYear(budgetRecord.getBudgetYear());
        annualBudget.setBudgetJustification(new gov.nsf.psm.proposaltransfer.api.model.proposal.budget.BudgetJustification
                (sanitizeString(budgetJustification.getProjDescText()), new FileReference(extractFolderPath(budgetJustification.getFilePath()), budgetJustification.getFilePath(), budgetJustification.getPageCount(), budgetJustification.getLastUpdatedTmsp())));
        annualBudget.setInstitutionId(institutionId);

        /** Equipment Costs **/
        List<ItemCost> equipmentCosts = budgetRecord
                .getEquipmentList()
                .stream()
                .map(equipmentCost -> new ItemCost(sanitizeString(equipmentCost.getEquipmentName()), equipmentCost.getEquipmentDollarAmount()))
                .collect(Collectors.toList());

        annualBudget.setEquipmentCosts(new EquipmentCosts(equipmentCosts));

        /** Fringe Benefits Cost **/
        annualBudget.setFringeBenefitsCost(new Cost(budgetRecord.getFringeBenefitCost().getFringeBenefitDollarAmount()));

        /** Indirect Costs **/
        List<ProratedItemCost> indirectCosts = budgetRecord
                .getIndirectCostsList()
                .stream()
                .map(indirectCost -> new ProratedItemCost(sanitizeString(indirectCost.getIndirectCostItemName()), indirectCost.getIndirectCostBaseDollarAmount(), indirectCost.getIndirectCostRate()))
                .collect(Collectors.toList());

        annualBudget.setIndirectCosts(new IndirectCosts(indirectCosts));

        /** Other Direct Costs **/
        OtherDirectCosts otherDirectCosts = new OtherDirectCosts();
        otherDirectCosts.setComputerServicesCost(new Cost(budgetRecord.getOtherDirectCost().getComputerServicesDollarAmount()));
        otherDirectCosts.setConsultantCost(new Cost(budgetRecord.getOtherDirectCost().getConsultantServicesDollarAmount()));
        otherDirectCosts.setMaterialsAndSuppliesCost(new Cost(budgetRecord.getOtherDirectCost().getMaterialsDollarAmount()));
        otherDirectCosts.setOtherCost(new Cost(budgetRecord.getOtherDirectCost().getOtherDirectCostDollarAmount()));
        otherDirectCosts.setPublicationCost(new Cost(budgetRecord.getOtherDirectCost().getPublicationDollarAmount()));
        otherDirectCosts.setSubcontractsCost(new Cost(budgetRecord.getOtherDirectCost().getSubContractDollarAmount()));

        annualBudget.setOtherDirectCosts(otherDirectCosts);

        /** Participant Costs **/
        ParticipantCosts participantCosts = new ParticipantCosts();
        participantCosts.setOtherParticipantCost(new Cost(budgetRecord.getParticipantsSupportCost().getOtherDollarAmount()));
        participantCosts.setParticipantTravelCost(new Cost(budgetRecord.getParticipantsSupportCost().getTravelDollarAmount()));
        participantCosts.setStipendCost(new Cost(budgetRecord.getParticipantsSupportCost().getStipendDollarAmount()));
        participantCosts.setSubsistenceCost(new Cost(budgetRecord.getParticipantsSupportCost().getSubsistenceDollarAmount()));
        participantCosts.setParticipantCount(budgetRecord.getParticipantsSupportCost().getPartNumberCount());

        annualBudget.setParticipantCosts(participantCosts);


        PersonnelCosts personnelCosts = new PersonnelCosts();

        /** Personnel Costs **/
        List<gov.nsf.psm.proposaltransfer.api.model.proposal.budget.cost.units.SeniorPersonnelCost> seniorPersonnelCosts = budgetRecord.getSrPersonnelList()
                .stream()
                .map(seniorPersonnelCost -> convertSeniorPersonnelCost(seniorPersonnelCost))
                .collect(Collectors.toList());

        personnelCosts.setSeniorPersonnelCosts(seniorPersonnelCosts);

        for (OtherPersonnelCost otherPersonnelCost : budgetRecord.getOtherPersonnelList()) {
            PersonnelCost personnelCost = new PersonnelCost(
                    otherPersonnelCost.getOtherPersonDollarAmount(),
                    otherPersonnelCost.getOtherPersonCount(),
                    new MonthDistribution(otherPersonnelCost.getOtherPersonMonthCount(), 0.0, 0.0)
            );

            if (otherPersonnelCost.getOtherPersonTypeCode().equals(OtherPersonnelCost.CODE_STUDENTS_POST_DOCTORAL)) {
                personnelCosts.setPostDoctoratesCost(personnelCost);
            } else if (otherPersonnelCost.getOtherPersonTypeCode().equals(OtherPersonnelCost.CODE_OTHER_PROFESSIONALS)) {
                personnelCosts.setOtherProfessionalsCost(personnelCost);
            } else if (otherPersonnelCost.getOtherPersonTypeCode().equals(OtherPersonnelCost.CODE_STUDENTS_GRADUATE)) {
                personnelCosts.setGraduateStudentsCost(personnelCost);
            } else if (otherPersonnelCost.getOtherPersonTypeCode().equals(OtherPersonnelCost.CODE_STUDENTS_UNDERGRADUATE)) {
                personnelCosts.setUndergraduateStudentsCost(personnelCost);
            } else if (otherPersonnelCost.getOtherPersonTypeCode().equals(OtherPersonnelCost.CODE_CLERICAL)) {
                personnelCosts.setSecretarialCost(personnelCost);
            } else if (otherPersonnelCost.getOtherPersonTypeCode().equals(OtherPersonnelCost.CODE_OTHER)) {
                personnelCosts.setOtherPersonnelCost(personnelCost);
            }
        }

        annualBudget.setPersonnelCosts(personnelCosts);

        /** Small Business Fee Costs **/
        annualBudget.setSmallBusinessFeeCost(new Cost());

        /** Travel Costs **/
        annualBudget.setTravelCosts(new TravelCosts(
                new Cost(budgetRecord.getTravelCost().getDomesticTravelDollarAmount()),
                new Cost(budgetRecord.getTravelCost().getForeignTravelDollarAmount())
        ));

        annualBudget.setLastUpdatedTimestamp(lastUpdatedTimestamp);

        return annualBudget;
    }

    private static gov.nsf.psm.proposaltransfer.api.model.proposal.budget.cost.units.SeniorPersonnelCost convertSeniorPersonnelCost(SeniorPersonnelCost seniorPersonnelCost) {
        gov.nsf.psm.proposaltransfer.api.model.proposal.budget.cost.units.SeniorPersonnelCost _seniorPersonnelCost =
                new gov.nsf.psm.proposaltransfer.api.model.proposal.budget.cost.units.SeniorPersonnelCost();

        _seniorPersonnelCost.setFirstName(seniorPersonnelCost.getSeniorPersonFirstName());
        _seniorPersonnelCost.setMiddleInitial(seniorPersonnelCost.getSeniorPersonMiddleInitial());
        _seniorPersonnelCost.setLastName(seniorPersonnelCost.getSeniorPersonLastName());
        _seniorPersonnelCost.setAmount(seniorPersonnelCost.getSeniorPersonDollarAmount());
        _seniorPersonnelCost.setMonthDistribution(new MonthDistribution(seniorPersonnelCost.getSeniorPersonMonthCount(), 0.0, 0.0));

        return _seniorPersonnelCost;
    }


    private static ProjectPersonnel convertPersonnel(List<Personnel> personnelList, List<BiographicalSketch> bioSketchList, List<CurrentAndPendingSupport> currPendSuppList, List<COA> coaList, List<PiCoPiInformation> piInformations, boolean isUpdated) {

        if (CollectionUtils.isEmpty(personnelList)) {
            throw new IllegalArgumentException("PersonnelList parameter cannot be null or empty");
        }

        ProjectPersonnel projectPersonnel = new ProjectPersonnel();
        projectPersonnel.setUpdated(isUpdated);

        for (Personnel personnel : personnelList) {
            if (!allowableFastLaneRole(personnel.getPSMRole())) {
                continue;
            }

            PrincipleInvestigator person = convertPerson(personnel, piInformations);

            person.setBiographicalSketch(findAndConvertBioSketch(bioSketchList, personnel.getPropPersId()));
            person.addSupport(findAndConvertSupport(currPendSuppList, personnel.getPropPersId()));
            person.setCollaborativeAffiliatesFileReference(findAndConvertCoaFileReference(coaList, personnel.getPropPersId()));

            if (personnel.getPSMRole().getCode().equals(PSMRole.ROLE_PI)) {
                projectPersonnel.setPrincipleInvestigator(person);
            } else if (personnel.getPSMRole().getCode().equals(PSMRole.ROLE_CO_PI)) {
                projectPersonnel.addCoPrincipleInvestigator(person);
            } else if (personnel.getPSMRole().getCode().equals(PSMRole.ROLE_OSP)) {
                projectPersonnel.addSeniorPersonnel(person);
            }
        }


        return projectPersonnel;
    }

    private static FileReference findAndConvertCoaFileReference(List<COA> coaList, String personId) {
        if (CollectionUtils.isEmpty(coaList) || StringUtils.isEmpty(personId)) {
            throw new IllegalArgumentException("COAList and PSM personId parameters cannot be null or empty");
        }

        COA coa = coaList
                .stream()
                .filter(_coa -> Objects.nonNull(_coa) && Objects.nonNull(_coa.getPerson()) && StringUtils.isNotEmpty(_coa.getPerson().getPropPersId()))
                .filter(_coa -> StringUtils.equals(_coa.getPerson().getPropPersId(), personId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not find COA for PSM person w/ ID: " + personId));

        return new FileReference(extractFolderPath(coa.getCoaPdfFilePath()), coa.getCoaPdfFilePath(), coa.getCoaPdfPageCount(), coa.getLastUpdatedTmsp());
    }

    private static boolean allowableFastLaneRole(PSMRole psmRole) {
        if (psmRole == null || StringUtils.isEmpty(psmRole.getCode())) {
            throw new IllegalArgumentException("psmRole parameter cannot be null (neither can psmRole.code");
        }

        return psmRole.getCode().equals(PSMRole.ROLE_PI)
                || psmRole.getCode().equals(PSMRole.ROLE_CO_PI)
                || psmRole.getCode().equals(PSMRole.ROLE_OSP);
    }

    private static Support findAndConvertSupport(List<CurrentAndPendingSupport> currPendSuppList, String personId) {
        if (CollectionUtils.isEmpty(currPendSuppList) || StringUtils.isEmpty(personId)) {
            throw new IllegalArgumentException("currPendSuppList and PSM personId parameters cannot be null or empty");
        }

        CurrentAndPendingSupport currentAndPendingSupport = currPendSuppList
                .stream()
                .filter(supp -> Objects.nonNull(supp) && Objects.nonNull(supp.getPerson()) && StringUtils.isNotEmpty(supp.getPerson().getPropPersId()))
                .filter(supp -> supp.getPerson().getPropPersId().equals(personId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not find CurrentAndPendingSupport for PSM person w/ ID: " + personId));

        Support support = new Support();
        support.setFileReference(new FileReference(extractFolderPath(currentAndPendingSupport.getFilePath()), currentAndPendingSupport.getFilePath(), currentAndPendingSupport.getPageCount(), currentAndPendingSupport.getLastUpdatedTmsp()));

        return support;
    }

    private static gov.nsf.psm.proposaltransfer.api.model.proposal.BiographicalSketch findAndConvertBioSketch(List<BiographicalSketch> bioSketchList, String personId) {
        if (CollectionUtils.isEmpty(bioSketchList) || StringUtils.isEmpty(personId)) {
            throw new IllegalArgumentException("bioSketchList and PSM personId parameters cannot be null or empty");
        }

        BiographicalSketch sketch = bioSketchList
                .stream()
                .filter(bio -> Objects.nonNull(bio) && Objects.nonNull(bio.getPerson()) && Objects.nonNull(bio.getPerson().getPropPersId()))
                .filter(bio -> bio.getPerson().getPropPersId().equals(personId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not find BiographicalSketch for PSM person w/ ID: " + personId));

        return new gov.nsf.psm.proposaltransfer.api.model.proposal.BiographicalSketch(
                new FileReference(extractFolderPath(sketch.getFilePath()), sketch.getFilePath(), sketch.getPageCount(), sketch.getLastUpdatedTmsp())
        );
    }

    private static PrincipleInvestigator convertPerson(Personnel person, List<PiCoPiInformation> piInformations) {
        if (person == null) {
            throw new IllegalArgumentException("Person parameter cannot be null");
        }

        PrincipleInvestigator principleInvestigator = new PrincipleInvestigator();

        principleInvestigator.setNsfId(sanitizeString(person.getNsfId()));
        principleInvestigator.setFirstName(sanitizeString(person.getFirstName()));
        principleInvestigator.setMiddleInitial(sanitizeString(person.getMiddleName()));
        principleInvestigator.setLastName(sanitizeString(person.getLastName()));
        principleInvestigator.setDegreeType(sanitizeString(person.getAcadDegree()));
        principleInvestigator.setDegreeYear(person.getAcadYear());
        principleInvestigator.setDepartment(sanitizeString(person.getDeptName()));
        principleInvestigator.setPhoneNumber(sanitizeString(person.getPhoneNumber()));
        principleInvestigator.setFaxNumber(sanitizeString(person.getFaxNumber()));
        principleInvestigator.setEmailAddress(sanitizeString(person.getEmail()));

        principleInvestigator.setStreet1(sanitizeString(person.getAddrLine1()));
        principleInvestigator.setStreet2(sanitizeString(person.getAddrLine2()));
        principleInvestigator.setCity(sanitizeString(person.getCityName()));
        principleInvestigator.setState(sanitizeString(person.getStateCode()));
        principleInvestigator.setCountry(sanitizeString(person.getCountryCode()));
        principleInvestigator.setZipCode(sanitizeString(person.getPostalCode()));
        principleInvestigator.setLastUpdatedTimestamp(person.getLastUpdatedTmsp());


        return principleInvestigator;
    }

    private static gov.nsf.psm.proposaltransfer.api.model.proposal.coversheet.CoverSheet convertCoversheet(CoverSheet coverSheet, ProposalTransferHeader proposalHeader, ProposalElectronicSign electronicSignature, BigDecimal requestAmount) {
        if (coverSheet == null || proposalHeader == null) {
            throw new IllegalArgumentException("CoverSheet and ProposalHeader parameters cannot be null");
        }

        gov.nsf.psm.proposaltransfer.api.model.proposal.coversheet.CoverSheet _coverSheet =
                new gov.nsf.psm.proposaltransfer.api.model.proposal.coversheet.CoverSheet();

        _coverSheet.setAccomplishmentBasedRenewal(false);
        _coverSheet.setAwardeeOrganization(convertAwardeeOrganization(coverSheet, proposalHeader.getAwardeeOrganization()));
        _coverSheet.setBeginningInvestigator(coverSheet.isBeginningInvestigator());
        _coverSheet.setCollaborationType(COLLAB_TYPE_MAP.get(proposalHeader.getCollabType()));
        _coverSheet.setDeadlineDate(proposalHeader.getDeadline().getDeadlineDate());
        _coverSheet.setDisclosureOfLobbyActivities(coverSheet.isDisclosureLobbyActy());
        _coverSheet.setExemptionSubsection(sanitizeString(coverSheet.getExemptionSubsection()));
        _coverSheet.setFederalAgencies(convertFederalAgencies(coverSheet.getFederalAgencies()));
        _coverSheet.setHistoricPlaces(coverSheet.isHistoricPlace());
        _coverSheet.setHumanSubjects(coverSheet.isHumanSubject());
        _coverSheet.setHumanSubjectsAssuranceNumber(sanitizeString(coverSheet.getHumanSubjectAssuNumber()));
        _coverSheet.setIrbApprovalDate(coverSheet.getiRbAppDate());
        _coverSheet.setVertebrateAnimals(coverSheet.isVertebrateAnimal());
        _coverSheet.setPshAnimalWelfareAssuranceNumber(sanitizeString(coverSheet.getAnimalWelfareAssuNumber()));
        _coverSheet.setIacucApprovalDate(coverSheet.getiAcucSAppDate());
        _coverSheet.setInternationalActivitiesCountryNames(convertInternationalCountryNames(coverSheet.getInternationalActyCountries()));
        _coverSheet.setInternationalActvitiesCountryName(!CollectionUtils.isEmpty(coverSheet.getInternationalActyCountries()));
        _coverSheet.setPrimaryPerformancePlace(convertPrimaryPlaceOfPerformance(coverSheet.getPpop()));
        _coverSheet.setProjectTitle(sanitizeString(proposalHeader.getProposalTitle()));
        _coverSheet.setProposalType(PROP_TYPE_MAP.get(proposalHeader.getProposalType()));
        _coverSheet.setProprietaryAndPriviledgedInformation(coverSheet.isProprietaryPrivileged());
        _coverSheet.setProposalStartDate(coverSheet.getRequestedStartDate());
        _coverSheet.setSolicitationNumber(sanitizeString(proposalHeader.getFundingOp().getFundingOpportunityId()));
        _coverSheet.setProposalDuration(coverSheet.getProposalDuration());
        _coverSheet.setUnitsOfConsideration(convertUnitsOfConsideration(proposalHeader.getUocs()));
        _coverSheet.setRequestedAmount(requestAmount != null ? requestAmount.setScale(2) : null);
        _coverSheet.setDebarmentAndSuspension(BooleanUtils.toBoolean(electronicSignature.getDebarFlag()));
        _coverSheet.setDebarmentAndSuspensionExplanation(sanitizeString(electronicSignature.getDebarText()));
        _coverSheet.setLastUpdatedTimestamp(coverSheet.getLastUpdatedTmsp());
        _coverSheet.setNatureRequestCode(proposalHeader.getNatureRequestCode());
        _coverSheet.setRecommendedAwardInstrumentCode(proposalHeader.getRecommendAwdInstr());

        return _coverSheet;
    }

    private static List convertUnitsOfConsideration(UnitOfConsideration[] uocs) {
        if (ArrayUtils.isEmpty(uocs)) {
            throw new IllegalArgumentException("Units Of Consideration array must not be null or empty");
        }

        Comparator<UnitOfConsideration> comparator = new Comparator<UnitOfConsideration>() {
            @Override
            public int compare(UnitOfConsideration o1, UnitOfConsideration o2) {
                return Integer.valueOf(o1.getUocOrdrNum()).compareTo(Integer.valueOf(o2.getUocOrdrNum()));
            }
        };

        Arrays.sort(uocs, comparator);

        List<gov.nsf.psm.proposaltransfer.api.model.proposal.coversheet.UnitOfConsideration>
                _uocs = new ArrayList<>();

        for (UnitOfConsideration uoc : uocs) {
            _uocs.add(new gov.nsf.psm.proposaltransfer.api.model.proposal.coversheet.UnitOfConsideration(
                    uoc.getDirectorate().getDirectorateCode(),
                    uoc.getDivision().getDivisionCode(),
                    uoc.getProgramElement().getProgramElementCode(),
                    uoc.getLastUpdatedTmsp()
            ));
        }
        return _uocs;
    }

    private static PrimaryPerformancePlace convertPrimaryPlaceOfPerformance(PrimaryPlaceOfPerformance primaryPlaceOfPerformance) {
        if (primaryPlaceOfPerformance == null) {
            throw new IllegalArgumentException("PrimaryPlaceOfPerformance parameter cannot be null");
        }

        PrimaryPerformancePlace primaryPerformancePlace = new PrimaryPerformancePlace();

        primaryPerformancePlace.setStreetAddress(sanitizeString(primaryPlaceOfPerformance.getStreetAddress()));
        primaryPerformancePlace.setCity(sanitizeString(primaryPlaceOfPerformance.getCityName()));
        primaryPerformancePlace.setState(sanitizeString(primaryPlaceOfPerformance.getStateCode()));
        primaryPerformancePlace.setZipCode(sanitizeString(primaryPlaceOfPerformance.getPostalCode()));
        primaryPerformancePlace.setCountryCode(sanitizeString(primaryPlaceOfPerformance.getCountryCode()));
        primaryPerformancePlace.setInstitutionId(sanitizeString(primaryPlaceOfPerformance.getOrganizationId()));
        primaryPerformancePlace.setOrganizationName(sanitizeString(primaryPlaceOfPerformance.getOrganizationName()));
        primaryPerformancePlace.setInstitutionId(Integer.toString(primaryPlaceOfPerformance.getpPopId()));
        primaryPerformancePlace.setLastUpdatedTimestamp(primaryPlaceOfPerformance.getLastUpdatedTmsp());

        return primaryPerformancePlace;
    }

    private static List<String> convertInternationalCountryNames(List<InternationalActyCountry> internationalActivityCountries) {
        if (CollectionUtils.isEmpty(internationalActivityCountries)) {
            return Collections.emptyList();
        }

        return internationalActivityCountries
                .stream()
                .filter(country -> Objects.nonNull(country))
                .map(country -> sanitizeString(country.getIntlCountryCode()))
                .collect(Collectors.toList());
    }

    private static List<gov.nsf.psm.proposaltransfer.api.model.proposal.coversheet.FederalAgency> convertFederalAgencies(List<FederalAgency> federalAgencies) {
        if (CollectionUtils.isEmpty(federalAgencies)) {
            return Collections.emptyList();
        }

        return federalAgencies
                .stream()
                .map(federalAgency -> new gov.nsf.psm.proposaltransfer.api.model.proposal.coversheet.FederalAgency(sanitizeString(federalAgency.getFedAgencyNameAbbreviation()), federalAgency.getLastUpdatedTmsp()))
                .collect(Collectors.toList());
    }

    private static AwardeeOrganization convertAwardeeOrganization(CoverSheet coverSheet, Institution awdOrganization) {
        if (coverSheet == null || awdOrganization == null) {
            throw new IllegalArgumentException("CoverSheet and Awardee Institution parameters cannot be null");
        }


        AwardeeOrganization organization = new AwardeeOrganization();
        gov.nsf.referencedataservice.api.model.TimeZone timeZone = new gov.nsf.referencedataservice.api.model.TimeZone();
        timeZone.setTimeZoneName(sanitizeString(awdOrganization.getTimeZone()));

        organization.setId(sanitizeString(awdOrganization.getId()));
        organization.setForProfit(coverSheet.isForProfit());
        organization.setMinorityBusiness(coverSheet.isMinoritybusiness());
        organization.setSmallBusiness(coverSheet.isSmallbusiness());
        organization.setWomenOwnedBusiness(coverSheet.isWomenOwnedbusiness());
        organization.setAddressType(sanitizeString(awdOrganization.getAddressType()));
        organization.setDunsId(sanitizeString(awdOrganization.getDunsNumber()));
        organization.setInstName(sanitizeString(awdOrganization.getOrganizationName()));
        organization.setTaxId(awdOrganization.getTaxId() != null ? awdOrganization.getTaxId().replace("-", "") : null);
        organization.setTimeZone(timeZone);
        organization.setAddress(convertInstitutionAddress(awdOrganization.getAddress()));

        return organization;
    }

    private static Address convertInstitutionAddress(InstitutionAddress instAddress) {
        Address address = new Address();

        address.setStreet1(sanitizeString(instAddress.getStreetAddress()));
        address.setStreet2(sanitizeString(instAddress.getStreetAddress2()));
        address.setCity(sanitizeString(instAddress.getCityName()));
        address.setState(sanitizeString(instAddress.getStateCode()));
        address.setCountry(sanitizeString(instAddress.getCountryCode()));
        address.setZipCode(sanitizeString(instAddress.getPostalCode()));

        return address;
    }

    private static gov.nsf.psm.proposaltransfer.api.model.proposal.ReferencesCited convertReferencesCited(ReferencesCited referencesCited) {

        if (referencesCited == null) {
            throw new IllegalArgumentException("ReferencesCited parameters cannot be null");
        }

        return new gov.nsf.psm.proposaltransfer.api.model.proposal.ReferencesCited(
                sanitizeString(referencesCited.getRefCitedText()),
                new FileReference(extractFolderPath(referencesCited.getFilePath()), referencesCited.getFilePath(), referencesCited.getPageCount(), referencesCited.getLastUpdatedTmsp())
        );
    }

    private static gov.nsf.psm.proposaltransfer.api.model.proposal.ProjectSummary convertProjectSummary(ProjectSummary projectSummary) {
        if (projectSummary == null) {
            throw new IllegalArgumentException("ProjectSummary parameter cannot be null");
        }

        return new gov.nsf.psm.proposaltransfer.api.model.proposal.ProjectSummary(
                sanitizeString(projectSummary.getOverview()),
                sanitizeString(projectSummary.getBrodimpact()),
                sanitizeString(projectSummary.getIntellmerit()),
                new FileReference(extractFolderPath(projectSummary.getFilePath()), projectSummary.getFilePath(), projectSummary.getPageCount(), projectSummary.getLastUpdatedTmsp())
        );
    }

    private static gov.nsf.psm.proposaltransfer.api.model.proposal.ProjectDescription convertProjectDescription(ProjectDescription projectDescription) {
        if (projectDescription == null) {
            throw new IllegalArgumentException("ProjectDescription parameter cannot be null");
        }

        return new gov.nsf.psm.proposaltransfer.api.model.proposal.ProjectDescription(
                new FileReference(extractFolderPath(projectDescription.getFilePath()), projectDescription.getFilePath(), projectDescription.getPageCount(), projectDescription.getLastUpdatedTmsp())
        );
    }


    private static FileReference convertFacilitiesAndEquipment(FacilitiesEquipment facilitiesEquipment) {
        if (facilitiesEquipment == null) {
            throw new IllegalArgumentException("FacilitiesEquipment parameter cannot be null");
        }

        return new FileReference(extractFolderPath(facilitiesEquipment.getFilePath()), facilitiesEquipment.getFilePath(), facilitiesEquipment.getPageCount(), facilitiesEquipment.getLastUpdatedTmsp());
    }

    private static FileReference convertDataManagementPlan(DataManagementPlan dataManagementPlan) {
        if (dataManagementPlan == null) {
            throw new IllegalArgumentException("DataManagementPlan parameter cannot be null");
        }

        return new FileReference(extractFolderPath(dataManagementPlan.getFilePath()), dataManagementPlan.getFilePath(), dataManagementPlan.getPageCount(), dataManagementPlan.getLastUpdatedTmsp());
    }

    private static FileReference convertMentoringPlan(PostDocMentPlan postDocMentPlan) {
        if (postDocMentPlan == null) {
            throw new IllegalArgumentException("PostDocMentPlan parameter cannot be null");
        }

        return new FileReference(extractFolderPath(postDocMentPlan.getFilePath()), postDocMentPlan.getFilePath(), postDocMentPlan.getPageCount(), postDocMentPlan.getLastUpdatedTmsp());
    }

    private static String extractFolderPath(String filePath) {
        if (StringUtils.isEmpty(filePath) || filePath.split("/").length == 1) {
            return filePath;
        }

        String folderPath = "";
        String[] parts = filePath.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            folderPath += parts[i] + "/";
        }

        return folderPath;
    }

    private static String sanitizeString(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }

        return str.trim();
    }

    private String trim(String str){
        return str != null ? str.trim() : str;
    }
}
