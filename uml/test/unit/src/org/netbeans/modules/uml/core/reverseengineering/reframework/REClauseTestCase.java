package org.netbeans.modules.uml.core.reverseengineering.reframework;


/**
 * Test cases for REClause.
 */
public class REClauseTestCase extends AbstractRETestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REClauseTestCase.class);
    }

    private REClause rec;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        rec = new REClause();
        createBaseElement(rec, "UML:Clause");
        attr("isDeterminate", "true");
    }
    
    public void testGetIsDeterminate()
    {
        assertTrue(rec.getIsDeterminate());
    }
}
