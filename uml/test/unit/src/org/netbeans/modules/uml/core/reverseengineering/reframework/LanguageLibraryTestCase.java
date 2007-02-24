package org.netbeans.modules.uml.core.reverseengineering.reframework;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.ModuleUnitTestSuiteBuilder;
import java.io.File;

/**
 * Test cases for LanguageLibrary.
 */
public class LanguageLibraryTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(LanguageLibraryTestCase.class);
    }

    private static ILanguageLibrary ll = new LanguageLibrary();
    
    static
    {
        
        ll.setIndex(ModuleUnitTestSuiteBuilder.tempDotUmlDirName 
            + File.separator + "config" + File.separator // NOI18N
            + "Libraries" + File.separator + "Java16.index"); // NOI18N
        
        ll.setLookupFile(ModuleUnitTestSuiteBuilder.tempDotUmlDirName
            + File.separator + "config" + File.separator // NOI18N
            + "Libraries" + File.separator + "Java16.etd"); // NOI18N
    }

    public void testFindClass()
    {
        assertEquals("String", ll.findClass("String").getName()); // NOI18N
        assertEquals("ArrayList",  // NOI18N
                ll.findClass("java::util::ArrayList").getName()); // NOI18N
    }
}
