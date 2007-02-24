package org.netbeans.modules.uml.core.reverseengineering.reframework;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for InitializeEvent.
 */
public class InitializeEventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(InitializeEventTestCase.class);
    }

    private IInitializeEvent ie;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        ie = new InitializeEvent();
    }
    
    public void testSetStringRepresentation()
    {
        ie.setStringRepresentation("ASCII");
        assertEquals("ASCII", ie.getStringRepresentation());
    }
}
