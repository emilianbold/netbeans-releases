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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.editor.ext.html.parser;

import java.util.List;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.text.BadLocationException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.editor.ext.html.test.TestBase;
import org.netbeans.junit.MockServices;

/** SyntaxParser unit tests
 *
 * @author Marek Fukala
 */
public class SyntaxParserTest extends TestBase {

    private static final LanguagePath languagePath = LanguagePath.get(HTMLTokenId.language());

    public SyntaxParserTest(String testName) throws IOException, BadLocationException {
        super(testName);
    }

    public static Test xsuite() throws IOException, BadLocationException {
        TestSuite suite = new TestSuite();
        suite.addTest(new SyntaxParserTest("testParseBrokenSource"));
        suite.addTest(new SyntaxParserTest("testParseBrokenSource2"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Logger.getLogger(SyntaxParser.class.getName()).setLevel(Level.FINE);
        MockServices.setServices(MockMimeLookup.class);
    }

    public void testOpenTag() throws BadLocationException {
        String text = "<div>";
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(1, elements.size());

        SyntaxElement div = elements.get(0);

        assertNotNull(div);
        assertEquals(SyntaxElement.TYPE_TAG, div.type());
        assertTrue(div instanceof SyntaxElement.Tag);

        SyntaxElement.Tag divTag = (SyntaxElement.Tag) div;

        assertEquals("div", divTag.getName());
        assertFalse(divTag.isEmpty());
        assertTrue(divTag.isOpenTag());
        assertEquals(0, divTag.offset());
        assertEquals(text.length(), divTag.length());
        assertEquals(text, divTag.text());

    }

    public void testEndTag() throws BadLocationException {
        String text = "</div>";
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(1, elements.size());

        SyntaxElement div = elements.get(0);

        assertNotNull(div);
        assertEquals(SyntaxElement.TYPE_ENDTAG, div.type());
        assertTrue(div instanceof SyntaxElement.Tag);

        SyntaxElement.Tag divTag = (SyntaxElement.Tag) div;

        assertEquals("div", divTag.getName());
        assertFalse(divTag.isEmpty());
        assertFalse(divTag.isOpenTag());
        assertEquals(0, divTag.offset());
        assertEquals(text.length(), divTag.length());
        assertEquals(text, divTag.text());

    }

    public void testTagWithOneAttribute() throws BadLocationException {
        String text = "<div align=\"center\"/>";
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(1, elements.size());

        SyntaxElement div = elements.get(0);

        assertNotNull(div);
        assertEquals(SyntaxElement.TYPE_TAG, div.type());
        assertTrue(div instanceof SyntaxElement.Tag);

        SyntaxElement.Tag divTag = (SyntaxElement.Tag) div;

        assertEquals("div", divTag.getName());
        assertTrue(divTag.isEmpty());
        assertTrue(divTag.isOpenTag());
        assertEquals(0, divTag.offset());
        assertEquals(text.length(), divTag.length());
        assertEquals(text, divTag.text());

        List<SyntaxElement.TagAttribute> attributes = divTag.getAttributes();

        assertNotNull(attributes);
        assertEquals(1, attributes.size());

        SyntaxElement.TagAttribute align = attributes.get(0);

        assertEquals("align", align.getName());
        assertEquals(5, align.getNameOffset());
        assertEquals("\"center\"", align.getValue());
        assertEquals(11, align.getValueOffset());
        assertEquals("\"center\"".length(), align.getValueLength());

    }

    public void testTagWithUnquotedAttribute() throws BadLocationException {
        String text = "<div align=center/>";
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(1, elements.size());

        SyntaxElement div = elements.get(0);

        assertNotNull(div);
        assertEquals(SyntaxElement.TYPE_TAG, div.type());
        assertTrue(div instanceof SyntaxElement.Tag);

        SyntaxElement.Tag divTag = (SyntaxElement.Tag) div;

        assertEquals("div", divTag.getName());
        assertTrue(divTag.isEmpty());
        assertTrue(divTag.isOpenTag());
        assertEquals(0, divTag.offset());
        assertEquals(text.length(), divTag.length());
        assertEquals(text, divTag.text());

        List<SyntaxElement.TagAttribute> attributes = divTag.getAttributes();

        assertNotNull(attributes);
        assertEquals(1, attributes.size());

        SyntaxElement.TagAttribute align = attributes.get(0);

        assertEquals("align", align.getName());
        assertEquals(5, align.getNameOffset());
        assertEquals("center", align.getValue());
        assertEquals(11, align.getValueOffset());
        assertEquals("center".length(), align.getValueLength());

    }

    //+ new line and tab in the tag and whitespaces around the equal operator
    public void testTagWithOneAttribute2() throws BadLocationException {
        String text = "<div \t \n align =\t \"center\"/>";
        //             012345 67 890123456 78 9012345 678
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(1, elements.size());

        SyntaxElement div = elements.get(0);

        assertNotNull(div);
        assertEquals(SyntaxElement.TYPE_TAG, div.type());
        assertTrue(div instanceof SyntaxElement.Tag);

        SyntaxElement.Tag divTag = (SyntaxElement.Tag) div;

        assertEquals("div", divTag.getName());
        assertTrue(divTag.isEmpty());
        assertTrue(divTag.isOpenTag());
        assertEquals(0, divTag.offset());
        assertEquals(text.length(), divTag.length());
        assertEquals(text, divTag.text());

        List<SyntaxElement.TagAttribute> attributes = divTag.getAttributes();

        assertNotNull(attributes);
        assertEquals(1, attributes.size());

        SyntaxElement.TagAttribute align = attributes.get(0);

        assertEquals("align", align.getName());
        assertEquals(9, align.getNameOffset());
        assertEquals("\"center\"", align.getValue());
        assertEquals(18, align.getValueOffset());
        assertEquals("\"center\"".length(), align.getValueLength());

    }

    public void testTagWithMoreAttributes() throws BadLocationException {
        String text = "<div align=\"center\" \t\n title=\"mydiv\" />";
        //             012345678901 2345678 90 1 23456789 012345 6789
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(1, elements.size());

        SyntaxElement div = elements.get(0);

        assertNotNull(div);
        assertEquals(SyntaxElement.TYPE_TAG, div.type());
        assertTrue(div instanceof SyntaxElement.Tag);

        SyntaxElement.Tag divTag = (SyntaxElement.Tag) div;

        assertEquals("div", divTag.getName());
        assertTrue(divTag.isEmpty());
        assertTrue(divTag.isOpenTag());
        assertEquals(0, divTag.offset());
        assertEquals(text.length(), divTag.length());
        assertEquals(text, divTag.text());

        List<SyntaxElement.TagAttribute> attributes = divTag.getAttributes();

        assertNotNull(attributes);
        assertEquals(2, attributes.size());

        SyntaxElement.TagAttribute attr = attributes.get(0);

        assertEquals("align", attr.getName());
        assertEquals(5, attr.getNameOffset());
        assertEquals("\"center\"", attr.getValue());
        assertEquals(11, attr.getValueOffset());
        assertEquals("\"center\"".length(), attr.getValueLength());

        attr = attributes.get(1);

        assertEquals("title", attr.getName());
        assertEquals(23, attr.getNameOffset());
        assertEquals("\"mydiv\"", attr.getValue());
        assertEquals(29, attr.getValueOffset());
        assertEquals("\"mydiv\"".length(), attr.getValueLength());

    }

    public void testEntityReference() throws BadLocationException {

        String text = "&nbsp; &amp;";
        //             012345678901
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(3, elements.size());

        SyntaxElement e1 = elements.get(0);
        SyntaxElement e2 = elements.get(2);

        assertNotNull(e1);
        assertNotNull(e2);

        assertEquals(SyntaxElement.TYPE_ENTITY_REFERENCE, e1.type());
        assertEquals(SyntaxElement.TYPE_ENTITY_REFERENCE, e2.type());

        assertEquals(0, e1.offset());
        assertEquals(7, e2.offset());

        assertEquals(6, e1.length());
        assertEquals(5, e2.length());

        assertEquals("&nbsp;", e1.text());
        assertEquals("&amp;", e2.text());

    }

    public void testComment() throws BadLocationException {
        String text = "<!-- comment -->";
        //             01234567890123456
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(1, elements.size());

        SyntaxElement e1 = elements.get(0);

        assertNotNull(e1);

        assertEquals(SyntaxElement.TYPE_COMMENT, e1.type());

        assertEquals(0, e1.offset());

        assertEquals(text.length(), e1.length());

        assertEquals(text, e1.text());

    }

    public void testMultipleComments() throws BadLocationException {
        String comments = "<!-- comment1 --><!-- comment2 -->";
        String text = comments + "\n";

        //"<!-- comment1 --><!-- comment2 -->\n"
        //             01234567890123456
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(2, elements.size());

        SyntaxElement e1 = elements.get(0);

        assertNotNull(e1);

        assertEquals(SyntaxElement.TYPE_COMMENT, e1.type());

        assertEquals(0, e1.offset());

        assertEquals(comments.length(), e1.length());

        assertEquals(comments, e1.text());

    }

    public void testMultipleCommentsSeparated() throws BadLocationException {
        String comment1 = "<!-- comment1 -->";
        String comment2 = "<!-- comment2 -->";
        String text = comment1 + "\t\n " + comment2 + "\n";
        //             <!-- comment1 -->\t\n <!-- comment2 -->\n
        //             012345678901234567 8 9012345678901234567 890
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(4, elements.size());

        SyntaxElement e1 = elements.get(0);
        SyntaxElement e2 = elements.get(2);

        assertNotNull(e1);
        assertNotNull(e2);

        assertEquals(SyntaxElement.TYPE_COMMENT, e1.type());
        assertEquals(SyntaxElement.TYPE_COMMENT, e2.type());

        assertEquals(0, e1.offset());
        assertEquals(20, e2.offset());

        assertEquals(comment1.length(), e1.length());
        assertEquals(comment2.length(), e2.length());

        assertEquals(comment1, e1.text());
        assertEquals(comment2, e2.text());

    }

    public void testSimpleSGMLDeclaration() throws BadLocationException {
        String text = "<!X Y Z>";
        //             0123456789
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(1, elements.size());

        SyntaxElement e1 = elements.get(0);

        assertNotNull(e1);

        assertEquals(SyntaxElement.TYPE_DECLARATION, e1.type());

        SyntaxElement.Declaration declaration = (SyntaxElement.Declaration) e1;

        assertEquals(0, e1.offset());

        assertEquals(text.length(), e1.length());

        assertEquals(text, e1.text());

        assertNull(declaration.getRootElement());
        assertNull(declaration.getPublicIdentifier());
        assertNull(declaration.getDoctypeFile());

    }

    public void testSGMLDeclaration() throws BadLocationException {
        String text = "<!X -- comment -- Y \n \t Z>";
        //             0123456789
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(1, elements.size());

        SyntaxElement e1 = elements.get(0);

        assertNotNull(e1);

        assertEquals(SyntaxElement.TYPE_DECLARATION, e1.type());

        SyntaxElement.Declaration declaration = (SyntaxElement.Declaration) e1;

        assertEquals(0, e1.offset());

        assertEquals(text.length(), e1.length());

        assertEquals(text, e1.text());

        assertNull(declaration.getRootElement());
        assertNull(declaration.getPublicIdentifier());
        assertNull(declaration.getDoctypeFile());

    }

    public void testDoctype() throws BadLocationException {
        String text = "<!DOCTYPE html \t PUBLIC \"id part 2\" \n \"file\">";
        //             0123456789
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(1, elements.size());

        SyntaxElement e1 = elements.get(0);

        assertNotNull(e1);

        assertEquals(SyntaxElement.TYPE_DECLARATION, e1.type());

        SyntaxElement.Declaration declaration = (SyntaxElement.Declaration) e1;

        assertEquals(0, e1.offset());

        assertEquals(text.length(), e1.length());

        assertEquals(text, e1.text());

        assertEquals("html", declaration.getRootElement());
        assertEquals("id part 2", declaration.getPublicIdentifier());
        assertEquals("\"file\"", declaration.getDoctypeFile());

    }

    public void testDoctypeSimplePublicId() throws BadLocationException {
        String text = "<!DOCTYPE html \t PUBLIC \"simpleid\" \n \"file\">";
        //             0123456789

        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(1, elements.size());

        SyntaxElement e1 = elements.get(0);

        assertNotNull(e1);

        assertEquals(SyntaxElement.TYPE_DECLARATION, e1.type());

        SyntaxElement.Declaration declaration = (SyntaxElement.Declaration) e1;

        assertEquals(0, e1.offset());

        assertEquals(text.length(), e1.length());

        assertEquals(text, e1.text());

        assertEquals("html", declaration.getRootElement());
        assertEquals("simpleid", declaration.getPublicIdentifier());
        assertEquals("\"file\"", declaration.getDoctypeFile());

    }

    public void testTagWithStyleAttributes() throws BadLocationException {
        String text = "<div style=\"color:red\"/>";
        //             012345678901 2345678 90 1 23456789 012345 6789

        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(1, elements.size());

        SyntaxElement div = elements.get(0);

        assertNotNull(div);
        assertEquals(SyntaxElement.TYPE_TAG, div.type());
        assertTrue(div instanceof SyntaxElement.Tag);

        SyntaxElement.Tag divTag = (SyntaxElement.Tag) div;

        List<SyntaxElement.TagAttribute> attributes = divTag.getAttributes();

        assertNotNull(attributes);
        assertEquals(1, attributes.size());

        SyntaxElement.TagAttribute attr = attributes.get(0);

        assertEquals("style", attr.getName());
        assertEquals(5, attr.getNameOffset());
        assertEquals("\"color:red\"", attr.getValue());
        assertEquals(11, attr.getValueOffset());
        assertEquals("\"color:red\"".length(), attr.getValueLength());


    }

    public void testParseText() throws BadLocationException {
        String text = "<div>text</div>last";
        //             012345678901 2345678 90 1 23456789 012345 6789
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals("Unexpected number of elements: " + elementsToString(elements), 4, elements.size());

        SyntaxElement se = elements.get(1);
        assertNotNull(se);
        assertEquals(SyntaxElement.TYPE_TEXT, se.type());
        assertEquals("text", se.text());

        se = elements.get(3);
        assertNotNull(se);
        assertEquals(SyntaxElement.TYPE_TEXT, se.type());
        assertEquals("last", se.text());
    }

    public void testParseNewLineText() throws BadLocationException {
        String text = "<div>\n</div>\nlast";
        //             012345678901 2345678 90 1 23456789 012345 6789
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals("Unexpected number of elements: " + elementsToString(elements), 4, elements.size());

        SyntaxElement se = elements.get(1);
        assertNotNull(se);
        assertEquals(SyntaxElement.TYPE_TEXT, se.type());

        se = elements.get(3);
        assertNotNull(se);
        assertEquals(SyntaxElement.TYPE_TEXT, se.type());
    }

    public void testParseBrokenSource() throws BadLocationException {
        String text = "<div></";
        //             012345678901 2345678 90 1 23456789 012345 6789
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(2, elements.size());

        SyntaxElement div = elements.get(0);

        assertNotNull(div);
        assertEquals(SyntaxElement.TYPE_TAG, div.type());
        assertTrue(div instanceof SyntaxElement.Tag);

        SyntaxElement error = elements.get(1);

        assertNotNull(error);
        assertEquals(SyntaxElement.TYPE_ERROR, error.type());

    }

    public void testParseBrokenSource2() throws BadLocationException {
        String text = "<div></</div>";
        //             012345678901 2345678 90 1 23456789 012345 6789
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(3, elements.size());

        SyntaxElement se = elements.get(0);
        assertNotNull(se);
        assertEquals(SyntaxElement.TYPE_TAG, se.type());
        assertTrue(se instanceof SyntaxElement.Tag);

        se = elements.get(1);
        assertNotNull(se);
        assertEquals(SyntaxElement.TYPE_ERROR, se.type());

        se = elements.get(2);
        assertNotNull(se);
        assertEquals(SyntaxElement.TYPE_ENDTAG, se.type());
        assertTrue(se instanceof SyntaxElement.Tag);

    }

    //we do not properly parse the end tag here
    public void testParseBrokenOpenTag() throws BadLocationException {
        String text = "<div align= </div>";
        //             012345678901 2345678 90 1 23456789 012345 6789
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        System.out.println(elementsToString(elements));

        assertNotNull(elements);
        assertEquals(2, elements.size());

        SyntaxElement div = elements.get(0);

        assertNotNull(div);
        assertEquals(SyntaxElement.TYPE_TAG, div.type());
        assertTrue(div instanceof SyntaxElement.Tag);

        SyntaxElement endtag = elements.get(1);

        assertNotNull(endtag);
        assertEquals(SyntaxElement.TYPE_TEXT, endtag.type());

    }

    //in this case the end tag should be properly parsed
    public void testParseBrokenOpenTag2() throws BadLocationException {
        String text = "<div align=@# </div>";
        //             012345678901 2345678 90 1 23456789 012345 6789
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        System.out.println(elementsToString(elements));

        assertNotNull(elements);
        assertEquals(2, elements.size());

        SyntaxElement div = elements.get(0);

        assertNotNull(div);
        assertEquals(SyntaxElement.TYPE_TAG, div.type());
        assertTrue(div instanceof SyntaxElement.Tag);

        SyntaxElement endtag = elements.get(1);

        assertNotNull(endtag);
        assertEquals(SyntaxElement.TYPE_ENDTAG, endtag.type());

    }

    public void testParseBrokenOpenTagAtFileEnd() throws BadLocationException {
        String text = "<div align=";
        //             012345678901 2345678 90 1 23456789 012345 6789
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(1, elements.size());

        SyntaxElement div = elements.get(0);

        assertNotNull(div);
        assertEquals(SyntaxElement.TYPE_TAG, div.type());
        assertTrue(div instanceof SyntaxElement.Tag);

    }

    //we do not properly parse the end tag here
    public void testParseUnfinishedOpenTag() throws BadLocationException {
        String text = "<col";
        //             01234567
        List<SyntaxElement> elements = SyntaxParser.parse(text).getElements();

        assertNotNull(elements);
        assertEquals(1, elements.size());

        SyntaxElement div = elements.get(0);

        assertNotNull(div);
        assertEquals(SyntaxElement.TYPE_TAG, div.type());
        assertTrue(div instanceof SyntaxElement.Tag);
        assertEquals("<col", div.text());
        assertEquals(4, div.length());

    }

    private static String elementsToString(List<SyntaxElement> elements) {
        StringBuffer sb = new StringBuffer();
        for(SyntaxElement se : elements) {
            sb.append(se.toString());
            sb.append("\n");
        }
        return sb.toString();
    }


}