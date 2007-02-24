
/*
 * Created on Sep 25, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;

import java.util.Iterator;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IEvent;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author aztec
 *
 */
public class StateTestCase extends AbstractUMLTestCase
{
	private IState state = null;
	private IProcedure procedure = null; 
	
	public StateTestCase()
	{
		super();
	}
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(StateTestCase.class);
	}
	
	protected void setUp()
	{
		state = factory.createState(null);
		project.addElement(state);
		
		procedure = (IProcedure)FactoryRetriever.instance().createType("Procedure", null);
		//procedure.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(procedure);
	}
	
	public void testAddContent()
	{
		IRegion region = (IRegion)FactoryRetriever.instance().createType("Region", null);
		//region.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(region);
		
		//Add and Get Content
		state.addContent(region);
		ETList<IRegion> contents = state.getContents();
		assertNotNull(contents);
		
        assertEquals(region.getXMIID(), contents.get(1).getXMIID());
		
		//Remove Input
		state.removeContent(region);
		contents = state.getContents();
        assertEquals(1, contents.size());
	}

	public void testSetIsComposite()
	{
		state.setIsComposite(true);
		assertTrue(state.getIsComposite());
	}

	public void testSetIsOrthogonal()
	{
		state.setIsOrthogonal(true);
		assertTrue(state.getIsOrthogonal());
	}
	
	public void testSetIsSimple()
	{
		state.setIsSimple(true);
		assertTrue(state.getIsSimple());
	}
	
	public void testSetIsSubmachineState()
	{
		state.setIsSubmachineState(true);
		assertTrue(state.getIsSubmachineState());
	}
	
	public void testSetDoActivity()
	{	
		state.setDoActivity(procedure);
		IProcedure procedureGot = state.getDoActivity();
		assertEquals(procedure.getXMIID(), procedureGot.getXMIID());
	}
	
	public void testSetExit()
	{	
		state.setExit(procedure);
		IProcedure procedureGot = state.getExit();
		assertEquals(procedure.getXMIID(), procedureGot.getXMIID());
	}
	
	public void testSetEntry()
	{	
		state.setEntry(procedure);
		IProcedure procedureGot = state.getEntry();
		assertEquals(procedure.getXMIID(), procedureGot.getXMIID());
	}
	
	public void testSetStateInvariant()
	{
		IConstraint constraint = factory.createConstraint(null);
		state.setStateInvariant(constraint);
		IConstraint constraintGot = state.getStateInvariant();
		assertEquals(constraint.getXMIID(), constraintGot.getXMIID());
	}
	
	public void testSetSubmachine()
	{
		IStateMachine machine = factory.createStateMachine(null);
		project.addElement(machine);
		
		state.setSubmachine(machine);
		IStateMachine machineGot = state.getSubmachine();
		assertNotNull(machineGot);
		assertEquals(machine.getXMIID(), machineGot.getXMIID());
	}
	
	public void testAddDefferableEvent()
	{
		IEvent event = (IEvent)FactoryRetriever.instance().createType("Event", null);
//		{			
//			public void establishNodePresence(Document doc, Node node)
//			{
//				super.buildNodePresence("UML:Event", doc, node);
//			}
//		};
//		event.prepareNode(DocumentFactory.getInstance().createElement(""));

		if (event == null)
		{
			return;
		}
		project.addElement(event);
		
		//Add and Get DefferableEvent
		state.addDefferableEvent(event);
		ETList<IEvent> contents = state.getDeferrableEvents();
		assertNotNull(contents);
		
		Iterator iter = contents.iterator();
		while (iter.hasNext())
		{
			IEvent eventGot = (IEvent)iter.next();
			assertEquals(event.getXMIID(), eventGot.getXMIID());							
		}
		
		//Remove Input
		state.removeDeferrableEvent(event);
		contents = state.getDeferrableEvents();
		if (contents != null)
		{
			assertEquals(0,contents.size());
		} 
	}
}



