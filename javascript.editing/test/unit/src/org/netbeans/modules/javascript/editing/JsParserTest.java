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

package org.netbeans.modules.javascript.editing;

import java.util.Collections;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;

/**
 *
 * @author Tor Norbye
 */
public class JsParserTest extends JsTestBase {
    
    public JsParserTest(String testName) {
        super(testName);
    }

    private void checkParseTree(final String file, final String caretLine, final int nodeType) throws Exception {
        JsParser.runtimeException = null;
        
        FileObject f = getTestFile(file);
        Source source = Source.create(f);

        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(source.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(source, caretOffset);
        } else {
            caretOffset = -1;
        }

        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                JsParseResult jspr = AstUtilities.getParseResult(r);
                assertNotNull("Expecting JsParseResult, but got " + r, jspr);

                Node root = jspr.getRootNode();
                assertNotNull("Parsing broken input failed for " + file + "; " + jspr.getDiagnostics(), root);

                // Ensure that we find the node we're looking for
                if (nodeType != -1) {
                    OffsetRange range = jspr.getSanitizedRange();
                    int adjustedOffset;
                    if (range.containsInclusive(caretOffset)) {
                        adjustedOffset = range.getStart();
                    } else {
                        adjustedOffset = caretOffset;
                    }
                    AstPath path = new AstPath(root, adjustedOffset);
                    Node closest = path.leaf();
                    assertNotNull(closest);
                    String leafName = closest.getClass().getName();
                    leafName = leafName.substring(leafName.lastIndexOf('.')+1);
                    assertEquals(Token.fullName(nodeType) + " != " + Token.fullName(closest.getType()), nodeType, closest.getType());
                }
                assertNull(JsParser.runtimeException);
            }
        });

    }

    private void checkNoParseAbort(String file) throws Exception {
        JsParser.runtimeException = null;

        FileObject f = getTestFile(file);
        Source source = Source.create(f);

        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                JsParseResult jspr = AstUtilities.getParseResult(r);
                assertNotNull("Expecting JsParseResult, but got " + r, jspr);

                assertNull(JsParser.runtimeException);
            }
        });
    }
    
    public void testPartial1() throws Exception {
        checkParseTree("testfiles/broken1.js", "\"str\".^", Token.GETPROP);
    }

    public void testPartial2() throws Exception {
        checkParseTree("testfiles/broken2.js", "x.^", Token.GETPROP);
    }
    
    public void testPartial3() throws Exception {
        checkParseTree("testfiles/broken3.js", "new String().^", Token.GETPROP);
    }
    
    public void testPartial4() throws Exception {
        checkParseTree("testfiles/broken4.js", "call(50,^)", Token.NUMBER);
    }
    
    public void testPartial5() throws Exception {
        checkParseTree("testfiles/broken5.js", "call(50, ^)", Token.CALL);
    }

    public void testPartial6() throws Exception {
        checkParseTree("testfiles/broken6.js", "x = new ^", Token.SCRIPT);
    }

    public void testPartial7() throws Exception {
        checkParseTree("testfiles/broken7.js", "k.^", Token.GETPROP);
    }

    public void testPartial8() throws Exception {
        checkParseTree("testfiles/broken8.js", "partialLiteralName^", Token.OBJECTLIT);
    }

    public void testPartial9() throws Exception {
        checkParseTree("testfiles/broken9.js", "x^", Token.OBJECTLIT);
    }

    public void testPartial10() throws Exception {
        checkParseTree("testfiles/broken10.js", "xy^", Token.OBJECTLIT);
    }

    public void testPartial11() throws Exception {
        checkNoParseAbort("testfiles/broken11.js");
    }

    public void testPartial12() throws Exception {
        checkNoParseAbort("testfiles/broken12.js");
    }

    public void testPartial13() throws Exception {
        // http://www.netbeans.org/issues/show_bug.cgi?id=133173
        checkParseTree("testfiles/broken13.js", "__UNKN^OWN__", Token.BLOCK);
    }

    public void testPartial14() throws Exception {
        // Variation of
        // http://www.netbeans.org/issues/show_bug.cgi?id=133173
        checkParseTree("testfiles/broken14.js", "__UNK^NOWN__", Token.SETNAME);
    }

    public void testPartial15() throws Exception {
        // Variation of
        // http://www.netbeans.org/issues/show_bug.cgi?id=133173
        checkParseTree("testfiles/broken15.js", "__UNK^NOWN__", Token.FUNCTION);
    }

    public void test136495a() throws Exception {
        checkParseTree("testfiles/lbracketlist.js", "__UNK^NOWN__", Token.ARRAYLIT);
    }

    public void test136495b() throws Exception {
        checkParseTree("testfiles/embedding/issue136495.erb.js", "__UNK^NOWN__", Token.ARRAYLIT);
    }

    public void test120499() throws Exception {
        checkParseTree("testfiles/issue120499.js", "__UNK^NOWN__", Token.BLOCK);
    }

    public void test148423() throws Exception {
        checkParseTree("testfiles/issue148423.js", "__UNK^NOWN__", Token.STRING);
    }

    public void test149019() throws Exception {
        checkParseTree("testfiles/issue149019.js", "__UNK^NOWN__", Token.STRING);
    }

    public void testNPE() throws Exception {
        checkParseTree("testfiles/broken16.js", ";pa^rent", Token.BLOCK);
    }

    public void testGeneratedIdentifiers() throws Exception {
        checkParseTree("testfiles/generated_identifiers.js", "__UNK^NOWN__", Token.SETNAME);
    }

// XXX: parsingapi
//    public void testIncremental1() throws Exception {
//        checkIncremental("testfiles/dragdrop.js",
//                1.7d, // Expect it to be at least twice as fast as non-incremental
//                "for (i = 1; i < ^drops.length; ++i)", INSERT+"target",
//                "if (Element.isPa^rent", REMOVE+"re"
//                );
//    }
//
//    public void testIncremental2() throws Exception {
//        checkIncremental("testfiles/rename.js",
//                0.0d, // small file: no expectation for it to be faster
//                "bbb: function(^ppp)", REMOVE+"pp"
//                );
//    }
//
//    public void testIncremental3() throws Exception {
//        checkIncremental("testfiles/semantic3.js",
//                0.0d, // small file: no expectation for it to be faster
//                "document.createElement(\"option\");^", INSERT+"\nfoo = 5;\n"
//                );
//    }
//
//    public void testIncremental4() throws Exception {
//        checkIncremental("testfiles/issue149226.js",
//                0.0d, // small file - no speedup expected
//                "^localObject", INSERT+"var "
//                );
//    }
//
//    public void testIncremental5() throws Exception {
//        checkIncremental("testfiles/incremental.js",
//                0.0d, // small file - no speedup expected
//                "1,^", INSERT+"\n"
//                );
//    }
//
//    public void testIncremental6() throws Exception {
//        checkIncremental("testfiles/incremental.js",
//                0.0d, // small file - no speedup expected
//                "4, ^", INSERT+"\n"
//                );
//    }
//
//    public void testIncremental7() throws Exception {
//        checkIncremental("testfiles/incremental2.js",
//                0.0d, // small file - no speedup expected
//                "alert(s^.a);", REMOVE+".",
//                "alert(s^a);", INSERT+".a"
//                );
//    }

    public void testValidResult() throws Exception {
        // Make sure we get a valid parse result out of an aborted parse
        FileObject fo = getTestFile("testfiles/issue149226.js");
        Source source = Source.create(fo);

        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                JsParseResult jspr = AstUtilities.getParseResult(r);
                assertNotNull("Expecting JsParseResult, but got " + r, jspr);
            }
        });
    }
}
