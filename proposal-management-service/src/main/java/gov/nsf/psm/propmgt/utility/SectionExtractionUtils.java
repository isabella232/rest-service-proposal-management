package gov.nsf.psm.propmgt.utility;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nsf.psm.foundation.model.compliance.doc.DocumentModel;
import gov.nsf.psm.foundation.model.compliance.doc.PageModel;
import gov.nsf.psm.foundation.model.compliance.doc.SectionModel;

public class SectionExtractionUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SectionExtractionUtils.class); 
    
    private SectionExtractionUtils() {}
    
    public static Map<SectionModel, StringBuilder> getExtractedSections(DocumentModel docModel, PageModel model) {
        Map<SectionModel, StringBuilder> sections = new LinkedHashMap<>();
        if (!docModel.getSections().isEmpty()) {
            LOGGER.info("");
            LOGGER.info("---------------- Extracting Sections ----------------");
            LOGGER.info("Begin extracting sections from page " + model.getPageNumber());
            if (!model.getLines().isEmpty()) {
                SectionModel prevSection = null;
                for (SectionModel section : docModel.getSections()) {
                    sections = processSections(docModel, model, section, prevSection, sections);
                    prevSection = section;
                }
            }
            LOGGER.info("Done extracting sections from page " + model.getPageNumber());
        }
        return sections;
    }
    
    public static Map<SectionModel, StringBuilder> processSections(DocumentModel docModel, PageModel pageModel, SectionModel section, SectionModel prevSection, Map<SectionModel, StringBuilder> sections) {
        if (prevSection == null || prevSection.getBeginPage() <= section.getBeginPage()) {
            if (section.getBeginPage() == pageModel.getPageNumber()) {
                processPageWithSectionHeader(docModel, pageModel, section, prevSection, sections);
            }
            else { // Page without section header
                if (prevSection != null && section.getBeginPage() > pageModel.getPageNumber()
                        && section.getBeginPage() > (prevSection.getBeginPage())) {
                    List<String> lines = pageModel.getTextLines();
                    replaceWithPreviousSection(lines, sections, prevSection);
                }
            }
        }
        return sections;
    }
    
    private static void processPageWithSectionHeader(DocumentModel docModel, PageModel pageModel,
            SectionModel section, SectionModel prevSection, Map<SectionModel, StringBuilder> sections) {
        List<String> lines = null;
        if (prevSection == null) {
            lines = getBeginLines(docModel, pageModel, section);
        } else if (docModel.getSections().indexOf(section) < (docModel.getSections().size() - 1)
                && section.getBeginPage() == docModel.getSections()
                        .get(docModel.getSections().indexOf(section) + 1).getBeginPage()) {
            int idx = (int) docModel.getSections().get(docModel.getSections().indexOf(section) + 1)
                    .getBeginLine();
            lines = pageModel.getTextLines().subList((int) section.getBeginLine(), idx - 1);
        } else if (section.getBeginPage() > prevSection.getBeginPage()) {
            int idx = (int) section.getBeginLine();
            List<String> appendLines = pageModel.getTextLines().subList(0, idx - 1);
            correctSection(appendLines, sections, prevSection);
            idx = getSectionIndex(docModel, pageModel, section);
            if (idx > (int) section.getBeginLine()) {
                lines = pageModel.getTextLines().subList((int) section.getBeginLine(), idx - 1);
            }
        } else {
            lines = pageModel.getTextLines().subList((int) section.getBeginLine(),
                    pageModel.getTextLines().size());
        }
        if (lines != null) {
            addLinesToSection(lines, sections, section);
        }
    }
    
    private static List<String> getBeginLines(DocumentModel docModel, PageModel model, SectionModel section) {
        int idx = getFirstSectionIndex(docModel, model, section);
        return model.getTextLines().subList((int) section.getBeginLine(), idx - 1);
    }
    
    private static int getFirstSectionIndex(DocumentModel docModel, PageModel model, SectionModel section) {
        int idx = model.getTextLines().size();
        if (docModel.getSections().size() > 1) {
            idx = (int) docModel.getSections()
                    .get(docModel.getSections().indexOf(section) + 1)
                    .getBeginLine();
        }
        return idx;
    }
    
    private static int getSectionIndex(DocumentModel docModel, PageModel model, SectionModel section) {
        int idx = 0;
        if (docModel.getSections()
                .indexOf(section) < (docModel.getSections().size() - 1)) {
            idx = (int) docModel.getSections()
                    .get(docModel.getSections().indexOf(section) + 1)
                    .getBeginLine();
        } else {
            idx = model.getTextLines().size();
        }
        return idx;
    }
    
    private static void correctSection(List<String> appendLines, Map<SectionModel, StringBuilder> sections, SectionModel prevSection) {
        for (String line : appendLines) {
            StringBuilder bldr = sections.get(prevSection);
            if(bldr != null) {
                bldr.append(line);
                sections.replace(prevSection, bldr);
            }
        }
    }
    
    private static void addLinesToSection(List<String> lines, Map<SectionModel, StringBuilder> sections, SectionModel section) {
        for (String line : lines) {
            if (sections.containsKey(section)) {
                StringBuilder bldr = sections.get(section);
                bldr.append(line);
                sections.replace(section, bldr);
            } else {
                sections.put(section, new StringBuilder(line));
            }
        }
    }

    private static void replaceWithPreviousSection(List<String> lines, Map<SectionModel, StringBuilder> sections, SectionModel section) {
        for (String line : lines) {
            if (sections.containsKey(section)) {
                StringBuilder bldr = sections.get(section);
                bldr.append(line);
                sections.replace(section, bldr);
            } else {
                sections.put(section, new StringBuilder(line));
            }
        }
    }
    
}
