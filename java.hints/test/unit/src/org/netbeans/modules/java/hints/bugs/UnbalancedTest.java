/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class UnbalancedTest extends NbTestCase {

    public UnbalancedTest(String name) {
        super(name);
    }

    public void testArrayWriteOnly() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private byte[] arr;\n" +
                       "    private void t() { arr[0] = 0; }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertContainsWarnings("2:19-2:22:verifier:ERR_UnbalancedArrayWRITE arr");
    }

    public void testArrayReadOnly1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private byte[] arr;\n" +
                       "    private void t() { System.err.println(arr[0]); }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertContainsWarnings("2:19-2:22:verifier:ERR_UnbalancedArrayREAD arr");
    }

    public void testArrayReadOnly2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private byte[] arr = new byte[0];\n" +
                       "    private void t() { System.err.println(arr[0]); }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertContainsWarnings("2:19-2:22:verifier:ERR_UnbalancedArrayREAD arr");
    }

    public void testArrayNeg1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private byte[] arr;\n" +
                       "    private void t() { arr[0] = 0; System.err.println(arr[0]); }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertWarnings();
    }

    public void testArrayNeg2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private byte[] arr;\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertWarnings();
    }

    public void testArrayNeg3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private byte[] arr;\n" +
                       "    private void t() { System.err.println(arr[0]); }\n" +
                       "    private Object g() { return arr; }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertWarnings();
    }

    public void testArrayNeg4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private byte[] arr = {1, 2, 3};\n" +
                       "    private void t() { System.err.println(arr[0]); }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertWarnings();
    }

    public void testArrayNeg206855() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "  private final int aa[][] = new int[3][3];\n" +
                       "  public Test() {\n" +
                       "    aa[0][0] = 1;\n" +
                       "  }\n" +
                       "  public int get() {\n" +
                       "    return aa[0][0];\n" +
                       "  }\n" +
                       "}\n")
                .run(Unbalanced.Array.class)
                .assertWarnings();
    }

    public void testCollectionWriteOnly1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll;\n" +
                       "    private void t() { coll.add(\"a\"); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertContainsWarnings("2:35-2:39:verifier:ERR_UnbalancedCollectionWRITE coll");
    }

    public void testCollectionWriteOnly2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll;\n" +
                       "    private void t() { coll.add(\"a\"); coll.iterator(); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertContainsWarnings("2:35-2:39:verifier:ERR_UnbalancedCollectionWRITE coll");
    }

    public void testCollectionReadOnly1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll;\n" +
                       "    private void t() { String str = coll.get(0); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertContainsWarnings("2:35-2:39:verifier:ERR_UnbalancedCollectionREAD coll");
    }

    public void testCollectionReadOnly2() throws Exception {//XXX ?
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll;\n" +
                       "    private void t() { String str = coll.remove(0); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertContainsWarnings("2:35-2:39:verifier:ERR_UnbalancedCollectionREAD coll");
    }

    public void testCollectionReadOnly3() throws Exception {//XXX ?
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll = new java.util.ArrayList<String>(1);\n" +
                       "    private void t() { String str = coll.remove(0); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertContainsWarnings("2:35-2:39:verifier:ERR_UnbalancedCollectionREAD coll");
    }

    public void testMapReadOnly1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Map<String, String> map;\n" +
                       "    private void t() { String str = map.get(\"a\"); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertContainsWarnings("2:42-2:45:verifier:ERR_UnbalancedCollectionREAD map");
    }

    public void testCollectionNeg1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll;\n" +
                       "    private void t() { coll.add(\"a\"); System.err.println(coll.get(0)); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertWarnings();
    }

    public void testCollectionNeg2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll;\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertWarnings();
    }

    public void testCollectionNeg3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll;\n" +
                       "    private void t() { System.err.println(coll.get(0)); }\n" +
                       "    private Object g() { return coll; }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertWarnings();
    }

    public void testCollectionNeg4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll = new java.util.ArrayList<String>(java.util.Arrays.asList(\"foo\"));\n" +
                       "    private void t() { System.err.println(coll.get(0)); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertWarnings();
    }

    public void testCollectionNegAddTested() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List<String> coll = new java.util.ArrayList<String>();\n" +
                       "    public void t1(String str) { if (coll.add(str)) System.err.println(\"\"); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertWarnings();
    }

    public void testCollectionLocalVariable() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void t() { java.util.List<String> coll = new java.util.ArrayList<String>(); String str = coll.get(0); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertContainsWarnings("2:46-2:50:verifier:ERR_UnbalancedCollectionREAD coll");
    }

    public void testCollectionNegNonPrivate() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    java.util.List<String> coll = new java.util.ArrayList<String>();\n" +
                       "    public void t1(String str) { if (coll.add(str)) System.err.println(\"\"); }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertWarnings();
    }

    public void testCollectionNegEnhForLoop() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.List;\n" +
                       "public class Test {\n" +
                       "    public int t1(List<List<String>> ll) { int total = 0; for (List<String> l : ll) total += l.size(); return total; }\n" +
                       "}\n")
                .run(Unbalanced.Collection.class)
                .assertWarnings();
    }
}
