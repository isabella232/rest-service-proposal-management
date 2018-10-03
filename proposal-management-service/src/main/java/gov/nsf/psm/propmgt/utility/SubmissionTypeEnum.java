package gov.nsf.psm.propmgt.utility;

import java.util.List;

import gov.nsf.psm.foundation.model.lookup.BaseLookUp;

public enum SubmissionTypeEnum {
    
    SUBMISSION_TYPE;
    
    private static List<BaseLookUp> submissionTypeLookUp;

    /**
     * 
     * @return the submissionTypeLookUp
     */
    public static List<BaseLookUp> getSubmissionTypeLookUp() {
        return submissionTypeLookUp;
    }

    /**
     * 
     * @param submissionTypeLookUp the submissionTypeLookUp to set
     */
    public static void setSubmissionTypeLookUp(List<BaseLookUp> submissionTypeLookUp) {
        SubmissionTypeEnum.submissionTypeLookUp = submissionTypeLookUp;
    }
    
    public static String getDescription(String code){
        for(BaseLookUp blu : submissionTypeLookUp){
            if(blu.getCode().equals(code)){
                return blu.getDescription();
            }
        }
        return null;
    }
    
    public static String getCode(String description){
        for(BaseLookUp blu : submissionTypeLookUp){
            if(blu.getDescription().equals(description)){
                return blu.getCode();
            }
        }
        return null;
    }    

}
