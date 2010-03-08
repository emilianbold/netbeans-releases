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

package org.netbeans.modules.java.hints;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class CollectionRemoveTest extends TreeRuleTestBase {

    public CollectionRemoveTest(String name) {
        super(name);
    }

    public void testSimple1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private void test () {" +
                            "        java.util.List<String> l = null;" +
                            "        l.rem|ove(new Object());" +
                            "    }" +
                            "}",
                            "0:106-0:128:verifier:SC: java.util.Collection.remove, Object, String");
    }

    public void testSimple2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private void test () {" +
                            "        java.util.List<String> l = null;" +
                            "        l.con|tains(new Object());" +
                            "    }" +
                            "}",
                            "0:106-0:130:verifier:SC: java.util.Collection.contains, Object, String");
    }

    public void testSimple3() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private void test () {" +
                            "        java.util.List<String> l = null;" +
                            "        l.rem|ove(Integer.valueOf(1));" +
                            "    }" +
                            "}",
                            "0:106-0:134:verifier:SCIT: java.util.Collection.remove, Integer, String");
    }

    public void testSimple4() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private void test () {" +
                            "        java.util.List<String> l = null;" +
                            "        l.con|tains(Integer.valueOf(1));" +
                            "    }" +
                            "}",
                            "0:106-0:136:verifier:SCIT: java.util.Collection.contains, Integer, String");
    }

    public void testSimple5() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private void test () {" +
                            "        java.util.Map<String, Number> l = null;" +
                            "        l.con|tainsKey(Integer.valueOf(1));" +
                            "    }" +
                            "}",
                            "0:113-0:146:verifier:SCIT: java.util.Map.containsKey, Integer, String");
    }

    public void testSimple6() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private void test () {" +
                            "        java.util.Map<String, Number> l = null;" +
                            "        l.con|tainsValue(\"\");" +
                            "    }" +
                            "}",
                            "0:113-0:132:verifier:SCIT: java.util.Map.containsValue, String, Number");
    }

    public void testExtends1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test extends java.util.LinkedList<String> {" +
                            "    private void test () {" +
                            "        re|move(new Object());" +
                            "    }" +
                            "}",
                            "0:103-0:123:verifier:SC: java.util.Collection.remove, Object, String");
    }

    public void testExtends2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test extends java.util.LinkedList<String> {" +
                            "    private void test () {" +
                            "        new Runnable() {" +
                            "            public void run() {" +
                            "                re|move(new Object());" +
                            "            }" +
                            "        }" +
                            "    }" +
                            "}",
                            "0:166-0:186:verifier:SC: java.util.Collection.remove, Object, String");
    }

    public void testBoxing1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private void test () {" +
                            "        java.util.List<Integer> l = null;" +
                            "        l.con|tains(1);" +
                            "    }" +
                            "}");
    }

    public void testBoxing2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private void test () {" +
                            "        java.util.List<String> l = null;" +
                            "        l.con|tains(1);" +
                            "    }" +
                            "}",
                            "0:106-0:119:verifier:SCIT: java.util.Collection.contains, int, String");
    }

    public void testExtendsWildcard() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private void test () {" +
                            "        java.util.List<? extends String> l = null;" +
                            "        l.con|tains(\"\");" +
                            "    }" +
                            "}");
    }

    public void testExtendsWildcard2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private void test (boolean b) {" +
                            "        test(get().cont|ains(\"\"));\n" +
                            "    }\n" +
                            "    private java.util.List<? extends String> get() {return null;}\n" +
                            "}");
    }

    public void testSuperWildcard() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private void test () {" +
                            "        java.util.List<? super String> l = null;" +
                            "        l.con|tains(\"\");" +
                            "    }" +
                            "}");
    }

    public void testWildcard() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;" +
                            "public class Test {" +
                            "    private void test () {" +
                            "        java.util.List<?> l = null;" +
                            "        l.con|tains(\"\");" +
                            "    }" +
                            "}");
    }

    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath tp) {
        while (tp != null) {
            if (tp.getLeaf().getKind() != Kind.METHOD_INVOCATION) {
                tp = tp.getParentPath();
                continue;
            }
            return new CollectionRemove().run(info, tp);
        }

        return null;
    }

    static {
        NbBundle.setBranding("test");
    }

}
