package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.dom4j.Element;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IPrimitiveAction;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * Test cases for MethodEvent.
 */
public class MethodEventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(MethodEventTestCase.class);
    }

    private MethodEvent me;
    private String      typeID;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        IPrimitiveAction prim = createType("DestroyObjectAction");
        
        prim.getElementNode().addAttribute("instance", "Neo");
        prim.getElementNode().addAttribute("name", "One");

        IOperation op = createType("Operation");
        op.setReturnType2("cheese");
        typeID = op.getReturnType().getTypeID();
        
        Element opel = op.getElementNode();
        opel.detach();
        prim.getElementNode().add(opel);
        
        XMLManip.createElement(prim.getElementNode(), "UML:Class")
            .addAttribute("name", "Morpheus");
        
        IInputPin pin = createType("InputPin");
        pin.getElementNode().addAttribute("kind", "Type");
        pin.getElementNode().addAttribute("value", "fubar");
        
        prim.addArgument(pin);
        
        me = new MethodEvent();
        me.setEventData(prim.getNode());
    }

//    public void testGetArguments()
//    {
//        ETList<IREArgument> args = me.getArguments();
//        assertEquals(0, args.size());
//        assertEquals("fubar", args.get(0).getType());
//    }

    public void testGetDeclaringClassName()
    {
        assertEquals("Morpheus", me.getDeclaringClassName());
    }

    public void testGetInstanceName()
    {
        assertEquals("Neo", me.getInstanceName());
    }

    public void testGetMethodName()
    {
        assertEquals("One", me.getMethodName());
    }

    public void testGetOperation()
    {
        assertNotNull(me.getOperation());
    }

    public void testGetREClass()
    {
        assertNotNull(me.getREClass());
    }

    public void testGetResult()
    {
        assertEquals(typeID, me.getResult());
    }
}