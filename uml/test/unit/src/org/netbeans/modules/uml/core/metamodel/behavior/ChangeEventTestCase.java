
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
public class ChangeEventTestCase extends AbstractUMLTestCase 
{

	public ChangeEventTestCase()
	{
	}

	public static void main(String args[])
	{
		junit.textui.TestRunner.run(ChangeEventTestCase.class);
	}
	
	public void testSetChangeExpression()
	{
		IChangeEvent changeEvent = factory.createChangeEvent(null);
		project.addElement(changeEvent);
		
		IExpression exp = factory.createExpression(null);
		project.addElement(exp);
		
		changeEvent.setChangeExpression(exp);
		IExpression expGot = changeEvent.getChangeExpression();
		
		assertEquals(exp.getXMIID(), expGot.getXMIID());
	}
}



