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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import java.io.File;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 * Test different modifications in try/catch/finally section.
 * 
 * @author Pavel Flaska
 */
public class TryTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of TryTest */
    public TryTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(TryTest.class);
        return suite;
    }

    /**
     * Renames variable in try body.
     */
    public void testRenameInTryBody() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.*;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "            File f = new File(\"auto\");\n" +
            "            FileInputStream fis = new FileInputStream(f);\n" +
            "        } catch (FileNotFoundException ex) {\n" +
            "        } catch (NullPointerException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.*;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "            File f = new File(\"auto\");\n" +
            "            FileInputStream input = new FileInputStream(f);\n" +
            "        } catch (FileNotFoundException ex) {\n" +
            "        } catch (NullPointerException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                TryTree tt = (TryTree) method.getBody().getStatements().get(0);
                VariableTree var = (VariableTree) tt.getBlock().getStatements().get(1);
                workingCopy.rewrite(var, make.setLabel(var, "input"));
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * #96551: Incorrectly formatted catch
     */
    public void testInsertCatchClause() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.*;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "            File f = new File(\"auto\");\n" +
            "            FileInputStream fis = new FileInputStream(f);\n" +
            "        } catch (FileNotFoundException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.*;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "            File f = new File(\"auto\");\n" +
            "            FileInputStream fis = new FileInputStream(f);\n" +
            "        } catch (NullPointerException npe) {\n" +
            "        } catch (FileNotFoundException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                TryTree tt = (TryTree) method.getBody().getStatements().get(0);
                CatchTree njuKec = make.Catch(make.Variable(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "npe",
                        make.Identifier("NullPointerException"),
                        null),
                    make.Block(Collections.<StatementTree>emptyList(), false)
                );
                workingCopy.rewrite(tt, make.insertTryCatch(tt, 0, njuKec));
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * #96551: Incorrectly formatted catch
     */
    public void testAddCatchClause() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.*;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "            File f = new File(\"auto\");\n" +
            "            FileInputStream fis = new FileInputStream(f);\n" +
            "        } catch (FileNotFoundException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.*;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "            File f = new File(\"auto\");\n" +
            "            FileInputStream fis = new FileInputStream(f);\n" +
            "        } catch (FileNotFoundException ex) {\n" +
            "        } catch (NullPointerException npe) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                TryTree tt = (TryTree) method.getBody().getStatements().get(0);
                CatchTree njuKec = make.Catch(make.Variable(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "npe",
                        make.Identifier("NullPointerException"),
                        null),
                    make.Block(Collections.<StatementTree>emptyList(), false)
                );
                workingCopy.rewrite(tt, make.addTryCatch(tt, njuKec));
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFF() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.*;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "            System.err.println(0);\n" +
            "        } catch (FileNotFoundException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.*;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "            System.err.println(0);\n" +
            "        } catch (FileNotFoundException ex) {\n" +
            "        } finally {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                TryTree tt = (TryTree) method.getBody().getStatements().get(0);
                TryTree nue = make.Try(tt.getBlock(), tt.getCatches(), make.Block(Collections.<StatementTree>emptyList(), false));
                workingCopy.rewrite(tt, nue);
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

}
