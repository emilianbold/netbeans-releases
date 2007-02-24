package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 */
public class AllCommonActivitiesTests 
{
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
    
	public static Test suite()
	{
        TestSuite suite = new TestSuite("commonactivities Tests");

        suite.addTest(new TestSuite(ActivityEdgeTestCase.class));
        suite.addTest(new TestSuite(ActivityEventDispatcherTestCase.class));
        suite.addTest(new TestSuite(ActivityGroupTestCase.class));
        suite.addTest(new TestSuite(ActivityNodeTestCase.class));
        suite.addTest(new TestSuite(ActivityPartitionTestCase.class));
        suite.addTest(new TestSuite(ActivityTestCase.class));
        suite.addTest(new TestSuite(ComplexActivityGroupTestCase.class));
        suite.addTest(new TestSuite(DecisionNodeTestCase.class));
        suite.addTest(new TestSuite(InterruptibleActivityRegionTestCase.class));
        suite.addTest(new TestSuite(InvocationNodeTestCase.class));
        suite.addTest(new TestSuite(IterationActivityGroupTestCase.class));
        suite.addTest(new TestSuite(JoinNodeTestCase.class));
        suite.addTest(new TestSuite(ObjectFlowTestCase.class));
        suite.addTest(new TestSuite(ObjectNodeTestCase.class));
        suite.addTest(new TestSuite(SignalNodeTestCase.class));

        return suite;
	}
}