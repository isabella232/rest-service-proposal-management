package gov.nsf.psm.propmgt.service;

import gov.nsf.components.rest.model.response.CollectionResponse;
import gov.nsf.components.rest.model.response.ModelResponse;
import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.Institution;
import gov.nsf.psm.foundation.model.PSMRole;
import gov.nsf.psm.foundation.model.Personnel;
import gov.nsf.psm.foundation.model.proposal.ProposalTransfer;
import gov.nsf.psm.propmgt.utility.ProposalTransferConverter;
import gov.nsf.psm.propmgt.utility.ProposalTransferEmailUtility;
import gov.nsf.psm.proposaldata.ProposalDataServiceClient;
import gov.nsf.psm.proposaltransfer.api.exception.ProposalTransferRequestException;
import gov.nsf.psm.proposaltransfer.api.model.ProposalTransferRequest;
import gov.nsf.psm.proposaltransfer.api.model.RequestAction;
import gov.nsf.psm.proposaltransfer.api.model.RequestParams;
import gov.nsf.psm.proposaltransfer.api.model.RequestStatus;
import gov.nsf.psm.proposaltransfer.client.ProposalTransferServiceClient;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * ProposalManagementForTransferServiceImpl
 */
public class ProposalManagementForTransferServiceImpl implements ProposalManagementForTransferService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProposalManagementForTransferServiceImpl.class);

    @Autowired
    ProposalTransferServiceClient proposalTransferService;

    @Autowired
    ProposalDataServiceClient proposalDataServiceClient;

    @Autowired
    ExternalServices externalServices;

    @Autowired
    ProposalTransferEmailUtility proposalTransferEmailUtility;

    @Autowired
    ProposalTransferConverter proposalTransferConverter;


    /**
     * This method retrieves the ProposalTransfer object containing all the necessary information for the transferToFastLane
     * functionality
     *
     * @param propPrepId
     * @param propRevId
     * @return
     * @throws CommonUtilException
     */
    @Override
    public ProposalTransfer getProposalForTransfer(String propPrepId, String propRevId) throws CommonUtilException {
        LOGGER.info("Entering getProposalForTransfer with PropPropId: " + propPrepId + " and RevId: " + propRevId);
        ProposalTransfer proposalTransfer = null;

        try {
            proposalTransfer = proposalDataServiceClient.getProposalForTransfer(propPrepId, propRevId);

            proposalTransfer.getProposalHeader().setPropPrepId(propPrepId);
            proposalTransfer.getProposalHeader().setPropRevId(propRevId);
            proposalTransfer.getProposalHeader()
                    .setAwardeeOrganization(getAwardeeInstitutionById(proposalTransfer.getProposalHeader().getAwardeeOrganization().getId()));
            proposalTransfer.setPersonnels(populatePersonnelInstitution(proposalTransfer.getPersonnels()));

        } catch (Exception ex) {
            LOGGER.error("Failed to get and populate the ProposalTransfer object: ", ex);
            throw new CommonUtilException("Failed to get and populate the ProposalTransfer object: ", ex);
        }

        LOGGER.info("Leaving getProposalForTransfer with Proposal Transfer Object: " + proposalTransfer);

        return proposalTransfer;
    }




    /**
     * This method looks up the PSM proposal by propId/revId, converts it to ProposalTransferRequest
     * and submits the request to the ProposalTransferService to be processed
     *
     * Should the creation or processing of the request fail, an email will be sent out noting this failure
     * Should the transfer succeed, an email will be sent out with the details of the successful transfer
     *
     * @param propId
     * @param revId
     * @param userNsfId
     * @return
     * @throws CommonUtilException
     */
    @Override
    public ProposalTransferRequest transferToFastLane(String propId, String revId, String userNsfId) throws CommonUtilException {
        LOGGER.info("Entering transferToFastLane with PropId: " + propId + " and RevId: " + revId);

        ProposalTransferRequest proposalTransferRequest = null;
        ProposalTransfer proposalTransfer = null;

        try {
            proposalTransfer = this.getProposalForTransfer(propId, revId);
            proposalTransferRequest = proposalTransferConverter.convertToProposalTransferRequest(proposalTransfer);
        } catch (Exception ex) {
            proposalTransferEmailUtility.sendFailureEmail(propId, revId, userNsfId, ExceptionUtils.getStackTrace(ex));
            throw new CommonUtilException("Failed to build ProposalTransferRequest: ", ex);
        }

        //R14 + B14 case
        if (proposalTransfer.getProposalHeader().getRevnType().equals("PUPD")) {
            proposalTransferRequest.setRequestAction(RequestAction.PFU);
            transfer(proposalTransferRequest);
            proposalTransferRequest.setRequestAction(RequestAction.BUDGET_UPDATE);
        }

        return transfer(proposalTransferRequest);

    }

    /**
     * Calls the proposalTransferService.processTransferRequest to transfer the proposal data to the
     * downstream systems
     *
     * @param proposalTransferRequest
     * @return
     * @throws CommonUtilException
     */
    protected ProposalTransferRequest transfer(ProposalTransferRequest proposalTransferRequest) throws CommonUtilException {
        ModelResponse<ProposalTransferRequest> transferResponse = null;

        LOGGER.info("Calling processTransferRequest with " + proposalTransferRequest.toString());

        try {
            transferResponse = proposalTransferService.processTransferRequest(proposalTransferRequest);
        } catch (Exception ex) {
            proposalTransferEmailUtility.sendFailureEmail(proposalTransferRequest, ExceptionUtils.getStackTrace(ex));
            throw new CommonUtilException("Transferring the PSM proposal to FastLane failed with exception: ", ex);
        }

        LOGGER.info("Received response " + transferResponse.toString());

        if (transferResponse.hasErrors()) {
            proposalTransferEmailUtility.sendFailureEmail(transferResponse.hasData() ? transferResponse.getData() : proposalTransferRequest, transferResponse.getErrors());
            throw new CommonUtilException("Transferring the PSM proposal to FastLane failed with errors: " + transferResponse);
        }

        if (transferResponse.getData().getRequestStatus() == RequestStatus.FAILED) {
            proposalTransferEmailUtility.sendFailureEmail(transferResponse.getData(), transferResponse.getData().getProcessingInformation().getProcessingException());
            throw new CommonUtilException("Transferring the PSM proposal to FastLane failed with processing error: " + transferResponse.getData().getProcessingInformation().getProcessingException());
        }

        proposalTransferEmailUtility.sendSuccessEmail(transferResponse.getData());

        return transferResponse.getData();
    }

    @Override
    public ProposalTransferRequest manuallyRetryTransfer(String propId, String revId, String userNsfId) throws CommonUtilException {


        ProposalTransferRequest failedRequest = getMostRecentFailedRequest(propId, revId);
        //Case where no failed PDT record exists: create and process new request(s)
        if (failedRequest == null) {
            LOGGER.info("No PDT record was found for propId: " + propId + " revId: " + revId);
            LOGGER.info("Calling transferToFastLane to create new record");

            return transferToFastLane(propId, revId, userNsfId);
        }

        LOGGER.debug("Retrieved the following failed proposal transfer request: " + failedRequest);

        ProposalTransfer proposalTransfer = null;

        try {
            proposalTransfer = this.getProposalForTransfer(propId, revId);
        } catch (Exception ex) {
            proposalTransferEmailUtility.sendFailureEmail(propId, revId, userNsfId, ExceptionUtils.getStackTrace(ex));
            throw new CommonUtilException("Failed to retrieve PSM proposal: ", ex);
        }

        if (proposalTransfer.getProposalHeader().getRevnType().equals("PUPD")) {

            LOGGER.debug("Handling retry for special R14+B14 case");
            //There exists a failed B14 request, this means that the R14 succeeded, but the B14 failed, simply retry this failed request and return
            if (failedRequest.getRequestAction() == RequestAction.BUDGET_UPDATE ){
                return retryTransferRequest(failedRequest);
            }
            //There exists a failed request on the initial R14, retry this, then transfer new B14 request (see below)
            if (failedRequest.getRequestAction() == RequestAction.PFU) {
                retryTransferRequest(failedRequest);
            } else {
                throw new CommonUtilException("RequestAction was neither PFU/BUDGET_UPDATE(R14/B14): Something is seriously wrong");
            }

            ProposalTransferRequest budgetUpdateRequest =
                    proposalTransferConverter.convertToProposalTransferRequest(proposalTransfer, RequestAction.BUDGET_UPDATE);

            return transfer(budgetUpdateRequest);
        }

        return retryTransferRequest(failedRequest);

    }

    protected ProposalTransferRequest retryTransferRequest(ProposalTransferRequest failedRequest) throws CommonUtilException {
        ModelResponse<ProposalTransferRequest> retryResponse = new ModelResponse<ProposalTransferRequest>();

        try {
            retryResponse = proposalTransferService.retryTransferRequest(failedRequest.getId(), true);
        } catch (ProposalTransferRequestException ex) {
            proposalTransferEmailUtility.sendFailureEmail(failedRequest, ExceptionUtils.getStackTrace(ex));
            throw new CommonUtilException("An error occurred calling the PDT manual retry");
        }

        if (retryResponse == null || retryResponse.hasErrors()) {
            proposalTransferEmailUtility.sendFailureEmail(retryResponse.hasData() ? retryResponse.getData() : failedRequest, retryResponse.getErrors());
            throw new CommonUtilException("The manual retry failed with server errors: " + retryResponse);
        }

        if (retryResponse.getData().getRequestStatus() == RequestStatus.FAILED) {
            proposalTransferEmailUtility.sendFailureEmail(retryResponse.getData(), retryResponse.getData().getProcessingInformation().getProcessingException());
            throw new CommonUtilException("The manual retry failed with processing exception: " + retryResponse.getData().getProcessingInformation().getProcessingException());
        }

        proposalTransferEmailUtility.sendSuccessEmail(retryResponse.getData());

        return retryResponse.getData();

    }



    protected ProposalTransferRequest getMostRecentFailedRequest(String propId, String revId) throws CommonUtilException {
        Map<String, Object> searchCriteria = new HashMap<String, Object>();
        searchCriteria.put(RequestParams.PROPOSAL_PREPARATION_ID, propId); //30
        searchCriteria.put(RequestParams.PROPOSAL_PREPARATION_REV_ID, revId); //46
        searchCriteria.put(RequestParams.REQUEST_STATUS, RequestStatus.FAILED);

        CollectionResponse<ProposalTransferRequest> response = null;
        try {
            response = proposalTransferService.getTransferRequests(searchCriteria, Collections.singletonList("-id"));
        } catch (ProposalTransferRequestException ex) {
            throw new CommonUtilException("An error occurred looking up the PDT record", ex);
        }

        if (response == null) {
            throw new CommonUtilException("An error occurred looking up the PDT record: the response was null");
        }

        if (response.hasErrors()) {
            throw new CommonUtilException("An error occurred looking up the PDT record: the response had errors: " + response.getErrors());
        }

        if (response.hasData()) {
            return ((ArrayList<ProposalTransferRequest>) response.getData()).get(0);
        }

        return null;
    }

    /**
     * This method looks up the Institution by its ID
     *
     * @param institutionId
     * @return
     * @throws CommonUtilException
     */
    protected Institution getAwardeeInstitutionById(String institutionId) throws CommonUtilException {

        try {
            return externalServices.getInstitutionById(institutionId);
        } catch (Exception ex) {
            LOGGER.error("Failed to lookup awardeeInsitution by ID: ", ex);
            throw new CommonUtilException("Failed to lookup awardeeInsitution by ID: ", ex);
        }
    }

    /**
     * This method populates the Institution field of each Personnel object
     *
     * @param personnel
     * @return
     * @throws CommonUtilException
     */
    protected List<Personnel> populatePersonnelInstitution(List<Personnel> personnel) throws CommonUtilException {
        try {
            for (Personnel person : personnel) {
                if (PSMRole.ROLE_PI.equals(person.getPSMRole().getCode()) || PSMRole.ROLE_CO_PI.equals(person.getPSMRole().getCode())) {
                    person.setInstitution(externalServices.getInstitutionById(person.getInstitution().getId()));
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to resolve additional PI information: ", ex);
            throw new CommonUtilException("Failed to resolve additional PI information: ", ex);
        }
        return personnel;

    }
}
