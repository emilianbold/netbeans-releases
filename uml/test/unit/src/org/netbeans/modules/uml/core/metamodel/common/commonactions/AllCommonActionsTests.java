package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 */
public class AllCommonActionsTests 
{
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
    
	public static Test suite()
	{
        TestSuite suite = new TestSuite("Commonactions Tests");

        suite.addTest(new TestSuite(AddAttributeValueActionTestCase.class));
        suite.addTest(new TestSuite(AddVariableValueActionTestCase.class));
        suite.addTest(new TestSuite(AttributeActionTestCase.class));
        suite.addTest(new TestSuite(CallBehaviorActionTestCase.class));
        suite.addTest(new TestSuite(ClauseTestCase.class));
        suite.addTest(new TestSuite(ClearAssociationActionTestCase.class));
        suite.addTest(new TestSuite(ConditionalActionTestCase.class));
        suite.addTest(new TestSuite(CreateObjectActionTestCase.class));
        suite.addTest(new TestSuite(DestroyObjectActionTestCase.class));
        suite.addTest(new TestSuite(GroupActionTestCase.class));
        suite.addTest(new TestSuite(LinkActionTestCase.class));
        suite.addTest(new TestSuite(LinkEndCreationDataTestCase.class));
        suite.addTest(new TestSuite(LinkEndDataTestCase.class));
        suite.addTest(new TestSuite(LoopActionTestCase.class));
        suite.addTest(new TestSuite(PrimitiveFunctionTestCase.class));
        suite.addTest(new TestSuite(ReadAttributeActionTestCase.class));
        suite.addTest(new TestSuite(ReadLinkActionTestCase.class));
        suite.addTest(new TestSuite(ReadSelfActionTestCase.class));
        suite.addTest(new TestSuite(ReadVariableActionTestCase.class));
        suite.addTest(new TestSuite(SwitchActionTestCase.class));
        suite.addTest(new TestSuite(SwitchOptionTestCase.class));
        suite.addTest(new TestSuite(SynchronizedActionTestCase.class));
        suite.addTest(new TestSuite(TestIdentityActionTestCase.class));
        suite.addTest(new TestSuite(VariableActionTestCase.class));
        suite.addTest(new TestSuite(VariableTestCase.class));
        suite.addTest(new TestSuite(WriteAttributeActionTestCase.class));
        suite.addTest(new TestSuite(WriteVariableActionTestCase.class));


        return suite;
	}
}
