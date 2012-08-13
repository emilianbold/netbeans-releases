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
package org.netbeans.modules.javascript2.editor.jsdoc;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.editor.jsdoc.model.DescriptionElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.JsDocElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.JsDocElementType;
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

    private static final JsDocElementType[] expectedTypes = new JsDocElementType[] {
        // context sensitive type
        JsDocElementType.CONTEXT_SENSITIVE,

        // classic types
        JsDocElementType.ARGUMENT, JsDocElementType.AUGMENTS, JsDocElementType.AUTHOR, JsDocElementType.BORROWS,
        JsDocElementType.CLASS, JsDocElementType.CONSTANT, JsDocElementType.CONSTRUCTOR, JsDocElementType.CONSTRUCTS,
        JsDocElementType.DEFAULT, JsDocElementType.DEPRECATED, JsDocElementType.DESCRIPTION, JsDocElementType.EVENT,
        JsDocElementType.EXAMPLE, JsDocElementType.EXTENDS, JsDocElementType.FIELD, JsDocElementType.FILE_OVERVIEW,
        JsDocElementType.FUNCTION, JsDocElementType.IGNORE, JsDocElementType.INNER, JsDocElementType.LENDS,
        JsDocElementType.MEMBER_OF, JsDocElementType.NAME, JsDocElementType.NAMESPACE,
        JsDocElementType.PARAM, JsDocElementType.PRIVATE, JsDocElementType.PROPERTY, JsDocElementType.PUBLIC,
        JsDocElementType.REQUIRES, JsDocElementType.RETURN, JsDocElementType.RETURNS, JsDocElementType.SEE,
        JsDocElementType.SINCE, JsDocElementType.STATIC, JsDocElementType.THROWS, JsDocElementType.TYPE,
        JsDocElementType.VERSION
    };

    public void testParsedTypesForAsterisksComment() throws Exception {
        checkElementTypes("testfiles/jsdoc/allTypesAsterisks.js");
    }

    public void testParsedTypesForNoAsteriskComment() throws Exception {
        checkElementTypes("testfiles/jsdoc/allTypesNoAsterisk.js");
    }

    public void testParsedContextSensitiveContentNoAsterisk() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/allTypesNoAsterisk.js"));
        List<? extends JsDocElement> tags = getFirstJsDocBlock(source.createSnapshot()).getTags();
        assertEquals(JsDocElementType.CONTEXT_SENSITIVE, tags.get(0).getType());
        assertEquals("This could be description", ((DescriptionElement) tags.get(0)).getDescription());
    }

    public void testParsedContextSensitiveContentAsterisks() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/allTypesAsterisks.js"));
        List<? extends JsDocElement> tags = getFirstJsDocBlock(source.createSnapshot()).getTags();
        assertEquals(JsDocElementType.CONTEXT_SENSITIVE, tags.get(0).getType());
        assertEquals("This could be description", ((DescriptionElement) tags.get(0)).getDescription());
    }

    public void testParsingLongComments() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/jsdoc/windowStub.js"));
        List<? extends JsDocElement> tags = getFirstJsDocBlock(source.createSnapshot()).getTags();
        assertEquals(JsDocElementType.CONTEXT_SENSITIVE, tags.get(0).getType());
    }

    private void checkElementTypes(String filePath) {
        Source source = getTestSource(getTestFile(filePath));
        List<? extends JsDocElement> tags = getFirstJsDocBlock(source.createSnapshot()).getTags();
        for (int i = 0; i < expectedTypes.length; i++) {
            assertEquals(expectedTypes[i], tags.get(i).getType());
        }
    }

    private JsDocComment getFirstJsDocBlock(Snapshot snapshot) {
        Iterator<Entry<Integer, JsDocComment>> iterator = JsDocParser.parse(snapshot).entrySet().iterator();
        return iterator.next().getValue();
    }
}
