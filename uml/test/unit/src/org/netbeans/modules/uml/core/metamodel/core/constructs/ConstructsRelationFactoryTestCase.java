package org.netbeans.modules.uml.core.metamodel.core.constructs;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for ConstructsRelationFactory.
 */
public class ConstructsRelationFactoryTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ConstructsRelationFactoryTestCase.class);
    }

    private IConstructsRelationFactory constructsRelationFactory =
        new ConstructsRelationFactory();
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
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        u1.delete();
        u2.delete();
    }

    
    public void testCreateExtend()
    {
        assertNotNull(constructsRelationFactory.createExtend(u1, u2));
    }

    public void testCreateInclude()
    {
        assertNotNull(constructsRelationFactory.createInclude(u1, u2));
    }
}