/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.memory_usage;

import org.netbeans.junit.NbTestSuite;

/**
 * Empty Test case.
 *
 * @author  mmirilovic@netbeans.org
 */
public class EmptyTestCase extends testUtilities.PerformanceTestCase {
    
    
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
