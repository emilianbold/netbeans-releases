/*
 * XTestReport.java
 *
 * Created on November 19, 2001, 4:45 PM
 */

package org.netbeans.xtest.pe.xmlbeans;

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
    public String   xmlat_attributes;
    public long     xmlat_testsTotal;
    public long     xmlat_testsPass;
    public long     xmlat_testsFail;
    public long     xmlat_testsError;
    public boolean  xmlat_fullReport;
    
    // child elements
    public SystemInfo[] xmlel_SystemInfo;
    public TestRun[] xmlel_TestRun;
    
}
