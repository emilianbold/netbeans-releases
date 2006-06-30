/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import java.util.*;
import org.netbeans.xtest.util.SerializeDOM;

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
    private String project_id;
    private String team;
    
    private String scannedPropertiesName = null;
    
    
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
    
    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }
    
    public void setTeam(String team) {
        if ((team != null) & (team.length() > 0)) {
            this.team = team;
        } else {
            this.team = null;
        }
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
    
    public void setScanPropertiesForAttributes(boolean value) {
        if (value == true) {    
            /// !!!!!!!!!!!!!!!!
            /// !!!!!!!!!!!!!!!!
            /// !!!!!!!!!!!!!!!!
            this.scannedPropertiesName = "xtest.report.attribute|";
            /// !!!!!!!!!!!!!!!!
            /// !!!!!!!!!!!!!!!!
        }
    }
    
    // scan all ant's properties beginning with scannedPropertiesName, cut the prefix and
    // copy the new key and value to attributes of xtest results report
    public void scanAndFillAttributes(XTestResultsReport report) {
        Project project = getProject();
        Hashtable props = project.getProperties();
        //Properties userProperties = project.getUserProperties();
        // scan for all properties beginning with
        Iterator entrySetIterator = props.entrySet().iterator();
        Hashtable processedProperties = new Hashtable();
        while (entrySetIterator.hasNext()) {
            Map.Entry entry = (Map.Entry)entrySetIterator.next();
            String key = (String)entry.getKey();
            if (key.startsWith(scannedPropertiesName)) {
                // yes this is the scanned property
                String value = (String)entry.getValue();
                if (value != null) {
                    String newKey = key.substring(scannedPropertiesName.length());
                    processedProperties.put(newKey, value);
                }
            }            
        }
        
        // now copy the scanned properties to XTestResultsReport
        int arraySize = processedProperties.size();
        if (arraySize > 0) {
            report.xmlel_Attribute = new Attribute[arraySize];
            int arrayPointer = 0;
            Iterator processedEntrySetIterator = processedProperties.entrySet().iterator();
            while (processedEntrySetIterator.hasNext()) {
                Map.Entry entry = (Map.Entry)processedEntrySetIterator.next();
                String key = (String)entry.getKey();
                String value = (String)entry.getValue();
                Attribute attribute = new Attribute(key, value);
                report.xmlel_Attribute[arrayPointer] = attribute;
                arrayPointer++;
            }
        }        
    }
    
    
    public XTestResultsReport getReport() {
        XTestResultsReport report = new XTestResultsReport();
        report.xmlat_project = project;
        report.xmlat_project_id = project_id;
        report.xmlat_build = build;
        report.xmlat_testingGroup = testingGroup;
        report.xmlat_testedType = testedType;
        report.xmlat_host = host;
        report.xmlat_comment = comment;
        report.xmlat_team = team;
        report.xmlat_timeStamp = new java.sql.Timestamp(System.currentTimeMillis());
        if (scannedPropertiesName != null) {
            scanAndFillAttributes(report);
        }
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
