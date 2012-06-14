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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javadoc.hints;

import com.sun.source.util.TreePath;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Jan Pokorsky
 */
public class GenerateJavadocFixTest extends JavadocTestSupport {

    public GenerateJavadocFixTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(GenerateJavadocFixTest.class);
//        suite.addTest(new GenerateJavadocFixTest("testGenerateMethodJavadoc"));
        return suite;
    }

    @Override
    protected void doFixTest(String code, String expectation, TreePath tpath) throws Exception {
        super.doFixTest(code, expectation, tpath, true);
    }
    
    public void testGenerateMethodJavadoc() throws Exception {
        doMemberFixTest(
                "package test;\n" +
                "import java.io.IOException;\n" +
                "class Zima {\n" +
                "    @Deprecated <T> int leden(int param1, int param2, T param3) throws IOException, IllegalArgumentException, java.io.FileNotFoundException {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "import java.io.IOException;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     *\n" +
                "     * @param <T>\n" +
                "     * @param param1\n" +
                "     * @param param2\n" +
                "     * @param param3\n" +
                "     * @return\n" +
                "     * @throws IOException\n" +
                "     * @throws IllegalArgumentException\n" +
                "     * @throws java.io.FileNotFoundException\n" +
                "     * @deprecated\n" +
                "     */\n" +
                "    @Deprecated <T> int leden(int param1, int param2, T param3) throws IOException, IllegalArgumentException, java.io.FileNotFoundException {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n");        
    }
    
    public void testGenerateConstructorJavadoc() throws Exception {
        doConstructorFixTest(
                "package test;\n" +
                "import java.io.IOException;\n" +
                "class Zima {\n" +
                "    @Deprecated <T> Zima(int param1, int param2, T param3) throws IOException, IllegalArgumentException, java.io.FileNotFoundException {\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "import java.io.IOException;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     *\n" +
                "     * @param <T>\n" +
                "     * @param param1\n" +
                "     * @param param2\n" +
                "     * @param param3\n" +
                "     * @throws IOException\n" +
                "     * @throws IllegalArgumentException\n" +
                "     * @throws java.io.FileNotFoundException\n" +
                "     * @deprecated\n" +
                "     */\n" +
                "    @Deprecated <T> Zima(int param1, int param2, T param3) throws IOException, IllegalArgumentException, java.io.FileNotFoundException {\n" +
                "    }\n" +
                "}\n");        
    }
    
    public void testGenerateClassJavadoc() throws Exception {
        System.setProperty("user.name", "Alois");
        doClassFixTest(
                "package test;\n" +
                "@Deprecated class Zima<P,Q> {\n" +
                "}\n",
                
                "package test;\n" +
                "/**\n" +
                " *\n" +
                " * @author Alois\n" +
                " * @param <P>\n" +
                " * @param <Q>\n" +
                " * @deprecated\n" +
                " */\n" +
                "@Deprecated class Zima<P,Q> {\n" +
                "}\n");        
    }
    
    public void testGenerateFieldJavadoc() throws Exception {
        doMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    @Deprecated\n" +
                "    int leden;\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     *\n" +
                "     * @deprecated\n" +
                "     */\n" +
                "    @Deprecated\n" +
                "    int leden;\n" +
                "}\n");        
    }
    
    public void testGenerateFieldGroupJavadoc() throws Exception { //#213499
        doMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    @Deprecated\n" +
                "    int leden, unor;\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    @Deprecated\n" +
                "    int leden,\n" +
                "    /**\n" +
                "     *\n" +
                "     * @deprecated\n" +
                "     */\n" +
                "    unor;\n" +
                "}\n", 2);        
    }
    
    public void testGenerateEnumConstantJavadoc_124114() throws Exception {
        doMemberFixTest(
                "package test;\n" +
                "enum Zima {LEDEN, UNOR}\n",
                
                "package test;\n" +
                "enum Zima {\n" +
                "    /**\n" +
                "     *\n" +
                "     */\n" +
                "    LEDEN, UNOR}\n");
    }
    
    public void testGenerateEnumConstantJavadoc_124114b() throws Exception {
        doMemberFixTest(
                "package test;\n" +
                "enum Zima {LEDEN, UNOR}\n",
                
                "package test;\n" +
                "enum Zima {LEDEN,\n" +
                "    /**\n" +
                "     *\n" +
                "     */\n" +
                "    UNOR}\n", 2);
    }

}
