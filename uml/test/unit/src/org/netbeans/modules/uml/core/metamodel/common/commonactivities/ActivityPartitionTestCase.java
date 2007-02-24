package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;/**
 * Test cases for ActivityPartition.
 */
public class ActivityPartitionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ActivityPartitionTestCase.class);
    }

    private IActivityPartition activityPartition;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
		activityPartition = (IActivityPartition)FactoryRetriever.instance().createType("ActivityPartition", null);
		//activityPartition.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(activityPartition);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        //activityPartition.delete();
    }

    
    public void testSetActivity()
    {
		IActivity activity = factory.createActivity(null);
		project.addElement(activity);
		
		activityPartition.setActivity(activity);
		IActivity activityGot = activityPartition.getActivity();
		assertNotNull(activityGot);
		assertEquals(activity.getXMIID(), activityGot.getXMIID());
    }

    public void testSetIsDimension()
    {
		activityPartition.setIsDimension(true);
		assertTrue(activityPartition.getIsDimension());
    }

    public void testSetIsExternal()
    {
		activityPartition.setIsExternal(true);
		assertTrue(activityPartition.getIsExternal());
    }


    public void testSetRepresents()
    {
		IActivity activity = factory.createActivity(null);
        project.addElement(activity);
		activityPartition.setRepresents(activity);
		IElement elemGot = activityPartition.getRepresents();
		assertEquals(activity.getXMIID(), elemGot.getXMIID());
    }


    public void testAddSubPartition()
    {
		IActivityPartition partition = (IActivityPartition)FactoryRetriever.instance().createType("ActivityPartition", null);
		//partition.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(partition);
		
		activityPartition.addSubPartition(partition);
		ETList<IActivityPartition> partitions = activityPartition.getSubPartitions();
		assertNotNull(partitions);
				
		Iterator iter = partitions.iterator();
		while (iter.hasNext())
		{
			IActivityPartition partitionGot = (IActivityPartition)iter.next();
			assertEquals(partition.getXMIID(), partitionGot.getXMIID());							
		}
		
		//Remove Input
		activityPartition.removeSubPartition(partition);
		partitions = activityPartition.getSubPartitions();
		if (partitions != null)
		{
			assertEquals(0,partitions.size());
		}
    } 
}
