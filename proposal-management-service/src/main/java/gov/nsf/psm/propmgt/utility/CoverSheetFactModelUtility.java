package gov.nsf.psm.propmgt.utility;

import java.util.ArrayList;
import java.util.List;

import gov.nsf.psm.factmodel.CoverSheetFactModel;
import gov.nsf.psm.factmodel.InternationalActivitiesFactModel;
import gov.nsf.psm.factmodel.OtherFederalAgenciesFactModel;
import gov.nsf.psm.foundation.model.coversheet.CoverSheet;
import gov.nsf.psm.foundation.model.coversheet.FederalAgency;
import gov.nsf.psm.foundation.model.coversheet.InternationalActyCountry;

public class CoverSheetFactModelUtility {

    private CoverSheetFactModelUtility() {
        // private constructor
    }

    public static CoverSheetFactModel getCoverSheetFactModel(CoverSheet cv) {
        CoverSheetFactModel cvfm = new CoverSheetFactModel();
        cvfm.setCoverSheetId(cv.getCoverSheetId());
        cvfm.setPropPrepId(cv.getPropPrepId());
        cvfm.setPropRevId(cv.getPropRevId());
        cvfm.setOrganizationName(cv.getPpop().getOrganizationName());
        cvfm.setCountryCode(cv.getPpop().getCountryCode());
        cvfm.setStreetAddress(cv.getPpop().getStreetAddress());
        cvfm.setCityName(cv.getPpop().getCityName());
        cvfm.setStateCode(cv.getPpop().getStateCode());
        cvfm.setPostalCode(cv.getPpop().getPostalCode());
        cvfm.setVertebrateAnimal(cv.isVertebrateAnimal());
        cvfm.setVrtbAnimalAPType(cv.getVrtbAnimalAPType());
        cvfm.setiAcucSAppDate(cv.getiAcucSAppDate());
        cvfm.setAnimalWelfareAssuNumber(cv.getAnimalWelfareAssuNumber());
        cvfm.setHumanSubject(cv.isHumanSubject());
        cvfm.setHumanSubjectAssuNumber(cv.getHumanSubjectAssuNumber());
        cvfm.setHumanSubjectAPEType(cv.getHumanSubjectsAPEType());
        cvfm.setiRbAppDate(cv.getiRbAppDate());
        if(cv.getExemptionSubsection()!=null)
        	cvfm.setExemptionSubsection(cv.getExemptionSubsection().trim());
        else
        	
        	cvfm.setExemptionSubsection(cv.getExemptionSubsection());
     
        cvfm.setIntlActivities(cv.isIntlActivities());
        cvfm.setRequestedStartDate(cv.getRequestedStartDate());
        cvfm.setProposalDuration(cv.getProposalDuration());
        setDataForIntActivitiesFactModel(cvfm, cv.getInternationalActyCountries());
        return cvfm;

    }

    public static void setDataForIntActivitiesFactModel(CoverSheetFactModel cvfm, List<InternationalActyCountry> list) {
        List<InternationalActivitiesFactModel> intActivitiesList = new ArrayList<InternationalActivitiesFactModel>();
        if (list != null && !list.isEmpty()) {
            for (InternationalActyCountry ic : list) {
                InternationalActivitiesFactModel fm = new InternationalActivitiesFactModel();
                fm.setIntlActyCountryName(ic.getIntlCountryName());
                fm.setIntlCountryCode(ic.getIntlCountryCode());
                intActivitiesList.add(fm);
            }
        }
        cvfm.setIntActivitiesList(intActivitiesList);
    }

    public static void setDataForOtherFederalAgenciesFactModelData(CoverSheetFactModel cvfm, List<FederalAgency> list) {
        List<OtherFederalAgenciesFactModel> ofList = new ArrayList<OtherFederalAgenciesFactModel>();
        if (list != null && !list.isEmpty()) {
            for (FederalAgency fa : list) {
                OtherFederalAgenciesFactModel of = new OtherFederalAgenciesFactModel();
                of.setFedAgencyNameAbbreviation(fa.getFedAgencyNameAbbreviation());
                ofList.add(of);
            }
        }
        cvfm.setOtherFedAgenciesList(ofList);
    }
}
