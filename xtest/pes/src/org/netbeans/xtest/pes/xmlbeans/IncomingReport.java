/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


/*
 * IncomingReport.java
 *
 * Created on May 27, 2002, 9:50 PM
 */

package org.netbeans.xtest.pes.xmlbeans;

import org.netbeans.xtest.pe.xmlbeans.*;
import org.netbeans.xtest.pe.*;

import java.io.*;

/**
 *
 * @author  breh
 */
public class IncomingReport extends ManagedReport {
    
    /** Creates a new instance of IncomingReport */
    public IncomingReport() {
    }
    
    
    // attributes
    public String xmlat_reportRoot;
    public boolean xmlat_replace;
     
    // getters/setters
    public String getReportRoot() {
        return xmlat_reportRoot;
    }
    
    public File getReportDir() throws IOException {
        if (getReportRoot()!=null) {
            File root = new File (getReportRoot());
            if (root.isDirectory()) {
                return root; 
            } else {
                throw new IOException("reportRoot is not valid directory");
            }
        } else {
            throw new IOException("reportRoot is not specified, cannot return reportDir");
        }
    }
    
    public void setReportRoot(String reportRoot) {
        xmlat_reportRoot = reportRoot;
    }
    
    public boolean isReplace() {
        return xmlat_replace;
    }
    
    public void setReplace(boolean replace) {
        xmlat_replace = replace;
    }
    
    // bussiness methods
    // is report valid ?
    private boolean valid;
    private String invalidMessage = "";
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    /** Sets a validity and describing message. */
    public void setValid(boolean valid, String message) {
        this.valid = valid;
        this.invalidMessage = message;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public String getInvalidMessage() {
        return this.invalidMessage;
    }
    
    // is this report used only for reconfiguration
    private boolean reconfiguration=false;
    
    public void setReconfiguration(boolean reconfiguration) {
        this.reconfiguration = reconfiguration;
    }
    
    public boolean isReconfiguration() {
        return reconfiguration;
    }
    
    // name of the archive where report is packed
    private File archiveFile;
    
    public void setArchiveFile(File archive) {
        archiveFile = archive;
    }
    
    public File getArchiveFile() {
        return archiveFile;
    }
    

    // check validity of the report
    // this means, report has all xml and html files generated, including index.html
    public boolean areReportFilesValid() {
        try {
            File reportRootDir = this.getReportDir();            
            if (!areReportFilesValid(reportRootDir)) return false;
        } catch (IOException ioe) {
            // a problem - not valid
            return false;
        }
        return true;
    }


    public void readIncomingReport(IncomingReport ir) {
        readManagedReport(ir);
        this.xmlat_reportRoot = ir.xmlat_reportRoot;
        this.xmlat_replace = ir.xmlat_replace;
    }
    
   public static IncomingReport loadIncomingReportFromFile(File reportFile) throws IOException {
       try {
           XMLBean xmlBean = XMLBean.loadXMLBean(reportFile);
           if (!(xmlBean instanceof IncomingReport)) {
               throw new IOException("Loaded file "+reportFile+" does not contain IncomingReport");
           }
           return (IncomingReport)xmlBean;
       } catch (ClassNotFoundException cnfe) {
           throw new IOException("Loaded file "+reportFile+" does not contain IncomingReport, caused by ClassNotFoundException :"+cnfe.getMessage());
       }
    }

    
    
}

