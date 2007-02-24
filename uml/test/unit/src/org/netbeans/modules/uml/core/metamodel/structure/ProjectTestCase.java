package org.netbeans.modules.uml.core.metamodel.structure;

import java.io.File;
import java.io.IOException;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.eventframework.EventDispatchController;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 *
 */
public class ProjectTestCase extends AbstractUMLTestCase
{
    //project is available from the super class.
    private static final String FILE_NAME = new File("AAAAA").getAbsolutePath();
    // Initialize the variable with relative path
    private static final String NEW_FILE_NAME = new File("test\\News\\News.etd").getAbsolutePath();
    
    private TestProjectEventsListener m_projectListener =
        new TestProjectEventsListener();
    private IStructureEventDispatcher m_Dispatcher = null;
    
    public static boolean callingModeModified = false;
    public static boolean callingPreModeModified = false;
    
    public ProjectTestCase()
    {
        super();
    }
    
    protected void setUp()
    {
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
        m_Dispatcher.registerForProjectEvents(m_projectListener);
        ret.setController(cont);
    }
    
    protected void tearDown()
    {
        m_Dispatcher.revokeProjectSink(m_projectListener);
    }
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(ProjectTestCase.class);
    }
    
    public void testSave()
    {
        project.setDirty(true);
        project.save(FILE_NAME,true);
        
        File file = new File(project.getFileName());
        assertTrue(file.exists());
        //assertEquals("A.etd",file.getName());
    }
    
    public void testGetBaseDirectory()
    {
        try
        {
            assertEquals(new File("test", "A").getAbsoluteFile(),
                new File(project.getBaseDirectory()).getCanonicalFile());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail();
        }
    }
    
	public void testSetFileName() {
		String tstr = project.getFileName();
		project.setFileName(NEW_FILE_NAME);
		assertEquals(NEW_FILE_NAME, project.getFileName());
		project.setFileName(tstr);
   }
    
    public void testGetDefaultLanguage()
    {
        String defLang = project.getDefaultLanguage();
        ILanguage lang = project.getDefaultLanguage2();
        assertEquals(defLang, lang.getName());
        assertEquals("Java", defLang);
    }
    
    public void testSetDirty()
    {
        project.setDirty(false);
        project.setChildrenDirty(false);
        assertFalse(project.isDirty());
    }
    
    public void testGetWSProject()
    {
        IWSProject wsProj = project.getWSProject();
        assertNotNull(wsProj);
        assertNotNull(project.getName());
        assertEquals(project.getName(), wsProj.getName());
    }
    
    //TODO: need to be completed
    public void testGetReferencedLibraries()
    {
        ETList<String> libs = project.getReferencedLibraries();
    }
    
    
    public void testSetMode()
    {
//		project.setMode("PSK_IMPLEMENTATION");
        project.setMode("Implementation");
        assertNotNull(project.getMode());
//		assertEquals("PSK_IMPLEMENTATION",project.getMode());
        assertEquals("Implementation",project.getMode());
        assertTrue(ProjectTestCase.callingModeModified);
        assertTrue(ProjectTestCase.callingPreModeModified);
    }
}


