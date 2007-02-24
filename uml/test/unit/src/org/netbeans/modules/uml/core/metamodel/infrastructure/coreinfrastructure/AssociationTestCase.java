package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 */
public class AssociationTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(AssociationTestCase.class);
    }
    
    private IAssociation assoc;
    private IClass first, second;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        first = createClass("First");
        second = createClass("Second");
        assoc = relFactory.createAssociation(first, second, project);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        project.removeElement(first);
        project.removeElement(second);
        project.removeElement(assoc);
        first.delete();
        second.delete();
        assoc.delete();
    }
    
    public void testGetAllParticipants()
    {
        ETList<IElement> parts = assoc.getAllParticipants();
        assertNotNull(parts);
        assertEquals(2, parts.size());
        assertEquals(first.getXMIID(), parts.get(0).getXMIID());
        assertEquals(second.getXMIID(), parts.get(1).getXMIID());
    }
    
    public void testRemoveEnd()
    {
        assoc.removeEnd(assoc.getEnds().get(0));
        ETList<IAssociationEnd> ends = assoc.getEnds();
        assertNotNull(ends);
        assertEquals(1, ends.size());
        assertEquals(
            second.getXMIID(),
            ends.get(0).getParticipant().getXMIID());
    }
    
    public void testAddEnd()
    {
        IAssociationEnd end = factory.createAssociationEnd(null);
        assoc.addEnd(end);
        
        ETList<IAssociationEnd> ends = assoc.getEnds();
        assertNotNull(ends);
        assertEquals(3, ends.size());
        assertEquals(end.getXMIID(), ends.get(2).getXMIID());
    }
    
    public void testAddEnd2()
    {
        IClass third = createClass("Third");
        assoc.addEnd2(third);
        ETList<IAssociationEnd> ends = assoc.getEnds();
        assertNotNull(ends);
        assertEquals(3, ends.size());
        assertEquals(
            third.getXMIID(),
            ends.get(2).getParticipant().getXMIID());
    }
    
    public void testAddEnd3()
    {
        // Note: this is no different from testAddEnd2
        
        IClass third = createClass("Third");
        assoc.addEnd3(third);
        ETList<IAssociationEnd> ends = assoc.getEnds();
        assertNotNull(ends);
        assertEquals(3, ends.size());
        assertEquals(
            third.getXMIID(),
            ends.get(2).getParticipant().getXMIID());
    }
    
    public void testGetEndIndex()
    {
        ETList<IAssociationEnd> ends = assoc.getEnds();
        assertNotNull(ends);
        assertEquals(2, ends.size());
        assertEquals(0, assoc.getEndIndex(ends.get(0)));
        assertEquals(1, assoc.getEndIndex(ends.get(1)));
    }
    
    public void testGetEnds()
    {
        // Tested by most methods upstairs.
    }
    

    public void testSetIsDerived()
    {
        assoc.setIsDerived(true);
        assertTrue(assoc.getIsDerived());
        assoc.setIsDerived(false);
        assertFalse(assoc.getIsDerived());
    }
    
    public void testGetIsDerived()
    {
        // Tested by testSetIsDerived
    }
    
    public void testGetIsReflexive()
    {
        assertFalse(assoc.getIsReflexive());
        // Point association back at first class.
        assoc.getEnds().get(1).setParticipant(first);
        assertEquals(
            first.getXMIID(),
            assoc.getEnds().get(0).getParticipant().getXMIID());
        assertTrue(assoc.getIsReflexive());
    }
    
    public void testGetNumEnds()
    {
        assertEquals(2, assoc.getNumEnds());
        assertEquals(assoc.getEnds().size(), assoc.getNumEnds());
    }
    
    public void testTransformToAggregation()
    {
        assertNotNull(assoc.transformToAggregation(false));
    }
}