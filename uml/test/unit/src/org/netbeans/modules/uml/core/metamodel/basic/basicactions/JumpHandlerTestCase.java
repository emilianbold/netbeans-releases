
/*
 * Created on Sep 25, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.basic.basicactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal;
import org.netbeans.modules.uml.core.support.umlutils.ETList;/**
 * @author aztec
 *
 */
public class JumpHandlerTestCase extends AbstractUMLTestCase
{
	private IJumpHandler jumpHandler = null;
	public JumpHandlerTestCase()
	{
		super();		
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(JumpHandlerTestCase.class);
	}

	protected void setUp() throws Exception
	{
		jumpHandler = (IJumpHandler)FactoryRetriever.instance().createType("JumpHandler", null);
		//jumpHandler.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(jumpHandler);
	}
	
	public void testSetBody()
	{
		IHandlerAction action = (IHandlerAction)FactoryRetriever.instance().createType("HandlerAction", null);
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
		
		jumpHandler.setBody(action);
		IHandlerAction actionGot = jumpHandler.getBody();
		assertNotNull(actionGot);
		assertEquals(action.getXMIID(), actionGot.getXMIID());
	}
	
	public void testSetIsDefault()
	{
		jumpHandler.setIsDefault(true);
		assertTrue(jumpHandler.getIsDefault());
	}
	
	public void testSetJumpType() 
	{
		ISignal sig = (ISignal)FactoryRetriever.instance().createType("Signal", null);
		//sig.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(sig);
		
		jumpHandler.setJumpType(sig);
		ISignal sigGot = jumpHandler.getJumpType();
		assertNotNull(sigGot);
		assertEquals(sig.getXMIID(), sigGot.getXMIID());
	}
	
	public void testAddProtectedAction()
	{
		IAction protectedAction = factory.createCreateAction(null);		
		project.addElement(protectedAction);
		
		//Add & get Outputs
		jumpHandler.addProtectedAction(protectedAction);
		ETList<IAction> protectedActions = jumpHandler.getProtectedActions();		
		assertNotNull(protectedActions);
				
		Iterator iter = protectedActions.iterator();
		while (iter.hasNext())
		{
			IAction protectedActionGot = (IAction)iter.next();
			assertEquals(protectedAction.getXMIID(), protectedActionGot.getXMIID());							
		}
		
		//Remove Output
		jumpHandler.removeProtectedAction(protectedAction);
		protectedActions = jumpHandler.getProtectedActions();	
		if (protectedActions != null)
		{
			assertEquals(0,protectedActions.size());
		}			
	}

}



