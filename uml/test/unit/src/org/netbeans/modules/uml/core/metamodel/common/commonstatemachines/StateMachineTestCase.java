
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
public class StateMachineTestCase extends AbstractUMLTestCase
{ 
	private IStateMachine stateMachine = null;
	public StateMachineTestCase()
	{
		super();
	}

	public static void main(String args[])
	{
		junit.textui.TestRunner.run(StateMachineTestCase.class);
	}
	
	protected void setUp()
	{
		stateMachine = factory.createStateMachine(null);
		project.addElement(stateMachine);
	}
	
	public void testAddConformance()
	{
		IProtocolConformance protConf = factory.createProtocolConformance(null);
		project.addElement(protConf);
		
		stateMachine.addConformance(protConf);
		ETList<IProtocolConformance> confs = stateMachine.getConformances();
		assertNotNull(confs);
		
		Iterator iter = confs.iterator();
		while (iter.hasNext())
		{
			IProtocolConformance protConfGot = (IProtocolConformance)iter.next();
			assertEquals(protConf.getXMIID(), protConfGot.getXMIID());							
		}
		
		//Remove Input
		stateMachine.removeConformance(protConf);
		confs = stateMachine.getConformances();
		if (confs != null)
		{
			assertEquals(0,confs.size());
		} 
	}
	
	public void testAddRegion()
	{
		IRegion region = (IRegion)FactoryRetriever.instance().createType("Region", null);
		//region.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(region);
		
		stateMachine.addRegion(region);
		ETList<IRegion> regions = stateMachine.getRegions();
		assertNotNull(regions);

        assertEquals(region.getXMIID(), regions.get(1).getXMIID());
		
		//Remove Input
		stateMachine.removeRegion(region);
		assertEquals(1,stateMachine.getRegions().size());
	}
	
	public void testAddConnectionPoint()
	{
		IUMLConnectionPoint connectionPoint = (IUMLConnectionPoint)FactoryRetriever.instance().createType("ConnectionPoint", null);
		//connectionPoint.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(connectionPoint);
		
		stateMachine.addConnectionPoint(connectionPoint);
		ETList<IUMLConnectionPoint> connectionPoints = stateMachine.getConnectionPoints();
		assertNotNull(connectionPoints);
		
		Iterator iter = connectionPoints.iterator();
		while (iter.hasNext())
		{
			IUMLConnectionPoint connectionPointGot = (IUMLConnectionPoint)iter.next();
			assertEquals(connectionPoint.getXMIID(), connectionPointGot.getXMIID());							
		}
		
		//Remove Input
		stateMachine.removeConnectionPoint(connectionPoint);
		connectionPoints = stateMachine.getConnectionPoints();
		if (connectionPoints != null)
		{
			assertEquals(0,connectionPoints.size());
		}
	}
	
	public void testAddSubmachineState()
	{
		IState state = factory.createState(null);		
		project.addElement(state);
		
		stateMachine.addSubmachineState(state);
		ETList<IState> states = stateMachine.getSubmachinesStates();
		assertNotNull(states);
		
		Iterator iter = states.iterator();
		while (iter.hasNext())
		{
			IState stateGot = (IState)iter.next();
			assertEquals(state.getXMIID(), stateGot.getXMIID());							
		}
		
		//Remove Input
		stateMachine.removeSubmachineState(state);
		states = stateMachine.getSubmachinesStates();
		if (states != null)
		{
			assertEquals(0,states.size());
		}
	}
}



