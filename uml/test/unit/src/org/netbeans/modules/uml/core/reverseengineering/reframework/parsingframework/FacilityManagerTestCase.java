package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;

/**
 * Test cases for FacilityManager.
 */
public class FacilityManagerTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(FacilityManagerTestCase.class);
    }

    private IFacilityManager fm = product.getFacilityManager();

    public void testRetrieveFacility()
    {
        assertNotNull(fm.retrieveFacility("Parsing.UMLParser"));
    }

    public void testGetFacilityNames()
    {
        IStrings s = fm.getFacilityNames();
        assertNotNull(s);
        assertTrue(s.size() > 0);
    }
}
