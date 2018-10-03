package gov.nsf.psm.propmgt.utility;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import gov.nsf.psm.factmodel.PSMMessage;
import gov.nsf.psm.foundation.model.Institution;
import gov.nsf.psm.foundation.model.InstitutionAddress;
import gov.nsf.psm.foundation.model.PSMMessageType;
import gov.nsf.psm.foundation.model.PSMMessages;
import gov.nsf.psm.foundation.model.PSMRole;
import gov.nsf.psm.foundation.model.ProjectSummary;
import gov.nsf.psm.foundation.model.Section;
import gov.nsf.psm.foundation.model.budget.Budget;
import gov.nsf.psm.foundation.model.budget.BudgetRecord;
import gov.nsf.psm.foundation.model.budget.FringeBenefitCost;
import gov.nsf.psm.foundation.model.budget.InstitutionBudget;
import gov.nsf.psm.foundation.model.budget.OtherPersonnelCost;
import gov.nsf.psm.foundation.model.budget.SeniorPersonnelCost;
import gov.nsf.psm.foundation.model.lookup.CollaborativeTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.InstitutionRoleTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.OtherPersonnelRoleTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.ProposalTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.SeniorPersonRoleTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.SubmissionTypeLookUp;
import gov.nsf.psm.foundation.model.proposal.ProposalConfig;
import gov.nsf.psm.foundation.model.proposal.ProposalPermission;

public class MockUtility {
    
    private static int msgId;

    private MockUtility() {
    }
    
    public static Institution getMockInstitution(){
    	Institution psmInstitution = new Institution();
        InstitutionAddress psmInstitutionAddress = new InstitutionAddress();
        psmInstitution.setId("1234567890");
        psmInstitution.setDunsNumber("1234567890");
        psmInstitution.setOrganizationName("Dummy Institution");
        psmInstitution.setTaxId("tin");
        psmInstitution.setTimeZone("");
        psmInstitution.setAddressType("Dummy Address Type");
        psmInstitutionAddress.setStreetAddress("Street Address");
        psmInstitutionAddress.setStreetAddress2("Street Address");
        psmInstitutionAddress.setCityName("Street Address");
        psmInstitutionAddress.setStateCode("VA");
        psmInstitutionAddress.setCountryCode("US");
        psmInstitutionAddress.setPostalCode("20170");
        psmInstitution.setAddress(psmInstitutionAddress);
        return psmInstitution;
    }

    public static List<SubmissionTypeLookUp> getSubmissionTypeLookUp() {
          
          List<SubmissionTypeLookUp> subTypeLookUp = new ArrayList<SubmissionTypeLookUp>();
          
          subTypeLookUp.add(new SubmissionTypeLookUp("01", "Letter of Intent"));
          subTypeLookUp.add(new SubmissionTypeLookUp("02", "Preliminary Proposal"));
          subTypeLookUp.add(new SubmissionTypeLookUp("03", "Full Proposal"));
          subTypeLookUp.add(new SubmissionTypeLookUp("04", "Full Proposal related to a Preliminary Proposal"));
          subTypeLookUp.add(new SubmissionTypeLookUp("05", "Renewal"));
          subTypeLookUp.add(new SubmissionTypeLookUp("06", "Accomplishment Based Renewal"));
          
          return subTypeLookUp;
     }
     
