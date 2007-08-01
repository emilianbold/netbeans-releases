/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xslt.mapper.view;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.soa.mapper.basicmapper.BasicMapper;
import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;
import org.netbeans.modules.soa.mapper.common.IMapperEvent;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.IMapperViewModel;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.IMapperCanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdaterFactory;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeView;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xslt.mapper.methoid.LiteralUpdaterFactory;
import org.netbeans.modules.xslt.mapper.model.MapperContext;
import org.netbeans.modules.xslt.mapper.model.MapperContextChangeListener;
import org.netbeans.modules.xslt.mapper.model.SchemaModelBridge;
import org.netbeans.modules.xslt.mapper.model.XsltModelBridge;
import org.netbeans.modules.xslt.mapper.model.SourceTreeModel;
import org.netbeans.modules.xslt.mapper.model.XsltNodesTreeRenderer;
import org.netbeans.modules.xslt.mapper.model.nodes.Node;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.DeleteAction;
import org.netbeans.modules.xslt.mapper.model.targettree.TargetTreeModel;
import org.netbeans.modules.xslt.model.XslModel;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author Alexey
 */
public class XsltMapper extends BasicMapper implements HelpCtx.Provider{
    
    
    
    private MapperContext context;
    
    private Lookup lookup;
    
    private ILiteralUpdaterFactory myLiteralUpdaterFactory;
    private LiteralEditListener myLiteralEditListener;
    
    private XsltModelBridge xslModelBridge;
    private SchemaModelBridge schemaModelBridge;
    private DiagramBuilder diagramBuilder;
    
    private ErrorPanel errorPanel;
    
    private PredicateManager myPredicateManager;
    
    public XsltMapper(Lookup lookup) {
        super();
        this.lookup = lookup;
        this.context = (MapperContext)lookup.lookup(MapperContext.class);// TODO r new MapperContext(lookup);
        
        myPredicateManager = new PredicateManager(this);
        errorPanel = new ErrorPanel(this);
        
        initializeTrees();
        
        xslModelBridge = new XsltModelBridge(this);
        schemaModelBridge = new SchemaModelBridge(this);
        diagramBuilder = new DiagramBuilder(this);
        super.addMapperListener(xslModelBridge);
        super.setMapperRule(new XsltMapperRule(this));
        
        if (context != null) {
            context.addMapperContextChangeListener(new MapperContextChangeListener() {
                public void sourceTypeChanged(AXIComponent oldComponent, AXIComponent newComponent) {
                    // TODO a
                    //                    System.out.println("mappperView sourceType changed ");
                }
                public void targetTypeChanged(AXIComponent oldComponent, AXIComponent newComponent) {
                    // TODO a
                    //                    System.out.println("mappperView targetType changed ");
                }
                public void xslModelChanged(XslModel oldModel, XslModel newModel) {
                    // TODO a
                    //                    System.out.println("mappperView xslModel changed ");
                }

                public void xslModelStateChanged(State oldValue, State newValue) {
                    // TODO a
                    //                    System.out.println("mappperView xslModelStateChanged changed ");
                }

                public void tMapModelStateChanged(State oldValue, State newValue) {
                    // TODO a
                    //                    System.out.println("mappperView tMapModelStateChanged changed ");
                }
            });
        }
        //
        this.setLiteralUpdaterFactory(new LiteralUpdaterFactory(this));
        
        xslModelBridge.updateDiagram();
        
        
    }
    
    public MapperContext getContext(){
        return context;
    }
    
    public Lookup getLookup(){
        return lookup;
    }
    
