
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
public class BinaryOperatorActionTestCase extends AbstractUMLTestCase
{
	private IBinaryOperatorAction binaryOpAction = null;
	private IInputPin inputPin = null;
	
	/**
	 * 
	 */
	public BinaryOperatorActionTestCase()
	{
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(BinaryOperatorActionTestCase.class);
	}
	
	public void setUp()
	{
		binaryOpAction = (IBinaryOperatorAction)FactoryRetriever.instance().createType("BinaryOperatorAction", null);
		//binaryOpAction.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(binaryOpAction);
		
		inputPin = (IInputPin)FactoryRetriever.instance().createType("InputPin", null);
		//inputPin.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(inputPin);
	}
	
	public void testSetLeftOperand()
	{		
		binaryOpAction.setLeftOperand(inputPin);
		IInputPin inPinGot = binaryOpAction.getLeftOperand();
		assertNotNull(inPinGot);
		assertEquals(inputPin.getXMIID(), inPinGot.getXMIID());
	}
	
	public void testSetRightOperand()
	{
		binaryOpAction.setRightOperand(inputPin);
		IInputPin inPinGot = binaryOpAction.getRightOperand();
		assertNotNull(inPinGot);
		assertEquals(inputPin.getXMIID(), inPinGot.getXMIID());
	}

}


