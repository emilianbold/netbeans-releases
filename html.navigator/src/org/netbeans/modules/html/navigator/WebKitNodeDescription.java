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
package org.netbeans.modules.html.navigator;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node.Attribute;
import org.openide.nodes.Children;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeOp;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;

/**
 * Implementation of {@link Description} for WebKit's DOM nodes.
 *
 * @author marekfukala
 */
public class WebKitNodeDescription extends DOMNodeDescription {
    private NodeListener nodeListener;

    private void registerListener(final org.openide.nodes.Node nbNode) {
        
        nodeListener = new NodeListener() {

            @Override
            public void childrenAdded(NodeMemberEvent ev) {
                fireChange();
            }

            @Override
            public void childrenRemoved(NodeMemberEvent ev) {
                fireChange();
            }

            @Override
            public void childrenReordered(NodeReorderEvent ev) {
                fireChange();
            }

            @Override
            public void nodeDestroyed(NodeEvent ev) {
                fireChange();
            }

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                fireChange();
            }

        };
        
        nbNode.addNodeListener(NodeOp.weakNodeListener(nodeListener, nbNode));
    }

    private Node webKitNode;
    private final String elementPath;
    private final Map<String, String> attributes;
    private Collection<WebKitNodeDescription> children;
    private Description parent;
    private org.openide.nodes.Node nbNode;
    
    private transient final ChangeSupport changeSupport = new ChangeSupport(this);

    public void addChangeListener(ChangeListener listener ) {
        changeSupport.addChangeListener( listener );
    }
    
    public void removePropertyChangeListener(ChangeListener listener ) {
        changeSupport.removeChangeListener( listener );
    }

    public static WebKitNodeDescription forNode(Description parent, final org.openide.nodes.Node nbNode) {
        Node webKitNode = Utils.getWebKitNode(nbNode);
        if (webKitNode == null) {
            return null;
        }
        
        return new WebKitNodeDescription(parent, webKitNode, nbNode);
    }

    /**
     * fires changes recursively
     */
    private void fireChange() {
        changeSupport.fireChange();
        if (parent != null && (parent instanceof WebKitNodeDescription) ) {
            ((WebKitNodeDescription) parent).fireChange();
        }
    }

    private WebKitNodeDescription(Description parent, Node webKitNode, org.openide.nodes.Node nbNode) {
        Parameters.notNull("webKitNode", webKitNode);

        this.parent = parent;
        this.webKitNode = webKitNode;
        this.elementPath = new WebKitNodeTreePath(webKitNode).getElementPath();
        this.nbNode = nbNode;

        //init attributes map
        Collection<Attribute> attrs = webKitNode.getAttributes();
        if (attrs.isEmpty()) {
            attributes = Collections.emptyMap();
        } else {
            attributes = new HashMap<String, String>();
            for (Attribute a : attrs) {
                String key = a.getName().toLowerCase();
                String val = a.getValue();
                attributes.put(key, val);
            }
        }
        registerListener(nbNode);

    }
    
    public org.openide.nodes.Node getOONNode() {
        return nbNode;
    }
    
    public Node getWebKitNode() {
        return webKitNode;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WebKitNodeDescription)) {
            return false;
        }
        WebKitNodeDescription descr = (WebKitNodeDescription) obj;
        return Diff.equals(this, descr, false);
    }

    @Override
    public int hashCode() {
        return Diff.hashCode(this, false);
    }

    @Override
    public String getName() {
        return getName(webKitNode);
    }

    private static String getName(Node webKitNode) {
        return Utils.getWebKitNodeName(webKitNode);
    }

    @Override
    protected String getElementPath() {
        return elementPath;
    }

    @Override
    protected Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public synchronized Collection<WebKitNodeDescription> getChildren() {
        if (children == null) {
            final List<Node> wkChildren = webKitNode.getChildren();
            if (wkChildren == null || wkChildren.isEmpty()) {
                return Collections.emptyList();
            }
            children = new ArrayList<WebKitNodeDescription>();
            Children.MUTEX.readAccess(new Runnable() {
                @Override
                public void run() {
                    org.openide.nodes.Node[] nodes = nbNode.getChildren().getNodes();
                    for (Node child : wkChildren) {
                        int i = 0;
                        while (i < nodes.length && Utils.getWebKitNode(nodes[i]) != child) {
                            i++;
                        };
                        if (i >= nodes.length) {
                            //netbeans fake node
                            continue;
                        }
                        switch (child.getNodeType()) {
                            case org.w3c.dom.Node.ELEMENT_NODE:
                            case org.w3c.dom.Node.DOCUMENT_NODE:
                                children.add(new WebKitNodeDescription(WebKitNodeDescription.this, child, nodes[i]));
                                break;
                        }
                    }
                }
            });
        }
        return children==null?Collections.EMPTY_LIST:children;
    }

    @Override
    public int getType() {
        return DOM;
    }

    @Override
    public Description getParent() {
        return parent;
    }

    public static class WebKitNodeTreePath {

        private static final char ELEMENT_PATH_ELEMENTS_DELIMITER = '/';
        private static final char ELEMENT_PATH_INDEX_DELIMITER = '|';
        private Node first, last;

        public WebKitNodeTreePath(Node last) {
            this(null, last);
        }

        /**
         * @param first may be null; in such case a path from the root is
         * created
         */
        public WebKitNodeTreePath(Node first, Node last) {
            this.first = first;
            this.last = last;
        }

        public Node first() {
            return first;
        }

        public Node last() {
            return last;
        }

        /**
         * returns a list of nodes from the first node to the last node
         * including the boundaries.
         */
        public List<Node> path() {
            List<Node> path = new ArrayList<Node>();
            Node node = last;
            while (node != null) {
                path.add(node);
                if (node == first) {
                    break;
                }
                node = node.getParent();
            }
            return path;
        }

        @Override
        public String toString() {
            return getElementPath();
        }

        private static String getNodeId(Node node) {
            return Utils.getWebKitNodeName(node);
        }

        private String getElementPath() {
            StringBuilder sb = new StringBuilder();
            List<Node> p = path();
            for (int i = p.size() - 2; i >= 0; i--) { //do not include the root element
                Node node = p.get(i);
                Node parent = node.getParent();
                int myIndex = parent == null ? 0 : getIndexInSimilarNodes(node.getParent(), node);
                sb.append(getNodeId(node));
                if (myIndex > 0) {
                    sb.append(ELEMENT_PATH_INDEX_DELIMITER);
                    sb.append(myIndex);
                }

                if (i > 0) {
                    sb.append(ELEMENT_PATH_ELEMENTS_DELIMITER);
                }
            }
            return sb.toString();
        }

        private static int getIndexInSimilarNodes(final Node parent, final Node node) {
            final int index[] = new int[]{-1};
            Children.MUTEX.readAccess(new Runnable() {
                @Override
                public void run() {
                    for (Node child : parent.getChildren()) {
                        String nodeId = getNodeId(node);
                        String childId = getNodeId(child);
                        if (nodeId.equals(childId) && node.getNodeType() == child.getNodeType()) {
                            index[0]++;
                        }
                        if (child == node) {
                            break;
                        }
                    }
                }
            });
            return index[0];
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof WebKitNodeTreePath)) {
                return false;
            }
            WebKitNodeTreePath path = (WebKitNodeTreePath) o;
            return getElementPath().equals(path.getElementPath());
        }

        @Override
        public int hashCode() {
            return getElementPath().hashCode();
        }
    }
}
