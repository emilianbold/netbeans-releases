package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 */
public class AllJavaRPComponentTests
{
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite("JavaRPComponent Tests");
        
        suite.addTest(new TestSuite(JavaAttributeChangeFacilityTestCase.class));
        suite.addTest(new TestSuite(JavaAssociationTestCase.class));
        suite.addTest(new TestSuite(JavaClassChangeHandlerTestCase.class));
        suite.addTest(new TestSuite(JavaGeneralizationChangeHandlerTestCase.class));
        suite.addTest(new TestSuite(JavaImplementationChangeHandlerTestCase.class));
        suite.addTest(new TestSuite(JavaAttributeChangeHandlerTestCase.class));
        suite.addTest(new TestSuite(JavaMethodChangeHandlerTestCase.class));
	    suite.addTest(new TestSuite(JavaEnumerationChangeHandlerTestCase.class));
	    suite.addTest(new TestSuite(JavaInterfaceChangeHandlerTestCase.class));
	    suite.addTest(new TestSuite(JavaRequestProcessorTestCase.class));
        
        return suite;
    }
}