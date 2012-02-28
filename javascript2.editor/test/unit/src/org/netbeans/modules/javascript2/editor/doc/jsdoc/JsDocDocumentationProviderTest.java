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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.javascript2.editor.model.DocParameter;
import org.netbeans.modules.javascript2.editor.model.Type;
import org.netbeans.modules.javascript2.editor.model.impl.TypeImpl;
import org.netbeans.modules.javascript2.editor.model.impl.TypesImpl;
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
public class JsDocDocumentationProviderTest extends JsDocTestBase {

    public JsDocDocumentationProviderTest(String testName) {
        super(testName);
    }

    private static void checkReturnType(Source source, final int offset, final String expected) throws Exception {
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                assertTrue(result instanceof JsParserResult);
                JsParserResult parserResult = (JsParserResult) result;

                JsDocDocumentationProvider documentationProvider = getDocumentationProvider(parserResult);
                if (expected == null) {
                    assertNull(documentationProvider.getReturnType(getNodeForOffset(parserResult, offset)));
                } else {
                    assertEquals(expected,
                            documentationProvider.getReturnType(getNodeForOffset(parserResult, offset)).get(0).toString());
                }
            }
        });
    }

    private void checkParameter(Source source, final int offset, final FakeDocParameter expectedParam) throws Exception {
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                assertTrue(result instanceof JsParserResult);
                JsParserResult parserResult = (JsParserResult) result;

                JsDocDocumentationProvider documentationProvider = getDocumentationProvider(parserResult);
                if (expectedParam == null) {
                    assertNull(documentationProvider.getParameters(getNodeForOffset(parserResult, offset)));
                } else {
                    List<DocParameter> parameters = documentationProvider.getParameters(getNodeForOffset(parserResult, offset));
                    assertEquals(expectedParam.getDefaultValue(), parameters.get(0).getDefaultValue());
                    assertEquals(expectedParam.getParamDescription(), parameters.get(0).getParamDescription());
                    assertEquals(expectedParam.getParamName(), parameters.get(0).getParamName());
                    assertEquals(expectedParam.getParamTypes().toString(), parameters.get(0).getParamTypes().toString());
                    assertEquals(expectedParam.isOptional(), parameters.get(0).isOptional());
                }
            }
        });
    }

    public void testGetReturnTypeForReturn() throws Exception {
        Source testSource = getTestSource(getTestFile("testfiles/jsdoc/classWithJsDoc.js"));

        final int caretOffset = getCaretOffset(testSource, "Shape.prototype.clone = function(){^");
        checkReturnType(testSource, caretOffset, "Shape");
    }

    public void testGetReturnTypeForReturns() throws Exception {
        Source testSource = getTestSource(getTestFile("testfiles/jsdoc/classWithJsDoc.js"));

        final int caretOffset = getCaretOffset(testSource, "Shape.prototype.clone2 = function(){^");
        checkReturnType(testSource, caretOffset, "Shape");
    }

    public void testGetReturnTypeForType() throws Exception {
        Source testSource = getTestSource(getTestFile("testfiles/jsdoc/classWithJsDoc.js"));

        final int caretOffset = getCaretOffset(testSource, "Rectangle.prototype.getClassName= function(){^");
        checkReturnType(testSource, caretOffset, "String");
    }

    public void testGetNullReturnTypeAtNoReturnTypeComment() throws Exception {
        Source testSource = getTestSource(getTestFile("testfiles/jsdoc/classWithJsDoc.js"));

        final int caretOffset = getCaretOffset(testSource, "Shape.prototype.clone3 = function(){^");
        checkReturnType(testSource, caretOffset, null);
    }

    public void testGetNullReturnTypeByMissingComment() throws Exception {
        Source testSource = getTestSource(getTestFile("testfiles/jsdoc/returnTypes.js"));

        final int caretOffset = getCaretOffset(testSource, "Shape.prototype.clone4 = function(){^");
        checkReturnType(testSource, caretOffset, null);
    }

    public void testGetReturnTypeAtFunction() throws Exception {
        Source testSource = getTestSource(getTestFile("testfiles/jsdoc/returnTypes.js"));

        final int caretOffset = getCaretOffset(testSource, "function martion () {^");
        checkReturnType(testSource, caretOffset, "Number");
    }

    public void testGetReturnTypeAtObjectFunction() throws Exception {
        Source testSource = getTestSource(getTestFile("testfiles/jsdoc/returnTypes.js"));

        final int caretOffset = getCaretOffset(testSource, "getVersion: function() {^");
        checkReturnType(testSource, caretOffset, "Number");
    }

    public void testGetParametersForOnlyNameParam() throws Exception {
        Source testSource = getTestSource(getTestFile("testfiles/jsdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(testSource, "function line5(accessLevel){^}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter("accessLevel", null, "", false,
                Arrays.asList(new TypeImpl("")));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForNameAndTypeParam() throws Exception {
        Source testSource = getTestSource(getTestFile("testfiles/jsdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(testSource, "function line1(userName){^}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter("userName", null, "", false, 
                Arrays.asList(new TypeImpl("String")));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForNameAndMoreTypesParam() throws Exception {
        Source testSource = getTestSource(getTestFile("testfiles/jsdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(testSource, "function line2(product){^}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter("product", null, "", false,
                Arrays.asList(new TypeImpl("String"), new TypeImpl("Number")));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForFullDocParam() throws Exception {
        Source testSource = getTestSource(getTestFile("testfiles/jsdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(testSource, "function line6(userName){^}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter("userName", null, "name of the user", false,
                Arrays.asList(new TypeImpl("String")));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForFullDocOptionalParam() throws Exception {
        Source testSource = getTestSource(getTestFile("testfiles/jsdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(testSource, "function line3(accessLevel){^}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter("accessLevel", null, "accessLevel is optional", true,
                Arrays.asList(new TypeImpl("String")));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForDefaultValueParam() throws Exception {
        Source testSource = getTestSource(getTestFile("testfiles/jsdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(testSource, "function line4(accessLevel){^}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter("accessLevel", "\"author\"", "accessLevel is optional", true,
                Arrays.asList(new TypeImpl("String")));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForNameAndTypeArgument() throws Exception {
        Source testSource = getTestSource(getTestFile("testfiles/jsdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(testSource, "function line7(userName){^}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter("userName", null, "", false,
                Arrays.asList(new TypeImpl("String")));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForDefaultValueArgument() throws Exception {
        Source testSource = getTestSource(getTestFile("testfiles/jsdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(testSource, "function line8(userName){^}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter("userName", "\"Jackie\"", "userName is optional", true,
                Arrays.asList(new TypeImpl("String")));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForDefaultValueWithSpacesArgument() throws Exception {
        Source testSource = getTestSource(getTestFile("testfiles/jsdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(testSource, "function line9(userName){^}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter("userName", "\"for example Jackie Chan\"", "userName is optional", true,
                Arrays.asList(new TypeImpl("String")));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    private static class FakeDocParameter implements DocParameter {

        String paramName, defaultValue, paramDesc;
        boolean optional;
        List<? extends Type> paramTypes;

        public FakeDocParameter(String paramName, String defaultValue, String paramDesc, boolean optional, List<? extends Type> paramTypes) {
            this.paramName = paramName;
            this.defaultValue = defaultValue;
            this.paramDesc = paramDesc;
            this.optional = optional;
            this.paramTypes = paramTypes;
        }
        @Override
        public String getParamName() {
            return paramName;
        }

        @Override
        public String getDefaultValue() {
            return defaultValue;
        }

        @Override
        public boolean isOptional() {
            return optional;
        }

        @Override
        public String getParamDescription() {
            return paramDesc;
        }

        @Override
        public List<? extends Type> getParamTypes() {
            return paramTypes;
        }

    }
}
