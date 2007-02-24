package org.netbeans.modules.uml.core.metamodel.core.constructs;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for UseCase.
 */
public class UseCaseTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(UseCaseTestCase.class);
    }

    private IUseCase useCase, useCase2;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        useCase = (IUseCase)FactoryRetriever.instance().createType("UseCase", null);
        //useCase.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(useCase);
        
        useCase2 = (IUseCase)FactoryRetriever.instance().createType("UseCase", null);
        //useCase2.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(useCase2);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        useCase.delete();
    }

    
    public void testAddExtend()
    {
        IExtend ex = new ConstructsRelationFactory().createExtend(useCase, useCase2);
        useCase.addExtend(ex);
        assertEquals(1, useCase.getExtends().size());
        assertEquals(ex.getXMIID(), useCase.getExtends().get(0).getXMIID());
    }

    public void testRemoveExtend()
    {
        testAddExtend();
        useCase.removeExtend(useCase.getExtends().get(0));
        assertEquals(0, useCase.getExtends().size());
    }

    public void testAddExtendedBy()
    {
        IExtend ex = new ConstructsRelationFactory().createExtend(useCase2, useCase);
        useCase.addExtendedBy(ex);
        assertEquals(1, useCase.getExtendedBy().size());
        assertEquals(ex.getXMIID(), useCase.getExtendedBy().get(0).getXMIID());
    }

    public void testRemoveExtendedBy()
    {
        testAddExtendedBy();
        useCase.removeExtendedBy(useCase.getExtendedBy().get(0));
        assertEquals(0, useCase.getExtendedBy().size());

    }

    public void testGetExtendedBy()
    {
        // Tested by testAddExtendedBy
    }

    public void testGetExtends()
    {
        // Tested by testAddExtend.
    }

    public void testCreateExtensionPoint()
    {
        IExtensionPoint ep = useCase.createExtensionPoint();
        assertNotNull(ep);
    }

    public void testAddExtensionPoint()
    {
        IExtensionPoint ep = useCase.createExtensionPoint();
        useCase.addExtensionPoint(ep);
        assertEquals(1, useCase.getExtensionPoints().size());
        assertEquals(ep.getXMIID(), useCase.getExtensionPoints().get(0).getXMIID());
    }

    public void testRemoveExtensionPoint()
    {
        testAddExtensionPoint();
        useCase.removeExtensionPoint(useCase.getExtensionPoints().get(0));
        assertEquals(0, useCase.getExtensionPoints().size());
    }

    public void testGetExtensionPoints()
    {
        // Tested by testAddExtensionPoint.
    }

    public void testAddInclude()
    {
        IInclude in = new ConstructsRelationFactory().createInclude(useCase, useCase2);
        useCase.addInclude(in);
        assertEquals(1, useCase.getIncludes().size());
        assertEquals(in.getXMIID(), useCase.getIncludes().get(0).getXMIID());
    }

    public void testRemoveInclude()
    {
        testAddInclude();
        useCase.removeInclude(useCase.getIncludes().get(0));
        assertEquals(0, useCase.getIncludes().size());
    }

    public void testAddIncludedBy()
    {
        IInclude in = new ConstructsRelationFactory().createInclude(useCase2, useCase);
        useCase.addIncludedBy(in);
        assertEquals(1, useCase.getIncludedBy().size());
        assertEquals(in.getXMIID(), useCase.getIncludedBy().get(0).getXMIID());
    }

    public void testRemoveIncludedBy()
    {
        testAddIncludedBy();
        useCase.removeIncludedBy(useCase.getIncludedBy().get(0));
        assertEquals(0, useCase.getIncludedBy().size());
    }

    public void testGetIncludedBy()
    {
        // Tested by testGetIncludedBy
    }

    public void testGetIncludes()
    {
        // Tested by testAddInclude.
    }

    public void testCreateUseCaseDetail()
    {
        IUseCaseDetail det = useCase.createUseCaseDetail();
        assertNotNull(det);
    }

    public void testRemoveUseCaseDetail()
    {
        testAddUseCaseDetail();
        useCase.removeUseCaseDetail(useCase.getDetails().get(0));
        assertEquals(0, useCase.getDetails().size());
    }

    public void testAddUseCaseDetail()
    {
        IUseCaseDetail det = useCase.createUseCaseDetail();
        useCase.addUseCaseDetail(det);
        assertEquals(1, useCase.getDetails().size());
        assertEquals(det.getXMIID(), useCase.getDetails().get(0).getXMIID());
    }
    
    public void testGetDetails()
    {
        // Tested by testAddUseCaseDetail
    }
}