    public DiagramBuilder getBuilder(){
        return this.diagramBuilder;
    }
    protected void initializeTrees() {
        
        JTree sourceTree = getMapperViewManager().getSourceView().getTree();
        sourceTree.setModel(new SourceTreeModel(this));
        sourceTree.setRootVisible(true);
        sourceTree.setShowsRootHandles(true);
        sourceTree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        sourceTree.expandRow(0);
        sourceTree.setCellRenderer(new XsltNodesTreeRenderer());
        //
        final JTree targetTree = getMapperViewManager().getDestView().getTree();
        targetTree.setModel(new TargetTreeModel(this));
        targetTree.setRootVisible(false);
        targetTree.setShowsRootHandles(true);
        targetTree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        targetTree.expandRow(0);
        targetTree.setCellRenderer(new XsltNodesTreeRenderer());
        //
        // Add the mouse listener for popup menu
        MouseListener pupupMouseListener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }
            
            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Object source = e.getSource();
                    assert source instanceof JTree;
                    TreePath path = ((JTree)source).getPathForLocation(
                            e.getX(), e.getY());
                    if (path != null) {
                        Object lastComp = path.getLastPathComponent();
                        if (lastComp instanceof TreeNode) {
                            TreeNode node = (TreeNode)lastComp;
                            JPopupMenu popup = node.constructPopupMenu();
                            //
                            if (popup != null) {
                                popup.show(e.getComponent(), e.getX(), e.getY());
                            }
                        }
                    }
                }
            }
        };
        //
        targetTree.addMouseListener(pupupMouseListener);
        sourceTree.addMouseListener(pupupMouseListener);
        //
        targetTree.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_DELETE) {
                    TreeNode selectedNode = (TreeNode)targetTree.
                            getSelectionPath().getLastPathComponent();
                    DeleteAction da = new DeleteAction(XsltMapper.this, selectedNode);
                    da.actionPerformed(null);
                }
            }
        });
        
    }
    
    public void setLiteralUpdaterFactory(ILiteralUpdaterFactory literalUpdaterFactory) {
        super.setLiteralUpdaterFactory(literalUpdaterFactory);
        //
        myLiteralUpdaterFactory = literalUpdaterFactory;
        //
        // also set up listener on literal edit events
        if (myLiteralEditListener == null) {
            IMapperCanvasView canvasView = getMapperViewManager().getCanvasView();
            myLiteralEditListener = new LiteralEditListener(this);
            canvasView.getCanvas().addCanvasMouseListener(myLiteralEditListener);
            getMapperController().addMapperListener(myLiteralEditListener);
        }
    }
    
    public ILiteralUpdaterFactory getLiteralUpdaterFactory() {
        return myLiteralUpdaterFactory;
    }
    
    
    
    public IMapperNode getMapperNode(TreeNode node){
        
        
        TreePath path = TreeNode.getTreePath(node);
        
        IMapperNode result = findNodeInTree(path, getMapperViewManager().getSourceView());
        
        if ( result == null ){
            return findNodeInTree(path, getMapperViewManager().getDestView());
        }
        
        return result;
        
        
        
        
    }
    private IMapperNode findNodeInTree(TreePath path, IMapperTreeView view){
        if (view.getTree().getModel().getRoot() == path.getPathComponent(0)){
            return view.getMapperTreeNode(path);
        }
        
        return null;
    }
    
    
    public void addLink(Node node_from, Node node_to){
        IMapperNode startNode = node_from.getOutputNode();
        IMapperNode endNode = node_to.getInputNode(node_from);
        if (startNode!= null & endNode != null){
            IMapperLink link = super.createLink(startNode, endNode);
            addLink(link);
        }
    }
    /**
     * Remove a link from this mapper. If any link end point nodes are tree
     * node, and no other links connect to the node. The tree node will also be
     * removed.
     *
     * @param link  the link to be remove from this mapper.
     */
    public void removeLink(IMapperLink link) {
        IMapperNode startNode = link.getStartNode();
        IMapperNode endNode = link.getEndNode();
        if (startNode != null) {
            startNode.removeLink(link);
        }
        if (endNode != null) {
            endNode.removeLink(link);
        }
        
        if ((getMapperViewManager().getMapperModel() != null)
                && (getMapperViewManager().getMapperModel().getSelectedViewModel() != null)) {
            IMapperViewModel viewModel = getMapperViewManager().getMapperModel().getSelectedViewModel();
            
            if (startNode instanceof IMapperTreeNode
                    && (startNode.getLinkCount() == 0)) {
                viewModel.removeNode(startNode);
            }
            
            if (endNode instanceof IMapperTreeNode
                    && (endNode.getLinkCount() == 0)) {
                viewModel.removeNode(endNode);
            }
        }
        if (!diagramBuilder.isUpdating()){
            getMapperViewManager().postMapperEvent(
                    MapperUtilities.getMapperEvent(this, link,
                    IMapperEvent.LINK_DEL, "Link removed from mapper"));
        }
    }
    /**
     * Add a link to this mapper. This method add the link to the start node and
     * end node if the link is not in the nodes yet. Next, add the start and end
     * node to the model, if any of the two node is a group node, it only adds
     * the root node to the model. Finally, it posts a IMapperEvent.NEW_LINK
     * event.
     *
     * @param link  the link to be added.
     */
    public void addLink(IMapperLink link) {
        IMapperNode startNode = link.getStartNode();
        IMapperNode endNode = link.getEndNode();
        connectLink(link);
        if ((getMapperViewManager().getMapperModel() != null)
                && (getMapperViewManager().getMapperModel().getSelectedViewModel() != null)) {
            IMapperViewModel viewModel = getMapperViewManager().getMapperModel().getSelectedViewModel();
            
            if (startNode != null) {
                IMapperNode groupNode = startNode;
                
                while (groupNode.getGroupNode() != null) {
                    groupNode = groupNode.getGroupNode();
                }
                
                if (!(viewModel.containsNode(groupNode))) {
                    addNode(groupNode);
                }
            }
            
            if (endNode != null) {
                IMapperNode groupNode = endNode;
                
                while (groupNode.getGroupNode() != null) {
                    groupNode = groupNode.getGroupNode();
                }
                
                if (!(viewModel.containsNode(groupNode))) {
                    addNode(groupNode);
                }
            }
        }
        
        if (!diagramBuilder.isUpdating()){
            getMapperViewManager().postMapperEvent(
                    MapperUtilities.getMapperEvent(this, link,
                    IMapperEvent.LINK_ADDED, "New Link added to mapper")); //NOI18N
        }
    }
    
    /**
     * Add a node to this mapper. Node is added (to fire event) only if there is
     * a selected view model in the mapper model. Then a IMapperEvent.NODE_ADDED
     * event is posted.
     *
     * @param node  the group node to be added.
     */
    public void addNode(IMapperNode node) {
        if ((getMapperViewManager().getMapperModel() != null)
                && (getMapperViewManager().getMapperModel().getSelectedViewModel() != null)) {
            
            IMapperViewModel viewModel = getMapperViewManager().getMapperModel().getSelectedViewModel();
            viewModel.addNode(node);
            if (!diagramBuilder.isUpdating()){
                getMapperViewManager().postMapperEvent(
                        MapperUtilities.getMapperEvent(this, node,
                        IMapperEvent.NODE_ADDED, "New Node added to mapper")); //NOI18N
            }
        }
    }
    
    public void setError(String message) {
        if (message == null){
            errorPanel.uninstall();
        } else {
            errorPanel.setMessage(message);
            errorPanel.install();
            
        }
    }
    
    public PredicateManager getPredicateManager() {
        return myPredicateManager;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("xslt_editor_design_about"); //NOI18N
    }
    
    
}
