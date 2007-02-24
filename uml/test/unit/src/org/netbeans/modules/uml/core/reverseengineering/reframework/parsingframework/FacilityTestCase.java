package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for Facility.
 */
public class FacilityTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(FacilityTestCase.class);
    }

    private IFacility f = 
            product.getFacilityManager().retrieveFacility("Parsing.UMLParser");

    public void testAddProperty()
    {
        f.addProperty("x", "y");
        assertEquals("y", f.getPropertyValue("x"));
        
        IFacilityProperty fac = new FacilityProperty();
        fac.setName("z");
        fac.setValue("a");
        f.addProperty(fac);
        assertEquals("a", f.getProperty("z").getValue());
        
        assertEquals("y", f.getProperty("x").getValue());
    }
}
