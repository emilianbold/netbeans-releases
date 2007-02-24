package org.netbeans.modules.uml.core.metamodel.infrastructure;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;

/**
 * Test cases for Part.
 */
public class PartTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(PartTestCase.class);
    }

    private IPart part;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        part = factory.createPart(null);
        project.addElement(part);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        part.delete();
    }
    
    public void testSetDefiningFeature()
    {
        IClass cl = createClass("D");
        IAttribute at = cl.createAttribute("int", "k");
        cl.addAttribute(at);

        part.setDefiningFeature(at);
        assertEquals(at.getXMIID(), part.getDefiningFeature().getXMIID());
    }

    public void testGetDefiningFeature()
    {
        // Tested by testSetDefiningFeature.
    }

    public void testSetInitialCardinality()
    {
        part.setInitialCardinality(10);
        assertEquals(10, part.getInitialCardinality());
        part.setInitialCardinality(1);
        assertEquals(1, part.getInitialCardinality());
    }

    public void testGetInitialCardinality()
    {
        // Tested by testSetInitialCardinality.
    }

    public void testSetIsWhole()
    {
        assertFalse(part.getIsWhole());
        part.setIsWhole(true);
        assertTrue(part.getIsWhole());
        part.setIsWhole(false);
        assertFalse(part.getIsWhole());
    }

    public void testGetIsWhole()
    {
        // Tested by testSetIsWhole.
    }

    public void testSetPartKind()
    {
        part.setPartKind(BaseElement.PK_AUXILIARY);
        assertEquals(BaseElement.PK_AUXILIARY, part.getPartKind());
        part.setPartKind(BaseElement.PK_INTERFACEIMPLEMENTATION);
        assertEquals(BaseElement.PK_INTERFACEIMPLEMENTATION, part.getPartKind());
    }

    public void testGetPartKind()
    {
        // Tested by testSetPartKind.
    }
}