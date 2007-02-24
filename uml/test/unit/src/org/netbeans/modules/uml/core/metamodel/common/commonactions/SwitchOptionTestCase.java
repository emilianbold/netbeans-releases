package org.netbeans.modules.uml.core.metamodel.common.commonactions;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;

/**
 * Test cases for SwitchOption.
 */
public class SwitchOptionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(SwitchOptionTestCase.class);
    }

    private ISwitchOption opt;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        opt = (ISwitchOption)FactoryRetriever.instance().createType("SwitchOption", null);
        //opt.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(opt);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        opt.delete();
    }

    
    public void testSetFallThrough()
    {
        opt.setFallThrough(true);
        assertTrue(opt.getFallThrough());
        opt.setFallThrough(false);
        assertFalse(opt.getFallThrough());
    }

    public void testGetFallThrough()
    {
        // Tested by testSetFallThrough.
    }

    public void testSetIsDefault()
    {
        opt.setIsDefault(true);
        assertTrue(opt.getIsDefault());
        opt.setIsDefault(false);
        assertFalse(opt.getIsDefault());
    }

    public void testGetIsDefault()
    {
        // Tested by testSetIsDefault.
    }
}