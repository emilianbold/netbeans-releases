package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;

/**
 * Test cases for UMLBinding.
 */
public class UMLBindingTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(UMLBindingTestCase.class);
    }

    private IUMLBinding bind;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        bind = (IUMLBinding)FactoryRetriever.instance().createType("Binding", null);
        //bind.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(bind);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        bind.delete();
    }
    
    public void testSetActual()
    {
        IClassifier par = createClass("Actual");
        bind.setActual(par);
        assertEquals(par.getXMIID(), bind.getActual().getXMIID());
    }
    
    public void testGetActual()
    {
        // Tested by setActual.
    }
    
    public void testSetFormal()
    {
        IClassifier form = createClass("Formal");
        bind.setFormal(form);
        assertEquals(form.getXMIID(), bind.getFormal().getXMIID());
    }
    
    public void testGetFormal()
    {
        // Tested by setFormal.
    }
}