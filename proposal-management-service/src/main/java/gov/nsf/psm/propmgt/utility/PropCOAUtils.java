package gov.nsf.psm.propmgt.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.Advisee;
import gov.nsf.psm.foundation.model.Affiliation;
import gov.nsf.psm.foundation.model.COA;
import gov.nsf.psm.foundation.model.COAEnum;
import gov.nsf.psm.foundation.model.COAResult;
import gov.nsf.psm.foundation.model.CoEditor;
import gov.nsf.psm.foundation.model.Collaborator;
import gov.nsf.psm.foundation.model.Relationship;
import gov.nsf.psm.foundation.model.compliance.ss.RowModel;
import gov.nsf.psm.foundation.model.compliance.ss.SpreadsheetModel;
import gov.nsf.psm.foundation.model.compliance.ss.TableModel;
import gov.nsf.psm.foundation.model.compliance.ss.WorksheetModel;
import gov.nsf.psm.foundation.model.filestorage.UploadFileResponse;
import gov.nsf.psm.foundation.model.lookup.AdviseeTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.CoEditorTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.CollaboratorTypeLookUp;
import gov.nsf.psm.foundation.model.lookup.RelationshipTypeLookUp;
import gov.nsf.psm.foundation.utility.ModelUtils;
import gov.nsf.psm.propmgt.common.Constants;

