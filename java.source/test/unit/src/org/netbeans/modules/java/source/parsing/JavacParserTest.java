/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.parsing;

import java.io.File;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class JavacParserTest extends NbTestCase {

    public JavacParserTest(String name) {
        super(name);
    }

    private FileObject sourceRoot;

    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        clearWorkDir();
        prepareTest();
    }

    public void test1() throws Exception {
        FileObject f1 = createFile("test/Test1.java", "package test; class Test1");
        FileObject f2 = createFile("test/Test2.java", "package test; class Test2{}");
        FileObject f3 = createFile("test/Test3.java", "package test; class Test3{}");

        ClasspathInfo cpInfo = ClasspathInfo.create(f2);
        JavaSource js = JavaSource.create(cpInfo, f2, f3);

        SourceUtilsTestUtil.compileRecursively(sourceRoot);

        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController parameter) throws Exception {
                if ("Test3".equals(parameter.getFileObject().getName())) {
                    TypeElement te = parameter.getElements().getTypeElement("test.Test1");
                    assertNotNull(te);
                    assertNotNull(parameter.getTrees().getPath(te));
                }
                assertEquals(Phase.PARSED, parameter.toPhase(Phase.PARSED));
                assertNotNull(parameter.getCompilationUnit());
            }
        }, true);
    }

    private FileObject createFile(String path, String content) throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, path);
        TestUtilities.copyStringToFile(file, content);

        return file;
    }

    private void prepareTest() throws Exception {
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);

        assertNotNull(workFO);

        sourceRoot = workFO.createFolder("src");
        
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
    }

}