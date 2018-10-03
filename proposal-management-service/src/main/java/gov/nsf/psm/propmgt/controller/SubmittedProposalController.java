package gov.nsf.psm.propmgt.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.nsf.components.rest.model.response.CollectionResponse;
import gov.nsf.psm.foundation.controller.PsmBaseController;
import gov.nsf.psm.foundation.ember.model.EmberModel;
import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.Section;
import gov.nsf.psm.foundation.model.SectionResponse;
import gov.nsf.psm.foundation.model.proposal.ProposalCompleteTransfer;
import gov.nsf.psm.foundation.model.proposal.ProposalTransfer;
import gov.nsf.psm.propmgt.common.Constants;
import gov.nsf.psm.propmgt.service.ProposalManagementForTransferService;
import gov.nsf.psm.propmgt.service.ProposalManagementService;
import gov.nsf.psm.proposaltransfer.api.exception.ProposalTransferRequestException;
import gov.nsf.psm.proposaltransfer.api.model.ProposalTransferRequest;
import gov.nsf.psm.proposaltransfer.api.model.RequestParams;
import gov.nsf.psm.proposaltransfer.client.ProposalTransferServiceClient;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@RequestMapping(path = "/internal/api/v1/submitted")
@ApiResponses(value = { @ApiResponse(code = 404, message = "Resource not found"),
        @ApiResponse(code = 500, message = "Internal server error") })
public class SubmittedProposalController extends PsmBaseController {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ProposalManagementService proposalManagementService;

    @Autowired
    ProposalManagementForTransferService proposalManagementForTransferService;

    @Autowired
    ProposalTransferServiceClient proposalTransferRequestService;

