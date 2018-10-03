package gov.nsf.psm.propmgt.service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.Institution;
import gov.nsf.psm.foundation.model.PSMMessage;
import gov.nsf.psm.foundation.model.PSMMessages;
import gov.nsf.psm.foundation.model.Personnel;
import gov.nsf.psm.foundation.model.PersonnelData;
import gov.nsf.psm.foundation.model.Personnels;
import gov.nsf.psm.foundation.model.ProposalElectronicSign;
import gov.nsf.psm.foundation.model.ProposalUpdateJustification;
import gov.nsf.psm.foundation.model.Section;
import gov.nsf.psm.foundation.model.SectionResponse;
import gov.nsf.psm.foundation.model.UploadableProposalSection;
import gov.nsf.psm.foundation.model.budget.InstitutionBudget;
import gov.nsf.psm.foundation.model.compliance.ComplianceResponse;
import gov.nsf.psm.foundation.model.coversheet.CoverSheet;
import gov.nsf.psm.foundation.model.lookup.Deadline;
import gov.nsf.psm.foundation.model.lookup.ElectronicCertificationText;
import gov.nsf.psm.foundation.model.lookup.State;
import gov.nsf.psm.foundation.model.proposal.ProposalCompleteTransfer;
import gov.nsf.psm.foundation.model.proposal.ProposalHeader;
import gov.nsf.psm.foundation.model.proposal.ProposalPackage;
import gov.nsf.psm.foundation.model.proposal.ProposalPersonnelSectionStatus;
import gov.nsf.psm.foundation.model.proposal.ProposalQueryResult;
import gov.nsf.psm.foundation.model.proposal.ProposalResponse;
import gov.nsf.psm.foundation.model.proposal.ProposalSection;
import gov.nsf.psm.foundation.model.proposal.ProposalSectionStatus;

public interface ProposalManagementService {

    public ProposalPackage saveProposal(ProposalPackage proposalPackage) throws CommonUtilException;

    public ProposalPackage getProposal(String propPrepId, String revId) throws CommonUtilException;

    public PSMMessages saveUploadedSection(String propPrepId, String revisionId, Section section, String instId,
            byte[] uploadedFile, Map<String, String> metaData) throws CommonUtilException;

    public ProposalSection getSectionData(String propPrepId, String revisionId, Section section)
            throws CommonUtilException;

    public ByteArrayOutputStream getSectionFile(String propPrepId, String revisionId, Section section)
            throws CommonUtilException;

    public PSMMessages deleteSectionFile(String propPrepId, String revisionId, Section section, String nsfId)
            throws CommonUtilException;

    public InstitutionBudget getInstitutionBudget(String propPrepId, String revisionId, Section section, String instId)
            throws CommonUtilException;

    public InstitutionBudget getInstitutionBudgetForValidate(String propPrepId, String revisionId, Section section,
            String instId) throws CommonUtilException;

    public SectionResponse validateInstitutionBudget(InstitutionBudget instBudget) throws CommonUtilException;

    public SectionResponse saveInstitutionBudget(InstitutionBudget instBudget) throws CommonUtilException;

    public Personnels getNsfIdDetailsFromUDS(String nsfId, String roleCode) throws CommonUtilException;

    public Personnels getNsfIdDetailsFromUDS(String nsfId) throws CommonUtilException;

    public Personnels getEmailIdDetailsFromUDS(String emailId) throws CommonUtilException;

    public Personnels getEmailIdDetailsFromUDS(String emailId, String roleCode) throws CommonUtilException;

    public List<Personnel> getPersonnels(String propPrepId, String revisionId) throws CommonUtilException;

    public List<Personnel> getPersonnelsForValidate(String propPrepId, String revisionId) throws CommonUtilException;

    public Personnel getPersonnel(String propPrepId, String revisionId, String nsfId) throws CommonUtilException;

    public SectionResponse savePersonnel(Personnel personnel) throws CommonUtilException;

    public SectionResponse deletePersonnel(String propPrepId, String revisionId, String propPersId)
            throws CommonUtilException;

    public ByteArrayOutputStream getProposalFile(String propPrepId, String revisionId) throws CommonUtilException;

    public PSMMessages saveUploadedSeniorPersonnelDocument(String propPrepId, String revisionId, String propPersId,
            Section section, byte[] uploadedFile, Map<String, String> metaData) throws CommonUtilException;

    public Object getSeniorPersonnelSectionData(String propPrepId, String propRevId, String propPersId, Section section)
            throws CommonUtilException;

    public ByteArrayOutputStream getSeniorPersonnelSectionFile(String propPrepId, String revisionId, String propPersId,
            Section section) throws CommonUtilException;

    public PSMMessages deleteSeniorPersonnelSectionFile(String propPrepId, String revisionId, String propPersId,
            Section section, String nsfId) throws CommonUtilException;

    public SectionResponse replacePersonnel(String propPrepId, String propRevId, String oldPersId, String newPersId,
            String newRoleCode, String nsfId) throws CommonUtilException;

    public SectionResponse updateProposal(ProposalHeader proposalHeader) throws CommonUtilException;

    public ComplianceResponse previewCOASection(byte[] uploadedFile, Map<String, String> metaData)
            throws CommonUtilException;

    public PSMMessages deleteUploadedCOASection(String propPrepId, String revisionId, String propPersId, String nsfId)
            throws CommonUtilException;

    public ByteArrayOutputStream getUploadedCOASectionFile(String propPrepId, String revisionId, String propPersId)
            throws CommonUtilException;

    public ComplianceResponse saveUploadedCOASection(String propPrepId, String revisionId, String propPersId,
            byte[] uploadedFile, Map<String, String> metaData) throws CommonUtilException;

