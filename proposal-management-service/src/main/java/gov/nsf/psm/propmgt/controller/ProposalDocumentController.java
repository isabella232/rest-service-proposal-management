package gov.nsf.psm.propmgt.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.interceptor.security.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.nsf.psm.foundation.controller.PsmBaseController;
import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.Section;
import gov.nsf.psm.propmgt.common.Constants;
import gov.nsf.psm.propmgt.service.ProposalManagementService;
import gov.nsf.psm.propmgt.service.UserDetailsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/v1/proposal/document")
public class ProposalDocumentController extends PsmBaseController {

    @Autowired
    ProposalManagementService proposalManagementService;

    @Autowired
    UserDetailsService userDetailsService;

    @Value("${propmgt.security.enable-secure-endpoints}")
    public Boolean enableSecureEndpoints;

    private static final String AUTH_PARAM = "token";

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(path = "/section/{propPrepId}/{revisionId}/{sectionCode}/file.pdf", method = RequestMethod.POST)
    @ResponseBody
    public void getSectionFile(@PathVariable String propPrepId, @PathVariable String revisionId,
            @PathVariable String sectionCode, @RequestParam(value = AUTH_PARAM, required = false) String token,
            HttpServletResponse response) throws CommonUtilException {
        checkUserToken(token);
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

    @ApiOperation(value = "Get a proposal personnel file", notes = "This API retrieves a proposal personnel file (PDF)", response = ByteArrayOutputStream.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful get of file", response = ByteArrayOutputStream.class) })
    @RequestMapping(path = "/personnel/{propPrepId}/{revisionId}/{sectionCode}/{propPersId}/file.pdf", method = RequestMethod.POST)
    @ResponseBody
    public void getProposalPersonnelSectionFile(@PathVariable String propPrepId, @PathVariable String revisionId,
            @PathVariable String sectionCode, @PathVariable String propPersId,
            @RequestParam(value = AUTH_PARAM, required = false) String token, HttpServletResponse response)
            throws CommonUtilException {
        // NOSONAR
        checkUserToken(token);
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

    @RequestMapping(path = "/personnel/coa/{propPrepId}/{revisionId}/{propPersId}/file.pdf", method = RequestMethod.POST)
    @ResponseBody
    public void getCOASectionFile(@PathVariable String propPrepId, @PathVariable String revisionId,
            @PathVariable String propPersId, @RequestParam(value = AUTH_PARAM, required = false) String token,
            HttpServletResponse response) throws CommonUtilException {
        checkUserToken(token);
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

    @RequestMapping(path = "/coversheet/{propPrepId}/{propRevId}/file.pdf", method = RequestMethod.POST)
    @ResponseBody
    public void getCoverSheetPdf(@PathVariable String propPrepId, @PathVariable String propRevId,
            @RequestParam(value = AUTH_PARAM, required = false) String token, HttpServletResponse response)
            throws CommonUtilException {
        checkUserToken(token);
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

    @RequestMapping(path = "/budget/{propPrepId}/{propRevId}/{instId}/file.pdf", method = RequestMethod.POST)
    @ResponseBody
    public void getBudgetPdf(@PathVariable String propPrepId, @PathVariable String propRevId,
            @PathVariable String instId, @RequestParam(value = AUTH_PARAM, required = false) String token,
            HttpServletResponse response) throws CommonUtilException {
        checkUserToken(token);
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

    @RequestMapping(path = "/proposal/{propPrepId}/{revisionId}/file.pdf", method = RequestMethod.POST)
    @ResponseBody
    public void getProposalFile(@PathVariable String propPrepId, @PathVariable String revisionId,
            @RequestParam(value = AUTH_PARAM, required = false) String token, HttpServletResponse response)
            throws CommonUtilException {
        checkUserToken(token);
        try {
            ByteArrayOutputStream outputStream = proposalManagementService.getProposalFile(propPrepId, revisionId);
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.setContentType("application/pdf");
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }

    @RequestMapping(path = "/externalview/pdf/{propPrepId}/{revisionId}/file.pdf", method = RequestMethod.POST)
    @ResponseBody
    public void getProposalPdfForExternalUsers(@PathVariable String propPrepId, @PathVariable String revisionId,
            @RequestParam(value = AUTH_PARAM, required = false) String token, HttpServletResponse response)
            throws CommonUtilException {
        checkUserToken(token);
        try {
            ByteArrayOutputStream outputStream = proposalManagementService.getProposalFileForExternalUsers(propPrepId,
                    revisionId);
            FileCopyUtils.copy(outputStream.toByteArray(), response.getOutputStream());
            response.setContentType("application/pdf");
            response.flushBuffer();
        } catch (IOException e) {
            throw new CommonUtilException(Constants.GET_FILE_IO_EXCEPTION, e);
        }
    }

    /**
     * Checks if oAuth Token is valid.
     * 
     * @param token
     * @throws AccessDeniedException
     * @throws CommonUtilException
     */
    private void checkUserToken(String token) throws AccessDeniedException, CommonUtilException {
        LOGGER.debug("Enable Secure Endpoints: " + enableSecureEndpoints + "|" + "Token: " + token);
        if (enableSecureEndpoints) {
            LOGGER.debug("Valid Token Check: " + userDetailsService.isValidUserToken(token));
            if (!userDetailsService.isValidUserToken(token)) {
                throw new AccessDeniedException("Invalid token");
            }
        }
    }
}
