package org.netbeans.modules.uml.core.metamodel.core.constructs;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for Enumeration.
 */
public class EnumerationTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(EnumerationTestCase.class);
    }

    private IEnumeration enumeration;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        enumeration = factory.createEnumeration(null);
        project.addElement(enumeration);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        enumeration.delete();
    }

    public void testRemoveLiteral()
    {
        testAddLiteral();
        enumeration.removeLiteral(enumeration.getLiterals().get(0));
        assertEquals(0, enumeration.getLiterals().size());
    }

    public void testAddLiteral()
    {
        IEnumerationLiteral lit = factory.createEnumerationLiteral(null);
        project.addElement(lit);
        enumeration.addLiteral(lit);
        assertEquals(1, enumeration.getLiterals().size());
        assertEquals(lit.getXMIID(), enumeration.getLiterals().get(0).getXMIID());
    }

    public void testGetLiterals()
    {
        // Tested by testAddLiteral.
    }
}