
/*
 * Created on Sep 26, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;

import junit.framework.Test;
import junit.framework.TestSuite;
/**
 * @author aztec
 *
 */
public class AllCommonStateMachineTests
{

	public static Test suite()
	{
	   TestSuite suite = new TestSuite("Common State Machine Tests");
	   
	   suite.addTest(new TestSuite(PseudoStateTestCase.class));	   
	   suite.addTest(new TestSuite(RegionTestCase.class));
	   suite.addTest(new TestSuite(StateMachineTestCase.class));
	   suite.addTest(new TestSuite(StateTestCase.class));
	   suite.addTest(new TestSuite(StateVertexTestCase.class));
	   suite.addTest(new TestSuite(TransitionTestCase.class));
	   suite.addTest(new TestSuite(UMLConnectionPointTestCase.class));
	   suite.addTest(new TestSuite(ProtocolConformanceTestCase.class));
	   return suite;
	}
	
	public static void main(String args[]) 
	{
		junit.textui.TestRunner.run(suite());
	}
}



