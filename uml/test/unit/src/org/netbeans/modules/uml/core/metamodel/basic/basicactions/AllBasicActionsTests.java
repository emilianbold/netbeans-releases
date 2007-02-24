
/*
 * Created on Sep 25, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.basic.basicactions;

import junit.framework.Test;
import junit.framework.TestSuite;
/**
 * @author aztec
 *
 */
public class AllBasicActionsTests
{

	public static Test suite()
	{
	   TestSuite suite = new TestSuite("BasicActions Tests");
	   
	   suite.addTest(new TestSuite(ActionTestCase.class));
	   suite.addTest(new TestSuite(BehaviorInvocationTestCase.class));
	   suite.addTest(new TestSuite(BinaryOperatorActionTestCase.class));
	   suite.addTest(new TestSuite(BroadCastSignalActionTestCase.class));
	   suite.addTest(new TestSuite(CallOperationActionTestCase.class));
	   suite.addTest(new TestSuite(HandlerActionTestCase.class));
	   suite.addTest(new TestSuite(InputPinTestCase.class));
	   suite.addTest(new TestSuite(JumpActionTestCase.class));
	   suite.addTest(new TestSuite(JumpHandlerTestCase.class));
	   suite.addTest(new TestSuite(OperatorActionTestCase.class));
	   suite.addTest(new TestSuite(OutputPinTestCase.class));
	   suite.addTest(new TestSuite(PrimitiveActionTestCase.class));
	   
	   return suite;
	}
	
	public static void main(String args[]) 
	{
		junit.textui.TestRunner.run(suite());
	}

}



