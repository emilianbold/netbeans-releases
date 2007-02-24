package org.netbeans.modules.uml.core.eventframework;

import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.EventManager;

import junit.framework.TestCase;

public class NotifyListenersTestCase extends TestCase 
{

	EventManager m_Manager = null;
	EventFunctor eventFunctor = null;
	EventFrameworkTestListener1 eveFWListener1 = null;
	EventFrameworkTestListener2 eveFWListener2 = null;
	int listnrNo = 0;
	
	protected void setUp()
	{
		m_Manager = new EventManager();
		eveFWListener1 = new EventFrameworkTestListener1();
		eveFWListener2 = new EventFrameworkTestListener2();
	}
	
	public void testNotifyListeners()
	{
		//  Adds the listeners to EventManager
		m_Manager.addListener(eveFWListener1,null);
		m_Manager.addListener(eveFWListener2,null);
		
		//	Make sure that the variable in the listener class is set false
		assertFalse(EventFrameworkTestListener1.isCalled);
		
		// Tests the whether the function gets executed correctly by calling the first listener
		eventFunctor = new EventFunctor("org.netbeans.modules.uml.core.eventframework.EventFrameworkTestListener1","testMethod1");
		m_Manager.notifyListeners(eventFunctor);
		assertTrue(EventFrameworkTestListener1.isCalled);
		
		// Make sure that the variable in the listener class is set false
		assertFalse(EventFrameworkTestListener2.isCalled);
		
		// Tests the whether the function gets executed correctly by calling the second listener
		eventFunctor = new EventFunctor("org.netbeans.modules.uml.core.eventframework.EventFrameworkTestListener2","testMethod2");
		m_Manager.notifyListeners(eventFunctor);
		assertTrue(EventFrameworkTestListener2.isCalled);
		
		// Tests the number of listeners
		listnrNo = m_Manager.getNumListeners();
		assertEquals(listnrNo,2);
		
		// Tests whether the listeners are getting removed 
		m_Manager.removeListener(eveFWListener1);
		m_Manager.removeListener(eveFWListener2);
		listnrNo = m_Manager.getNumListeners();
		assertEquals(listnrNo,0);
		
	}
	
	protected void tearDown()
	{
		eveFWListener1.isCalled = false;
		eveFWListener2.isCalled = false;
	}

}
