package org.netbeans.modules.uml.core.support.umlmessagingcore;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllMessagingCoreTests
{
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite("MessagingCore Tests");
        
        //$JUnit-BEGIN$
        suite.addTest(new TestSuite(MessageDataFilterTestCase.class));
        suite.addTest(new TestSuite(MessageDataTestCase.class));
        suite.addTest(new TestSuite(MessageFacilityFilterTestCase.class));
        suite.addTest(new TestSuite(UMLMessagingHelperTestCase.class));
        
        
        //$JUnit-END$
        return suite;
    }
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(suite());
    }
    
    
}


