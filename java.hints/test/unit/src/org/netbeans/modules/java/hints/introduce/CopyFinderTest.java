/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class CopyFinderTest extends NbTestCase {
    
    public CopyFinderTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }

    public void testSimple1() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; y = i + i; y = i + i;}}", 90 - 22, 95 - 22, 101 - 22, 106 - 22);
    }
    
//    public void testSimple2() throws Exception {
//        performTest("package test; public class Test {public void test() {int i = 0; y = i + i; y = i + i + i;}}", 90 - 22, 95 - 22, 101 - 22, 106 - 22);
//    }
    
    public void testSimple3() throws Exception {
        performTest("package test; public class Test {public void test() {int i = System.currentTimeMillis(); y = System.currentTimeMillis();}}", 83 - 22, 109 - 22, 115 - 22, 141 - 22);
    }
    
    public void testSimple4() throws Exception {
        performTest("package test; import java.util.ArrayList; public class Test {public void test() {Object o = new ArrayList<String>();o = new ArrayList<String>();}}", 114 - 22, 137- 22, 142 - 22, 165 - 22);
    }
    
    public void testSimple5() throws Exception {
        performTest("package test; public class Test {public void test() {Object o = null; String s = (String) o; s = (String) o; s = (String) null; o = (Object) o;}}", 103 - 22, 113 - 22, 119 - 22, 129 - 22);
    }
    
    public void testSimple6() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; y = i + i; y = i + i;} public void test2() {int i = 0; y = i + i; y = i + i;}}", 90 - 22, 95 - 22, 101 - 22, 106 - 22);
    }
    
    public void testSimple7() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; y = i != 0 ? i + i : i * i; y = i != 0 ? i + i : i * i; y = i != 1 ? i + i : i * i; y = i == 0 ? i + i : i * i; y = i != 0 ? i * i : i * i; y = i != 0 ? i + i : i + i; y = i != 0 ? i + i : i * 1;}}", 90 - 22, 112 - 22, 118 - 22, 140 - 22);
    }
    
    public void testSimple8() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; int y = -i; y = -i; y = +i; y = +y;}}", 94 - 22, 96 - 22, 102 - 22, 104 - 22);
    }
    
    public void testSimple9() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; int y = i *= 9; y = i *= 9; y = i /= 9; y = i *= 8; y = y *= 9;}}", 94 - 22, 100 - 22, 106 - 22, 112 - 22);
    }
    
    public void testSimple10() throws Exception {
        performTest("package test; public class Test {public void test() {int[] i = null; int y = i[1]; y = i[1]; y = i[y]; y = i[0];}}", 99 - 22, 103 - 22, 109 - 22, 113 - 22);
    }
    
    public void testSimple11() throws Exception {
        performTest("package test; public class Test {public void test() {int[] i = new int[0]; i = new int[0]; i = new int[1];}}", 85 - 22, 95 - 22, 101 - 22, 111 - 22);
    }
    
    public void testSimple12() throws Exception {
        performTest("package test; public class Test {public void test() {int[] i = new int[1]; i = new int[1]; i = new int[0];}}", 85 - 22, 95 - 22, 101 - 22, 111 - 22);
    }
    
    public void testSimple13() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; int y = (i); y = (i); y = i;}}", 94 - 22, 97 - 22, 103 - 22, 106 - 22);
    }
    
    public void testSimple14() throws Exception {
        performTest("package test; public class Test {public void test() {Object o = null; boolean b = o instanceof String; b = o instanceof String; b = o instanceof Object;}}", 104 - 22, 123 - 22, 129 - 22, 148 - 22);
    }
    
    public void testSimple15() throws Exception {
        performTest("package test; public class Test {private int x = 1; private int y = 1; public void test() {int x = 1; int y = 1;}}", 90 - 22, 91 - 22, 71 - 22, 72 - 22, 121 - 22, 122 - 22, 132 - 22, 133 - 22);
    }
    
    public void testSimple16() throws Exception {
        performTest("package test; public class Test {public void test(int i) {int y = \"\".length(); test(\"\".length());} }", 88 - 22, 99 - 22, 106 - 22, 117 - 22);
    }
    
    public void testSimple17() throws Exception {
        performTest("package test; public class Test {public void test2() {int a = test(test(test(1))); a = test(test(test(1))); a = test(test(test(1)));} public int test(int i) {return 0;} }", 94 - 22, 101 - 22, 119 - 22, 126 - 22, 144 - 22, 151 - 22);
    }
    
    protected void prepareTest(String code) throws Exception {
        clearWorkDir();
        
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        FileObject data = FileUtil.createData(sourceRoot, "test/Test.java");
        
        TestUtilities.copyStringToFile(FileUtil.toFile(data), code);
        
        data.refresh();
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        doc = ec.openDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        
        JavaSource js = JavaSource.forFileObject(data);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    protected CompilationInfo info;
    private Document doc;
    
    protected void performTest(String code, int start, int end, int... duplicates) throws Exception {
        assertTrue(duplicates.length % 2 == 0);
        
        prepareTest(code);
        
        TreePath path = IntroduceHint.validateSelection(info, start, end);
        
        assertNotNull(path);

        Collection<TreePath> result = computeDuplicates(path);

        //        assertEquals(f.result.toString(), duplicates.length / 2, f.result.size());
        
        int[] dupes = new int[result.size() * 2];
        int   index = 0;
        
        for (TreePath tp : result) {
            dupes[index++] = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tp.getLeaf());
            dupes[index++] = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tp.getLeaf());
        }
        
        assertTrue(Arrays.toString(dupes), Arrays.equals(duplicates, dupes));
    }

    protected Collection<TreePath> computeDuplicates(TreePath path) {
        return CopyFinder.computeDuplicates(info, path, new TreePath(info.getCompilationUnit()), new AtomicBoolean());
    }

}
