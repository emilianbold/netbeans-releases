package org.netbeans.modules.uml.core.metamodel.core.constructs;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for Extend.
 */
public class ExtendTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ExtendTestCase.class);
    }

    private IExtend extend;
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
        
        extend = new ConstructsRelationFactory().createExtend(u1, u2);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        extend.delete();
        u1.delete();
        u2.delete();
    }

    
    public void testSetBase()
    {
        extend.setBase(u1);
        assertEquals(u1.getXMIID(), extend.getBase().getXMIID());
    }

    public void testGetBase()
    {
        // Tested by testSetBase.
    }

    public void testSetCondition2()
    {
        extend.setCondition2("x == y");
        assertEquals("x == y", extend.getCondition().getExpression());
    }

    public void testSetCondition()
    {
        IConstraint c = factory.createConstraint(null);
        project.addElement(c);
        extend.setCondition(c);
        assertEquals(c.getXMIID(), extend.getCondition().getXMIID());
    }

    public void testGetCondition()
    {
        // Tested by testSetCondition.
    }

    public void testAddExtensionLocation()
    {
        IExtensionPoint ep = (IExtensionPoint)FactoryRetriever.instance().createType("ExtensionPoint", null);
        //ep.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(ep);
        extend.addExtensionLocation(ep);
        assertEquals(1, extend.getExtensionLocations().size());
        assertEquals(ep.getXMIID(), extend.getExtensionLocations().get(0).getXMIID());
    }

    public void testRemoveExtensionLocation()
    {
        testAddExtensionLocation();
        extend.removeExtensionLocation(extend.getExtensionLocations().get(0));
        assertEquals(0, extend.getExtensionLocations().size());
    }

    public void testGetExtensionLocations()
    {
        // Tested by testAddExtensionLocation.
    }

    public void testSetExtension()
    {
        extend.setExtension(u1);
        assertEquals(u1.getXMIID(), extend.getExtension().getXMIID());
    }

    public void testGetExtension()
    {
        // Tested by testSetExtension.
    }
}