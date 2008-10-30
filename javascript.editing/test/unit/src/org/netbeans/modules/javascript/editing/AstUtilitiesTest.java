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

import java.util.ArrayList;
import java.util.List;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.CompilationInfo;
import org.netbeans.modules.javascript.editing.lexer.JsCommentTokenId;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;

/**
 *
 * @todo Lots of other methods to test!
 *  
 * @author Tor Norbye
 */
public class AstUtilitiesTest extends JsTestBase {

    public AstUtilitiesTest(String testName) {
        super(testName);
    }

    //private AstElement findBySignature(CompilationInfo info, String signature) {
    //    JsParseResult jpr = AstUtilities.getParseResult(info);
    //    assertNotNull(jpr);
    //    Node root = jpr.getRootNode();
    //    assertNotNull(root);
    //
    //    for (AstElement element : jpr.getStructure().getElements()) {
    //        if (signature.equals(element.getSignature())) {
    //            return element;
    //        }
    //    }
    //
    //    return null;
    //}

    private void addAllNodes(Node node, List<Node> list) {
        list.add(node);
        if (node.hasChildren()) {
            for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                addAllNodes(child, list);
            }
        }
    }
    
    // Make sure we don't bomb out analyzing any of these files
    public void testStress() throws Exception {
        for (String file : JAVASCRIPT_TEST_FILES) {
            CompilationInfo info = getInfo(file);
            BaseDocument doc = LexUtilities.getDocument(info, false);
            List<Node> allNodes = new ArrayList<Node>();
            Node root = AstUtilities.getRoot(info);
            assertNotNull(file + " had unexpected parsing errors", root);
            //if (root == null) {
            //    continue;
            //}
            addAllNodes(root, allNodes);
            List<Node> callNodes = new ArrayList<Node>();
            
            AstUtilities.addNodesByType(root, new int[] { Token.CALL, Token.NEW }, callNodes);
            for (Node callNode : callNodes) {
                String s = AstUtilities.getCallName(callNode, true);
                String t = AstUtilities.getCallName(callNode, false);
            }

            for (Node node : allNodes) {
                String type = AstUtilities.getExpressionType(node);
                TokenSequence<? extends JsCommentTokenId> ts = AstUtilities.getCommentFor(info, doc, node);
                AstUtilities.getFirstChild(node);
                AstUtilities.getSecondChild(node);
                AstUtilities.getRange(node);
                AstUtilities.getRange(info, node);
                AstUtilities.getNameRange(node);
                AstUtilities.isNameNode(node);
            }
        }
    }
    
    
    // TODO add correctness tests too - not just stress tests!
}
