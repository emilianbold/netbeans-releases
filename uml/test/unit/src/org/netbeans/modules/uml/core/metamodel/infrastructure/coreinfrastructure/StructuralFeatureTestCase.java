package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;


/**
 * Test cases for StructuralFeature.
 */
public class StructuralFeatureTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(StructuralFeatureTestCase.class);
    }

    private IStructuralFeature feat;
    private IClassifier c;
    
    protected void setUp() throws Exception
    {
        super.setUp();
     
        c = createClass("Czar");
        IAttribute at = c.createAttribute("char", "dz");
        c.addAttribute(at);
        feat = at;
    }
    
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        feat.delete();
        c.delete();
    }

    public void testSetClientChangeability()
    {
        feat.setClientChangeability(0);
        assertEquals(0, feat.getClientChangeability());
        feat.setClientChangeability(1);
        assertEquals(1, feat.getClientChangeability());
    }
    
    public void testGetClientChangeability()
    {
        // Tested by setClientChangeability.
    }
    
    public void testSetIsTransient()
    {
        assertFalse(feat.getIsTransient());
        feat.setIsTransient(true);
        assertTrue(feat.getIsTransient());
        feat.setIsTransient(false);
        assertFalse(feat.getIsTransient());
    }
    
    public void testGetIsTransient()
    {
        // Tested by setIsTransient.
    }
    
    public void testSetIsVolatile()
    {
        assertFalse(feat.getIsVolatile());
        feat.setIsVolatile(true);
        assertTrue(feat.getIsVolatile());
        feat.setIsVolatile(false);
        assertFalse(feat.getIsVolatile());
    }
    
    public void testGetIsVolatile()
    {
        // Tested by setIsVolatile.
    }
    
//    public void testSetMultiplicity()
//    {
//        IMultiplicity mul = factory.createMultiplicity(null);
//        feat.setMultiplicity(mul);
//        
//        assertEquals(mul.getXMIID(), feat.getMultiplicity().getXMIID());
//    }
    
    public void testGetMultiplicity()
    {
        // Tested by setMultiplicity.
    }
    
    public void testSetOrdering()
    {
        feat.setOrdering(0);
        assertEquals(0, feat.getOrdering());
        feat.setOrdering(1);
        assertEquals(1, feat.getOrdering());
    }
    
    public void testGetOrdering()
    {
        // Tested by setOrdering.
    }
    
    public void testSetType2()
    {
        feat.setType2("ANSI");
        assertEquals("ANSI", feat.getTypeName());
    }
    public void testSetTypeName()
    {
        feat.setTypeName("ANSI");
        assertEquals("ANSI", feat.getTypeName());
    }
    
    public void testGetTypeName()
    {
        // Tested by setTypeName.
    }
    
    public void testSetType()
    {
        IClassifier cz = createClass("NewType");
        feat.setType(cz);
        assertEquals(cz.getXMIID(), feat.getType().getXMIID());
    }
    
    public void testGetType()
    {
        // Tested by setType.
    }
}