public class PropCOAUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PropCOAUtils.class); 
    
    private PropCOAUtils() {
        // Empty constructor
    }
    
    public static SpreadsheetModel processLookups(SpreadsheetModel model, List<AdviseeTypeLookUp> adviseeLookUps,
        List<CollaboratorTypeLookUp> collaboratorLookUps, List<CoEditorTypeLookUp> coeditorTypeLookups,  List<RelationshipTypeLookUp> relationshipTypeLookups) {
        for(WorksheetModel sheet : model.getWorksheets()) {
            for(TableModel table : sheet.getTables()) {
               for(int i = 1; i < table.getRows().size(); i++) {
                   RowModel row = table.getRows().get(i);
                   String value = row.getCells().get(0).getValue().replaceAll(":", "");
                   populateAdviseeRows(row, adviseeLookUps, value);
                   populateCollaboratorRows(row, collaboratorLookUps, value);
                   populateCoEditorRows(row, coeditorTypeLookups, value);
                   populateRelationshipRows(row, relationshipTypeLookups, value);
              }
           }
        }
        return model;
    }
    
    public static void populateAdviseeRows(RowModel row, List<AdviseeTypeLookUp> adviseeLookUps, String value) {
        List<AdviseeTypeLookUp> r = adviseeLookUps.stream()
                .filter(item -> item.getCode().trim().equalsIgnoreCase(value))
                .collect(Collectors.toList());
        if(!r.isEmpty()) {
            row.getCells().get(0).setValue(r.get(0).getDescription());
        }
    }
    
    public static void populateCollaboratorRows(RowModel row, List<CollaboratorTypeLookUp> collaboratorLookUps, String value) {
        List<CollaboratorTypeLookUp> r = collaboratorLookUps.stream()
                .filter(item -> item.getCode().trim().equalsIgnoreCase(value))
                .collect(Collectors.toList());
        if(!r.isEmpty()) {
            row.getCells().get(0).setValue(r.get(0).getDescription());
        }
    }
    
    public static void populateCoEditorRows(RowModel row, List<CoEditorTypeLookUp> coeditorTypeLookups, String value) {
        List<CoEditorTypeLookUp> r = coeditorTypeLookups.stream()
                .filter(item -> item.getCode().trim().equalsIgnoreCase(value))
                .collect(Collectors.toList());
        if(!r.isEmpty()) {
            row.getCells().get(0).setValue(r.get(0).getDescription());
        }
    }

    public static void populateRelationshipRows(RowModel row, List<RelationshipTypeLookUp> relationshipTypeLookups, String value) {
        List<RelationshipTypeLookUp> r = relationshipTypeLookups.stream()
                .filter(item -> item.getCode().trim().equalsIgnoreCase(value))
                .collect(Collectors.toList());
        if(!r.isEmpty()) {
            row.getCells().get(0).setValue(r.get(0).getDescription());
        }
    }
    
    public static COAResult convertSpreadsheetModelToCOA(SpreadsheetModel model, UploadFileResponse fsResp, String fileName, long revId, long persId) throws CommonUtilException {

          COA coa = getNewCOA(fileName, fsResp.getFilePath(), revId, persId);
          
          List<Affiliation> affiliations = new ArrayList<>();
          List<Advisee> advisees = new ArrayList<>();
          List<Collaborator> collaborators = new ArrayList<>();
          List<CoEditor> coeditors = new ArrayList<>();
          List<Relationship> relationships = new ArrayList<>();
          List<String> invalidTypes = new ArrayList<>();
          
          for(WorksheetModel sheet : model.getWorksheets()) {
              for(TableModel table : sheet.getTables()) {
                  populateSpreadsheetData(table, affiliations, advisees, collaborators, coeditors, relationships, invalidTypes);
              }
          }
          
          coa.setAdvisees(advisees);
          coa.setAffiliations(affiliations);
          coa.setRelationships(relationships);
          coa.setCollaborators(collaborators);
          coa.setCoEditors(coeditors);
          COAResult coaResult = new COAResult();
          coaResult.setCoa(coa);
          coaResult.setInvalidTypes(invalidTypes);
          return coaResult;
    }
    
    public static void populateSpreadsheetData(TableModel table, List<Affiliation> affiliations, List<Advisee> advisees, List<Collaborator> collaborators, List<CoEditor> coeditors, List<Relationship> relationships, List<String> invalidTypes) throws CommonUtilException {
        
        for(int i = 1; i < table.getRows().size(); i++) {
            
            String personnelType = "";
            RowModel row = table.getRows().get(i);
            String code = row.getCells().get(0).getValue().replaceAll(":", "").trim();
            
            if(StringUtils.isEmpty(code) && table.getName().trim().replace(" ", "").equalsIgnoreCase(COAEnum.TABLE_A.getTableId().replace(" ", ""))) {
                personnelType = COAEnum.TABLE_A.getPersonnelType();
            } else if(!StringUtils.isEmpty(code) && table.getName().trim().replace(" ", "").equalsIgnoreCase(COAEnum.TABLE_B.getTableId().replace(" ", ""))) {
                personnelType = COAEnum.TABLE_B.getPersonnelType();
            } else if(!StringUtils.isEmpty(code) && table.getName().trim().replace(" ", "").equalsIgnoreCase(COAEnum.TABLE_C.getTableId().replace(" ", ""))) {
                personnelType = COAEnum.TABLE_C.getPersonnelType();
            } else if(!StringUtils.isEmpty(code) && table.getName().trim().replace(" ", "").equalsIgnoreCase(COAEnum.TABLE_D.getTableId().replace(" ", ""))) {
                personnelType = COAEnum.TABLE_D.getPersonnelType();
            } else if(!StringUtils.isEmpty(code) && table.getName().trim().replace(" ", "").equalsIgnoreCase(COAEnum.TABLE_E.getTableId().replace(" ", ""))) {
                personnelType = COAEnum.TABLE_E.getPersonnelType();
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_MISSING_CODE);
            }
            switch(personnelType) {
                case COA.AFFILIATIONS:
                    affiliations.add(populateAffiliation(row, invalidTypes));
                    break;
                case COA.ADVISEES:
                    if(Arrays.asList(Arrays.stream(Advisee.CODES.values()).map(Advisee.CODES::name).toArray(String[]::new)).indexOf(code) > -1) {
                        advisees.add(populateAdvisee(row, code, invalidTypes));
                    }
                    break;
                case COA.COLLABORATORS:
                    if(Arrays.asList(Arrays.stream(Collaborator.CODES.values()).map(Collaborator.CODES::name).toArray(String[]::new)).indexOf(code) > -1) {
                        collaborators.add(populateCollaborator(row, code, invalidTypes));
                    }
                    break;
                case COA.COEDITORS:
                    if(Arrays.asList(Arrays.stream(CoEditor.CODES.values()).map(CoEditor.CODES::name).toArray(String[]::new)).indexOf(code) > -1) {
                        coeditors.add(populateCoEditor(row, code, invalidTypes));
                    }
                    break;
                case COA.RELATIONSHIPS:
                    if(Arrays.asList(Arrays.stream(Relationship.CODES.values()).map(Relationship.CODES::name).toArray(String[]::new)).indexOf(code) > -1) {
                        relationships.add(populateRelationship(row, code, invalidTypes));
                    }
                    break;
                default:
                    //
            }
        }
    }
    
    public static Affiliation populateAffiliation(RowModel row, List<String> invalidTypes) throws CommonUtilException {
        int maxIdx = row.getCells().size()-1;
        Affiliation affiliation = new Affiliation();
        SimpleDateFormat formatter = new SimpleDateFormat(Constants.DT_FORMAT);
        
        String persName = "";
        if(maxIdx >= 1) {
            persName = row.getCells().get(1).getValue().trim();
        }
        String orgAfflName = "";
        if(maxIdx >= 2) {
            orgAfflName = row.getCells().get(2).getValue().trim();
        }
        if(!StringUtils.isEmpty(persName)) {
            if(persName.length() <= Affiliation.SRPERSNAME_MAX_LENGTH) {
                affiliation.setSrPersName(persName);
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_FIELD_LENGTH);
            }
        }
        if(!StringUtils.isEmpty(orgAfflName)) {
            if(orgAfflName.length() <= Affiliation.ORGAFFLNAME_MAX_LENGTH) {
                affiliation.setOrgAfflName(orgAfflName);
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_FIELD_LENGTH);
            }
        }
        String val = "";
        if(maxIdx >= 3) {
            val = row.getCells().get(3).getValue();
        }
        if(!StringUtils.isEmpty(val)) {
            if(ModelUtils.isDate(val)) {
                try {
                    affiliation.setLastActvDate(formatter.parse(val.trim()));
                } catch (ParseException e) {
                    LOGGER.error(e.getMessage());
                    invalidTypes.add(COAResult.INVALID_TYPE_INVALID_DATE);
                }
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_INVALID_DATE);
            }
        }
        return affiliation;
    }
    
    public static Advisee populateAdvisee(RowModel row, String code, List<String> invalidTypes) throws CommonUtilException {
        int maxIdx = row.getCells().size()-1;
        Advisee advisee = new Advisee();
        
        String adviseeName = "";
        if(maxIdx >= 1) {
            adviseeName = row.getCells().get(1).getValue().trim();
        }
        String orgAfflName = "";
        if(maxIdx >= 2) {
            orgAfflName = row.getCells().get(2).getValue().trim();
        }
        String emailDept = "";
        if(maxIdx >= 3) {
            emailDept = row.getCells().get(3).getValue().trim();
        }
        if(!StringUtils.isEmpty(adviseeName)) {
            if(adviseeName.length() <= Advisee.ADVISEENAME_MAX_LENGTH) {
                advisee.setAdviseeName(adviseeName);
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_FIELD_LENGTH);
            }
        }
        if(!StringUtils.isEmpty(orgAfflName)) {
            if(orgAfflName.length() <= Advisee.ORGAFFLNAME_MAX_LENGTH) {
                advisee.setOrgAfflName(orgAfflName);
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_FIELD_LENGTH);
            }
        }
        if(!StringUtils.isEmpty(emailDept)) {
            if(emailDept.length() <= Advisee.EMAILDEPT_MAX_LENGTH) {
                advisee.setEmailDeptName(emailDept);
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_FIELD_LENGTH);
            }
        }
        advisee.setAdviseeTypeCode(code);
        return advisee;
    }

    public static Collaborator populateCollaborator(RowModel row, String code, List<String> invalidTypes) throws CommonUtilException {
        int maxIdx = row.getCells().size()-1;
        Collaborator collaborator = new Collaborator();
        SimpleDateFormat formatter = new SimpleDateFormat(Constants.DT_FORMAT);
        
        String clbrName = "";
        if(maxIdx >= 1) {
            clbrName = row.getCells().get(1).getValue().trim();
        }
        String orgAfflName = "";
        if(maxIdx >= 2) {
            orgAfflName = row.getCells().get(2).getValue().trim();
        }
        String emailDept = "";
        if(maxIdx >= 3) {
            emailDept = row.getCells().get(3).getValue().trim();
        }
        if(!StringUtils.isEmpty(clbrName)) {
            if(clbrName.length() <= Collaborator.CLBRNAME_MAX_LENGTH) {
                collaborator.setClbrName(clbrName);
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_FIELD_LENGTH);
            }
        }
        if(!StringUtils.isEmpty(orgAfflName)) {
            if(orgAfflName.length() <= Collaborator.ORGAFFLNAME_MAX_LENGTH) {
                collaborator.setOrgAfflName(orgAfflName);
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_FIELD_LENGTH);
            }
        }
        if(!StringUtils.isEmpty(emailDept)) {
            if(emailDept.length() <= Collaborator.EMAILDEPT_MAX_LENGTH) {
                 collaborator.setEmailDeptName(emailDept);
            } else {
                 invalidTypes.add(COAResult.INVALID_TYPE_FIELD_LENGTH);
            }
        }
        collaborator.setClbrTypeCode(code);
        String val = "";
        if(maxIdx >= 4) {
            val = row.getCells().get(4).getValue();
        }
        if(!StringUtils.isEmpty(val)) {
            if(ModelUtils.isDate(val)) {
                try {
                    collaborator.setLastActvDate(formatter.parse(val.trim()));
                } catch (ParseException e) {
                    LOGGER.error(e.getMessage());
                    invalidTypes.add(COAResult.INVALID_TYPE_INVALID_DATE);
                }
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_INVALID_DATE);
            }
        }
        return collaborator;
    }
    
    public static CoEditor populateCoEditor(RowModel row, String code, List<String> invalidTypes) throws CommonUtilException {
        int maxIdx = row.getCells().size()-1;
        CoEditor coeditor = new CoEditor();
        SimpleDateFormat formatter = new SimpleDateFormat(Constants.DT_FORMAT);
        
        String coeditorName = "";
        if(maxIdx >= 1) {
            coeditorName = row.getCells().get(1).getValue().trim();
        }
        String orgAfflName = "";
        if(maxIdx >= 2) {
            orgAfflName = row.getCells().get(2).getValue().trim();
        }
        String jrnlCollection = "";
        if(maxIdx >= 3) {
            jrnlCollection = row.getCells().get(3).getValue().trim();
        }
        if(!StringUtils.isEmpty(coeditorName)) {
            if(coeditorName.length() <= CoEditor.COEDITORNAME_MAX_LENGTH) {
                coeditor.setCoEditorName(coeditorName);
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_FIELD_LENGTH);
            }
        }
        if(!StringUtils.isEmpty(orgAfflName)) {
            if(orgAfflName.length() <= CoEditor.ORGAFFLNAME_MAX_LENGTH) {
                coeditor.setOrgAfflName(orgAfflName);
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_FIELD_LENGTH);
            }
        }
        if(!StringUtils.isEmpty(jrnlCollection)) {
            if(jrnlCollection.length() <= CoEditor.JRNLCOLLECTION_MAX_LENGTH) {
                coeditor.setJournalCollection(jrnlCollection);
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_FIELD_LENGTH);
            }
        }
        coeditor.setCoEditorTypeCode(code);
        String val = "";
        if(maxIdx >= 4) {
            val = row.getCells().get(4).getValue();
        }
        if(!StringUtils.isEmpty(val)) {
            if(ModelUtils.isDate(val)) {
                try {
                    coeditor.setLastActvDate(formatter.parse(val.trim()));
                } catch (ParseException e) {
                    LOGGER.error(e.getMessage());
                    invalidTypes.add(COAResult.INVALID_TYPE_INVALID_DATE);
                }
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_INVALID_DATE);
            }
        }
        return coeditor;
    }
    
    public static Relationship populateRelationship(RowModel row, String code, List<String> invalidTypes) throws CommonUtilException {
        int maxIdx = row.getCells().size()-1;
        Relationship relationship = new Relationship();
        SimpleDateFormat formatter = new SimpleDateFormat(Constants.DT_FORMAT);
        
        String relName = "";
        if(maxIdx >= 1) {
            relName = row.getCells().get(1).getValue().trim();
        }
        String orgAfflName = "";
        if(maxIdx >= 2) {
            orgAfflName = row.getCells().get(2).getValue().trim();
        }
        String emailDeptName = "";
        if(maxIdx >= 3) {
            emailDeptName = row.getCells().get(3).getValue().trim();
        }
        if(!StringUtils.isEmpty(relName)) {
            if(relName.length() <= Relationship.RELNAME_MAX_LENGTH) {
                relationship.setRelationshipName(relName);
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_FIELD_LENGTH);
            }
        }
        if(!StringUtils.isEmpty(orgAfflName)) {
            if(orgAfflName.length() <= Relationship.ORGAFFLNAME_MAX_LENGTH) {
                relationship.setOrgAfflName(orgAfflName);
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_FIELD_LENGTH);
            }
        }
        if(!StringUtils.isEmpty(emailDeptName)) {
            if(emailDeptName.length() <= Relationship.EMAILDEPT_MAX_LENGTH) {
                relationship.setEmailDeptName(emailDeptName);
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_FIELD_LENGTH);
            }
        }
        relationship.setRelationshipTypeCode(code);
        String val = "";
        if(maxIdx >= 4) {
            val = row.getCells().get(4).getValue();
        }
        if(!StringUtils.isEmpty(val)) {
            if(ModelUtils.isDate(val)) {
                try {
                    relationship.setLastActvDate(formatter.parse(val.trim()));
                } catch (ParseException e) {
                    LOGGER.error(e.getMessage());
                    invalidTypes.add(COAResult.INVALID_TYPE_INVALID_DATE);
                }
            } else {
                invalidTypes.add(COAResult.INVALID_TYPE_INVALID_DATE);
            }
        }
        return relationship;
    }
    
    public static COA getNewCOA(String fileName, String filePath, Long revId, Long persId) {
        COA coa = new COA();
        coa.setCoaExcelFileName(fileName);
        coa.setCoaExcelFilePath(filePath);
        coa.setCoaPdfFileName("");
        coa.setCoaPdfFilePath("");
        coa.setPropPrepRevId(revId);
        coa.setPropPersId(String.valueOf(persId));
        return coa;
    }
    
    public static SpreadsheetModel convertTableNames(SpreadsheetModel model) {
        for(WorksheetModel sheet : model.getWorksheets()) {
            for(TableModel table : sheet.getTables()) {
               switch(table.getName()) {
                   case COA.TABLE_A:
                       table.setName(COAEnum.TABLE_A.getTableName());
                       break;
                   case COA.TABLE_B:
                       table.setName(COAEnum.TABLE_B.getTableName());
                       break;
                   case COA.TABLE_C:
                       table.setName(COAEnum.TABLE_C.getTableName());
                       break;
                   case COA.TABLE_D:
                       table.setName(COAEnum.TABLE_D.getTableName());
                       break;
                   case COA.TABLE_E:
                       table.setName(COAEnum.TABLE_E.getTableName());
                       break;
                   default:
                       break;
                   }
               }
        }
        return model;    
    }
    
}
