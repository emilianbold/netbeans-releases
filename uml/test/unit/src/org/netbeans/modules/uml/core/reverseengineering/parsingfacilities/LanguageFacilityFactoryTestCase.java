package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for LanguageFacilityFactory.
 */
public class LanguageFacilityFactoryTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(LanguageFacilityFactoryTestCase.class);
    }
    
    private ILanguageFacilityFactory lff;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        lff = new LanguageFacilityFactory();
    }
    
    public void testRetrieveFacility()
    {
        assertNotNull(lff.retrieveFacility("Parser.UMLParser"));
    }
}
