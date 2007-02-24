
/*
 * Created on Oct 17, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.behavior;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
/**
 * @author aztec
 *
 */
public class CallActionTestCase extends AbstractUMLTestCase
{

	public CallActionTestCase()
	{
	}
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(CallActionTestCase.class);
	}
	
	public void testSetOperation()
	{   
		ICallAction callAction = factory.createCallAction(null); 
		project.addElement(callAction);
		     
		IClass cl = createClass("Trellis");
		IOperation op = cl.createOperation("int", "almond");
		cl.addOperation(op);
		
		callAction.setOperation(op);
		IOperation opGot = callAction.getOperation();
		
		assertEquals(op.getXMIID(), opGot.getXMIID());
	}
}



