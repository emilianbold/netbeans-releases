package org.netbeans.modules.uml.core.metamodel.structure;

import java.io.File;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.eventframework.EventDispatchController;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 *
 */
public class ArtifactTestCase extends AbstractUMLTestCase
{
    private IArtifact arti = null;
    private TestArtifactEventsListener m_artifactListener =
        new TestArtifactEventsListener();
    private IStructureEventDispatcher m_Dispatcher = null;
    
    public static boolean callingPreModified = false;
    public static boolean callingModified = false;
    private IDeployment depAdded = factory.createDeployment(null);
    
    public ArtifactTestCase()
    {
        super();
    }
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(ArtifactTestCase.class);
    }
    
    protected void setUp() throws Exception
    {
        arti = factory.createArtifact(null);
        project.addElement(arti);
        
        EventDispatchRetriever ret = EventDispatchRetriever.instance();
        IEventDispatchController cont = ret.getController();
        if (cont == null)
        {
            cont = new EventDispatchController();
        }
        m_Dispatcher =  (IStructureEventDispatcher)
        cont.retrieveDispatcher(EventDispatchNameKeeper.EDT_STRUCTURE_KIND);
        if (m_Dispatcher == null)
        {
            m_Dispatcher =  new StructureEventDispatcher();
            cont.addDispatcher(EventDispatchNameKeeper.EDT_STRUCTURE_KIND,
                m_Dispatcher);
        }
        m_Dispatcher.registerForArtifactEvents(m_artifactListener);
        ret.setController(cont);
    }
    
    protected void tearDown() throws Exception
    {
        super.tearDown();
        m_Dispatcher.revokeArtifactSink(m_artifactListener);
    }
    
    public void testAddDeployment()
    {
        assertNotNull(arti);
        
        project.addElement(depAdded);
        arti.addDeployment(depAdded);
        
        ETList<IDeployment> deps = arti.getDeployments();
        assertNotNull(deps);
        
        IDeployment depGot = null;
        if (deps != null)
        {
            for (int i=0;i<deps.size();i++)
            {
                depGot = deps.get(i);
            }
        }
        assertEquals(depAdded.getXMIID(), depGot.getXMIID());
    }
    
    public void testSetContent()
    {
        assertNotNull(arti);
        IDeploymentSpecification spec = factory.createDeploymentSpecification(null);
        arti.setContent(spec);
        project.addElement(spec);
        IDeploymentSpecification spec1 = arti.getContent();
        assertEquals(spec.getXMIID(),spec1.getXMIID());
    }
    
    public void testAddImplementedElement()
    {
        //NamedElement creation
    }
    
//	public void testSetFileName()
//	{
//		String str = "test/A/NewFile.java";
//        String absPath = new File(str).getAbsolutePath();
//        writeFile(absPath, "public class NewFile{}");
//        arti.setFileName(absPath);
//		String newStr = arti.getFileName();
//		assertTrue(ArtifactTestCase.callingPreModified);
//		assertNotNull(newStr);
//	    assertEquals(new File(str).getAbsolutePath(),newStr);
//	}
    
    public void testGetBaseDir()
    {
        Artifact fac = (Artifact)arti;
        String sourceFile = new File("c", "D.java").getAbsolutePath();
        String qualifiedName = "c::D";
        String baseD = fac.getBaseDir(sourceFile,qualifiedName);
        assertEquals(new File(sourceFile).getParentFile().getParent(), baseD);
    }
    
    public void testRemoveDeployment()
    {
        arti.removeDeployment(depAdded);
        ETList<IDeployment> deps = arti.getDeployments();
        assertTrue(deps == null || deps.size() == 0);
    }
}

