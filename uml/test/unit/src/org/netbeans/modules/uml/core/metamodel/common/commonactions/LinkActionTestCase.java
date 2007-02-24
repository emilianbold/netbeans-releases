package org.netbeans.modules.uml.core.metamodel.common.commonactions;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
/**
 * Test cases for LinkAction.
 */
public class LinkActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(LinkActionTestCase.class);
    }

    private ILinkAction linkAction;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        linkAction = (ILinkAction)FactoryRetriever.instance().createType("ReadLinkAction", null);
        //linkAction.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(linkAction);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        linkAction.delete();
    }

    
    public void testAddEndData()
    {
        ILinkEndData end = (ILinkEndData)FactoryRetriever.instance().createType("LinkEndData", null);
        //end.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(end);
        linkAction.addEndData(end);
        assertEquals(1, linkAction.getEndData().size());
        assertEquals(end.getXMIID(), linkAction.getEndData().get(0).getXMIID());
    }

    public void testRemoveEndData()
    {
        testAddEndData();
        linkAction.removeEndData(linkAction.getEndData().get(0));
        assertEquals(0, linkAction.getEndData().size());
    }

    public void testGetEndData()
    {
        // Tested by testAddEndData
    }
}