    public static List<ProposalTypeLookUp> getPropoosalTypeLookUp() {
          
          List<ProposalTypeLookUp> propTypeLookUp = new ArrayList<ProposalTypeLookUp>();
          
          propTypeLookUp.add(new ProposalTypeLookUp("01", "Research", "RESEARCH"));
          propTypeLookUp.add(new ProposalTypeLookUp("02", "Rapid Response Research (RAPID) Proposals", "RAPID"));
          propTypeLookUp.add(new ProposalTypeLookUp("03", "Early-concept Grants for Exploratory Research (EAGER)", "EAGER"));
          propTypeLookUp.add(new ProposalTypeLookUp("04", "Research Advanced by Interdisciplinary Research and Engineering (RAISE)", "RAISE"));
          propTypeLookUp.add(new ProposalTypeLookUp("05", "Grant Opportunities for Academic Liaison with Industry (GOALI)", "GOALI"));
          propTypeLookUp.add(new ProposalTypeLookUp("06", "Ideas Lab", "IDEASLAB"));
          propTypeLookUp.add(new ProposalTypeLookUp("07", "Research", "FASED"));
          propTypeLookUp.add(new ProposalTypeLookUp("08", "Conferences", "CONFERENCE"));
          propTypeLookUp.add(new ProposalTypeLookUp("09", "Equipment", "EQUIPMENT"));
          propTypeLookUp.add(new ProposalTypeLookUp("10", "Travel", "TRAVEL"));
          propTypeLookUp.add(new ProposalTypeLookUp("11", "NSF Center Proposals", "CENTER PRO"));
          propTypeLookUp.add(new ProposalTypeLookUp("12", "Major Research Equipment and Facility Construction Proposals", "MAJOR PROP"));
          propTypeLookUp.add(new ProposalTypeLookUp("13", "Fellowship", "FELLOWSHIP"));
          
          return propTypeLookUp;
     }
     
    public static List<CollaborativeTypeLookUp> getCollaborativeType() {

          List<CollaborativeTypeLookUp> collabTypeLookUp = new ArrayList<CollaborativeTypeLookUp>();

          collabTypeLookUp.add(new CollaborativeTypeLookUp("01", "Collaborative Subaward"));
          collabTypeLookUp.add(new CollaborativeTypeLookUp("02", "Non-Collaborative"));
          collabTypeLookUp.add(new CollaborativeTypeLookUp("03", "Collaborative Lead"));
          collabTypeLookUp.add(new CollaborativeTypeLookUp("04", "Collaborative Non-lead"));

          return collabTypeLookUp;
     }
    
    public static List<OtherPersonnelRoleTypeLookUp> getOtherPersonnelRoleTypeLookUp() {

        List<OtherPersonnelRoleTypeLookUp> otherPersonnelRoleTypeList = new ArrayList<OtherPersonnelRoleTypeLookUp>();

        otherPersonnelRoleTypeList.add(new OtherPersonnelRoleTypeLookUp("01", "Postdoctoral Scholars"));
        otherPersonnelRoleTypeList.add(new OtherPersonnelRoleTypeLookUp("02", "Other Professionals"));
        otherPersonnelRoleTypeList.add(new OtherPersonnelRoleTypeLookUp("03", "Graduate Students"));
        otherPersonnelRoleTypeList.add(new OtherPersonnelRoleTypeLookUp("04", "Undergraduate Students"));
        otherPersonnelRoleTypeList.add(new OtherPersonnelRoleTypeLookUp("05", "Administrative/Clerical"));
        otherPersonnelRoleTypeList.add(new OtherPersonnelRoleTypeLookUp("06", "Other"));
        
        return otherPersonnelRoleTypeList;
   }
    
    public static List<SeniorPersonRoleTypeLookUp> getSeniorPersonRoleTypeLookUp() {

        List<SeniorPersonRoleTypeLookUp> seniorPersonRoleList = new ArrayList<SeniorPersonRoleTypeLookUp>();

        seniorPersonRoleList.add(new SeniorPersonRoleTypeLookUp("01", "Principal Investigator", "PI","PI"));
        seniorPersonRoleList.add(new SeniorPersonRoleTypeLookUp("02", "CoPrincipal Investigator", "CoPI","CoPI"));
        seniorPersonRoleList.add(new SeniorPersonRoleTypeLookUp("03", "Other Sr Personnel", "OAU","OAU"));

        return seniorPersonRoleList;
   }
    
