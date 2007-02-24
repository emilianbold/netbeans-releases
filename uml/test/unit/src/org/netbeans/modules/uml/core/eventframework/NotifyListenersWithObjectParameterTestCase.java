package org.netbeans.modules.uml.core.eventframework;

import junit.framework.TestCase;

public class NotifyListenersWithObjectParameterTestCase extends TestCase
{
	EventManager m_Manager = null;
	EventFunctor eventFunctor = null; 
	EventFrameworkTestListener1 eveFWListener1 = null;
		
	protected void setUp()
	{
		m_Manager = new EventManager();
		eveFWListener1 = new EventFrameworkTestListener1();
	}
	
	public void testNotifyListenersWithObjectParameterTestCase()
	{
		// Adds the listener to EventManager
		m_Manager.addListener(eveFWListener1,null);
		
		// Pass the listener information to the EventFunctor
		eventFunctor = new EventFunctor(EventFrameworkTestListener1.class,"testMethod2");
		
		// Make sure that the variable in the listener class is null
		assertNull(EventFrameworkTestListener1.functRet);
		
		// Tests the whether the function gets executed correctly 		
		m_Manager.notifyListeners(eventFunctor,new String[] {"Hello"});
		assertEquals("Helloworld",EventFrameworkTestListener1.functRet);		
	}	
}
