
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
public class JumpActionTestCase extends AbstractUMLTestCase
{

	public JumpActionTestCase()
	{
		super();
	}	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(JumpActionTestCase.class);
	}
	
	public void testSetJumpValue()
	{
		IJumpAction action = (IJumpAction)FactoryRetriever.instance().createType("JumpAction", null);
		//action.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(action);
		
		IInputPin inputPin = (IInputPin)FactoryRetriever.instance().createType("InputPin", null);
		//inputPin.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(inputPin);
		
		action.setJumpValue(inputPin);
		IInputPin inputPinGot = action.getJumpValue();
		assertEquals(inputPin.getXMIID(), inputPinGot.getXMIID());
	}

}



