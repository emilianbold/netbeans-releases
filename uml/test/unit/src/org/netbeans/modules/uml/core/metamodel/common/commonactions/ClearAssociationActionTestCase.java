package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IInputPin;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
/**
 * Test cases for ClearAssociationAction.
 */
public class ClearAssociationActionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ClearAssociationActionTestCase.class);
    }

    private IClearAssociationAction act;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        act = (IClearAssociationAction)FactoryRetriever.instance().createType("ClearAssociationAction", null);
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
    
    public void testSetAssociation()
    {
        IClass c1 = createClass("C1"), c2 = createClass("C2");
        IAssociation assoc = relFactory.createAssociation(c1, c2, project);
        act.setAssociation(assoc);
        assertEquals(assoc.getXMIID(), act.getAssociation().getXMIID());
    }

    public void testGetAssociation()
    {
        // Tested by testSetAssociation.
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