    public static List<InstitutionRoleTypeLookUp> getInstitutionRoleTypeLookup() {

        List<InstitutionRoleTypeLookUp> instRoleTypeList = new ArrayList<InstitutionRoleTypeLookUp>();

        instRoleTypeList.add(new InstitutionRoleTypeLookUp("01", "Primary Institution"));
        instRoleTypeList.add(new InstitutionRoleTypeLookUp("02", "Awardee Institution"));
        instRoleTypeList.add(new InstitutionRoleTypeLookUp("03", "SubContract Institution"));

        return instRoleTypeList;
   }
    
    public static ProjectSummary getProjectSummary(){
        
        ProjectSummary ps = new ProjectSummary();
        ps.setPropPrepId("1234567");
        ps.setOrigFileName("projectSummary.pdf");
        ps.setPageCount(1);
        ps.setFilePath("/test/test/test.pdf");
        
        return ps;
    }
    
    public static Budget getBudget(String propPrepId, Section section, String instId) {
        Budget budget = new Budget();
        budget.setPropPrepId(propPrepId);
        budget.setRevnId(1);
        
        List<InstitutionBudget> instBudgetList = new ArrayList<InstitutionBudget>();
        InstitutionBudget instBudget = new InstitutionBudget();
        instBudget.setInstId(instId);
        instBudget.setPropInstRecId(1L);
        instBudget.setPropPrepId(propPrepId);
        instBudget.setPropRevId("1");
        
        List<BudgetRecord> budgetRecordList = new ArrayList<BudgetRecord>();
        
        BudgetRecord budgetRecord1 = new BudgetRecord();
        budgetRecord1.setBudgetYear(1);
        
        List<SeniorPersonnelCost> srPersonnelList = new ArrayList<SeniorPersonnelCost>();
        SeniorPersonnelCost spCost = new SeniorPersonnelCost();
        spCost.setSeniorPersonDollarAmount(new BigDecimal("100.00"));
        spCost.setSeniorPersonFirstName("John");
        spCost.setSeniorPersonInstId(instId);
        spCost.setSeniorPersonJustificationText("This is a test justification");
        spCost.setSeniorPersonLastName("Smith");
        spCost.setSeniorPersonMiddleInitial("A");
        spCost.setSeniorPersonMonthCount("2");
        spCost.setSeniorPersonNsfId("000812345");
        spCost.setSeniorPersonRoleCode("01");
        srPersonnelList.add(spCost);
        
        SeniorPersonnelCost spCost2 = new SeniorPersonnelCost();
        spCost2.setSeniorPersonDollarAmount(new BigDecimal("300.00"));
        spCost2.setSeniorPersonFirstName("Ed");
        spCost2.setSeniorPersonInstId(instId);
        spCost2.setSeniorPersonJustificationText("This is a test justification2");
        spCost2.setSeniorPersonLastName("Pulaski");
        spCost2.setSeniorPersonMonthCount("1");
        spCost2.setSeniorPersonNsfId("000812444");
        spCost2.setSeniorPersonRoleCode("02");
        srPersonnelList.add(spCost2);
        
        List<OtherPersonnelCost> otherPersonnelList = new ArrayList<OtherPersonnelCost>();
        OtherPersonnelCost opCost = new OtherPersonnelCost();
        opCost.setOtherPersonCount(1);
        opCost.setOtherPersonDollarAmount(new BigDecimal("100.00"));
        opCost.setOtherPersonMonthCount(new Long("1"));
        opCost.setOtherPersonTypeCode("03");
        otherPersonnelList.add(opCost);
        
        FringeBenefitCost fbCost = new FringeBenefitCost();
        fbCost.setFringeBenefitBudgJustification("This is a test fringe benefit justification");
        fbCost.setFringeBenefitDollarAmount(new BigDecimal("20.00"));
        
        budgetRecord1.setSrPersonnelList(srPersonnelList);
        budgetRecord1.setOtherPersonnelList(otherPersonnelList);
        budgetRecord1.setFringeBenefitCost(fbCost);
        
        BudgetRecord budgetRecord2 = new BudgetRecord();
        budgetRecord2.setBudgetYear(2);
        
        List<OtherPersonnelCost> otherPersonnelList2 = new ArrayList<OtherPersonnelCost>();
        OtherPersonnelCost opCost2 = new OtherPersonnelCost();
        opCost2.setOtherPersonCount(1);
        opCost2.setOtherPersonDollarAmount(new BigDecimal("5000.00"));
        opCost2.setOtherPersonMonthCount(new Long("6"));
        opCost2.setOtherPersonTypeCode("03");
        otherPersonnelList2.add(opCost2);
        
        FringeBenefitCost fbCost2 = new FringeBenefitCost();
        fbCost2.setFringeBenefitBudgJustification("This is a test fringe benefit justification2");
        fbCost2.setFringeBenefitDollarAmount(new BigDecimal("40.00"));
        
        budgetRecord2.setSrPersonnelList(srPersonnelList);
        budgetRecord2.setOtherPersonnelList(otherPersonnelList);
        budgetRecord2.setFringeBenefitCost(fbCost);
                
        budgetRecordList.add(budgetRecord1);
        budgetRecordList.add(budgetRecord2);
        instBudget.setBudgetRecordList(budgetRecordList);
        instBudgetList.add(instBudget);
        budget.setInstBudget(instBudgetList);
        
        return budget;
        
    }
    
