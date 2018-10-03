package gov.nsf.psm.propmgt.utility;

import org.springframework.util.StringUtils;

import gov.nsf.psm.foundation.model.COA;
import gov.nsf.psm.foundation.model.proposal.ProposalHeader;
import gov.nsf.psm.foundation.model.proposal.ProposalPackage;

public class PropCopyUtils {
    
    private PropCopyUtils() {
        // Empty constructor
    }
    
    // Method will become unnecessary when ProposalPackage goes away
    public static ProposalHeader populateProposalHeader(ProposalPackage proposalPackage, ProposalHeader header) {

        header.setDeadline(proposalPackage.getDeadline());
        header.setFundingOp(proposalPackage.getFundingOp());
        header.setProposalTitle(proposalPackage.getProposalTitle());
        header.setUocs(proposalPackage.getUocs());
        header.setProposalStatus(proposalPackage.getProposalStatus());
        header.setInstitution(proposalPackage.getInstitution());
        header.setLastUpdatedPgm(proposalPackage.getLastUpdatedPgm());
        header.setLastUpdatedTmsp(proposalPackage.getLastUpdatedTmsp());
        header.setLastUpdatedUser(proposalPackage.getLastUpdatedUser());
        header.setPropPrepId(proposalPackage.getPropPrepId());
        header.setRevNum(proposalPackage.getRevNum());
        header.setPropRevId(proposalPackage.getPropRevId());
        return header;

    }
    
    public static COA copyCOA(COA origCOA) {
        COA coa = new COA();
        coa.setAdvisees(origCOA.getAdvisees());
        coa.setAffiliations(origCOA.getAffiliations());
        coa.setCoaExcelFileName(origCOA.getCoaExcelFileName());
        coa.setCoaExcelFilePath(origCOA.getCoaExcelFilePath());
        coa.setCoaPdfFileName(origCOA.getCoaPdfFileName());
        coa.setCoaPdfFilePath(origCOA.getCoaPdfFilePath());
        coa.setCoaPdfPageCount(origCOA.getCoaPdfPageCount());
        coa.setCoEditors(origCOA.getCoEditors());
        coa.setCollaborators(origCOA.getCollaborators());
        coa.setFile(origCOA.getFile());
        coa.setFilePath(origCOA.getFilePath());
        coa.setPageCount(origCOA.getPageCount());
        coa.setPropPersId(origCOA.getPropPersId());
        coa.setPerson(origCOA.getPerson());
        coa.setPropMessages(origCOA.getPropMessages());
        coa.setPropPrepId(origCOA.getPropPrepId());
        coa.setRelationships(origCOA.getRelationships());
        coa.setPropPrepRevId(origCOA.getPropPrepRevId());
        coa.setSectionCode(origCOA.getSectionCode());
        return coa;
    }
    
    public static String correctDBString(String value) {
        return value != null && !StringUtils.isEmpty(value) ? value.trim() : null;
    }

}
