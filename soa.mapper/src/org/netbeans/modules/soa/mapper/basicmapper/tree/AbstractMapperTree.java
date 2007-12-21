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

package org.netbeans.modules.soa.mapper.basicmapper.tree;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.dnd.Autoscroll;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.nwoods.jgo.JGoView;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import javax.swing.ToolTipManager;
import org.netbeans.modules.soa.mapper.basicmapper.BasicMapperView;
import org.netbeans.modules.soa.mapper.basicmapper.MapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeView;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.IMapperViewModel;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMapperLink;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasLink;
import org.netbeans.modules.soa.ui.TooltipTextProvider;

/**
 * <p>
 *
 * Title: </p> AbstractMapperTree <p>
 *
 * Description: </p> AbstractMapperTree provides common functionalities for
 * basic mapper tree.<p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 */
public abstract class AbstractMapperTree
extends BasicMapperView
implements IMapperTreeView {

    protected static final Stroke STROKE_SELECTED    = new BasicStroke(2);
    protected static final Stroke STROKE_HIGHLIGHTED = new BasicStroke(1);
    protected static final Stroke STROKE_FOLDED      = new BasicStroke(
            1,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL,
            0,
            new float[] { 5, 3 },
            0);
    
    int mScrollToVisible;

    /**
     * the mapper model listener.
     */
    private PropertyChangeListener mModelListener = new ViewModelChangeListener();

    /**
     * the node list listener
     */
    private PropertyChangeListener mNodeLinkListener = new NodeLinkListener();

    /**
     * the cached tree node of this tree, subset of viewmodel.
     */
    private List mTreeNode;

    /**
     * The java tree repersenting this mapper view
     */
    private JTree mTree;

    /**
     * the scroller that holds this tree
     */
    private JScrollPane mScroller;

    /**
     * TreePath which was under mouse last time
     */
    private TreePath lastPath;
    
    
    /**
     * Returns a JTree with a sample model.
     */
    public AbstractMapperTree() {
        this(null);
    }

    /**
     * Returns an instance of JTree which displays the root node -- the tree is
     * created using the specified data model.
     *
     * @param newModel  Description of the Parameter
     */
    public AbstractMapperTree(TreeModel newModel) {
        initialize();
        if (newModel != null) {
            mTree.setModel(newModel);
        }
        setAutoLayout(new MapperTreeAutoLayout(this));
    }

    /**
     * Return the scroller pane of this tree.
     *
     * @return   the scroller pane of this tree.
     */
    public JScrollPane getScrollerPane() {
        return mScroller;
    }

    /**
     * Set the view model of this view should display.
     *
     * @param viewModel  The new viewModel value
     */
    public void setViewModel(IMapperViewModel viewModel) {
        IMapperViewModel oldModel = getViewModel();
        if (oldModel != null) {
            oldModel.removePropertyChangeListener(mModelListener);
        }

        if (viewModel != null) {
            viewModel.addPropertyChangeListener(mModelListener);
            filterNodeModel(viewModel);
        } else {
            mTreeNode.clear();
        }

        super.setViewModel(viewModel);
        autoLayout();
    }

    /**
     * Gets the tree attribute of the AbstractMapperTree object
     *
     * @return   The tree value
     */
    public JTree getTree() {
        return mTree;
    }

    /**
     * Return the cached tree nodes for this tree, the cache is clear for each
     * view model.
     *
     * @return   a list of cached tree nodes.
     */
    public List getCachedTreeNodes() {
        return mTreeNode;
    }

    /**
     * Return the mapper tree node of this view that contains the specified tree
     * path.
     *
     * @param treePath  the tree path to be store in the mapper tree node.
     * @return          the mapper tree node of this view that contains the
     *      specified tree path.
     */
    public IMapperTreeNode getMapperTreeNode(TreePath treePath) {
        List cachedTreeNodes = getCachedTreeNodes();
        for (int i = 0; i < cachedTreeNodes.size(); i++) {
            IMapperTreeNode treeNode = (IMapperTreeNode) cachedTreeNodes.get(i);

            if (treeNode.getPath().equals(treePath)) {
                return treeNode;
            }
        }
        return newMapperTreeNode(treePath);
    }

    /**
     * Gives a description of this mapper tree.
     *
     * @return   a string repersentation of this mapper tree
     */
    public String toString() {
        String desc = super.toString() + " [link model=";

        if (getViewModel() != null) {
            desc += (
                getViewModel().getClass()
                .getName() + ", node count="
                + getViewModel().getNodeCount() + "]"
                );
        } else {
            desc += " null]";
        }

        return desc;
    }

    /**
     * Return the bounding rectangle of the specified tree path. This method
     * will return the path bounds depend if the lowest level tree path is
     * visible, if not it returns the one up level path bounds. If no path bound
     * can be found (all levels of the path is not visible), return null.
     *
     * @param path  the tree path to be matched
     * @return      the rectangle bounding of the specified path, or null the
     *      treepath is not visible or not belong to this tree.
     */
    public Rectangle getShowingPathRectBound(TreePath path) {
        Rectangle pathRect = null;

        /*
         * hiding treepath, get its parent
         */
        while ((path != null)
            && ((pathRect = getTree().getPathBounds(path)) == null)) {
            path = path.getParentPath();
        }

        // if the path is the root and the root is not for display.
        // return null to force the link not draw
        if (path != null
            && path.getParentPath() == null
            && !getTree().isRootVisible()) {
            return null;
        }

        return pathRect;
    }

    protected abstract void highlightSingleLink(IMapperLink link);
    
    protected abstract void unHighlightSingleLink(IMapperLink link);
    
    /**
     * Return the point where the link start to be drawn of the specified
     * rectangle.
     *
     * @param pathRect  the rectangle bounding of the path
     * @return          the point where the link start to be drawn of the
     *      specified rectangle.
     */
    public abstract Point getTreeNodePoint(Rectangle pathRect);

    /**
     * Autolayout the mapper nodes in this tree view.
     */
    protected void autoLayout() {
        if (getAutoLayout() != null) {
            getAutoLayout().autoLayout();
        } else {
            // backward comp
            mTree.repaint();
        }
    }

    /**
     * Draw the specified node on this tree graphics. The actural look of the
     * node is up to the subclass. This method is called by <code>paint(Graphics)</code>
     * when there is a node need to be displayed on the tree.
     *
     * @param g     the tree graphic where the link will be display on
     * @param node  Dthe specified tree node to be drawn
     */
    protected abstract void drawNode(Graphics g, IMapperTreeNode node);


    /**
     * Return a new mapper tree node of this view that contains the specified tree
     * path.
     *
     * @param treePath  the tree path to be store in the mapper tree node.
     * @return          a new mapper tree node of this view that contains the
     *      specified tree path.
     */
    protected abstract IMapperTreeNode newMapperTreeNode(TreePath treePath);
    

    /**
     * Since the view model of this tree is the layer that contains all the
     * nodes for the three views. This method filter out only store the nodes
     * that has a IMapperTreeNode Node and display it.
     *
     * @param viewModel  the view model.
     */
    protected void filterNodeModel(IMapperViewModel viewModel) {

        synchronized (mTreeNode) {
            Iterator iter = mTreeNode.iterator();
            while (iter.hasNext()) {
                ((IMapperNode) iter.next()).removePropertyChangeListener(mNodeLinkListener);
            }
            mTreeNode.clear();

            iter = viewModel.getNodes().iterator();
            while (iter.hasNext()) {
                Object node = iter.next();
                if (node instanceof IMapperTreeNode && contains((IMapperTreeNode) node)) {
                    ((IMapperTreeNode) node).addPropertyChangeListener(mNodeLinkListener);
                    mTreeNode.add(node);
                }
            }
        }
    }

    /**
     * Initialize this tree.
     */
    private void initialize() {
        mTreeNode = new Vector();
        mTree = new TreeView();
        mTree.addMouseMotionListener(new TreeMouseMotionAdaptor());
        
        mScroller = new JScrollPane(mTree);
        mScroller.getVerticalScrollBar().addAdjustmentListener(new VerticalBarListener());
        mScroller.setBorder(null);
        setViewComponent(mScroller);
    }

    public Point getViewOffset() {
        return getScrollerPane().getViewport().getViewPosition();
    }
    
    protected Color getDrawColor(IMapperTreeNode treeNode) {
        if        (treeNode.isSelectedLink()) {
            return ICanvasMapperLink.DEFAULT_LINK_SELECTED_COLOR;
        } else if (treeNode.isHighlightLink()) {
            return ICanvasMapperLink.DEFAULT_LINK_SELECTED_COLOR;
        }
        return ICanvasMapperLink.DEFAULT_LINK_COLOR;
    }
    
    protected void drawLine(
            IMapperTreeNode treeNode, 
            boolean isFolded, 
            Graphics g, 
            int x1, 
            int y1, 
            int x2, 
            int y2) {
        Graphics2D g2d = (Graphics2D) g;
        Stroke oldStroke = g2d.getStroke();
        Color oldColor = g2d.getColor();
        g.setColor(getDrawColor(treeNode));
        if        (treeNode.isSelectedLink()) {
            g2d.setStroke(STROKE_SELECTED);
        } else if (treeNode.isHighlightLink()) {
            g2d.setStroke(STROKE_HIGHLIGHTED);
        } else if (isFolded) {
            g2d.setStroke(STROKE_FOLDED);
        }
        g2d.drawLine(x1, y1, x2, y2);
        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }
    
    
    class TreeMouseMotionAdaptor extends MouseMotionAdapter {
        public void mouseMoved(MouseEvent e) {

            //if highlighting is not enabled then return
            if (!getViewManager().isHighlightLink() || !getViewManager().isToggleHighlighting()) {
                unHighlightLastPath();
                return;
            }

            TreePath path = mTree.getClosestPathForLocation(e.getX(), e.getY());
            if (path != null) {
                //unhighlight last path if it is differnt than current path
                if (lastPath != path) {
                    unHighlightLastPath();

                    //highlight current path
                    Rectangle rect = mTree.getPathBounds(path);
                    if (rect.contains(e.getX(), e.getY())) {
                        IMapperTreeNode node = getMapperTreeNode(path);
                        if (node.getLinkCount() > 0) {
                            node.setHighlightLink(true);
                            mTree.repaint();
                            //now also find out link in canvas and highlight
                            //them as well
                            highlightCanvasLinks(node.getLinks());
                            lastPath = path;
                        }

                    } else {
                        unHighlightLastPath();
                    }
                }
            } else {
                unHighlightLastPath();
            }
        }

        private void unHighlightLastPath() {
            if (lastPath != null) {
                IMapperTreeNode node = getMapperTreeNode(lastPath);
                node.setHighlightLink(false);
                mTree.repaint();
                lastPath = null;

                //now unhilight canvas links
                unHighlightCanvasLinks(node.getLinks());
            }
        }

        private void highlightCanvasLinks(List links) {
            Iterator it = links.iterator();
            while (it.hasNext()) {
                IMapperLink link = (IMapperLink) it.next();
                ICanvasLink cLink = getViewManager().getCanvasView().getCanvas()
                        .getCanvasLinkByDataObject(link);
                //if a canvas link is found then highlight it
                if (cLink != null) {
                    cLink.startHighlighting();
                }

                //also if link goes to opposite tree node then highlight link
                // in tree
                //and scroll to tree node.

                if (getViewManager().getCanvasView().getCanvas() instanceof JGoView) {
                    highlightSingleLink(link);
                }
            }

        }

        private void unHighlightCanvasLinks(List links) {
            Iterator it = links.iterator();
            while (it.hasNext()) {
                IMapperLink link = (IMapperLink) it.next();
                ICanvasLink cLink = getViewManager().getCanvasView().getCanvas()
                        .getCanvasLinkByDataObject(link);
                if (cLink != null) {
                    cLink.stopHighlighting();
                }

                //also if link goes to opposite tree node then unhighlight link
                // in tree

                if (getViewManager().getCanvasView().getCanvas() instanceof JGoView) {
                    unHighlightSingleLink(link);
                }
            }
        }
    }
    
    
    /**
     * TreeView draws the links of the mapper node on the JTree, and
     * autoscrolling enable.
     *
     * @author    sleong
     * @created   December 23, 2002
     */
    protected class TreeView
         extends JTree implements Autoscroll {

        public TreeView() {
            super();
            ToolTipManager.sharedInstance().registerComponent(this);
        }
        
		// Java BUG ID: 4407536
        // DropTarget.DropTargetAutoScroller.updateRegion does not take
        // into account the scroller view, thus, its region calculations are off.
        // Workaround:
        // To remedy this, we enter in huge number here in the insets size so that 
        // autoscrolling will always be triggered as we drag around in the tree.
        // - Josh
        private int UNREALISTIC_SIZE = 99999; // we want inner scroll region containment to return false
        private Insets mAutoscrollInsets = new Insets(20, 20, UNREALISTIC_SIZE, UNREALISTIC_SIZE);

        /**
         * Draws the tree and the linked lines. This method first call super the
         * draw the tree. Then loop through each nodes in the IViewModel, then
         * call <code>drawNode
         * </code> to draw the links.
         *
         * @param g  the Graphics that this tree to draw on.
         */
        public void paint(Graphics g) {
            super.paint(g);

            if (mTreeNode == null) {
                return;
            }

            Color orgColor = g.getColor();
            g.setColor(ICanvasMapperLink.DEFAULT_LINK_COLOR);
            
            synchronized (mTreeNode) {
                Iterator iter = mTreeNode.iterator();
                while (iter.hasNext()) {
                    IMapperTreeNode treeNode = (IMapperTreeNode) iter.next();
                    g.setColor(ICanvasMapperLink.DEFAULT_LINK_COLOR);
                    drawNode(g, treeNode);
                }
            }
            g.setColor(orgColor);
        }

        /**
         * Return the boundary of the autoscroll kicks in.
         *
         * @return   the boundary of the autoscroll kicks in.
         */
        public Insets getAutoscrollInsets() {
            return mAutoscrollInsets;
        }

        /**
         * Overrides java.awt.dnd.Autoscroll.autoscroll, this method is empty.
         *
         * @param point  A Point indicating the location of the cursor that
         *      triggered this operation
         */
        public void autoscroll(java.awt.Point point) {
            Point movePoint = new Point (point.x - 10, point.y - 10);
            this.scrollRectToVisible(new Rectangle (movePoint, new Dimension (30,30)));
        }

        public void scrollPathToVisible(TreePath path) {
            mScrollToVisible++;
            super.scrollPathToVisible(path);
            mScrollToVisible--;
        }
        
        public String getToolTipText(MouseEvent event) {
            TreePath tPath = this.getPathForLocation(event.getX(), event.getY());
            if (tPath != null) {
                Object lastPathComp = tPath.getLastPathComponent();
                if (lastPathComp != null && 
                        lastPathComp instanceof TooltipTextProvider) {
                    String txt = ((TooltipTextProvider)lastPathComp).getTooltipText();
                    if (txt != null && txt.length() != 0) {
                        return txt;
                    }
                }
            }
            //
            return super.getToolTipText(event);
        }
    }

    /**
     * VerticalBarListener listens on this scroller pane vertical bar adjustment
     * changes and set the node y.
     *
     * @author    Un Seng Leong
     * @created   December 4, 2002
     */
    private class VerticalBarListener
         implements AdjustmentListener {
        /**
         * Set the mapper tree node y position when the vertical scroll bar
         * changes.
         *
         * @param e  the AdjustmentEvent event object
         */
        public void adjustmentValueChanged(AdjustmentEvent e) {
            if (mScrollToVisible > 0) {
                return;
            }
            int yPos = getScrollerPane().getViewport().getViewPosition().y;

            Iterator iter = mTreeNode.iterator();
            Object node = null;
            Point nodePoint = null;
            TreePath path = null;

            while (iter.hasNext()) {
                node = iter.next();

                if (node instanceof IMapperTreeNode) {
                    path = ((IMapperTreeNode) node).getPath();

                    Rectangle pathRect = getShowingPathRectBound(path);

                    if (pathRect == null) {
                        return;
                    }

                    nodePoint = getTreeNodePoint(pathRect);

                    if ((nodePoint != null) && node instanceof MapperNode) {
                        ((MapperNode) node).setX(nodePoint.x);
                        ((MapperNode) node).setY(nodePoint.y - yPos);
                    }
                }
            }
        }
    }

    /**
     * This class listen on the change of add/remove node model. Call autoLayout
     * if any new or removed nodes of this tree.
     *
     * @author    sleong
     * @created   December 4, 2002
     */
    private class ViewModelChangeListener
         implements PropertyChangeListener {
        /**
         * Repaint the tree when there is a node changed on the view model.
         *
         * @param e  the PropertyChangeEvent
         */
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(IMapperViewModel.NODE_ADDED)) {

                Object node = e.getNewValue();
                if (node instanceof IMapperTreeNode
                    && contains((IMapperTreeNode) node)) {
                    mTreeNode.add(node);
                    ((IMapperTreeNode) node).addPropertyChangeListener(
                        mNodeLinkListener);
                    autoLayout();
                }
            } else if (
                e.getPropertyName().equals(IMapperViewModel.NODE_REMOVED)) {
                Object node = (IMapperNode) e.getOldValue();

                if (node instanceof IMapperTreeNode
                    && contains((IMapperTreeNode) node)) {
                    mTreeNode.remove(node);
                    ((IMapperTreeNode) node).removePropertyChangeListener(
                        mNodeLinkListener);
                    autoLayout();
                }
            }
        }
    }

    /**
     * Description of the Class
     *
     * @author    sleong
     * @created   February 16, 2003
     */
    private class NodeLinkListener implements PropertyChangeListener {
        /**
         * Repaint the tree when there is a link changed on the node.
         *
         * @param e  the PropertyChangeEvent
         */
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(IMapperNode.LINK_ADDED)
                || e.getPropertyName().equals(IMapperNode.LINK_REMOVED)) {
                autoLayout();
            }
        }
    }


}
