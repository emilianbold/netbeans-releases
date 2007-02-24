package org.netbeans.modules.uml.core.roundtripframework.roundtripevents;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class AllRoundtripEventsTests {

	
	public static void main(String[] args) 
	{
		TestRunner.run(suite());
	}
	
	public static Test suite()
	{
		TestSuite suite = new TestSuite("RoundtripEvents Tests");
		suite.addTest(new TestSuite(RoundTripAttributeEventsTestCase.class));
		suite.addTest(new TestSuite(RoundTripClassEventsTestCase.class));
		suite.addTest(new TestSuite(RoundTripEnumAndEnumLiteralEventsTestCase.class));
		suite.addTest(new TestSuite(RoundTripInterfaceEventsTestCase.class));
		suite.addTest(new TestSuite(RoundTripOperationEventsTestCase.class));
		suite.addTest(new TestSuite(RoundTripPackageEventsTestCase.class));
		suite.addTest(new TestSuite(RoundTripRelationEventsTestCase.class));
		
		return(suite);
	}
}
