
/*
 * Created on Oct 17, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.behavior;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;

/** @author aztec
 *
 */
public class CallEventTestCase extends AbstractUMLTestCase
{

	public CallEventTestCase()
	{
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(CallEventTestCase.class);
	}
	
	public void testSetOperation()
	{   
		ICallEvent callEvent = factory.createCallEvent(null); 
		project.addElement(callEvent);
		     
		IClass cl = createClass("Trellis");
		IOperation op = cl.createOperation("int", "almond");
		cl.addOperation(op);
		
		callEvent.setOperation(op);
		IOperation opGot = callEvent.getOperation();
		
		assertEquals(op.getXMIID(), opGot.getXMIID());
	}

}



