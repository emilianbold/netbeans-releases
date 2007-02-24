package org.netbeans.modules.uml.core.generativeframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for ExpansionResult.
 */
public class ExpansionResultTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ExpansionResultTestCase.class);
    }

    private IExpansionResult res;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        res = new ExpansionResult();
    }
    
    public void testSetPreText()
    {
        res.setPreText("PreText");
        assertEquals("PreText", res.getPreText());
    }

    public void testGetPreText()
    {
        // Tested by testSetPreText.
    }

    public void testSetVariable()
    {
        IExpansionVariable var = new ExpansionVariable();
        res.setVariable(var);
        assertEquals(var, res.getVariable());
    }

    public void testGetVariable()
    {
        // Tested by testSetVariable.
    }
}