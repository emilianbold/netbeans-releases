/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.ws.qaf.rest;

import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author lukas
 */
public class RestNodeTest extends RestTestBase {

    private static final String[] services = {"ItemResource", "ItemsResource [/items]", "SampleResource [sample]"}; //NOI18N

    private static final String addMethod =
            "    @javax.ws.rs.POST\n" +
            "    @ConsumeMime(\"application/xml\")\n" +
            "    public void postXml() {\n" +
            "    }\n";

    public RestNodeTest(String name) {
        super(name);
    }

    @Override
    protected String getProjectName() {
        return "Sample"; //NOI18N
    }

    public void testNodesAfterOpen() {
        assertEquals("Some node not shown", 3, getRestNode().getChildren().length);
        assertEquals("Some method not shown for " + services[0], 3, //NOI18N
                getMethodsNode(services[0]).getChildren().length); //NOI18N
        assertEquals("Some method not shown for " + services[1], 1, //NOI18N
                getMethodsNode(services[1]).getChildren().length); //NOI18N
        assertEquals("Some method not shown for " + services[2], 2, //NOI18N
                getMethodsNode(services[2]).getChildren().length); //NOI18N
        assertEquals("Offending locator for " + services[0], 0, //NOI18N
                getSubresourceNode(services[0]).getChildren().length); //NOI18N
        assertEquals("Missing locator for " + services[1], 1, //NOI18N
                getSubresourceNode(services[1]).getChildren().length); //NOI18N
        assertEquals("Offending locator for " + services[2], 0, //NOI18N
                getSubresourceNode(services[2]).getChildren().length); //NOI18N
    }

    public void testOpenOnResource() {
        Node n = getResourceNode(services[2]);
        String open = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
        n.performPopupActionNoBlock(open);
        EditorOperator eo = new EditorOperator(services[2].substring(0, 14));
        assertNotNull(services[2] + " not opened?", eo);
    }

    public void testOpenOnMethod() {
        Node n = new Node(getMethodsNode(services[0]), "getXML"); //NOI18N
        String open = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
        n.performPopupActionNoBlock(open);
        EditorOperator eo = new EditorOperator(services[0]);
        assertNotNull(services[0] + " not opened?", eo);
        assertEquals("wrong line", 40, eo.getLineNumber()); //NOI18N
    }

    public void testOpenOnLocator() {
        Node n = new Node(getSubresourceNode(services[1]), "{name}"); //NOI18N
        String open = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
        n.performPopupActionNoBlock(open);
        EditorOperator eo = new EditorOperator(services[1].substring(0, 13));
        assertNotNull(services[0] + " not opened?", eo);
        assertEquals("wrong line", 47, eo.getLineNumber()); //NOI18N
    }

    public void testAddMethod() {
        EditorOperator eo = new EditorOperator(services[0]);
        assertNotNull(services[0] + " not opened?", eo);
        eo.select(59);
        eo.insert(addMethod);
        eo.save();
        // let's wait for a while here...
        new EventTool().waitNoEvent(1000);
        assertEquals("New method not shown for " + services[0], 4, //NOI18N
                getMethodsNode(services[0]).getChildren().length); //NOI18N
    }
    
    public void testRemoveMethod() {
        EditorOperator eo = new EditorOperator(services[0]);
        assertNotNull(services[0] + " not opened?", eo);
        eo.deleteLine(60);
        eo.deleteLine(60);
        eo.deleteLine(60);
        eo.deleteLine(60);
        eo.save();
        // let's wait for a while here...
        new EventTool().waitNoEvent(1000);
        assertEquals("New method still shown for " + services[0], 3, //NOI18N
                getMethodsNode(services[0]).getChildren().length); //NOI18N
    }
    
    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new RestNodeTest("testNodesAfterOpen")); //NOI18N
        suite.addTest(new RestNodeTest("testOpenOnResource")); //NOI18N
        suite.addTest(new RestNodeTest("testOpenOnMethod")); //NOI18N
        suite.addTest(new RestNodeTest("testOpenOnLocator")); //NOI18N
        suite.addTest(new RestNodeTest("testAddMethod")); //NOI18N
        suite.addTest(new RestNodeTest("testRemoveMethod")); //NOI18N
        return suite;
    }

    /* Method allowing test execution directly from the IDE. */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
    }

    private Node getRestNode() {
        String restNodeLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.nodes.Bundle", "LBL_RestServices");
        Node restNode = new Node(getProjectRootNode(), restNodeLabel);
        restNode.expand();
        return restNode;
    }

    private Node getResourceNode(String resourceName) {
        Node n = new Node(getRestNode(), resourceName);
        n.expand();
        return n;
    }

    private Node getMethodsNode(String resourceName) {
        String methodsLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.nodes.Bundle", "LBL_HttpMethods");
        Node n = new Node(getResourceNode(resourceName), methodsLabel);
        n.expand();
        return n;
    }

    private Node getSubresourceNode(String resourceName) {
        String subresourceLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.nodes.Bundle", "LBL_SubResourceLocators");
        Node n = new Node(getResourceNode(resourceName), subresourceLabel);
        n.expand();
        return n;
    }
}
