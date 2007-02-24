package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for Message.
 */
public class MessageTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(MessageTestCase.class);
    }

    private IMessage m;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        m = createType("Message");
    }
    
    public void testResetAutoNumber()
    {
        // We can't really test this in isolation, but this is tested in
        // Interaction.
    }

    public void testGetAutoNumber()
    {
        assertEquals("1", m.getAutoNumber());
    }

    public void testSetConnector()
    {
        IConnector c = createType("Connector");
        m.setConnector(c);
        assertEquals(c.getXMIID(), m.getConnector().getXMIID());
    }

    public void testGetConnector()
    {
        // Tested by testSetConnector.
    }

    public void testSetInitiatingAction()
    {
        IExecutionOccurrence eo = createType("ActionOccurrence");
        m.setInitiatingAction(eo);
        assertEquals(eo.getXMIID(), m.getInitiatingAction().getXMIID());
    }

    public void testGetInitiatingAction()
    {
        // Tested by testSetInitiatingAction.
    }

    public void testSetInteractionOperand()
    {
        IEventOccurrence se = createType("EventOccurrence"),
                         re = createType("EventOccurrence");
        IAtomicFragment  sef = createType("AtomicFragment"),
                         ref = createType("AtomicFragment");
        sef.addElement(se);
        ref.addElement(re);
        
        m.setSendEvent(se);
        m.setReceiveEvent(re);
        
        IInteractionOperand op = createType("InteractionOperand");
        m.setInteractionOperand(op);
        assertEquals(op.getXMIID(), m.getInteractionOperand().getXMIID());
    }

    public void testGetInteractionOperand()
    {
        // Tested by testSetInteractionOperand.
    }

    public void testSetInteraction()
    {
        IInteraction inter = createType("Interaction");
        m.setInteraction(inter);
        assertEquals(inter.getXMIID(), m.getInteraction().getXMIID());
    }

    public void testGetInteraction()
    {
        // Tested by testSetInteraction.
    }

    public void testSetKind()
    {
        m.setKind(BaseElement.MK_CREATE);
        assertEquals(BaseElement.MK_CREATE, m.getKind());
        
        m.setKind(BaseElement.MK_SYNCHRONOUS);
        assertEquals(BaseElement.MK_SYNCHRONOUS, m.getKind());
    }

    public void testGetKind()
    {
        // Tested by testSetKind.
    }

    public void testSetOperationInvoked()
    {
        IOperation op = createType("Operation");
        IEventOccurrence se = createType("EventOccurrence"),
                         re = createType("EventOccurrence");
        se.setStartExec((IActionOccurrence) createType("ActionOccurrence"));
        re.setFinishExec((IProcedureOccurrence) createType("ProcedureOccurrence"));
        
        m.setSendEvent(se);
        m.setReceiveEvent(re);
        
        IInteraction inter = createType("Interaction");
        m.setInteraction(inter);
        
        m.setOperationInvoked(op);
        assertEquals(op.getXMIID(), m.getOperationInvoked().getXMIID());
    }

    public void testGetOperationInvoked()
    {
        // Tested by testSetOperationInvoked.
    }

    public void testSetReceiveEvent()
    {
        IEventOccurrence eo = createType("EventOccurrence");
        m.setReceiveEvent(eo);
        assertEquals(eo.getXMIID(), m.getReceiveEvent().getXMIID());
    }

    public void testGetReceiveEvent()
    {
        // Tested by testSetReceiveEvent.
    }

    public void testGetReceivingClassifier()
    {
        testSetReceiveEvent();
        ILifeline life = createType("Lifeline");
        IClassifier c = createType("Class");
        life.setRepresentingClassifier(c);
        m.getReceiveEvent().setLifeline(life);
        assertEquals(c.getXMIID(), m.getReceivingClassifier().getXMIID());
    }

    public void testGetReceivingLifeline()
    {
        testSetReceiveEvent();
        ILifeline life = createType("Lifeline");
        m.getReceiveEvent().setLifeline(life);
        assertEquals(life.getXMIID(), m.getReceivingLifeline().getXMIID());
    }

    public void testGetReceivingOperations()
    {
        testSetReceiveEvent();
        ILifeline life = createType("Lifeline");
        IClassifier c = createType("Class");
        life.setRepresentingClassifier(c);
        m.getReceiveEvent().setLifeline(life);
        
        
        IOperation op = c.createOperation("int", "a");
        c.addOperation(op);
        
        assertEquals(1, m.getReceivingOperations().size());
        assertEquals(op.getXMIID(), m.getReceivingOperations().get(0).getXMIID());
    }

    public void testGetRecurrence()
    {
        // AZTEC: TODO: How to test?
    }

    public void testSetSendEvent()
    {
        IEventOccurrence eo = createType("EventOccurrence");
        m.setSendEvent(eo);
        assertEquals(eo.getXMIID(), m.getSendEvent().getXMIID());
    }

    public void testGetSendEvent()
    {
        // Tested by testSetSendEvent.
    }

    public void testGetSendingClassifier()
    {
        testSetSendEvent();
        ILifeline life = createType("Lifeline");
        IClassifier c = createType("Class");
        life.setRepresentingClassifier(c);
        m.getSendEvent().setLifeline(life);
        assertEquals(c.getXMIID(), m.getSendingClassifier().getXMIID());
    }

    public void testGetSendingLifeline()
    {
        testSetSendEvent();
        ILifeline life = createType("Lifeline");
        m.getSendEvent().setLifeline(life);
        assertEquals(life.getXMIID(), m.getSendingLifeline().getXMIID());
    }

    public void testSetSendingMessage()
    {
        IMessage sending = createType("Message");
        m.setSendingMessage(sending);
        assertEquals(sending.getXMIID(), m.getSendingMessage().getXMIID());
    }

    public void testGetSendingMessage()
    {
        // Tested by testSetSendingMessage.
    }
}