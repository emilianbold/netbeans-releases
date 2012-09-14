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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript2.editor.formatter;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.csl.api.test.CslTestBase.IndentPrefs;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
//import org.netbeans.modules.php.editor.lexer.PHPTokenId;
//import org.netbeans.modules.php.smarty.editor.TplKit;
//import org.netbeans.modules.php.smarty.editor.indent.TplIndentTaskFactory;
//import org.netbeans.modules.php.smarty.editor.lexer.TplTokenId;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

public class JsFormatterEmbeddedTest extends JsTestBase {

    public JsFormatterEmbeddedTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AbstractIndenter.inUnitTestRun = true;

        MockMimeLookup.setInstances(MimePath.parse("text/javascript"), JsTokenId.javascriptLanguage());
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory, new HtmlKit("text/html"), HTMLTokenId.language());
//        MockMimeLookup.setInstances(MimePath.parse("text/php"), PHPTokenId.language());
//        TplIndentTaskFactory tplReformatFactory = new TplIndentTaskFactory();
//        MockMimeLookup.setInstances(MimePath.parse("text/x-tpl"), tplReformatFactory, new TplKit("text/x-tpl"), TplTokenId.language());
    }

    @Override
    protected BaseDocument getDocument(FileObject fo, String mimeType, Language language) {
        // for some reason GsfTestBase is not using DataObjects for BaseDocument construction
        // which means that for example Java formatter which does call EditorCookie to retrieve
        // document will get difference instance of BaseDocument for indentation
        try {
             DataObject dobj = DataObject.find(fo);
             assertNotNull(dobj);

             EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
             assertNotNull(ec);

             return (BaseDocument)ec.openDocument();
        }
        catch (Exception ex){
            fail(ex.toString());
            return null;
        }
    }

    public void testEmbeddedSimple1() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedSimple1.html", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple2() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedSimple2.html", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple3() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedSimple3.html", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple4() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedSimple4.html", new IndentPrefs(4,4));
    }

    public void testEmbeddedTrimmed1() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedTrimmed1.html", new IndentPrefs(4,4));
    }
    
    public void testEmbeddedMultipleSections1() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedMultipleSections1.html", new IndentPrefs(4,4));
    }

    public void testEmbeddedMultipleSections2() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedMultipleSections2.html", new IndentPrefs(4,4));
    }

    // XXX also see org.netbeans.modules.php.editor.js.JsFormatterEmbeddedTest
}
