/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s): Alexandru Gyori <Alexandru.Gyori at gmail.com>
 * 
 * Portions Copyrighted 2012-2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.jdk.mapreduce;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author alexandrugyori
 */
public class ForLoopToFunctionalHintTest extends NbTestCase {

    public ForLoopToFunctionalHintTest(String name) {
        super(name);
    }

    public void testSimpleConvert() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        for (Integer l : ls) \n"
                + "            System.out.println(l);\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:verifier:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        ls.stream().forEach(( l) -> { \n"
                + "            System.out.println(l);\n"
                + "        });\n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testChainingMapForEachcConvert() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        for (Integer l : ls) {\n"
                + "            String s = l.toString();\n"
                + "            System.out.println(s);\n"
                + "        }\n"
                + "            \n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:verifier:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        ls.stream().map(( l) -> l.toString()).forEach(( s) -> {\n"
                + "            System.out.println(s);\n"
                + "        });\n"
                + "            \n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testChainingFilterMapForEachConvert() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        for (Integer l : ls) {\n"
                + "            if(l!=null)\n"
                + "            {\n"
                + "                String s = l.toString();\n"
                + "                System.out.println(s);\n"
                + "            }\n"
                + "        }\n"
                + "            \n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:verifier:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        ls.stream().filter(( l) -> (l!=null)).map(( l) -> l.toString()).forEach(( s) -> {\n"
                + "            System.out.println(s);\n"
                + "        });\n"
                + "            \n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testSmoothLongerChaining() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1,2,3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        for (Integer a : ls) {\n"
                + "            Integer l = new Integer(a.intValue());\n"
                + "            if(l!=null)\n"
                + "            {\n"
                + "                String s = l.toString();\n"
                + "                System.out.println(s);\n"
                + "            }\n"
                + "        }\n"
                + "            \n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:verifier:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1,2,3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        ls.stream().map(( a) -> new Integer(a.intValue())).filter(( l) -> (l!=null)).map(( l) -> l.toString()).forEach(( s) -> {\n"
                + "            System.out.println(s);\n"
                + "        });\n"
                + "            \n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testNonFilteringIfChaining() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1,2,3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        for (Integer a : ls) {\n"
                + "            Integer l = new Integer(a.intValue());\n"
                + "            if(l!=null)\n"
                + "            {                \n"
                + "                String s = l.toString();\n"
                + "                if(s!=null)\n"
                + "                    System.out.println(s);\n"
                + "                System.out.println(\"cucu\");\n"
                + "            }\n"
                + "        }\n"
                + "            \n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:verifier:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1,2,3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {        \n"
                + "        ls.stream().map(( a) -> new Integer(a.intValue())).filter(( l) -> (l!=null)).map(( l) -> l.toString()).map(( s) -> {\n"
                + "            if(s!=null)\n"
                + "                System.out.println(s);\n"
                + "            return s;\n"
                + "        }).forEach(( _) -> {\n"
                + "            System.out.println(\"cucu\");\n"
                + "        });\n"
                + "            \n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testContinuingIfFilterSingleStatement() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {\n"
                + "        for (Integer l : ls) {            \n"
                + "            if (l == null) {\n"
                + "                continue;\n"
                + "            }\n"
                + "            String s = l.toString();\n"
                + "            if (s != null) {\n"
                + "                System.out.println(s);\n"
                + "            }     \n"
                + "\n"
                + "        }\n"
                + "\n"
                + "\n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:verifier:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public void test(List<Integer> ls) {\n"
                + "        ls.stream().filter(( l) -> !(l == null)).map(( l) -> l.toString()).filter(( s) -> (s != null)).forEach(( s) -> {\n"
                + "            System.out.println(s);\n"
                + "        });\n"
                + "\n"
                + "\n"
                + "    }\n"
                + "}");
    }

    public void testChainedAnyMatch() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        for(Integer l:ls)\n"
                + "        {\n"
                + "            String s = l.toString();\n"
                + "            Object o = foo(s);\n"
                + "            if(o==null)\n"
                + "                return true;\n"
                + "        }\n"
                + "        \n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }\n"
                + "    \n"
                + "    Object foo(Object o)\n"
                + "    {\n"
                + "        return o;\n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:verifier:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        if (ls.stream().map(( l) -> l.toString()).map(( s) -> foo(s)).anyMatch(( o) -> (o==null))) {\n"
                + "            return true;\n"
                + "        }\n"
                + "        \n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }\n"
                + "    \n"
                + "    Object foo(Object o)\n"
                + "    {\n"
                + "        return o;\n"
                + "    }\n"
                + "}");
    }

    public void testChainedNoneMatch() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        for(Integer l:ls)\n"
                + "        {\n"
                + "            String s = l.toString();\n"
                + "            Object o = foo(s);\n"
                + "            if(o==null)\n"
                + "                return false;\n"
                + "        }\n"
                + "        \n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }\n"
                + "    \n"
                + "    Object foo(Object o)\n"
                + "    {\n"
                + "        return o;\n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:verifier:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        if (!ls.stream().map(( l) -> l.toString()).map(( s) -> foo(s)).noneMatch(( o) -> (o==null))) {\n"
                + "            return false;\n"
                + "        }\n"
                + "        \n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }\n"
                + "    \n"
                + "    Object foo(Object o)\n"
                + "    {\n"
                + "        return o;\n"
                + "    }\n"
                + "}");
    }

    public void testNoNeededVariablesMerging() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) throws Exception {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) throws Exception {\n"
                + "        Integer i=0;        \n"
                + "        for(Integer l : ls)\n"
                + "        {         \n"
                + "            System.out.println();\n"
                + "            System.out.println(\"\");\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i) throws Exception\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("14:8-14:11:verifier:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) throws Exception {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) throws Exception {\n"
                + "        Integer i=0;        \n"
                + "        ls.stream().map(( _) -> {         \n"
                + "            System.out.println();\n"
                + "            return _;\n"
                + "        }).forEach(( _) -> {\n"
                + "            System.out.println(\"\");\n"
                + "        });\n"
                + "        System.out.println(i);\n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i) throws Exception\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testSomeChainingWithNoNeededVar() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        for(Integer a:ls)\n"
                + "        {\n"
                + "            Integer l = new Integer(a.intValue());\n"
                + "            if(l==null)\n"
                + "            {\n"
                + "                String s=l.toString();\n"
                + "                if(s!=null)\n"
                + "                {\n"
                + "                    System.out.println(s);\n"
                + "                }\n"
                + "                System.out.println(\"cucu\");\n"
                + "            }   \n"
                + "            System.out.println();\n"
                + "        }\n"
                + "        \n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }\n"
                + "        \n"
                + "    Object foo(Object o)\n"
                + "    {\n"
                + "        return o;\n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("12:8-12:11:verifier:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3));\n"
                + "    }\n"
                + "\n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        ls.stream().map(( a) -> new Integer(a.intValue())).map(( l) -> {\n"
                + "            if(l==null)\n"
                + "            {\n"
                + "                String s=l.toString();\n"
                + "                if(s!=null)\n"
                + "                {\n"
                + "                    System.out.println(s);\n"
                + "                }\n"
                + "                System.out.println(\"cucu\");\n"
                + "            }   \n"
                + "            return l;\n"
                + "        }).forEach(( _) -> {\n"
                + "            System.out.println();\n"
                + "        });\n"
                + "        \n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }\n"
                + "        \n"
                + "    Object foo(Object o)\n"
                + "    {\n"
                + "        return o;\n"
                + "    }\n"
                + "}");
    }

    public void testSimpleReducer() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        for(Integer l : ls)\n"
                + "            i++;\n"
                + "        System.out.println(i);\n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("14:8-14:11:verifier:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        i = ls.stream().reduce(i, ( accumulator, _) -> accumulator + 1, null);\n"
                + "        System.out.println(i);\n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "}");
    }

    public void testChainedReducer() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        for(Integer l : ls)\n"
                + "        {             \n"
                + "            if(l!=null)\n"
                + "            {\n"
                + "                foo(l);\n"
                + "                i++;\n"
                + "            }\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("14:8-14:11:verifier:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        i = ls.stream().filter(( l) -> (l!=null)).map(( l) -> {\n"
                + "            foo(l);\n"
                + "            return l;\n"
                + "        }).reduce(i, ( accumulator, _) -> accumulator + 1, null);\n"
                + "        System.out.println(i);\n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testChainedReducerWithMerging() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        for(Integer l : ls)\n"
                + "        {        \n"
                + "            String s =l.toString();\n"
                + "            System.out.println(s);\n"
                + "            foo(l);\n"
                + "            if(l!=null)\n"
                + "            {\n"
                + "                foo(l);                \n"
                + "                i--;\n"
                + "            }\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .findWarning("14:8-14:11:verifier:" + Bundle.ERR_ForLoopToFunctionalHint())
                .applyFix()
                .assertOutput("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        i = ls.stream().map(( l) -> {        \n"
                + "            String s =l.toString();\n"
                + "            System.out.println(s);\n"
                + "            foo(l);\n"
                + "            return l;\n"
                + "        }).filter(( l) -> (l!=null)).map(( l) -> {\n"
                + "            foo(l);\n"
                + "            return l;\n"
                + "        }).reduce(i, ( accumulator, _) -> accumulator - 1, null);\n"
                + "        System.out.println(i);\n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}");
    }

    public void testNoHintDueToNEF() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        for(Integer l : ls)\n"
                + "        {        \n"
                + "            String s =l.toString();\n"
                + "            System.out.println(s);\n"
                + "            foo(l,i);            \n"
                + "            if(l!=null)\n"
                + "            {                           \n"
                + "                i++;\n"
                + "            }\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings();
    }

    public void testNoHintDueToBreak() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        for(Integer l : ls)\n"
                + "        {                      \n"
                + "            if(l!=null)\n"
                + "            {                           \n"
                + "                break;\n"
                + "            }\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return true;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings();
    }

    public void testNoHintDueToReturnInt() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public int test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        for(Integer l : ls)\n"
                + "        {                      \n"
                + "            if(l!=null)\n"
                + "            {                           \n"
                + "                return 0;\n"
                + "            }\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return 1;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings();
    }

    public void testNoHintDueToMultipleReturnBoolean() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        for(Integer l : ls)\n"
                + "        {                      \n"
                + "            if(l==null)\n"
                + "            {                           \n"
                + "                return true;\n"
                + "            }\n"
                + "            if(l.toString()==null)\n"
                + "                return true;\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings();
    }

    public void testNoHintDueToLabeledContinue() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        label:\n"
                + "        for(Integer l : ls)\n"
                + "        {                      \n"
                + "            if(l==null)\n"
                + "            {                           \n"
                + "                continue;// label;\n"
                + "            }\n"
                + "            if(l.toString()==null)\n"
                + "                return true;\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings("15:8-15:11:verifier:" + Bundle.ERR_ForLoopToFunctionalHint());
    }

    public void testNoHintDueToNonEliminableContinue() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) {\n"
                + "        Integer i=0;\n"
                + "        \n"
                + "        for(Integer l : ls)\n"
                + "        {                      \n"
                + "            if(l==null)\n"
                + "            {                           \n"
                + "                continue;\n"
                + "            }\n"
                + "            else if(l.toString()==null)\n"
                + "                return true;\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i)\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings("15:8-15:11:verifier:" + Bundle.ERR_ForLoopToFunctionalHint());
    }

    public void testNoHintDueToMethodThrowingException() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) throws Exception {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) throws Exception {\n"
                + "        Integer i=0;\n"
                + "        \n"
                + "        for(Integer l : ls)\n"
                + "        {         \n"
                + "            foo(l,1);\n"
                + "            if(l==null)\n"
                + "            {                           \n"
                + "                continue;\n"
                + "            }\n"
                + "            else if(l.toString()==null)\n"
                + "                return true;\n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i) throws Exception\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings();
    }

    public void testNoHintDueToExplicitThrow() throws Exception {
        HintTest.create()
                .input("package testdemo;\n"
                + "\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "class TestDemo {\n"
                + "\n"
                + "    public static void main(String[] args) throws Exception {\n"
                + "        new TestDemo().test(Arrays.asList(1, 2, 3,7));\n"
                + "    }\n"
                + "\n"
                + "   \n"
                + "    public Boolean test(List<Integer> ls) throws Exception {\n"
                + "        Integer i=0;\n"
                + "        \n"
                + "        for(Integer l : ls)\n"
                + "        {         \n"
                + "            throw new Exception();            \n"
                + "            \n"
                + "        }\n"
                + "        System.out.println(i);\n"
                + "        return false;\n"
                + "\n"
                + "\n"
                + "    }    \n"
                + "    private void foo(Object o, int i) throws Exception\n"
                + "    {\n"
                + "        \n"
                + "    }\n"
                + "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings();
    }
    
    public void testNPEForReturnWithExpressions() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.util.List;" +
                       "class Test {\n" +
                       "    public void test(List<Integer> ls) throws Exception {\n" +
                       "        for(Integer l : ls) {\n" +
                       "            return ;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .sourceLevel("1.8")
                .run(ForLoopToFunctionalHint.class)
                .assertWarnings();
    }
}
