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


}
