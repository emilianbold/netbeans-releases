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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.web.inspect.ElementHandle;
import org.netbeans.modules.web.inspect.PageModel;
import org.netbeans.modules.web.inspect.script.Script;
import org.netbeans.modules.web.webkit.debugging.api.dom.DOM;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;
import org.openide.util.RequestProcessor;

/**
 * WebKit-based implementation of {@code PageModel}.
 *
 * @author Jan Stola
 */
public class WebKitPageModel extends PageModel {
    /** Request processor used by this class. */
    private RequestProcessor RP = new RequestProcessor(WebKitPageModel.class);
    /** Entry point to WebKit debuggin API. */
    private WebKitDebugging webKit;
    /** Document node. */
    private DOMNode documentNode;
    /** Nodes of the document (maps ID of the node to the node itself).*/
    private Map<Integer,DOMNode> nodes = new HashMap<Integer,DOMNode>();
    /** Selected nodes. */
    private List<? extends org.openide.nodes.Node> selectedNodes = Collections.EMPTY_LIST;
    /** Highlighted nodes. */
    private List<? extends org.openide.nodes.Node> highlightedNodes = Collections.EMPTY_LIST;
    /** WebKit DOM domain listener. */
    private DOM.Listener domListener;
    /** Determines whether the selection mode is switched on. */
    private boolean selectionMode;

    /** Logger used by this class */
    //private java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(WebKitPageModel.class.getName());
    
    /**
     * Creates a new {@code WebKitPageModel}.
     * 
     * @param webKit WebKit to base the model on.
     */
    public WebKitPageModel(WebKitDebugging webKit) {
        this.webKit = webKit;
        addPropertyChangeListener(new WebPaneSynchronizer());
        
        // Register DOM domain listener
        domListener = createDOMListener();
        DOM dom = webKit.getDOM();
        dom.addListener(domListener);

        initializePage();
    }

    /**
     * Prepares the page for inspection.
     */
    private void initializePage() {
        // documentUpdated event is not delivered when no node information
        // was sent to the client => requesting document node to make sure
        // that we obtain next documentUpdated event (that we need to be able
        // to reinitialize the page)
        getDocumentNode();
        String initScript = Script.getScript("initialization"); // NOI18N
        webKit.getRuntime().evaluate(initScript);
    }

    @Override
    protected void dispose() {
        DOM dom = webKit.getDOM();
        dom.removeListener(domListener);
    }

    @Override
    public org.openide.nodes.Node getDocumentNode() {
        synchronized (this) {
            if (documentNode == null) {
                DOM dom = webKit.getDOM();
                Node node = dom.getDocument();
                if (node != null) {
                    documentNode = updateNodes(node);
                }
            }
            return documentNode;
        }
    }

