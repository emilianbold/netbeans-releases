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
    
    /** Creates new UnitTestCase */
    public UnitTestCase() {
    }
    
    // attributes
    public String   xmlat_class;
    public String   xmlat_name;
    public String   xmlat_test;
    public String   xmlat_result;
    public String   xmlat_workdir;
    public String   xmlat_message;
    public String   xmlat_type;
    public long     xmlat_time;

}
