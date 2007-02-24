package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for Derivation.
 */
public class DerivationTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(DerivationTestCase.class);
    }

    private IDerivation derv;
    private IClass inst, templ;
        
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        inst  = createClass("Instance");
        templ = createClass("T");
        templ.addTemplateParameter(createClass("Type"));
        derv  = relFactory.createDerivation(inst, templ);
        
        project.addElement(derv);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        project.removeElement(derv);
        derv.delete();
        inst.delete();
        templ.delete();
    }

    private IUMLBinding createBinding()
    {
        IUMLBinding bind = (IUMLBinding)FactoryRetriever.instance().createType("Binding", null);
        //bind.prepareNode(DocumentFactory.getInstance().createElement(""));
        
        project.addElement(bind);
        
        return bind;
    }
    
    public void testAddBinding()
    {
        IUMLBinding bind = createBinding();
        derv.addBinding(bind);
        
        ETList<IUMLBinding> binds = derv.getBindings();
        assertNotNull(binds);
        assertEquals(2, binds.size());
        assertEquals(bind.getXMIID(), binds.get(1).getXMIID());
    }

    public void testRemoveBinding()
    {
        testAddBinding();
        derv.removeBinding(derv.getBindings().get(1));
        assertEquals(1, derv.getBindings().size());
    }
    
    public void testGetBindings()
    {
        // Tested by testAddBinding and testRemoveBinding
    }
    
    public void testSetDerivedClassifier()
    {
        IClass dc = createClass("DervCl");
        derv.setDerivedClassifier(dc);
        assertEquals(dc.getXMIID(), derv.getDerivedClassifier().getXMIID());
    }
    
    public void testGetDerivedClassifier()
    {
        // Tested by setDerivedClassifier.
    }
    
    public void testSetTemplate()
    {
        IClass t = createClass("Template");
        t.addTemplateParameter(createClass("TemplatePar"));
        derv.setTemplate(t);
        assertEquals(t.getXMIID(), derv.getTemplate().getXMIID());
    }
    
    public void testGetTemplate()
    {
        // Tested by setTemplate.
    }
}