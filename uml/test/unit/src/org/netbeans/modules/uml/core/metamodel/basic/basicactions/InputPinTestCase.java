
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
public class InputPinTestCase extends AbstractUMLTestCase
{

	public InputPinTestCase()
	{
		super();		
	}

	public void testAddAction()
	{
		IInputPin inputPin = (IInputPin)FactoryRetriever.instance().createType("InputPin", null);
		//inputPin.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(inputPin);
		
		IAction action = factory.createCreateAction(null);		
		project.addElement(action);
		
		inputPin.setAction(action);
		IAction actionGot = inputPin.getAction();
		assertEquals(action, actionGot);
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(InputPinTestCase.class);
	}
	
}



