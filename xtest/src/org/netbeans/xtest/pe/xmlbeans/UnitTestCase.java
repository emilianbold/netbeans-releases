/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * UnitTestCase.java
 *
 * Created on November 1, 2001, 6:14 PM
 */

package org.netbeans.xtest.pe.xmlbeans;

/**
 *
 * @author  mb115822
 */
public class UnitTestCase extends XMLBean {

    public static final String TEST_PASS="pass";
    public static final String TEST_FAIL="fail";
    public static final String TEST_ERROR="error";
    public static final String TEST_UNKNOWN="unknown";
    public static final String TEST_UNEXPECTED_PASS="unexpected pass";
    public static final String TEST_EXPECTED_FAIL="expected fail";
    
    /** Creates new UnitTestCase */
    public UnitTestCase() {
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
    
    /** Getter for property className.
     * @return Value of property className.
     */
    public String getClassName() {
        return xmlat_class;
    }
    
    /** Setter for property className.
     * @param className New value of property className.
     */
    public void setClassName(String className) {
        xmlat_class = className;
    }
    
    /** Getter for property result.
     * @return Value of property result.
     */
    public String getResult() {
        return xmlat_result;
    }
    
    /** Setter for property result.
     * @param result New value of property result.
     */
    public void setResult(String result) {
        xmlat_result = result;
    }
    
    /** Getter for property workdir.
     * @return Value of property workdir.
     */
    public String getWorkdir() {
        return xmlat_workdir;
    }
    
    /** Setter for property workdir.
     * @param workdir New value of property workdir.
     */
    public void setWorkdir(String workdir) {
        xmlat_workdir = workdir;
    }
    
    /** Getter for property message.
     * @return Value of property message.
     */
    public String getMessage() {
        return xmlat_message;
    }
    
    /** Setter for property message.
     * @param message New value of property message.
     */
    public void setMessage(String message) {
        xmlat_message = message;
    }
    
    /** Getter for property type.
     * @return Value of property type.
     */
    public String getType() {
        return xmlat_type;
    }
    
    /** Setter for property type.
     * @param type New value of property type.
     */
    public void setType(String type) {
        xmlat_type = type;
    }
    
    /** Getter for property unitTestSuite_id.
     * @return Value of property unitTestSuite_id.
     */
    public long getUnitTestSuite_id() {
        return this.unitTestSuite_id;
    }
    
    /** Setter for property unitTestSuite_id.
     * @param unitTestSuite_id New value of property unitTestSuite_id.
     */
    public void setUnitTestSuite_id(long unitTestSuite_id) {
        this.unitTestSuite_id = unitTestSuite_id;
    }
    
    /** Getter for property stackTrace.
     * @return Value of property stackTrace.
     */
    public String getStackTrace() {
        return xml_cdata;
    }
    
    /** Setter for property stackTrace.
     * @param stackTrace New value of property stackTrace.
     */
    public void setStackTrace(String stackTrace) {
        xml_cdata = stackTrace;
    }

    /** Getter for property failReason.
     * @return Value of property failReason.
     */
    public String getFailReason() {
        return xmlat_failReason;
    }
    
    /** Setter for property failReason.
     * @param message New value of property failReason.
     */
    public void setFailReason(String failReason) {
        xmlat_failReason = failReason;
    }

    // attributes
    public String   xmlat_class;
    public String   xmlat_name;
    public String   xmlat_result;
    public String   xmlat_workdir;
    public String   xmlat_message;
    public String   xmlat_failReason;
    public String   xmlat_type;
    public long     xmlat_time;

    /** Holds value of property unitTestSuite_id. */
    private long unitTestSuite_id;
    
}
