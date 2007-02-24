package org.netbeans.modules.uml.core.eventframework;

import junit.framework.TestCase;

import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.eventframework.EventContext;
import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventPayload;
import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class StopBlockingWithDispatcherAsArgumentTestCase extends TestCase implements IEventFrameworkEventsSink
{
	private boolean testData = false;
	
	EventDispatcher eventDispather = null;
	EventContext context = null;
	EventPayload payload = null;
	
	protected void setUp()
	{
		// Initializes the EventDispatcher, EventContext,  EventPayload
		eventDispather = new EventDispatcher();
		context = new EventContext();
		payload = new EventPayload();
	}
	public  void testStopBlockingWithDispatcherAsArgument()
	{
		// Set the EventDispatcher and the listener to the EventManager
		eventDispather.registerForEventFrameworkEvents(this);
		assertFalse(testData);
		
		// Fire the event and test that its executed correctly
		eventDispather.fireEventContextPushed(context,payload);
		assertTrue(testData);
		testData = false;
		
		// Start blocking the events from EventDispatcher
		EventBlocker.startBlocking(eventDispather);
		
		// Test that no events are fired from that dispatcher
		eventDispather.fireEventContextPushed(context,payload);
		assertFalse(testData);
		
		// Stop blocking the events
		EventBlocker.stopBlocking(false,eventDispather);
		
		// Test that events are correctly fired from that dispatcher
		eventDispather.fireEventContextPushed(context,payload);
		assertTrue(testData);
	}

	public void onPreEventContextPushed()
	{
		
	}
	public void onPreEventContextPushed(IEventContext pContext, IResultCell pCell) {
		// TODO Auto-generated method stub
		
	}
	public void onEventContextPushed(IEventContext pContext, IResultCell pCell) {
		testData = true;
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
