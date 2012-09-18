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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.inspect.PageModel;
import org.netbeans.modules.web.inspect.files.Files;
import org.netbeans.modules.web.inspect.webkit.ui.CSSStylesPanel;
import org.netbeans.modules.web.webkit.debugging.api.TransportStateException;
import org.netbeans.modules.web.webkit.debugging.api.dom.DOM;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * WebKit-based implementation of {@code PageModel}.
 *
 * @author Jan Stola
 */
public class WebKitPageModel extends PageModel {
    /** Request processor used by this class. */
    private RequestProcessor RP = new RequestProcessor(WebKitPageModel.class);
    /** Entry point to WebKit debugging API. */
    WebKitDebugging webKit;
    /** Document node. */
    private DOMNode documentNode;
    /** Nodes of the document (maps ID of the node to the node itself).*/
    private Map<Integer,DOMNode> nodes = Collections.synchronizedMap(new HashMap<Integer,DOMNode>());
    /** Selected nodes. */
    private List<? extends org.openide.nodes.Node> selectedNodes = Collections.EMPTY_LIST;
    /** Highlighted nodes. */
    private List<? extends org.openide.nodes.Node> highlightedNodes = Collections.EMPTY_LIST;
    /** WebKit DOM domain listener. */
    private DOM.Listener domListener;
    /** Determines whether the selection mode is switched on. */
    private boolean selectionMode;
    /** Determines whether the selection between the IDE and the browser pane is synchronized. */
    private boolean synchronizeSelection;
    /** Owner project of the inspected page. */
    private Project project;
    /** Page context. */
    private Lookup pageContext;
    /** Updater of the stylesheets in the browser according to changes of the corresponding source files. */
    private CSSUpdater cSSUpdater = CSSUpdater.getDefault();
    /**
     * Map with content documents in the inspected page. Maps node ID of
     * the document node to the corresponding {@code RemoteObject}.
     */
    private Map<Integer,RemoteObject> contentDocumentMap = new HashMap<Integer,RemoteObject>();
    /** Logger used by this class */
    static final Logger LOG = Logger.getLogger(WebKitPageModel.class.getName());

    /**
     * Creates a new {@code WebKitPageModel}.
     *
     * @param pageContext page context.
     */
    public WebKitPageModel(Lookup pageContext) {
        this.pageContext = pageContext;
        this.webKit = pageContext.lookup(WebKitDebugging.class);
        this.project = pageContext.lookup(Project.class);
        addPropertyChangeListener(new WebPaneSynchronizer());

        // Register DOM domain listener
        domListener = createDOMListener();
        DOM dom = webKit.getDOM();
        dom.addListener(domListener);

        try {
            initializePage();
        } catch (TransportStateException tsex) {
            // The underlying transport became invalid
            // before the page was initialized.
        }
    }

    /**
     * Prepares the page for inspection.
     */
    private void initializePage() {
        // documentUpdated event is not delivered when no node information
        // was sent to the client => requesting document node to make sure
        // that we obtain next documentUpdated event (that we need to be able
        // to reinitialize the page)
        org.openide.nodes.Node node = getDocumentNode();

        if (node == null) {
            LOG.info("getDocumentNode() returned null!"); // NOI18N
        } else {
            // Do not initialize the temporary page unnecessarily
            Node webKitNode = node.getLookup().lookup(Node.class);
            webKitNode = convertNode(webKitNode);
            Node.Attribute attr = webKitNode.getAttribute(":netbeans_temporary"); // NOI18N
            if (attr == null) {
                // init
                String initScript = Files.getScript("initialization"); // NOI18N
                webKit.getRuntime().evaluate(initScript);
                cSSUpdater.start(webKit);
            }
        }
    }

    /**
     * Returns the underlaying {@code WebKitDebugging} object.
     *
     * @return the underlaying {@code WebKitDebugging} object.
     */
    public WebKitDebugging getWebKit() {
        return webKit;
    }

