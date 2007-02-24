package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for AssociationEnd.
 */
public class AssociationEndTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(AssociationEndTestCase.class);
    }

    private IAssociation assoc;
    private IClass first, second;
    private IAssociationEnd end;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        first = createClass("First");
        second = createClass("Second");
        assoc = relFactory.createAssociation(first, second, project);

        end = assoc.getEnds().get(0);
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
        end = null;
    }
    
    public void testGetAssociation()
    {
        assertNotNull(end.getAssociation());
        assertEquals(assoc.getXMIID(), end.getAssociation().getXMIID());
    }
    
    public void testSetAssociation()
    {
        IClass third = createClass("Third");
        IClass fourth = createClass("Fourth");
        IAssociation assoc2 =
            relFactory.createAssociation(third, fourth, project);
        end.setAssociation(assoc2);
        
        assertNotNull(end.getAssociation());
        assertEquals(assoc2.getXMIID(), end.getAssociation().getXMIID());
    }
    
    public void testGetIsNavigable()
    {
        assertFalse(end.getIsNavigable());
        end = end.makeNavigable();
        assertTrue(end.getIsNavigable());
    }
    
    public void testMakeNavigable()
    {
        // Tested by testGetIsNavigable
    }
    
    public void testGetOtherEnd()
    {
        ETList<IAssociationEnd> other = end.getOtherEnd();
        assertNotNull(other);
        assertEquals(1, other.size());
        assertEquals(assoc.getEnds().get(1).getXMIID(), other.get(0).getXMIID());
    }
    
    public void testGetOtherEnd2()
    {
        IAssociationEnd other = end.getOtherEnd2();
        assertNotNull(other);
        assertEquals(assoc.getEnds().get(1).getXMIID(), other.getXMIID());
    }
    
    public void testSetParticipant()
    {
        IClass jefferson = createClass("Jefferson");
        end.setParticipant(jefferson);
        assertNotNull(end.getParticipant());
        assertEquals(jefferson.getXMIID(), end.getParticipant().getXMIID());
    }
    
    public void testGetParticipant()
    {
        // Tested by testSetParticipant
    }
    
    public void testCreateQualifier()
    {
        IAttribute qualifier = end.createQualifier("float", "bizarre");
        assertNotNull(qualifier);
        assertEquals("bizarre", qualifier.getName());
    }
    
    public void testAddQualifier()
    {
        IAttribute qualifier = end.createQualifier("float", "bizarre");
        end.addQualifier(qualifier);
        
        ETList<IAttribute> quals = end.getQualifiers();
        assertNotNull(quals);
        assertEquals(1, quals.size());
        assertEquals(qualifier.getXMIID(), quals.get(0).getXMIID());
    }
    
    public void testRemoveQualifier()
    {
        testAddQualifier();
        end.removeQualifier(end.getQualifiers().get(0));
        assertTrue(
            end.getQualifiers() == null || end.getQualifiers().size() == 0);
    }
    
    public void testCreateQualifier2()
    {
        IClass ike;
        IAttribute qualifier = end.createQualifier2(ike = createClass("Ike"), 
                                                    "p");
        assertNotNull(qualifier);
        end.addQualifier(qualifier);
        assertEquals("p", qualifier.getName());
        assertEquals(ike.getXMIID(), qualifier.getType().getXMIID());
    }
    
    public void testCreateQualifier3()
    {
        IAttribute qual = end.createQualifier3();
        assertNotNull(qual);
    }

    public void testGetQualifiers()
    {
        // Tested by preceding qualifier methods.
    }
    
    
    public void testIsSameParticipant()
    {
        assertTrue(end.isSameParticipant(first));
        assertFalse(end.isSameParticipant(second));
    }
}