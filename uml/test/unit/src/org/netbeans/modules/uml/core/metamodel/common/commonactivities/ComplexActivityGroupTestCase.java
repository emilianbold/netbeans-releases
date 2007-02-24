package org.netbeans.modules.uml.core.metamodel.common.commonactivities;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for ComplexActivityGroup.
 */
public class ComplexActivityGroupTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ComplexActivityGroupTestCase.class);
    }

    private IComplexActivityGroup group;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        group = (IComplexActivityGroup)FactoryRetriever.instance().createType("ComplexActivityGroup", null);
        //group.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(group);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        group.delete();
    }

    
    public void testSetKind()
    {
        group.setGroupKind(BaseElement.AGK_STRUCTURED);
        assertEquals(BaseElement.AGK_STRUCTURED, group.getGroupKind());
        group.setGroupKind(BaseElement.AGK_ITERATION);
        assertEquals(BaseElement.AGK_ITERATION, group.getGroupKind());
        group.setGroupKind(BaseElement.AGK_INTERRUPTIBLE);
        assertEquals(BaseElement.AGK_INTERRUPTIBLE, group.getGroupKind());
        group.setGroupKind(BaseElement.AGK_STRUCTURED);
        assertEquals(BaseElement.AGK_STRUCTURED, group.getGroupKind());
    }

    public void testGetKind()
    {
        // Tested by testSetKind.
    }
}