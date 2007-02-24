package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
/**
 * Test cases for AttributeAction.
 */
public class AttributeActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(AttributeActionTestCase.class);
    }

    private IAttributeAction act;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        act = (IAttributeAction)FactoryRetriever.instance().createType("ReadAttributeAction", null);
        //act.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(act);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        act.delete();
    }

    
    public void testSetAttribute()
    {
        IClass c = createClass("C");
        IAttribute at = c.createAttribute("int", "a");
        c.addAttribute(at);
        act.setAttribute(at);
        assertEquals(at.getXMIID(), act.getAttribute().getXMIID());
    }

    public void testGetAttribute()
    {
        // Tested by testSetAttribute.
    }

    public void testSetObject()
    {
        IInputPin pin = (IInputPin)FactoryRetriever.instance().createType("InputPin", null);
        //pin.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(pin);
        act.setObject(pin);
        assertEquals(pin.getXMIID(), act.getObject().getXMIID());
    }

    public void testGetObject()
    {
        // Tested by testSetObject.
    }
}