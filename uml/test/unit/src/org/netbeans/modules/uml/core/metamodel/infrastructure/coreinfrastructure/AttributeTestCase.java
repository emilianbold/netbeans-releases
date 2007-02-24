package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;

/**
 */
public class AttributeTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(AttributeTestCase.class);
    }
    
    private IClass     clazz;
    private IAttribute attr;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        clazz = createClass("Classe");
        attr  = clazz.createAttribute("float", "burger");
        clazz.addAttribute(attr);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        clazz.removeElement(attr);
        attr.delete();
        project.removeElement(clazz);
        clazz.delete();
    }
    
    public void testSetAssociationEnd()
    {
        IAssociationEnd end = factory.createAssociationEnd(null);
        project.addElement(end);
        attr.setAssociationEnd(end);
        assertNotNull(attr.getAssociationEnd());
        assertEquals(end.getXMIID(), attr.getAssociationEnd().getXMIID());
    }
    
    public void testGetAssociationEnd()
    {
        // Tested by testSetAssociationEnd
    }
    
    public void testSetDefault()
    {
        IExpression expr = factory.createExpression(project);
        attr.setDefault(expr);
        assertNotNull(attr.getDefault());
        assertEquals(expr.getXMIID(), attr.getDefault().getXMIID());
    }
    
    public void testGetDefault()
    {
        // Tested by testSetDefault()
    }
    
    public void testSetDefault2()
    {
        attr.setDefault2("10.0");
        assertEquals("10.0", attr.getDefault2());
    }
    
    public void testGetDefault2()
    {
        // Tested by testSetDefault2
    }
    
    public void testSetDefault3()
    {
        attr.setDefault3("java", "5.0");
        assertEquals(new ETPairT<String,String>("java", "5.0"), attr.getDefault3());
    }
    
    public void testGetDefault3()
    {
        // Tested by testSetDefault3
    }
    
    public void testSetDerivationRule()
    {
        IExpression expr = factory.createExpression(null);
        attr.setDerivationRule(expr);
        assertNotNull(attr.getDerivationRule());
        assertEquals(expr.getXMIID(), attr.getDerivationRule().getXMIID());
    }
    
    public void testGetDerivationRule()
    {
        // Tested by testSetDerivationRule
    }
    
    public void testSetHeapBased()
    {
        assertFalse(attr.getHeapBased());
        attr.setHeapBased(true);
        assertTrue(attr.getHeapBased());
        attr.setHeapBased(false);
        assertFalse(attr.getHeapBased());
    }
    
    public void testGetHeapBased()
    {
        // Tested by testSetHeapBased
    }
    
    public void testSetIsDerived()
    {
        assertFalse(attr.getIsDerived());
        attr.setIsDerived(true);
        assertTrue(attr.getIsDerived());
        attr.setIsDerived(false);
        assertFalse(attr.getIsDerived());
    }
    
    public void testGetIsDerived()
    {
        // Tested by testSetIsDerived
    }
    
    public void testSetIsPrimaryKey()
    {
        assertFalse(attr.getIsPrimaryKey());
        attr.setIsPrimaryKey(true);
        assertTrue(attr.getIsPrimaryKey());
        attr.setIsPrimaryKey(false);
        assertFalse(attr.getIsPrimaryKey());
    }
    
    public void testGetIsPrimaryKey()
    {
        // Tested by testSetIsPrimaryKey
    }
    
    public void testSetIsWithEvents()
    {
        assertFalse(attr.getIsWithEvents());
        attr.setIsWithEvents(true);
        assertTrue(attr.getIsWithEvents());
        attr.setIsWithEvents(false);
        assertFalse(attr.getIsWithEvents());
    }
    
    public void testGetIsWithEvents()
    {
        // Tested by setIsWithEvents.
    }
}