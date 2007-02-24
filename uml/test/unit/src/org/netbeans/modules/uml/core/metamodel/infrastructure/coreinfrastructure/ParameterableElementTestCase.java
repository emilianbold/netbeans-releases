package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;


/**
 * Test cases for ParameterableElement.
 */
public class ParameterableElementTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ParameterableElementTestCase.class);
    }

    private IParameterableElement pel;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        pel = createClass("Pel");
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        pel.delete();
    }

    public void testSetDefaultElement()
    {
        IParameterableElement def = createClass("Def");
        pel.setDefaultElement(def);
        assertEquals(def.getXMIID(), pel.getDefaultElement().getXMIID());
    }

    public void testSetDefaultElement2()
    {
        pel.setDefaultElement2("Def");
        assertEquals("Def", pel.getDefaultElement().getName());
    }
    
    public void testGetDefaultElement()
    {
        // Tested by setDefaultElement.
    }
    
    public void testSetTemplate()
    {
        IClassifier temp = createClass("T");
        pel.setTemplate(temp);
        assertEquals(temp.getXMIID(), pel.getTemplate().getXMIID());
    }
    
    public void testGetTemplate()
    {
        // Tested by setTemplate.
    }
    
    public void testSetTypeConstraint()
    {
        pel.setTypeConstraint("Cons");
        assertEquals("Cons", pel.getTypeConstraint());
    }
    
    public void testGetTypeConstraint()
    {
        // Tested by setTypeConstraint.
    }
}