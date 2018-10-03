package gov.nsf.psm.propmgt.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
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
import gov.nsf.psm.foundation.model.PSMMessage;
import gov.nsf.psm.foundation.model.PSMMessages;
import gov.nsf.psm.foundation.model.Personnel;
import gov.nsf.psm.foundation.model.Personnels;
import gov.nsf.psm.foundation.model.Section;
import gov.nsf.psm.foundation.model.SectionResponse;
import gov.nsf.psm.foundation.model.UploadableProposalSection;
import gov.nsf.psm.foundation.model.compliance.ComplianceResponse;
import gov.nsf.psm.foundation.model.compliance.ss.SpreadsheetModel;
import gov.nsf.psm.foundation.model.proposal.ProposalSection;
import gov.nsf.psm.propmgt.common.Constants;
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
public class ProposalManagementPersonnelController extends PsmBaseController {

    @Autowired
    ProposalManagementService proposalManagementService;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private static final String PERSONNEL = "personnel";
    private static final String SECTIONRESPONSE = "sectionResponse";

    @ApiOperation(value = "Get a list of personnel", notes = "This API retrieves a list of personnel for a given NSF ID and role code. Uses User Data Service.", response = Personnels.class)
    @RequestMapping(path = "/personnel/uds/{nsfId}/{roleCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel getNsfIdDetailsFromUDS(@PathVariable String nsfId, @PathVariable String roleCode)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementPersonnelController.getNsfIdDetailsFromUDS()");
        Personnels personnels = proposalManagementService.getNsfIdDetailsFromUDS(nsfId, roleCode);
        return new EmberModel.Builder<>(PERSONNEL, personnels.getPersonnelsSorted()).build();
    }

    @ApiOperation(value = "Get a list of personnel", notes = "This API retrieves a list of personnel for a given NSF ID. Uses User Data Service.", response = Personnels.class)
    @RequestMapping(path = "/personnel/uds/{nsfId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel getNsfIdDetailsFromUDS(@PathVariable String nsfId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementPersonnelController.getNsfIdDetailsFromUDS()");
        Personnels personnels = proposalManagementService.getNsfIdDetailsFromUDS(nsfId);
        return new EmberModel.Builder<>(PERSONNEL, personnels.getPersonnelsSorted()).build();
    }

    @ApiOperation(value = "Get Personnel by Email Id", notes = "This API retrieves a list of personnel for a given Email Id and Role Code (optional). Uses User Data Service.", response = Personnels.class)
    @RequestMapping(path = "/personnel/uds/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public EmberModel getEmailIdDetailsFromUDS(@RequestParam(value = "email", required = true) String email,
            @RequestParam(value = "roleCode", required = false, defaultValue = "") String roleCode)
            throws CommonUtilException {
        /*
         * Had to create a default value as SF encounters an error with optional
         * parameter
         */
        Personnels personnels = new Personnels();
        if (roleCode != null && !"".equals(roleCode)) {
            personnels = proposalManagementService.getEmailIdDetailsFromUDS(email, roleCode);
        } else {
            personnels = proposalManagementService.getEmailIdDetailsFromUDS(email);
        }
        return new EmberModel.Builder<>(PERSONNEL, personnels.getPersonnelsSorted()).build();
    }

    @ApiOperation(value = "Get a personnel object", notes = "This API retrieves a personnel object from the PSM database", response = Personnel.class)
    @RequestMapping(path = "/proposal/personnel/{propPrepId}/{revisionId}/{propPersId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel getPersonnel(@PathVariable String propPrepId, @PathVariable String revisionId,
            @PathVariable String propPersId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementPersonnelController.getPersonnels()");
        Personnel personnel = proposalManagementService.getPersonnel(propPrepId, revisionId, propPersId);
        return new EmberModel.Builder<>(PERSONNEL, personnel).build();
    }

    @ApiOperation(value = "Get a list of personnel objects", notes = "This API retrieves a list of personnel objects from the PSM database", response = Personnel.class)
    @RequestMapping(path = "/proposal/personnel/{propPrepId}/{revisionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel getPersonnels(@PathVariable String propPrepId, @PathVariable String revisionId)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementPersonnelController.getPersonnels()");
        List<Personnel> personnels = proposalManagementService.getPersonnels(propPrepId, revisionId);
        return new EmberModel.Builder<>(PERSONNEL, personnels).build();
    }

    @ApiOperation(value = "Delete a personnel object", notes = "This API deletes a personnel object from the PSM database", response = SectionResponse.class)
    @RequestMapping(path = "/proposal/personnel/{propPrepId}/{revisionId}/{propPersId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel deletePersonnel(@PathVariable String propPrepId, @PathVariable String revisionId,
            @PathVariable String propPersId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementPersonnelController.deletePersonnel()");
        // Same question about SectionResponse do we actually need it.
        SectionResponse sectionResponse = proposalManagementService.deletePersonnel(propPrepId, revisionId, propPersId);
        return new EmberModel.Builder<>(SECTIONRESPONSE, sectionResponse).build();
    }

    @ApiOperation(value = "Save a personnel object", notes = "This API saves a personnel object to the PSM database", response = SectionResponse.class)
    @RequestMapping(path = "/proposal/personnel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel savePersonnel(@Valid @RequestBody Personnel personnel,
            @RequestHeader(PropMgtUtil.NSFID) String nsfId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.savePersonnel");
        PropMgtUtil.setAuditFields(personnel, nsfId);
        SectionResponse sectionResponse = proposalManagementService.savePersonnel(personnel);
        return new EmberModel.Builder<>(SECTIONRESPONSE, sectionResponse).build();
    }

    @ApiOperation(value = "Upload a proposal section file", notes = "This API takes a proposal section file and saves it to PSM systems", response = ProposalSection.class)
    @RequestMapping(path = "/proposal/personnel/{propPrepId}/{revisionId}/{sectionCode}/{propPersId}/upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel uploadProposalPersonnelSectionFile(MultipartHttpServletRequest request,
            @PathVariable String propPrepId, @PathVariable String revisionId, @PathVariable String sectionCode,
            @PathVariable String propPersId, @RequestHeader(PropMgtUtil.NSFID) String nsfId)
            throws CommonUtilException {
        // NOSONAR
        LOGGER.debug("ProposalManagementServiceController.uploadProposalPersonnelSectionFile()");
        // this is should be moved to service.
        ProposalSection propSection = new ProposalSection();
        Section section = Section.getSection(sectionCode);
        PSMMessages messages = new PSMMessages();

        try { // may be the next 6 lines should be moved to a utility method and
              // define a FileClass for
              // fileByteArr and metaData.
            ApplicationPart part = (ApplicationPart) request.getParts().toArray()[0];
            Map<String, String> metaData = new HashMap<String, String>();
            String origFileName = PropMgtUtil.getOrigFileName(part);
            byte[] fileByteArr = ByteStreams.toByteArray(part.getInputStream());
            LOGGER.debug("*** filename: *** " + origFileName);
            metaData.put("origFileName", origFileName);

            // can it done differently than first getting the messages and than
            // validation if there is no
            // message the Proposal Section Object for response.

            if (nsfId != null) {
                metaData.put(PropMgtUtil.NSFID, nsfId);
            }
            messages = proposalManagementService.saveUploadedSeniorPersonnelDocument(propPrepId, revisionId, propPersId,
                    section, fileByteArr, metaData);

            if (!messages.hasError()) {
                propSection = (ProposalSection) proposalManagementService.getSeniorPersonnelSectionData(propPrepId,
                        revisionId, propPersId, section);
            }
        } catch (IOException e) {
            throw new CommonUtilException(Constants.UPLOAD_SECTION_IO_EXCEPTION, e);
        } catch (ServletException e) {
            throw new CommonUtilException(Constants.UPLOAD_SECTION_SERVLET_EXCEPTION, e);
        }

        return new EmberModel.Builder<ProposalSection>(section.getCamelCaseName(), propSection)
                .sideLoad(PSMMessages.getJSONName(), messages.getPsmMessages()).build();
    }

    // endpoint should be changed to move some parameter locations and remove
    // the fixed file name.
    @ApiOperation(value = "Get a proposal personnel file", notes = "This API retrieves a proposal personnel file (PDF)", response = ByteArrayOutputStream.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful get of file", response = ByteArrayOutputStream.class) })
    @RequestMapping(path = "/proposal/personnel/{propPrepId}/{revisionId}/{sectionCode}/{propPersId}/file.pdf", method = RequestMethod.GET)
    @ResponseBody
    public void getProposalPersonnelSectionFile(@PathVariable String propPrepId, @PathVariable String revisionId,
            @PathVariable String sectionCode, @PathVariable String propPersId, HttpServletResponse response)
            throws CommonUtilException {
        // NOSONAR
        LOGGER.debug("ProposalManagementServiceController.getProposalPersonnelSectionFile()");
        try {
            // this should be moved to service implementation.
            Section section = Section.getSection(sectionCode);
            ByteArrayOutputStream outputStream = proposalManagementService.getSeniorPersonnelSectionFile(propPrepId,
                    revisionId, propPersId, section);
            response.setContentType("application/pdf");
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }

    @ApiOperation(value = "Delete a proposal personnel file", notes = "This API deletes a proposal personnel file from PSM systems", response = PSMMessages.class)
    @RequestMapping(path = "/proposal/personnel/{propPrepId}/{revisionId}/{sectionCode}/{propPersId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel deleteProposalPersonnelSectionFile(@PathVariable String propPrepId,
            @PathVariable String revisionId, @PathVariable String sectionCode, @PathVariable String propPersId,
            @RequestHeader(PropMgtUtil.NSFID) String nsfId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.deleteProposalPersonnelSectionFile()");
        // should be moved to service
        Section section = Section.getSection(sectionCode);
        PSMMessages psmMessages = proposalManagementService.deleteSeniorPersonnelSectionFile(propPrepId, revisionId,
                propPersId, section, nsfId);
        return new EmberModel.Builder<>(PSMMessages.getJSONName(), psmMessages.getPsmMessages()).build();
    }

    // should be modified to move path variables.
    @ApiOperation(value = "Get a proposal personnel file", notes = "This API retrieves a proposal personnel file from PSM systems", response = Object.class)
    @RequestMapping(path = "/proposal/personnel/{propPrepId}/{revisionId}/{sectionCode}/{propPersId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel getProposalPersonnelSectionData(@PathVariable String propPrepId, @PathVariable String revisionId,
            @PathVariable String sectionCode, @PathVariable String propPersId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementPersonnelController.getProposalPersonnelSectionData()");
        // should be moved to service
        Section section = Section.getSection(sectionCode);
        // any specific issue why it was defined as Object()
        Object proposalSection = new Object();
        PSMMessages psmMessages = new PSMMessages();
        proposalSection = proposalManagementService.getSeniorPersonnelSectionData(propPrepId, revisionId, propPersId,
                section);
        // Getting Error/Warning Messages
        // if (sectionCode.equalsIgnoreCase(Section.BIOSKETCH.getCode())) {
        List<PSMMessage> warnMsgList = proposalManagementService.getProposalWarningMessages(propPrepId, revisionId,
                propPersId, sectionCode);

        if (warnMsgList != null && !warnMsgList.isEmpty()) {
            psmMessages.addMessagesList(warnMsgList);
        }
        LOGGER.debug(
                "ProposalManagementServiceController.getSectionData() PsmMessages : " + psmMessages.getPsmMessages());
        // }
        return new EmberModel.Builder<Object>(section.getCamelCaseName(), proposalSection)
                .sideLoad(PSMMessages.getJSONName(), psmMessages.getPsmMessages()).build();
    }

    @ApiOperation(value = "Replace one personnel with another personnel and update the role", notes = "This API replaces a target person with a given person and updates the role", response = SectionResponse.class)
    @RequestMapping(path = "/proposal/personnel/{propPrepId}/{propRevId}/replacePersonnel/{oldPersId}/{newPersId}/{newRoleCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel replacePersonnel(@PathVariable String propPrepId, @PathVariable String propRevId,
            @PathVariable String oldPersId, @PathVariable String newPersId, @PathVariable String newRoleCode,
            @RequestHeader(PropMgtUtil.NSFID) String nsfId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementPersonnelController.replacePersonnel()");
        SectionResponse response = new SectionResponse();
        PSMMessages psmMessages = new PSMMessages();
        response = proposalManagementService.replacePersonnel(propPrepId, propRevId, oldPersId, newPersId, newRoleCode,
                nsfId);

        return new EmberModel.Builder<>(SectionResponse.getClassCamelCaseName(), response)
                .sideLoad(PSMMessages.getJSONName(), psmMessages.getPsmMessages()).build();
    }

    @RequestMapping(path = "/proposal/personnel/coa/preview", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel previewCOA(MultipartHttpServletRequest request) throws CommonUtilException {
        PSMMessages messages = new PSMMessages();
        SpreadsheetModel model = null;
        try {
            ApplicationPart part = (ApplicationPart) request.getParts().toArray()[0];
            byte[] fileByteArr = ByteStreams.toByteArray(part.getInputStream());
            Map<String, String> metaData = new HashMap<String, String>();
            String origFileName = PropMgtUtil.getOrigFileName(part);
            metaData.put("origFileName", origFileName);

            ComplianceResponse response = proposalManagementService.previewCOASection(fileByteArr, metaData);
            model = response.getModel().getSsModel();
            messages = response.getMessages();
        } catch (ServletException e) {
            throw new CommonUtilException(Constants.UPLOAD_SECTION_SERVLET_EXCEPTION, e);
            // IOException should have been handled differently as this will
            // pop-up to user.
        } catch (IOException e) {
            throw new CommonUtilException(Constants.UPLOAD_SECTION_IO_EXCEPTION, e);
        }

        return new EmberModel.Builder<ProposalSection>("acceptRequired", true).sideLoad(model)
                .sideLoad("message", messages.getPsmMessages()).build();

    }

    @RequestMapping(path = "/proposal/personnel/coa/{propPrepId}/{revisionId}/{propPersId}/delete", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel deleteCOA(@PathVariable String propPrepId, @PathVariable String revisionId,
            @PathVariable String propPersId, @RequestHeader(PropMgtUtil.NSFID) String nsfId)
            throws CommonUtilException {
        PSMMessages psmMessages = proposalManagementService.deleteUploadedCOASection(propPrepId, revisionId, propPersId,
                nsfId);
        return new EmberModel.Builder<>(PSMMessages.getJSONName(), psmMessages.getPsmMessages()).build();
    }

    @RequestMapping(path = "/proposal/personnel/coa/{propPrepId}/{revisionId}/{propPersId}/get", method = RequestMethod.GET)
    @ResponseBody
    public EmberModel getCOASectionData(@PathVariable String revisionId, @PathVariable String propPersId)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.getCOASectionData()");
        UploadableProposalSection proposalSection = null;

        PSMMessages psmMessages = new PSMMessages();
        proposalSection = (UploadableProposalSection) proposalManagementService.getCOASectionData(revisionId,
                propPersId);

        return new EmberModel.Builder<Object>(Section.COA.getCamelCaseName(), proposalSection)
                .sideLoad(PSMMessages.getJSONName(), psmMessages.getPsmMessages()).build();
    }

    @RequestMapping(path = "/proposal/personnel/coa/{propPrepId}/{revisionId}/{propPersId}/file.pdf", method = RequestMethod.GET)
    @ResponseBody
    public void getCOASectionFile(@PathVariable String propPrepId, @PathVariable String revisionId,
            @PathVariable String propPersId, HttpServletResponse response) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.getSectionFile()");
        try {
            // getting the section should be moved to service method.
            ByteArrayOutputStream outputStream = proposalManagementService.getUploadedCOASectionFile(propPrepId,
                    revisionId, propPersId);
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.setContentType("application/pdf");
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }

    @RequestMapping(path = "/proposal/personnel/coa/{propPrepId}/{revisionId}/{propPersId}/upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel uploadCOA(MultipartHttpServletRequest request, @PathVariable String propPrepId,
            @PathVariable String revisionId, @PathVariable String propPersId,
            @RequestHeader(PropMgtUtil.NSFID) String nsfId) throws CommonUtilException {
        PSMMessages messages = new PSMMessages();
        SpreadsheetModel model = null;
        try {
            ApplicationPart part = (ApplicationPart) request.getParts().toArray()[0];
            byte[] fileByteArr = ByteStreams.toByteArray(part.getInputStream());
            Map<String, String> metaData = new HashMap<String, String>();
            String origFileName = PropMgtUtil.getOrigFileName(part);
            metaData.put("origFileName", origFileName);

            if (nsfId != null) {
                metaData.put(PropMgtUtil.NSFID, nsfId);
            }
            ComplianceResponse response = proposalManagementService.saveUploadedCOASection(propPrepId, revisionId,
                    propPersId, fileByteArr, metaData);
            model = response.getModel().getSsModel();
            messages = response.getMessages();
        } catch (ServletException e) {
            throw new CommonUtilException(Constants.UPLOAD_SECTION_SERVLET_EXCEPTION, e);
            // IOException should have been handled differently as this will
            // pop-up to user.
        } catch (IOException e) {
            throw new CommonUtilException(Constants.UPLOAD_SECTION_IO_EXCEPTION, e);
        }

        return new EmberModel.Builder<ProposalSection>("message", messages.getPsmMessages()).sideLoad(model).build();

    }

    @ApiOperation(value = "Get a list of proposal section statuses", notes = "This API returns a list of all proposal section statuses", responseContainer = "List")
    @RequestMapping(path = "/proposal/personnel/{propPrepId}/{revisionId}/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public EmberModel getProposalSectionStatuses(@PathVariable String propPrepId, @PathVariable String revisionId)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.getProposalSectionStatuses()");

        return new EmberModel.Builder<Section>("proposalSections",
                proposalManagementService.getAllPersonnelSectionStatuses(propPrepId, revisionId)).build();
    }

    @ApiOperation(value = "Get a list of proposal section statuses", notes = "This API returns a list of all proposal section statuses", responseContainer = "List")
    @RequestMapping(path = "/proposal/personnel/{propPrepId}/{revisionId}/status/personnel", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public EmberModel getProposalSectionStatusesByPersonnel(@PathVariable String propPrepId,
            @PathVariable String revisionId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.getAllSectionStatusesByPersonnel()");
        return new EmberModel.Builder<Section>("personnel",
                proposalManagementService.getAllSectionStatusesByPersonnel(propPrepId, revisionId)).build();
    }

    @ApiOperation(value = "Get the status for a given proposal section", notes = "This API returns a proposal section status", responseContainer = "List")
    @RequestMapping(path = "/proposal/personnel/{propPrepId}/{revisionId}/{sectionCode}/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public EmberModel getProposalSectionStatus(@PathVariable String propPrepId, @PathVariable String revisionId,
            @PathVariable String propPersId, @PathVariable String sectionCode) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.getProposalSectionStatuses()");
        return new EmberModel.Builder<Section>("proposalSections", proposalManagementService
                .getSinglePersonnelSectionStatus(propPrepId, revisionId, propPersId, sectionCode)).build();
    }
}
