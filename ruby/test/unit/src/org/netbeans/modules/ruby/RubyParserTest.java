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

import org.jruby.nb.ast.Node;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;

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

    private void checkParseTree(String file, String caretLine, String nodeName) throws Exception {
        CompilationInfo info = getInfo(file);
        
        String text = info.getText();

        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = text.indexOf(caretLine);
            assertTrue(lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
            ((GsfTestCompilationInfo)info).setCaretOffset(caretOffset);
        }

        Node root = AstUtilities.getRoot(info);
        assertNotNull("Parsing broken input failed for " + file, root);
        
        // Ensure that we find the node we're looking for
        if (nodeName != null) {
            RubyParseResult rpr = AstUtilities.getParseResult(info);
            OffsetRange range = rpr.getSanitizedRange();
            if (range.containsInclusive(caretOffset)) {
                caretOffset = range.getStart();
            }
            AstPath path = new AstPath(root, caretOffset);
            Node closest = path.leaf();
            assertNotNull(closest);
            String leafName = closest.getClass().getName();
            leafName = leafName.substring(leafName.lastIndexOf('.')+1);
            assertEquals(nodeName, leafName);
        }
    }
        
    public void testPartial1() throws Exception {
        checkParseTree("testfiles/broken1.rb", "x.^", "VCallNode");
    }
    
    public void testPartial1b() throws Exception {
        // Recover even when the caret is elsewhere
        checkParseTree("testfiles/broken1.rb", null, null);
    }

    public void testPartial2() throws Exception {
        checkParseTree("testfiles/broken2.rb", "Foo.new.^", "CallNoArgNode");
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
}
