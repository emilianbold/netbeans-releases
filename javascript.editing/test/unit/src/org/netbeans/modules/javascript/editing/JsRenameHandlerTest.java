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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

/**
 *
 * @author Tor Norbye
 */
public class JsRenameHandlerTest extends JsTestBase {
    
    public JsRenameHandlerTest(String testName) {
        super(testName);
    }

    public void testRename1() throws Exception {
        checkRenameSections("testfiles/rename.js", "x^xx");
    }

    public void testRename2() throws Exception {
        checkRenameSections("testfiles/rename.js", "function a^aa() {");
    }

    public void testRename3() throws Exception {
        checkRenameSections("testfiles/rename.js", "var y^yy");
    }

    public void testRename4() throws Exception {
        checkRenameSections("testfiles/rename.js", "b^bb:");
    }

    public void testRename5() throws Exception {
        checkRenameSections("testfiles/rename.js", "function(p^pp)");
    }

    public void testRename6() throws Exception {
        checkRenameSections("testfiles/rename.js", "alert(p^pp)");
    }

    public void testRename7() throws Exception {
        checkRenameSections("testfiles/rename.js", "al^ert(ppp)");
    }

    public void testRename8() throws Exception {
        checkRenameSections("testfiles/rename.js", "funct^ion");
    }

    public void testRename9a() throws Exception {
        checkRenameSections("testfiles/webuifunc.js", "function(dom^Node, props");
    }

    public void testRename9b() throws Exception {
        checkRenameSections("testfiles/webuifunc.js", "@param {Node} dom^Node The DOM node");
    }

    public void testRename9c() throws Exception {
        checkRenameSections("testfiles/webuifunc.js", "if (dom^Node == null || props == null) {");
    }

    public void testRename10() throws Exception {
        checkRenameSections("testfiles/webuifunc.js", "function(domNode, pro^ps");
    }

    public void testRename11a() throws Exception {
        checkRenameSections("testfiles/webuifunc.js", "third^param)");
    }

    public void testRename11b() throws Exception {
        checkRenameSections("testfiles/webuifunc.js", "{Object}  third^param");
    }

    public void testRename12() throws Exception {
        checkRenameSections("testfiles/webuifunc.js", "@param f^oo");
    }

    public void testRename13() throws Exception {
        checkRenameSections("testfiles/webuifunc.js", "* n^ew @param");
    }

    public void testRename14() throws Exception {
        checkRenameSections("testfiles/webuifunc.js", " @param test^param");
    }

    public void testOffsets136162() throws Exception {
        checkRenameSections("testfiles/rename2.js", "f^oo = \"bar\";");
    }

    public void test137522() throws Exception {
        checkRenameSections("testfiles/137522.js", "updated.each(function(it^em)");
    }

    public void test137522b() throws Exception {
        checkRenameSections("testfiles/137522.js", " test_func_2(it^em);");
    }

    public void testNesting() throws Exception {
        checkRenameSections("testfiles/nesting.js", "otherfunc(elem^ent) ");
    }
}
