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
 * TestBag.java
 *
 * Created on November 1, 2001, 6:20 PM
 */

package org.netbeans.xtest.pe.xmlbeans;

/**
 *
 * @author  mb115822
 */
public class TestBag extends XMLBean {

    /** Creates new TestBag */
    public TestBag() {
    }
    
    // attributes
    public String   xmlat_name;
    public String   xmlat_module;
    public String   xmlat_testType;
    public String   xmlat_executor;
    public String   xmlat_testAttribs;    
    public java.sql.Timestamp     xmlat_timeStamp;
    public long     xmlat_time;
    public long     xmlat_testsTotal;
    public long     xmlat_testsPass;
    public long     xmlat_testsFail;
    public long     xmlat_testsError;
    
    public String   xmlat_bagID;
    
    // child elements
    public UnitTestSuite[] xmlel_UnitTestSuite;
}
