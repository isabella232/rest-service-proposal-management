package gov.nsf.psm.propmgt.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nsf.psm.factmodel.BiographicalSketchFactModel;
import gov.nsf.psm.factmodel.BudgetJustificationFactModel;
import gov.nsf.psm.factmodel.BudgetRevisionImpactFactModel;
import gov.nsf.psm.factmodel.COAFactModel;
import gov.nsf.psm.factmodel.CurrentAndPendingSupportFactModel;
import gov.nsf.psm.factmodel.DataManagementPlanFactModel;
import gov.nsf.psm.factmodel.DeadlineFactModel;
import gov.nsf.psm.factmodel.FacilitiesEquipmentFactModel;
import gov.nsf.psm.factmodel.FundingOpportunityFactModel;
import gov.nsf.psm.factmodel.InstitutionFactModel;
import gov.nsf.psm.factmodel.PostDocMentPlanFactModel;
import gov.nsf.psm.factmodel.ProjectDescriptionFactModel;
import gov.nsf.psm.factmodel.ProjectSummaryFactModel;
import gov.nsf.psm.factmodel.ProposalFactModel;
import gov.nsf.psm.factmodel.ProposalUpdateJustificationFactModel;
import gov.nsf.psm.factmodel.ReferencesCitedFactModel;
import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.BiographicalSketch;
import gov.nsf.psm.foundation.model.COA;
import gov.nsf.psm.foundation.model.CurrentAndPendingSupport;
import gov.nsf.psm.foundation.model.FundingOpportunity;
import gov.nsf.psm.foundation.model.Institution;
import gov.nsf.psm.foundation.model.Section;
import gov.nsf.psm.foundation.model.SrPersonUploadData;
import gov.nsf.psm.foundation.model.UploadableProposalSection;
import gov.nsf.psm.foundation.model.lookup.Deadline;
import gov.nsf.psm.foundation.model.proposal.ProposalPackage;
import gov.nsf.psm.propmgt.common.Constants;



public class ProposalFactModelUtility {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(ProposalFactModelUtility.class);

	
	private ProposalFactModelUtility() {
        // private constructor
    }

