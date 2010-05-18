/*
 * ReportCustomizationOptions.java
 * 
 * Created on Oct 16, 2007, 10:38:13 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.reportgenerator.api;

/**
 *
 * @author radval
 */
public class ReportCustomizationOptions {
    
    private  boolean mGenerateVerboseReport = true;
    private  boolean mIncludeOnlyElementsWithDocumentation = false;
    
    public void setGenerateVerboseReport(boolean generateVerboseReport) {
            mGenerateVerboseReport = generateVerboseReport;
    }
        
    public boolean isGenerateVerboseReport() {
        return mGenerateVerboseReport;
    }
    
    public void setIncludeOnlyElementsWithDocumentation(boolean include) {
        this.mIncludeOnlyElementsWithDocumentation = include;
    }
    
    public boolean isIncludeOnlyElementsWithDocumentation() {
        return this.mIncludeOnlyElementsWithDocumentation;
    }
}
