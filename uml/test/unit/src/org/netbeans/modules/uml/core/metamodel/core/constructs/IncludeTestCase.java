package org.netbeans.modules.uml.core.metamodel.core.constructs;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for Include.
 */
public class IncludeTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(IncludeTestCase.class);
    }

    private IInclude include;
    private IUseCase u1, u2;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        u1 = (IUseCase)FactoryRetriever.instance().createType("UseCase", null);
        //u1.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(u1);
        
        u2 = (IUseCase)FactoryRetriever.instance().createType("UseCase", null);
        //u2.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(u2);
        
        include = new ConstructsRelationFactory().createInclude(u1, u2);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        include.delete();
        u1.delete();
        u2.delete();
    }

    
    public void testSetAddition()
    {
        include.setAddition(u1);
        assertEquals(u1.getXMIID(), include.getAddition().getXMIID());
    }

    public void testGetAddition()
    {
        // Tested by testSetAddition.
    }

    public void testSetBase()
    {
        include.setBase(u1);
        assertEquals(u1.getXMIID(), include.getBase().getXMIID());
    }

    public void testGetBase()
    {
        // Tested by testSetBase.
    }
}