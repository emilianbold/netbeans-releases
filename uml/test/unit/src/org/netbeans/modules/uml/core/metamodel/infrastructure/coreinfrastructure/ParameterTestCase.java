package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.Procedure;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;

/**
 * Test cases for Parameter.
 */
public class ParameterTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ParameterTestCase.class);
    }

    private IOperation op;
    private IClass     c;
    private IParameter par;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        c = createClass("Xyzzy");
        op = c.createOperation("int", "zig");
        c.addOperation(op);
        
        par = op.createParameter("float", "d");
        op.addParameter(par);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        par.delete();
        op.delete();
        c.delete();
    }
    
    public void testGetBehavioralFeature()
    {
        assertEquals(op.getXMIID(), par.getBehavioralFeature().getXMIID());
    }
    
    public void testGetBehavior()
    {
        Procedure proc = (Procedure)FactoryRetriever.instance().createType("Procedure", null);
        //proc.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(proc);
        
        proc.addParameter(par);
        assertEquals(proc.getXMIID(), par.getBehavior().getXMIID());
    }
    
    public void testSetDefault2()
    {
        par.setDefault2("10");
        assertEquals("10", par.getDefault2());
    }
    
    public void testGetDefault2()
    {
        // Tested by setDefault2.
    }
    
    public void testSetDefault3()
    {
        par.setDefault3("java", "25");
        assertEquals("java,25", par.getDefault3());
    }
    
    public void testGetDefault3()
    {
        // Tested by setDefault3.
    }
    
    public void testSetDefault()
    {
        IExpression expr;
        par.setDefault(expr = factory.createExpression(null));
        assertEquals(expr.getXMIID(), par.getDefault().getXMIID());
    }
    
    public void testGetDefault()
    {
        // Tested by setDefault.
    }
    
    public void testSetDirection()
    {
        par.setDirection(0);
        assertEquals(0, par.getDirection());
        par.setDirection(1);
        assertEquals(1, par.getDirection());
    }
    
    public void testGetDirection()
    {
        // Tested by setDirection.
    }
    
    public void testPerformDuplication()
    {
        IVersionableElement ver = par.performDuplication();
        assertNotNull(ver);
        assertTrue(ver instanceof IParameter);
        
        IParameter cloneP = (IParameter) ver;
        assertEquals(par.getName(), cloneP.getName());
        op.addParameter(cloneP);
        assertEquals(par.getTypeName(), cloneP.getTypeName());
    }
    
    public void testSetName()
    {
        par.setName("ice");
        assertEquals("ice", par.getName());
    }
    
    public void testGetName()
    {
        // Tested by setName.
    }
    
    public void testSetParameterKind()
    {
        par.setParameterKind(1);
        assertEquals(1, par.getParameterKind());

        par.setParameterKind(0);
        assertEquals(0, par.getParameterKind());
    }
    
    public void testGetParameterKind()
    {
        // Tested by setParameterKind.
    }
    
    public void testSetType2()
    {
        par.setType2("Canberra");
        assertEquals("Canberra", par.getTypeName());
    }
    
    public void testSetTypeName()
    {
        par.setTypeName("Canberra");
        assertEquals("Canberra", par.getTypeName());
    }
    
    public void testGetTypeName()
    {
        // Tested by setTypeName.
    }
}