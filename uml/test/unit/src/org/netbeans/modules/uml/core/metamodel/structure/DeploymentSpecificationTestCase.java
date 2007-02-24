package org.netbeans.modules.uml.core.metamodel.structure;

import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 *
 */
public class DeploymentSpecificationTestCase extends AbstractUMLTestCase
{
    private IDeploymentSpecification depSpec = null;
    
    public DeploymentSpecificationTestCase()
    {
        super();
    }
    
    protected void setUp()
    {
        depSpec = factory.createDeploymentSpecification(null);
        project.addElement(depSpec);
    }
    
    public void testAddDeploymentDescriptor()
    {
        IArtifact arti = factory.createArtifact(null);
        project.addElement(arti);
        depSpec.addDeploymentDescriptor(arti);
        ETList<IArtifact> elems = depSpec.getDeploymentDescriptors();
        assertNotNull(elems);
        
        IArtifact artiGot = null;
        if (elems != null)
        {
            for (int i=0;i<elems.size();i++)
            {
                artiGot = elems.get(i);
            }
        }
        assertEquals(arti, artiGot);
    }
    
    public void testSetContainer()
    {
        INode node = factory.createNode(null);
        project.addElement(node);
        depSpec.setContainer(node);
        assertEquals(node,depSpec.getContainer());
    }
    
    public void testSetDeploymentLocation()
    {
        String loc = "newDepLoc";
        depSpec.setDeploymentLocation(loc);
        assertEquals( loc, depSpec.getDeploymentLocation());
    }
    
    
    public void testSetExecutionLocation()
    {
        String loc = "newExecLoc";
        depSpec.setExecutionLocation(loc);
        assertEquals(loc, depSpec.getExecutionLocation());
    }
    
    public void testAddDeployment()
    {
        IDeployment dep = factory.createDeployment(null);
        project.addElement(dep);
        depSpec.addDeployment(dep);
        ETList<IDeployment> elems = depSpec.getDeployments();
        assertNotNull(elems);
        
        IDeployment depGot = null;
        if (elems != null)
        {
            for (int i=0;i<elems.size();i++)
            {
                depGot = elems.get(i);
            }
        }
        assertEquals(dep, depGot);
    }
    
    public void testSetConfiguredComponent()
    {
        IComponent comp = factory.createComponent(null);
        project.addElement(comp);
        depSpec.setConfiguredComponent(comp);
        assertEquals(comp,depSpec.getConfiguredComponent());
    }
    
    public void testSetConfiguredAssembly()
    {
        IComponentAssembly ca = (IComponentAssembly)FactoryRetriever.instance().createType("ComponentAssembly", null);
        //ca.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(ca);
        depSpec.setConfiguredAssembly(ca);
        assertEquals(ca.getXMIID(), depSpec.getConfiguredAssembly().getXMIID());
    }
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(DeploymentSpecificationTestCase.class);
    }
}