    /**
     * Creates DOM domain listener.
     * 
     * @return DOM domain listener.
     */
    private DOM.Listener createDOMListener() {
        return new DOM.Listener() {
            @Override
            public void childNodesSet(Node parent) {
                synchronized(WebKitPageModel.this) {
                    int nodeId = parent.getNodeId();
                    DOMNode domNode = nodes.get(nodeId);
                    if (domNode != null) {
                        updateNodes(parent);
                        domNode.updateChildren();
                    }
                }
            }

            @Override
            public void childNodeRemoved(Node parent, Node child) {
                synchronized(WebKitPageModel.this) {
                    int nodeId = parent.getNodeId();
                    DOMNode domNode = nodes.get(nodeId);
                    if (domNode != null) {
                        domNode.updateChildren();
                    }
                    nodes.remove(child.getNodeId());
                }
            }

            @Override
            public void childNodeInserted(Node parent, Node child) {
                synchronized(WebKitPageModel.this) {
                    int nodeId = parent.getNodeId();
                    updateNodes(child);
                    DOMNode domNode = nodes.get(nodeId);
                    if (domNode != null) {
                        domNode.updateChildren();
                    }
                }
            }

            @Override
            public void documentUpdated() {
                synchronized(WebKitPageModel.this) {
                    nodes.clear();
                    documentNode = null;
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            firePropertyChange(PROP_DOCUMENT, null, null);
                        }
                    });
                }
            }

            @Override
            public void attributeModified(Node node, String attrName) {
                synchronized(WebKitPageModel.this) {
                    // Attribute modifications that represent selection/highlight
                    final boolean selected = ":netbeans_selected".equals(attrName); // NOI18N
                    final boolean highlighted = ":netbeans_highlighted".equals(attrName); // NOI18N
                    if (selected || highlighted) {
                        Node.Attribute attr = node.getAttribute(attrName);
                        String attrValue = attr.getValue();
                        DOMNode n = getNode(node.getNodeId());
                        final List<? extends org.openide.nodes.Node> selection;
                        // attrValue == "false" is sent when the selection should be cleared only
                        if (n == null || "false".equals(attrValue)) { // NOI18N
                            selection = Collections.EMPTY_LIST;
                        } else {
                            selection = Collections.singletonList(n);
                        }
                        RP.post(new Runnable() {
                            @Override
                            public void run() {
                                if (selected) {
                                    setSelectedNodes(selection);
                                } else {
                                    setHighlightedNodes(selection);
                                }
                            }
                        });
                        return;
                    }

                    // Notifications via attribute modification of the glass-pane
                    if (node.isInjectedByNetBeans()) {
                        Node.Attribute attr = node.getAttribute("id"); // NOI18N
                        if (attr != null && "netbeans_glasspane".equals(attr.getValue())) { // NOI18N
                            // Attribute modifications that represent selection mode switching
                            if (":netbeans_selection_mode".equals(attrName)) { // NOI18N
                                attr = node.getAttribute(attrName);
                                final boolean mode = "true".equals(attr.getValue()); // NOI18N
                                RP.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        setSelectionMode(mode);
                                    }
                                });
                            }
                        }
                        return;
                    }

                    // Update DOMNode
                    int nodeId = node.getNodeId();
                    DOMNode domNode = nodes.get(nodeId);
                    if (domNode != null) {
                        domNode.updateAttributes();
                    }
                }
            }

            @Override
            public void attributeRemoved(Node node, String attrName) {
                synchronized(WebKitPageModel.this) {
                    int nodeId = node.getNodeId();
                    DOMNode domNode = nodes.get(nodeId);
                    if (domNode != null) {
                        domNode.updateAttributes();
                    }
                }
            }

            @Override
            public void characterDataModified(Node node) {
                synchronized(WebKitPageModel.this) {
                    int nodeId = node.getNodeId();
                    DOMNode domNode = nodes.get(nodeId);
                    if (domNode != null) {
                        domNode.updateCharacterData();
                    }
                }
            }
        };
    }

    /**
     * Updates the map of known nodes with the information about the specified
     * node and its sub-nodes.
     * 
     * @param node node to start the update at.
     * @return {@code DOMNode} that corresponds to the specified node.
     */
    private DOMNode updateNodes(Node node) {
        int nodeId = node.getNodeId();
        DOMNode domNode = nodes.get(nodeId);
        if (domNode == null) {
            domNode = new DOMNode(this, node);
            nodes.put(nodeId, domNode);
        }
        List<Node> subNodes = node.getChildren();
        if (subNodes == null) {
            webKit.getDOM().requestChildNodes(nodeId);
        } else {
            for (Node subNode : subNodes) {
                updateNodes(subNode);
            }
            domNode.updateChildren();
        }
        return domNode;
    }

    /**
     * Returns {@code DOMNode} with the specified ID.
     * 
     * @param nodeId ID of the requested {@code DOMNode}.
     * @return {@code DOMNode} with the speicified ID.
     */
    DOMNode getNode(int nodeId) {
        synchronized(WebKitPageModel.this) {
            return nodes.get(nodeId);
        }
    }

    @Override
    public Map<String, String> getComputedStyle(ElementHandle element) {
        // PENDING
        return Collections.EMPTY_MAP;
    }

    @Override
    public Collection<ResourceInfo> getResources() {
        // PENDING
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<RuleInfo> getMatchedRules(ElementHandle element) {
        // PENDING
        return Collections.EMPTY_LIST;
    }

    @Override
    public void reloadResource(ResourceInfo resource) {
        // PENDING
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void setSelectedNodes(List<? extends org.openide.nodes.Node> nodes) {
        synchronized (this) {
            if (selectedNodes.equals(nodes)) {
                return;
            }
            selectedNodes = nodes;
        }
        firePropertyChange(PROP_SELECTED_NODES, null, null);
    }

    @Override
    public List<org.openide.nodes.Node> getSelectedNodes() {
        synchronized (this) {
            return Collections.unmodifiableList(selectedNodes);
        }
    }

    @Override
    public void setHighlightedNodes(List<? extends org.openide.nodes.Node> nodes) {
        synchronized (this) {
            if (highlightedNodes.equals(nodes)) {
                return;
            }
            highlightedNodes = nodes;
        }
        firePropertyChange(PROP_HIGHLIGHTED_NODES, null, null);
    }

    @Override
    public List<? extends org.openide.nodes.Node> getHighlightedNodes() {
        synchronized (this) {
            return Collections.unmodifiableList(highlightedNodes);
        }
    }

    @Override
    public void setSelectionMode(boolean selectionMode) {
        synchronized (this) {
            if (this.selectionMode == selectionMode) {
                return;
            }
            this.selectionMode = selectionMode;
        }
        firePropertyChange(PROP_SELECTION_MODE, !selectionMode, selectionMode);
    }

    @Override
    public boolean isSelectionMode() {
        synchronized (this) {
            return selectionMode;
        }
    }

    class WebPaneSynchronizer implements PropertyChangeListener {
        private final Object LOCK_HIGHLIGHT = new Object();
        private final Object LOCK_SELECTION = new Object();

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (propName.equals(PageModel.PROP_HIGHLIGHTED_NODES)) {
                updateHighlight();
            } else if (propName.equals(PageModel.PROP_SELECTED_NODES)) {
                updateSelection();
            } else if (propName.equals(PageModel.PROP_SELECTION_MODE)) {
                updateSelectionMode();
            } else if (propName.equals(PageModel.PROP_DOCUMENT)) {
                initializePage();
                updateSelectionMode();
            }
        }

        private void updateHighlight() {
            synchronized (LOCK_HIGHLIGHT) {
                List<? extends org.openide.nodes.Node> nodes = getHighlightedNodes();
                webKit.getRuntime().evaluate("NetBeans.initNextHighlight()"); // NOI18N
                for (org.openide.nodes.Node node : nodes) {
                    Node webKitNode = node.getLookup().lookup(Node.class);
                    RemoteObject remote = webKit.getDOM().resolveNode(webKitNode, null);
                    if (remote != null) {
                        webKit.getRuntime().callFunctionOn(remote, "function() {NetBeans.addElementToNextHighlight(this);}"); // NOI18N
                    }
                }
                webKit.getRuntime().evaluate("NetBeans.finishNextHighlight()"); // NOI18N
            }            
        }

        private void updateSelection() {
            synchronized (LOCK_SELECTION) {
                List<? extends org.openide.nodes.Node> nodes = getSelectedNodes();
                webKit.getRuntime().evaluate("NetBeans.initNextSelection()"); // NOI18N
                for (org.openide.nodes.Node node : nodes) {
                    Node webKitNode = node.getLookup().lookup(Node.class);
                    RemoteObject remote = webKit.getDOM().resolveNode(webKitNode, null);
                    if (remote != null) {
                        webKit.getRuntime().callFunctionOn(remote, "function() {NetBeans.addElementToNextSelection(this);}"); // NOI18N
                    }
                }
                webKit.getRuntime().evaluate("NetBeans.finishNextSelection()"); // NOI18N                    
            } 
        }

        private void updateSelectionMode() {
            boolean selectionMode = isSelectionMode();
            String code = "NetBeans.setSelectionMode("+selectionMode+")"; // NOI18N
            webKit.getRuntime().evaluate(code);
        }

    }

}
