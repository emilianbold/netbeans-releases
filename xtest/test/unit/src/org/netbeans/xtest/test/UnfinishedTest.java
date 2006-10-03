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
package org.netbeans.xtest.test;

import java.util.Enumeration;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/** This test verifies reporting of unfinished tests (see http://www.netbeans.org/issues/show_bug.cgi?id=73182).
 */
public class UnfinishedTest extends NbTestCase {
    
    /** Need to be defined because of JUnit */
    public UnfinishedTest(String name) {
        super(name);
    }

    /** Create test suite. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(UnfinishedTest.class);
        return suite;
    }
    
    /** Set up. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }

    /** This test case should pass.  */
    public void test1() {
    }
    
    /** This test case should end as 'uknown' with message 'Did not finish.'. */
    public void test2() {
        System.exit(1);
    }
    
    /** This test case should end as 'uknown' with message 'Did not start.'. */
    public void test3() {
    }
    
    /** This test case should end as 'uknown' with message 'Did not start.'. */
    public void test4() {
    }
}