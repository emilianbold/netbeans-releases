package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for Event.
 */
public class EventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(EventTestCase.class);
    }

    private IEvent event;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        // We currently have no concrete (XMI complete) class we can use, so we 
        // fake one.
        event = (IEvent)FactoryRetriever.instance().createType("Event", null);
//        {
//            public void establishNodePresence(Document doc, Node parent)
//            {
//                buildNodePresence("UML:Event",doc,parent);
//            }
//        };
//        event.prepareNode(DocumentFactory.getInstance().createElement(""));
		if (event != null)
		{
			project.addElement(event);
		}
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
		if (event == null)
		{
			return;
		}
        project.removeElement(event);
        event.delete();
    }

    public void testAddArgument()
    {
		if (event == null)
		{
			return;
		}
        IValueSpecification val = factory.createExpression(null);
        event.addArgument(val);
        
        ETList<IValueSpecification> vals = event.getArguments();
        assertEquals(1, vals.size());
        assertEquals(val.getXMIID(), vals.get(0).getXMIID());
    }
    
    public void testRemoveArgument()
    {
		if (event == null)
		{
			return;
		}
        testAddArgument();
        event.removeArgument(event.getArguments().get(0));
        assertEquals(0, event.getArguments().size());
    }
    
    public void testGetArguments()
    {
        // Tested by testAddArgument and testRemoveArgument
    }
}