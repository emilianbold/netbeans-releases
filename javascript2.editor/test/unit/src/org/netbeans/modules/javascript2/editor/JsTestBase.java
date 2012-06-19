/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.javascript2.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.BeforeClass;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.MockServices;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.javascript2.editor.lexer.CommonTokenId;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.test.MockLookup;

/**
 * @author Tor Norbye
 */
public abstract class JsTestBase extends CslTestBase {
    
    public static String JS_SOURCE_ID = "classpath/js-source"; // NOI18N
    
    public JsTestBase(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new TestJsLanguage();
    }
    
    @Override
    protected String getPreferredMimeType() {
        return CommonTokenId.JAVASCRIPT_MIME_TYPE;
    }
    
    public static class TestJsLanguage extends JsLanguage {

        public TestJsLanguage() {
            super();
        }

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.singleton(JS_SOURCE_ID);
        }
        
        
        
    }
//    @Override
//    protected Parser getParser() {
//        JsParser.runtimeException = null;
//        return super.getParser();
//    }
//
//    @Override
//    protected void validateParserResult(ParserResult result) {
//        if (JsParser.runtimeException != null) {
//            JsParser.runtimeException.printStackTrace();
//        }
//        JsTestBase.assertNull(JsParser.runtimeException != null ? JsParser.runtimeException.toString() : "", JsParser.runtimeException);
//    }
//
    @Override
    protected void setUp() throws Exception {        
        TestLanguageProvider.register(getPreferredLanguage().getLexerLanguage());
        super.setUp();//        JsIndexer.setClusterUrl("file:/bogus"); // No translation
//

    }

    

    
    
    
    
  
    
//    @Override
//    public Formatter getFormatter(IndentPrefs preferences) {
//        if (preferences == null) {
//            preferences = new IndentPrefs(4,4);
//        }
//
//        Preferences prefs = MimeLookup.getLookup(MimePath.get(CommonTokenId.JAVASCRIPT_MIME_TYPE)).lookup(Preferences.class);
//        prefs.putInt(SimpleValueNames.SPACES_PER_TAB, preferences.getIndentation());
//
//        JsFormatter formatter = new JsFormatter();
//
//        return formatter;
//    }

// XXX: parsingapi
//    // Called via reflection from GsfUtilities. This is necessary because
//    // during tests, going from a FileObject to a BaseDocument only works
//    // if all the correct data loaders are installed and working - and that
//    // hasn't been the case; we end up with PlainDocuments instead of BaseDocuments.
//    // If anyone can figure this out, please let me know and simplify the
//    // test infrastructure.
//    public static BaseDocument getDocumentFor(FileObject fo) {
//        BaseDocument doc = GsfTestBase.createDocument(read(fo));
//        doc.putProperty(org.netbeans.api.lexer.Language.class, CommonTokenId.language());
//        doc.putProperty("mimeType", CommonTokenId.JAVASCRIPT_MIME_TYPE);
//
//        return doc;
//    }
    

//    @Override
//    protected void assertEquals(String message, BaseDocument doc, ParserResult expected, ParserResult actual) throws Exception {
//        Node expectedRoot = ((JsParseResult)expected).getRootNode();
//        Node actualRoot = ((JsParseResult)actual).getRootNode();
//        assertEquals(doc, expectedRoot, actualRoot);
//    }
//
//    private boolean assertEquals(BaseDocument doc, Node expected, Node actual) throws Exception {
//        assertEquals(expected.hasChildren(), actual.hasChildren());
//        if (expected.getType() != actual.getType() ||
//                expected.hasChildren() != actual.hasChildren() /* ||
//                expected.getSourceStart() != actual.getSourceStart() ||
//                expected.getSourceEnd() != actual.getSourceEnd()*/
//                ) {
//            String s = null;
//            Node curr = expected;
//            while (curr != null) {
//                String desc = curr.toString();
//                int start = curr.getSourceStart();
//                int line = Utilities.getLineOffset(doc, start);
//                desc = desc + " (line " + line + ")";
//                if (curr.getType() == Token.FUNCTION) {
//                    String name = null;
//                    Node label = ((FunctionNode)curr).labelNode;
//                    if (label != null) {
//                        name = label.getString();
//                    } else {
//                        for (Node child = curr.getFirstChild(); child != null; child = child.getNext()) {
//                            if (child.getType() == Token.FUNCNAME) {
//                                desc = child.getString();
//                                break;
//                            }
//                        }
//                    }
//                    if (name != null) {
//                        desc = desc + " : " + name + "()";
//                    }
//                } else if (curr.getType() == Token.OBJECTLIT) {
//                    String[] names = AstUtilities.getObjectLitFqn(curr);
//                    if (names != null) {
//                        desc = desc + " : " + names[0];
//                    }
//                }
//                if (s == null) {
//                    s = desc;
//                } else {
//                    s = desc + " - " + s;
//                }
//                curr = curr.getParentNode();
//            }
//            fail("node mismatch: Expected=" + expected + ", Actual=" + actual + "; path=" + s);
//        }
//
//        if (expected.hasChildren()) {
//            for (Node expectedChild = expected.getFirstChild(),
//                    actualChild = actual.getFirstChild();
//                    expectedChild != null; expectedChild = expectedChild.getNext(), actualChild = actualChild.getNext()) {
//                assertEquals(expectedChild.getNext() != null, actualChild.getNext() != null);
//                assertEquals(doc, expectedChild, actualChild);
//            }
//        }
//
//        return true;
//    }
// XXX: parsingapi
//    @Override
//    protected void verifyIncremental(ParserResult result, EditHistory history, ParserResult oldResult) {
//        JsParseResult pr = (JsParseResult)result;
//        assertNotNull(pr.getIncrementalParse());
//        assertNotNull(pr.getIncrementalParse().newFunction);
//    }
    
    
}
