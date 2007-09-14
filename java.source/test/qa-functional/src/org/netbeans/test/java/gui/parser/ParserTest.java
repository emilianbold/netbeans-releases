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

package org.netbeans.test.java.gui.parser;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.event.KeyEvent;
import org.netbeans.junit.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jellytools.actions.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests Java Parser.
 * @author Roman Strobl, Jiri Prox
 */
public class ParserTest extends JellyTestCase {
    
    /** Switch to generate golden files */
    private boolean generateGoledenFiles = false;
    
    // name of sample project
    private static final String TEST_PROJECT_NAME = "default";
    
    // name of sample package
    private static String testPackageName;
    
    // name of sample class
    private static String testClassName;
    
    
    // workdir, default /tmp, changed to NBJUnit workdir during test
    private String workDir = "/tmp";
    
    private static String projectDir;
    
    private File classPathWorkDir;
    
    private FileObject artefact;
    
    private FileObject testFileObject;
    
    /**
     * Adds tests into the test suite.
     * @return suite
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ParserTest("testCreateInnerClass"));
        suite.addTest(new ParserTest("testCreateInnerInterface"));
        suite.addTest(new ParserTest("testCreateConstructor"));
        suite.addTest(new ParserTest("testCreateField"));
        suite.addTest(new ParserTest("testCreateMethod"));
        suite.addTest(new ParserTest("testCreateOuterClass"));
        suite.addTest(new ParserTest("testCreateOuterInterface"));
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
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        try {
            File wd = getWorkDir();
            workDir = wd.toString();
        } catch (IOException e) { }
        classPathWorkDir=new File(getDataDir(), ("projects."+TEST_PROJECT_NAME+".src").replace('.', File.separatorChar));
        artefact=FileUtil.toFileObject(classPathWorkDir);
        testPackageName = getClass().getPackage().getName()+"."+getClass().getSimpleName();
        testClassName = getName();
        getLog().println("testPackage="+testPackageName+" testClass="+testClassName);
        testFileObject = artefact.getFileObject((testPackageName + "." + testClassName).replace(".","/")+".java");
    }
    
    public void tearDown() {
        System.out.println("########  "+getName()+"  #######");
        if(generateGoledenFiles) fail("Passive mode");
    }
    
    /**
     * Creates a new instance of ParserTest
     * @param testName name of test
     */
    public ParserTest(String testName) {
        super(testName);
    }
    
