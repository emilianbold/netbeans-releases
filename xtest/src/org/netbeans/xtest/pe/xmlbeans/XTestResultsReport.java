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
 * XTestReport.java
 *
 * Created on November 19, 2001, 4:45 PM
 */

package org.netbeans.xtest.pe.xmlbeans;

import java.io.*;

/**
 *
 * @author  mb115822
 * @version
 */
public class XTestResultsReport extends XMLBean {

    /** Creates new XTestReport */
    public XTestResultsReport() {
    }

    // XML attributes
    public java.sql.Timestamp     xmlat_timeStamp;
    public long     xmlat_time;
    public String   xmlat_project;
    public String   xmlat_build;
    public String   xmlat_testingGroup;
    public String   xmlat_testedType;
    public String   xmlat_host;
    public long     xmlat_testsTotal;
    public long     xmlat_testsPass;
    public long     xmlat_testsFail;
    public long     xmlat_testsUnexpectedPass;
    public long     xmlat_testsExpectedFail;
    public long     xmlat_testsError;
    public boolean  xmlat_fullReport;
    // project_id - project_id of the report in the database 
    public String   xmlat_project_id;
    // link - link to local pes -> this is not full link, but only the part from         
    // root of team web server
    public String   xmlat_webLink;
    // team - id of team which submitted the results
    public String   xmlat_team;
    // short info from TestRun's ModuleError element
    public String   xmlat_brokenModules;
    
    // child elements
    public SystemInfo[] xmlel_SystemInfo;
    public TestRun[] xmlel_TestRun;
    
    /** Holds value of property systemInfo_id. */
    private long systemInfo_id;    
    
    
    private String invalidMessage = "";
    
    /** Returns a reason why this report is not valid.
     * @return reason
     */
    public String getInvalidMessage() {
        return invalidMessage;
    }
    
    public void setInvalidMessage(String invalidMessage) {
        this.invalidMessage += " "+invalidMessage;
    }
    
    // business methods
    public boolean isValid() {
        boolean status = true;
        if (xmlat_project == null) {
            setInvalidMessage("Project is null.");
            status = false;
        }
        if (xmlat_build == null) {
            setInvalidMessage("Build number is null.");
            status = false;
        }
        if (xmlat_testingGroup == null) {
            setInvalidMessage("Testing group is null.");
            status = false;
        }
        if (xmlat_testedType == null) {
            setInvalidMessage("Tested type is null.");
            status = false;
        }
        if (xmlat_host == null) {
            setInvalidMessage("Host is null.");
            status = false;
        }
        if (xmlat_testsTotal < 1) {
            setInvalidMessage("Tests total is "+xmlat_testsTotal+". Must be > 0.");
            status = false;
        }
        if (xmlel_SystemInfo == null || xmlel_SystemInfo.length < 1) {
            setInvalidMessage("System info is null or empty.");
            status = false;
        }
        return status;
    }
    
    public boolean equals(Object obj) {
        return equalByAttributes(obj);
    }
    
    /** Getter for property timeStamp.
     * @return Value of property timeStamp.
     */
    public java.sql.Timestamp getTimeStamp() {
        return xmlat_timeStamp;
    }
    
    /** Setter for property timeStamp.
     * @param timeStamp New value of property timeStamp.
     */
    public void setTimeStamp(java.sql.Timestamp timeStamp) {
        xmlat_timeStamp = timeStamp;
    }
    
    /** Getter for property time.
     * @return Value of property time.
     */
    public long getTime() {
        return xmlat_time;
    }
    
    /** Setter for property time.
     * @param time New value of property time.
     */
    public void setTime(long time) {
        xmlat_time = time;
    }
    
    /** Getter for property build.
     * @return Value of property build.
     */
    public String getBuild() {
        return xmlat_build;
    }
    
    /** Setter for property build.
     * @param build New value of property build.
     */
    public void setBuild(String build) {
        xmlat_build = build;
    }
    
    /** Getter for property host.
     * @return Value of property host.
     */
    public String getHost() {
        return xmlat_host;
    }
    
    /** Setter for property host.
     * @param host New value of property host.
     */
    public void setHost(String host) {
        xmlat_host = host;
    }
    
    /** Getter for property project.
     * @return Value of property project.
     */
    public String getProject() {
        return xmlat_project;
    }
    
    /** Setter for property project.
     * @param project New value of property project.
     */
    public void setProject(String project) {
        xmlat_project = project;
    }
    
    /** Getter for property testedType.
     * @return Value of property testedType.
     */
    public String getTestedType() {
        return xmlat_testedType;
    }
    
    /** Setter for property testedType.
     * @param testedType New value of property testedType.
     */
    public void setTestedType(String testedType) {
        xmlat_testedType = testedType;
    }
    
    /** Getter for property testingGroup.
     * @return Value of property testingGroup.
     */
    public String getTestingGroup() {
        return xmlat_testingGroup;
    }
    
    /** Setter for property testingGroup.
     * @param testingGroup New value of property testingGroup.
     */
    public void setTestingGroup(String testingGroup) {
        xmlat_testingGroup = testingGroup;
    }
    
    /** Getter for property testsError.
     * @return Value of property testsError.
     */
    public long getTestsError() {
        return xmlat_testsError;
    }
    
    /** Setter for property testsError.
     * @param testsError New value of property testsError.
     */
    public void setTestsError(long testsError) {
        xmlat_testsError = testsError;
    }
    
    /** Getter for property testsFail.
     * @return Value of property testsFail.
     */
    public long getTestsFail() {
        return xmlat_testsFail;
    }
    
    /** Setter for property testsFail.
     * @param testsFail New value of property testsFail.
     */
    public void setTestsFail(long testsFail) {
        xmlat_testsFail = testsFail;
    }
    
