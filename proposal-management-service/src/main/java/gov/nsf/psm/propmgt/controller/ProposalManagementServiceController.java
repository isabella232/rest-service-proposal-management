package gov.nsf.psm.propmgt.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.catalina.core.ApplicationPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.common.io.ByteStreams;

import gov.nsf.psm.foundation.controller.PsmBaseController;
import gov.nsf.psm.foundation.ember.model.EmberModel;
import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.FundingOpportunity;
import gov.nsf.psm.foundation.model.Institution;
import gov.nsf.psm.foundation.model.PSMMessage;
import gov.nsf.psm.foundation.model.PSMMessages;
import gov.nsf.psm.foundation.model.ProposalElectronicSign;
import gov.nsf.psm.foundation.model.ProposalUpdateJustification;
import gov.nsf.psm.foundation.model.Section;
import gov.nsf.psm.foundation.model.SectionResponse;
import gov.nsf.psm.foundation.model.budget.InstitutionBudget;
import gov.nsf.psm.foundation.model.coversheet.CoverSheet;
import gov.nsf.psm.foundation.model.lookup.Deadline;
import gov.nsf.psm.foundation.model.lookup.ElectronicCertificationText;
import gov.nsf.psm.foundation.model.proposal.ProposalHeader;
import gov.nsf.psm.foundation.model.proposal.ProposalPackage;
import gov.nsf.psm.foundation.model.proposal.ProposalQueryResult;
import gov.nsf.psm.foundation.model.proposal.ProposalResponse;
import gov.nsf.psm.foundation.model.proposal.ProposalSection;
import gov.nsf.psm.propmgt.common.Constants;
import gov.nsf.psm.propmgt.common.PropMgtMessagesEnum;
import gov.nsf.psm.propmgt.service.ProposalManagementService;
import gov.nsf.psm.propmgt.utility.PropMgtUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/v1")
@ApiResponses(value = { @ApiResponse(code = 404, message = "Resource not found"),
        @ApiResponse(code = 500, message = "Internal server error") })
public class ProposalManagementServiceController extends PsmBaseController {

    @Autowired
    ProposalManagementService proposalManagementService;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @ApiOperation(value = "Get Due date objects ", notes = "This API returns the list of due date objects for a funding opportunity", response = FundingOpportunity.class, responseContainer = "List")
    @RequestMapping(path = "/deadlineDates/{fundingOpId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public EmberModel getDueDates(@PathVariable String fundingOpId) throws CommonUtilException {
        LOGGER.info("ProposalManagementLookupController.getDueDates()");
        List<Deadline> deadlineDates = proposalManagementService.getDueDates(fundingOpId);
        return new EmberModel.Builder<>(Deadline.class, deadlineDates).build();
    }