    public static InstitutionBudget getInstitutionBudget(String propPrepId, Section section, String instId) {        
        return getBudget(propPrepId, section, instId).getInstBudget().get(0);
    }
    

    private static PSMRole getPIRole() {
        PSMRole pi = new PSMRole();
        pi.setCode("01");
        pi.setDescription("Principal Investigator");
        pi.setUserDataServiceRole("PI");

        return pi;
    }

    private static PSMRole getSPORole() {
        PSMRole spo = new PSMRole();
        spo.setCode("02");
        spo.setDescription("Sponsored Program Officer");
        spo.setUserDataServiceRole("SPO");

        return spo;
    }

    private static Institution getInstitutionP() {
        Institution inst = new Institution();
        inst.setId("1234567890");
        inst.setOrganizationName("Princeton University");
        inst.setDunsNumber("9872987319823-82371");
        return inst;
    }

    public static Institution getInstitutionM() {
        Institution inst = new Institution();
        inst.setId("0987654321");
        inst.setOrganizationName("University of Maryland College Park");
        inst.setDunsNumber("9872987319823-82371");
        return inst;
    }

    public static List<PSMRole> getPSMRoles() {
        List<PSMRole> psmRoles = new ArrayList<>();
        PSMRole pi = getPIRole();
        PSMRole spo = getSPORole();
        psmRoles.add(pi);
        psmRoles.add(spo);

        return psmRoles;
    }

    public List<String> getProposalOAUs() {
        List<String> proposals = new ArrayList<String>();
        proposals.add("1000");
        proposals.add("1001");
        proposals.add("1002");

        return proposals;
    }
    
    /*
     * Mock User Roles
     * 
     ** Roles
         * 01 - PI
         * 05 - SPO
         * 06 - AOR
     */
    
    public static Map<String, PSMRole> getUserRolesMap() {
        Map<String, PSMRole> userRoleMap = new HashMap<String, PSMRole>();
        
        userRoleMap.put("PI", new PSMRole("01", "Principal Investigator", "PI", "PI"));
        userRoleMap.put("SPO", new PSMRole("05", "Sponsored Program Officer", "SPO", "SPO"));
        userRoleMap.put("AOR", new PSMRole("06", "Authorized Organizational Representative", "AOR", "AOR"));
        
        return userRoleMap;
    }
    
    /*
     * Mock User Permissions
     */
    
