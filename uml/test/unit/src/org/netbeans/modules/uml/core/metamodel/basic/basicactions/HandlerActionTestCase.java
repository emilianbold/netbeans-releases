
/*
 * Created on Sep 24, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.basic.basicactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author aztec
 *
 */
public class HandlerActionTestCase extends AbstractUMLTestCase
{
	private HandlerAction action = null;
	public HandlerActionTestCase()
	{
		super();		
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(HandlerActionTestCase.class);
	}
	protected void setUp() throws Exception
	{
		action = (HandlerAction)FactoryRetriever.instance().createType("HandlerAction", null);
//		{			
//			public void establishNodePresence(Document doc, Node node)
//			{
//				super.buildNodePresence("UML:HandlerAction", doc, node);
//			}
//		};
//		action.prepareNode(DocumentFactory.getInstance().createElement(""));
		if (action != null)
		{
			project.addElement(action);
		}
	}
	
	public void testSetBody()
	{
		if (action == null)
		{
			return;	
		}
		IAction newAction = factory.createCreateAction(null);		
		project.addElement(newAction);
				
		action.setBody(newAction);
		IAction actionGot = action.getBody();
		assertEquals(newAction.getXMIID(), actionGot.getXMIID()); 
	}
	
	public void testSetJumpValue()
	{
		if (action == null)
		{
			return;	
		}
		IOutputPin jumpVal = (IOutputPin)FactoryRetriever.instance().createType("OutputPin", null);
		//jumpVal.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(jumpVal);
		
		action.setJumpValue(jumpVal);
		IOutputPin jumpValGot = action.getJumpValue();
		assertNotNull(jumpValGot);
		assertEquals(jumpVal.getXMIID(), jumpValGot.getXMIID());	
	}
	
	public void testAddJumpHandler()
	{
		if (action == null)
		{
			return;	
		}
		IJumpHandler handler = (IJumpHandler)FactoryRetriever.instance().createType("JumpHandler", null);
		//handler.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(handler);
		
		//Add & get Outputs
		action.addHandler(handler);
		ETList<IJumpHandler> handlers = action.getHandlers();		
		assertNotNull(handlers);
				
		Iterator iter = handlers.iterator();
		while (iter.hasNext())
		{
			IJumpHandler handlerGot = (IJumpHandler)iter.next();
			assertEquals(handler.getXMIID(), handlerGot.getXMIID());							
		}
		
		//Remove Output
		action.removeHandler(handler);
		handlers = action.getHandlers();	
		if (handlers != null)
		{
			assertEquals(0,handlers.size());
		}			
	}	

}



