package org.netbeans.modules.uml.core.metamodel.common.commonactions;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;

/**
 * Test cases for Variable.
 */
public class VariableTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(VariableTestCase.class);
    }

    private IVariable var;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        var = (IVariable)FactoryRetriever.instance().createType("Variable", null);
        //var.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(var);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        var.delete();
    }

    
    public void testSetScope()
    {
        IGroupAction ga = (IGroupAction)FactoryRetriever.instance().createType("GroupAction", null);
        //ga.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(ga);
        var.setScope(ga);
        assertEquals(ga.getXMIID(), var.getScope().getXMIID());
    }

    public void testGetScope()
    {
        // Tested by testSetScope.
    }
}