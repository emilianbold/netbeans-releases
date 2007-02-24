
/*
 * Created on Sep 29, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author aztec
 *
 */
public class ActivityTestCase extends AbstractUMLTestCase
{
	private IActivity activity = null; 

	protected void setUp()
	{
		activity = factory.createActivity(null); 
        project.addElement(activity);
	}
	
	public void testAddEdge()
	{
		IActivityEdge edge = (IActivityEdge)FactoryRetriever.instance().createType("ControlFlow", null);
		//edge.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(edge);
		
		activity.addEdge(edge);
		ETList<IActivityEdge> edges = activity.getEdges();
		assertNotNull(edges);
				
		Iterator iter = edges.iterator();
		while (iter.hasNext())
		{
			IActivityEdge edgeGot = (IActivityEdge)iter.next();
			assertEquals(edge.getXMIID(), edgeGot.getXMIID());							
		}
		
		//Remove Input
		activity.removeEdge(edge);
		edges = activity.getEdges();
		if (edges != null)
		{
			assertEquals(0,edges.size());
		}
	}
	
	public void testAddGroup()
	{
		IActivityGroup group = (IActivityGroup)FactoryRetriever.instance().createType("ActivityPartition", null);
		//group.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(group);
		
		activity.addGroup(group);
		ETList<IActivityGroup> groups = activity.getGroups();
		assertNotNull(groups);
				
		Iterator iter = groups.iterator();
		while (iter.hasNext())
		{
			IActivityGroup groupGot = (IActivityGroup)iter.next();
			assertEquals(group.getXMIID(), groupGot.getXMIID());							
		}
		
		//Remove Input
		activity.removeGroup(group);
		groups = activity.getGroups();
		if (groups != null)
		{
			assertEquals(0,groups.size());
		}
	}
	
	public void testAddNode()
	{
		IActivityNode node = (IActivityNode)FactoryRetriever.instance().createType("InvocationNode", null);
		//node.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(node);
    
		activity.addNode(node);
		ETList<IActivityNode> nodes = activity.getNodes();
		assertNotNull(nodes);
				
		Iterator iter = nodes.iterator();
		while (iter.hasNext())
		{
			IActivityNode nodeGot = (IActivityNode)iter.next();
			assertEquals(node.getXMIID(), nodeGot.getXMIID());							
		}
		
		//Remove Input
		activity.removeNode(node);
		nodes = activity.getNodes();
		if (nodes != null)
		{
			assertEquals(0,nodes.size());
		}
	}
	
	public void testAddPartition()
	{
		IActivityPartition partition = (IActivityPartition)FactoryRetriever.instance().createType("ActivityPartition", null);
		//partition.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(partition);
    
		activity.addPartition(partition);
		ETList<IActivityPartition> partitions = activity.getPartitions();
		assertNotNull(partitions);
				
		Iterator iter = partitions.iterator();
		while (iter.hasNext())
		{
			IActivityPartition partitionGot = (IActivityPartition)iter.next();
			assertEquals(partition.getXMIID(), partitionGot.getXMIID());							
		}
		
		//Remove Input
		activity.removePartition(partition);
		partitions = activity.getPartitions();
		if (partitions != null)
		{
			assertEquals(0,partitions.size());
		}
	}
	
	public void testSetIsSingleCopy()
	{
		activity.setIsSingleCopy(true);
		assertTrue(activity.getIsSingleCopy());
	}
	
	public void testSetKind()
	{
		activity.setKind(1);
		assertEquals(1,activity.getKind());
	}
	
	public static void main(String args[])
	{
		junit.textui.TestRunner.run(ActivityTestCase.class);
	}
	
	
}



