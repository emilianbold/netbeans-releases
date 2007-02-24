package org.netbeans.modules.uml.core.metamodel.infrastructure;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;

/**
 * Test cases for Collaboration.
 */
public class CollaborationTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(CollaborationTestCase.class);
    }

    private ICollaboration coll;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        coll = factory.createCollaboration(null);
        project.addElement(coll);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        coll.delete();
    }
    
    public void testAddConstrainingElement()
    {
        IClassifier cons = createClass("Constrain");
        coll.addConstrainingElement(cons);
        assertEquals(1, coll.getConstrainingElements().size());
        assertEquals(cons.getXMIID(), 
            coll.getConstrainingElements().get(0).getXMIID());
    }
    
    public void testRemoveConstrainingElement()
    {
        testAddConstrainingElement();
        coll.removeConstrainingElement(coll.getConstrainingElements().get(0));
        assertEquals(0, coll.getConstrainingElements().size());
    }
    
    public void testGetConstrainingElements()
    {
        // Tested by testAddConstrainingElement.
    }
    
    public void testGetExpandedElementType()
    {
        assertEquals("Collaboration", coll.getExpandedElementType());
    }

    public void testAddNestedClassifier()
    {
        IClassifier nestable = createClass("Nestable");
        coll.addNestedClassifier(nestable);
        assertEquals(1, coll.getNestedClassifiers().size());
        assertEquals(nestable.getXMIID(), 
            coll.getNestedClassifiers().get(0).getXMIID());
    }
    
    public void testRemoveNestedClassifier()
    {
        testAddNestedClassifier();
        coll.removeNestedClassifier(coll.getNestedClassifiers().get(0));
        assertEquals(0, coll.getNestedClassifiers().size());
    }
    
    public void testGetNestedClassifiers()
    {
        // Tested by testAddNestedClassifier.
    }
}