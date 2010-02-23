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

package org.netbeans.modules.java.hints.jackpot.impl;

import com.sun.source.tree.IfTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;
import java.util.Collections;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.java.source.pretty.VeryPretty;

/**
 *
 * @author lahvac
 */
public class UtilitiesTest extends TestBase {

    public UtilitiesTest(String name) {
        super(name);
    }

    public void testParseAndAttributeExpressionStatement() throws Exception {
        prepareTest("test/Test.java", "package test; public class Test{}");

        Scope s = Utilities.constructScope(info, Collections.singletonMap("$1", info.getTreeUtilities().parseType("int", info.getTopLevelElements().get(0))));
        Tree result = Utilities.parseAndAttribute(info, "$1 = 1;", s);

        assertTrue(result.getKind().name(), result.getKind() == Kind.EXPRESSION_STATEMENT);
    }

    public void testParseAndAttributeVariable() throws Exception {
        prepareTest("test/Test.java", "package test; public class Test{}");

        Scope s = Utilities.constructScope(info, Collections.singletonMap("$1", info.getTreeUtilities().parseType("int", info.getTopLevelElements().get(0))));
        Tree result = Utilities.parseAndAttribute(info, "int $2 = $1;", s);

        assertTrue(result.getKind().name(), result.getKind() == Kind.VARIABLE);
    }

    public void testParseAndAttributeMultipleStatements() throws Exception {
        prepareTest("test/Test.java", "package test; public class Test{}");

        Scope s = Utilities.constructScope(info, Collections.<String, TypeMirror>emptyMap());
        Tree result = Utilities.parseAndAttribute(info, "String $2 = $1; int $l = $2.length(); System.err.println($l);", s);

        assertTrue(result.getKind().name(), result.getKind() == Kind.BLOCK);

        String golden = "{\n" +
                        "    $$1$;\n" +
                        "    String $2 = $1;\n" +
                        "    int $l = $2.length();\n" +
                        "    System.err.println($l);\n" +
                        "    $$2$;\n" +
                        "}";
        assertEquals(golden.replaceAll("[ \n\r]+", " "), result.toString().replaceAll("[ \n\r]+", " "));
    }

    public void testParseAndAttributeMethod() throws Exception {
        prepareTest("test/Test.java", "package test; public class Test{}");

        Scope s = Utilities.constructScope(info, Collections.singletonMap("$1", info.getTreeUtilities().parseType("int", info.getTopLevelElements().get(0))));
        String methodCode = "private int test(int i) { return i; }";
        Tree result = Utilities.parseAndAttribute(info, methodCode, s);

        assertEquals(Kind.METHOD, result.getKind());
        assertEquals(methodCode.replaceAll("[ \n\r]+", " "), result.toString().replaceAll("[ \n\r]+", " ").trim());
    }

    public void testParseAndAttributeMultipleClassMembers() throws Exception {
        prepareTest("test/Test.java", "package test; public class Test{}");

        Scope s = Utilities.constructScope(info, Collections.singletonMap("$1", info.getTreeUtilities().parseType("int", info.getTopLevelElements().get(0))));
        String code = "private int i; private int getI() { return i; } private void setI(int i) { this.i = i; }";
        Tree result = Utilities.parseAndAttribute(info, code, s);

        String golden = "class $ {\n" +
                        "    $$1$;\n" +
                        "    private int i;\n" +
                        "    private int getI() {\n" +
                        "        return i;\n" +
                        "    }\n" +
                        "    private void setI(int i) {\n" +
                        "        this.i = i;\n" +
                        "    }\n" +
                        "}";

        assertEquals(golden.replaceAll("[ \n\r]+", " "), result.toString().replaceAll("[ \n\r]+", " ").trim());
    }

    public void testParseAndAttributeFieldModifiersVariable() throws Exception {
        prepareTest("test/Test.java", "package test; public class Test{}");

        Scope s = Utilities.constructScope(info, Collections.<String, TypeMirror>emptyMap());
        String code = "$mods$ java.lang.String $name;";
        Tree result = Utilities.parseAndAttribute(info, code, s);

//        String golden = "$mods$ java.lang.String $name";
        String golden = "$mods$java.lang.String $name";

        assertEquals(golden.replaceAll("[ \n\r]+", " "), result.toString().replaceAll("[ \n\r]+", " ").trim());
    }

