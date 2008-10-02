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

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.ruby.rhtml.RhtmlIndentTaskFactory;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;

/**
 *
 * @author Tor Norbye
 */
public class EmbeddedFormatterTest extends JsTestBase {
    public EmbeddedFormatterTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        try {
            TestLanguageProvider.register(RhtmlTokenId.language());
        } catch (IllegalStateException ise) {
            // Already registered?
        }
        try {
            TestLanguageProvider.register(HTMLTokenId.language());
        } catch (IllegalStateException ise) {
            // Already registered?
        }
        try {
            TestLanguageProvider.register(JsTokenId.language());
        } catch (IllegalStateException ise) {
            // Already registered?
        }

        RhtmlIndentTaskFactory rhtmlReformatFactory = new RhtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse(RubyInstallation.RHTML_MIME_TYPE), rhtmlReformatFactory);
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory);
    }

    @Override
    public BaseDocument getDocument(String s, final String mimeType, final Language language) {
        BaseDocument doc = super.getDocument(s, mimeType, language);
        doc.putProperty("mimeType", RubyInstallation.RHTML_MIME_TYPE);
        doc.putProperty(org.netbeans.api.lexer.Language.class, RhtmlTokenId.language());

        return doc;
    }

    public void testFormat1() throws Exception {
        reformatFileContents("testfiles/embedding/issue146936.erb", new IndentPrefs(2,2));
    }

    public void testFormat2() throws Exception {
        reformatFileContents("testfiles/embedding/embed124916.erb", new IndentPrefs(2,2));
    }

    public void testFormat3() throws Exception {
        reformatFileContents("testfiles/embedding/issue136495.erb", new IndentPrefs(2,2));
    }

    public void testFormat4() throws Exception {
        reformatFileContents("testfiles/embedding/mixed.erb", new IndentPrefs(2,2));
    }
}
