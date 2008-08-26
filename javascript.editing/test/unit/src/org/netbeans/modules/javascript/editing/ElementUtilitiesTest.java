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

package org.netbeans.modules.javascript.editing;

import java.util.Arrays;
import java.util.List;
import org.mozilla.nb.javascript.FunctionNode;
import org.mozilla.nb.javascript.Node;
import org.netbeans.modules.gsf.api.CompilationInfo;

/**
 *
 * @author Martin Adamek
 */
public class ElementUtilitiesTest extends JsTestBase {

    public ElementUtilitiesTest(String testName) {
        super(testName);
    }
    
    /**
     * @todo test IndexedFunction
     */
    public void testGetComments() throws Exception {
        CompilationInfo info = getInfo("testfiles/types2.js");
        String caretLine = "    insertBeeefore: func^tion(el, values, returnElement){";
        FunctionAstElement element = getFunctionAstElement(info, caretLine);
        List<String> comments = ElementUtilities.getComments(info, element);
        
        List<String> expected = Arrays.asList(
                "Applies bla bla bla bla",
                "foo foo bar bar",
                "@param {Mixed} el The bla bla bla",
                "@param {Object/Array} values The bla bla bloo (i.e. {0}) or an",
                "object (i.e. {foo: 'bar'})",
                "@param {Boolean} returnElement (optional) true to return a Axt.Element (defaults to blii)",
                "@return {HTMLElement/Axt.Element} The new node or Alement",
                "@private",
                "@constructor",
                "@deprecated"
                );

        assertEquals(expected, comments);
    }

    public static FunctionAstElement getFunctionAstElement(CompilationInfo info, String caretLine) throws Exception {
        Node root = AstUtilities.getRoot(info);
        String text = AstUtilities.getParseResult(info).getSource();
        
        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = text.indexOf(caretLine);
            assertTrue(lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
        }
        
        FunctionNode functionNode = AstUtilities.findMethodAtOffset(root, caretOffset);
        return (FunctionAstElement) AstElement.getElement(info, functionNode);
    }
    
}
