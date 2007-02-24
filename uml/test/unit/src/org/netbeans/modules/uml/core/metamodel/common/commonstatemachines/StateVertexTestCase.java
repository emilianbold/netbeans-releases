
/*
 * Created on Sep 26, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;
import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author aztec
 *
 */
public class StateVertexTestCase extends AbstractUMLTestCase
{
	private IStateVertex stateVertex = null;
	public StateVertexTestCase()
	{
		super();
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(StateVertexTestCase.class);
	}
	
	protected void setUp()
	{
		stateVertex = (IStateVertex)FactoryRetriever.instance().createType("StateVertex", null);
		//stateVertex.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(stateVertex); 
	}
	
	public void testAddIncomingTransition()
	{
		ITransition trans = factory.createTransition(null);
		project.addElement(trans);
		
		//add and get
		stateVertex.addIncomingTransition(trans);
		ETList<ITransition> transitions = stateVertex.getIncomingTransitions();
		assertNotNull(transitions);
				
		Iterator iter = transitions.iterator();
		while (iter.hasNext())
		{
			ITransition transGot = (ITransition)iter.next();
			assertEquals(trans.getXMIID(), transGot.getXMIID());							
		}
		
		//Remove Input
		stateVertex.removeIncomingTransition(trans);
		transitions = stateVertex.getIncomingTransitions();
		if (transitions != null)
		{
			assertEquals(0,transitions.size());
		}
	}
	
	public void testAddOutgoingTransition()
	{
		ITransition trans = factory.createTransition(null);
		project.addElement(trans);
		
		//add and get
		stateVertex.addOutgoingTransition(trans);
		ETList<ITransition> transitions = stateVertex.getOutgoingTransitions();
		assertNotNull(transitions);
				
		Iterator iter = transitions.iterator();
		while (iter.hasNext())
		{
			ITransition transGot = (ITransition)iter.next();
			assertEquals(trans.getXMIID(), transGot.getXMIID());							
		}
		
		//Remove Input
		stateVertex.removeOutgoingTransition(trans);
		transitions = stateVertex.getOutgoingTransitions();
		if (transitions != null)
		{
			assertEquals(0,transitions.size());
		}
	}
	
	public void testSetContainer()
	{
		IRegion region = (IRegion)FactoryRetriever.instance().createType("Region", null);
		//region.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(region);
		 
		stateVertex.setContainer(region);
		IRegion regionGot = stateVertex.getContainer();
		assertNotNull(regionGot);
		assertEquals(region.getXMIID(), regionGot.getXMIID());
	}

}



