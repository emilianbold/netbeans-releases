package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IOutputPin;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
/**
 * Test cases for CreateObjectAction.
 */
public class CreateObjectActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(CreateObjectActionTestCase.class);
    }

    private ICreateObjectAction act;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        act = (ICreateObjectAction)FactoryRetriever.instance().createType("CreateObjectAction", null);
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

    
    public void testSetClassifier()
    {
        IClassifier c = createClass("Daimyo");
        act.setClassifier(c);
        assertEquals(c.getXMIID(), act.getClassifier().getXMIID());
    }

    public void testGetClassifier()
    {
        // Tested by testSetClassifier.
    }

    public void testSetResult()
    {
        IOutputPin pin = (IOutputPin)FactoryRetriever.instance().createType("OutputPin", null);
        //pin.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(pin);
        
        act.setResult(pin);
        assertEquals(pin.getXMIID(), act.getResult().getXMIID());
    }

    public void testGetResult()
    {
        // Tested by testSetResult.
    }
}