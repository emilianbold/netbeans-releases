package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;


/**
 * Test cases for Feature.
 */
public class FeatureTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(FeatureTestCase.class);
    }

    private IFeature    feat;
    private IClassifier clazz;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        clazz = createClass("Calliope");
        IOperation op = clazz.createOperation("int", "moo");
        clazz.addOperation(op);
        
        feat = op;
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        clazz.delete();
    }

    public void testSetFeaturingClassifier()
    {
        IClassifier featC = createClass("Feature");
        feat.setFeaturingClassifier(featC);
        assertEquals(featC.getXMIID(), 
            feat.getFeaturingClassifier().getXMIID());
    }
    
    public void testGetFeaturingClassifier()
    {
        // Tested by setFeaturingClassifier.
    }
    
    public void testSetIsStatic()
    {
        assertFalse(feat.getIsStatic());
        feat.setIsStatic(true);
        assertTrue(feat.getIsStatic());
        feat.setIsStatic(false);
        assertFalse(feat.getIsStatic());
    }
    
    public void testGetIsStatic()
    {
        // Tested by setIsStatic.
    }
    
    public void testGetQualifiedName2()
    {
        assertEquals("Calliope::moo", feat.getQualifiedName2());
    }
    
    public void testMoveToClassifier()
    {
        IClassifier other = createClass("Other");
        feat.moveToClassifier(other);
        
        assertEquals(0, clazz.getOwnedElementsByName("moo").size());
        assertEquals(1, other.getOwnedElementsByName("moo").size());
        assertEquals(feat.getXMIID(), 
            other.getOwnedElementsByName("moo").get(0).getXMIID());
        other.delete();
    }
    
    public void testDuplicateToClassifier()
    {
        IClassifier other = createClass("Other");
        feat.duplicateToClassifier(other);
        assertEquals(1, clazz.getOwnedElementsByName("moo").size());
        assertEquals(1, other.getOwnedElementsByName("moo").size());
        other.delete();
    }
    
    public void testDuplicateToClassifier2()
    {
        // ?? Why have this function at all?
        testDuplicateToClassifier();
    }
}