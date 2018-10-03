package gov.nsf.psm.propmgt.utility;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import gov.nsf.psm.factmodel.BudgetRecordFactModel;
import gov.nsf.psm.factmodel.EquipmentCostFactModel;
import gov.nsf.psm.factmodel.FringeBenefitCostFactModel;
import gov.nsf.psm.factmodel.IndirectCostFactModel;
import gov.nsf.psm.factmodel.InstitutionBudgetFactModel;
import gov.nsf.psm.factmodel.OtherDirectCostFactModel;
import gov.nsf.psm.factmodel.OtherPersonnelCostFactModel;
import gov.nsf.psm.factmodel.ParticipantSupportCostFactModel;
import gov.nsf.psm.factmodel.SeniorPersonnelCostFactModel;
import gov.nsf.psm.factmodel.TravelCostFactModel;
import gov.nsf.psm.foundation.model.budget.BudgetRecord;
import gov.nsf.psm.foundation.model.budget.EquipmentCost;
import gov.nsf.psm.foundation.model.budget.IndirectCost;
import gov.nsf.psm.foundation.model.budget.InstitutionBudget;
import gov.nsf.psm.foundation.model.budget.OtherPersonnelCost;
import gov.nsf.psm.foundation.model.budget.SeniorPersonnelCost;

public class InstBudgetUtils {

    private InstBudgetUtils() {
        // Private constructor
    }
    
