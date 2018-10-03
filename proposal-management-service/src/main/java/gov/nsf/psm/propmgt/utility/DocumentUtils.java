package gov.nsf.psm.propmgt.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

import gov.nsf.psm.factmodel.FileFactModel;
import gov.nsf.psm.foundation.model.PSMMessage;
import gov.nsf.psm.foundation.model.budget.BudgetRecord;
import gov.nsf.psm.foundation.model.budget.SeniorPersonnelCost;
import gov.nsf.psm.foundation.model.compliance.ComplianceData;
import gov.nsf.psm.foundation.model.compliance.doc.DocumentModel;
import gov.nsf.psm.foundation.model.compliance.doc.KeywordModel;
import gov.nsf.psm.propmgt.common.Constants;

public class DocumentUtils {

    private DocumentUtils() {
    }

    /**
     * Creates page references for compliance validation messages
     * 
     * @param List<String>
     * @param name
     *            compliancePages
     * @param Long
     * @param name
     *            noOfPages
     * @return String
     */
    public static String getPageReferences(List<String> compliancePages, Long noOfPages) {
        String exp = "";
        if (compliancePages != null && noOfPages != null) {
            if (noOfPages > 1) {
                if (compliancePages.size() < noOfPages) {
                    exp = ".  See the following page(s): " + Joiner.on(", ").join(compliancePages) + ".";
                } else {
                    exp = "";
                }
            } else {
                exp = "";
            }
        }
        return exp;
    }

    @SuppressWarnings({ "squid:S3655" })
    public static List<KeywordModel> getKeywords(BudgetRecord rec) {

        List<KeywordModel> keywords = new ArrayList<>();

        // Senior Personnel
        if (rec.getSrPersonnelList() != null && !rec.getSrPersonnelList().isEmpty()) {
            List<KeywordModel> subHeadings = new ArrayList<>();
            for (SeniorPersonnelCost cost : rec.getSrPersonnelList()) {
                if (cost.getSeniorPersonDollarAmount().compareTo(BigDecimal.ZERO) > 0) {
                    subHeadings.add(new KeywordModel(getPersonnelName(cost)));
                }
            }
            KeywordModel heading = new KeywordModel(DocumentKeywordEnum.SENIOR_PERSONNEL.value());
            long count = keywords.stream()
                    .filter(HeadingFactModel -> heading.getValue().equals(HeadingFactModel.getValue())).count();
            if (count < 1) {
                keywords.add(heading);
            }
            KeywordModel model = keywords.stream()
                    .filter(HeadingFactModel -> heading.getValue().equals(HeadingFactModel.getValue())).findFirst()
                    .get();
            if (keywords.indexOf(model) > -1) {
                keywords.remove(model);
            }
            model.setKeywords(subHeadings);
            keywords.add(model);

        }

        return keywords;

    }

    public static String getPersonnelName(SeniorPersonnelCost cost) {
        return (cost.getSeniorPersonFirstName() + " " + cost.getSeniorPersonLastName()).replaceAll("  ", " ");
    }
    
    public static Double roundComplianceValue(double size) {
        BigDecimal bd = BigDecimal.valueOf(size);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    public static FileFactModel getFileFactModel(String fileName, float fileSize) {
        FileFactModel fileModel = new FileFactModel();
        fileModel.setName(fileName);
        fileModel.setSize(((float) fileSize / 1024) / 1024);
        return fileModel;
    }
    
    public static ComplianceData checkPDFDimensions(DocumentModel docModel) {
        ComplianceData data = new ComplianceData();
        List<PSMMessage> msgList = new ArrayList<>();
        if(docModel.getPages() != null 
            && !docModel.getPages().isEmpty() 
                && (docModel.getPages().get(0).getWidth() > 612 || docModel.getPages().get(0).getHeight() > 792)) {
                    PSMMessage msg = new PSMMessage();
                    msg.setDescription(Constants.DOC_COMPLIANCE_PDF_SIZE);
                    msg.setId(Constants.DOC_COMPLIANCE_PDF_SIZE_ID);
                    msgList.add(msg);
        }
        data.setMessages(msgList);
        return data;
    }

}
