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

package org.netbeans.modules.javascript.editing;

import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;

/**
 * Test JavaScript completion in other files
 *
 * @author Tor Norbye
 */
public class HtmlCompletionTest extends JsTestBase {

    public HtmlCompletionTest(String testName) {
        super(testName);


        // Don't truncate in unit tests; it's non-deterministic which items we end up
        // with coming out of the index so golden file diffing doesn't work
        JsIndex.MAX_SEARCH_ITEMS = Integer.MAX_VALUE;
        JsCodeCompletion.MAX_COMPLETION_ITEMS = Integer.MAX_VALUE;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        try {
            TestLanguageProvider.register(HTMLTokenId.language());
        } catch (IllegalStateException ise) {
            // Already registered?
        }
    }

    @Override
    public BaseDocument getDocument(String s, final String mimeType, final Language language) {
        BaseDocument doc = super.getDocument(s, mimeType, language);
        doc.putProperty("mimeType", "text/html");
        doc.putProperty(org.netbeans.api.lexer.Language.class, HTMLTokenId.language());

        return doc;
    }

    @Override
    protected String getPreferredMimeType() {
        return "text/html";
    }

    public void test1() throws Exception {
        checkCompletion("testfiles/completion/lib/rails-index.html", "$('^search-text').", false);
    }

    public void test2() throws Exception {
        checkCompletion("testfiles/completion/lib/rails-index.html", "$$('^ul", false);
    }

    public void test3() throws Exception {
        checkCompletion("testfiles/completion/lib/rails-index.html", "$$('li:^f');", false);
    }

    public void test4() throws Exception {
        checkCompletion("testfiles/completion/lib/rails-index.html", "$$('ul.^');", false);
    }

    public void test5() throws Exception {
        checkCompletion("testfiles/completion/lib/rails-index.html", "$$('li:f^');", false);
    }

    public void test6() throws Exception {
        checkCompletion("testfiles/completion/lib/rails-index.html", "x = getElementById('^')", false);
    }

    public void test7() throws Exception {
        checkCompletion("testfiles/completion/lib/rails-index.html", "x = getElementsByName('^')", false);
    }

    public void test8() throws Exception {
        checkCompletion("testfiles/completion/lib/rails-index.html", "x = addClass('^')", false);
    }

    public void test9() throws Exception {
        checkCompletion("testfiles/completion/lib/rails-index.html", "x = toggleClass('^')", false);
    }

    public void test10() throws Exception {
        checkCompletion("testfiles/completion/lib/rails-index.html", "x = getElementsByTagName('^')", false);
    }
}