    public CoverSheet getCoverSheet(String propPrepId, String propRevId) throws CommonUtilException;

    public CoverSheet getCoverSheetForValidate(String propPrepId, String propRevId) throws CommonUtilException;

    public SectionResponse saveCoverSheet(CoverSheet coverSheet) throws CommonUtilException;

    public SectionResponse changeAwardeeOrganization(String propPrepId, String propRevId, String coverSheetId,
            Institution institution) throws CommonUtilException;

    public Map<Section, ProposalSectionStatus> getSingleSectionStatus(String propPrepId, String revisionId,
            String instId, String sectionCode) throws CommonUtilException;

    public Map<Section, ProposalSectionStatus> getAllSectionStatuses(String propPrepId, String revisionId)
            throws CommonUtilException;

    public List<Institution> getUserOrganizations(String propPrepId, String propRevId) throws CommonUtilException;

    public ProposalSection getCOASectionData(String propRevId, String propPersId)
            throws NumberFormatException, CommonUtilException;

    public List<State> getStates() throws CommonUtilException;

    public List<Deadline> getDueDates(String fundingOpId) throws CommonUtilException;

    public SectionResponse validateCoversheet(CoverSheet coverSheet) throws CommonUtilException;

    public ProposalPackage getProposalAccess(String propPrepId, String propRevId) throws CommonUtilException;

    public SectionResponse setProposalAccess(ProposalPackage proposalPackage) throws CommonUtilException;

    public Map<Section, ProposalSectionStatus> getSinglePersonnelSectionStatus(String propPrepId, String revisionId,
            String propPersId, String sectionCode) throws CommonUtilException;

    public Map<Section, ProposalSectionStatus> getAllPersonnelSectionStatuses(String propPrepId, String revisionId)
            throws CommonUtilException;

    public ByteArrayOutputStream getBudgetPdf(String propPrepId, String revisionId, String instId,
            boolean isPrintEntire) throws CommonUtilException;

    public ByteArrayOutputStream getCoverSheetPdf(String propPrepId, String propRevId, boolean printPageNumbers)
            throws CommonUtilException;

    public List<ProposalQueryResult> getProposals(String nsfId, Boolean submitted) throws CommonUtilException;

    public PSMMessages validateEntireProposal(String propPrepId, String propRevId, String nsfId)
            throws CommonUtilException;

    public ByteArrayOutputStream getSubmittedProposalPdf(String nsfPropId, String nsfTempPropId, String sectionCode,
            String prepId, String revId) throws CommonUtilException;

    public ByteArrayOutputStream getProposalPdf(String nsfPropId) throws CommonUtilException;

    public ByteArrayOutputStream getProposalPdfForExternalUsers(String nsfPropId) throws CommonUtilException;

    public ByteArrayOutputStream getProposalFileForExternalUsers(String propPrepId, String revisionId)
            throws CommonUtilException;

    public ByteArrayOutputStream getEntirePdfForSubmittedProposal(String nsfTempPropId) throws CommonUtilException;

    public ByteArrayOutputStream getSubmittedProposalPdfForExternalUsers(String nsfTemPropId)
            throws CommonUtilException;

    public List<ProposalPersonnelSectionStatus> getProposalPersonnelStatusBySection(PersonnelData data)
            throws CommonUtilException;

    public List<ProposalPersonnelSectionStatus> getAllSectionStatusesByPersonnel(String propPrepId, String propRevId)
            throws CommonUtilException;

    public Map<Section, UploadableProposalSection> getAllSectionsUploadedFilePath(String propPrepId, String propRevId,
            String propPersId) throws CommonUtilException;

    public SectionResponse validateProposalSections(String propPrepId, String propRevId, String nsfId,
            CoverSheet coverSheet, InstitutionBudget instBudget) throws CommonUtilException;

    public List<PSMMessage> getProposalWarningMessages(String propPrepId, String propRevId, String propPersId,
            String sectionCode) throws CommonUtilException;

    public ProposalResponse createProposalRevision(String propPrepId, String propRevId, String nsfPropId, String nsfId)
            throws CommonUtilException;

    public ProposalResponse createPFURevision(String propPrepId, String propRevId, String nsfPropId, Personnel person)
            throws CommonUtilException;

    public ProposalElectronicSign getAORSignature(String propPrepId, String propRevId) throws CommonUtilException;

    public SectionResponse submitProposal(ProposalElectronicSign proposalElectronicSign) throws CommonUtilException;

    public CoverSheet getCoverSheetForElectronicSign(String propPrepId, String propRevId) throws CommonUtilException;

    public ProposalPackage getProposal(String propPrepId, String propRevId, String nsfId) throws CommonUtilException;

    public ElectronicCertificationText getElectronicCertificationText(String electronicCertTypeCode)
            throws CommonUtilException;

    public SectionResponse completeTransfer(String propId, String revId,
            ProposalCompleteTransfer proposalCompleteTransfer) throws CommonUtilException;

    public SectionResponse retryTransferAndComplete(String propPrepId, String propRevId, String userNsfId)
            throws CommonUtilException;

    public SectionResponse saveProposalUpdateJustification(ProposalUpdateJustification proposalUpdateJustification)
            throws CommonUtilException;

    public ProposalUpdateJustification getProposalUpdateJustification(String propPrepId, String propRevId)
            throws CommonUtilException;

    public SectionResponse transferAndComplete(String propPrepId, String propRevId, String userNsfId)
            throws CommonUtilException;

    public ProposalPackage getProposalForm(String propPrepId, String propRevId, String nsfId)
            throws CommonUtilException;

    public int getRevNumberForRevisedBudget(String propPrepId, String propRevId) throws CommonUtilException;

}
