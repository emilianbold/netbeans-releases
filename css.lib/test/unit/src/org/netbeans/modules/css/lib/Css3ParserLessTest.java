/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import static junit.framework.Assert.assertEquals;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.css.lib.api.*;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class Css3ParserLessTest extends CssTestBase {

    public Css3ParserLessTest(String testName) {
        super(testName);
    }

//     public static Test suite() throws IOException, BadLocationException {
//        System.err.println("Beware, only selected tests runs!!!");
//        TestSuite suite = new TestSuite();
//        suite.addTest(new Css3ParserTest("testIssue211103"));
//        return suite;
//    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        CssParserResult.IN_UNIT_TESTS = true;
    }

    public void testAllANTLRRulesHaveNodeTypes() {
        for (String rule : Css3Parser.ruleNames) {
            assertNotNull(NodeType.valueOf(rule));
        }
    }

    public void testLessVariable() {
        String source = "@color: #4D926F;\n"
                + "\n"
                + "#header {\n"
                + "  color: @color;\n"
                + "}\n"
                + "h2 {\n"
                + "  color: @color;\n"
                + "}";

        CssParserResult result = TestUtil.parse(source);

        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

//        assertEquals(0, result.getDiagnostics().size());

    }

    public void testLessFunction() {
        String source =
                "#header {\n"
                + "  color: (@base-color * 3);\n"
                + "  border-left: @the-border;\n"
                + "  border-right: (@the-border * 2);\n"
                + "}\n";

        CssParserResult result = TestUtil.parse(source);

        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

//        assertEquals(0, result.getDiagnostics().size());

    }

    public void testLessFunction2() {
        String source =
                "#footer {\n"
                + "  color: (@base-color + #003300);\n"
                + "  border-color: desaturate(@red, 10%);\n"
                + "}";
        ;

        CssParserResult result = TestUtil.parse(source);

        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

//        assertEquals(0, result.getDiagnostics().size());

    }

    public void testLessMixinDeclaration() {
        String source =
                ".rounded-corners (@radius: 5px) {\n"
                + "  -webkit-border-radius: @radius;\n"
                + "  -moz-border-radius: @radius;\n"
                + "  -ms-border-radius: @radius;\n"
                + "  -o-border-radius: @radius;\n"
                + "  border-radius: @radius;\n"
                + "}";
        ;

        CssParserResult result = TestUtil.parse(source);

        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

//        assertEquals(0, result.getDiagnostics().size());

    }

    public void testLessMixinDeclaration2() {
        String source =
                ".box-shadow (@x: 0, @y: 0, @blur: 1px, @color: #000) {\n"
                + "  box-shadow: @arguments;\n"
                + "  -moz-box-shadow: @arguments;\n"
                + "  -webkit-box-shadow: @arguments;\n"
                + "}";
        ;

        CssParserResult result = TestUtil.parse(source);

        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

//        assertEquals(0, result.getDiagnostics().size());

    }

    public void testLessMixinDeclarationAdvancedArguments() {
        String source =
                ".mixin1 (...) {}"
                + ".mixin2 () {}"
                + ".mixin3 (@a: 1) {}"
                + ".mixin4 (@a: 1, ...) {}"
                + ".mixin5 (@a, ...) {}";
        ;

        CssParserResult result = TestUtil.parse(source);

        NodeUtil.dumpTree(result.getParseTree());
        assertResultOK(result);

//        assertEquals(0, result.getDiagnostics().size());

    }
}