    /** Getter for property testsPass.
     * @return Value of property testsPass.
     */
    public long getTestsPass() {
        return xmlat_testsPass;
    }
    
    /** Setter for property testsPass.
     * @param testsPass New value of property testsPass.
     */
    public void setTestsPass(long testsPass) {
        xmlat_testsPass = testsPass;
    }
    
    /** Getter for property testsUnexpectedPass.
     * @return Value of property testsUnexpectedPass.
     */
    public long getTestsUnexpectedPass() {
        return xmlat_testsUnexpectedPass;
    }
    
    /** Setter for property testsUnexpectedPass.
     * @param testsPass New value of property testsUnexpectedPass.
     */
    public void setTestsUnexpectedPass(long testsUnexpectedPass) {
        xmlat_testsUnexpectedPass = testsUnexpectedPass;
    }

    /** Getter for property testsExpectedFail.
     * @return Value of property testsExpectedFail.
     */
    public long getTestsExpectedFail() {
        return xmlat_testsExpectedFail;
    }
    
    /** Setter for property testsExpectedFail.
     * @param testsExpectedFail New value of property testsExpectedFail.
     */
    public void setTestsExpectedFail(long testsExpectedFail) {
        xmlat_testsExpectedFail = testsExpectedFail;
    }

    /** Getter for property testsTotal.
     * @return Value of property testsTotal.
     */
    public long getTestsTotal() {
        return xmlat_testsTotal;
    }
    
    /** Setter for property testsTotal.
     * @param testsTotal New value of property testsTotal.
     */
    public void setTestsTotal(long testsTotal) {
        xmlat_testsTotal = testsTotal;
    }
    
    /** Getter for property systemInfo_id.
     * @return Value of property systemInfo_id.
     */
    public long getSystemInfo_id() {
        return this.systemInfo_id;
    }
    
    /** Setter for property systemInfo_id.
     * @param systemInfo_id New value of property systemInfo_id.
     */
    public void setSystemInfo_id(long systemInfo_id) {
        this.systemInfo_id = systemInfo_id;
    }
    
    /** getter for weblink 
     */
    public void setWebLink(String webLink) {
        xmlat_webLink = webLink;
    }

    /** setter for weblink 
     */
    public String getWebLink() {
        return xmlat_webLink;
    }
    
    public void setProject_id(String project_id) {
        this.xmlat_project_id = project_id;
    }
    
    public String getProject_id() {
        return this.xmlat_project_id;
    }
    
    /** getter for team
    */
    public void setTeam(String team) {
        xmlat_team = team;
    }

    /** setter for weblink 
     */
    public String getTeam() {
        return xmlat_team;
    }

    /** Getter for property brokenModules.
     * @return Value of property brokenModules.
     */
    public String getBrokenModules() {
        return xmlat_brokenModules;
    }
    
    /** Setter for property brokenModules.
     * @param brokenModules New value of property brokenModules.
     */
    public void setBrokenModules(String brokenModules) {
        this.xmlat_brokenModules = brokenModules;
    }
    
    // read xtest result report (without testruns)
    public void readXTestResultsReport(XTestResultsReport xtr) {
        xmlat_timeStamp = xtr.xmlat_timeStamp;
        xmlat_time = xtr.xmlat_time;
        xmlat_project = xtr.xmlat_project;
        xmlat_project_id = xtr.xmlat_project_id;
        xmlat_team = xtr.xmlat_team;
        xmlat_build = xtr.xmlat_build;
        xmlat_testingGroup = xtr.xmlat_testingGroup;
        xmlat_testedType = xtr.xmlat_testedType;
        xmlat_host = xtr.xmlat_host;
        xmlat_testsTotal = xtr.xmlat_testsTotal;
        xmlat_testsPass = xtr.xmlat_testsPass;
        xmlat_testsFail = xtr.xmlat_testsFail;
        xmlat_testsUnexpectedPass = xtr.xmlat_testsUnexpectedPass;
        xmlat_testsExpectedFail = xtr.xmlat_testsExpectedFail;
        xmlat_testsError = xtr.xmlat_testsError;
        xmlat_fullReport = xtr.xmlat_fullReport;
        xmlat_webLink = xtr.xmlat_webLink;      
    }
    
    // load XTestResultsReport from a file
    /** this one should be deprecated as well :-(
     * @param reportFile
     * @throws IOException
     * @throws ClassNotFoundException
     * @return
     */    
    public static XTestResultsReport loadFromFile(File reportFile) throws IOException, ClassNotFoundException {
        XMLBean xmlBean = XMLBean.loadXMLBean(reportFile);
        if (!(xmlBean instanceof XTestResultsReport)) {
            throw new ClassNotFoundException("Loaded file "+reportFile+" does not contain XTestRestultsReport");
        }
        return (XTestResultsReport)xmlBean;
    }
    
    public static XTestResultsReport loadXTestResultsReportFromFile(File reportFile) throws IOException {
        try {
            XMLBean xmlBean = XMLBean.loadXMLBean(reportFile);
            if (!(xmlBean instanceof XTestResultsReport)) {
                throw new ClassNotFoundException("Loaded file "+reportFile+" does not contain XTestRestultsReport");
            }
            return (XTestResultsReport)xmlBean;
        } catch (ClassNotFoundException cnfe) {
            throw new IOException("Loaded file "+reportFile+" does not contain XTestRestultsReport, caused by ClassNotFoundException :"+cnfe.getMessage());
        }
    }    
    
    // old method name - should be deprecated
    /**
     * @param reportFile
     * @throws IOException
     * @throws ClassNotFoundException
     * @return
     * @deprecated
     */    
    public static XTestResultsReport loadReportFromFile(File reportFile) throws IOException, ClassNotFoundException {
        return loadFromFile(reportFile);
    }
    
    
}
