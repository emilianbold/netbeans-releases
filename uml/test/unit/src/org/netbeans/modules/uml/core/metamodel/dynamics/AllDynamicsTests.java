package org.netbeans.modules.uml.core.metamodel.dynamics;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 */
public class AllDynamicsTests 
{
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
    
	public static Test suite()
	{
        TestSuite suite = new TestSuite("Dynamics Tests");

        suite.addTest(new TestSuite(ActionOccurrenceTestCase.class));
        suite.addTest(new TestSuite(AtomicFragmentTestCase.class));
        suite.addTest(new TestSuite(ChangeSignalTestCase.class));
        suite.addTest(new TestSuite(CombinedFragmentTestCase.class));
        suite.addTest(new TestSuite(DynamicsEventDispatcherTestCase.class));
        suite.addTest(new TestSuite(EventOccurrenceTestCase.class));
        suite.addTest(new TestSuite(ExecutionOccurrenceTestCase.class));
        suite.addTest(new TestSuite(GateTestCase.class));
        suite.addTest(new TestSuite(GeneralOrderingTestCase.class));
        suite.addTest(new TestSuite(InteractionConstraintTestCase.class));
        suite.addTest(new TestSuite(InteractionFragmentTestCase.class));
        suite.addTest(new TestSuite(InteractionOccurrenceTestCase.class));
        suite.addTest(new TestSuite(InteractionOperandTestCase.class));
        suite.addTest(new TestSuite(InteractionTestCase.class));
        suite.addTest(new TestSuite(InterGateConnectorTestCase.class));
        suite.addTest(new TestSuite(LifelineTestCase.class));
        suite.addTest(new TestSuite(MessageConnectorTestCase.class));
        suite.addTest(new TestSuite(MessageTestCase.class));
        suite.addTest(new TestSuite(ProcedureOccurrenceTestCase.class));
        suite.addTest(new TestSuite(StateInvariantTestCase.class));
        suite.addTest(new TestSuite(TimeSignalTestCase.class));

        return suite;
	}
}