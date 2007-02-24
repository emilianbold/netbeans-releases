
/*
 * Created on Sep 24, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.basic.basicactions;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
/**
 * @author aztec
 *
 */
public class OperatorActionTestCase extends AbstractUMLTestCase
{
	
	public OperatorActionTestCase()
	{
		super();
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(OperatorActionTestCase.class);
	}
	
	public void testSetOperatorType()
	{
		IOperatorAction action = (IOperatorAction)FactoryRetriever.instance().createType("OperatorAction", null);
//		{			
//			public void establishNodePresence(Document doc, Node node)
//			{
//				super.buildNodePresence("UML:OperatorAction", doc, node);
//			}
//		};
//		action.prepareNode(DocumentFactory.getInstance().createElement(""));
		if (action == null)
		{
			return;
		}
		project.addElement(action);
		
		action.setOperatorType("static");
		String operatorTypeGot = action.getOperatorType();
		assertEquals("static", operatorTypeGot);
	}

}



