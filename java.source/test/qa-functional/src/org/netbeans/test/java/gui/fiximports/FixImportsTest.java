 /*
  * The contents of this file are subject to the terms of the Common Development
  * and Distribution License (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
  * or http://www.netbeans.org/cddl.txt.
  *
  * When distributing Covered Code, include this CDDL Header Notice in each file
  * and include the License file at http://www.netbeans.org/cddl.txt.
  * If applicable, add the following below the CDDL Header, with the fields
  * enclosed by brackets [] replaced by your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  * Microsystems, Inc. All Rights Reserved.
  */

package org.netbeans.test.java.gui.fiximports;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import javax.swing.ComboBoxModel;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.java.FixAllImports;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.java.Utilities;


/**
 * Tests Fix Imports.
 * @author Roman Strobl
 */
public class FixImportsTest extends JellyTestCase {
    
    // name of sample project
    private static final String TEST_PROJECT_NAME = "default";
    
    // path to sample files
    private static final String TEST_PACKAGE_PATH =
            "org.netbeans.test.java.gui.fiximports";
    
    // name of sample package
    private static final String TEST_PACKAGE_NAME = TEST_PACKAGE_PATH+".test";
    
    // name of sample class
    private static final String TEST_CLASS_NAME = "TestClass";
    
    /**
     * error log
     */
    protected static PrintStream err;
    
    /**
     * standard log
     */
    protected static PrintStream log;
    
    // workdir, default /tmp, changed to NBJUnit workdir during test
    private String workDir = "/tmp";
    
    // actual directory with project
    private static String projectDir;
    