    @ApiOperation(value = "Get COA section document (PDF)", notes = "This API returns a PDF file for the Collaborators and Other Affiliates (COA) document for a senior key person on a proposal", response = ByteArrayOutputStream.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful get of file", response = ByteArrayOutputStream.class) })
    @RequestMapping(path = "/proposal/coa/{propPrepId}/{revisionId}/{propPersId}/file.pdf", method = RequestMethod.GET)
    @ResponseBody
    public void getCOASectionFile(@PathVariable String propPrepId, @PathVariable String revisionId,
            @PathVariable String propPersId, HttpServletResponse response) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.getCOASectionFile()");
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

    @ApiOperation(value = "Get proposal section document (PDF)", notes = "This API returns a PDF file for the requested section on a proposal", response = ByteArrayOutputStream.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful get of file", response = ByteArrayOutputStream.class) })
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
            response.setContentType("application/pdf");
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }

    @RequestMapping(path = "/proposal/budget/{propPrepId}/{propRevId}/{instId}/file.pdf", method = RequestMethod.GET)
    @ResponseBody
    public void getBudgetPdf(@PathVariable String propPrepId, @PathVariable String propRevId,
            @PathVariable String instId, HttpServletResponse response) throws CommonUtilException {
        LOGGER.debug("ProposalManagementServiceController.getProposalFile()");
        try {
            ByteArrayOutputStream outputStream = proposalManagementService.getBudgetPdf(propPrepId, propRevId, instId,
                    false);
            response.setContentType("application/pdf");
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
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
            response.setContentType("application/pdf");
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }

    @ApiOperation(value = "Get proposal personnel document (PDF)", notes = "This API returns a personnel document (Biographical Sketch, Current & Pending Support) for the requested senior key person on a proposal", response = ByteArrayOutputStream.class)
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

    @ApiOperation(value = "Get Submitted Proposal Section Pdf", notes = "This API returns a Submitted Proposal Section Pdf (Cover Sheet, Biographical Sketch, Current & Pending Support,COA, Budget etc..)", response = ByteArrayOutputStream.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful get of file", response = ByteArrayOutputStream.class) })
    @RequestMapping(path = "/proposal/{nsfPropId}/{sectionCode}/file.pdf", method = RequestMethod.GET)
    @ResponseBody
    public void getSubmittedProposalPdf(@PathVariable String nsfPropId, @PathVariable String sectionCode,
            HttpServletResponse response) throws CommonUtilException {

        LOGGER.debug("ProposalManagementServiceController.getSubmittedProposalPdf()");
        try {
            ByteArrayOutputStream outputStream = proposalManagementService.getSubmittedProposalPdf(nsfPropId, null,
                    sectionCode, null, null);
            response.setContentType("application/pdf");
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }

    @ApiOperation(value = "Get Submitted Proposal Section Pdf based on Temporary Proposal Id", notes = "This API returns a Submitted Proposal Section Pdf (Cover Sheet, Biographical Sketch, Current & Pending Support,COA, Budget etc..)", response = ByteArrayOutputStream.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful get of file", response = ByteArrayOutputStream.class) })
    @RequestMapping(path = "/section/{nsfTempPropId}/{sectionCode}/file.pdf", method = RequestMethod.GET)
    @ResponseBody
    public void getSubmittedSetionPdf(@PathVariable String nsfTempPropId, @PathVariable String sectionCode,
            HttpServletResponse response) throws CommonUtilException {

        LOGGER.debug("ProposalManagementServiceController.getSubmittedSetionPdf()");
        try {
            ByteArrayOutputStream outputStream = proposalManagementService.getSubmittedProposalPdf(null, nsfTempPropId,
                    sectionCode, null, null);
            response.setContentType("application/pdf");
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }

    @ApiOperation(value = "Get Submitted Proposal Entire Pdf based on Proposal Id", notes = "This API returns a Submitted Proposal Entire Pdf.", response = ByteArrayOutputStream.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful get of file", response = ByteArrayOutputStream.class) })
    @RequestMapping(path = "/proposal/pdf/{nsfPropId}/file.pdf", method = RequestMethod.GET)
    @ResponseBody
    public void getProposalPdf(@PathVariable String nsfPropId, HttpServletResponse response)
            throws CommonUtilException {

        LOGGER.debug("ProposalManagementServiceController.getProposalPdf()");
        try {
            ByteArrayOutputStream outputStream = proposalManagementService.getProposalPdf(nsfPropId);
            response.setContentType("application/pdf");
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }

    @ApiOperation(value = "Get Submitted Proposal Entire Pdf based on Proposal Id for External Users", notes = "This API returns a Submitted Proposal Entire Pdf.", response = ByteArrayOutputStream.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful get of file", response = ByteArrayOutputStream.class) })
    @RequestMapping(path = "/proposal/pdf/externalview/{nsfPropId}/file.pdf", method = RequestMethod.GET)
    @ResponseBody
    public void getProposalPdfForExternalUsers(@PathVariable String nsfPropId, HttpServletResponse response)
            throws CommonUtilException {

        LOGGER.debug("ProposalManagementServiceController.getProposalPdfForExternalUsers()");
        try {
            ByteArrayOutputStream outputStream = proposalManagementService.getProposalPdfForExternalUsers(nsfPropId);
            response.setContentType("application/pdf");
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }

    @ApiOperation(value = "Get Submitted Proposal Entire Pdf based on Temporary Proposal Id", notes = "This API returns a Submitted Proposal's Enitre Pdf", response = ByteArrayOutputStream.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful get of file", response = ByteArrayOutputStream.class) })
    @RequestMapping(path = "/proposal/entirepdf/{nsfTempPropId}/file.pdf", method = RequestMethod.GET)
    @ResponseBody
    public void getPrintEntirePdfForSubmittedProposal(@PathVariable String nsfTempPropId, HttpServletResponse response)
            throws CommonUtilException {

        LOGGER.debug("ProposalManagementServiceController.getPrintEntirePdfForSubmittedProposal()");
        try {
            /**
             * Following line commented out and calling new method to avoid
             * Single Copy Documents (ie. COA, SRL & RNI) and removing 2nd page
             * of CV.
             **/
            ByteArrayOutputStream outputStream = proposalManagementService
                    .getSubmittedProposalPdfForExternalUsers(nsfTempPropId);
            response.setContentType("application/pdf");
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }

    @ApiOperation(value = "Get proposal for transfer", notes = "This API retrieves proposal data for transfer", response = ProposalTransfer.class)
    @RequestMapping(path = "/proposal/{propPrepId}/{propRevId}/transfer", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public EmberModel getProposalForTransfer(@PathVariable String propPrepId, @PathVariable String propRevId)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementLookupController.getProposalForTransfer()");
        /* TODO: validate that the proposal has been submitted */
        ProposalTransfer proposal = proposalManagementForTransferService.getProposalForTransfer(propPrepId, propRevId);
        return new EmberModel.Builder<>(ProposalTransfer.getClassCamelCaseName(), proposal).build();
    }

    /**
     * Complete Transfer because post-PDT transfer actions failed
     *
     * This endpoint should be used under the following circumstances:
     *
     * Given PSM submits a proposal action (submission/revision): Then PSM saves
     * and signs the proposal SUCCEEDS Then PDT downstream transfer SUCCEEDS
     * Then PSM completeTransfer FAILS (PDF generation/transfer & FL PropID
     * persistence)
     *
     * @param propPrepId
     *            - PSM proposal prep ID
     * @param propRevId
     *            - PSM proposal rev ID
     * @param proposalCompleteTransfer
     *            - This JSON payload should contain the FastLane Proposal ID
     *            and Temp ID generated from PDT transfer
     * @return
     * @throws CommonUtilException
     */
    @ApiOperation(value = "Complete transfer of submitted proposal", notes = "This API complete transfer of submitted proposal", response = ProposalTransfer.class)
    @RequestMapping(path = "/proposal/{propPrepId}/{propRevId}/completeTransfer", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public EmberModel completeTransferOfSubmittedProposal(@PathVariable String propPrepId,
            @PathVariable String propRevId, @RequestBody ProposalCompleteTransfer proposalCompleteTransfer)
            throws CommonUtilException {
        LOGGER.debug("ProposalManagementController.completeTransferOfSubmittedProposal()");
        SectionResponse sectionResponse = proposalManagementService.completeTransfer(propPrepId, propRevId,
                proposalCompleteTransfer);
        return new EmberModel.Builder<>("completeTransferOfSubmittedProposal", sectionResponse).build();
    }

    /**
     * Manual Data Transfer because bad data was sent to PDT resulting in a
     * FAILURE
     *
     * This endpoint should be used under the following circumstances:
     *
     * Given PSM submits a proposal action (submission/revision): Then PSM saves
     * and signs the proposal SUCCEEDS Then PDT downstream transfer FAILS DUE TO
     * BAD DATA
     *
     * PSM will need to fix the bad data in their system then call this
     *
     * @param propPrepId
     * @param propRevId
     * @param nsfId
     * @return
     * @throws CommonUtilException
     */
    @ApiOperation(value = "Manual data transfer to FL of submitted proposal", notes = "This API does manual data transfer of data to fl", response = SectionResponse.class)
    @RequestMapping(path = "/proposal/{propPrepId}/{propRevId}/manualDataTransfer/{nsfId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public EmberModel manualDataTransfer(@PathVariable String propPrepId, @PathVariable String propRevId,
            @PathVariable String nsfId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementController.completeTransferOfSubmittedProposal()");
        SectionResponse sectionResponse = proposalManagementService.transferAndComplete(propPrepId, propRevId, nsfId);
        return new EmberModel.Builder<>("manualDataTransfer", sectionResponse).build();
    }

    /**
     * Manual PDT transfer retry
     *
     * This endpoint should be used under the following circumstances:
     *
     * Given PSM submits a proposal action (submission/revision): Then PSM saves
     * and signs the proposal SUCCEEDS Then PDT downstream transfer FAILS
     * because PDT or FL DB is down
     * 
     * @param propPrepId
     * @param propRevId
     * @param nsfId
     * @return
     * @throws CommonUtilException
     */
    @ApiOperation(value = "Manual data transfer to FL of submitted proposal", notes = "This API does manual retry data transfer of proposals that failed to transfer downstream", response = SectionResponse.class)
    @RequestMapping(path = "/proposal/{propPrepId}/{propRevId}/retryDataTransfer/{nsfId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public EmberModel retryDataTransfer(@PathVariable String propPrepId, @PathVariable String propRevId,
            @PathVariable String nsfId) throws CommonUtilException {
        LOGGER.debug("ProposalManagementController.completeTransferOfSubmittedProposal()");
        SectionResponse sectionResponse = proposalManagementService.retryTransferAndComplete(propPrepId, propRevId,
                nsfId);
        return new EmberModel.Builder<>("retryDataTransfer", sectionResponse).build();
    }

    /**
     * Gets all Proposal Transfer Request filtered by the passed search criteria
     *
     * @return CollectionResponse<ProposalTransferRequest>
     * @throws ProposalTransferRequestException
     */
    @ApiOperation(value = "Get all Proposal Transfer Requests filtered by search criteria", notes = "This API returns all the Proposal Transfer Requests filtered by the search criteria passed as query parameters", response = CollectionResponse.class, responseContainer = "Object")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "Application Id", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "proposalPreparationId", value = "Proposal Id", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "proposalPreparationRevisionId", value = "Proposal Revision Id", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "proposalType", value = "Proposal Type", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "submissionType", value = "Proposal Submission Type", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "collaborationType", value = "Proposal Collaboration Type", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "requestStatus", value = "Proposal Transfer Request Status", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "requestAction", value = "Proposal Transfer Request Action", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "fastLaneProposalId", value = "FastLane Proposal Id", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "fastLaneProposalTempId", value = "FastLane Proposal Temp Id", required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = "Sort", required = false, dataType = "string", paramType = "query") })
    @RequestMapping(path = "/transferrequests", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public CollectionResponse<ProposalTransferRequest> getAllProposalTransferRequests(
            @ApiIgnore @RequestParam Map<String, Object> requestParams,
            @RequestParam(value = RequestParams.SORT, required = false, defaultValue = "") List<String> sortCriteria)
            throws ProposalTransferRequestException {
        return proposalTransferRequestService.getTransferRequests(requestParams, sortCriteria);
    }

    @ApiOperation(value = "Get proposal entire PDF", notes = "This API returns a entire proposal PDF", response = ByteArrayOutputStream.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful get of file", response = ByteArrayOutputStream.class) })
    @RequestMapping(path = "/proposal/entirepdf/{propPrepId}/{revisionId}/file.pdf", method = RequestMethod.GET)
    @ResponseBody
    public void getProposalFile(@PathVariable String propPrepId, @PathVariable String revisionId,
            HttpServletResponse response) throws CommonUtilException {

        try {
            LOGGER.debug("@@@ SubmittedProposalController.getProposalFile() ---- propPrepId : " + propPrepId
                    + " revisionId : " + revisionId);

            ByteArrayOutputStream outputStream = proposalManagementService.getProposalFile(propPrepId, revisionId);
            response.setContentType("application/pdf");
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }

    @ApiOperation(value = "Get Submitted Proposal Entire Pdf based on Temporary Proposal Id for External Users", notes = "This API returns a Submitted Proposal's Enitre Pdf", response = ByteArrayOutputStream.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful get of file", response = ByteArrayOutputStream.class) })
    @RequestMapping(path = "/proposal/entirepdf/externalview/{nsfTempPropId}/file.pdf", method = RequestMethod.GET)
    @ResponseBody
    public void getSubmittedProposalPdfForExternalUsers(@PathVariable String nsfTempPropId,
            HttpServletResponse response) throws CommonUtilException {

        LOGGER.debug("@@@ SubmittedProposalController.getSubmittedProposalPdfForExternalUsers() nsfTempPropId : "
                + nsfTempPropId);
        try {
            ByteArrayOutputStream outputStream = proposalManagementService
                    .getSubmittedProposalPdfForExternalUsers(nsfTempPropId);
            response.setContentType("application/pdf");
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }
}