    @RequestMapping(path = "/proposal", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel createProposal(@RequestBody ProposalPackage proposalPackage,
            @RequestHeader(PropMgtUtil.NSFID) String nsfId) throws CommonUtilException {

        // why proposal is called as proposal package? any specific reasons.
        // TODO: validate the user has PI ROLE
        PropMgtUtil.setAuditFields(proposalPackage, nsfId);
        ProposalPackage responsePackage = proposalManagementService.saveProposal(proposalPackage);
        return new EmberModel.Builder<>(ProposalPackage.getClassCamelCaseName(), responsePackage).build();
    }

    @RequestMapping(path = "/proposal/{propPrepId}/{revisionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel getProposal(@PathVariable String propPrepId, @PathVariable String revisionId,
            @RequestHeader(PropMgtUtil.NSFID) String nsfId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementLookupController.getProposalForm()");
        // same for proposal package.
        ProposalPackage responsePackage = proposalManagementService.getProposalForm(propPrepId, revisionId, nsfId);
        PSMMessages messages = new PSMMessages();
        if ((responsePackage.getPsmMessages() != null)
                && (!responsePackage.getPsmMessages().getPsmMessages().isEmpty())) {
            messages = responsePackage.getPsmMessages();
            responsePackage.setPsmMessages(new PSMMessages());
        }
        return new EmberModel.Builder<>(ProposalPackage.getClassCamelCaseName(), responsePackage)
                .sideLoad(PSMMessages.getJSONName(), messages.getPsmMessages()).build();
    }

    // discussion about whether it is section code first or institution first,
    @RequestMapping(path = "/proposal/{propPrepId}/{revisionId}/{sectionCode}/{instId}/upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel uploadSection(MultipartHttpServletRequest request, @PathVariable String propPrepId,
            @PathVariable String revisionId, @PathVariable String sectionCode, @PathVariable String instId,
            @RequestHeader(PropMgtUtil.NSFID) String nsfId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.uploadSection()");
        // this is not required here as controller is not using the section. if
        // needed a validator can
        // be added to validate the input params.
        Section section = Section.getSection(sectionCode);
        ProposalSection propSection = new ProposalSection();
        PSMMessages messages = new PSMMessages();
        // why not wrap everything in the try just after logger line which will
        // be bring the return also
        // in same block
        try {
            ApplicationPart part = (ApplicationPart) request.getParts().toArray()[0];
            byte[] fileByteArr = ByteStreams.toByteArray(part.getInputStream());
            Map<String, String> metaData = new HashMap<String, String>();
            String origFileName = PropMgtUtil.getOrigFileName(part);
            metaData.put("origFileName", origFileName);
            if (nsfId != null) {
                metaData.put(PropMgtUtil.NSFID, nsfId);
            } else {
                metaData.put(PropMgtUtil.NSFID, "psm");
            }

            messages = proposalManagementService.saveUploadedSection(propPrepId, revisionId, section, instId,
                    fileByteArr, metaData);

            if (!messages.hasError()) {
                propSection = proposalManagementService.getSectionData(propPrepId, revisionId, section);
            }
        } catch (ServletException e) {
            throw new CommonUtilException(Constants.UPLOAD_SECTION_SERVLET_EXCEPTION, e);
            // IOException should have been handled differently as this will
            // pop-up to user.
        } catch (IOException e) {
            throw new CommonUtilException(Constants.UPLOAD_SECTION_IO_EXCEPTION, e);
        } catch (CommonUtilException e) {
            LOGGER.debug("uploadSection", e);
            messages.addMessage(PropMgtMessagesEnum.PM_E_001.getMessage());
            // TODO: ServletException should have handled differently as this
            // will
            // pop-up to user
        }
        return new EmberModel.Builder<ProposalSection>(section.getCamelCaseName(), propSection)
                .sideLoad(PSMMessages.getJSONName(), messages.getPsmMessages()).build();
    }

    @RequestMapping(path = "/proposal/{propPrepId}/{revisionId}/{sectionCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel getSectionData(@PathVariable String propPrepId, @PathVariable String revisionId,
            @PathVariable String sectionCode) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.getSectionData()");
        // not required as this should have been done in Service
        Section section = Section.getSection(sectionCode);
        ProposalSection proposalSection = new ProposalSection();
        PSMMessages psmMessages = new PSMMessages();
        try {
            proposalSection = proposalManagementService.getSectionData(propPrepId, revisionId, section);
            LOGGER.debug("proposalSection :: " + proposalSection);
            List<PSMMessage> warnMsgList = proposalManagementService.getProposalWarningMessages(propPrepId, revisionId,
                    null, section.getCode());
            if (warnMsgList != null && !warnMsgList.isEmpty()) {
                psmMessages.addMessagesList(warnMsgList);
                LOGGER.debug("ProposalManagementServiceController.getSectionData()   PsmMessages : "
                        + psmMessages.getPsmMessages());
            }
        } catch (CommonUtilException e) {
            LOGGER.error(Constants.GET_SECTION_DATA_ERROR, e);
            psmMessages.addMessage(PropMgtMessagesEnum.PM_E_001.getMessage());
        }
        return new EmberModel.Builder<ProposalSection>(section.getCamelCaseName(), proposalSection)
                .sideLoad(PSMMessages.getJSONName(), psmMessages.getPsmMessages()).build();
    }

