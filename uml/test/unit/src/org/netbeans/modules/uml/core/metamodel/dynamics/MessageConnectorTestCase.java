package org.netbeans.modules.uml.core.metamodel.dynamics;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for MessageConnector.
 */
public class MessageConnectorTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(MessageConnectorTestCase.class);
    }

    private IMessageConnector mc;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        mc = createType("MessageConnector");
    }
    
    public void testGetConnectedLifelines()
    {
        testSetFromLifeline();
        testSetToLifeline();
        
        ETList<ILifeline> lives = mc.getConnectedLifelines();
        assertEquals(2, lives.size());
    }

    public void testSetFromLifeline()
    {
        ILifeline line = createType("Lifeline");
        line.setRepresentingClassifier((IClassifier) createType("Class"));
        mc.setFromLifeline(line);
        assertEquals(line.getXMIID(), mc.getFromLifeline().getXMIID());
    }

    public void testGetFromLifeline()
    {
        // Tested by testSetFromLifeline.
    }

    public void testAddMessage()
    {
        IMessage m = createType("Message");
        mc.addMessage(m);
        assertEquals(1, mc.getMessages().size());
        assertEquals(m.getXMIID(), mc.getMessages().get(0).getXMIID());
    }

    public void testRemoveMessage()
    {
        testAddMessage();
        mc.removeMessage(mc.getMessages().get(0));
        assertEquals(0, mc.getMessages().size());
    }

    public void testGetMessages()
    {
        // Tested by testAddMessage.
    }

    public void testSetToLifeline()
    {
        ILifeline line = createType("Lifeline");
        line.setRepresentingClassifier((IClassifier) createType("Class"));
        mc.setToLifeline(line);
        assertEquals(line.getXMIID(), mc.getToLifeline().getXMIID());
    }

    public void testGetToLifeline()
    {
        // Tested by testSetToLifeline.
    }
}