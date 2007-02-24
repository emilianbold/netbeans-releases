package org.netbeans.modules.uml.core.metamodel.common.commonactivities;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for IterationActivityGroup.
 */
public class IterationActivityGroupTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(IterationActivityGroupTestCase.class);
    }

    private IIterationActivityGroup group;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        group = (IIterationActivityGroup)FactoryRetriever.instance().createType("ComplexActivityGroup", null);
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
        group.setKind(BaseElement.IAG_TEST_AT_END);
        assertEquals(BaseElement.IAG_TEST_AT_END, group.getKind());

        group.setKind(BaseElement.IAG_TEST_AT_BEGIN);
        assertEquals(BaseElement.IAG_TEST_AT_BEGIN, group.getKind());
        
        group.setKind(5);
        assertTrue(5 != group.getKind());
        
        group.setKind(7);
        assertTrue(7 != group.getKind());
    }

    public void testGetKind()
    {
        // Tested by testSetKind.
    }

    public void testSetTest()
    {
        IValueSpecification spec = factory.createExpression(null);
        project.addElement(spec);
        group.setTest(spec);
        assertEquals(spec.getXMIID(), group.getTest().getXMIID());
    }

    public void testGetTest()
    {
        // Tested by testSetTest.
    }
}