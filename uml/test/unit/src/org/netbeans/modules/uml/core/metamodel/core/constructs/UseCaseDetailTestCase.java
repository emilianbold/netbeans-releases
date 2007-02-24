package org.netbeans.modules.uml.core.metamodel.core.constructs;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for UseCaseDetail.
 */
public class UseCaseDetailTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(UseCaseDetailTestCase.class);
    }

    private IUseCaseDetail useCaseDetail;
    private IUseCase       u1;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        u1 = (IUseCase)FactoryRetriever.instance().createType("UseCase", null);
        //u1.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(u1);
        
        useCaseDetail = u1.createUseCaseDetail();
        u1.addUseCaseDetail(useCaseDetail);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        useCaseDetail.delete();
        u1.delete();
    }
    
    public void testSetBody()
    {
        useCaseDetail.setBody("dlrowolleh");
        assertEquals("dlrowolleh", useCaseDetail.getBody());
    }

    public void testGetBody()
    {
        // Tested by testSetBody.
    }

    public void testSetParentDetail()
    {
        IUseCaseDetail sub = u1.createUseCaseDetail();
        sub.setParentDetail(useCaseDetail);
        assertEquals(useCaseDetail.getXMIID(), sub.getParentDetail().getXMIID());
    }

    public void testGetParentDetail()
    {
        // Tested by testSetParentDetail.
    }

    public void testCreateSubDetail()
    {
        assertNotNull(useCaseDetail.createSubDetail());
    }

    public void testAddSubDetail()
    {
        IUseCaseDetail sub = useCaseDetail.createSubDetail();
        useCaseDetail.addSubDetail(sub);
        assertEquals(1, useCaseDetail.getSubDetails().size());
        assertEquals(sub.getXMIID(), useCaseDetail.getSubDetails().get(0).getXMIID());
    }

    public void testRemoveSubDetail()
    {
        testAddSubDetail();
        useCaseDetail.removeSubDetail(useCaseDetail.getSubDetails().get(0));
        assertEquals(0, useCaseDetail.getSubDetails().size());
    }

    public void testGetSubDetails()
    {
        // Tested by testAddSubDetail.
    }
}