package org.netbeans.modules.uml.core.metamodel.core.constructs;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for EnumerationLiteral.
 */
public class EnumerationLiteralTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(EnumerationLiteralTestCase.class);
    }

    private IEnumerationLiteral enumerationLiteral;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        enumerationLiteral = factory.createEnumerationLiteral(null);
        project.addElement(enumerationLiteral);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        enumerationLiteral.delete();
    }

    
    public void testSetEnumeration()
    {
        IEnumeration enumeration = factory.createEnumeration(null);
        project.addElement(enumeration);
        enumerationLiteral.setEnumeration(enumeration);
        assertEquals(enumeration.getXMIID(), enumerationLiteral.getEnumeration().getXMIID());
    }

    public void testGetEnumeration()
    {
        // Tested by testSetEnumeration.
    }
}