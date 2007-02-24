package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 */
public class BehaviorTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(BehaviorTestCase.class);
    }
    
    private IBehavior behavior;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        behavior = (IBehavior)FactoryRetriever.instance().createType("Procedure", null);
        //behavior.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(behavior);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        project.removeElement(behavior);
        behavior.delete();
    }
    
    public void testSetContext()
    {
        assertNull(behavior.getContext());
        IClassifier context = createClass("Context");
        behavior.setContext(context);
        assertNotNull(behavior.getContext());
        assertEquals(context.getXMIID(), behavior.getContext().getXMIID());        
    }
    
    public void testGetContext()
    {
        // Tested by setContext.
    }
    
    public void testSetIsReentrant()
    {
        assertFalse(behavior.getIsReentrant());
        behavior.setIsReentrant(true);
        assertTrue(behavior.getIsReentrant());
        behavior.setIsReentrant(false);
        assertFalse(behavior.getIsReentrant());
    }
    
    public void testGetIsReentrant()
    {
        // Tested by setIsReentrant.
    }
    
    public void testAddParameter()
    {
        IParameter par = factory.createParameter(null);
        behavior.addParameter(par);
        
        ETList<IParameter> pars = behavior.getParameters();
        assertNotNull(pars);
        assertEquals(1, pars.size());
        assertEquals(par.getXMIID(), pars.get(0).getXMIID());
    }
    
    public void testRemoveParameter()
    {
        testAddParameter();
        behavior.removeParameter(behavior.getParameters().get(0));
        ETList<IParameter> pars = behavior.getParameters();
        assertTrue(pars == null || pars.size() == 0);
    }
    
    public void testGetParameters()
    {
        // Tested by testAddParameter.
    }
    public void testSetRepresentedFeature()
    {
        IClass clazz = createClass("First");
        IOperation oper = factory.createOperation(clazz);
        clazz.addOperation(oper);
        behavior.setRepresentedFeature(oper);
        assertNotNull(behavior.getRepresentedFeature());
        assertEquals(
            oper.getXMIID(),
            behavior.getRepresentedFeature().getXMIID());
    }
    
    public void testGetRepresentedFeature()
    {
        // Tested by setRepresentedFeature.
    }
    
    public void testSetSpecification()
    {
        IClass clazz = createClass("First");
        IOperation oper = factory.createOperation(clazz);
        clazz.addOperation(oper);
        behavior.setSpecification(oper);
        assertNotNull(behavior.getSpecification());
        assertEquals(oper.getXMIID(), behavior.getSpecification().getXMIID());
    }
    
    public void testGetSpecification()
    {
        // Tested by setSpecification.
    }
}