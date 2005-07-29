/*
 * LibraryStartVisualPanelTest.java
 * JUnit based test
 *
 * Created on July 28, 2005, 11:42 AM
 */

package org.netbeans.modules.apisupport.project.ui.wizard;

import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.openide.WizardDescriptor;

/**
 *
 * @author mkleint
 */
public class LibraryStartVisualPanelTest extends NbTestCase {
    
    protected static String EEP = "apisupport/project/test/unit/data/test-library-0.1_01.jar";
    private File libraryPath = null;
    public LibraryStartVisualPanelTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(LibraryStartVisualPanelTest.class);
        
        return suite;
    }

    protected void setUp() throws Exception {
        super.setUp();
        File nbrootF = new File(System.getProperty("test.nbroot"));
        libraryPath = new File(nbrootF, EEP);
        assertTrue("librarypath exists.", libraryPath.exists());
    }
    
    /**
     * Test of populateProjectData method, of class org.netbeans.modules.apisupport.project.ui.wizard.LibraryStartVisualPanel.
     */
    public void testPopulateProjectData() {
        System.out.println("populateProjectData");
        
        NewModuleProjectData data = new NewModuleProjectData();
        
        String paths = libraryPath.getAbsolutePath();
        
        LibraryStartVisualPanel.populateProjectData(data, paths);
        assertEquals("test-library", data.getProjectName());
        assertEquals("org.apache.commons.logging", data.getCodeNameBase());
    }
    
}
