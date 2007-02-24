package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.dom4j.DocumentFactory;

/**
 * Test cases for AttributeEvent.
 */
public class AttributeEventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(AttributeEventTestCase.class);
    }

    private IAttributeEvent ae;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        ae = new AttributeEvent();
        ae.setEventData(
                DocumentFactory.getInstance().createElement("UML:Attribute"));
    }

    public void testGetREAttribute()
    {
        assertNotNull(ae.getREAttribute());
    }
}
