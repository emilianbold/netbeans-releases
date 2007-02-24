package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;

import junit.textui.TestRunner;
/**
 * Test cases for Aggregation.
 */
public class AggregationTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        TestRunner.run(AggregationTestCase.class);
    }
    
    private  IClass       aggregator, part;
    private  IAggregation aggregation;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        aggregator = createClass("Aggregator");
        part       = createClass("Part");
        
        aggregation = (IAggregation) relFactory.createAssociation2(aggregator, 
            part, AssociationKindEnum.AK_AGGREGATION, false, false, project);
        assertNotNull(aggregation);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        project.removeOwnedElement(aggregator);
        project.removeOwnedElement(part);
        aggregator.delete();
        part.delete();
    }
    
    public void testSetAggregateEnd()
    {
        IAssociationEnd end = factory.createAssociationEnd(null);
        aggregation.setAggregateEnd(end);
        
        assertNotNull(aggregation.getAggregateEnd());
        assertEquals(end.getXMIID(), aggregation.getAggregateEnd().getXMIID());
    }
    
    public void testGetAggregateEnd()
    {
        assertEquals(
            aggregator.getXMIID(),
            aggregation.getAggregateEnd().getParticipant().getXMIID());
    }
    
    public void testIsAggregateEnd()
    {
        IAssociationEnd end = factory.createAssociationEnd(null);
        aggregation.setAggregateEnd(end);
        assertTrue(aggregation.isAggregateEnd(end));
    }
    
    public void testSetAggregateEnd2()
    {
        IClass newAgg = createClass("NewAggregator");
        aggregation.setAggregateEnd2(newAgg);
        
        assertNotNull(aggregation.getAggregateEnd().getParticipant());
        assertEquals(
            newAgg.getXMIID(),
            aggregation.getAggregateEnd().getParticipant().getXMIID());
    }
    
    public void testReverseEnds()
    {
        aggregation.reverseEnds();
        assertEquals(aggregator.getXMIID(), 
            aggregation.getPartEnd().getParticipant().getXMIID());
        assertEquals(part.getXMIID(),
            aggregation.getAggregateEnd().getParticipant().getXMIID());
    }
    
    public void testSetIsComposite()
    {
        aggregation.setIsComposite(true);
        assertTrue(aggregation.getIsComposite());
        aggregation.setIsComposite(false);
        assertFalse(aggregation.getIsComposite());
    }
    
    public void testGetIsComposite()
    {
        // Tested by testSetIsComposite
    }
    
    public void testGetPartEnd()
    {
        assertNotNull(aggregation.getPartEnd());
        assertEquals(
            part.getXMIID(),
            aggregation.getPartEnd().getParticipant().getXMIID());
    }
    
    public void testSetPartEnd()
    {
        IAssociationEnd end = factory.createAssociationEnd(null);
        aggregation.setPartEnd(end);
        
        assertNotNull(aggregation.getPartEnd());
        assertEquals(end.getXMIID(), aggregation.getPartEnd().getXMIID());
    }
    
    public void testSetPartEnd2()
    {
        IClass newPart = createClass("NewPart");
        aggregation.setPartEnd2(newPart);
        
        assertNotNull(aggregation.getPartEnd().getParticipant());
        assertEquals(
            newPart.getXMIID(),
            aggregation.getPartEnd().getParticipant().getXMIID());
    }
    
    public void testTransformToAssociation()
    {
        IAssociation assoc = aggregation.transformToAssociation();
        assertNotNull(assoc);
    }
}