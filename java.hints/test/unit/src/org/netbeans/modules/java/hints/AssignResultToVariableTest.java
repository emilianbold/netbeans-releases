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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.hints;

import org.netbeans.modules.java.hints.AssignResultToVariable;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class AssignResultToVariableTest extends NbTestCase {
    
    public AssignResultToVariableTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }
    
    public void testNameGuess1() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {toString();}}", 54, "toString");
    }

    public void testNameGuess2() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getX();} public int getX() {return 0;}}", 54, "x");
    }
    
    public void testNameGuess3() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getData();} public int getData() {return 0;}}", 54, "data");
    }
    
    public void testNameGuess4() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getProcessedData();} public int getProcessedData() {return 0;}}", 54, "processedData");
    }
    
    public void testNameGuess5() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {isEnabled();} public boolean isEnabled() {return true;}}", 54, "enabled");
    }
    
    public void testNameGuess6() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {get();} public int get() {return 0;}}", 52, "get");
    }
    
    public void testDoNothingForVoidReturnType() throws Exception {
        performTestAnalysisTest("package test; public class Test {public void t() {get();} public void get() {}}", 51, Collections.<String>emptyList());
    }
    
    public void testProposeHint() throws Exception {
        performTestAnalysisTest("package test; public class Test {public void t() {get();} public int get() {}}", 51, Collections.<String>singletonList("0:50-0:53:hint:Assign Return Value To New Variable"));
    }
    
    protected void prepareTest(String code) throws Exception {
        FileObject workFO = makeScratchDir(this);
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        FileObject data = FileUtil.createData(sourceRoot, "test/Test.java");
        
        writeIntoFile(data, code);
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        Document doc = ec.openDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        
        JavaSource js = JavaSource.forFileObject(data);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    private CompilationInfo info;
    
    private void performNameGuessTest(String code, int position, String desiredName) throws Exception {
        prepareTest(code);
        
        TreePath tp = info.getTreeUtilities().pathFor(position);
        
        String name = AssignResultToVariable.guessName(info, tp);
        
        assertEquals(desiredName, name);
    }
    
    private void performTestAnalysisTest(String code, int offset, List<String> golden) throws Exception {
        prepareTest(code);
        
        final AssignResultToVariable artv = new AssignResultToVariable();
        final List<ErrorDescription> errors = new ArrayList<ErrorDescription>();
        
        Method m = CaretAwareJavaSourceTaskFactory.class.getDeclaredMethod("setLastPosition", FileObject.class, int.class);
        
        assertNotNull(m);
        
        m.setAccessible(true);
        
        m.invoke(null, new Object[] {info.getFileObject(), offset});
        
        class ScannerImpl extends TreePathScanner {
            @Override
            public Object scan(Tree tree, Object p) {
                if (tree != null && artv.getTreeKinds().contains(tree.getKind())) {
                    List<ErrorDescription> localErrors = artv.run(info, new TreePath(getCurrentPath(), tree));
                    
                    if (localErrors != null) {
                        errors.addAll(localErrors);
                    }
                }
                return super.scan(tree, p);
            }
        };
        
        new ScannerImpl().scan(info.getCompilationUnit(), null);
        
        List<String> errorDisplaNames = new ArrayList<String>();
        
        for (ErrorDescription ed : errors) {
            errorDisplaNames.add(ed.toString());
        }
        
        assertEquals(golden, errorDisplaNames);
    }
    
    /**Copied from org.netbeans.api.project.
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            // Presumably using masterfs.
            return fo;
        } else {
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }
    
    private void writeIntoFile(FileObject file, String what) throws Exception {
        FileLock lock = file.lock();
        OutputStream out = file.getOutputStream(lock);
        
        try {
            out.write(what.getBytes());
        } finally {
            out.close();
            lock.releaseLock();
        }
    }
}
