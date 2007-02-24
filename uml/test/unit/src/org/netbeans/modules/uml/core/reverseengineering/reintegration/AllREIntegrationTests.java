package org.netbeans.modules.uml.core.reverseengineering.reintegration;

import org.netbeans.modules.uml.core.reverseengineering.reintegration.umlparsingintegratortestcases.AllUMLParsingIntegrationTestSuite;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
/**
 */
public class AllREIntegrationTests 
{
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
    
	public static Test suite()
	{
        TestSuite suite = new TestSuite("REIntegration Tests");

       suite.addTest(new TestSuite(UMLParsingIntegratorTestCase.class));
       suite.addTest(AllUMLParsingIntegrationTestSuite.suite());
        return suite;
	}
}