    private void dumpStructure(JavaSource js,final PrintStream ps,final String text) throws IOException {
        CancellableTask task = new CancellableTask<CompilationController>() {
            ElementVisitor scanner;
            public void cancel() {
                scanner.cancel();
            }
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                CompilationUnitTree cuTree = parameter.getCompilationUnit();
                List<? extends Tree> typeDecls = cuTree.getTypeDecls();
                TreePath cuPath = new TreePath( cuTree );
                List<Element> elements = new ArrayList<Element>( typeDecls.size() );
                for( Tree t : typeDecls ) {
                    TreePath p = new TreePath( cuPath, t );
                    Element e = parameter.getTrees().getElement(p);
                    if ( e != null ) {
                        elements.add( e );
                    }
                }
                scanner = new ElementVisitor(parameter,text);
                for (Element element : elements) {
                    scanner.scan(element,ps);
                }
            }
            
        };
        js.runModificationTask(task).commit();
    }
    
    public File getRefFile(int n) throws IOException {
        File f = new File(getWorkDir(),getName()+n+".ref");
        return f;
    }
    
    public File getGoldenFile(int n) {
        return super.getGoldenFile(getName()+n+".pass");
    }
    
    public File getNewGoldenFile(int n) {
        String fileName = "data/goldenfiles/"+testPackageName.replace('.', '/')+ "/" + testClassName + n +  ".pass";
        File f = new File(getDataDir().getParentFile().getParentFile().getParentFile(),fileName);
        f.getParentFile().mkdirs();
        return f;
    }
    
    public void compareGoldenFile(int n) {
        if(generateGoledenFiles) {
            try {
                File nGolden = getNewGoldenFile(n);
                File ref = getRefFile(n);
                BufferedReader br = new BufferedReader(new FileReader(ref));
                FileWriter fw = new FileWriter(nGolden);
                String line;
                while((line=br.readLine())!=null) fw.write(line+"\n");
                fw.close();
                br.close();
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        } else {
            try {
                assertFile(getRefFile(n),getGoldenFile(n), new File(getWorkDir(),getName()+n+".diff"));
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }
    
    
    
    private EditorOperator getDefaultEditor() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+testPackageName+"|"
                +testClassName);
        
        n.select();
        new OpenAction().perform();
        
        EditorOperator editor = new EditorOperator(testClassName);
        return editor;
    }
    
    private void save() {
        SaveAction saveAction = new SaveAction();
        MainWindowOperator.getDefault().menuBar().showMenuItem(saveAction.getMenuPath());
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        saveAction.perform();
    }
    
    /**
     * Tests parsing of inner class.
     */
    public void testCreateInnerClass() throws IOException {
        EditorOperator editor = getDefaultEditor();
        editor.insert("    public class InnerClass {\n        int a;\n    }\n", 18, 1);
        save();
        JavaSource js = JavaSource.forFileObject(testFileObject);
        FileWriter fw = new FileWriter(getRefFile(1));
        FileOutputStream fos = new FileOutputStream(getRefFile(1));
        PrintStream ps = new PrintStream(fos);
        dumpStructure(js,ps,editor.getText());
        compareGoldenFile(1);
    }
    
    
    /**
     * Tests parsing of inner interface.
     */
    public void testCreateInnerInterface() throws IOException {
        EditorOperator editor = getDefaultEditor();
        editor.insert("public interface Iface {\n        String method();\n }\n", 19, 1);
        save();
        JavaSource js = JavaSource.forFileObject(testFileObject);
        FileWriter fw = new FileWriter(getRefFile(1));
        FileOutputStream fos = new FileOutputStream(getRefFile(1));
        PrintStream ps = new PrintStream(fos);
        dumpStructure(js,ps,editor.getText());
        compareGoldenFile(1);
    }
    
    /**
     * Tests parsing of constructor.
     */
    public void testCreateConstructor() throws IOException {
        EditorOperator editor = getDefaultEditor();
        editor.insert("public testCreateConstructor(String param) { }\n", 20, 1);
        save();
        JavaSource js = JavaSource.forFileObject(testFileObject);
        FileWriter fw = new FileWriter(getRefFile(1));
        FileOutputStream fos = new FileOutputStream(getRefFile(1));
        PrintStream ps = new PrintStream(fos);
        dumpStructure(js,ps,editor.getText());
        compareGoldenFile(1);
        editor.deleteLine(20);
        fos = new FileOutputStream(getRefFile(2));
        ps = new PrintStream(fos);
        dumpStructure(js,ps,editor.getText());
        compareGoldenFile(2);
    }
    
    /**
     * Tests parsing of a field.
     */
    public void testCreateField() throws IOException {
        EditorOperator editor = getDefaultEditor();
        editor.insert("String field;\n", 21, 1);
        save();
        JavaSource js = JavaSource.forFileObject(testFileObject);
        FileWriter fw = new FileWriter(getRefFile(1));
        FileOutputStream fos = new FileOutputStream(getRefFile(1));
        PrintStream ps = new PrintStream(fos);
        dumpStructure(js,ps,editor.getText());
        compareGoldenFile(1);
        editor.deleteLine(21);
        fos = new FileOutputStream(getRefFile(2));
        ps = new PrintStream(fos);
        dumpStructure(js,ps,editor.getText());
        compareGoldenFile(2);
        
    }
    
    /**
     * Tests parsing of a method.
     */
    public void testCreateMethod() throws IOException {
        
        EditorOperator editor = getDefaultEditor();
        editor.insert("private int method(Object obj) { return 1; }\n", 22, 1);
        save();
        JavaSource js = JavaSource.forFileObject(testFileObject);
        FileWriter fw = new FileWriter(getRefFile(1));
        FileOutputStream fos = new FileOutputStream(getRefFile(1));
        PrintStream ps = new PrintStream(fos);
        dumpStructure(js,ps,editor.getText());
        compareGoldenFile(1);
        editor.deleteLine(22);
        fos = new FileOutputStream(getRefFile(2));
        ps = new PrintStream(fos);
        dumpStructure(js,ps,editor.getText());
        compareGoldenFile(2);
    }
    
    /**
     * Tests parsing of outer class.
     */
    public void testCreateOuterClass() throws IOException {
        
        EditorOperator editor = getDefaultEditor();
        editor.insert("class Outer { }\n", 23, 1);
        save();
        JavaSource js = JavaSource.forFileObject(testFileObject);
        FileWriter fw = new FileWriter(getRefFile(1));
        FileOutputStream fos = new FileOutputStream(getRefFile(1));
        PrintStream ps = new PrintStream(fos);
        dumpStructure(js,ps,editor.getText());
        compareGoldenFile(1);
        editor.deleteLine(23);
        fos = new FileOutputStream(getRefFile(2));
        ps = new PrintStream(fos);
        dumpStructure(js,ps,editor.getText());
        compareGoldenFile(2);
        
    }
    
    /**
     * Tests parsing of outer interface.
     */
    public void testCreateOuterInterface() throws IOException {
        
        EditorOperator editor = getDefaultEditor();
        editor.insert("interface Iface{ }\n", 24, 1);
        save();
        JavaSource js = JavaSource.forFileObject(testFileObject);
        FileWriter fw = new FileWriter(getRefFile(1));
        FileOutputStream fos = new FileOutputStream(getRefFile(1));
        PrintStream ps = new PrintStream(fos);
        dumpStructure(js,ps,editor.getText());
        compareGoldenFile(1);
        editor.deleteLine(23);
        fos = new FileOutputStream(getRefFile(2));
        ps = new PrintStream(fos);
        dumpStructure(js,ps,editor.getText());
        compareGoldenFile(2);
        
        
    }
    
    
}
