package org.netbeans.modules.uml.core.eventframework;

import junit.framework.TestCase;

import org.netbeans.modules.uml.core.eventframework.EventDispatchController;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementChangeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ClassifierEventDispatcher;

public class GetDispatcherTestCase extends TestCase 
{
	ClassifierEventDispatcher classifierEvntDisp = null;
	ElementChangeEventDispatcher elementChangeEvntDisp = null;
	EventDispatchController eventDispatchContlr = null;
	EventDispatchRetriever eventDispatchRetriever = null;
	protected  void setUp()
	{
		// Initializes EventDispatchers, EventDispatchController and EventDispatchRetriever
		classifierEvntDisp = new ClassifierEventDispatcher();
		elementChangeEvntDisp = new ElementChangeEventDispatcher();
		eventDispatchContlr = new EventDispatchController();
		eventDispatchRetriever = new EventDispatchRetriever(eventDispatchContlr);
		
	}
	public void testGetDispatcher()
	{
		// Make sure that the EventDispatchRetriever doesn't contains the EventDispatchers 
		ClassifierEventDispatcher clsfirevntDispchr = eventDispatchRetriever.getDispatcher("ClassifierDispatcher");
		ElementChangeEventDispatcher elemChngDispchr = eventDispatchRetriever.getDispatcher("ElementChangeEventDispatcher");
		assertNull(clsfirevntDispchr);
		assertNull(elemChngDispchr);
		
		// Adds the two dispatchers to EventDispatchController
		eventDispatchContlr.addDispatcher("ClassifierDispatcher",classifierEvntDisp);
		eventDispatchContlr.addDispatcher("ElementChangeEventDispatcher",elementChangeEvntDisp);
		
		// Checks whether the EventDispatchRetriever contains both the dispatchers
		clsfirevntDispchr = eventDispatchRetriever.getDispatcher("ClassifierDispatcher");
		elemChngDispchr = eventDispatchRetriever.getDispatcher("ElementChangeEventDispatcher");
		assertNotNull(clsfirevntDispchr);
		assertNotNull(elemChngDispchr);
	}

}
