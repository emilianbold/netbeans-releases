
/*
 * File       : AllModelAnalysisTests.java
 * Created on : Oct 21, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.modelanalysis;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author Aztec
 */
public class AllModelAnalysisTests
{
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite("ModelAnalysis Tests");
        suite.addTest(new TestSuite(ClassifierUtilitiesTestCase.class));
        return suite;
    }
}