    /**
     * Returns the owner project of the inspected page.
     * 
     * @return the owner project of the inspected page.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Returns the page context.
     * 
     * @return page context.
     */
    public Lookup getPageContext() {
        return pageContext;
    }

    @Override
    protected void dispose() {
        DOM dom = webKit.getDOM();
        dom.removeListener(domListener);
        cSSUpdater.stop();
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

    @Override
    public String getDocumentURL() {
        String documentURL = null;
        org.openide.nodes.Node node = getDocumentNode();
        if (node != null) {
            Node webKitNode = node.getLookup().lookup(Node.class);
            if (webKitNode != null) {
                documentURL = webKitNode.getDocumentURL();
            }
        }
        return documentURL;
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
                        domNode.updateChildren(parent);
                    }
                }
            }

            @Override
            public void childNodeRemoved(Node parent, Node child) {
                synchronized(WebKitPageModel.this) {
                    int nodeId = parent.getNodeId();
                    DOMNode domNode = nodes.get(nodeId);
                    if (domNode != null) {
                        domNode.updateChildren(parent);
                    }
                    // Nodes with a content document are removed and added
                    // again when a content document changes (and sometimes
                    // even when it doesn't change) => we are not removing
                    // them from 'nodes' collection to be able to reuse
                    // them once they are back.
                    Node contentDocument = child.getContentDocument();
                    if (contentDocument == null) {
                        nodes.remove(child.getNodeId());
                    } else {
                        contentDocumentMap.remove(contentDocument.getNodeId());
                    }
                }
            }

            @Override
            public void childNodeInserted(Node parent, Node child) {
                synchronized(WebKitPageModel.this) {
                    int nodeId = parent.getNodeId();
                    updateNodes(child);
                    DOMNode domNode = nodes.get(nodeId);
                    if (domNode != null) {
                        domNode.updateChildren(parent);
                    }
                }
            }

            @Override
            public void documentUpdated() {
                synchronized(WebKitPageModel.this) {
                    nodes.clear();
                    contentDocumentMap.clear();
                    documentNode = null;
                    selectedNodes = Collections.EMPTY_LIST;
                    highlightedNodes = Collections.EMPTY_LIST;
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
                        if (!isSelectionMode()) {
                            // Some delayed selection/highlight modifications
                            // can appear after deactivation of the selection mode
                            // => ignore these delayed events
                            return;
                        }
                        Node.Attribute attr = node.getAttribute(attrName);
                        DOMNode n = getNode(node.getNodeId());
                        final List<? extends org.openide.nodes.Node> selection;
                        if (n == null) {
                            selection = Collections.EMPTY_LIST;
                        } else {
                            String attrValue = attr.getValue();
                            if ("set".equals(attrValue)) { // NOI18N
                                selection = Collections.singletonList(n);
                            } else if ("clear".equals(attrValue)) { // NOI18N
                                selection = Collections.EMPTY_LIST;
                            } else {
                                List<org.openide.nodes.Node> newSelection = new ArrayList<org.openide.nodes.Node>();
                                newSelection.addAll(selectedNodes);
                                if ("add".equals(attrValue)) { // NOI18N
                                    newSelection.add(n);
                                } else if ("remove".equals(attrValue)) { // NOI18N
                                    newSelection.remove(n);
                                }
                                selection = newSelection;
                            }
                        }
                        RP.post(new Runnable() {
                            @Override
                            public void run() {
                                if (selected) {
                                    setSelectedNodes(selection);
                                } else {
                                    setHighlightedNodesImpl(selection);
                                }
                            }
                        });
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
        boolean updateChildren = false;
        List<Node> subNodes = null;
        synchronized (node) {
            List<Node> origSubNodes = node.getChildren();
            if (origSubNodes != null) {
                subNodes = new ArrayList<Node>(origSubNodes);
            }
        }
        if (subNodes == null) {
            int nodeType = node.getNodeType();
            if (nodeType == org.w3c.dom.Node.ELEMENT_NODE || nodeType == org.w3c.dom.Node.DOCUMENT_NODE) {
                webKit.getDOM().requestChildNodes(nodeId);
            }
        } else {
            for (Node subNode : subNodes) {
                updateNodes(subNode);
            }
            updateChildren = true;
        }
        final Node contentDocument = node.getContentDocument();
        if (contentDocument != null) {
            updateNodes(contentDocument);
            updateChildren = true;
            RP.post(new Runnable() {
                @Override
                public void run() {
                    String initScript = Files.getScript("initialization") // NOI18N
                        + "\nNetBeans.setSelectionMode("+selectionMode+");"; // NOI18N
                    RemoteObject remote = webKit.getDOM().resolveNode(contentDocument, null);
                    if (remote == null) {
                        LOG.log(Level.INFO, "Node with ID {0} resolved to null RemoteObject!", contentDocument.getNodeId()); // NOI18N
                    } else {
                        webKit.getRuntime().callFunctionOn(remote, "function() {\n"+initScript+"\n}");
                        synchronized (WebKitPageModel.this) {
                            contentDocumentMap.put(contentDocument.getNodeId(), remote);
                        }
                    }
                }
            });
        }
        if (updateChildren) {
            domNode.updateChildren(node);
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
        return nodes.get(nodeId);
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
        if (isSynchronizeSelection()) {
            setHighlightedNodesImpl(nodes);
        }
    }

    void setHighlightedNodesImpl(List<? extends org.openide.nodes.Node> nodes) {
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
        // Reset highlighted nodes
        if (!selectionMode) {
            setHighlightedNodesImpl(Collections.EMPTY_LIST);
        }
    }

    @Override
    public boolean isSelectionMode() {
        synchronized (this) {
            return selectionMode;
        }
    }

    @Override
    public void setSynchronizeSelection(boolean synchronizeSelection) {
        synchronized (this) {
            if (this.synchronizeSelection == synchronizeSelection) {
                return;
            }
            this.synchronizeSelection = synchronizeSelection;
        }
        firePropertyChange(PROP_SYNCHRONIZE_SELECTION, !synchronizeSelection, synchronizeSelection);
    }

    @Override
    public boolean isSynchronizeSelection() {
        synchronized (this) {
            return synchronizeSelection;
        }
    }

    /**
     * Invoke the specified script in all content documents.
     *
     * @param script script to invoke.
     */
    void invokeInAllDocuments(String script) {
        // Main document
        webKit.getRuntime().evaluate(script);

        // Content documents
        script = "function() {\n" + script + "\n}"; // NOI18N
        List<RemoteObject> documents;
        synchronized (this) {
            documents = new ArrayList<RemoteObject>(contentDocumentMap.size());
            documents.addAll(contentDocumentMap.values());
        }
        for (RemoteObject contentDocument : documents) {
            webKit.getRuntime().callFunctionOn(contentDocument, script);
        }
    }

    /**
     * Converts the WebKit node into a node that should be highlighted/selected.
     * Usually this method returns the passed node, but there are some exceptions
     * like document nodes.
     *
     * @param node node to convert.
     * @return node that should be highlighted/selected instead of the given node.
     */
    Node convertNode(Node node) {
        Node result = node;
        int type = node.getNodeType();
        if (type == org.w3c.dom.Node.DOCUMENT_NODE) {
            // Highlight/select document element
            synchronized (node) {
                List<Node> subNodes = node.getChildren();
                if (subNodes != null) {
                    for (Node subNode : subNodes) {
                        // There should be just one element
                        if (subNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                            result = subNode;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public CSSStylesView getCSSStylesView() {
        CSSStylesPanel view = CSSStylesPanel.getDefault();
        view.updatePageModel();
        return view;
    }

    class WebPaneSynchronizer implements PropertyChangeListener {
        private final Object LOCK_HIGHLIGHT = new Object();
        private final Object LOCK_SELECTION = new Object();

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            try {
                String propName = evt.getPropertyName();
                if (propName.equals(PageModel.PROP_HIGHLIGHTED_NODES)) {
                    if (shouldSynchronizeHighlight()) {
                        updateHighlight();
                    }
                } else if (propName.equals(PageModel.PROP_SELECTED_NODES)) {
                    if (shouldSynchronizeSelection()) {
                        updateSelection();
                    }
                } else if (propName.equals(PageModel.PROP_SELECTION_MODE)) {
                    updateSelectionMode();
                    updateSynchronization();
                } else if (propName.equals(PageModel.PROP_SYNCHRONIZE_SELECTION)) {
                    updateSelectionMode();
                    updateSynchronization();
                } else if (propName.equals(PageModel.PROP_DOCUMENT)) {
                    initializePage();
                    updateSelectionMode();
                }
            } catch (TransportStateException tse) {
                // The underlying transport became invalid. No need to worry
                // about failed synchronization as this means that the debugging
                // session has been finished.
            }
        }

        private boolean shouldSynchronizeSelection() {
            return isSelectionMode();
        }

        private boolean shouldSynchronizeHighlight() {
            return true;
        }

        private void updateSynchronization() {
            if (shouldSynchronizeSelection()) {
                updateSelection();
            } else {
                updateSelection(Collections.EMPTY_LIST);
            }
            if (shouldSynchronizeHighlight()) {
                updateHighlight();
            } else {
                updateHighlight(Collections.EMPTY_LIST);
            }
        }

        private void updateHighlight() {
            List<? extends org.openide.nodes.Node> nodes = getHighlightedNodes();
            updateHighlight(nodes);
        }

        private void updateHighlight(List<? extends org.openide.nodes.Node> nodes) {
            synchronized (LOCK_HIGHLIGHT) {
                // Initialize the next highlight in all content documents
                invokeInAllDocuments("NetBeans.initNextHighlight();"); // NOI18N

                // Add highlighted nodes into the next highlight (in their document)
                for (org.openide.nodes.Node node : nodes) {
                    Node webKitNode = node.getLookup().lookup(Node.class);
                    webKitNode = convertNode(webKitNode);
                    RemoteObject remote = webKit.getDOM().resolveNode(webKitNode, null);
                    if (remote != null) {
                        webKit.getRuntime().callFunctionOn(remote, "function() {NetBeans.addElementToNextHighlight(this);}"); // NOI18N
                    }
                }

                // Finalize the next highlight in all content documents
                invokeInAllDocuments("NetBeans.finishNextHighlight();"); // NOI18N
            }
        }

        private void updateSelection() {
            List<? extends org.openide.nodes.Node> nodes = getSelectedNodes();
            updateSelection(nodes);
        }

        private void updateSelection(List<? extends org.openide.nodes.Node> nodes) {
            synchronized (LOCK_SELECTION) {
                // Initialize the next selection in all content documents
                invokeInAllDocuments("NetBeans.initNextSelection();"); // NOI18N

                // Add selected nodes into the next selection (in their document)
                for (org.openide.nodes.Node node : nodes) {
                    Node webKitNode = node.getLookup().lookup(Node.class);
                    webKitNode = convertNode(webKitNode);
                    RemoteObject remote = webKit.getDOM().resolveNode(webKitNode, null);
                    if (remote != null) {
                        webKit.getRuntime().callFunctionOn(remote, "function() {NetBeans.addElementToNextSelection(this);}"); // NOI18N
                    }
                }

                // Finalize the next selection in all content documents
                invokeInAllDocuments("NetBeans.finishNextSelection();"); // NOI18N
            }
        }

        private synchronized void updateSelectionMode() {
            boolean selectionMode = isSelectionMode();
            
            // PENDING notify Chrome extension that the selection mode has changed

            // Activate/deactivate (observation of mouse events over) canvas
            invokeInAllDocuments("NetBeans.setSelectionMode("+selectionMode+")"); // NOI18N
        }

    }

}
