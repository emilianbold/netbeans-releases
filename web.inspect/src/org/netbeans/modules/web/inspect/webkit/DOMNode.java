/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.webkit;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * DOM node.
 *
 * @author Jan Stola
 */
public class DOMNode extends AbstractNode {
    /** Icon base of the node. */
    static final String ICON_BASE = "org/netbeans/modules/web/inspect/resources/domElement.png"; // NOI18N
    /** WebKit node represented by this node. */
    private Node node;
    /** Property sets of the node. */
    private PropertySet[] propertySets;

    /**
     * Creates a new {@code DOMNode}.
     * 
     * @param model page model the node belongs to.
     * @param node WebKit node represented by the node.
     */
    public DOMNode(WebKitPageModel model, Node node) {
        super(new DOMChildren(model), Lookups.fixed(node));
        this.node = node;
        setIconBaseWithExtension(ICON_BASE);
        updateDisplayName();
    }

    @Override
    public String getHtmlDisplayName() {
        ResourceBundle bundle = NbBundle.getBundle(DOMNode.class);
        String displayName;
        int nodeType = node.getNodeType();
        if (nodeType == 1) {
            // Element
            String pattern = bundle.getString("DOMNode.elementDisplayName"); //NOI18N
            String tagName = node.getNodeName().toLowerCase();
            String selector = getSelector();
            displayName = MessageFormat.format(pattern, tagName, selector);
        } else if (nodeType == 9) {
            displayName = bundle.getString("DOMNode.documentDisplayName"); //NOI18N
        } else {
            // Not used by now
            displayName = node.getNodeType() + " " + node.getNodeName() + " " + node.getNodeValue(); // NOI18N
        }
        return displayName;
    }

    /**
     * Returns ID and class-based selector that corresponds to this node.
     * 
     * @return ID and class-based selector that corresponds to this node.
     */
    private String getSelector() {
        StringBuilder selector = new StringBuilder();
        Node.Attribute idAttr = node.getAttribute("id"); // NOI18N
        if (idAttr != null) {
            selector.append('#').append(idAttr.getValue());
        }
        Node.Attribute classAttr = node.getAttribute("class"); // NOI18
        if (classAttr != null) {
            StringTokenizer st = new StringTokenizer(classAttr.getValue());
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                selector.append('.').append(token.trim());
            }
        }
        return selector.toString();
    }

    @Override
    public synchronized PropertySet[] getPropertySets() {
        if (propertySets == null) {
            propertySets = createPropertySets();
        }
        return propertySets;
    }

    /**
     * Creates property sets for this node.
     * 
     * @return property sets of this node.
     */
    private PropertySet[] createPropertySets() {
        return new PropertySet[] { new AttributesPropertySet(this) };
    }

    /**
     * Forces update of attributes (i.e. attribute property set) from the model.
     */
    synchronized void updateAttributes() {
        if (propertySets != null) {
            for (PropertySet set : propertySets) {
                if (set instanceof AttributesPropertySet) {
                    ((AttributesPropertySet)set).update();
                }
            }
            firePropertySetsChange(null, null);
        }
        updateDisplayName();
    }

    /**
     * Forces update of character data.
     */
    void updateCharacterData() {
        updateDisplayName();
    }

    /**
     * Forces update of the display name.
     */
    private void updateDisplayName() {
        String tagName = node.getNodeName().toLowerCase();
        String selector = getSelector();
        setDisplayName(tagName+selector);
    }

    /**
     * Returns the WebKit node that this node represents.
     * 
     * @return WebKit node represented by this node.
     */
    Node getNode() {
        return node;
    }

    /**
     * Forces update of the children/sub-nodes.
     */
    void updateChildren() {
        DOMChildren children = (DOMChildren)getChildren();
        children.updateKeys(node);
    }

    /**
     * Children for {@code DOMNode}.
     */
    static class DOMChildren extends Children.Keys<Integer> {
        /** Page model this node belongs to. */
        private WebKitPageModel pageModel;

        /**
         * Creates a new {@code DOMChildren}.
         * 
         * @param pageModel page model the node belongs to.
         */
        DOMChildren(WebKitPageModel pageModel) {
            this.pageModel = pageModel;
        }

        /**
         * Forces update of the keys/sub-nodes from the model.
         * 
         * @param node parent node of this children object.
         */
        void updateKeys(Node node) {
            List<Node> subNodes = node.getChildren();
            List<Integer> keys = new ArrayList<Integer>();
            if (subNodes != null) {
                for (Node subNode : subNodes) {
                    boolean isElement = (subNode.getNodeType() == 1);
                    if (isElement && !subNode.isInjectedByNetBeans()) {
                        keys.add(subNode.getNodeId());
                    }
                }
            }
            Node contentDocument = node.getContentDocument();
            if (contentDocument != null) {
                keys.add(contentDocument.getNodeId());
            }
            setKeys(keys);
            getNodes(true);
        }

        @Override
        protected org.openide.nodes.Node[] createNodes(Integer nodeId) {
            DOMNode node = pageModel.getNode(nodeId);
            if (node == null) {
                return null;
            } else {
                return new org.openide.nodes.Node[] { node };
            }
        }
        
    }

}
