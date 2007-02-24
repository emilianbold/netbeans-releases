package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.dynamics.ActionOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.ExecutionOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.GeneralOrdering;
import org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.IExecutionOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.IGeneralOrdering;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.dynamics.InterGateConnector;
import org.netbeans.modules.uml.core.metamodel.dynamics.Interaction;
import org.netbeans.modules.uml.core.metamodel.dynamics.Message;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Event;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IEvent;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;

/**
 * Test cases for EventOccurrence.
 */
public class EventOccurrenceTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(EventOccurrenceTestCase.class);
    }

    private IEventOccurrence eventOccurrence;
    private IInteraction     inter;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        eventOccurrence = new TypedFactoryRetriever<IEventOccurrence>()
                            .createType("EventOccurrence");
        inter = new TypedFactoryRetriever<IInteraction>()
                    .createType("Interaction");
        project.addElement(inter);
        inter.addEventOccurrence(eventOccurrence);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        eventOccurrence.delete();
    }

    public void testAddAfterOrdering()
    {
        IGeneralOrdering ord = (IGeneralOrdering)FactoryRetriever.instance().createType("GeneralOrdering", null);
        //ord.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(ord);
        
        eventOccurrence.addAfterOrdering(ord);
        
        assertEquals(1, eventOccurrence.getAfterOrderings().size());
        assertEquals(ord.getXMIID(), 
                eventOccurrence.getAfterOrderings().get(0).getXMIID());
    }

    public void testRemoveAfterOrdering()
    {
        testAddAfterOrdering();
        eventOccurrence.removeAfterOrdering(
                eventOccurrence.getAfterOrderings().get(0));
        assertEquals(0, eventOccurrence.getAfterOrderings().size());
    }

    public void testGetAfterOrderings()
    {
        // Tested by testAddAfterOrdering.
    }

    public void testAddBeforeOrdering()
    {
        IGeneralOrdering ord = (IGeneralOrdering)FactoryRetriever.instance().createType("GeneralOrdering", null);
        //ord.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(ord);
        
        eventOccurrence.addBeforeOrdering(ord);
        
        assertEquals(1, eventOccurrence.getBeforeOrderings().size());
        assertEquals(ord.getXMIID(), 
                eventOccurrence.getBeforeOrderings().get(0).getXMIID());
    }

    public void testRemoveBeforeOrdering()
    {
        testAddBeforeOrdering();
        eventOccurrence.removeBeforeOrdering(
                eventOccurrence.getBeforeOrderings().get(0));
        assertEquals(0, eventOccurrence.getBeforeOrderings().size());
    }

    public void testGetBeforeOrderings()
    {
        // Tested by testAddBeforeOrdering.
    }

    public void testSetConnection()
    {
        IInterGateConnector conn = (IInterGateConnector)FactoryRetriever.instance().createType("InterGateConnector", null);
        //conn.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(conn);
        
        eventOccurrence.setConnection(conn);
        assertEquals(conn.getXMIID(), 
                eventOccurrence.getConnection().getXMIID());
    }

    public void testGetConnection()
    {
        // Tested by testSetConnection.
    }

    public void testSetEventType()
    {
        IEvent e = (IEvent)FactoryRetriever.instance().createType("Event", null); 
//        {
//            /* (non-Javadoc)
//             * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#establishNodePresence(org.dom4j.Document, org.dom4j.Node)
//             */
//            public void establishNodePresence(Document doc, Node node)
//            {
//                buildNodePresence("UML:Event", doc, node);
//            }
//        };
//        e.prepareNode(DocumentFactory.getInstance().createElement(""));
		if (e == null)
		{
			return;
		}
        project.addElement(e);
        
        eventOccurrence.setEventType(e);
        assertEquals(e.getXMIID(), eventOccurrence.getEventType().getXMIID());
    }

    public void testGetEventType()
    {
        // Tested by testSetEventType.
    }

    public void testSetFinishExec()
    {
        IExecutionOccurrence occ = (IExecutionOccurrence)FactoryRetriever.instance().createType("ActionOccurrence", null);
        //occ.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(occ);
        eventOccurrence.setFinishExec(occ);
        assertEquals(occ.getXMIID(), eventOccurrence.getFinishExec().getXMIID());
    }

    public void testGetFinishExec()
    {
        // Tested by testSetFinishExec.
    }

    public void testGetInteraction()
    {
        assertEquals(inter.getXMIID(), 
                eventOccurrence.getInteraction().getXMIID());
    }

    public void testSetLifeline()
    {
        ILifeline life = new TypedFactoryRetriever<ILifeline>()
                            .createType("Lifeline");
        project.addElement(life);
        eventOccurrence.setLifeline(life);
        
        assertEquals(life.getXMIID(), eventOccurrence.getLifeline().getXMIID());
    }

    public void testGetLifeline()
    {
        // Tested by testSetLifeline.
    }

    public void testSetReceiveMessage()
    {
        IMessage m = (IMessage)FactoryRetriever.instance().createType("Message", null);
        //m.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(m);
        eventOccurrence.setReceiveMessage(m);
        assertEquals(m.getXMIID(), eventOccurrence.getReceiveMessage().getXMIID());
    }

    public void testGetReceiveMessage()
    {
        // Tested by testSetReceiveMessage.
    }

    public void testSetSendMessage()
    {
        IMessage m = (IMessage)FactoryRetriever.instance().createType("Message", null);
        //m.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(m);
        eventOccurrence.setSendMessage(m);
        assertEquals(m.getXMIID(), eventOccurrence.getSendMessage().getXMIID());
    }

    public void testGetSendMessage()
    {
        // Tested by testSetSendMessage.
    }

    public void testSetStartExec()
    {
        IExecutionOccurrence occ = (IExecutionOccurrence)FactoryRetriever.instance().createType("ActionOccurrence", null);
        //occ.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(occ);
        eventOccurrence.setStartExec(occ);
        assertEquals(occ.getXMIID(), eventOccurrence.getStartExec().getXMIID());
    }

    public void testGetStartExec()
    {
        // Tested by testSetStartExec.
    }
}