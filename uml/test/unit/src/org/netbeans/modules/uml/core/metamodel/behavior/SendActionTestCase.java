
/*
 * Created on Oct 17, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.behavior;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IReception;
/**
 * @author aztec
 *
 */
public class SendActionTestCase extends AbstractUMLTestCase
{
	public SendActionTestCase()
	{
	}
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(SendActionTestCase.class);
	}
	
	public void testSetReception()
	{
		ISendAction sendAction = factory.createSendAction(null);
		project.addElement(sendAction);
		
		IReception recep = factory.createReception(null);
		project.addElement(recep);
		
		sendAction.setReception(recep);
		IReception recepGot = sendAction.getReception();
		
		assertEquals(recep.getXMIID(), recepGot.getXMIID());
	}
}



