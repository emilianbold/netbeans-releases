/*
 * UnitTestSuite.java
 *
 * Created on November 1, 2001, 6:17 PM
 */

package org.netbeans.xtest.pe.xmlbeans;

/**
 *
 * @author  mb115822
 */
public class UnitTestSuite extends XMLBean {

    /** Creates new UnitTestSuite */
    public UnitTestSuite() {
    }
    
    // attributes
    public String   xmlat_name;
    public java.sql.Timestamp      xmlat_timeStamp;
    public long     xmlat_time;
    public long     xmlat_testsTotal;
    public long     xmlat_testsPass;
    public long     xmlat_testsFail;
    public long     xmlat_testsError;
    
    // child elements
    public UnitTestCase[] xmlel_UnitTestCase;

}
