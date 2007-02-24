package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for ActivityEdge.
 */
public class ActivityEdgeTestCase extends AbstractUMLTestCase
{
	private IActivityEdge edge = null;
	
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ActivityEdgeTestCase.class);
    }

    //private IActivityEdge activityEdge;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
		edge = (IActivityEdge)FactoryRetriever.instance().createType("ControlFlow", null);
		//edge.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(edge);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    
    public void testSetActivity()
    {
		IActivity activity = factory.createActivity(null);
		project.addElement(activity);
		
		edge.setActivity(activity);
		IActivity activityGot = edge.getActivity();	
		assertNotNull(activityGot);	
		assertEquals(activity.getXMIID(), activityGot.getXMIID()); 
    }

    public void testAddGroup()
    {
		IActivityGroup group = (IActivityGroup)FactoryRetriever.instance().createType("ActivityPartition", null);
		//group.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(group);
		
		edge.addGroup(group);
		ETList<IActivityGroup> groups = edge.getGroups();
		assertNotNull(groups);
				
		Iterator iter = groups.iterator();
		while (iter.hasNext())
		{
			IActivityGroup groupGot = (IActivityGroup)iter.next();
			assertEquals(group.getXMIID(), groupGot.getXMIID());							
		}
		
		//Remove Input
		edge.removeGroup(group);
		groups = edge.getGroups();
		if (groups != null)
		{
			assertEquals(0,groups.size());
		}
    }

    public void testSetGuard()
    {
		IValueSpecification vSpec = factory.createExpression(null);
		project.addElement(vSpec);
		
        edge.setGuard(vSpec);
		IValueSpecification vSpecGot = edge.getGuard();
		assertEquals(vSpec.getXMIID(), vSpecGot.getXMIID());
    }

    public void testSetSource()
    {
		IActivityNode node = (IActivityNode)FactoryRetriever.instance().createType("InvocationNode", null);
		//node.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(node);
		
		edge.setSource(node);
		IActivityNode nodeGot = edge.getSource();
		assertEquals(node.getXMIID(), nodeGot.getXMIID());
    }

    public void testSetTarget()
    {
		IActivityNode node = (IActivityNode)FactoryRetriever.instance().createType("InvocationNode", null);
		//node.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(node);
		
		edge.setTarget(node);
		IActivityNode nodeGot = edge.getTarget();
		assertNotNull(nodeGot);
		assertEquals(node.getXMIID(), nodeGot.getXMIID());
    }

    public void testSetWeight()
    {
		IValueSpecification weight = factory.createExpression(null);
		project.addElement(weight);
		
		edge.setWeight(weight);
		IValueSpecification weightGot = edge.getWeight();
		assertEquals(weight.getXMIID(), weightGot.getXMIID());
    } 
}
