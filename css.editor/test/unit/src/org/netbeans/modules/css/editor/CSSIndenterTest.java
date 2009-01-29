/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.css.editor;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.junit.MockServices;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.css.editor.test.TestBase;
import org.netbeans.modules.css.lexer.api.CSSTokenId;
import org.netbeans.modules.gsf.GsfIndentTaskFactory;
import org.netbeans.modules.gsf.GsfTestBase.IndentPrefs;
import org.netbeans.modules.html.editor.NbReaderProvider;
import org.netbeans.modules.java.source.save.Reformatter;
import org.openide.util.Lookup;

/**
 */
public class CSSIndenterTest extends TestBase {

    public CSSIndenterTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        NbReaderProvider.setupReaders();

        MockServices.setServices(TestLanguageProvider.class, MockMimeLookup.class);
        // init TestLanguageProvider
        Lookup.getDefault().lookup(TestLanguageProvider.class);
        TestLanguageProvider.register(CSSTokenId.language());
        TestLanguageProvider.register(HTMLTokenId.language());
        TestLanguageProvider.register(JspTokenId.language());
        TestLanguageProvider.register(JavaTokenId.language());

        GsfIndentTaskFactory jspReformatFactory = new GsfIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-jsp"), jspReformatFactory);
        GsfIndentTaskFactory htmlReformatFactory = new GsfIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory);
        Reformatter.Factory factory = new Reformatter.Factory();
        MockMimeLookup.setInstances(MimePath.parse("text/x-java"), factory);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testFormatting() throws Exception {
        format("a{\nbackground: red,\nblue;\n  }\n",
               "a{\n    background: red,\n        blue;\n}\n", null);
        format("a{     background: green,\nyellow;\n      }\n",
               "a{     background: green,\n           yellow;\n}\n", null);

        // comments formatting:
        format("a{\n/*comme\n  *dddsd\n nt*/\nbackground: red;\n}",
               "a{\n    /*comme\n      *dddsd\n     nt*/\n    background: red;\n}", null);

        // even though lines are preserved they will be indented according to indent of previous line;
        // I'm not sure it is OK but leaving like that for now:
        format("a{\ncolor: red; /* start\n   comment\n end*/ background: blue;\n}",
               "a{\n    color: red; /* start\n       comment\n     end*/ background: blue;\n}", null);

        // formatting of last line:
        format("a{\nbackground: red,",
               "a{\n    background: red,", null);
        format("a{\nbackground: red,\n",
               "a{\n    background: red,\n", null);
    }

    public void testEmbeddedFormatting() throws Exception {
        reformatFileContents("testfiles/format1.jsp", "text/x-jsp", JspTokenId.language(), new IndentPrefs(4,4));
    }

    public void testFormattingNetBeansCSS() throws Exception {
        reformatFileContents("testfiles/netbeans.css",new IndentPrefs(4,4));
    }

    public void testIndentation() throws Exception {
        // property indentation:
        insertNewline("a{^background: red;\n  }\n", "a{\n    ^background: red;\n  }\n", null);
        insertNewline("a{\n    background: red;\n}\na{\n    background: red;^\n}", "a{\n    background: red;\n}\na{\n    background: red;\n    ^\n}", null);
        insertNewline("a{background: red;}\nb{^", "a{background: red;}\nb{\n    ^", null);
        insertNewline("a {       background: red;^a: b;", "a {       background: red;\n          ^a: b;", null);
        insertNewline("a {       background: red,^blue", "a {       background: red,\n              ^blue", null);
        // value indentation:
        insertNewline("a{\n    background:^red;\n  }\n", "a{\n    background:\n        ^red;\n  }\n", null);
        insertNewline("a { background: red,^blue; }", "a { background: red,\n        ^blue; }", null);
        // indentation of end of line:
        insertNewline("a { background: red,^", "a { background: red,\n        ^", null);
        insertNewline("a { background: red,^\n", "a { background: red,\n        ^\n", null);
        // property indentation:
        insertNewline("a{\n    background: red;^\n  }\n", "a{\n    background: red;\n    ^\n  }\n", null);
        // new rule indentation:
        insertNewline("a{\n    background: red;\n  }^", "a{\n    background: red;\n  }\n  ^", null);
    }

}
