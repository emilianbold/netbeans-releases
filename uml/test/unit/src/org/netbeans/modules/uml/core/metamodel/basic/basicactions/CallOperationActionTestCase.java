
/*
 * Created on Sep 24, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.basic.basicactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author aztec
 *
 */
public class CallOperationActionTestCase extends AbstractUMLTestCase
{
	private ICallOperationAction action = null;
	private IOperation  op;
	private IClassifier cl;
	
	public CallOperationActionTestCase()
	{
		super();		
	}
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(CallOperationActionTestCase.class);
	}
	protected void setUp() throws Exception
	{
		action = (ICallOperationAction)FactoryRetriever.instance().createType("CallOperationAction", null);
		//action.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(action);
	}
	
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	public void testSetOperation()
	{	
		cl = createClass("Trellis");
		op = cl.createOperation("int", "almond");
		cl.addOperation(op);
		
		action.setOperation(op);
		IOperation operGot = action.getOperation();
		assertNotNull(operGot);
		assertEquals(op.getXMIID(), operGot.getXMIID()); 
	}
	
	public void testAddToResult()
	{
		IOutputPin result = (IOutputPin)FactoryRetriever.instance().createType("OutputPin", null);
		//result.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(result);	
		//Add Result
		action.addToResult(result);
		
		//Get Result
		ETList<IOutputPin> results = action.getResults();
		assertNotNull(results);
		
		Iterator iter = results.iterator();
		while (iter.hasNext())
		{
			IOutputPin resultGot = (IOutputPin)iter.next();
			assertEquals(result.getXMIID(), resultGot.getXMIID());							
		}
		
		//Remove Result
		action.removeFromResult(result);
		results = action.getResults();
		if (results != null)
		{
			assertEquals(0,results.size());
		}
	}
	

}



