
/*
 * Created on Oct 17, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.behavior;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author aztec
 *
 */
public class AllBehaviorTests
{
	public static void main(String[] args)
	{
		TestRunner.run(suite());
	}
    
	public static Test suite()
	{
	   TestSuite suite = new TestSuite("Behavior Tests");
      
	   suite.addTest(new TestSuite(ActionSequenceTestCase.class));
	   suite.addTest(new TestSuite(AssignmentActionTestCase.class));
	   suite.addTest(new TestSuite(CallActionTestCase.class));
	   suite.addTest(new TestSuite(CallEventTestCase.class));
	   suite.addTest(new TestSuite(ChangeEventTestCase.class));
	   suite.addTest(new TestSuite(CreateActionTestCase.class));
	   suite.addTest(new TestSuite(SendActionTestCase.class));
	   suite.addTest(new TestSuite(SignalEventTestCase.class));
	   suite.addTest(new TestSuite(TimeEventTestCase.class));
	   
	   return suite;
	}
}



