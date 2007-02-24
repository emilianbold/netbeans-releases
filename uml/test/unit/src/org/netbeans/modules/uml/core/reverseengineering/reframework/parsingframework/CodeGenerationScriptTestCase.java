package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.ModuleUnitTestSuiteBuilder;
import java.io.File;
import java.io.IOException;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;

/**
 * Test cases for CodeGenerationScript.
 */
public class CodeGenerationScriptTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(CodeGenerationScriptTestCase.class);
    }

    private ICodeGenerationScript cgs;
    private IClassifier           c;
    private IAttribute            attr;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        cgs = new CodeGenerationScript();
        
        c = createClass("NY");
        assertNotNull(c.getNode());
        assertNotNull(c.getNode().getDocument());
        assertNotNull( attr = c.createAttribute("int", "harlem") );
        c.addAttribute(attr);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        attr.delete();
        c.delete();
        super.tearDown();
    }

    public void testExecute()
    {
        cgs.setLanguage(c.getLanguages().get(0));
        // String location = ProductHelper.getConfigManager()
        //    .getDefaultConfigLocation();
        
        String location = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
            + File.separator + "config" + File.separator; // NOI18N

		if ((location != null) && (location.length() > 0))
		{
		  String addinFile = "";
		  File file = new File(location);
		  File parent = file.getParentFile();
		  if (parent != null)
		  {
                try
                {
                    addinFile = parent.getCanonicalPath() + File.separator
                            + "scripts";
                } 
                catch (IOException e)
                {
				e.printStackTrace();
			}
		  }

            String gtPath = new File(addinFile + "/java/java_attribute.gt")
                    .getAbsolutePath();
            cgs.setFile(gtPath);
		  cgs.setName("java_attribute.gt");
		}
 
        assertEquals("private int harlem;", cgs.execute(attr));
    }
}