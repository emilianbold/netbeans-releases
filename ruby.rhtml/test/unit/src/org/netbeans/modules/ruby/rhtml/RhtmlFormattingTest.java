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
package org.netbeans.modules.ruby.rhtml;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;

/**
 *
 * @author Tor Norbye
 */
public class RhtmlFormattingTest extends RhtmlTestBase {
    public RhtmlFormattingTest(String testName) {
        super(testName);
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

    @Override
    protected String getPreferredMimeType() {
        return RubyInstallation.RUBY_MIME_TYPE;
    }

    public void reformatFileContents(String file) throws Exception {
        reformatFileContents(file, new IndentPrefs(2,2));
    }
    
    public void testFormat1() throws Exception {
        reformatFileContents("testfiles/format1.rhtml");
    }
    
    public void testFormat2() throws Exception {
        // this test fails for me because of #159952
        reformatFileContents("testfiles/format2.rhtml");
    }

    public void testFormat2b() throws Exception {
        // Same as format2.rhtml, but flushed left to ensure that
        // we're not reformatting correctly just by luck
        reformatFileContents("testfiles/format2b.rhtml");
    }

    public void testFormat3() throws Exception {
        reformatFileContents("testfiles/format3.rhtml");
    }

    public void testFormat4() throws Exception {
        reformatFileContents("testfiles/format4.rhtml");
    }

//    public void testFormat5() throws Exception {
//        format("<%\ndef foo\nwhatever\nend\n%>\n",
//                "<%\ndef foo\n  whatever\nend\n%>\n", null);
//    }
//
//    public void testFormat6() throws Exception {
//        format("<% if true %>\nhello\n%<= foo %>\n<% end %>\n",
//                "<% if true %>\n  hello\n%  <= foo %>\n<% end %>\n", null);
//    }
//
//    public void testFormat7() throws Exception {
//        format("<% foo %><% if true %>\nhello\n%<= foo %>\n<% end %>\n",
//                "<% foo %><% if true %>\n  hello\n%  <= foo %>\n<% end %>\n", null);
//    }
}
