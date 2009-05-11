/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 * The following shell script was used to generate the code snippets
 * <code>cat test/unit/data/test/Test.java | tr '\n' ' ' | tr '\t' ' ' | sed -E 's| +| |g' | sed 's|"|\\"|g'</code>
 * @author Samuel Halliday
 */
public class SwitchTest extends GeneratorTest {

    public SwitchTest(String name) {
        super(name);
    }

    public void test158129() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String test = "public class Test { void m(int p) { switch (p) { ca|se 0: } } }";
        // XXX whitespace "public class Test { void m(int p) { switch (p) { case 0: break; } } }"
        String golden = "public class Test { void m(int p) { switch (p) { case 0:break;\n } } }";
        final int index = test.indexOf("|");
        assertTrue(index != -1);
        TestUtilities.copyStringToFile(testFile, test.replace("|", ""));
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                if (copy.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                    return;
                }
                TreeMaker make = copy.getTreeMaker();
                TreePath node = copy.getTreeUtilities().pathFor(index);
                assertTrue(node.getLeaf().getKind() == Kind.CASE);
                CaseTree original = (CaseTree) node.getLeaf();
                System.err.println("ORIGINAL " + original);
                List<StatementTree> st = new ArrayList<StatementTree>();
                st.addAll(original.getStatements());
                st.add(make.Break(null));
                CaseTree modified = make.Case(original.getExpression(), st);
                System.err.println("MODIFIED " + modified);
                copy.rewrite(original, modified);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    // XXX I don't understand what these are used for
    @Override
    String getSourcePckg() {
        return "";
    }

    @Override
    String getGoldenPckg() {
        return "";
    }
}
