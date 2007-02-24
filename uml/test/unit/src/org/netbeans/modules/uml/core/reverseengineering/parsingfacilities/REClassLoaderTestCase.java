package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.reverseengineering.reframework.FileSystemClassLocator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.LanguageLibrary;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * Test cases for REClassLoader.
 */
public class REClassLoaderTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REClassLoaderTestCase.class);
    }
    
    private IREClassLoader recl;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        recl = new REClassLoader();
        
        FileSystemClassLocator fsl = new FileSystemClassLocator();
        fsl.addBaseDirectory(".");
        
        recl.setClassLocator(fsl);
        writeFile("Xyz.java", "public class Xyz { } class Other { }");
    }
    
    public void testLoadClass()
    {
        IREClass c = recl.loadClass("Xyz");
        assertEquals("Xyz", c.getName());
    }
    
    public void testLoadClassFromFile()
    {
        IREClass c = recl.loadClassFromFile("Xyz.java", "Xyz");
        assertEquals("Xyz", c.getName());
    }
    
    public void testLoadClassesFromFile()
    {
        ETList<IREClass> c = recl.loadClassesFromFile("Xyz.java");
        assertEquals(2, c.size());
        assertEquals("Xyz", c.get(0).getName());
        assertEquals("Other", c.get(1).getName());
    }
    
    public void testGetErrorInFile()
    {
        writeFile("Xyz.java", "public class Xyz { } A class Other { }");
        recl.loadFile("Xyz.java");
        ETList<IErrorEvent> errors = recl.getErrorInFile("Xyz.java");
        assertTrue(errors.size() > 0);
    }
    
    public void testLoadFile()
    {
        writeFile("Xyz.java", "public class Xyz { } class Other { } " +
            "class Third { }");
        recl.loadFile("Xyz.java");
        assertEquals(3, recl.getLoadedClasses().size());
        ETList<IErrorEvent> errors = recl.getErrorInFile("Xyz.java");
        assertTrue(errors.size() == 0);
    }
    
    public void testAddLibrary()
    {
        LanguageLibrary ll = new LanguageLibrary();
        ll.setIndex("../config/Libraries/Java16.index");
        ll.setLookupFile("../config/Libraries/Java16.etd");
        
        recl.addLibrary(ll);
        
        IREClass c = recl.loadClass("java::lang::String");
        assertNotNull(c);
        assertEquals("String", c.getName());
    }
    
    public void testGetLoadedClasses()
    {
        // Tested by testLoadFile
    }
}