    public void testParseAndAttributeIfWithParenthetised() throws Exception {
        prepareTest("test/Test.java", "package test; public class Test{}");

        Scope s = Utilities.constructScope(info, Collections.<String, TypeMirror>emptyMap());
        String code = "if ($c) { $1$; System.err.println('a'); $2$; }";
        Tree result = Utilities.parseAndAttribute(info, code, s);

        IfTree it = (IfTree) result;

        assertEquals(Kind.PARENTHESIZED, it.getCondition().getKind());

        String golden = "if ($c) { $1$; System.err.println('a'); $2$; }";

        assertEquals(golden.replaceAll("[ \n\r]+", " "), result.toString().replaceAll("[ \n\r]+", " ").trim());
    }

    public void testParseAndAttributeMultipleStatements2() throws Exception {
        prepareTest("test/Test.java", "package test; public class Test{}");

        Scope s = Utilities.constructScope(info, Collections.<String, TypeMirror>emptyMap());
        Tree result = Utilities.parseAndAttribute(info, "$type $name = $map.get($key); if ($name == null) { $map.put($key, $name = $init); }", s);

        assertTrue(result.getKind().name(), result.getKind() == Kind.BLOCK);

        String golden = "{" +
                        "    $$1$;" +
                        "    $type $name = $map.get($key);" +
                        "    if ($name == null) {" +
                        "        $map.put($key, $name = $init);" +
                        "    }" +
                        "    $$2$;\n" +
                        "}";
        assertEquals(golden.replaceAll("[ \n\r]+", " "), result.toString().replaceAll("[ \n\r]+", " "));
    }

    public void testSimpleExpression() throws Exception {
        prepareTest("test/Test.java", "package test; public class Test{}");

        Scope s = Utilities.constructScope(info, Collections.<String, TypeMirror>emptyMap());
        Tree result = Utilities.parseAndAttribute(info, "$1.isDirectory()", s);

        assertTrue(result.getKind().name(), result.getKind() == Kind.METHOD_INVOCATION);

        String golden = "$1.isDirectory()";
        assertEquals(golden.replaceAll("[ \n\r]+", " "), result.toString().replaceAll("[ \n\r]+", " "));
    }

    public void testToHumanReadableTime() {
        long time = 202;
        assertEquals(    "5s", Utilities.toHumanReadableTime(time +=           5 * 1000));
        assertEquals(  "3m5s", Utilities.toHumanReadableTime(time +=      3 * 60 * 1000));
        assertEquals("7h3m5s", Utilities.toHumanReadableTime(time += 7 * 60 * 60 * 1000));
    }

    public void testGeneralization() throws Exception {
        performGeneralizationTest("package test;\n" +
                                  "public class Test {\n" +
                                  "    class Inner {\n" +
                                  "        Inner(int i) {}\n" +
                                  "    }\n" +
                                  "    public static void main(String[] args) {\n" +
                                  "        int i = 1;\n" +
                                  "        Test c = null;\n" +
                                  "        c.new Inner(i++) {};\n" +
                                  "    }\n" +
                                  "}\n",
                                  "package test;\n" +
                                  "public class Test {\n" +
                                  "    class Inner {\n" +
                                  "        Inner(int $0) { }\n" +
                                  "    }\n" +
                                  "    public static void main(String[] $1) {\n" +
                                  "        int $2 = 1;\n" +
                                  "        Test $3 = null;\n" +
                                  "        $4;\n" + //XXX
                                  "    }\n" +
                                  "}\n");
    }
    private void performGeneralizationTest(String code, String generalized) throws Exception {
        prepareTest("test/Test.java", code);

        Tree generalizedTree = Utilities.generalizePattern(info, new TreePath(info.getCompilationUnit()));
        VeryPretty vp = new VeryPretty(info);

        vp.print((JCTree) generalizedTree);

        String repr = vp.toString();

        assertEquals(generalized.replaceAll("[ \n\t]+", " "),
                     repr.replaceAll("[ \n\t]+", " "));
    }

}
