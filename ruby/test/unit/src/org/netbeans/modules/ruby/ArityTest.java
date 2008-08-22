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

package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.List;
import org.jruby.nb.ast.Node;
import org.jruby.nb.ast.NodeType;
import org.jruby.nb.ast.types.INameNode;

/**
 *
 * @author Tor Norbye
 */
public class ArityTest extends RubyTestBase {

    public ArityTest(String testName) {
        super(testName);
    }

    public void testArityMatches() {
        assertTrue(Arity.matches(Arity.createTestArity(0, 0), Arity.createTestArity(0,0)));
        assertTrue(Arity.matches(Arity.createTestArity(1, 1), Arity.createTestArity(1,1)));
        assertTrue(Arity.matches(Arity.createTestArity(3, 3), Arity.createTestArity(3,5)));
        assertTrue(Arity.matches(Arity.createTestArity(5, 5), Arity.createTestArity(3,5)));
        assertTrue(Arity.matches(Arity.createTestArity(4, 4), Arity.createTestArity(3,5)));
        assertTrue(Arity.matches(Arity.createTestArity(4, 4), Arity.createTestArity(3,5)));
        assertTrue(Arity.matches(Arity.createTestArity(4, 4), Arity.createTestArity(3,5)));
        assertTrue(Arity.matches(Arity.createTestArity(4, 4), Arity.createTestArity(0,Integer.MAX_VALUE)));

        assertFalse(Arity.matches(Arity.createTestArity(2, 2), Arity.createTestArity(1,1)));
        assertFalse(Arity.matches(Arity.createTestArity(2, 2), Arity.createTestArity(0,1)));
        assertFalse(Arity.matches(Arity.createTestArity(2, 2), Arity.createTestArity(3,4)));
    }
    
    
    private void checkCallArity(Node root, String methodName, Arity arity) {
        List<Node> nodes = new ArrayList<Node>();
        AstUtilities.addNodesByType(root, new NodeType[] { NodeType.CALLNODE, NodeType.VCALLNODE, NodeType.FCALLNODE }, nodes);
        for (Node n : nodes) {
            assertTrue(AstUtilities.isCall(n));
            String name = ((INameNode)n).getName();
            if (name.equals(methodName)) {
                assertEquals(arity, Arity.getCallArity(n));
                return;
            }
        }
        assertTrue(methodName + " not found", false);
    }
    
    private void checkDefArity(Node root, String methodName, Arity arity) {
        List<Node> nodes = new ArrayList<Node>();
        AstUtilities.addNodesByType(root, new NodeType[] { NodeType.DEFNNODE, NodeType.DEFSNODE }, nodes);
        for (Node n : nodes) {
            String name = ((INameNode)n).getName();
            if (name.equals(methodName)) {
                assertEquals(arity, Arity.getDefArity(n));
                return;
            }
        }
        assertTrue(methodName + " not found", false);
    }
    
    public void testCallArity() {
        Node root = getRootNode("testfiles/arity.rb");
        checkCallArity(root, "foo1!", Arity.createTestArity(0, 0));
        checkCallArity(root, "foo2", Arity.createTestArity(1, 1));
        checkCallArity(root, "foo3", Arity.createTestArity(2, 2));
        checkCallArity(root, "foo4", Arity.createTestArity(1, Integer.MAX_VALUE));
    }

    public void testCallArity2() {
        Node root = getRootNode("testfiles/arity.rb");
        checkCallArity(root, "foo5a", Arity.createTestArity(3, Integer.MAX_VALUE));
        checkCallArity(root, "foo5b", Arity.createTestArity(4, Integer.MAX_VALUE));
        checkCallArity(root, "foo5c", Arity.createTestArity(4, Integer.MAX_VALUE));
    }
    
    public void testDefArity() {
        Node root = getRootNode("testfiles/arity.rb");
        checkDefArity(root, "foo6", Arity.createTestArity(0, 0));
        checkDefArity(root, "foo7", Arity.createTestArity(1, 1));
        checkDefArity(root, "foo8", Arity.createTestArity(2, 2));
        checkDefArity(root, "foo9", Arity.createTestArity(2, 4));
        checkDefArity(root, "foo10", Arity.createTestArity(2, Integer.MAX_VALUE));
    }

    public void testDefArity2() {
        Node root = getRootNode("testfiles/arity.rb");
        checkDefArity(root, "foo6", Arity.createTestArity(0, 0));
        checkDefArity(root, "foo11", Arity.createTestArity(2, 3));
    }
}
