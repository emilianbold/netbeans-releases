/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.doc.jsdoc;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.editor.doc.jsdoc.model.DescriptionElement;
import org.netbeans.modules.javascript2.editor.doc.jsdoc.model.JsDocElement;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocParserTest extends JsTestBase {

    public JsDocParserTest(String name) {
        super(name);
    }

    private static final JsDocElement.Type[] expectedTypes = new JsDocElement.Type[] {
        // context sensitive type
        JsDocElement.Type.CONTEXT_SENSITIVE,

        // classic types
        JsDocElement.Type.ARGUMENT, JsDocElement.Type.AUGMENTS, JsDocElement.Type.AUTHOR, JsDocElement.Type.BORROWS,
        JsDocElement.Type.CLASS, JsDocElement.Type.CONSTANT, JsDocElement.Type.CONSTRUCTOR, JsDocElement.Type.CONSTRUCTS,
        JsDocElement.Type.DEFAULT, JsDocElement.Type.DEPRECATED, JsDocElement.Type.DESCRIPTION, JsDocElement.Type.EVENT,
        JsDocElement.Type.EXAMPLE, JsDocElement.Type.EXTENDS, JsDocElement.Type.FIELD, JsDocElement.Type.FILE_OVERVIEW,
        JsDocElement.Type.FUNCTION, JsDocElement.Type.IGNORE, JsDocElement.Type.INNER, JsDocElement.Type.LENDS,
        JsDocElement.Type.MEMBER_OF, JsDocElement.Type.NAME, JsDocElement.Type.NAMESPACE,
        JsDocElement.Type.PARAM, JsDocElement.Type.PRIVATE, JsDocElement.Type.PROPERTY, JsDocElement.Type.PUBLIC,
        JsDocElement.Type.REQUIRES, JsDocElement.Type.RETURN, JsDocElement.Type.RETURNS, JsDocElement.Type.SEE,
        JsDocElement.Type.SINCE, JsDocElement.Type.STATIC, JsDocElement.Type.THROWS, JsDocElement.Type.TYPE,
        JsDocElement.Type.VERSION
    };

    public void testCleanElementText01() throws Exception {
        String text = "/** \n"
            + " * Construct a new Shape object.\n"
            + " * ";

        String cleanedText = JsDocParser.cleanElementText(text);
        assertEquals("Construct a new Shape object.", cleanedText);
    }

    public void testCleanElementText02() throws Exception {
        String text = "@class This is the basic Shape class.\n"
            + " * It can be considered an abstract class, even though no such thing\n"
            + " * really existing in JavaScript\n";

        String cleanedText = JsDocParser.cleanElementText(text);
        assertEquals("@class This is the basic Shape class. It can be considered an abstract "
                + "class, even though no such thing really existing in JavaScript", cleanedText);
    }

    public void testCleanElementText03() throws Exception {
        String text = "@constructor\n"
            + " * ";

        String cleanedText = JsDocParser.cleanElementText(text);
        assertEquals("@constructor", cleanedText);
    }

    public void testCleanElementText04() throws Exception {
        String text = "@throws MemoryException if there is no more memory\n"
            + " * ";

        String cleanedText = JsDocParser.cleanElementText(text);
        assertEquals("@throws MemoryException if there is no more memory", cleanedText);
    }

    public void testCleanElementText05() throws Exception {
        String text = "@throws MemoryException if there is no more memory\n"
            + "*";

        String cleanedText = JsDocParser.cleanElementText(text);
        assertEquals("@throws MemoryException if there is no more memory", cleanedText);
    }

    public void testCleanElementText06() throws Exception {
        String text = "@return A new shape\n"
            + "*/";

        String cleanedText = JsDocParser.cleanElementText(text);
        assertEquals("@return A new shape", cleanedText);
    }

    public void testCleanElementText07() throws Exception {
        String text = "@return A new shape\n"
            + "   */";

        String cleanedText = JsDocParser.cleanElementText(text);
        assertEquals("@return A new shape", cleanedText);
    }

    public void testCleanElementText08() throws Exception {
        String text = "/**\n"
            + " * Get the value of the height for the Rectangle.\n"
            + " * Another getter is the {@link Shape#getColor} method in the\n"
            + " * {@link Shape base Shape class}.";

        String cleanedText = JsDocParser.cleanElementText(text);
        assertEquals("Get the value of the height for the Rectangle. Another getter is the "
                + "{@link Shape#getColor} method in the {@link Shape base Shape class}.", cleanedText);
    }

    public void testCleanElementText09() throws Exception {
        String text = "/** I am Chuck Noris */";

        String cleanedText = JsDocParser.cleanElementText(text);
        assertEquals("I am Chuck Noris", cleanedText);
    }

    public void testCleanElementText10() throws Exception {
        String text = "/**\n"
            + " * @nevim\n"
            + " **/";

        String cleanedText = JsDocParser.cleanElementText(text);
        assertEquals("@nevim", cleanedText);
    }

    public void testTagPatternOnFirstCommentText() throws Exception {
        String comment = "/**\n"
            + " * Construct a new Shape object.\n"
            + " * @class This is the basic Shape class.";
        assertEquals(
                "Construct a new Shape object.",
                getFirstPatternMatch(comment));
    }

    public void testTagPatternOnJsDocElement() throws Exception {
        String comment = "/** @throws MemoryException if there is no more memory\n"
            + " * @throws GeneralShapeException rarely (if ever)\n"
            + " * ";
        assertEquals(
                "@throws MemoryException if there is no more memory",
                getFirstPatternMatch(comment));
    }

    public void testTagPatternForLastElement() throws Exception {
        String comment = "/** @return A new shape\n"
            + " */";
        assertEquals(
                "@return A new shape",
                getFirstPatternMatch(comment));
    }

    public void testTagPatternForElementWithLink() throws Exception {
        String comment = "/**\n"
            + " * Get the value of the height for the Rectangle.\n"
            + " * Another getter is the {@link Shape#getColor} method in the\n"
            + " * {@link Shape base Shape class}.\n"
            + " * @return The height of this Rectangle\n";
        assertEquals(
                "Get the value of the height for the Rectangle. Another getter is the "
                + "{@link Shape#getColor} method in the {@link Shape base Shape class}.",
                getFirstPatternMatch(comment));
    }

    public void testParsedTypesForAsterisksComment() throws Exception {
        checkElementTypes("testfiles/jsdoc/allTypesAsterisks.js");
    }

    public void testParsedTypesForNoAsteriskComment() throws Exception {
        checkElementTypes("testfiles/jsdoc/allTypesNoAsterisk.js");
    }

    public void testParsedContextSensitiveContentNoAsterisk() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/allTypesNoAsterisk.js"));
        List<? extends JsDocElement> tags = getFirstJsDocBlock(source.createSnapshot()).getTags();
        assertEquals(JsDocElement.Type.CONTEXT_SENSITIVE, tags.get(0).getType());
        assertEquals("This could be description", ((DescriptionElement) tags.get(0)).getDescription().toString());
    }

    public void testParsedContextSensitiveContentAsterisks() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/allTypesAsterisks.js"));
        List<? extends JsDocElement> tags = getFirstJsDocBlock(source.createSnapshot()).getTags();
        assertEquals(JsDocElement.Type.CONTEXT_SENSITIVE, tags.get(0).getType());
        assertEquals("This could be description", ((DescriptionElement) tags.get(0)).getDescription().toString());
    }

    public void testParsingLongComments() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/windowStub.js"));
        List<? extends JsDocElement> tags = getFirstJsDocBlock(source.createSnapshot()).getTags();
        assertEquals(JsDocElement.Type.CONTEXT_SENSITIVE, tags.get(0).getType());
    }

    private void checkElementTypes(String filePath) {
        Source source = getTestSource(getTestFile(filePath));
        List<? extends JsDocElement> tags = getFirstJsDocBlock(source.createSnapshot()).getTags();
        for (int i = 0; i < expectedTypes.length; i++) {
            assertEquals(expectedTypes[i], tags.get(i).getType());
        }
    }

    private JsDocBlock getFirstJsDocBlock(Snapshot snapshot) {
        Iterator<Entry<Integer, JsDocBlock>> iterator = JsDocParser.parse(snapshot).entrySet().iterator();
        return iterator.next().getValue();
    }

    private String getFirstPatternMatch(String text) {
        JsDocParser.CommentStrip[] partsOfComment = JsDocParser.getCleanedCommentStrips(text, -1);
        return partsOfComment[0].getStrip();
    }
}
