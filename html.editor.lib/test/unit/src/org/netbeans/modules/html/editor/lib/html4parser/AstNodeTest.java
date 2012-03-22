/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.html.editor.lib.html4parser;

import java.util.Collection;
import java.util.Collections;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.HtmlVersionTest;
import org.netbeans.modules.html.editor.lib.api.tree.ElementType;
import org.netbeans.modules.html.editor.lib.api.tree.Node;
import org.netbeans.modules.html.editor.lib.test.TestBase;

/**
 *
 * @author mfukala@netbeans.org
 */
public class AstNodeTest extends TestBase {

    public AstNodeTest(String testName) {
        super(testName);
    }


    @Override
    protected void setUp() throws Exception {
        HtmlVersionTest.setDefaultHtmlVersion(HtmlVersion.HTML41_TRANSATIONAL);
        super.setUp();
    }
    
    public static Test xsuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new AstNodeTest(""));
        return suite;
    }

    public void testNamespaces() throws Exception {
        AstNode node = new AstNode("div", ElementType.OPEN_TAG, 0, 1, false);

        assertEquals("div", node.name());
        assertEquals("div", node.getNameWithoutPrefix());
        assertNull(node.getNamespacePrefix());

        node = new AstNode("ui:composition", ElementType.OPEN_TAG, 0, 1, false);

        assertEquals("ui:composition", node.name());
        assertEquals("composition", node.getNameWithoutPrefix());
        assertEquals("ui", node.getNamespacePrefix());

    }

    public void testAttribute() {
        AstNode.AstAttribute attr = new AstNode.AstAttribute("name", "value", 0, 6);
        assertEquals("name", attr.name());
        assertEquals("name", attr.nameWithoutNamespacePrefix());
        assertNull(attr.namespacePrefix());

        attr = new AstNode.AstAttribute("xmlns:h", "value", 0, 6);
        assertEquals("xmlns:h", attr.name());
        assertEquals("h", attr.nameWithoutNamespacePrefix());
        assertEquals("xmlns", attr.namespacePrefix());


    }

    public void testRemoveChildren_Issue_191276() {
        AstNode node = new AstNode("div", ElementType.OPEN_TAG, 0, 3, false);
        AstNode node2 = new AstNode("div", ElementType.OPEN_TAG, 3, 6, true);
        AstNode node3 = new AstNode("div", ElementType.OPEN_TAG, 6, 9, true);

        node.addChild(node2);
        node.addChild(node3);

        Collection<Node> children = node.children();
        AstNode child = (AstNode)children.iterator().next();
        
        //we can directly remove the children w/o java.util.ConcurrentModificationException
        node.removeChildren(Collections.<AstNode>singletonList(child));
    }

}
