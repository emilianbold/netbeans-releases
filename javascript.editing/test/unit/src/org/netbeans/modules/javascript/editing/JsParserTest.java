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

package org.netbeans.modules.javascript.editing;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;

/**
 *
 * @author Tor Norbye
 */
public class JsParserTest extends JsTestBase {
    
    public JsParserTest(String testName) {
        super(testName);
    }

    private void checkParseTree(String file, String caretLine, int nodeType) throws Exception {
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
            ((TestCompilationInfo)info).setCaretOffset(caretOffset);
        }

        Node root = AstUtilities.getRoot(info);
        assertNotNull("Parsing broken input failed for " + file, root);
        
        // Ensure that we find the node we're looking for
        if (nodeType != -1) {
            JsParseResult rpr = AstUtilities.getParseResult(info);
            OffsetRange range = rpr.getSanitizedRange();
            if (range.containsInclusive(caretOffset)) {
                caretOffset = range.getStart();
            }
            AstPath path = new AstPath(root, caretOffset);
            Node closest = path.leaf();
            assertNotNull(closest);
            String leafName = closest.getClass().getName();
            leafName = leafName.substring(leafName.lastIndexOf('.')+1);
            assertEquals(Token.fullName(nodeType) + " != " + Token.fullName(closest.getType()), nodeType, closest.getType());
        }
    }
        
    public void testPartial1() throws Exception {
        checkParseTree("testfiles/broken1.rb", "\"str\".^", Token.STRING);
    }

    public void testPartial2() throws Exception {
        checkParseTree("testfiles/broken2.rb", "x.^", Token.NAME);
    }
    
    public void testPartial3() throws Exception {
        checkParseTree("testfiles/broken3.rb", "new String().^", Token.NEW);
    }
    
    public void testPartial4() throws Exception {
        checkParseTree("testfiles/broken4.rb", "call(50,^)", Token.NUMBER);
    }
    
    public void testPartial5() throws Exception {
        checkParseTree("testfiles/broken5.rb", "call(50, ^)", Token.CALL);
    }

    public void testPartial6() throws Exception {
        checkParseTree("testfiles/broken6.rb", "x = new ^", Token.SCRIPT);
    }

    public void testPartial7() throws Exception {
        checkParseTree("testfiles/broken7.rb", "k.^", Token.NAME);
    }
}
