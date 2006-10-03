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

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/** This test verifies that number of passes, fails and errors is correct for 
 * SpeedSuite (see http://www.netbeans.org/issues/show_bug.cgi?id=75001).
 */
public class SpeedSuiteTest75001 extends NbTestCase {
    
    /** Need to be defined because of JUnit */
    public SpeedSuiteTest75001(String name) {
        super(name);
    }

    /** Create test suite. */
    public static NbTestSuite suite() {
        return NbTestSuite.linearSpeedSuite(SpeedSuiteTest75001.class, 0, 2);
    }
    
    /** Set up. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }

    public void test1() throws InterruptedException {
        Thread.sleep(10);
    }
    
    public void test2() throws InterruptedException {
        Thread.sleep(10);
    }

    public void test3() throws InterruptedException {
        Thread.sleep(10);
    }
}