package gov.nsf.psm.propmgt.service;

import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.proposal.ProposalTransfer;
import gov.nsf.psm.proposaltransfer.api.model.ProposalTransferRequest;

/**
 * ProposalManagementForTransferService
 */
public interface ProposalManagementForTransferService {

    public ProposalTransfer getProposalForTransfer(String propPrepId, String revId) throws CommonUtilException;

    ProposalTransferRequest manuallyRetryTransfer(String propId, String revId, String userNsfId) throws CommonUtilException;

    public ProposalTransferRequest transferToFastLane(String propId, String revId, String userNsfId) throws CommonUtilException;

}
