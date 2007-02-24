package org.netbeans.modules.uml.core.metamodel.core.constructs;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for PartFacade.
 */
public class PartFacadeTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(PartFacadeTestCase.class);
    }

    private IPartFacade partFacade;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        partFacade = (IPartFacade)FactoryRetriever.instance().createType("PartFacade", null);
        //partFacade.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(partFacade);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        partFacade.delete();
    }

    
    public void testGetExpandedElementType()
    {
        assertEquals("PartFacade", partFacade.getExpandedElementType());
    }

    public void testSetFeaturingClassifier()
    {
        IClassifier c = createClass("MPEG");
        partFacade.setFeaturingClassifier(c);
        assertEquals(c.getXMIID(), partFacade.getFeaturingClassifier().getXMIID());
    }

    public void testGetFeaturingClassifier()
    {
        // Tested by testSetFeaturingClassifier.
    }

    public void testSetOwner()
    {
        IClassifier c = createClass("MPEG");
        partFacade.setOwner(c);
        assertEquals(c.getXMIID(), partFacade.getOwner().getXMIID());
    }
}