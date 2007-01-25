/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xslt.mapper.view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mapper.basicmapper.BasicMapper;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.IMapperCanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdaterFactory;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeView;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xslt.mapper.methoid.LiteralUpdaterFactory;
import org.netbeans.modules.xslt.mapper.model.MapperContext;
import org.netbeans.modules.xslt.mapper.model.MapperContextChangeListener;
import org.netbeans.modules.xslt.mapper.model.SourceTreeModel;
import org.netbeans.modules.xslt.mapper.model.XsltNodesTreeRenderer;
import org.netbeans.modules.xslt.mapper.model.nodes.Node;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionType;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.PopupMenuFactory;
import org.netbeans.modules.xslt.mapper.model.targettree.TargetTreeModel;
import org.netbeans.modules.xslt.model.XslModel;
import org.openide.util.Lookup;

/**
 *
 * @author Alexey
 */
public class XsltMapper extends BasicMapper {
    
    
    
    private MapperContext context;
    
    private Lookup lookup;
    
    private ILiteralUpdaterFactory myLiteralUpdaterFactory;
    private LiteralEditListener myLiteralEditListener;
    
    public XsltMapper(Lookup lookup) {
        super();
        this.lookup = lookup;
        this.context = (MapperContext)lookup.lookup(MapperContext.class);// TODO r new MapperContext(lookup);
        
        
        
        
        initializeTrees();
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
            });
        }
        //
        this.setLiteralUpdaterFactory(new LiteralUpdaterFactory(this));
        
        
    }
    
    public MapperContext getContext(){
        return context;
    }
    
    public Lookup getLookup(){
        return lookup;
    }
    
    protected void initializeTrees() {
        
        JTree sourceTree = getMapperViewManager().getSourceView().getTree();
        sourceTree.setModel(new SourceTreeModel(this));
        sourceTree.setRootVisible(true);
        sourceTree.setShowsRootHandles(true);
        sourceTree.expandRow(0);
        sourceTree.setCellRenderer(new XsltNodesTreeRenderer());
        
        final JTree targetTree = getMapperViewManager().getDestView().getTree();
        targetTree.setModel(new TargetTreeModel(this));
        targetTree.setRootVisible(false);
        targetTree.setShowsRootHandles(true);
        targetTree.expandRow(0);
        targetTree.setCellRenderer(new XsltNodesTreeRenderer());
        //
        // Add the mouse listener for popup menu
        targetTree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }
            
            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    TreePath path = targetTree.getPathForLocation(
                            e.getX(), e.getY());
                    if (path != null) {
                        Object lastComp = path.getLastPathComponent();
                        if (lastComp instanceof TreeNode) {
                            TreeNode node = (TreeNode)lastComp;
                            JPopupMenu popup = PopupMenuFactory.
                                    constructPopupMenu(node, ActionType.class);
                            if (popup != null) {
                                popup.show(e.getComponent(), e.getX(), e.getY());
                            }
                        }
                    }
                }
            }
        });
        
        new DiagramBuilder(this).updateDiagram();
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
    
    public void addLink(Node node_from, Node node_to){
        IMapperNode startNode = node_from.getOutputNode();
        IMapperNode endNode = node_to.getInputNode(node_from);
        if (startNode!= null & endNode != null){
            IMapperLink link = super.createLink(startNode, endNode);
            super.addLink(link);
        }
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
}
