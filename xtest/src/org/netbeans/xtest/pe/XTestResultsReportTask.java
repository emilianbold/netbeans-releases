/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * XTestResultsReportTask.java
 *
 * Created on December 3, 2001, 2:43 PM
 */

package org.netbeans.xtest.pe;

import org.apache.tools.ant.*;
import org.netbeans.xtest.pe.xmlbeans.*;
import java.io.*;
import org.w3c.dom.*;

/**
 *
 * @author  mb115822
 * @version 
 */
public class XTestResultsReportTask extends Task{

    /** Creates new XTestResultsReportTask */
    public XTestResultsReportTask() {
    }
    
    private File outfile;
    private String project;
    private String build;
    private String testingGroup;
    private String testedType;
    private String host;
    private String comment;
    
    
    public void setTestingGroup(String testingGroup) {
        this.testingGroup = testingGroup;
    }
    
    public void setTestedType(String testedType) {
        this.testedType = testedType;
    }

    public void setOutFile(File outfile) {
        this.outfile = outfile;
    }
    
    public void setProject(String project) {
        this.project = project;
    }    
    
    public void setBuild(String build) {
        this.build = build;
    }
    
    public void setHost(String host) {
        if (host.startsWith("${")|host.equals("")) {
            this.host = SystemInfo.getCurrentHost();
        } else {
            this.host = host;
        }
    }
    
    public void setComment(String comment) {
        if (!comment.startsWith("${")) {
            this.comment = comment;
        }        
    }
    
    public XTestResultsReport getReport() {
        XTestResultsReport report = new XTestResultsReport();
        report.xmlat_project = project;
        report.xmlat_build = build;
        report.xmlat_testingGroup = testingGroup;
        report.xmlat_testedType = testedType;
        report.xmlat_host = host;
        report.xmlat_comment = comment;
        report.xmlat_timeStamp = new java.sql.Timestamp(System.currentTimeMillis());
        return report;
    }
    
    public void execute () throws BuildException {
        log("Generating test report info xml");
        XTestResultsReport report;
        try {
            Document doc = SerializeDOM.parseFile(this.outfile);
            report = (XTestResultsReport)XMLBean.getXMLBean(doc);
            log("Test report info xml already exists - skipping");
            return;
        } catch (Exception e) {            
            report = getReport();
        }
        //System.err.println("TR:"+tr);
        try {
            FileOutputStream outStream = new FileOutputStream(this.outfile);            
            SerializeDOM.serializeToStream(report.toDocument(),outStream);
            outStream.close();
        } catch (IOException ioe) {
            log("Cannot save test report:"+ioe);
            ioe.printStackTrace(System.err);
        } catch (Exception e) {
            log("XMLBean exception?:"+e);
            e.printStackTrace(System.err);           
        }
    }

}
