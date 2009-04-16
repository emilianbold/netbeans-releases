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

package org.netbeans.modules.ruby;

import java.util.Collections;
import org.jrubyparser.ast.Node;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tor Norbye
 */
public class RubyParserTest extends RubyTestBase {
    
    public RubyParserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private void checkParseTree(final String file, final String caretLine, final String nodeName) throws Exception {
        Source source = Source.create(getTestFile(file));

        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(source.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(source, caretOffset);
        } else {
            caretOffset = -1;
        }

        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result pr = resultIterator.getParserResult();
                RubyParseResult rpr = AstUtilities.getParseResult(pr);

                Node root = rpr.getRootNode();
                assertNotNull("Parsing broken input failed for " + file + "; " + rpr.getDiagnostics(), root);

                // Ensure that we find the node we're looking for
                if (nodeName != null) {
                    OffsetRange range = rpr.getSanitizedRange();
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
                    leafName = leafName.substring(leafName.lastIndexOf('.') + 1);
                    assertEquals(nodeName, leafName);
                }
            }
        });

    }
       
    public void testPartial1() throws Exception {
        checkParseTree("testfiles/broken1.rb", "x.^", "VCallNode");
    }
    
    public void testPartial1b() throws Exception {
        // Recover even when the caret is elsewhere
        checkParseTree("testfiles/broken1.rb", null, null);
    }

    public void testPartial2() throws Exception {
        checkParseTree("testfiles/broken2.rb", "Foo.new.^", "CallNode");
    }

    public void testPartial3() throws Exception {
        checkParseTree("testfiles/broken3.rb", "x = ^", "ClassNode");
    }

    public void testPartial3b() throws Exception {
        // Recover even when the caret is elsewhere
        checkParseTree("testfiles/broken3.rb", null, null);
    }

    public void testPartial4() throws Exception {
        checkParseTree("testfiles/broken4.rb", "Test::^", "ConstNode");
    }
    
    public void testPartial4b() throws Exception {
        // Recover even when the caret is elsewhere
        checkParseTree("testfiles/broken4.rb", null, null);
    }

    public void testPartial5() throws Exception {
        checkParseTree("testfiles/broken5.rb", "if true^", "TrueNode");
    }

    public void testPartial5MissingEnd() throws Exception {
        // An end is missing and we don't have a current line we can simply
        // clip out; try to compensate
        checkParseTree("testfiles/broken5.rb", null, null);
    }
    
    public void testPartial6() throws Exception {
        checkParseTree("testfiles/broken6.rb", "def ^", "ClassNode");
    }

    public void testPartial12() throws Exception {
        checkParseTree("testfiles/broken12.rb", " File.exists?(^)", "ArrayNode");
    }

    public void testErrors1() throws Exception {
        checkErrors("testfiles/colors.rb");
    }

    public void testErrors2() throws Exception {
        checkErrors("testfiles/broken1.rb");
    }

    public void testErrors3() throws Exception {
        checkErrors("testfiles/broken2.rb");
    }

    public void testErrors4() throws Exception {
        checkErrors("testfiles/broken3.rb");
    }

    public void testErrors5() throws Exception {
        checkErrors("testfiles/broken4.rb");
    }

    public void testErrors6() throws Exception {
        checkErrors("testfiles/broken5.rb");
    }

    public void testErrors7() throws Exception {
        checkErrors("testfiles/broken6.rb");
    }

    public void testValidResult() throws Exception {
        // Make sure we get a valid parse result out of an aborted parse
        FileObject fo = getTestFile("testfiles/broken6.rb");
        Source source = Source.create(fo);

        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public
            @Override
            void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                RubyParseResult jspr = AstUtilities.getParseResult(r);
                assertNotNull("Expecting JsParseResult, but got " + r, jspr);
            }
        });
    }
}
