package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;/**
 * Test cases for ActivityNode.
 */
public class ActivityNodeTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ActivityNodeTestCase.class);
    }

    private IActivityNode activityNode;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
		activityNode = (IActivityNode)FactoryRetriever.instance().createType("InvocationNode", null);
		//activityNode.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(activityNode);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        //activityNode.delete();
    }

    
    public void testSetActivity()
    {
		IActivity activity = factory.createActivity(null);
		project.addElement(activity);
				
		activityNode.setActivity(activity);
		IActivity activityGot = activityNode.getActivity();
		assertNotNull(activityGot);
		assertEquals(activity.getXMIID(), activityGot.getXMIID()); 
    }

    public void testAddGroup()
    {
		IActivityGroup group = (IActivityGroup)FactoryRetriever.instance().createType("ActivityPartition", null);
		//group.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(group);
		
		activityNode.addGroup(group);
		ETList<IActivityGroup> groups = activityNode.getGroups();
		assertNotNull(groups);
				
		Iterator iter = groups.iterator();
		while (iter.hasNext())
		{
			IActivityGroup groupGot = (IActivityGroup)iter.next();
			assertEquals(group.getXMIID(), groupGot.getXMIID());							
		}
		
		//Remove Input
		activityNode.removeGroup(group);
		groups = activityNode.getGroups();
		if (groups != null)
		{
			assertEquals(0,groups.size());
		}
    }

    public void testAddIncomingEdge()
    {
		IActivityEdge edge = (IActivityEdge)FactoryRetriever.instance().createType("ControlFlow", null);
		//edge.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(edge);
		
		activityNode.addIncomingEdge(edge);
		ETList<IActivityEdge> edges = activityNode.getIncomingEdges();
		assertNotNull(edges);
				
		Iterator iter = edges.iterator();
		while (iter.hasNext())
		{
			IActivityEdge edgeGot = (IActivityEdge)iter.next();
			assertEquals(edge.getXMIID(), edgeGot.getXMIID());							
		}
		
		//Remove
		activityNode.removeIncomingEdge(edge);
		edges = activityNode.getIncomingEdges();
		if (edges != null)
		{
			assertEquals(0,edges.size());
		}
		
    }

    public void testAddOutgoingEdge()
    {
		IActivityEdge edge = (IActivityEdge)FactoryRetriever.instance().createType("ControlFlow", null);
		//edge.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(edge);
		
		activityNode.addOutgoingEdge(edge);
		ETList<IActivityEdge> edges = activityNode.getOutgoingEdges();
		assertNotNull(edges);
				
		Iterator iter = edges.iterator();
		while (iter.hasNext())
		{
			IActivityEdge edgeGot = (IActivityEdge)iter.next();
			assertEquals(edge.getXMIID(), edgeGot.getXMIID());							
		}
		
		//Remove
		activityNode.removeOutgoingEdge(edge);
		edges = activityNode.getOutgoingEdges();
		if (edges != null)
		{
			assertEquals(0,edges.size());
		}
		
    }
}