    public static ProposalFactModel getProposalFactModel(Map<Section, UploadableProposalSection> proposalSectionMap, ProposalPackage propPackage, List<SrPersonUploadData>srPersUploadDataList) throws Exception
    {
    	
    	LOGGER.debug("ProposalFactModelUtility.getProposalFactModel()");
    
    	
    	ProposalFactModel propFactModel = new ProposalFactModel();
    	ProjectSummaryFactModel projSummFactModel= new ProjectSummaryFactModel();
    	BudgetJustificationFactModel budgJustFactModel= new BudgetJustificationFactModel(); 
    	ProjectDescriptionFactModel  projDescFactModel = new ProjectDescriptionFactModel();
    	ReferencesCitedFactModel refCitedFactModel = new ReferencesCitedFactModel();
    	DataManagementPlanFactModel dataManagFactModel = new DataManagementPlanFactModel();
    	FacilitiesEquipmentFactModel facilitiesFactModel  = new FacilitiesEquipmentFactModel();
    	PostDocMentPlanFactModel postDocMentPlanFactModel = new PostDocMentPlanFactModel();
    	BudgetRevisionImpactFactModel budgRevImpactFactModel = new BudgetRevisionImpactFactModel();
    	
    	propFactModel.setPropPrepId(propPackage.getPropPrepId());
    	propFactModel.setPropRevId(propPackage.getPropRevId());
    	propFactModel.setProposalTypeName(propPackage.getProposalType());
    	propFactModel.setSubmissionTypeName(propPackage.getSubmissionType());
    
    
      	
    	 for (Entry<Section, UploadableProposalSection> entry : proposalSectionMap.entrySet()) {
    		//Populate Project Summary Section
             if(entry.getKey().equals(Section.PROJ_SUMM))
             {
            	 UploadableProposalSection uploadableProjSummSection = entry.getValue();
            	 LOGGER.debug ("Upload Section Proj Summary-" + uploadableProjSummSection.toString());
            	 projSummFactModel.setFilePath(uploadableProjSummSection.getFilePath());
            	 
             }
             //Populate Budget Justification Section
             if(entry.getKey().equals(Section.BUDGET_JUST))
             {
            	 UploadableProposalSection uploadableBudjSection = entry.getValue();
            	 LOGGER.debug ("Upload Section Budg Justification -" + uploadableBudjSection.toString());
            	 budgJustFactModel.setFilePath(uploadableBudjSection.getFilePath());
            	 
             }
             //Populate Project Description Section 
             if(entry.getKey().equals(Section.PROJ_DESC))
             {
            	 UploadableProposalSection uploadableProjDescSection = entry.getValue();
            	 LOGGER.debug ("Upload Section Project Description-" + uploadableProjDescSection.toString());
            	 projDescFactModel.setFilePath(uploadableProjDescSection.getFilePath());
            	 
             }
             
             //Populate References Cited Section 
             if(entry.getKey().equals(Section.REF_CITED))
             {
            	 UploadableProposalSection uploadableReferenesCitedSection = entry.getValue();
            	 LOGGER.debug ("Upload Section References Cited-" + uploadableReferenesCitedSection.toString());
            	 refCitedFactModel.setFilePath(uploadableReferenesCitedSection.getFilePath());
            	 
             }
             
             //Populate DataManagementPlan Section 
             if(entry.getKey().equals(Section.DMP))
             {
            	 UploadableProposalSection uploadableDataManagementSection = entry.getValue();
            	 LOGGER.debug ("Upload Section DataManagement Plan-" + uploadableDataManagementSection.toString());
            	 dataManagFactModel.setFilePath(uploadableDataManagementSection.getFilePath());
            	 
             }
           //Populate Facilities and Equipment Section
             if(entry.getKey().equals(Section.FER))
             {
            	 UploadableProposalSection uploadableFacilitiesEquipmentSection = entry.getValue();
            	 LOGGER.debug ("Upload Section Faciliies Equipment-" + uploadableFacilitiesEquipmentSection.toString());
            	 facilitiesFactModel.setFilePath(uploadableFacilitiesEquipmentSection.getFilePath());
            	 
             }
             
           //Populate Facilities and PostDoctoral MentoringPlan Section
             if(entry.getKey().equals(Section.PMP))
             {
            	 UploadableProposalSection uploadablePostDocMentoringPlan = entry.getValue();
            	 LOGGER.debug ("Upload Section PostDoc Mentoring Plan -" + uploadablePostDocMentoringPlan.toString());
            	 postDocMentPlanFactModel.setFilePath(uploadablePostDocMentoringPlan.getFilePath());
            	 
             }
             //Populate Budget Impact Statement 
             if(entry.getKey().equals(Section.BUDI))
             {
            	 UploadableProposalSection uploadableSectionBudgetImpact = entry.getValue();
            	 LOGGER.debug ("Upload Section BudgetImpact  -" + uploadableSectionBudgetImpact.toString());
            	 budgRevImpactFactModel.setFilePath(uploadableSectionBudgetImpact.getFilePath());
            	 
             }
             
         }
    	
    	 propFactModel.setProjectSummary(projSummFactModel);
    	 propFactModel.setBudgJustifactionFactModel(budgJustFactModel);
    	 propFactModel.setProjDescFactModel(projDescFactModel);   
    	 propFactModel.setRefCitedFactModel(refCitedFactModel);
    	 propFactModel.setDataManagementPlanFactModel(dataManagFactModel);
    	 propFactModel.setFacilitesEquipFactModel(facilitiesFactModel);
    	 propFactModel.setPostDocMentPlanFactModel(postDocMentPlanFactModel);
    	 propFactModel.setBudgRevImpactFactModel(budgRevImpactFactModel); 
    	 
    	 //Senior Personnel Documents - Biographical Sketches, Current & Pending Support, COA 
    	 List<BiographicalSketchFactModel>  bioFactModelList = new  ArrayList<BiographicalSketchFactModel>();  	 
    	 List<CurrentAndPendingSupportFactModel> currPendingModelList = new ArrayList<CurrentAndPendingSupportFactModel>();
    	 List<COAFactModel> coaFactModelList = new ArrayList<COAFactModel>();
     		
    	 
    	 for (SrPersonUploadData srPersnData : srPersUploadDataList)
    	 {		
    		 LOGGER.debug ("srPersnData -" + srPersnData.toString());
    		 if(srPersnData.getBioSketch()!=null)
    		 {
    			 BiographicalSketch bioSketch = srPersnData.getBioSketch();
    			 BiographicalSketchFactModel bioSketchFactModel = new  BiographicalSketchFactModel();
    			 bioSketchFactModel.setPropPersId(bioSketch.getPerson().getPropPersId());
    			 bioSketchFactModel.setFirstName(bioSketch.getPerson().getFirstName());
    			 bioSketchFactModel.setLastName(bioSketch.getPerson().getLastName());
    			 bioSketchFactModel.setFilePath(bioSketch.getFilePath());
    			 bioSketchFactModel.setPageCount(bioSketch.getPageCount());
     			 bioFactModelList.add(bioSketchFactModel);
    			 
    		 }
    		 
    		 if(srPersnData.getCurrPendSupport()!=null)
    		 {
    			 CurrentAndPendingSupport currtAndPendSupport = srPersnData.getCurrPendSupport();
    			 CurrentAndPendingSupportFactModel currPendSupportFactModel = new CurrentAndPendingSupportFactModel();
    			 currPendSupportFactModel.setPropPersId(currtAndPendSupport.getPerson().getPropPersId());
    			 currPendSupportFactModel.setFirstName(currtAndPendSupport.getPerson().getFirstName());
    			 currPendSupportFactModel.setLastName(currtAndPendSupport.getPerson().getLastName());
    			 currPendSupportFactModel.setFilePath(currtAndPendSupport.getFilePath());
    			 currPendSupportFactModel.setPageCount(currtAndPendSupport.getPageCount());
    			 currPendingModelList.add(currPendSupportFactModel);
    			 
    		 }
    		 if(srPersnData.getCoa()!=null)
    		 {
    			COA collabAffiliator = srPersnData.getCoa();
    			COAFactModel coaFactModel = new COAFactModel();
    			
    			coaFactModel.setPropPersId(collabAffiliator.getPerson().getPropPersId());
    			coaFactModel.setFirstName(collabAffiliator.getPerson().getFirstName());
    			coaFactModel.setLastName(collabAffiliator.getPerson().getLastName());
    			coaFactModel.setFilePath(collabAffiliator.getCoaPdfFileName());
    			coaFactModel.setPageCount(collabAffiliator.getPageCount());
    			coaFactModelList.add(coaFactModel);
    			 
    		 }
    			 
    	 }
    	 propFactModel.setBiogSketchesFactModelList(bioFactModelList);
    	 propFactModel.setCurrPendSuppFactModelList(currPendingModelList);
    	 propFactModel.setCoaFactModelList(coaFactModelList);
    	 
    	 
    	 /*Retrieve the Institution  */
    	 
    	 InstitutionFactModel instFactModel = new InstitutionFactModel();
    	 Institution propPackInstitution =  propPackage.getInstitution();
    	 if(propPackInstitution!=null)
    	 {
	    	 instFactModel.setId(propPackInstitution.getId());
	    	 instFactModel.setDunsNumber(propPackInstitution.getDunsNumber());
	    	 instFactModel.setOrganizationName(propPackInstitution.getOrganizationName());
	    	 instFactModel.setTaxId(propPackInstitution.getTaxId());
	    	 instFactModel.setTimeZone(propPackInstitution.getTimeZone());
	    	 instFactModel.setSamRegistrationStatus(propPackInstitution.getSamRegistrationStatus());

    	 }
    	 propFactModel.setInstitutionFactModel(instFactModel);     	 
    	 
    	/* Funding Opportunity */
    	 FundingOpportunityFactModel fundOpFactModel = new FundingOpportunityFactModel();
    	 FundingOpportunity propPackFundOpportunity = propPackage.getFundingOp();
    	 if(propPackFundOpportunity!=null)
    	 {
	    	 fundOpFactModel.setFundingOpportunityId(propPackFundOpportunity.getFundingOpportunityId());
	    	 fundOpFactModel.setFundingOpportunityTitle(propPackFundOpportunity.getFundingOpportunityTitle());
	    	 fundOpFactModel.setFundingOpportunityEffectiveDate(propPackFundOpportunity.getFundingOpportnityEffectiveDate());
	    	 fundOpFactModel.setFundingOpportunityExpirationDate(propPackFundOpportunity.getFundingOpportnityExpirationDate());
	    	 fundOpFactModel.setGPG(isProgrmAnncGpg(propPackFundOpportunity.getFundingOpportunityId()));
	    	 fundOpFactModel.setCurrentGPGVersion(true);
    	 }	
    	 propFactModel.setFundingOppFactModel(fundOpFactModel);
    	 
    	 /* Deadline Information */
    	 DeadlineFactModel deadlineFactModel = new DeadlineFactModel();
    	 Deadline propPackDeadline = propPackage.getDeadline();
    	 if(propPackDeadline!=null)
    	 {
	    	 deadlineFactModel.setDeadlineDate(propPackDeadline.getDeadlineDate());
	    	 if(deadlineFactModel.getDeadlineDate()!=null)
	    	 {
	    		 Date dueDate = deadlineFactModel.getDeadlineDate();
	    		 //Append the 5:00 pm to the Due Date 
	    		 Calendar cal =Calendar.getInstance();
	    		 cal.setTime(dueDate);
	    		 cal.add(Calendar.HOUR_OF_DAY,17); // this will add 17 hours
	    		 dueDate = cal.getTime();
	    		 deadlineFactModel.setDeadlineDate(dueDate);
	    	 }
	    	 deadlineFactModel.setDeadlineTypeCode(propPackDeadline.getDeadlineTypeCode());
	    	 deadlineFactModel.setDeadlineTypeDesc(propPackDeadline.getDeadlineTypeDesc());
	    	 
    	 }
    	 if(propPackInstitution != null && propPackInstitution.getTimeZone()!=null)
    		 deadlineFactModel.setSubmitTimeStamp(deriveCurrentDateTimefromInstitutionTimezone(propPackInstitution.getTimeZone()));
	     propFactModel.setDeadlineFactModel(deadlineFactModel);
     	
     	return propFactModel;
    }
    
