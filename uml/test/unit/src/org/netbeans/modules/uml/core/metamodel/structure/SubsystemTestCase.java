package org.netbeans.modules.uml.core.metamodel.structure;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageableElement;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 *
 */
public class SubsystemTestCase extends AbstractUMLTestCase
{
    private ISubsystem subSys = null;
    public SubsystemTestCase()
    {
        super();
    }
    
    protected void setUp()
    {
        subSys = factory.createSubsystem(null);
        project.addElement(subSys);
    }
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(SubsystemTestCase.class);
    }
    
    public void testAddRealizationElement()
    {
        IPackageableElement packEleAdded = (IPackageableElement)
        factory.createDependency(null);
        project.addElement(packEleAdded);
        subSys.addRealizationElement(packEleAdded);
        ETList<IPackageableElement> elems = subSys.getRealizationElements();
        assertNotNull(elems);
        
        IPackageableElement packEleGot = null;
        if (elems != null)
        {
            for (int i=0;i<elems.size();i++)
            {
                packEleGot = elems.get(i);
            }
        }
        assertEquals(packEleAdded.getXMIID(), packEleGot.getXMIID());
    }
    
    public void testAddSpecificationElement()
    {
        IPackageableElement packEleAdded = (IPackageableElement)
        factory.createDependency(null);
        project.addElement(packEleAdded);
        subSys.addSpecificationElement(packEleAdded);
        ETList<IPackageableElement> elems = subSys.getSpecificationElements();
        assertNotNull(elems);
        
        IPackageableElement packEleGot = null;
        if (elems != null)
        {
            for (int i=0;i<elems.size();i++)
            {
                packEleGot = elems.get(i);
            }
        }
        assertEquals(packEleAdded.getXMIID(), packEleGot.getXMIID());
    }
    
}


