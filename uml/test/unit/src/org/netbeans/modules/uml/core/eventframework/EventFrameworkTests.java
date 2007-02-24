package org.netbeans.modules.uml.core.eventframework;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class EventFrameworkTests 
{
	public static Test suite()
	{
		TestSuite test = new TestSuite("Event Framework TestSuite");
		test.addTestSuite(NotifyListenersTestCase.class);
		test.addTestSuite(NotifyListenersWithObjectParameterTestCase.class);
		test.addTestSuite(NotifyListenersWithQualifiedProceedTestCase.class);
		test.addTestSuite(GetDispatcherTestCase.class);
        test.addTestSuite(RetrieveDispatcherOnProductTestCase.class);
		test.addTestSuite(RegisterForEventFrameworkEventsTestCase.class);
		test.addTestSuite(FirePreEventContextPushedTestCase.class);
		test.addTestSuite(FireEventContextPushedTestCase.class);
		test.addTestSuite(FirePreEventContextPoppedTestCase.class);
		test.addTestSuite(FireEventContextPoppedTestCase.class);
		test.addTestSuite(GetMechanismTestCase.class);
		test.addTestSuite(StartBlockingWithDispatcherAsArgumentTestCase.class);
		test.addTestSuite(StopBlockingWithDispatcherAsArgumentTestCase.class);
		test.addTestSuite(StartBlockingProductEventsTestCase.class);
		test.addTestSuite(StopBlockingProductEventsTestCase.class);
		return test;
	}
	public static void main(String[] args) 
	{
		TestRunner.run(suite());
	}

}
