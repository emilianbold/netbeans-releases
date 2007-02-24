package org.netbeans.modules.uml.core.reverseengineering.reframework;


/**
 * Test cases for RECriticalSection.
 */
public class RECriticalSectionTestCase extends AbstractRETestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(RECriticalSectionTestCase.class);
    }

    private RECriticalSection recs;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        recs = new RECriticalSection();
        createBaseElement(recs, "UML:CriticalSection");
        attr("representation", "Inigo");
    }

    public void testGetStringRepresentation()
    {
        assertEquals("Inigo", recs.getStringRepresentation());
    }
}