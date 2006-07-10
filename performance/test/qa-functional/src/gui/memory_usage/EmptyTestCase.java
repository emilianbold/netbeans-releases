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

package gui.memory_usage;

import org.netbeans.junit.NbTestSuite;

/**
 * Empty Test case.
 *
 * @author  mmirilovic@netbeans.org
 */
public class EmptyTestCase extends org.netbeans.performance.test.utilities.PerformanceTestCase {


    /** Creates a new instance of EmptyTestCase */
    public EmptyTestCase(String testName) {
        super(testName);
    }

    /** Creates a new instance of EmptyTestCase */
    public EmptyTestCase(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new EmptyTestCase("measureMemory"));
        return suite;
    }

    public void initialize(){
        // do nothing
    }
    
    public void prepare(){
        // do nothing
    }
    
    public org.netbeans.jemmy.operators.ComponentOperator open(){
        // do nothing
        return null;
    }
    
    public void shutdown(){
        // do nothing
    }
    
}
