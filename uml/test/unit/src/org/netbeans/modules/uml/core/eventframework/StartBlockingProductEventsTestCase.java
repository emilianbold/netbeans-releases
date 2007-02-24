package org.netbeans.modules.uml.core.eventframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.eventframework.EventContext;
import org.netbeans.modules.uml.core.eventframework.EventDispatchController;
import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventPayload;

public class StartBlockingProductEventsTestCase extends AbstractUMLTestCase{

	EventContext context;
	EventPayload payload;
	EventDispatcher eventDispather;
	EventDispatchController eventDispContlr;
	IEventDispatchController prodDispCntrlr;
	protected void setUp()
	{
		// Initializes the EventDispatcher, EventContext,  EventPayload
		// and EventDispatchController
		eventDispather = new EventBlockerTestDispatcher();
		context = new EventContext();
		payload = new EventPayload();

		// Storing the event dispatcher controller object already associated with this product
		prodDispCntrlr = product.getEventDispatchController();
		
		eventDispContlr = new EventDispatchController();
	}
	public  void testStartBlockingProductEvents()
	{
		// Add the EventDispatcher to EventDispatchController
		eventDispContlr.addDispatcher("UtilDispatcher",eventDispather);
		
        // Sets the EventDispatcher to the product
	    product.setEventDispatchController(eventDispContlr);
	    
	    // Get the EventDispatcher and fire the event
	    product.getEventDispatchController().retrieveDispatcher("UtilDispatcher").fireEventContextPopped(context,payload);
	    
	    // Make sure that the event is fired correctly
	    assertTrue(EventBlockerTestDispatcher.isCalled);
	    EventBlockerTestDispatcher.isCalled = false;
	    
	    // Block all the events 
	   	EventBlocker.startBlocking();
	   	
	   	// Get the EventDispatcher and try to fire the event
	   	// Test that the event is not fired
	   	product.getEventDispatchController().retrieveDispatcher("UtilDispatcher").fireEventContextPopped(context,payload);
		assertFalse(EventBlockerTestDispatcher.isCalled);
	}
	
	public void tearDown()
	{
		// Sets back the original EventDispatchController to the product
		product.setEventDispatchController(prodDispCntrlr);
	}
}
