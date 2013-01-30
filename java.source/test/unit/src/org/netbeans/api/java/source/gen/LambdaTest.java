/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree.ReferenceMode;
import com.sun.source.util.TreeScanner;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests correct adding cast to statement.
 *
 * @author Pavel Flaska
 */
public class LambdaTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of AddCastTest */
    public LambdaTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(LambdaTest.class);
        return suite;
    }

    public void testPrintMemberReference() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = null;\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "public class Test {\n" +
            "    public static void taragui() {\n" +
            "        Runnable r = Test::taragui;\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                final TreeMaker make = workingCopy.getTreeMaker();
                new TreeScanner<Void, Void>() {
                    @Override public Void visitLiteral(LiteralTree node, Void p) {
                        workingCopy.rewrite(node, make.MemberReference(ReferenceMode.INVOKE, make.Identifier("Test"), "taragui", Collections.<ExpressionTree>emptyList()));
                        return super.visitLiteral(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
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