    public static Set<ProposalPermission> getUserPermissions(Set<PSMRole> userPSMRoleSet, String propStatusCode) {
        
        Set<ProposalPermission> permissions = new HashSet<ProposalPermission>();
        
        for(PSMRole psmRole : userPSMRoleSet) {
	        String permCode = psmRole.getCode() + propStatusCode;
	        
	        /*
	         * Access User Roles
	         * 01 - PI
	         * 02 - Co-PI
	         * 04 - OAU
	         * 05 - SPO
	         * 06 - AOR
	         * 
	         * Proposal Statuses
	         * 00 - Not Forwarded To SPO
	         * 01 - Forwarded To SPO
	         * 02 - Returned to PI
	         * 03 - Sent to AOR
	         * 04 - Submitted to NSF (TBD)
	         */        
	        //hardcoded role values for mock purposes
	        switch(permCode) {
	        case "0100": 
	            permissions.addAll(getPINotForwardedToSPOPermissions());
	            break;
	        case "0101": 
	            permissions.addAll(getPIForwardedToSPOPermissions());
	            break;
	        case "0102": 
	            permissions.addAll(getPIReturnedToPIPermissions());
	            break;
	        case "0103": 
	            permissions.addAll(getPISentToAORPermissions());
	            break;
	        case "0200": 
	            permissions.addAll(getCoPINotForwardedToSPOPermissions());
	            break;
	        case "0201": 
	            permissions.addAll(getCoPIForwardedToSPOPermissions());
	            break;
	        case "0202": 
	            permissions.addAll(getCoPIReturnedToPIPermissions());
	            break;
	        case "0203": 
	            permissions.addAll(getCoPISentToAORPermissions());
	            break;
	        case "0400": 
	            permissions.addAll(getOAUNotForwardedToSPOPermissions());
	            break;
	        case "0401": 
	            permissions.addAll(getOAUForwardedToSPOPermissions());
	            break;
	        case "0402": 
	            permissions.addAll(getOAUReturnedToPIPermissions());
	            break;
	        case "0403": 
	            permissions.addAll(getOAUSentToAORPermissions());
	            break;
	        case "0501": 
	            permissions.addAll(getSPOViewForwardedToSPOPermissions());
	            break;
	        case "0503": 
	            permissions.addAll(getSPOViewSentToAORPermissions());
	            break;
	// TODO: Difference between an SPO View and SPO Edit?            
	//        case "0501": 
	//            permissions.addAll(getSPOViewEditForwardedToSPOPermissions());
	//            break;
	//        case "0503": 
	//            permissions.addAll(getSPOViewEditSentToAORPermissions());
	//            break;
	        case "0603": 
	            permissions.addAll(getAORSentToAORPermissions());
	            break;
	        default: 
	            permissions.addAll(getDefaultPermissions());
	            break;
	        }
        }  
        return permissions;
        
    }
    
    
    private static Map<Integer, ProposalPermission> getPermissionsTable() {
        //get $userRole and $propStatus
        //construct permissionsMatrix (blackbox)
        //hash or concat ($userRole + $propStatus) --> $permCode
        //permMatrix.get($permCode) -->Set<ProposalPermission>

        /** 1
         * $userRole:  PI - 00
         * $propStatus: Submitted to AOR - 02
         * $permCode: $userRole + $propStatus --> 0002
         * to construct permMatrix, query user_permissions table for $permCode rows
         * iterate through rows to construct userPermissionsList
         * 
         * DB table
         * id row = $permCode
         * each column is an access permission, boolean value
         * if the permissions ever change, we would just need to update the database
         * reconstruct the permMatrix for each user access
         * * expensive, but allows for live permissions checks/changes
         * 
         */
        
        /** 2
         * Code-based role determination
         * if(PI) getPIPermissions()
         * * getPIPermissions() --> if(propStatus == 1), else if(propStatus == 2) ....
         * 
         * or drools?
         */
        Map<Integer, ProposalPermission> permissions = new HashMap<Integer, ProposalPermission>();
        
        permissions.put(1, new ProposalPermission(1, "proposal.create", "Create Proposal"));
        permissions.put(2, new ProposalPermission(2, "proposal.update", "Update Proposal"));
        permissions.put(3, new ProposalPermission(3, "proposal.link", "Link or Unlink Non-Lead Proposal"));
        permissions.put(4, new ProposalPermission(4, "personnel.add.copi", "Add Co-PI"));
        permissions.put(5, new ProposalPermission(5, "personnel.add.osp", "Add Other Senior Personnel"));
        permissions.put(6, new ProposalPermission(6, "personnel.add.oau", "Add Other Authroized Personnel"));
        permissions.put(7, new ProposalPermission(7, "personnel.remove.pi", "Remove PI"));
        permissions.put(8, new ProposalPermission(8, "personnel.remove.copi", "Remove Co-PI"));
        permissions.put(9, new ProposalPermission(9, "personnel.remove.osp", "Remove Other Senior Personnel"));
        permissions.put(10, new ProposalPermission(10, "personnel.remove.oau", "Remove Other Authroized Personnel"));
        permissions.put(11, new ProposalPermission(11, "personnel.change-role.pi-copi", "Change PI Role to Co-PI Role"));
        permissions.put(12, new ProposalPermission(12, "personnel.change-role.pi-osp", "Change PI Role to OSP Role"));
        permissions.put(13, new ProposalPermission(13, "personnel.change-role.copi-pi", "Change Co-PI Role to PI Role"));
        permissions.put(14, new ProposalPermission(14, "personnel.change-role.copi-osp", "Change Co-PI Role to OSP Role"));
        permissions.put(15, new ProposalPermission(15, "personnel.change-role.osp-pi", "Change OSP Role to PI Role"));
        permissions.put(16, new ProposalPermission(16, "personnel.change-role.osp-copi", "Change OSP Role to Co-PI Role"));
        permissions.put(17, 
                new ProposalPermission(17, "personnel.document-upload", "Sr. Personnel Documents Data Entry or Upload"));
        permissions.put(18, new ProposalPermission(18, "budget.modify", "Budget Data Entry"));
        permissions.put(19, new ProposalPermission(19, "budget.justification.upload", "Budget Justification Upload"));
        permissions.put(20, 
                new ProposalPermission(20, "proposal.data.modify", "All Other Proposal Sections Data Entry or Upload"));
        permissions.put(21, new ProposalPermission(21, "proposal.view", "View Proposal"));
        permissions.put(22, new ProposalPermission(22, "proposal.spo.access", "Provide Access to SPO"));
        permissions.put(23, new ProposalPermission(23, "proposal.delete", "Delete Proposal"));
        permissions.put(24, new ProposalPermission(24, "proposal.print", "Print Proposal"));
        permissions.put(25, new ProposalPermission(25, "proposal.send", "Send to AOR"));
        permissions.put(26, new ProposalPermission(26, "proposal.return-pi", "Return to PI"));
        permissions.put(27, new ProposalPermission(27, "proposal.submit", "Submit Proposal to NSF"));
        
        return permissions;
    }
    
    
    private static Set<ProposalPermission> configurePermissions(int[] permCodes) {
    	Set<ProposalPermission> userPermissions = new HashSet<ProposalPermission>();
    	Map<Integer, ProposalPermission> permTable = getPermissionsTable();
    	
    	for(Integer permCode : permCodes) {
    		userPermissions.add(permTable.get(permCode));
    	}
    	return userPermissions;
    }
    
