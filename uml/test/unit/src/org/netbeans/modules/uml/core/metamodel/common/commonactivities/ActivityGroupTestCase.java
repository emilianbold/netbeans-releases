package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for ActivityGroup.
 */
public class ActivityGroupTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ActivityGroupTestCase.class);
    }

    private IActivityGroup activityGroup;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
  
		activityGroup = (IActivityGroup)FactoryRetriever.instance().createType("ActivityPartition", null);
		//activityGroup.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(activityGroup);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        //activityGroup.delete();
    }

    
    public void testSetActivity()
    {
		IActivity activity = factory.createActivity(null);
		project.addElement(activity);
		
		activityGroup.setActivity(activity);
		IActivity activityGot = activityGroup.getActivity();
		assertNotNull(activityGot);
		assertEquals(activity.getXMIID(), activityGot.getXMIID()); 
    }

    public void testAddEdgeContent()
    {
		IActivityEdge edge = (IActivityEdge)FactoryRetriever.instance().createType("ControlFlow", null);
		//edge.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(edge);
		
		activityGroup.addEdgeContent(edge);
		ETList<IActivityEdge> edges = activityGroup.getEdgeContents();
		assertNotNull(edges);
				
		Iterator iter = edges.iterator();
		while (iter.hasNext())
		{
			IActivityEdge edgeGot = (IActivityEdge)iter.next();
			assertEquals(edge.getXMIID(), edgeGot.getXMIID());							
		}
		
		//Remove Input
		activityGroup.removeEdgeContent(edge);
		edges = activityGroup.getEdgeContents();
		if (edges != null)
		{
			assertEquals(0,edges.size());
		}
    }

    public void testAddNodeContent()
    {
		IActivityNode node = (IActivityNode)FactoryRetriever.instance().createType("InvocationNode", null);
		//node.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(node);
		
		activityGroup.addNodeContent(node);
		ETList<IActivityNode> nodes = activityGroup.getNodeContents();
		assertNotNull(nodes);
				
		Iterator iter = nodes.iterator();
		while (iter.hasNext())
		{
			IActivityNode nodeGot = (IActivityNode)iter.next();
			assertEquals(node.getXMIID(), nodeGot.getXMIID());							
		}
		
		//Remove
		activityGroup.removeNodeContent(node);
		nodes = activityGroup.getNodeContents();
		if (nodes != null)
		{
			assertEquals(0,nodes.size());
		}
		
		
    }

    public void testAddSubGroup()
    {
		IActivityGroup group = (IActivityGroup)FactoryRetriever.instance().createType("ActivityPartition", null);
		//group.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(group);
		
		activityGroup.addSubGroup(group);
		ETList<IActivityGroup> groups = activityGroup.getSubGroups();
		assertNotNull(groups);
				
		Iterator iter = groups.iterator();
		while (iter.hasNext())
		{
			IActivityGroup groupGot = (IActivityGroup)iter.next();
			assertEquals(group.getXMIID(), groupGot.getXMIID());							
		}
		
		//Remove
		activityGroup.removeSubGroup(group);
		groups = activityGroup.getSubGroups();
		if (groups != null)
		{
			assertEquals(0,groups.size());
		}
		
    }
}
