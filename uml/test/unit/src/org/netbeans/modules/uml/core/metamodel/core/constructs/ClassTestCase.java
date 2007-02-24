package org.netbeans.modules.uml.core.metamodel.core.constructs;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IReception;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for Class.
 */
public class ClassTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ClassTestCase.class);
    }

    private IClass clazz;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        clazz = createClass("Indiana");
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        clazz.delete();
    }

    
    public void testSetIsActive()
    {
        clazz.setIsActive(true);
        assertTrue(clazz.getIsActive());
        clazz.setIsActive(false);
        assertFalse(clazz.getIsActive());
    }

    public void testGetIsActive()
    {
        // Tested by testSetIsActive.
    }

    public void testSetIsStruct()
    {
        clazz.setIsStruct(true);
        assertTrue(clazz.getIsStruct());
        clazz.setIsStruct(false);
        assertFalse(clazz.getIsStruct());
    }

    public void testGetIsStruct()
    {
        // Tested by testSetIsStruct.
    }

    public void testSetIsUnion()
    {
        clazz.setIsUnion(true);
        assertTrue(clazz.getIsUnion());
        clazz.setIsUnion(false);
        assertFalse(clazz.getIsUnion());
    }

    public void testGetIsUnion()
    {
        // Tested by testSetIsUnion.
    }

    public void testAddReception()
    {
        IReception rec = factory.createReception(null);
        project.addElement(rec);
        clazz.addReception(rec);

        assertEquals(1, clazz.getReceptions().size());
        assertEquals(rec.getXMIID(), clazz.getReceptions().get(0).getXMIID());
    }

    public void testRemoveReception()
    {
        testAddReception();
        clazz.removeReception(clazz.getReceptions().get(0));
        assertEquals(0, clazz.getReceptions().size());
    }

    public void testGetReceptions()
    {
        // Tested by testAddReception.
    }
}