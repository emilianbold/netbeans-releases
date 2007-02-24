
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
public class TimeEventTestCase extends AbstractUMLTestCase
{
	public TimeEventTestCase()
	{
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(TimeEventTestCase.class);
	}
	
	public void testSetWhen()
	{
		ITimeEvent timeEvent = factory.createTimeEvent(null);
		project.addElement(timeEvent);
		
		IExpression expression = factory.createExpression(null);
		project.addElement(expression);
		
		timeEvent.setWhen(expression);
		IExpression expressionGot = timeEvent.getWhen();
		
		assertEquals(expression.getXMIID(), expressionGot.getXMIID());
	}


}



