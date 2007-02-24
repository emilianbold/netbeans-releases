package org.netbeans.modules.uml.core.reverseengineering.reframework;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 */
public class AllREFrameworkTests 
{
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
    
	public static Test suite()
	{
        TestSuite suite = new TestSuite("Reframework Tests");

        suite.addTest(new TestSuite(ActionEventTestCase.class));
        suite.addTest(new TestSuite(AttributeEventTestCase.class));
        suite.addTest(new TestSuite(ClassEventTestCase.class));
        suite.addTest(new TestSuite(CompositeClassLocatorTestCase.class));
        suite.addTest(new TestSuite(CreationEventTestCase.class));
        suite.addTest(new TestSuite(DependencyEventTestCase.class));
        suite.addTest(new TestSuite(DestroyEventTestCase.class));
        suite.addTest(new TestSuite(InitializeEventTestCase.class));
        suite.addTest(new TestSuite(JumpEventTestCase.class));
        suite.addTest(new TestSuite(LanguageLibraryTestCase.class));
        suite.addTest(new TestSuite(MethodDetailParserDataTestCase.class));
        suite.addTest(new TestSuite(MethodEventTestCase.class));
        suite.addTest(new TestSuite(OperationEventTestCase.class));
        suite.addTest(new TestSuite(PackageEventTestCase.class));
        suite.addTest(new TestSuite(ParserDataTestCase.class));
        suite.addTest(new TestSuite(REActionSequenceTestCase.class));
        suite.addTest(new TestSuite(REActionTestCase.class));
        suite.addTest(new TestSuite(REArgumentTestCase.class));
        suite.addTest(new TestSuite(REAttributeTestCase.class));
        suite.addTest(new TestSuite(RECallActionTestCase.class));
        suite.addTest(new TestSuite(REClassElementTestCase.class));
        suite.addTest(new TestSuite(REClassFeatureTestCase.class));
        suite.addTest(new TestSuite(REClassTestCase.class));
        suite.addTest(new TestSuite(REClauseTestCase.class));
        suite.addTest(new TestSuite(RECreateActionTestCase.class));
        suite.addTest(new TestSuite(RECriticalSectionTestCase.class));
        suite.addTest(new TestSuite(REDestroyActionTestCase.class));
        suite.addTest(new TestSuite(REExceptionJumpHandlerEventTestCase.class));
        suite.addTest(new TestSuite(ReferenceEventTestCase.class));
        suite.addTest(new TestSuite(REOperationTestCase.class));
        suite.addTest(new TestSuite(REParameterTestCase.class));
        suite.addTest(new TestSuite(REReturnActionTestCase.class));
        suite.addTest(new TestSuite(TestEventTestCase.class));

        return suite;
	}
}