	public static Date deriveCurrentDateTimefromInstitutionTimezone(String timezone) throws ParseException {
		Date currentDateTimeAtInstitutuion = null;
		String sCurrentDate = "";

		SimpleDateFormat sdfFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

		// Truncate the Timezone string within (GMT-5.0)
		int endIndex = timezone.indexOf("(");

		timezone = timezone.substring(0, endIndex);
		timezone = timezone.trim();
		TimeZone tz = TimeZone.getTimeZone(timezone);

		sdfFormat.setTimeZone(tz);

		Calendar cal = Calendar.getInstance();
		Date dt = cal.getTime();

		sCurrentDate = sdfFormat.format(dt); // Convert to String first

		// HH converts hour in 24 hours format (0-23), hh(12 hour format)
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

		currentDateTimeAtInstitutuion = dateFormat.parse(sCurrentDate);
		LOGGER.debug("Current Date and time at Institution : " + currentDateTimeAtInstitutuion);
		return currentDateTimeAtInstitutuion;
	}
	
	/** Check if Pgm Annc is a GPG (NSF XX-1) proposal */
	public static boolean isProgrmAnncGpg(String pgm_annc_id)
	{
	
	  boolean isGPG = false;
	  if(pgm_annc_id!=null)
		{
		  	pgm_annc_id = pgm_annc_id.trim();
		    if ( pgm_annc_id.endsWith("1") && pgm_annc_id.startsWith("NSF") )
		    {
		      int idEnd = pgm_annc_id.indexOf("-")+1;
		      isGPG = "1".equals(pgm_annc_id.substring(idEnd));
		    }
	  }
	  return isGPG ;
	}

}
