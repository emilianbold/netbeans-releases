package org.netbeans.modules.uml.core.metamodel.structure;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


/**
 *
 */
public class DeploymentTestCase extends AbstractUMLTestCase
{
    private IDeployment dep = null;
    
    public DeploymentTestCase()
    {
        super();
    }
    
    protected void setUp()
    {
        dep = factory.createDeployment(null);
        project.addElement(dep);
    }
    
    public void testAddDeployedArtifact()
    {
        IArtifact arti = factory.createArtifact(null);
        project.addElement(arti);
        dep.addDeployedArtifact(arti);
        ETList<IArtifact> elems = dep.getDeployedArtifacts();
        assertNotNull(elems);
        
        IArtifact artiGot = null;
        if (elems != null)
        {
            for (int i=0;i<elems.size();i++)
            {
                artiGot = elems.get(i);
            }
        }
        assertNotNull(artiGot);
        assertEquals(arti.getXMIID(), artiGot.getXMIID());
    }
    
    public void testSetLocation()
    {
        INode node = factory.createNode(null);
        project.addElement(node);
        dep.setLocation(node);
        assertNotNull(dep.getLocation());
        assertEquals(node.getXMIID(), dep.getLocation().getXMIID());
    }
    
    public void testSetSpecification()
    {
        IDeploymentSpecification spec = factory.createDeploymentSpecification(null);
        project.addElement(spec);
        dep.setSpecification(spec);
        assertNotNull(dep.getSpecification());
        assertEquals(spec.getXMIID(),dep.getSpecification().getXMIID());
    }
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(DeploymentTestCase.class);
    }
}


