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
 * XTestTestRun.java
 *
 * Created on November 1, 2001, 6:09 PM
 */

package org.netbeans.xtest.pe.xmlbeans;

/**
 *
 * @author  mb115822
 */
public class TestRun extends XMLBean {

    /** Creates new XTestTestRun */
    public TestRun() {
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
    
    /** Getter for property config.
     * @return Value of property config.
     */
    public String getConfig() {
        return xmlat_config;
    }
    
    /** Setter for property config.
     * @param config New value of property config.
     */
    public void setConfig(String config) {
        xmlat_config = config;
    }
    
    /** Getter for property name.
     * @return Value of property name.
     */
    public String getName() {
        return xmlat_name;
    }
    
    /** Setter for property name.
     * @param name New value of property name.
     */
    public void setName(String name) {
        xmlat_name = name;
    }
    
    /** Getter for property attributes.
     * @return Value of property attributes.
     */
    public String getAttributes() {
        return xmlat_attributes;
    }
    
    /** Setter for property attributes.
     * @param attributes New value of property attributes.
     */
    public void setAttributes(String attributes) {
        xmlat_attributes = attributes;
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
    
    /** Getter for property runID.
     * @return Value of property runID.
     */
    public String getRunID() {
        return xmlat_runID;
    }
    
    /** Setter for property runID.
     * @param runID New value of property runID.
     */
    public void setRunID(String runID) {
        xmlat_runID = runID;
    }
    
    /** Getter for property XTestResultsReport_id.
     * @return Value of property XTestResultsReport_id.
     */
    public long getXTestResultsReport_id() {
        return this.XTestResultsReport_id;
    }
    
    /** Setter for property XTestResultsReport_id.
     * @param XTestResultsReport_id New value of property XTestResultsReport_id.
     */
    public void setXTestResultsReport_id(long XTestResultsReport_id) {
        this.XTestResultsReport_id = XTestResultsReport_id;
    }
    
    // XML attributes
    public java.sql.Timestamp      xmlat_timeStamp;
    public long     xmlat_time;
    public String   xmlat_config;
    public String   xmlat_name;
    public String   xmlat_attributes;
    public long     xmlat_testsTotal;
    public long     xmlat_testsPass;
    public long     xmlat_testsFail;
    public long     xmlat_testsError;
    public String   xmlat_runID;
    
    // child elements
    public TestBag[]    xmlel_TestBag;

    /** Holds value of property XTestResultsReport_id. */
    private long XTestResultsReport_id;    

}
