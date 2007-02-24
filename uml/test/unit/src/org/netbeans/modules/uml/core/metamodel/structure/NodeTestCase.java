package org.netbeans.modules.uml.core.metamodel.structure;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


/**
 *
 */
public class NodeTestCase extends AbstractUMLTestCase
{
    private INode node = null;
    
    protected void setUp()
    {
        node = factory.createNode(null);
        project.addElement(node);
    }
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(NodeTestCase.class);
    }
    
    public void testAddDeployment()
    {
        assertNotNull(node);
        IDeployment dep = factory.createDeployment(null);
        project.addElement(dep);
        node.addDeployment(dep);
        
        ETList<IDeployment> elems = node.getDeployments();
        IDeployment depGot = null;
        if (elems != null)
        {
            for (int i=0;i<elems.size();i++)
            {
                depGot = elems.get(i);
            }
        }
        assertNotNull(depGot);
        assertEquals(dep.getXMIID(), depGot.getXMIID());
    }
    
    public void testAddDeployedElement()
    {
        IClass clazz1 = factory.createClass(null);
        project.addElement(clazz1);
        node.addDeployedElement(clazz1);
        ETList<INamedElement> elems = node.getDeployedElements();
        assertNotNull(elems);
        
        INamedElement namedEleGot = null;
        if (elems != null)
        {
            for (int i=0;i<elems.size();i++)
            {
                namedEleGot = elems.get(i);
            }
        }
        assertNotNull(namedEleGot);
        assertEquals(((INamedElement)clazz1).getXMIID(), namedEleGot.getXMIID());
    }
    
    
}


