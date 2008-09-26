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
import java.util.Set;
import org.mozilla.nb.javascript.FunctionNode;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.PositionManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author tor
 */
public class JsPositionManagerTest extends JsTestBase {

    public JsPositionManagerTest(String name) {
        super(name);
    }

    private void addAll(Node node, List<Node> list) {
        list.add(node);
        if (node.hasChildren()) {
            for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                addAll(child, list);
            }
        }
    }

    public void testGetPosition1() throws Exception {
        GsfTestCompilationInfo info = getInfo("testfiles/prototype-new.js");
        PositionManager pm = getPreferredLanguage().getParser().getPositionManager();
        Node root = AstUtilities.getRoot(info);
        assertNotNull(root);

        List<Node> nodes = new ArrayList<Node>();
        addAll(root, nodes);
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

                    // Totally mismatched info (so resolving handles won't work)
                    // ...obtain a reasonable position anyway (from old info)
                    GsfTestCompilationInfo newInfo = getInfo("testfiles/rename.js");
                    OffsetRange newRange = pm.getOffsetRange(newInfo, element);
                    assertEquals(range, newRange);
                }
                break;
            }
        }
        assertTrue(found);
    }

    public void testGetPosition2() throws Exception {
        JsPositionManager jpm = new JsPositionManager();
        ElementHandle handle = new ElementHandle() {

            public FileObject getFileObject() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public String getMimeType() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public String getName() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public String getIn() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public ElementKind getKind() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Set<Modifier> getModifiers() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean signatureEquals(ElementHandle handle) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        };
        OffsetRange r = jpm.getOffsetRange(null, handle);
        assertNotNull(r);
    }

    public void testGetPosition3() throws Exception {
        JsPositionManager jpm = new JsPositionManager();
        OffsetRange r = jpm.getOffsetRange(null, null);
        assertNotNull(r);
    }

    public void testGetPosition4() throws Exception {
        JsPositionManager jpm = new JsPositionManager();
        ElementHandle handle = new JsElement() {

            @Override
            public String getName() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public ElementKind getKind() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public String getFqn() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        OffsetRange r = jpm.getOffsetRange(null, handle);
        assertNotNull(r);
    }
}