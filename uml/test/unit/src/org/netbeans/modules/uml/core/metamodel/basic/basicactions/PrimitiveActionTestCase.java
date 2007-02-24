
/*
 * Created on Sep 25, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.basic.basicactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author aztec
 *
 */
public class PrimitiveActionTestCase extends AbstractUMLTestCase
{
	private IValueSpecification argument = null;
	private IPrimitiveAction action = null; 
	public PrimitiveActionTestCase()
	{
		super();		
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(PrimitiveActionTestCase.class);
	}

	protected void setUp() throws Exception
	{
		action = (IPrimitiveAction)FactoryRetriever.instance().createType("PrimitiveAction", null);
//		 {			
//			public void establishNodePresence(Document doc, Node node)
//			{
//				super.buildNodePresence("UML:PrimitiveAction", doc, node);
//			}
//		};
//		action.prepareNode(DocumentFactory.getInstance().createElement(""));
		if (action != null)
		{		
			project.addElement(action);
		}
		argument = factory.createExpression(null);
		project.addElement(argument);	
	}
	public void testAddArgument()
	{
		if (action == null)
		{
			return;	
		}
		//Add & get Arguments
		action.addArgument(argument);
		ETList<IValueSpecification> arguments = action.getArguments();		
		assertNotNull(arguments);
						
		Iterator iter = arguments.iterator();
		while (iter.hasNext())
		{
			IValueSpecification argumentGot = (IValueSpecification)iter.next();
			assertEquals(argument.getXMIID(), argumentGot.getXMIID());							
		}
				
		//Remove Argument
		action.removeArgument(argument);
		arguments = action.getArguments();
		if (arguments != null)
		{
			assertEquals(0,arguments.size());
		}			
	}
	
	public void testSetTarget()
	{
		if (action == null)
		{
			return;	
		}
		action.setTarget(argument);
		IValueSpecification argumentGot = action.getTarget();
		assertNotNull(argumentGot);
		assertEquals(argument.getXMIID(), argumentGot.getXMIID());
	}
}



