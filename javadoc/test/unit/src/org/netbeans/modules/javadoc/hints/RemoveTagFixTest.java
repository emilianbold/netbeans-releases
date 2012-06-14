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

import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Jan Pokorsky
 */
public class RemoveTagFixTest extends JavadocTestSupport {

    public RemoveTagFixTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(RemoveTagFixTest.class);
//        suite.addTest(new RemoveTagFixTest("testRemoveReturnTagFix"));
        return suite;
    }

    public void testRemoveReturnTagFix() throws Exception {
        doMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @return \n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n");        
    }
    
    public void testRemoveReturnTagFixInLine() throws Exception {
        doMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /** @return bla */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /** */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n");        
    }

    public void testRemoveReturnTagFixHeaderLine() throws Exception {
        doMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /** @return bla\n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /** \n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n");        
    }

    public void testRemoveReturnTagFixTailLine() throws Exception {
        doMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @return bla */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n");        
    }

    public void testRemoveReturnTagAfterParamFix() throws Exception {
        doMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * @param a param a\n" +
                "     * @return \n" +
                "     */\n" +
                "    void leden(int a) {\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * @param a param a\n" +
                "     */\n" +
                "    void leden(int a) {\n" +
                "    }\n" +
                "}\n");        
    }

    public void testRemoveMultilineReturnTagAfterParamFix() throws Exception {
        doMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * @param a param a\n" +
                "     * @return bla bla\n" +
                "     *         bla bla bla bla\n" +
                "     */\n" +
                "    void leden(int a) {\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * @param a param a\n" +
                "     */\n" +
                "    void leden(int a) {\n" +
                "    }\n" +
                "}\n");        
    }

    public void testRemoveThrowsTagFix() throws Exception {
        doMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @throws java.io.IOException bla\n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n");        
    }

    public void testRemoveParamTagFix() throws Exception {
        doMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @param p1 description\n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n");        
    }

    public void testRemoveParamTagFix_124353() throws Exception {
        doMemberFixTest(
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @param p1 description\n" +
                "     * @return int\n" +
                "     */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n",
                
                "package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @return int\n" +
                "     */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n");        
    }

}
