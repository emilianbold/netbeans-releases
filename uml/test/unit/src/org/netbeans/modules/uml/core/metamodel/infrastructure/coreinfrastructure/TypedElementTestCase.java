package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;

/**
 * Test cases for TypedElement.
 */
public class TypedElementTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TypedElementTestCase.class);
    }
    
    private ITypedElement tyel;
    private IClassifier   c;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        c = createClass("Velociraptor");
        IAttribute at = c.createAttribute("int", "b");
        c.addAttribute(at);
        
        tyel = at;
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        tyel.delete();
        c.delete();
    }
    
    public void testPerformDuplication()
    {
        IVersionableElement ve = tyel.performDuplication();
        assertNotNull(ve);
        assertTrue(ve instanceof ITypedElement);
    }
    
    public void testSetIsSet()
    {
        assertFalse(tyel.getIsSet());
        tyel.setIsSet(true);
        assertTrue(tyel.getIsSet());
        tyel.setIsSet(false);
        assertFalse(tyel.getIsSet());
    }
    
    public void testGetIsSet()
    {
        // Tested by setIsSet.
    }
    
//    public void testSetMultiplicity()
//    {
//        IMultiplicity mul = factory.createMultiplicity(null);
//        tyel.setMultiplicity(mul);
//        assertEquals(mul.getXMIID(), tyel.getMultiplicity().getXMIID());
//    }
    
    public void testGetMultiplicity()
    {
        // Tested by setMultiplicity.
    }
    
    public void testSetOrdering()
    {
        tyel.setOrdering(0);
        assertEquals(0, tyel.getOrdering());
        tyel.setOrdering(1);
        assertEquals(1, tyel.getOrdering());
    }
    
    public void testGetOrdering()
    {
        // Tested by setOrdering.
    }

    public void testSetType()
    {
        IClassifier t = createClass("NewType");
        tyel.setType(t);
        assertEquals(t.getXMIID(), tyel.getType().getXMIID());
        assertEquals(t.getXMIID(), tyel.getTypeID());
    }
    
    public void testGetTypeID()
    {
        // Tested by setType.
    }
    
    public void testGetType()
    {
        // Tested by setType.
    }
}