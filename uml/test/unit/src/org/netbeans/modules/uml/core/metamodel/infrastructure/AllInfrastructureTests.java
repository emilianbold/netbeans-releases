package org.netbeans.modules.uml.core.metamodel.infrastructure;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 */
public class AllInfrastructureTests 
{
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
    
	public static Test suite()
	{
        TestSuite suite = new TestSuite("Infrastructure Tests");

        suite.addTest(new TestSuite(CollaborationTestCase.class));
        suite.addTest(new TestSuite(ConnectableElementTestCase.class));
        suite.addTest(new TestSuite(ConnectorEndTestCase.class));
        suite.addTest(new TestSuite(ConnectorTestCase.class));
        suite.addTest(new TestSuite(EncapsulatedClassifierTestCase.class));
        suite.addTest(new TestSuite(PartTestCase.class));
        suite.addTest(new TestSuite(PortTestCase.class));
        suite.addTest(new TestSuite(RelationFactoryTestCase.class));
        suite.addTest(new TestSuite(StructuredClassifierTestCase.class));


        return suite;
	}
}