
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
public class OutputPinTestCase extends AbstractUMLTestCase
{

	public OutputPinTestCase()
	{
		super();
	}
	
	public void testAddAction()
	{
		IOutputPin outputPin = (IOutputPin)FactoryRetriever.instance().createType("OutputPin", null);
		//outputPin.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(outputPin);
		
		IAction action = factory.createCreateAction(null);		
		project.addElement(action);
		
		outputPin.setAction(action);
		IAction actionGot = outputPin.getAction();
		assertEquals(action, actionGot);
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(OutputPinTestCase.class);
	}
	

}



