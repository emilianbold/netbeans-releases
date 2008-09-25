/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import java.util.ArrayList;
import java.util.List;
import org.mozilla.nb.javascript.FunctionNode;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.PositionManager;

/**
 *
 * @author tor
 */
public class JsPositionManagerTest extends JsTestBase {

    public JsPositionManagerTest(String name) {
        super(name);
    }

    public void testGetPosition1() throws Exception {
        GsfTestCompilationInfo info = getInfo("testfiles/prototype-new.js");
        PositionManager pm = getPreferredLanguage().getParser().getPositionManager();
        Node root = AstUtilities.getRoot(info);
        assertNotNull(root);

        List<Node> nodes = new ArrayList<Node>();
        for (Node node : nodes) {
            AstElement element = AstElement.getElement(info, node);
            if (element != null) {
                OffsetRange range = pm.getOffsetRange(info, element);
                assertNotNull(range);
                assertTrue(range != OffsetRange.NONE);
            }
        }

        // Look for one specific known case
        nodes.clear();
        AstUtilities.addNodesByType(root, new int[] { Token.FUNCNAME }, nodes);
        boolean found = false;
        for (Node node : nodes) {
            if (node.getString().equals("toQueryPair")) {
                found = true;
                Node func = node.getParentNode();
                assertNotNull(func);
                assertTrue(func instanceof FunctionNode);
                AstElement element = AstElement.getElement(info, func);
                if (element != null) {
                    OffsetRange range = pm.getOffsetRange(info, element);
                    assertNotNull(range);
                    assertTrue(range != OffsetRange.NONE);
                    String t = info.getText();
                    int expected = t.indexOf("function toQueryPair");
                    assertTrue(expected != -1);
                    assertEquals(expected, range.getStart());
                }
                break;
            }
        }
        assertTrue(found);
    }

    private void addAll(Node node, List<Node> list) {
        list.add(node);
        if (node.hasChildren()) {
            for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                addAll(child, list);
            }
        }
    }
}