package org.netbeans.modules.uml.core.metamodel.core.constructs;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 */
public class AllConstructsTests 
{
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
    
	public static Test suite()
	{
        TestSuite suite = new TestSuite("Constructs Tests");

        suite.addTest(new TestSuite(AliasedTypeTestCase.class));
        suite.addTest(new TestSuite(ClassTestCase.class));
        suite.addTest(new TestSuite(ConstructsRelationFactoryTestCase.class));
        suite.addTest(new TestSuite(EnumerationLiteralTestCase.class));
        suite.addTest(new TestSuite(EnumerationTestCase.class));
        suite.addTest(new TestSuite(ExtendTestCase.class));
        suite.addTest(new TestSuite(IncludeTestCase.class));
        suite.addTest(new TestSuite(PartFacadeTestCase.class));
        suite.addTest(new TestSuite(UseCaseDetailTestCase.class));
        suite.addTest(new TestSuite(UseCaseTestCase.class));


        return suite;
	}
}