    @RequestMapping(path = "/proposal/{propPrepId}/{revisionId}/{sectionCode}/file.pdf", method = RequestMethod.GET)
    @ResponseBody
    public void getSectionFile(@PathVariable String propPrepId, @PathVariable String revisionId,
            @PathVariable String sectionCode, HttpServletResponse response) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.getSectionFile()");
        try {
            // getting the section should be moved to service method.
            Section section = Section.getSection(sectionCode);
            ByteArrayOutputStream outputStream = proposalManagementService.getSectionFile(propPrepId, revisionId,
                    section);
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.setContentType("application/pdf");
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }

    @RequestMapping(path = "/proposal/{propPrepId}/{revisionId}/file.pdf", method = RequestMethod.GET)
    @ResponseBody
    public void getProposalFile(@PathVariable String propPrepId, @PathVariable String revisionId,
            HttpServletResponse response) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.getProposalFile()");
        try {
            ByteArrayOutputStream outputStream = proposalManagementService.getProposalFile(propPrepId, revisionId);
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.setContentType("application/pdf");
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }

    @RequestMapping(path = "/proposal/{propPrepId}/{revisionId}/{sectionCode}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel deleteSectionFile(@PathVariable String propPrepId, @PathVariable String revisionId,
            @PathVariable String sectionCode, @RequestHeader(PropMgtUtil.NSFID) String nsfId)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.deleteSectionFile()");
        // getting section should be moved to service code
        Section section = Section.getSection(sectionCode);
        PSMMessages psmMessages = proposalManagementService.deleteSectionFile(propPrepId, revisionId, section, nsfId);
        return new EmberModel.Builder<>(PSMMessages.getJSONName(), psmMessages.getPsmMessages()).build();
    }

    @RequestMapping(path = "/proposal/{propPrepId}/{revisionId}/{sectionCode}/{instId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel getInstitutionBudget(@PathVariable String propPrepId, @PathVariable String revisionId,
            @PathVariable String sectionCode, @PathVariable String instId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementLookupController.getInstitutionBudget()");
        // getting section should be moved to service code
        Section section = Section.getSection(sectionCode);
        InstitutionBudget responsePackage = proposalManagementService.getInstitutionBudget(propPrepId, revisionId,
                section, instId);
        return new EmberModel.Builder<InstitutionBudget>("institutionBudget", responsePackage)
                .sideLoad("message", responsePackage.getPropMessages()).build();
    }

    // business need for Validate only endpoint and if yes than it should be
    // changed full elaborated
    // names.
    @RequestMapping(path = "/validateInstBudget", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel validateInstitutionBudget(@RequestBody InstitutionBudget institutionBudget)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.validateInstitutionBudget");
        List<SectionResponse> instBudgets = new ArrayList<>();
        SectionResponse instBudgetResponse = proposalManagementService.validateInstitutionBudget(institutionBudget);
        instBudgets.add(instBudgetResponse);
        return new EmberModel.Builder<>(SectionResponse.class, instBudgets)
                .sideLoad("message", instBudgetResponse.getMessages()).build();
    }

    // endpoint needs to be change with fully elaborated names and save should
    // be removed from
    // endpoint.
    @RequestMapping(path = "/saveInstBudget", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel saveInstitutionBudget(@RequestBody InstitutionBudget institutionBudget,
            @RequestHeader(PropMgtUtil.NSFID) String nsfId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.saveInstitutionBudget");
        List<SectionResponse> instBudgets = new ArrayList<>();
        PropMgtUtil.setAuditFields(institutionBudget, nsfId);
        SectionResponse instBudgetResponse = proposalManagementService.saveInstitutionBudget(institutionBudget);
        instBudgets.add(instBudgetResponse);
        return new EmberModel.Builder<>(SectionResponse.class, instBudgets)
                .sideLoad("message", instBudgetResponse.getMessages()).build();
    }

    @ApiOperation(value = "Update the proposal", notes = "This API updates the proposal", response = SectionResponse.class)
    @RequestMapping(path = "/proposal/updateProposal", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel updateProposal(@Valid @RequestBody ProposalHeader proposalHeader,
            @RequestHeader(PropMgtUtil.NSFID) String nsfId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.updateProposal");

        SectionResponse response = new SectionResponse();
        PSMMessages psmMessages = new PSMMessages();
        PropMgtUtil.setAuditFields(proposalHeader, nsfId);
        response = proposalManagementService.updateProposal(proposalHeader);
        return new EmberModel.Builder<>(SectionResponse.getClassCamelCaseName(), response)
                .sideLoad(PSMMessages.getJSONName(), psmMessages.getPsmMessages()).build();
    }

    @ApiOperation(value = "Save a coversheet object", notes = "This API saves a coversheet object to the PSM database", response = SectionResponse.class)
    @RequestMapping(path = "/proposal/coverSheet", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel saveCoverSheet(@Valid @RequestBody CoverSheet coverSheet,
            @RequestHeader(PropMgtUtil.NSFID) String nsfId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.saveCoverSheet");
        PropMgtUtil.setAuditFields(coverSheet, nsfId);
        SectionResponse sectionResponse = proposalManagementService.saveCoverSheet(coverSheet);
        return new EmberModel.Builder<>("sectionResponse", sectionResponse).build();
    }

    @ApiOperation(value = "Save a Proposal updated justification object", notes = "This API saves proposal update justification object to the PSM database", response = SectionResponse.class)
    @RequestMapping(path = "/proposal/proposalUpdateJustification", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel saveProposalUpdateJustification(
            @Valid @RequestBody ProposalUpdateJustification proposalUpdateJustification,
            @RequestHeader(PropMgtUtil.NSFID) String nsfId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.saveProposalUpdateJustification");
        PropMgtUtil.setAuditFields(proposalUpdateJustification, nsfId);
        SectionResponse sectionResponse = proposalManagementService
                .saveProposalUpdateJustification(proposalUpdateJustification);

        return new EmberModel.Builder<>("message",
                sectionResponse != null ? sectionResponse.getMessages() : new ArrayList<PSMMessage>()).build();

    }

    @ApiOperation(value = "Get a Propsal Update Justification object", notes = "This API retrieves Proposal update justification object from the PSM database", response = ProposalUpdateJustification.class)
    @RequestMapping(path = "/proposal/proposalUpdateJustification/{propPrepId}/{propRevId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel getProposalUpdateJustification(@PathVariable String propPrepId, @PathVariable String propRevId)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementPersonnelController.getProposalUpdateJustification()");
        ProposalUpdateJustification proposalUpdateJustification = proposalManagementService
                .getProposalUpdateJustification(propPrepId, propRevId);
        return new EmberModel.Builder<>("proposalUpdateJustification", proposalUpdateJustification).build();
    }

    @ApiOperation(value = "Get a coversheet object", notes = "This API retrieves a coversheet object from the PSM database", response = CoverSheet.class)
    @RequestMapping(path = "/proposal/coverSheet/{propPrepId}/{propRevId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel getCoverSheet(@PathVariable String propPrepId, @PathVariable String propRevId)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementPersonnelController.getCoverSheet()");
        CoverSheet coverSheet = proposalManagementService.getCoverSheet(propPrepId, propRevId);
        return new EmberModel.Builder<>("coverSheet", coverSheet).sideLoad("message", coverSheet.getPsmMsgList())
                .build();
    }

    @ApiOperation(value = "Update Awardee Organizatioin", notes = "This API updates the Awardee Organization object to the PSM database", response = SectionResponse.class)
    @RequestMapping(path = "/proposal/updateAwardeeOrg/{propPrepId}/{propRevId}/{coverSheetId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel changeAwardeeOrganization(@PathVariable String propPrepId, @PathVariable String propRevId,
            @PathVariable String coverSheetId, @Valid @RequestBody Institution institution,
            @RequestHeader(PropMgtUtil.NSFID) String nsfId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.changeAwardeeOrganization");
        PropMgtUtil.setAuditFields(institution, nsfId);
        SectionResponse sectionResponse = proposalManagementService.changeAwardeeOrganization(propPrepId, propRevId,
                coverSheetId, institution);
        return new EmberModel.Builder<>("sectionResponse", sectionResponse).build();
    }

    @ApiOperation(value = "Get a list of proposal section statuses", notes = "This API returns a list of all proposal section statuses", responseContainer = "List")
    @RequestMapping(path = "/proposal/{propPrepId}/{revisionId}/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public EmberModel getProposalSectionStatuses(@PathVariable String propPrepId, @PathVariable String revisionId)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.getProposalSectionStatuses()");
        return new EmberModel.Builder<Section>("proposalSections",
                proposalManagementService.getAllSectionStatuses(propPrepId, revisionId)).build();
    }

    @ApiOperation(value = "Get the status for a given proposal section", notes = "This API returns a proposal section status", responseContainer = "List")
    @RequestMapping(path = "/proposal/{propPrepId}/{revisionId}/{instId}/status/{sectionCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public EmberModel getProposalSectionStatus(@PathVariable String propPrepId, @PathVariable String revisionId,
            @PathVariable String instId, @PathVariable String sectionCode) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.getProposalSectionStatuses()");

        return new EmberModel.Builder<Section>("proposalSections",
                proposalManagementService.getSingleSectionStatus(propPrepId, revisionId, instId, sectionCode)).build();
    }

    @ApiOperation(value = "Get a User Organizations List", notes = "This API retrieves user Organizations list from the PSM database", response = CoverSheet.class)
    @RequestMapping(path = "/proposal/getUserOrg/{propPrepId}/{propRevId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel getUserOrganizations(@PathVariable String propPrepId, @PathVariable String propRevId)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementPersonnelController.getUserOrganizations()");

        List<Institution> institutions = proposalManagementService.getUserOrganizations(propPrepId, propRevId);
        return new EmberModel.Builder<>("institution", institutions).build();
    }

    @ApiOperation(value = "Gets a proposal access status", notes = "This API gets a given proposal access status from PSM database", response = ProposalPackage.class)
    @RequestMapping(path = "/proposal/propAccess/{propPrepId}/{propRevId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel getProposalAccess(@PathVariable String propPrepId, @PathVariable String propRevId)
            throws CommonUtilException {
        LOGGER.debug("ProposalDataServiceController.getProposalStatus()");

        ProposalPackage propRes = proposalManagementService.getProposalAccess(propPrepId, propRevId);
        return new EmberModel.Builder<>("proposalPackage", propRes).build();
    }

    @ApiOperation(value = "Save a proposal Access Status", notes = "This API saves a given proposal Access Status to the PSM database", response = SectionResponse.class)
    @RequestMapping(path = "/proposal/propAccess", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel setProposalAccess(@RequestBody ProposalPackage proposalPackage,
            @RequestHeader(PropMgtUtil.NSFID) String nsfId) throws CommonUtilException {
        LOGGER.debug("ProposalDataServiceController.setProposalAccess()");

        PropMgtUtil.setAuditFields(proposalPackage, nsfId);
        SectionResponse sectionResponse = proposalManagementService.setProposalAccess(proposalPackage);
        return new EmberModel.Builder<>(SectionResponse.getClassCamelCaseName(), sectionResponse).build();
    }

    @ApiOperation(value = "Get a list of proposals", notes = "This API retrieves a proposal list from the PSM database based on various parameters", response = CoverSheet.class)
    @RequestMapping(path = "/proposal/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel getProposals(@RequestParam(value = "nsfId", required = true) String nsfId,
            @RequestParam(value = "submitted", required = false) Boolean submitted) throws CommonUtilException {
        LOGGER.debug("ProposalManagementPersonnelController.getProposals()");

        List<ProposalQueryResult> proposals = proposalManagementService.getProposals(nsfId, submitted);
        return new EmberModel.Builder<>("proposal", proposals).build();
    }

    @RequestMapping(path = "/proposal/budget/{propPrepId}/{propRevId}/{instId}/file.pdf", method = RequestMethod.GET)
    @ResponseBody
    public void getBudgetPdf(@PathVariable String propPrepId, @PathVariable String propRevId,
            @PathVariable String instId, HttpServletResponse response) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.getProposalFile()");
        try {
            ByteArrayOutputStream outputStream = proposalManagementService.getBudgetPdf(propPrepId, propRevId, instId,
                    false);
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.setContentType("application/pdf");
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }

    @RequestMapping(path = "/proposal/coversheet/{propPrepId}/{propRevId}/file.pdf", method = RequestMethod.GET)
    @ResponseBody
    public void getCoverSheetPdf(@PathVariable String propPrepId, @PathVariable String propRevId,
            HttpServletResponse response) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.getCoverSheetPdf()");
        try {
            ByteArrayOutputStream outputStream = proposalManagementService.getCoverSheetPdf(propPrepId, propRevId,
                    true);
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.setContentType("application/pdf");
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }

    @ApiOperation(value = "Validate entire proposal", notes = "This API returns the validation results of the entire proposal", responseContainer = "List")
    @RequestMapping(path = "/proposal/validate/{propPrepId}/{propRevId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public EmberModel validateEntireProposal(@PathVariable String propPrepId, @PathVariable String propRevId,
            @RequestHeader(PropMgtUtil.NSFID) String nsfId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementPersonnelController.validateEntireProposal()");
        PSMMessages validationMsgs = proposalManagementService.validateEntireProposal(propPrepId, propRevId, nsfId);
        return new EmberModel.Builder<>("validationMsgs", validationMsgs).build();
    }

    @ApiOperation(value = "Create a proposal revision", notes = "This API creates a proposal revision", response = ProposalResponse.class)
    @RequestMapping(path = "/proposal/{propPrepId}/{propRevId}/{nsfPropId}/revision/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public EmberModel createProposalRevision(@PathVariable String propPrepId, @PathVariable String propRevId,
            @PathVariable String nsfPropId, @RequestHeader(PropMgtUtil.NSFID) String nsfId) throws CommonUtilException {
        ProposalResponse response = proposalManagementService.createProposalRevision(propPrepId, propRevId, nsfPropId,
                nsfId);
        return new EmberModel.Builder<>(SectionResponse.getClassCamelCaseName(), response)
                .sideLoad("message", response.getMessages()).build();
    }

    @ApiOperation(value = "submt Proposal ", notes = "This API submit the proposal to  PSM database", response = SectionResponse.class)
    @RequestMapping(path = "/proposal/submit", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel submitProposal(@Valid @RequestBody ProposalElectronicSign proposalElectronicSign,
            @RequestHeader(PropMgtUtil.NSFID) String nsfId, HttpServletRequest request) throws CommonUtilException {
        String userIP = PropMgtUtil.getUserIPAddress(request);
        if (userIP != null) {
            proposalElectronicSign.setUserIPAddress(userIP);
        }
        PropMgtUtil.setAuditFields(proposalElectronicSign, nsfId);
        proposalElectronicSign.setUserNsfId(nsfId);
        SectionResponse sectionResponse = proposalManagementService.submitProposal(proposalElectronicSign);
        return new EmberModel.Builder<>(SectionResponse.getClassCamelCaseName(), sectionResponse).build();
    }

    @ApiOperation(value = "Get proposal electronic signature for a proposal ", notes = "This API retrieves electronic signature information from database ", response = ProposalPackage.class)
    @RequestMapping(path = "/proposal/electronicSign/{propPrepId}/{propRevId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel getProposalReviewInfoForSubmit(@PathVariable String propPrepId, @PathVariable String propRevId,
            @RequestHeader(PropMgtUtil.NSFID) String nsfId) throws CommonUtilException {

        ProposalPackage responsePackage = proposalManagementService.getProposal(propPrepId, propRevId, nsfId);
        CoverSheet coverSheet = proposalManagementService.getCoverSheetForElectronicSign(propPrepId, propRevId);
        ElectronicCertificationText electronicCertificationText = proposalManagementService
                .getElectronicCertificationText(Constants.ELECTRONIC_CERT_PROPOSAL_TYPE_CODE);

        return new EmberModel.Builder<>("responsePackage", responsePackage).sideLoad("coverSheet", coverSheet)
                .sideLoad("electronicCertificationText", electronicCertificationText).build();
    }

    @ApiOperation(value = "Gets AOR signature ", notes = "This API gets AOR signature from PSM database", response = ProposalElectronicSign.class)
    @RequestMapping(path = "/proposal/aorSignature/{propPrepId}/{propRevId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel getAORSignature(@PathVariable String propPrepId, @PathVariable String propRevId)
            throws CommonUtilException {
        ProposalElectronicSign proposalElectronicSign = proposalManagementService.getAORSignature(propPrepId,
                propRevId);
        return new EmberModel.Builder<>("proposalElectronicSign", proposalElectronicSign).build();
    }

}
