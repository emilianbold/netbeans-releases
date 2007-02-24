package org.netbeans.modules.uml.core.reverseengineering.reframework;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.TestUtils;
import org.netbeans.modules.uml.core.reverseengineering.reframework.CompositeClassLocator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.FileSystemClassLocator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ICompositeClassLocator;

/**
 * Test cases for CompositeClassLocator.
 */
public class CompositeClassLocatorTestCase extends TestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(CompositeClassLocatorTestCase.class);
    }

    private ICompositeClassLocator ccl;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        ccl = new CompositeClassLocator();
        FileSystemClassLocator a = new FileSystemClassLocator();
        FileSystemClassLocator b = new FileSystemClassLocator();
        a.addBaseDirectory(".");
        File base = new File("base");
        base.mkdir();
        b.addBaseDirectory(base.toString());
        
        ccl.addLocator(a);
        ccl.addLocator(b);
    }

    public void testLocateFile()
    {
        TestUtils.writeFile(null, null);
        TestUtils.writeFile("base/Yz.java", null);
        
        try
        {
            assertEquals(new File("Xyz.java").getAbsoluteFile().getCanonicalPath(), 
                    ccl.locateFile("Xyz"));
         
            // This should be found by the second locator.
            assertEquals(new File("base/Yz.java").getAbsoluteFile().getCanonicalPath(),
                    ccl.locateFile("Yz"));
            
            assertEquals(new File("base/Yz.java").getAbsoluteFile().getCanonicalPath(),
                    ccl.locateFile("base::Yz"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}