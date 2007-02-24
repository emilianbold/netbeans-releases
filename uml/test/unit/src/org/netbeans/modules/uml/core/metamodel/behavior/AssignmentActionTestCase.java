
/*
 * Created on Oct 17, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.behavior;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
/**
 * @author aztec
 *
 */
public class AssignmentActionTestCase extends AbstractUMLTestCase
{
	public AssignmentActionTestCase()
	{
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(AssignmentActionTestCase.class);
	}
	
	public void testSetValue()
	{
		IAssignmentAction action = factory.createAssignmentAction(null);
		project.addElement(action);
		
		IExpression exp = factory.createExpression(null);
		project.addElement(exp);
		
		action.setValue(exp);
		IExpression expGot = action.getValue();
		
		assertEquals(exp.getXMIID(), expGot.getXMIID());
	}
}



