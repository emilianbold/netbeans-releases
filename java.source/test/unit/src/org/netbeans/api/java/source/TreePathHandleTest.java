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

package org.netbeans.api.java.source;

import com.sun.source.util.TreePath;
import java.io.File;
import java.io.OutputStream;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class TreePathHandleTest extends NbTestCase {
    
    public TreePathHandleTest(String testName) {
        super(testName);
    }
    
    private FileObject sourceRoot;
    
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        
        File work = FileUtil.normalizeFile(TestUtil.createWorkFolder());
        FileObject workFO = FileUtil.toFileObject(work);
        
        assertNotNull(workFO);
        
        sourceRoot = workFO.createFolder("src");
        
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
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

    public void testHandleForMethodInvocation() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/test.java");
        
        writeIntoFile(file, "package test; public class test {public test() {aaa();} public void aaa() {}}");
        
        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        assertTrue(info.getDiagnostics().toString(), info.getDiagnostics().isEmpty());
        
        TreePath       tp       = info.getTreeUtilities().pathFor(49);
        TreePathHandle handle   = TreePathHandle.create(tp, info);
        TreePath       resolved = handle.resolve(info);
        
        assertNotNull(resolved);
        
        assertTrue(tp.getLeaf() == resolved.getLeaf());
    }
        
    public void testHandleForNewClass() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/test.java");
        
        writeIntoFile(file, "package test; public class test {public test() {new Runnable() {public void run() {}};}}");
        
        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        assertTrue(info.getDiagnostics().toString(), info.getDiagnostics().isEmpty());
        
        TreePath       tp       = info.getTreeUtilities().pathFor(55).getParentPath();
        TreePathHandle handle   = TreePathHandle.create(tp, info);
        TreePath       resolved = handle.resolve(info);
        
        assertNotNull(resolved);
        
        assertTrue(tp.getLeaf() == resolved.getLeaf());
    }
    
}
