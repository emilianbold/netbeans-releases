
/*
 * Created on Oct 17, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.behavior;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import java.util.Iterator;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author aztec
 *
 */
public class ActionSequenceTestCase extends AbstractUMLTestCase
{
	public ActionSequenceTestCase()
	{
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(ActionSequenceTestCase.class);
	}
	
	public void testAddAction()
	{
		IAction action = factory.createCreateAction(null);		
		project.addElement(action);
		
		IActionSequence actionSeq = factory.createActionSequence(null);
		project.addElement(actionSeq);
		
		actionSeq.addAction(action);
		ETList<IAction> retActions = actionSeq.getActions();		
		assertNotNull(retActions);
		
		boolean found = false;
		Iterator<IAction> iter = retActions.iterator();
		if (iter != null)
		{
			while (iter.hasNext())
			{
				IAction actionGot = iter.next();
				if (actionGot != null && actionGot.getXMIID().equals(action.getXMIID()))
				{
					found = true;
					break;		
				}
			}
		}
		assertTrue(found);
		
		actionSeq.removeAction(action);
		retActions = actionSeq.getActions();		
		if (retActions != null)
			assertEquals(0,retActions.size());
	}
}



