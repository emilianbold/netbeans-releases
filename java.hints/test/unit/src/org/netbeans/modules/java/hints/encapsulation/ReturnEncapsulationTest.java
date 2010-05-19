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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.encapsulation;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;
import org.netbeans.spi.editor.hints.Fix;



/**
 *
 * @author Tomas Zezula
 */
public class ReturnEncapsulationTest extends TestBase {

    public ReturnEncapsulationTest (final String name) {
        super(name,ReturnEncapsulation.class);
    }

    public void testReturnCollectionField() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    private java.util.List l;\n"+
                            "    public java.util.List getList() {\n"+
                            "        return l;\n"+
                            "    }\n"+
                            "}",
                            "4:8-4:17:verifier:Return of Collection Field");
    }

    public void testReturnCollectionLocal() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public java.util.List getList() {\n"+
                            "        java.util.List l = null;\n"+
                            "        return l;\n"+
                            "    }\n"+
                            "}");
    }

    public void testReturnCollectionFromOtherClass() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public java.util.List getList() {\n"+
                            "        return java.util.Collections.EMPTY_LIST;\n"+
                            "    }\n"+
                            "}");
    }

    public void testReturnArrayField() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    private int[] l;\n"+
                            "    public int[] getArry() {\n"+
                            "        return l;\n"+
                            "    }\n"+
                            "}",
                            "4:8-4:17:verifier:Return of Array Field");
    }

    public void testReturnArrayLocal() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public int[] getArry() {\n"+
                            "        int[] l = null;\n"+
                            "        return l;\n"+
                            "    }\n"+
                            "}");
    }

    public void testReturnDateField() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    private java.util.Date d;\n"+
                            "    public java.util.Date getDate() {\n"+
                            "        return d;\n"+
                            "    }\n"+
                            "}",
                            "4:8-4:17:verifier:Return of Date or Calendar Field");
    }

    public void testReturnDateLocal() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public java.util.Date getDate() {\n"+
                            "        java.util.Date d = null;\n"+
                            "        return d;\n"+
                            "    }\n"+
                            "}");
    }

    public void testReturnCalendarField() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    private java.util.Calendar d;\n"+
                            "    public java.util.Calendar getDate() {\n"+
                            "        return d;\n"+
                            "    }\n"+
                            "}",
                            "4:8-4:17:verifier:Return of Date or Calendar Field");
    }

    public void testReturnCalendarLocal() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    public java.util.Calendar getDate() {\n"+
                            "        java.util.Calendar d = null;\n"+
                            "        return d;\n"+
                            "    }\n"+
                            "}");
    }

    public void testReturnError() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "    private Foo d;\n"+
                            "    public java.util.Collection getCollection() {\n"+
                            "        return d;\n"+
                            "    }\n"+
                            "}");
    }

    public void testCollectionFix() throws Exception {
        performFixTest("test/Test.java",
                        "package test;\n" +
                        "public class Test {\n" +
                        "    private java.util.Collection l;\n"+
                        "    public java.util.Collection getCollection() {\n"+
                        "        return l;\n"+
                        "    }\n"+
                        "}",
                        "4:8-4:17:verifier:Return of Collection Field",
                        "Replace with java.util.Collections.unmodifiableCollection(l)",
                        ("package test;\n" +
                        "import java.util.Collections;\n"+
                        "public class Test {\n" +
                        "    private java.util.Collection l;\n"+
                        "    public java.util.Collection getCollection() {\n"+
                        "        return Collections.unmodifiableCollection(l);\n"+
                        "    }\n"+
                        "}").replaceAll("[ \t\n]+", " "));
    }

    public void testListFix() throws Exception {
        performFixTest("test/Test.java",
                        "package test;\n" +
                        "public class Test {\n" +
                        "    private java.util.List l;\n"+
                        "    public java.util.List getCollection() {\n"+
                        "        return l;\n"+
                        "    }\n"+
                        "}",
                        "4:8-4:17:verifier:Return of Collection Field",
                        "Replace with java.util.Collections.unmodifiableList(l)",
                        ("package test;\n" +
                        "import java.util.Collections;\n"+
                        "public class Test {\n" +
                        "    private java.util.List l;\n"+
                        "    public java.util.List getCollection() {\n"+
                        "        return Collections.unmodifiableList(l);\n"+
                        "    }\n"+
                        "}").replaceAll("[ \t\n]+", " "));
    }

    public void testSetFix() throws Exception {
        performFixTest("test/Test.java",
                        "package test;\n" +
                        "public class Test {\n" +
                        "    private java.util.Set l;\n"+
                        "    public java.util.Set getCollection() {\n"+
                        "        return l;\n"+
                        "    }\n"+
                        "}",
                        "4:8-4:17:verifier:Return of Collection Field",
                        "Replace with java.util.Collections.unmodifiableSet(l)",
                        ("package test;\n" +
                        "import java.util.Collections;\n"+
                        "public class Test {\n" +
                        "    private java.util.Set l;\n"+
                        "    public java.util.Set getCollection() {\n"+
                        "        return Collections.unmodifiableSet(l);\n"+
                        "    }\n"+
                        "}").replaceAll("[ \t\n]+", " "));
    }

    public void testMapFix() throws Exception {
        performFixTest("test/Test.java",
                        "package test;\n" +
                        "public class Test {\n" +
                        "    private java.util.Map l;\n"+
                        "    public java.util.Map getCollection() {\n"+
                        "        return l;\n"+
                        "    }\n"+
                        "}",
                        "4:8-4:17:verifier:Return of Collection Field",
                        "Replace with java.util.Collections.unmodifiableMap(l)",
                        ("package test;\n" +
                        "import java.util.Collections;\n"+
                        "public class Test {\n" +
                        "    private java.util.Map l;\n"+
                        "    public java.util.Map getCollection() {\n"+
                        "        return Collections.unmodifiableMap(l);\n"+
                        "    }\n"+
                        "}").replaceAll("[ \t\n]+", " "));
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

}
