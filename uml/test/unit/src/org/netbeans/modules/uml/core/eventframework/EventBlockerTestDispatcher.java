package org.netbeans.modules.uml.core.eventframework;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;

public class EventBlockerTestDispatcher extends EventDispatcher
{
	protected static boolean isCalled;
	
	public void fireEventContextPopped( IEventContext pContext, IEventPayload payLoad )
	{
		if( validateEvent( "EventContextPopped", pContext) )
		{
			isCalled = true;
		}
	}
	
	
}
