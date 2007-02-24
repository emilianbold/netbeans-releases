package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
/**
 */
public class AllCoreInfraTests 
{
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
    
	public static Test suite()
	{
	   TestSuite suite = new TestSuite("Core Infrastructure Tests");
      
	   suite.addTest(new TestSuite(ClassifierTestCase.class));
       suite.addTest(new TestSuite(AggregationTestCase.class));
       suite.addTest(new TestSuite(ArgumentTestCase.class));
       suite.addTest(new TestSuite(AssociationTestCase.class));
       suite.addTest(new TestSuite(AssociationEndTestCase.class));
       suite.addTest(new TestSuite(AttributeTestCase.class));
       suite.addTest(new TestSuite(BehaviorTestCase.class));
       suite.addTest(new TestSuite(BehavioralFeatureTestCase.class));
       suite.addTest(new TestSuite(ClassifierEventDispatcherTestCase.class));
       suite.addTest(new TestSuite(CollaborationOccurrenceTestCase.class));
       suite.addTest(new TestSuite(DerivationTestCase.class));
       suite.addTest(new TestSuite(EventTestCase.class));
       suite.addTest(new TestSuite(FeatureTestCase.class));
       suite.addTest(new TestSuite(GeneralizationTestCase.class));
       suite.addTest(new TestSuite(ImplementationTestCase.class));
       suite.addTest(new TestSuite(IncrementTestCase.class));
       suite.addTest(new TestSuite(InterfaceTestCase.class));
       suite.addTest(new TestSuite(NavigableEndTestCase.class));
       suite.addTest(new TestSuite(OperationTestCase.class));
       suite.addTest(new TestSuite(ParameterableElementTestCase.class));
       suite.addTest(new TestSuite(ParameterTestCase.class));
       suite.addTest(new TestSuite(RoleBindingTestCase.class));
       suite.addTest(new TestSuite(SignalTestCase.class));
       suite.addTest(new TestSuite(StructuralFeatureTestCase.class));
       suite.addTest(new TestSuite(TypedElementTestCase.class));
       suite.addTest(new TestSuite(UMLBindingTestCase.class));

	   return suite;
	}
}
