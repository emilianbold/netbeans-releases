/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.codegen;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.html.editor.test.TestBase;

/**
 *
 * @author daniel
 */
public class LoremIpsumGeneratorTest extends TestBase {

    private BaseDocument doc;
    private List<String> paragraphs = new ArrayList<String>();

    public LoremIpsumGeneratorTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() {
        doc = createDocument();
        paragraphs.add("one");
        paragraphs.add("two");
    }

    public void testMinimalDoc() throws Exception {
        String originalText = "<html><head></head><body>text</body></html>";
        doc.insertString(0, originalText, null);
        int insertPosition = originalText.indexOf("text");
        LoremIpsumGenerator.insertLoremIpsum(doc, paragraphs, "<p>", "    ", insertPosition);
        assertEquals("<html><head></head><body>"
                + "\n<p>\n    one\n</p>\n"
                + "<p>\n    two\n</p>\n"
                + "text</body></html>", new String(doc.getChars(0, doc.getLength())));
    }

    public void testMinimalDoc_DifferentTag() throws Exception {
        String originalText = "<html><head></head><body>text</body></html>";
        doc.insertString(0, originalText, null);
        int insertPosition = originalText.indexOf("text");
        LoremIpsumGenerator.insertLoremIpsum(doc, paragraphs, "<div>", "    ", insertPosition);
        assertEquals("<html><head></head><body>"
                + "\n<div>\n    one\n</div>\n"
                + "<div>\n    two\n</div>\n"
                + "text</body></html>", new String(doc.getChars(0, doc.getLength())));
    }

    public void testInsertBeforeNewline() throws Exception {
        String originalText = "<html><head></head>\n    <body>\ntext</body></html>";
        doc.insertString(0, originalText, null);
        int insertPosition = originalText.indexOf("text") - 1;
        LoremIpsumGenerator.insertLoremIpsum(doc, paragraphs, "<p>", "    ", insertPosition);
        assertEquals("<html><head></head>\n    <body>"
                + "\n        <p>\n            one\n        </p>"
                + "\n        <p>\n            two\n        </p>"
                + "\ntext</body></html>", new String(doc.getChars(0, doc.getLength())));
    }

    public void testInsertWithIndent() throws Exception {
        String originalText = "<html><head></head><body>\n    <p>text</p>\n</body></html>";
        doc.insertString(0, originalText, null);
        int insertPosition = originalText.indexOf("text");
        LoremIpsumGenerator.insertLoremIpsum(doc, paragraphs, "<p>", "  ", insertPosition);
        assertEquals("<html><head></head><body>"
                + "\n    <p>"
                + "\n      <p>\n        one\n      </p>"
                + "\n      <p>\n        two\n      </p>"
                + "\ntext</p>\n</body></html>", new String(doc.getChars(0, doc.getLength())));
    }

    public void testInsertWithIndentAndSelfClosingTag() throws Exception {
        String originalText = "<html><head></head><body>\n    <br/>text\n</body></html>";
        doc.insertString(0, originalText, null);
        int insertPosition = originalText.indexOf("text");
        LoremIpsumGenerator.insertLoremIpsum(doc, paragraphs, "<p>", "  ", insertPosition);
        assertEquals("<html><head></head><body>"
                + "\n    <br/>"
                + "\n    <p>\n      one\n    </p>"
                + "\n    <p>\n      two\n    </p>"
                + "\ntext\n</body></html>", new String(doc.getChars(0, doc.getLength())));
    }

    public void testInsertWithIndentAndClosingTag() throws Exception {
        String originalText = "<html><head></head><body><div>\n    </div>text\n</body></html>";
        doc.insertString(0, originalText, null);
        int insertPosition = originalText.indexOf("text");
        LoremIpsumGenerator.insertLoremIpsum(doc, paragraphs, "<p>", "  ", insertPosition);
        assertEquals("<html><head></head><body><div>"
                + "\n    </div>"
                + "\n  <p>\n    one\n  </p>"
                + "\n  <p>\n    two\n  </p>"
                + "\ntext\n</body></html>", new String(doc.getChars(0, doc.getLength())));
    }

    public void testInsertWithIndentAndClosingTagAndMixedIdentText() throws Exception {
        String originalText = "<html><head></head><body><div>\n \t  </div>text\n</body></html>";
        doc.insertString(0, originalText, null);
        int insertPosition = originalText.indexOf("text");
        LoremIpsumGenerator.insertLoremIpsum(doc, paragraphs, "<p>", "  ", insertPosition);
        assertEquals("<html><head></head><body><div>"
                + "\n \t  </div>"
                + "\n \t  <p>\n \t    one\n \t  </p>"
                + "\n \t  <p>\n \t    two\n \t  </p>"
                + "\ntext\n</body></html>", new String(doc.getChars(0, doc.getLength())));
    }

    public void testInsertDirectlyAfterIndent() throws Exception {
        String originalText = "<html><head></head><body>\n    text\n</body></html>";
        doc.insertString(0, originalText, null);
        int insertPosition = originalText.indexOf("text");
        LoremIpsumGenerator.insertLoremIpsum(doc, paragraphs, "<p>", "  ", insertPosition);
        assertEquals("<html><head></head><body>"
                + "\n    <p>\n      one\n    </p>"
                + "\n    <p>\n      two\n    </p>"
                + "\n    text\n</body></html>", new String(doc.getChars(0, doc.getLength())));
    }

    public void testInsertInsideIndent() throws Exception {
        String originalText = "<html><head></head><body>\n    text\n</body></html>";
        doc.insertString(0, originalText, null);
        int insertPosition = originalText.indexOf("text") - 2;
        LoremIpsumGenerator.insertLoremIpsum(doc, paragraphs, "<p>", "  ", insertPosition);
        assertEquals("<html><head></head><body>"
                + "\n    <p>\n      one\n    </p>"
                + "\n    <p>\n      two\n    </p>"
                + "\n    text\n</body></html>", new String(doc.getChars(0, doc.getLength())));
    }
}
