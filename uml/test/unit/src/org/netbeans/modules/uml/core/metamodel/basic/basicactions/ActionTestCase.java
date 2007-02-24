
/*
 * Created on Sep 15, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.basic.basicactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author aztec
 *
 */
public class ActionTestCase extends AbstractUMLTestCase
{
	private IValueSpecification vSpec = null;
	private IAction action = null;
	public ActionTestCase()
	{
		super();				
	}
	
	protected void setUp() throws Exception
	{
		action = factory.createCreateAction(null);		
		project.addElement(action);
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(ActionTestCase.class);
	}
	
	public void testAddInput()
	{
		vSpec = factory.createExpression(null);
		project.addElement(vSpec);
		
		//Add & get Inputs
		action.addInput(vSpec);
		ETList<IValueSpecification> specs = action.getInputs();
		assertNotNull(specs);
				
		Iterator iter = specs.iterator();
		while (iter.hasNext())
		{
			IValueSpecification vspecGot = (IValueSpecification)iter.next();
			assertEquals(vSpec.getXMIID(), vspecGot.getXMIID());							
		}
		
		//Remove Input
		action.removeInput(vSpec);
		specs = action.getInputs();
		if (specs != null)
		{
			assertEquals(0,specs.size());
		}
	}
	
	public void testAddOutput()
	{
		IOutputPin outPin = (IOutputPin)FactoryRetriever.instance().createType("OutputPin", null);
		//outPin.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(outPin);
		
		//Add & get Outputs
		action.addOutput(outPin);
		ETList<IOutputPin> outPins = action.getOutputs();		
		assertNotNull(outPins);
				
		Iterator iter = outPins.iterator();
		while (iter.hasNext())
		{
			IOutputPin outPinGot = (IOutputPin)iter.next();
			assertEquals(outPin.getXMIID(), outPinGot.getXMIID());							
		}
		
		//Remove Output
		action.removeOutput(outPin);
		outPins = action.getOutputs();
		if (outPins != null)
		{
			assertEquals(0,outPins.size());
		}			
	}	
	
	public void testAddJumpHandler()
	{
		IJumpHandler handler = (IJumpHandler)FactoryRetriever.instance().createType("JumpHandler", null);
		//handler.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(handler);
		
		//Add & get Outputs
		action.addJumpHandler(handler);
		ETList<IJumpHandler> handlers = action.getJumpHandlers();		
		assertNotNull(handlers);
				
		Iterator iter = handlers.iterator();
		while (iter.hasNext())
		{
			IJumpHandler handlerGot = (IJumpHandler)iter.next();
			assertEquals(handler.getXMIID(), handlerGot.getXMIID());							
		}
		
		//Remove Output
		action.removeJumpHandler(handler);
		handlers = action.getJumpHandlers();	
		if (handlers != null)
		{
			assertEquals(0,handlers.size());
		}			
	}	
	
	public void testAddPredecessor()
	{
		IAction predecessor = factory.createCreateAction(null);
		project.addElement(predecessor);
		
		//Add & get Outputs
		action.addPredecessor(predecessor);
		ETList<IAction> predecessors = action.getPredecessors();		
		assertNotNull(predecessors);
				
		Iterator iter = predecessors.iterator();
		while (iter.hasNext())
		{
			IAction predecessorGot = (IAction)iter.next();
			assertEquals(predecessor.getXMIID(), predecessorGot.getXMIID());							
		}
		
		//Remove Output
		action.removePredecessor(predecessor);
		predecessors = action.getPredecessors();	
		if (predecessors != null)
		{
			assertEquals(0,predecessors.size());
		}			
	}
	
	public void testAddSuccessor()
	{
		IAction successor = factory.createCreateAction(null);
		project.addElement(successor);
		
		//Add & get Outputs
		action.addSuccessor(successor);
		ETList<IAction> successors = action.getSuccessors();		
		assertNotNull(successors);
				
		Iterator iter = successors.iterator();
		while (iter.hasNext())
		{
			IAction successorGot = (IAction)iter.next();
			assertEquals(successor.getXMIID(), successorGot.getXMIID());							
		}
		
		//Remove Output
		action.removeSuccessor(successor);
		successors = action.getSuccessors();	
		if (successors != null)
		{
			assertEquals(0,successors.size());
		}			
	}
	
	public void testSetIsReadOnly()
	{
		action.setIsReadOnly(true);
		assertTrue(action.getIsReadOnly());
	}
}



