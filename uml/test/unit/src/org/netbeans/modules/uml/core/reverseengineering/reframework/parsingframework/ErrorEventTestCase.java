package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for ErrorEvent.
 */
public class ErrorEventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ErrorEventTestCase.class);
    }

    private IErrorEvent ee;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        ee = new ErrorEvent();
    }
    
    public void testGetFormattedMessage()
    {
        ee.setColumnNumber(10);
        ee.setLineNumber(25);
        ee.setFilename("Filename.java");
        ee.setErrorMessage("Error message");
        assertEquals("Filename.java(line=25, col=10) : Error message",
                     ee.getFormattedMessage());
    }
}