    /**
     * Adds tests into the test suite.
     * @return suite
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new FixImportsTest("testFixImports"));
        suite.addTest(new FixImportsTest("testFixImportsComplex"));
        suite.addTest(new FixImportsTest("testCancel"));
        suite.addTest(new FixImportsTest("testNothingToFix"));
        suite.addTest(new FixImportsTest("testRemoveAndAdd"));
        suite.addTest(new FixImportsTest("testRemoveUnused"));
        suite.addTest(new FixImportsTest("testStatusBar"));
        suite.addTest(new FixImportsTest("testCheckboxPersistence"));
        return suite;
    }
    
    /**
     * Main method for standalone execution.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /**
     * Sets up logging facilities.
     */
    @Override
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        err = getLog();
        log = getRef();
        JemmyProperties.getProperties().setOutput(new TestOut(null,
                new PrintWriter(err, true), new PrintWriter(err, false), null));
        try {
            File wd = getWorkDir();
            workDir = wd.toString();
        } catch (IOException e) { }
    }
    
    /**
     * Creates a new instance of JavaElementsTest
     * @param testName name of test
     */
    public FixImportsTest(String testName) {
        super(testName);
    }
    
    /**
     * Fix imports test.
     */
    public void testFixImports() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_CLASS_NAME);
        
        n.select();
        new OpenAction().perform();
        
        // test fix imports on Vector
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        editor.insert("Vector v = new Vector();\n", 15, 1);
        Utilities.takeANap(100);
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);        
        //FixAllImports fio = new FixAllImports();
        //fio.ok();
        // wait for fix imports
        for (int i=0; i<10; i++) {
            Utilities.takeANap(1000);
            if(editor.getText().contains("import java.util.Vector;")) break;
            System.out.println(MainWindowOperator.getDefault().getStatusText());
        }
        
        ref(editor.getText());
        
        compareReferenceFiles();
    }
    
    /**
     * Complex fix imports test.
     */
    public void testFixImportsComplex() {
        // test fix imports on List
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        editor.insert("List l = new List();\n", 17, 1);
        
        Utilities.takeANap(100);
        
        // invoke fix imports
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);        
        FixAllImports fio = new FixAllImports();
        
        JComboBoxOperator cbo = fio.cbo(0);
        ComboBoxModel cbm = cbo.getModel();
        int in;
        for(in=0;in<cbm.getSize();in++) {
            if(cbm.getElementAt(in).equals("java.util.List")) {
                cbo.setSelectedIndex(in);
                break;
            }
        }
        assertTrue("java.util.List not listed",in<cbm.getSize());
        fio.ok();
        // wait for fix imports
        for (int i=0; i<10; i++) {
            Utilities.takeANap(1000);
            if (editor.getText().contains("import java.util.List;")) break;
            //System.out.println(MainWindowOperator.getDefault().getStatusText());
        }
        ref(editor.getText());
        compareReferenceFiles();
        editor.close(false);
    }
    
    public void testRemoveUnused() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_CLASS_NAME);
        n.select();
        new OpenAction().perform();
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        try {
            editor.insert("import java.util.Date;\n", 7, 1);
            Utilities.takeANap(100);
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);                      
            assertFalse("Import is not removed",editor.getText().contains("import java.util.Date;"));                        
            editor.insert("List l;\n", 12, 1);
            editor.insert("import java.util.Date;\n", 7, 1);            
            Utilities.takeANap(100);
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);          
            FixAllImports fio = new FixAllImports();
            fio.cbRemoveUnusedImports().setSelected(false);
            fio.ok();
            assertTrue("Import is removed",editor.getText().contains("import java.util.Date;"));
        } finally {
            editor.close(false);
        }
    }
    
    public void testRemoveAndAdd() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_CLASS_NAME);
        n.select();
        new OpenAction().perform();
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        try {
            editor.insert("import java.util.Date;\n", 7, 1);
            editor.insert("List m;\n", 16, 1);
            Utilities.takeANap(100);
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
            
            FixAllImports fio = new FixAllImports();
            fio.cbRemoveUnusedImports().setSelected(true);
            fio.ok();
            assertFalse("Import is not removed",editor.getText().contains("import java.util.Date;"));
            assertTrue("Import is not added",editor.getText().contains("import java.util.List;"));
        } finally {
            editor.close(false);
        }
        
    }
    
    public void testNothingToFix() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_CLASS_NAME);
        n.select();
        new OpenAction().perform();
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        try {
            Utilities.takeANap(100);
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);          
            Utilities.takeANap(200);
            assertEquals(MainWindowOperator.getDefault().getStatusText(),"Nothing to fix in import statements.");            
        } finally {
            editor.close(false);
        }
        
    }
    
    public void testCancel() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_CLASS_NAME);
        n.select();
        new OpenAction().perform();
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        try {
            editor.insert("List m;\n", 15, 1);
            Utilities.takeANap(100);
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);            
            FixAllImports fio = new FixAllImports();
            fio.cancel();
            assertFalse("Import is added",editor.getText().contains("import java.util.List;"));
        } finally {
            editor.close(false);
        }
    }
    
    public void testCheckboxPersistence() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_CLASS_NAME);
        n.select();
        new OpenAction().perform();
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        try {
            editor.insert("List m;\n", 15, 1);
            Utilities.takeANap(100);
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);            
            FixAllImports fio = new FixAllImports();
            fio.cbRemoveUnusedImports().setSelected(false);
            fio.cancel();
            Utilities.takeANap(100);            
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);            
            fio = new FixAllImports();
            assertEquals("Checkbox state is not persistent",fio.cbRemoveUnusedImports().isSelected(),false);
            fio.cancel();
        } finally {
            editor.close(false);
        }
    }
    
    public void testStatusBar() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_CLASS_NAME);
        n.select();
        new OpenAction().perform();
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        try {
            Utilities.takeANap(100);
            editor.insert("import java.util.Date;\n", 7, 1);
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);            
            Utilities.takeANap(200);
            assertEquals(MainWindowOperator.getDefault().getStatusText(),"Unused imports were removed.");            
        } finally {
            editor.close(false);
        }
    }
           
}
