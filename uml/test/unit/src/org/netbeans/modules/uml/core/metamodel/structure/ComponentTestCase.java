package org.netbeans.modules.uml.core.metamodel.structure;

import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateMachine;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPart;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPort;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 *
 */

//Some test methods need to be verified.
public class ComponentTestCase extends AbstractUMLTestCase
{
    private IArtifact arti = null;
    private IComponent component = null;
    private INode node = factory.createNode(null);
    private IDeploymentSpecification spec = factory.createDeploymentSpecification(null);
    private IPort port = factory.createPort(null);
    private IElementImport impor = factory.createElementImport(null);
    private IConnector connector = factory.createConnector(null);
    private IPart part = factory.createPart(null);
    
    public ComponentTestCase()
    {
        super();
    }
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(ComponentTestCase.class);
    }
    
    protected void setUp()
    {
        arti = factory.createArtifact(null);
        component = factory.createComponent(null);
        project.addElement(arti);
        project.addElement(component);
    }
    
    public void testAddArtifact()
    {
        assertNotNull(component);
        component.addArtifact(arti);
        ETList<IArtifact> elems = component.getArtifacts();
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
    
    public void testAddNode()
    {
        project.addElement(node);
        component.addNode(node);
        
        ETList<INode> elems = component.getNodes();
        assertNotNull(elems);
        
        INode nodeGot = null;
        if (elems != null)
        {
            for (int i=0;i<elems.size();i++)
            {
                nodeGot = elems.get(i);
            }
        }
        assertNotNull(nodeGot);
        assertEquals(node.getXMIID(), nodeGot.getXMIID());
    }
    
    public void testAddInternalClassifier()
    {
        project.addElement(part);
        component.addInternalClassifier(part);
        ETList<IPart> elems = component.getInternalClassifiers();
        assertNotNull(elems);
        
        IPart partGot = null;
        if (elems != null)
        {
            for (int i=0;i<elems.size();i++)
            {
                partGot = elems.get(i);
            }
        }
        assertNotNull(partGot);
        assertEquals(part.getXMIID(), partGot.getXMIID());
    }
    
    public void testAddInternalConnector()
    {
        project.addElement(connector);
        component.addInternalConnector(connector);
        ETList<IConnector> elems = component.getInternalConnectors();
        assertNotNull(elems);
        
        IConnector connectorGot = null;
        if (elems != null)
        {
            for (int i=0;i<elems.size();i++)
            {
                connectorGot = elems.get(i);
            }
        }
        assertNotNull(connectorGot);
        assertEquals(connector.getXMIID(), connectorGot.getXMIID());
    }
    
    public void testAddElementImport()
    {
        project.addElement(impor);
        component.addElementImport(impor);
        ETList<IElementImport> elems = component.getElementImports();
        assertNotNull(elems);
        
        IElementImport imporGot = null;
        if (elems != null)
        {
            for (int i=0;i<elems.size();i++)
            {
                imporGot = elems.get(i);
            }
        }
        assertNotNull(imporGot);
        assertEquals(impor.getXMIID(), imporGot.getXMIID());
    }
    
    public void testSetSpecifyingStateMachine()
    {
        IStateMachine mach = factory.createStateMachine(null);
        assertNotNull(mach);
        project.addElement(mach);
        component.setSpecifyingStateMachine(mach);
        assertNotNull(component.getSpecifyingStateMachine());
        assertEquals(component.getSpecifyingStateMachine().getXMIID(), mach.getXMIID());
    }
    
    public void testAddExternalInterface()
    {
        project.addElement(port);
        component.addExternalInterface(port);
        ETList<IPort> elems = component.getExternalInterfaces();
        assertNotNull(elems);
        
        IPort portGot = null;
        if (elems != null)
        {
            for (int i=0;i<elems.size();i++)
            {
                portGot = elems.get(i);
            }
        }
        assertNotNull(portGot);
        assertEquals(port.getXMIID(), portGot.getXMIID());
    }
    
    public void testInstantiation()
    {
        int inst = 1;
        component.setInstantiation(1);
        assertEquals(inst, component.getInstantiation());
    }
    
    public void testAddDeploymentSpecification()
    {
        project.addElement(spec);
        component.addDeploymentSpecification(spec);
        ETList<IDeploymentSpecification> elems =
            component.getDeploymentSpecifications();
        assertNotNull(elems);
        
        IDeploymentSpecification specGot = null;
        if (elems != null)
        {
            for (int i=0;i<elems.size();i++)
            {
                specGot = elems.get(i);
            }
        }
        assertEquals(spec.getXMIID(), specGot.getXMIID());
    }
    
    public void testAddAssembly()
    {
        IComponentAssembly assembly = (IComponentAssembly)FactoryRetriever.instance().createType("ComponentAssembly", null);
        //assembly.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(assembly);
        component.addAssembly(assembly);
        
        assertEquals(1, component.getAssemblies().size());
        assertEquals(assembly.getXMIID(), component.getAssemblies().get(0).getXMIID());
        
        component.removeAssembly(assembly);
        assertTrue(component.getAssemblies() == null
            || component.getAssemblies().size() == 0);
    }
    
    public void testRemoveArtifact()
    {
        component.removeArtifact(arti);
        ETList<IArtifact> elems = component.getArtifacts();
        assertTrue(elems == null || elems.size() == 0);
    }
    public void testRemoveDeploymentSpecification()
    {
        component.removeDeploymentSpecification(spec);
        ETList<IDeploymentSpecification> elems =
            component.getDeploymentSpecifications();
        assertTrue(elems == null || elems.size() == 0);
    }
    
    public void testRemoveElementImport()
    {
        component.removeElementImport(impor);
        ETList<IElementImport> elems = component.getElementImports();
        assertTrue(elems == null || elems.size() == 0);
    }
    
    public void testRemoveExternalInterface()
    {
        component.removeExternalInterface(port);
        ETList<IPort> elems = component.getExternalInterfaces();
        assertTrue(elems == null || elems.size() == 0);
    }
    
    public void testRemoveInternalClassifier()
    {
        component.removeInternalClassifier(part);
        ETList<IPart> elems = component.getInternalClassifiers();
        assertTrue(elems == null || elems.size() == 0);
    }
    
    public void testRemoveNode()
    {
        component.removeNode(node);
        ETList<INode> elems = component.getNodes();
        assertTrue(elems == null || elems.size() == 0);
    }
    
    public void testRemoveInternalConnector()
    {
        component.removeInternalConnector(connector);
        ETList<IConnector> elems = component.getInternalConnectors();
        assertTrue(elems == null || elems.size() == 0);
    }
}


