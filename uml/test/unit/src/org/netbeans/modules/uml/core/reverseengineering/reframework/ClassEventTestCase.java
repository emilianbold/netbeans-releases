package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.dom4j.DocumentFactory;

/**
 * Test cases for ClassEvent.
 */
public class ClassEventTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ClassEventTestCase.class);
    }

    private IClassEvent ce;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        ce = new ClassEvent();
        
        ce.setEventData(DocumentFactory.getInstance()
                .createElement("UML:Class"));
    }

    public void testGetREClass()
    {
        assertNotNull(ce.getREClass());
    }
}