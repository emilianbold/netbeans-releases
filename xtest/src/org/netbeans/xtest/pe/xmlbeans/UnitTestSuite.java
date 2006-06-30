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
 * UnitTestSuite.java
 *
 * Created on November 1, 2001, 6:17 PM
 */

package org.netbeans.xtest.pe.xmlbeans;

import java.io.*;

/**
 *
 * @author  mb115822
 */
public class UnitTestSuite extends XMLBean {

    /** Creates new UnitTestSuite */
    public UnitTestSuite() {
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
    
    /** Getter for property unexpectedFailure.
     * @return Value of property unexpectedFailure.
     */
    public String getUnexpectedFailure() {
        return xmlat_unexpectedFailure;    
    }
    
    /** Setter for property unexpectedFailure.
     * @param unexpectedFailure New value of property unexpectedFailure.
     */
    public void setUnexpectedFailure(String unexpectedFailure) {
        xmlat_unexpectedFailure = unexpectedFailure;
    }
    
    /** Getter for property testBag_id.
     * @return Value of property testBag_id.
     */
    public long getTestBag_id() {
        return this.testBag_id;
    }    
 
    /** Setter for property testBag_id.
     * @param testBag_id New value of property testBag_id.
     */
    public void setTestBag_id(long testBag_id) {
        this.testBag_id = testBag_id;
    }
    
    // attributes
    public String   xmlat_name;
    public java.sql.Timestamp      xmlat_timeStamp;
    public long     xmlat_time;
    public long     xmlat_testsTotal;
    public long     xmlat_testsPass;
    public long     xmlat_testsFail;
    public long     xmlat_testsError;
    public long     xmlat_testsUnexpectedPass;
    public long     xmlat_testsExpectedFail;
    public String   xmlat_unexpectedFailure;
    
    // child elements
    public UnitTestCase[] xmlel_UnitTestCase;
    public Data[] xmlel_Data;

    /** Holds value of property testBag_id. */
    private long testBag_id;
    
    
    // load UnitTestSuite from a file
    public static UnitTestSuite loadFromFile(File aFile) throws IOException, ClassNotFoundException {
        XMLBean xmlBean = XMLBean.loadXMLBean(aFile);
        if (!(xmlBean instanceof UnitTestSuite)) {
            throw new ClassNotFoundException("Loaded file "+aFile+" does not contain UnitTestSuite");
        }
        return (UnitTestSuite)xmlBean;
    }
    
}
