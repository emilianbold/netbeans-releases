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

import com.sun.source.tree.BlockTree;
import com.sun.source.util.TreePath;
import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class TreeUtilitiesTest extends NbTestCase {
    
    public TreeUtilitiesTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }
    
    private CompilationInfo info;
    
    private void prepareTest(String filename, String code) throws Exception {
        File work = TestUtil.createWorkFolder();
        FileObject workFO = FileUtil.toFileObject(work);
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        FileObject packageRoot = sourceRoot.createFolder("test");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        FileObject testSource = packageRoot.createData(filename + ".java");
        
        assertNotNull(testSource);
        
        TestUtilities.copyStringToFile(FileUtil.toFile(testSource), code);
        
        JavaSource js = JavaSource.forFileObject(testSource);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, JavaSource.Phase.RESOLVED);
        
        assertNotNull(info);
    }

    public void testIsSynthetic1() throws Exception {
        prepareTest("Test", "package test; public class Test {public Test(){}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(47);
        BlockTree bt = (BlockTree) tp.getLeaf();
        
        tp = new TreePath(tp, bt.getStatements().get(0));
        
        assertTrue(info.getTreeUtilities().isSynthetic(tp));
    }
    
    public void testIsSynthetic2() throws Exception {
        prepareTest("Test", "package test; public class Test {public Test(){super();}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(47);
        BlockTree bt = (BlockTree) tp.getLeaf();
        
        tp = new TreePath(tp, bt.getStatements().get(0));
        
        assertFalse(info.getTreeUtilities().isSynthetic(tp));
    }
}
