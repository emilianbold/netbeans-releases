package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IFileInformation;

/**
 * Test cases for ClassLoaderListener.
 */
public class ClassLoaderListenerTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
         junit.textui.TestRunner.run(ClassLoaderListenerTestCase.class);
    }

    private IClassLoaderListener cll;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        cll = new ClassLoaderListener();

        IUMLParser parser = new LanguageFacilityFactory().getUMLParser();
        IUMLParserEventDispatcher disp = parser.getUMLParserDispatcher();
        String fileName = "Xyz.java";

        writeFile("Xyz.java", "public class Xyz { int x = 0, y = 1; " +
                "int z = x + y;  " +
                "int[] zigzag = new int[30]; " +
                "char tantalum = getChar(); " +
                "public void test() { }  " +
                "public char getChar() { return 'a'; } " +
                "} " +
                "class Other { }");

        if (disp != null)
        {
            disp.registerForUMLParserEvents(cll, fileName);
            parser.processStreamFromFile(fileName);
            disp.revokeUMLParserSink(cll);
        }
    }

    public void testGetFileInformation()
    {
        IFileInformation fi = cll.getFileInformation();
        assertNotNull(fi);
        assertEquals(2, fi.getTotalClasses());
    }

    public void testGetTopLevelAttributes()
    {
        assertEquals(0, cll.getTopLevelAttributes().size());
    }

    public void testGetTopLevelOperations()
    {
        assertEquals(0, cll.getTopLevelOperations().size());
    }
}