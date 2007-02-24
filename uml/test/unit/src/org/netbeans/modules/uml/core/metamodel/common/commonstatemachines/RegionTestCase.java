
/*
 * Created on Sep 25, 2003
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
public class RegionTestCase extends AbstractUMLTestCase
{
	private IRegion region = null;
	
	public RegionTestCase()
	{
		super();		
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(RegionTestCase.class);
	}
	
	protected void setUp()
	{
		region = (IRegion)FactoryRetriever.instance().createType("Region", null);
		//region.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(region); 
	}
	
	public void testAddTransition()
	{
		ITransition trans = factory.createTransition(null);
		project.addElement(trans);
		
		//add and get
		region.addTransition(trans);
		ETList<ITransition> transitions = region.getTransitions();
		assertNotNull(transitions);
				
		Iterator iter = transitions.iterator();
		while (iter.hasNext())
		{
			ITransition transGot = (ITransition)iter.next();
			assertEquals(trans.getXMIID(), transGot.getXMIID());							
		}
		
		//Remove Input
		region.removeTransition(trans);
		transitions = region.getTransitions();
		if (transitions != null)
		{
			assertEquals(0,transitions.size());
		}
	}
	
	public void testAddSubVertex()
	{
		IStateVertex vertex = (IStateVertex)FactoryRetriever.instance().createType("StateVertex", null);
		//vertex.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(vertex); 
		
		//add and get
		region.addSubVertex(vertex);
		ETList<IStateVertex> vertexes = region.getSubVertexes();
		assertNotNull(vertexes);
				
		Iterator iter = vertexes.iterator();
		while (iter.hasNext())
		{
			IStateVertex vertexGot = (IStateVertex)iter.next();
			assertEquals(vertex.getXMIID(), vertexGot.getXMIID());							
		}
		
		//Remove Input
		region.removeSubVertex(vertex);
		vertexes = region.getSubVertexes();
		if (vertexes != null)
		{
			assertEquals(0,vertexes.size());
		}
	}
}



