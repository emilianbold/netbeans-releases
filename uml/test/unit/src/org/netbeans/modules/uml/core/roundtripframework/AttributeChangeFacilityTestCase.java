package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for AttributeChangeFacility.
 */
public class AttributeChangeFacilityTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(AttributeChangeFacilityTestCase.class);
    }
    
    private IAttributeChangeFacility fac;
    private IClass                   c;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        fac = (IAttributeChangeFacility) product.getFacilityManager()
        .retrieveFacility("RoundTrip.JavaAttributeChangeFacility");
        c = createClass("Cthulhu");
    }
    
    public void testAddAttribute2()
    {
        fac.addAttribute2("a", "int", c, true, false);
        assertEquals("a", c.getAttributeByName("a").getName());
    }
}
