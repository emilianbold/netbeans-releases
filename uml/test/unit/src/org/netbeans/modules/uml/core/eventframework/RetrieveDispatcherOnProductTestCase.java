package org.netbeans.modules.uml.core.eventframework;

import org.netbeans.modules.uml.core.eventframework.EventContext;
import org.netbeans.modules.uml.core.eventframework.EventDispatchController;
import org.netbeans.modules.uml.core.eventframework.EventDispatchHelper;
import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventPayload;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventBlockerTestDispatcher;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

public class RetrieveDispatcherOnProductTestCase extends AbstractUMLTestCase
{
	EventContext context;
	EventPayload payload;
	EventDispatcher eventDispather;
	EventDispatchController eventDispContlr;
	IEventDispatcher eventDisp = null;
	EventDispatchHelper eventDispatchHelper;
	IEventDispatchController prodDispCntrlr;
	protected void setUp()
	{
		// Initializes the EventDispatcher, EventContext and the EventPayload,
		// EventDispatchController and EventDispatchHelper
		eventDispather = new EventBlockerTestDispatcher();
		context = new EventContext();
		payload = new EventPayload();
		// Storing the event dispatcher controller object already associated with this product
		prodDispCntrlr = product.getEventDispatchController();
		eventDispContlr = new EventDispatchController();
		eventDispatchHelper =new EventDispatchHelper();
	}

	public void testRetrieveDispatcherOnProduct()
	{
		// Adds the EventDispatcher to EventDispatchController
		eventDispContlr.addDispatcher("UtilDispatcher",eventDispather);
		
		// Make sure that the dispatcher is not been added to the EventDispatchHelper
	    eventDisp  = eventDispatchHelper.retrieveDispatcher("UtilDispatcher");
	    assertNull(eventDisp);

	    // Sets the EventDispatcher to the procuct
	    product.setEventDispatchController(eventDispContlr);
	    
	    //  Make sure that the dispatcher is been added to the EventDispatchHelper
	    eventDisp  = eventDispatchHelper.retrieveDispatcher("UtilDispatcher");
	    assertNotNull(eventDisp);
	}
	public void tearDown()
	{
		// Sets back the original EventDispatchController to the product
		product.setEventDispatchController(prodDispCntrlr);
	}
}