    // Proposal Not Forwarded to SPO 
    
    private static Set<ProposalPermission> getPINotForwardedToSPOPermissions() {        
    	return configurePermissions(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25});
    }
    
    private static Set<ProposalPermission> getCoPINotForwardedToSPOPermissions() {
    	return configurePermissions(new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25});
    }
    
    private static Set<ProposalPermission> getOAUNotForwardedToSPOPermissions() {
    	return configurePermissions(new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 24});
    }
    
    // Proposal Forwarded to SPO 
    
    private static Set<ProposalPermission> getPIForwardedToSPOPermissions() {
    	return configurePermissions(new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24});
    }
    
    
    private static Set<ProposalPermission> getCoPIForwardedToSPOPermissions() {
    	return configurePermissions(new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24});
    }
    
    private static Set<ProposalPermission> getOAUForwardedToSPOPermissions() {
    	return configurePermissions(new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 24});
    }
    
    private static Set<ProposalPermission> getSPOViewForwardedToSPOPermissions() {
    	return configurePermissions(new int[]{21, 24});
    }
    
    private static Set<ProposalPermission> getSPOViewEditForwardedToSPOPermissions() {
    	return configurePermissions(new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26});
    }
    
    
    private static Set<ProposalPermission> getPIReturnedToPIPermissions() {
    	return configurePermissions(new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25});
    }
    
    private static Set<ProposalPermission> getCoPIReturnedToPIPermissions() {
    	return configurePermissions(new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25});
    }
    
    private static Set<ProposalPermission> getOAUReturnedToPIPermissions() {
    	return configurePermissions(new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 24});
    }
    
    private static Set<ProposalPermission> getPISentToAORPermissions() {
    	return configurePermissions(new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24});
    }
    
    private static Set<ProposalPermission> getCoPISentToAORPermissions() {
    	return configurePermissions(new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24});
    }
    
    private static Set<ProposalPermission> getOAUSentToAORPermissions() {
    	return configurePermissions(new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 24});
    }
    
    private static Set<ProposalPermission> getSPOViewSentToAORPermissions() {
    	return configurePermissions(new int[]{21, 24});
    }
    
    private static Set<ProposalPermission> getSPOViewEditSentToAORPermissions() {
    	return configurePermissions(new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24, 26});
    }
    
    private static Set<ProposalPermission> getAORSentToAORPermissions() {
    	return configurePermissions(new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24, 26, 27});
    }
    
    private static Set<ProposalPermission> getDefaultPermissions() {
        return new HashSet<ProposalPermission>();
    }
    
    /*
     * Utility methods
     */
    
    public static int getRandomNumber(int low, int high){
        Random r = new Random();
        return r.nextInt(high-low) + low;    
    }

	public static ProposalConfig loadProposalConfig() {
		ProposalConfig propConfig = new ProposalConfig();
		
        List<Section> sectionList = new ArrayList<Section>();
        sectionList.add(Section.COVER_SHEET);
        //sectionList.add(Section.BIOSKETCH);
        //sectionList.add(Section.COA);
        //sectionList.add(Section.CURR_PEND_SUPP);
        sectionList.add(Section.PROJ_SUMM);
        sectionList.add(Section.PROJ_DESC);
        //sectionList.add(Section.RES_PRIOR_SUPP);
        sectionList.add(Section.REF_CITED);
        sectionList.add(Section.BUDGETS);
        sectionList.add(Section.BUDGET_JUST);
        sectionList.add(Section.FER);
        sectionList.add(Section.DMP);
        sectionList.add(Section.COLLAB_PLAN);
        sectionList.add(Section.MGT_PLAN);
        sectionList.add(Section.PMP);
        sectionList.add(Section.DEV_AUTH);
        //sectionList.add(Section.LOS);
        //sectionList.add(Section.RIS);
        sectionList.add(Section.SRL);
        sectionList.add(Section.RNI);
        //sectionList.add(Section.NNAE);
        sectionList.add(Section.OPBIO);
        sectionList.add(Section.SPD);
        
        propConfig.setSections(sectionList);
		return propConfig;
	}
    
	public static PSMMessages getMockValidationMsgs(){
		PSMMessages msgs = new PSMMessages();
		gov.nsf.psm.foundation.model.PSMMessage msg = new gov.nsf.psm.foundation.model.PSMMessage();
		msg.setId("CV_E_0326");
		msg.setType(PSMMessageType.ERROR);
		msg.setDescription("Equipment has funds for at least one year, but no name");
		
		msgs.addMessage(msg);
		
		return msgs;
	}
}
