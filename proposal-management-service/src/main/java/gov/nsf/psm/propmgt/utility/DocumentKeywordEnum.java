package gov.nsf.psm.propmgt.utility;

import gov.nsf.psm.foundation.model.budget.OtherPersonnelCost;

public enum DocumentKeywordEnum {

    SENIOR_PERSONNEL("Senior Personnel"),
    POSTDOCTORAL_SCHOLARS("Postdoctoral Scholars", OtherPersonnelCost.CODE_STUDENTS_POST_DOCTORAL), 
    OTHER_PROFESSIONALS("Other Professionals", OtherPersonnelCost.CODE_OTHER_PROFESSIONALS), 
    GRADUATE_STUDENTS("Graduate Students",OtherPersonnelCost.CODE_STUDENTS_GRADUATE), 
    UNDERGRADUATE_STUDENTS("Undergraduate Students",OtherPersonnelCost.CODE_STUDENTS_UNDERGRADUATE), 
    ADMINISTRATIVE_CLERICAL("Administrative/Clerical",OtherPersonnelCost.CODE_CLERICAL), 
    OTHER_SENIOR_PERSONNEL("Other Senior Personnel",OtherPersonnelCost.CODE_OTHER),
    FRINGE_BENEFITS("Fringe Benefits"),
    US_TERRITORIES_POSSESSIONS("US, territories, and possessions"), 
    FOREIGN("Foreign"),
    STIPENDS("Stipends"), 
    TRAVEL_SUPPORT("Travel"), 
    SUBSISTENCE("Subsistance"), 
    OTHER_PARTICIPANTS("Other Participants"),
    MATERIAL_SUPPLIES("Materials and Supplies"), 
    PUBLICATION_COSTS_DOC_DIST("Publication Costs/Documentation/Distrib"), 
    CONSULTING_SERVICES("Consultant Services"), 
    COMPUTER_SERVICES("Computer Services"), 
    SUBAWARDS("Subawards"), 
    OTHER_SERVICES("Other Services"),
    OTHER_PERSONNEL("Other Personnel", new DocumentKeywordEnum[] { ADMINISTRATIVE_CLERICAL, OTHER_SENIOR_PERSONNEL, OTHER_PROFESSIONALS,
            GRADUATE_STUDENTS, POSTDOCTORAL_SCHOLARS, UNDERGRADUATE_STUDENTS }), 
    TRAVEL("Travel", new DocumentKeywordEnum[] { US_TERRITORIES_POSSESSIONS,FOREIGN }), 
    PARTICIPANT_SUPPORT_COSTS("Participant Support Costs", 
            new DocumentKeywordEnum[] { STIPENDS, TRAVEL_SUPPORT, SUBSISTENCE,OTHER_PARTICIPANTS }), 
    OTHER_DIRECT_COSTS("Other Direct Costs",
            new DocumentKeywordEnum[] { MATERIAL_SUPPLIES, PUBLICATION_COSTS_DOC_DIST, CONSULTING_SERVICES, COMPUTER_SERVICES, SUBAWARDS, OTHER_SERVICES });

    DocumentKeywordEnum[] keywordEnums;
    String keyword;
    String code;

    private DocumentKeywordEnum(String keyword) {
        this.keyword = keyword;
    }

    private DocumentKeywordEnum(String keyword, String code) {
        this.keyword = keyword;
        this.code = code;
    }

    private DocumentKeywordEnum(String keyword, DocumentKeywordEnum[] keywordEnums) {
        this.keyword = keyword;
        this.keywordEnums = keywordEnums;
    }

    public String value() {
        return keyword;
    }

    public String code() {
        return code;
    }

}
