package org.netbeans.modules.uml.core.eventframework;

import junit.framework.TestCase;

import org.netbeans.modules.uml.core.eventframework.EventContext;
import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventPayload;
import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class RegisterForEventFrameworkEventsTestCase extends TestCase implements IEventFrameworkEventsSink{

	private boolean testData = false;
	EventDispatcher eventDisptcher = null;
	EventContext context = null;
	EventPayload payload = null;

	protected void setUp()
	{
		// Initializes the EventDispatcher, EventContext and the EventPayload
		eventDisptcher = new EventDispatcher();
		context = new EventContext();
		payload = new EventPayload();
	}
	public  void testRegisterForEventFrameworkEvents()
	{
		// Set the EventDispatcher and the listener to the EventManager
		eventDisptcher.registerForEventFrameworkEvents(this);
		assertFalse(testData);
		
		// Tests the whether the firing of events happens correctly which make sure that
		// the EventDispatcher and the listener are correctly set
		eventDisptcher.firePreEventContextPushed(context,payload);
		assertTrue(testData);
	}

	public void onPreEventContextPushed(IEventContext pContext, IResultCell pCell) {
		testData = true;
	}
	public void onEventContextPushed(IEventContext pContext, IResultCell pCell) {
		//		 TODO Auto-generated method stub
	}
	public void onPreEventContextPopped(IEventContext pContext, IResultCell pCell) {
		// TODO Auto-generated method stub
		
	}
	public void onEventContextPopped(IEventContext pContext, IResultCell pCell) {
		// TODO Auto-generated method stub
		
	}
	public void onEventDispatchCancelled(ETList<Object> pListeners, Object listenerWhoCancelled, IResultCell pCell) {
		// TODO Auto-generated method stub
		
	}

}