    public static boolean hasOtherPersonnelBudgetRecord(InstitutionBudget instBudget, String code) {
        if(instBudget != null && !StringUtils.isEmpty(code) && instBudget.getBudgetRecordList() != null) {
            for (BudgetRecord rec : instBudget.getBudgetRecordList()) {
                if(rec.getOtherPersonnelList() != null) {
                    for (OtherPersonnelCost cost : rec.getOtherPersonnelList()) {
                        if(cost.getOtherPersonTypeCode().equalsIgnoreCase(code)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public static OtherPersonnelCost getOtherPersonnelBudgetRecord(InstitutionBudget instBudget, String code) {
        if(instBudget != null && !StringUtils.isEmpty(code) && instBudget.getBudgetRecordList() != null) {
            for (BudgetRecord rec : instBudget.getBudgetRecordList()) {
                if(rec.getOtherPersonnelList() != null) {
                    for (OtherPersonnelCost cost : rec.getOtherPersonnelList()) {
                        if(cost.getOtherPersonTypeCode().equalsIgnoreCase(code) && cost.getOtherPersonDollarAmount().compareTo(BigDecimal.ZERO) > 0) {
                            return cost;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public static InstitutionBudgetFactModel getFactModel(InstitutionBudget budget) {
        
        InstitutionBudgetFactModel model = new InstitutionBudgetFactModel();
        model.setBudgetRecordList(new ArrayList<BudgetRecordFactModel>());
        model.setInstId(budget.getInstId());
        model.setInstPropRoleTypeCode(budget.getInstPropRoleTypeCode());
        model.setPropInstRecId(budget.getPropInstRecId());
        model.setPropPrepId(budget.getPropPrepId());
        model.setPropRevId(budget.getPropRevId());
        
        if(budget.getBudgetRecordList() != null) {
            for (BudgetRecord rec : budget.getBudgetRecordList()) {
    
                BudgetRecordFactModel recModel = new BudgetRecordFactModel();
                recModel.setBudgetYear(rec.getBudgetYear());
                
                /* Fringe Benefit Cost */
                if(rec.getFringeBenefitCost() != null) {
                    FringeBenefitCostFactModel frModel = new FringeBenefitCostFactModel();
                    frModel.setFringeBenefitBudgJustification(rec.getFringeBenefitCost().getFringeBenefitBudgJustification());
                    frModel.setFringeBenefitDollarAmount(rec.getFringeBenefitCost().getFringeBenefitDollarAmount());
                    recModel.setFringeBenefitCost(frModel);
                }
                
                /* Other Direct Cost*/
                if(rec.getOtherDirectCost() != null) {
                    OtherDirectCostFactModel odModel = new OtherDirectCostFactModel();
                    odModel.setComputerServicesDollarAmount(rec.getOtherDirectCost().getComputerServicesDollarAmount());
                    odModel.setConsultantServicesDollarAmount(rec.getOtherDirectCost().getConsultantServicesDollarAmount());
                    recModel.setOtherDirectCost(odModel);
                }
                
                /* Participant Support Cost */
                if(rec.getParticipantsSupportCost() != null) {
                    ParticipantSupportCostFactModel psModel = new ParticipantSupportCostFactModel();
                    psModel.setOtherDollarAmount(rec.getParticipantsSupportCost().getOtherDollarAmount());
                    psModel.setParticipantSupportJustificationText(rec.getParticipantsSupportCost().getParticipantSupportJustificationText());
                    psModel.setPartNumberCount(rec.getParticipantsSupportCost().getPartNumberCount());
                    psModel.setStipendDollarAmount(rec.getParticipantsSupportCost().getStipendDollarAmount());
                    psModel.setSubsistenceDollarAmount(rec.getParticipantsSupportCost().getSubsistenceDollarAmount());
                    psModel.setTravelDollarAmount(rec.getParticipantsSupportCost().getTravelDollarAmount());
                    recModel.setParticipantsSupportCost(psModel);
                }
                
                /* Travel Cost */
                if(rec.getTravelCost() != null) {
                    TravelCostFactModel trModel = new TravelCostFactModel();
                    trModel.setDomesticTravelDollarAmount(rec.getTravelCost().getDomesticTravelDollarAmount());
                    trModel.setForeignTravelDollarAmount(rec.getTravelCost().getForeignTravelDollarAmount());
                    trModel.setTravelCostBudgetJustificationText(rec.getTravelCost().getTravelCostBudgetJustificationText());
                    recModel.setTravelCost(trModel);
                }
                
                model.getBudgetRecordList().add(processCostLists(rec, recModel));
                
            }
        }
        
        return model;
        
    }
    
    public static BudgetRecordFactModel processCostLists(BudgetRecord rec, BudgetRecordFactModel recModel){
        
        /* Cost Lists */
        recModel.setEquipmentList(new ArrayList<EquipmentCostFactModel>());
        recModel.setIndirectCostsList(new ArrayList<IndirectCostFactModel>());
        recModel.setOtherPersonnelList(new ArrayList<OtherPersonnelCostFactModel>());
        recModel.setSrPersonnelList(new ArrayList<SeniorPersonnelCostFactModel>());
        
        /* Senior Personnel */
        if(rec.getSrPersonnelList()!=null)
        {
	        for (SeniorPersonnelCost cost : rec.getSrPersonnelList()) {
	            SeniorPersonnelCostFactModel spModel = new SeniorPersonnelCostFactModel();
	            spModel.setHidden(cost.isHidden());
	            spModel.setPropPersId(cost.getPropPersId());
	            spModel.setSeniorPersonDollarAmount(cost.getSeniorPersonDollarAmount());
	            spModel.setSeniorPersonFirstName(cost.getSeniorPersonFirstName());
	            spModel.setSeniorPersonInstId(cost.getSeniorPersonInstId());
	            spModel.setSeniorPersonJustificationText(cost.getSeniorPersonJustificationText());
	            spModel.setSeniorPersonLastName(cost.getSeniorPersonLastName());
	            spModel.setSeniorPersonMiddleInitial(cost.getSeniorPersonMiddleInitial());
	            spModel.setSeniorPersonMonthCount(cost.getSeniorPersonMonthCount());
	            spModel.setSeniorPersonNsfId(cost.getSeniorPersonNsfId());
	            spModel.setSeniorPersonRoleCode(cost.getSeniorPersonRoleCode());
	            recModel.getSrPersonnelList().add(spModel);
	        }
        }

        /* Other Personnel */
        if(rec.getOtherPersonnelList()!=null)
        {
	        for (OtherPersonnelCost cost : rec.getOtherPersonnelList()) {
	            OtherPersonnelCostFactModel opModel = new OtherPersonnelCostFactModel();
	            opModel.setOtherPersonCount(cost.getOtherPersonCount());
	            opModel.setOtherPersonDollarAmount(cost.getOtherPersonDollarAmount());
	            opModel.setOtherPersonMonthCount(cost.getOtherPersonMonthCount());
	            opModel.setOtherPersonTypeCode(cost.getOtherPersonTypeCode());
	            recModel.getOtherPersonnelList().add(opModel);
	        }
        }
        
        
        if(rec.getIndirectCostsList()!=null)
        {
	        /* Indirect Cost */
	        for (IndirectCost cost : rec.getIndirectCostsList()) {
	            IndirectCostFactModel icModel = new IndirectCostFactModel();
	            icModel.setIndirectCostBaseDollarAmount(cost.getIndirectCostBaseDollarAmount());
	            icModel.setIndirectCostBudgetJustificationText(cost.getIndirectCostBudgetJustificationText());
	            icModel.setIndirectCostItemName(cost.getIndirectCostItemName());
	            icModel.setIndirectCostRate(cost.getIndirectCostRate());
	            recModel.getIndirectCostsList().add(icModel);
	        }
        }
        
        if(rec.getEquipmentList()!=null)
        {
	        /* Equipment Cost */
	        for (EquipmentCost cost : rec.getEquipmentList()) {
	            EquipmentCostFactModel ecModel = new EquipmentCostFactModel();
	            ecModel.setEquipmentCostBudgetJsutificationText(cost.getEquipmentCostBudgetJsutificationText());
	            ecModel.setEquipmentDollarAmount(cost.getEquipmentDollarAmount());
	            ecModel.setEquipmentName(cost.getEquipmentName());
	            recModel.getEquipmentList().add(ecModel);
	        }
        }
        
        return recModel;
    }
}
