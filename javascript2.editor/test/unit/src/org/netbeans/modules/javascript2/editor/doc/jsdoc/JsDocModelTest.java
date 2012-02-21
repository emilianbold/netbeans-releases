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

import java.util.*;
import org.netbeans.modules.javascript2.editor.doc.jsdoc.model.*;
import org.netbeans.modules.javascript2.editor.model.JsComment;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocModelTest extends JsDocTestBase {

    public JsDocModelTest(String testName) {
        super(testName);
    }

    /**
     * The string should look like: <type>|<key1>=<value1>:<key2>=<value2>;<type>
     */
    private static List<JsDocElement> parseExpected(String expected) {
        List<JsDocElement> elements = new ArrayList<JsDocElement>();
        String[] tags = expected.split("[;]+");
        for (String tag : tags) {
            String[] tmp = tag.split("[|]+");
            FakeJsDocElement element = new FakeJsDocElement(JsDocElement.Type.fromString(tmp[0]));
            String[] keyValues = tmp[1].split("[:]+");
            for (String keyValue : keyValues) {
                String[] items = keyValue.split("[=]+");
                if (items.length == 1) {
                    // in context sensitive cases
                    element.addProperty("desc", items[0]);
                } else {
                    element.addProperty(items[0], items[1]);
                }
            }
            elements.add(element);
        }
        return elements;
    }

    private static void checkJsDocElements(String expected, List<JsDocElement> elements) {
        List<JsDocElement> expectedTags = parseExpected(expected);
        assertElementsEquality(expectedTags, elements);

    }

    private static void checkJsDocBlock(Source source, final int offset, final String expected) throws Exception {
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                assertTrue(result instanceof JsParserResult);
                JsParserResult parserResult = (JsParserResult) result;

                JsDocDocumentationProvider documentationProvider = getDocumentationProvider(parserResult);
                JsComment comment = documentationProvider.getCommentForOffset(offset);
                assertTrue(comment instanceof JsDocBlock);

                JsDocBlock jsDoc = (JsDocBlock) comment;
                checkJsDocElements(expected, jsDoc.getTags());
            }
        });
    }

    public void testContextSensitiveDescription() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line1;");
        checkJsDocBlock(source, caretOffset, "contextSensitive|This could be description");
    }

    public void testArgument() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line2;");
        checkJsDocBlock(source, caretOffset, "@argument|type=paramType:name=paramName:desc=paramDescription");
    }

    /**
     * Examples of expected values:
     * @borrows <param1> as <param2>
     * @type <type>
     * @author <desc>
     * @memberOf <namepath>
     * @param <type> <name> <desc>
     * @private
     * @throws <type> <desc>
     */
    private static void assertElementEquality(FakeJsDocElement expected, JsDocElement parsed) {
        switch (parsed.getType().getCategory()) {
            case ASSIGN:
                assertTrue(parsed instanceof AssignElement);
                AssignElement assignElement = (AssignElement) parsed;
                assertEquals(expected.getProperty("param1"), assignElement.getThisMemberName().toString());
                assertEquals(expected.getProperty("param2"), assignElement.getOtherMemberName().toString());
                break;
            case DECLARATION:
                assertTrue(parsed instanceof DeclarationElement);
                DeclarationElement declarationElement = (DeclarationElement) parsed;
                assertEquals(expected.getProperty("type"), declarationElement.getDeclaredType().toString());
                break;
            case DESCRIPTION:
                assertTrue(parsed instanceof DescriptionElement);
                DescriptionElement descElement = (DescriptionElement) parsed;
                assertEquals(expected.getProperty("desc"), descElement.getDescription().toString());
                break;
            case LINK:
                assertTrue(parsed instanceof LinkElement);
                LinkElement linkElement = (LinkElement) parsed;
                assertEquals(expected.getProperty("namepath"), linkElement.getLinkedPath().toString());
                break;
            case NAMED_PARAMETER:
                assertTrue(parsed instanceof NamedParameterElement);
                NamedParameterElement namedParameterElement = (NamedParameterElement) parsed;
                assertEquals(expected.getProperty("name"), namedParameterElement.getParamName().toString());
                assertEquals(expected.getProperty("desc"), namedParameterElement.getParamDescription().toString());
                assertEquals(expected.getProperty("type"), namedParameterElement.getParamTypes().toString());
                break;
            case SIMPLE:
                assertTrue(parsed instanceof SimpleElement);
                break;
            case UNNAMED_PARAMETER:
                assertTrue(parsed instanceof UnnamedParameterElement);
                UnnamedParameterElement unnamedParameterElement = (UnnamedParameterElement) parsed;
                assertEquals(expected.getProperty("desc"), unnamedParameterElement.getParamDescription().toString());
                assertEquals(expected.getProperty("type"), unnamedParameterElement.getParamTypes().toString());
                break;
            default:
                throw new AssertionError();
        }
    }

    private static void assertElementsEquality(List<JsDocElement> expectedTags, List<JsDocElement> elements) {
        Collections.sort(expectedTags, new JsDocElementComparator());
        Collections.sort(elements, new JsDocElementComparator());

        assertEquals(expectedTags.size(), elements.size());

        for (int i = 0; i < expectedTags.size(); i++) {
            JsDocElement expected = expectedTags.get(i);
            JsDocElement parsed = elements.get(i);
            assertElementEquality((FakeJsDocElement) expected, parsed);
        }
    }

    private static class JsDocElementComparator implements Comparator<JsDocElement> {

        @Override
        public int compare(JsDocElement o1, JsDocElement o2) {
            return o1.getType().toString().compareTo(o2.getType().toString());
        }

    }

    private static class FakeJsDocElement implements JsDocElement {

        private final Type type;
        private Map<String, String> properties = new HashMap<String, String>();

        public FakeJsDocElement(Type type) {
            assertNotNull(type);
            this.type = type;
        }

        @Override
        public Type getType() {
            return type;
        }

        public void addProperty(String key, String value) {
            assertNotNull(key);
            assertNotNull(value);
            properties.put(key, value);
        }

        public String getProperty(String key) {
            return properties.get(key);
        }
    }
}
