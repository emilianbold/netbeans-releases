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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.api.java.source;

import com.sun.source.util.TreePath;
import java.io.File;
import java.io.OutputStream;
import java.security.Permission;
import javax.lang.model.element.TypeElement;
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

    public void test126732() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/test.java");
        String code = "package test;\n" +
                      "public class Test {\n" +
                      "    public static void test() {\n" +
                      "        return Runnable() {\n" +
                      "                new Runnable() {\n" +
                      "        };\n" +
                      "    }\n" +
                      "}";

        writeIntoFile(file,code);

        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);

        TreePath       tp       = info.getTreeUtilities().pathFor(code.indexOf("new Runnable() {"));
        TreePathHandle handle   = TreePathHandle.create(tp, info);
        TreePath       resolved = handle.resolve(info);

        assertNotNull(resolved);

        assertTrue(tp.getLeaf() == resolved.getLeaf());
    }

    public void test134457() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/Test.java");
        String code = "package test;\n" +
                      "public class Test {\n" +
                      "    public static final String KONST = \"\";\n" +
                      "    public static void test() {\n" +
                      "        Test test = new Test();\n" +
                      "        test.KONST;\n" +
                      "    }\n" +
                      "}";
        
        writeIntoFile(file,code);
        
        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        TreePath       tp       = info.getTreeUtilities().pathFor(code.indexOf("ONST;"));
        TreePathHandle handle   = TreePathHandle.create(tp, info);
        TreePath       resolved = handle.resolve(info);
        
        assertNotNull(resolved);
        
        assertTrue(tp.getLeaf() == resolved.getLeaf());
    }
    
    public void testTreePathIsNotParsing() throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, "test/test.java");
        
        writeIntoFile(file, "package test; public class test {}");
        writeIntoFile(FileUtil.createData(sourceRoot, "test/test2.java"), "package test; public class test2 {}");
        
        JavaSource js = JavaSource.forFileObject(file);
        
        SourceUtilsTestUtil.compileRecursively(sourceRoot);
        
        js.runUserActionTask(new  Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                
                TypeElement string = parameter.getElements().getTypeElement("test.test2");
                
                SecurityManager old = System.getSecurityManager();
                
                System.setSecurityManager(new SecMan());
                
                TreePathHandle.create(string, parameter);
                
                System.setSecurityManager(old);
            }
        }, true);
    }
    
    private static final class SecMan extends SecurityManager {

        @Override
        public void checkRead(String file) {
            assertFalse(file.endsWith("test2.java"));
        }

        @Override
        public void checkRead(String file, Object context) {
            assertFalse(file.endsWith("test2.java"));
        }
        
        @Override
        public void checkPermission(Permission perm) {
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
        }

    }
}
