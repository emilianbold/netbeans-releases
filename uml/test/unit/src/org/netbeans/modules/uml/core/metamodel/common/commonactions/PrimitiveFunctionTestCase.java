package org.netbeans.modules.uml.core.metamodel.common.commonactions;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;

/**
 * Test cases for PrimitiveFunction.
 */
public class PrimitiveFunctionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(PrimitiveFunctionTestCase.class);
    }

    private IPrimitiveFunction func;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        func = (IPrimitiveFunction)FactoryRetriever.instance().createType("PrimitiveFunction", null);
        //func.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(func);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        func.delete();
    }

    
    public void testSetBody()
    {
        func.setBody("Xyzzy");
        assertEquals("Xyzzy", func.getBody());
    }

    public void testGetBody()
    {
        // Tested by testSetBody.
    }

    public void testSetLanguage()
    {
        func.setLanguage("Perl");
        assertEquals("Perl", func.getLanguage());
    }

    public void testGetLanguage()
    {
        // Tested by testSetLanguage.
    }
}