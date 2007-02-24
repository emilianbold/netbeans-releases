package org.netbeans.modules.uml.core.metamodel.common.commonactions;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;

/**
 * Test cases for SwitchAction.
 */
public class SwitchActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(SwitchActionTestCase.class);
    }

    private ISwitchAction act;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        act = (ISwitchAction)FactoryRetriever.instance().createType("SwitchAction", null);
        //act.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(act);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        act.delete();
    }

    
    public void testGetOption()
    {
        ISwitchOption swo = (ISwitchOption)FactoryRetriever.instance().createType("SwitchOption", null);
        //swo.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(swo);
        act.addClause(swo);
        
        assertEquals(1, act.getOption().size());
        assertEquals(swo.getXMIID(), act.getOption().get(0).getXMIID());
    }
}
