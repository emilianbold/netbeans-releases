package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * Test cases for Interaction.
 */
public class InteractionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(InteractionTestCase.class);
    }

    private IInteraction inter;
    private IMessage m1, m2, m3;
    private ILifeline last;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        inter = createType("Interaction");

        last = null;
        m1 = createMessage();
        m2 = createMessage();
        m3 = createMessage();
        
        inter.addMessage(m1);
        inter.addMessage(m2);
        inter.addMessage(m3);
    }
    
    private IMessage createMessage()
    {
        IMessage m = createType("Message");
        IEventOccurrence eo = createType("EventOccurrence");
        eo.setLifeline(last != null? last : (ILifeline) createType("Lifeline"));
        m.setSendEvent(eo);
        
        eo = createType("EventOccurrence");
        eo.setLifeline(last = createType("Lifeline"));
        m.setReceiveEvent(eo);
        
        return m;
    }

    public void testResetAutoNumbers()
    {
        assertEquals("1.1.1", m3.getAutoNumber());
        inter.resetAutoNumbers(m1);
        inter.removeMessage(m1);

        // What is the expected number, anyway?
        assertEquals("1.1", m3.getAutoNumber());
    }

    public void testAddConnector()
    {
        IConnector c = createType("Connector");
        inter.addConnector(c);
        assertEquals(1, inter.getConnectors().size());
        assertEquals(c.getXMIID(), inter.getConnectors().get(0).getXMIID());
    }

    public void testRemoveConnector()
    {
        testAddConnector();
        inter.removeConnector(inter.getConnectors().get(0));
        assertEquals(0, inter.getConnectors().size());
    }

    public void testGetConnectors()
    {
        // Tested by testAddConnector.
    }

    public void testAddEventOccurrence()
    {
        IEventOccurrence occ = createType("EventOccurrence");
        inter.addEventOccurrence(occ);
        assertEquals(1, inter.getEventOccurrences().size());
        assertEquals(occ.getXMIID(), inter.getEventOccurrences().get(0).getXMIID());
    }

    public void testRemoveEventOccurrence()
    {
        testAddEventOccurrence();
        inter.removeEventOccurrence(inter.getEventOccurrences().get(0));
        assertEquals(0, inter.getEventOccurrences().size());
    }

    public void testGetEventOccurrences()
    {
        // Tested by testAddEventOccurrence.
    }

    public void testAddGate()
    {
        IGate g = createType("Gate");
        inter.addGate(g);
        assertEquals(1, inter.getGates().size());
        assertEquals(g.getXMIID(), inter.getGates().get(0).getXMIID());
    }

    public void testRemoveGate()
    {
        testAddGate();
        inter.removeGate(inter.getGates().get(0));
        assertEquals(0, inter.getGates().size());
    }

    public void testGetGates()
    {
        // Tested by testAddGate.
    }

    public void testGetGeneralOrderings()
    {
        IGeneralOrdering go = createType("GeneralOrdering");
        inter.addElement(go);
        assertEquals(1, inter.getGeneralOrderings().size());
        assertEquals(go.getXMIID(), inter.getGeneralOrderings().get(0).getXMIID());
    }

    public void testAddLifeline()
    {
        ILifeline life = createType("Lifeline");
        inter.addLifeline(life);
        assertEquals(1, inter.getLifelines().size());
        assertEquals(life.getXMIID(), inter.getLifelines().get(0).getXMIID());
    }

    public void testRemoveLifeline()
    {
        testAddLifeline();
        inter.removeLifeline(inter.getLifelines().get(0));
        assertEquals(0, inter.getLifelines().size());
    }

    public void testGetLifelines()
    {
        // Tested by testAddLifeline.
    }

    public void testCreateMessage()
    {
        ILifeline toElement = createType("Lifeline");
        toElement.setInteraction((IInteraction) createType("Interaction"));
        IInteractionFragment toOwner = createType("Interaction");
        IOperation op = createType("Operation");
        
        assertNotNull(inter.createMessage(toElement, toOwner, op, 
                        BaseElement.MK_SYNCHRONOUS));
    }

    public void testInsertMessage()
    {
        ILifeline toElement = createType("Lifeline");
        toElement.setInteraction((IInteraction) createType("Interaction"));
        IInteractionFragment toOwner = createType("Interaction");
        IOperation op = createType("Operation");
        
        assertNotNull(inter.insertMessage(m3, toElement, toOwner, op, 
                        BaseElement.MK_SYNCHRONOUS));
    }

    public void testHandleMessageAdded()
    {
        // No code to test
    }

    public void testInsertMessageBefore()
    {
        IMessage m4 = createMessage();
        inter.insertMessageBefore(m4, m3);
        
        assertEquals(4, inter.getMessages().size());
        assertEquals(m4.getXMIID(), inter.getMessages().get(2).getXMIID());
    }

    public void testHandleMessageDeleted()
    {
        // No code to test
    }

    public void testAddMessage()
    {
        // Tested by setup
    }

    public void testRemoveMessage()
    {
        inter.removeMessage(m2);
        assertEquals(2, inter.getMessages().size());
        assertEquals(m1.getXMIID(), inter.getMessages().get(0).getXMIID());
        assertEquals(m3.getXMIID(), inter.getMessages().get(1).getXMIID());        
    }

    public void testGetMessages()
    {
        // Tested all over the shop.
    }
}