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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.browser.api.Page;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.Visualizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author marekfukala
 */
public class HtmlNavigatorPanelUI extends JPanel implements ExplorerManager.Provider {

    private static final Logger LOGGER = Logger.getLogger(HtmlNavigatorPanelUI.class.getSimpleName());
    
    //Refresh queue
    public static RequestProcessor RP = new RequestProcessor(HtmlNavigatorPanelUI.class);

    private ExplorerManager manager;
    
    private BeanTreeView view;
    
    private Lookup lookup;
    
    private Page pageModel;
    
    /**
     * FileObject inspected by pageInspector
     * can be null.
     */
    private FileObject inspectedFileObject;
    //the UI actions
    private Action[] panelActions;
    //UI stuff
    private WaitNode waitNode = new WaitNode();
    
    //private HtmlElementDescription sourceDescription;
    private ChangeListener changeListener;
    
    private HashMap<Node, HtmlElementNode> domToNb = new HashMap<Node, HtmlElementNode>();    
    private RequestProcessor.Task domTask;
    private RequestProcessor.Task sourceTask;
    
    private FileObject lastInspectedFileObject;
    
    
    private final PropertyChangeListener pageInspectorListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (PageInspector.PROP_MODEL.equals(propName)) {
                final Page page = PageInspector.getDefault().getPage();
                setPageModel(page);
                refreshDOM();
            }
        }
    };

    private final PropertyChangeListener pageModelListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (Page.PROP_DOCUMENT.equals(propName)) {
                pageModelDocumentChanged();
            } else if (Page.PROP_SELECTED_NODES.equals(propName)) {
                updateSelection();
            } else if (Page.PROP_HIGHLIGHTED_NODES.equals(propName)) {
                updateHighlight();
            } else if (Page.PROP_BROWSER_SELECTED_NODES.equals(propName)) {
                updateEditor();
            }
        }
    };

    private final LookupListener lookupListener = new LookupListener() {
        @Override
        public void resultChanged(final LookupEvent ev) {
            refreshSource((Lookup.Result<Object>) ev.getSource());
        }
    };
    
    public HtmlNavigatorPanelUI() {
        manager = new ExplorerManager();
        initTreeView();

        setLayout(new BorderLayout());
        add(view, BorderLayout.CENTER);

        manager.setRootContext(waitNode);

        panelActions = new Action[]{};

        lookup = ExplorerUtils.createLookup(manager, getActionMap());
        final PageInspector pageInspector = PageInspector.getDefault();

        //listen on the page inspector
        if (pageInspector!=null)
            pageInspector.addPropertyChangeListener(pageInspectorListener);
        
        manager.addPropertyChangeListener(createSelectedNodesListener());
    }

    /** 
     * Set a new Page.
     * It will install a new PropertyChangeListener for 
     * the Page changes
     */ 
    private synchronized void setPageModel(Page model) {
        if (this.pageModel == model) {
            return;
        }
        
        if (this.pageModel != null) {
            this.pageModel.removePropertyChangeListener(pageModelListener);
        }
        this.pageModel = model;
        if (this.pageModel !=null) {
            this.pageModel.addPropertyChangeListener(pageModelListener);
        } else {
            pageModelRemoved();
        }
        inspectedFileObject = getInspectedFile(this.pageModel);
    }
    
    /**
     * Refresh DOM tree to empty
     */
    private void pageModelRemoved() {
        HtmlElementNode root = getRootNode();
        if (root != null) {
            root.setDescription(Description.empty(Description.DOM));
        }
    }

    private void pageModelDocumentChanged() {
        //try to find corresponding FileObject for the inspected document
        synchronized (this) {
            inspectedFileObject = getInspectedFile(this.pageModel);
        }
        
        inspectedFileChanged();
        
        RP.post(new Runnable() {
            public void run() {
                //refresh selection
                if (pageModel != null) {
                    List nodes = translate(manager.getSelectedNodes());
                    pageModel.setSelectedNodes(nodes);
                }
            }
        });
    }
    
    private void inspectedFileChanged() {
        HtmlElementNode root = getRootNode();
        if(root != null) {
            if(root.getFileObject() == null || root.getFileObject().equals(inspectedFileObject)) {
                //the inspected file no more corresponds to the navigator's active file
                refreshDOM();
            } else {
                root.setDescription(Description.empty(Description.DOM));
            }
        }
    }

    /**
     * Find corresponding FileObject for given pageModel.
     */
    @CheckForNull
    private FileObject getInspectedFile(Page pageModel) {
        if (pageModel == null) {
            return null;
        }
        String inspectedURL = pageModel.getDocumentURL();
        if (inspectedURL == null) {
            return null;
        }
        try {
            URL url = new URL(inspectedURL);
            Project owner = getCurrentProject();
            if (owner == null) {
                return null;
            }
            return ServerURLMapping.fromServer(owner, url);
        } catch (MalformedURLException ex) {
            //unknown url -> unknown fileObject
            return null;
        }
    }
    
    public synchronized void refreshDOM() {
        refreshDOM(0);
    }

    public synchronized void refreshDOM(int delay) {
        if (domTask != null) {
            domTask.cancel();
        }

        domTask = RP.post(new Runnable() {
            @Override
            public void run() {
                refreshNodeDOMStatus();
            }
        }, delay);
    }
    
    private synchronized void refreshSource(final Lookup.Result<Object> result) {
        if (sourceTask != null) {
            sourceTask.cancel();
        }

        sourceTask = RP.post(new Runnable() {
            @Override
            public void run() {
                refresh(result);
            }
        });
    }    
    
    private void cacheDomToNb(Node root) {
        if (root==null)
            return;
        if (root instanceof HtmlElementNode) {
            HtmlElementNode htmlElementNode = (HtmlElementNode)root;
            Node res = htmlElementNode.getDOMNode();
            if (res!=null) {
                domToNb.put(res, htmlElementNode);
            }
        }
        for (Node n:root.getChildren().getNodes()) {
            cacheDomToNb(n);
        }
    }

    private void refreshNodeDOMStatus() {
        LOGGER.fine("refreshNodeDOMStatus()");
        HtmlElementNode root = getRootNode();
        //now apply to dom descriptions
        if (pageModel == null) {
            return;
        }
        Node documentNode = pageModel.getDocumentNode();

        if (documentNode == null) {
            return;
        }
        
        WebKitNodeDescription domDescription = WebKitNodeDescription.forNode(null, documentNode);
        changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                refreshDOM();
            }
        };
        domDescription.addChangeListener(WeakListeners.change(changeListener, domDescription));
        if (((root.getFileObject() == null && inspectedFileObject == null)) || 
                (inspectedFileObject!=null && inspectedFileObject.equals(root.getFileObject()))) {
            root.setDescription(domDescription);
        } else {
            root.setDescription(WebKitNodeDescription.empty(WebKitNodeDescription.DOM));
        }

        domToNb.clear();
        cacheDomToNb(root);
        
        LOGGER.fine("root.refreshDOMStatus() called");
    }

    public Page getPageModel() {
        return pageModel;
    }

    public Action[] getActions() {
        return panelActions;
    }

    private Result<Object> contextResult;
    void activate(Lookup context) {
        contextResult = context.lookupResult(Object.class);
        contextResult.addLookupListener(lookupListener);
        refreshSource(contextResult);
    }
    
    private void refresh(Lookup.Result<Object> result) {
        Collection<? extends Object> allInstances = result.allInstances();
        if (allInstances.isEmpty()) {
            return;
        }

        Project p = null;
        FileObject f = null;
        URL url = null;
        for (Object o : result.allInstances()) {
            if (o instanceof Project) {
                p = (Project) o;
            } else if (o instanceof FileObject) { 
                f = (FileObject) o;
            } else if (o instanceof URL) {
                url = (URL) o;
            }
        }
        
        if (f==null) {
            return;
        }
        
        final FileObject fo = (p==null || url ==null)?f:ServerURLMapping.fromServer(p, url);
        
        if (fo != null) {
            if (!("text/html".equals(FileUtil.getMIMEType(fo)) || ("text/xhtml".equals(FileUtil.getMIMEType(fo))))) {
                return;
            }

            final PageInspector pageInspector = PageInspector.getDefault();
            if (pageInspector==null) {
                setPageModel(null);
            } else {
                setPageModel(pageInspector.getPage());
            }

            Source source = Source.create(fo);
            if (source == null || ! ("text/html".equals(source.getMimeType()) || "text/xhtml".equals(source.getMimeType()))) {
                return;
            }

            //TODO: uncomment when working again
            //showWaitNode();

            try {
                ParserManager.parse(Collections.singleton(source), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        ResultIterator it = WebUtils.getResultIterator(resultIterator, "text/html");
                        
                        setParserResult((HtmlParserResult) it.getParserResult());
                        //inspectedFileObject = getInspectedFileFromPageModel();
                        refreshDOM();
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            showWaitNode();
            final PageInspector pageInspector = PageInspector.getDefault();
            if (pageInspector==null) {
                setPageModel(null);
            } else {
                setPageModel(pageInspector.getPage());
            }
            setSourceDescription((SourceDescription)SourceDescription.empty(SourceDescription.SOURCE));
            refreshDOM();
        }
    }
    

    void deactivate() {
        RP.post(new Runnable() {
            public void run() {
                setPageModel(null);
            }
        });
        if (contextResult != null) {
            contextResult.removeLookupListener(lookupListener);
        }
        domToNb.clear();
    }

    private void showWaitNode() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                view.setRootVisible(true);
                manager.setRootContext(waitNode);
            }
        };
        
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    public HtmlElementNode getRootNode() {
        Node node = manager.getRootContext();
        return node instanceof HtmlElementNode ? (HtmlElementNode) node : null;
    }

    public void selectElementNode(final int offset) {
        HtmlElementNode root = getRootNode();
        if (root == null) {
            return;
        }

        final Node match = root.getNodeForOffset(offset);
        if (match == null) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Node[] selectedNodes = manager.getSelectedNodes();
                if (!(selectedNodes != null && selectedNodes.length == 1 && selectedNodes[0] == match)) {
                    try {
                        manager.setSelectedNodes(new Node[]{match});
                        LOGGER.log(Level.FINE, "Selected node set to {0}", match.getDisplayName());
                    } catch (PropertyVetoException propertyVetoException) {
                        Exceptions.printStackTrace(propertyVetoException);
                    }
                }
            }
        });
    }

    /**
     * Called by the SourceTask or NavigatorPanel when the ParserResult or the
     * caret position has changed.
     *
     * @param result
     * @param offset
     */
    public void setParserResult(HtmlParserResult result) {
        FileObject file = result.getSnapshot().getSource().getFileObject();
        setSourceDescription(new HtmlElementDescription(null, result.root(), file));
    }
    
    private void setSourceDescription(final SourceDescription description) {
        FileObject f;
        if (description instanceof HtmlElementDescription) {
            f = ((HtmlElementDescription) description).getFileObject();
        } else {       
            f = null;
        }
        
        final FileObject fileObject = f;
        
        final HtmlElementNode rootNode = getRootNode();

        if (rootNode != null && fileObject!=null && fileObject.equals(rootNode.getFileObject())) {
            //same file, just update the content
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    long startTime = System.currentTimeMillis();
                    rootNode.setDescription(description);
                    long endTime = System.currentTimeMillis();
                    Logger.getLogger("TIMER").log(Level.FINE, "Navigator Merge",
                            new Object[]{fileObject, endTime - startTime});

                    LOGGER.fine("refresh() - same file, descriptions updated");
                }
            };
            RP.post(r);
        } else {
            // new fileobject => refresh completely
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    view.setRootVisible(false);
                    view.setAutoWaitCursor(false);
                    
                    HtmlElementNode root = new HtmlElementNode(description, HtmlNavigatorPanelUI.this, fileObject);
                    manager.setRootContext(root);
                    
                    LOGGER.fine("refresh() - new file, set new explorer root node");

                    int expandDepth = -1;

                    // impl hack: Node expansion is synced by VisualizerNode to the AWT thread, possibly delayed
                    expandNodeByDefaultRecursively(manager.getRootContext(), 0, expandDepth);
                    // set soe back only after all pending expansion events are processed:
//                    Mutex.EVENT.writeAccess(new Runnable() {
//                        @Override
//                        public void run() {
//                            view.setScrollOnExpand( scrollOnExpand );
//                        }
//                    });
                    view.setAutoWaitCursor(true);
                }
            };
            RP.post(r);
        }

    }

    public void setCaretOffset(final int offset) {
        if (offset != -1) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    selectElementNode(offset);
                }
            });
        }
    }

    void performExpansion(final Collection<Node> expand, final Collection<Node> expandRec) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (Node n : expand) {
                    expandNode(n);
                }

                for (Node n : expandRec) {
                    expandNodeByDefaultRecursively(n);
                }
            }
        };

        RP.post(r);
    }

    public void expandNode(Node n) {
        view.expandNode(n);
    }

    private void expandNodeByDefaultRecursively(Node node) {
        // using 0, -1 since we cannot quickly resolve currentDepth
        expandNodeByDefaultRecursively(node, 0, -1);
    }

    private void expandNodeByDefaultRecursively(Node node, int currentDepth, int maxDepth) {
        if (maxDepth >= 0 && currentDepth >= maxDepth) {
            return;
        }
        expandNode(node);
        for (Node subNode : node.getChildren().getNodes()) {
            expandNodeByDefaultRecursively(subNode, currentDepth + 1, maxDepth);
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public Lookup getLookup() {
        return lookup;
    }

    private Project getCurrentProject() {
        if (inspectedFileObject != null) {
            return FileOwnerQuery.getOwner(lastInspectedFileObject=inspectedFileObject);
        }
        if (getRootNode()!=null && getRootNode().getFileObject()!=null) {
                return FileOwnerQuery.getOwner(lastInspectedFileObject=getRootNode().getFileObject());
            }
        Node rootContext = manager.getRootContext();
        FileObject fo = null;
        if (rootContext instanceof HtmlElementNode) {
            fo = ((HtmlElementNode) rootContext).getFileObject();
        } else {
            LOGGER.log(Level.WARNING, "Root context is not HtmlElementNode");
        }
        if (fo == null) {
            fo = lastInspectedFileObject;
            if (fo == null) {
                LOGGER.log(Level.WARNING, "Cannot find current project");
                return null;
            }
        }
        Project owner = FileOwnerQuery.getOwner(fo);
        return owner;
    }

    private static class WaitNode extends AbstractNode {

        private Image waitIcon = ImageUtilities.loadImage("org/netbeans/modules/html/navigator/resources/wait.png"); // NOI18N
        private String displayName;

        @NbBundle.Messages("lbl.wait.node=Please wait...")
        WaitNode() {
            super(Children.LEAF);
            displayName = Bundle.lbl_wait_node();
        }

        @Override
        public Image getIcon(int type) {
            return waitIcon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @java.lang.Override
        public java.lang.String getDisplayName() {
            return displayName;
        }
    }
    
    //Things copyied from Web Page Inspection module mostly DomPanel, which should be later removed

    /** Determines whether we are just updating view from the model. */
    private boolean updatingView = false;
    
    
    /**
     * Initializes the tree view.
     */
    private void initTreeView() {
        view = new BeanTreeView() {
            {
                MouseAdapter listener = createTreeMouseListener();
                tree.addMouseListener(listener);
                tree.addMouseMotionListener(listener);
                tree.setCellRenderer(createTreeCellRenderer(tree.getCellRenderer()));
            }
        };
        view.setAllowedDragActions(DnDConstants.ACTION_NONE);
        view.setAllowedDropActions(DnDConstants.ACTION_NONE);
        view.setRootVisible(false);
    }
    
/**
     * Creates a mouse listener for the DOM tree.
     * 
     * @return mouse listener for the DOM tree.
     */
    private MouseAdapter createTreeMouseListener() {
        return new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                processEvent(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                processEvent(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                processEvent(null);
                // Make sure that lastHover != <any potential value>
                // i.e., make sure that change in hover is triggered when
                // mouse returns into this component
                lastHover = new Object();
            }

            // The last node we were hovering over.
            private Object lastHover = null;
            
            /**
             * Processes the specified mouse event.
             * 
             * @param e mouse event to process.
             */
            private void processEvent(MouseEvent e) {
                Object hover = null;
                if (e != null) {
                    JTree tree = (JTree)e.getSource();
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if  (path != null) {
                        hover = path.getLastPathComponent();
                    }
                }
                if (hover != lastHover) {
                    lastHover = hover;
                    final List<? extends Node> highlight;
                    if (hover != null) {
                        Node node = Visualizer.findNode(hover);
                        if (node instanceof HtmlElementNode) {
                            final Node domNode = ((HtmlElementNode) node).getDOMNode();
                            if (domNode!=null) {
                                highlight = Arrays.asList(domNode);
                            } else {
                                highlight = Collections.EMPTY_LIST;
                            }
                        } else {
                            highlight = Arrays.asList(node);
                        }
                    } else {
                        highlight = Collections.EMPTY_LIST;
                    }
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            if (pageModel!=null)
                                pageModel.setHighlightedNodes(highlight);
                        }
                    });
                }
            }
            
        };
    }    
    
    /**
     * Highlighted (visualizer) nodes.
     * This collection is for rendering purposes only.
     */
    private final List highlightedTreeNodes = new ArrayList();

    /**
     * Updates the set of highlighted nodes.
     */
    final void updateHighlight() {
        if (pageModel==null) {
            return;
        }
        synchronized (highlightedTreeNodes) {
            highlightedTreeNodes.clear();
            //System.out.println("highlighted treenodes cleared");
            for (Node node : pageModel.getHighlightedNodes()) {
                Node n = getHtmlNode(node);
                if (n!=null) {
                    TreeNode visualizer = Visualizer.findVisualizer(n);
                    highlightedTreeNodes.add(visualizer);
                    //System.out.println("added " + highlightedTreeNodes);
                }
            }
        }
        view.repaint();
    }
    
    private HtmlElementNode getHtmlNode(Node node) {
        HtmlElementNode result;
        if (node instanceof HtmlElementNode) {
            result = (HtmlElementNode)node;
        } else {
            result = domToNb.get(node);
        }
        return result;
    }

    /**
     * Determines whether the given (visualizer) node is highlighted.
     * 
     * @param treeNode (visualizer) node to check.
     * @return {@code true} when the specified node should be highlighted,
     * returns {@code false} otherwise.
     */
    boolean isHighlighted(Object treeNode) {
        synchronized (highlightedTreeNodes) {
            //System.out.println("isHighlighted " + highlightedTreeNodes );
            //System.out.println("treeNode " + treeNode );
            return highlightedTreeNodes.contains(treeNode);
        }
    } 
    
    /**
     * Creates a cell renderer for the DOM tree.
     * 
     * @param delegate delegating/original tree renderer.
     * @return call renderer for the DOM tree.
     */
    private TreeCellRenderer createTreeCellRenderer(final TreeCellRenderer delegate) {
        Color origColor = UIManager.getColor("Tree.selectionBackground"); // NOI18N
        Color color = origColor.brighter().brighter();
        if (color.equals(Color.WHITE)) { // Issue 217127
            color = origColor.darker();
        }
        // Color used for hovering highlight
        final Color hoverColor = color;
        return new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel component;
                if (!selected && isHighlighted(value)) {
                    component = (JLabel)delegate.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, hasFocus);
                    component.setBackground(hoverColor);
                    component.setOpaque(true);
                } else {
                    component = (JLabel)delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                }
                return component;
            }
        };
    }
    
     /**
     * Updates the content of the panel. It fetches the current data
     * from the model and updates the view accordingly.
     */
    private void update() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                final Node node = pageModel.getDocumentNode();
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        //update(node);
                        updateSelection();
                        updateHighlight();
                    }
                });
            }
        });
    }
    
    /**
     * Updates the set of selected nodes.
     */
    private void updateSelection() {
        if (EventQueue.isDispatchThread()) {
            List<? extends Node> nodes = pageModel==null?Collections.EMPTY_LIST:pageModel.getSelectedNodes();
            ArrayList<HtmlElementNode> selection = new ArrayList<HtmlElementNode>();
            
            int i = 0;
            for (Node n:nodes) {
                HtmlElementNode htmlNode = getHtmlNode(n);
                if (htmlNode!=null) {
                    selection.add(htmlNode);
                }
            }
            updatingView = true;
            try {
                manager.setSelectedNodes(selection.toArray(new Node[0]));
            } catch (PropertyVetoException pvex) {
                Logger.getLogger(HtmlNavigatorPanelUI.class.getName()).log(Level.FINE, null, pvex);
            } finally {
                updatingView = false;
            }
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateSelection();
                }
            });
        }
    }
    
    private void updateEditor() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                List<? extends Node> nodes = pageModel == null ? Collections.EMPTY_LIST : pageModel.getSelectedNodes();
                for (Node n : nodes) {
                    HtmlElementNode htmlNode = getHtmlNode(n);
                    if (htmlNode != null) {
                        updateCaretInEditor(htmlNode);
                        break;
                    }
                }
            }
        });
    }
    
    /**
     * Editor caret update for the first selected node - won't open editor, or make it focused.
     */ 
    private void updateCaretInEditor(HtmlElementNode node) {
        FileObject fileObject = node.getFileObject();
        if (fileObject != null) {
            try {
                DataObject d = DataObject.find(fileObject);
                EditorCookie ec = (EditorCookie) d.getCookie(EditorCookie.class);
                if (ec != null) {
                    JEditorPane[] openedPanes = ec.getOpenedPanes();
                    if(openedPanes != null && openedPanes.length > 0) {
                        JEditorPane pane = openedPanes[0];
                        SourceDescription description = node.getSourceDescription();
                        if(description != null) {
                            int offset = description.getFrom();
                            if(offset != -1) {
                                pane.setCaretPosition(offset);
                            }
                        }
                    }
                }
            }
            catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    /**
     * Creates a listener for selected nodes.
     * 
     * @return listener for selected nodes.
     */
    private PropertyChangeListener createSelectedNodesListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if (ExplorerManager.PROP_SELECTED_NODES.equals(propName)) {
                    if (updatingView) {
                        // This change was triggered by update from the model
                        // => no need to synchronize back into the model.
                        return;
                    }
                    final List nodes = translate(manager.getSelectedNodes());
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            if (pageModel != null) {
                                pageModel.setSelectedNodes(nodes);
                            }
                        }
                    });
                }
            }

        };
    }    

    private List translate(Node[] selectedNodes) {
        List result = new ArrayList();
        for (Node n : selectedNodes) {
            if (n instanceof HtmlElementNode) {
                Node domNode = ((HtmlElementNode) n).getDOMNode();
                if (domNode == null) {
                    result.add(n);
                } else {
                    result.add(domNode);
                }
            }
        }
        return result;
    }
    
    /**
     * Only for tests
     * @param <T>
     * @param task
     * @return 
     */
    public <T> Future<T> performTest(Callable<T> task) {
        return RP.submit(task);
    }
    
}
