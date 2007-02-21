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

package org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo;

import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.SwingUtilities;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoSelection;
import com.nwoods.jgo.JGoView;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.util.DrawPort;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMapperLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasLinkLabel;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasNode;

/**
 * <p>
 *
 * Title: </p> AbstractCanvasLink <p>
 *
 * Description: </p> AbstractCanvasLink provides the basic implemenation of a
 * canvas link with a JGoLink. <p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 */
public abstract class AbstractCanvasLink
     extends JGoLink
     implements ICanvasMapperLink {

    /**
     * the canvas of this link
     */
    private ICanvasView mCanvas;

    /**
     * the mapper link of this canvas link.
     */
    private IMapperLink mMapperLink;

    /**
     * the pixels of the middle segement indent
     */
    private static final int MID_SEGEMTNT_IDENT = 5;

    /**
     * the mid point of this link, this is for debugging purpose.
     */
    private int mMidPoint = 0;

    /**
     * the offset pixels that counts to be overlap segement.
     */
    private static final int OVERLAP_OFFSET_PIXELS = 2;

    /**
     * The value indicates the mid segement should go left.
     */
    private static final int INDENT_TO_LEFT = -1;

    /**
     * The value indicates the mid segement should go right.
     */
    private static final int INDENT_TO_RIGHT = 1;

    protected JGoPort endPort;
    protected JGoPort startPort;
    
    private JGoPen pen;
    private JGoBrush brush;
    private boolean mIsSelected;
    
    
    
    /**
     * Creates a new AbstractCanvasLink object, with a specified mapper link.
     *
     * @param link  the mapper link this canvas link repersents.
     */
    public AbstractCanvasLink(IMapperLink link) {
        mMapperLink = link;
        this.setRelinkable(false);
        this.setOrthogonal(true);
        this.setJumpsOver(true);
        
        this.pen = this.getPen();
        this.brush = this.getBrush();
    }
    
    protected AbstractCanvasLink(JGoPort fromPort, JGoPort toPort) {
        super(fromPort, toPort);
        this.setJumpsOver(true);
    }

    /**
     * Return the destination node of this link.
     *
     * @return   the destination node of this link.
     */
    public ICanvasNode getDestinationNode() {
        if (mMapperLink.getEndNode() instanceof ICanvasNode) {
            return (ICanvasNode) mMapperLink.getEndNode();
        }

        return null;
    }

    /**
     * Return the label of this link.
     *
     * @return   always null.
     */
    public ICanvasLinkLabel getLabel() {
        return null;
    }

    /**
     * Return the canvas of this link.
     *
     * @return   the canvas view of this link.
     */
    public ICanvasView getMapperCanvas() {
        return mCanvas;
    }

    /**
     * Return the mapper link.
     *
     * @return   the mapper link.
     */
    public IMapperLink getMapperLink() {
        return mMapperLink;
    }

    /**
     * Return the source node fo this link.
     *
     * @return   the source node fo this link.
     */
    public ICanvasNode getSourceNode() {
        if (mMapperLink.getStartNode() instanceof ICanvasNode) {
            return (ICanvasNode) mMapperLink.getStartNode();
        }

        return null;
    }

    /**
     * Set the label of this link
     *
     * @param label  the label of this link.
     */
    public void setLabel(ICanvasLinkLabel label) { }

    /**
     * Set if the label is visible.
     *
     * @param val  the label visible value
     */
    public void setLabelVisible(boolean val) { }

    /**
     * Set the canvas of this link.
     *
     * @param canvas  the canvas of this link.
     */
    public void setMapperCanvas(ICanvasView canvas) {
        mCanvas = canvas;
        pen = JGoPen.makeStockPen(DEFAULT_LINK_COLOR);
        brush = JGoBrush.makeStockBrush(DEFAULT_LINK_COLOR);
        this.setPen(pen);
        this.setBrush(brush);
    }

    /**
     * update the label text
     *
     * @param name  the text of the lable of this link
     */
    public void updateLabelText(String name) { }

    /**
     * Sets the dataObject attribute of the AbstractCanvasLink object
     *
     * @param obj  The new dataObject value
     */
    public void setDataObject(Object obj) { }

    /**
     * Gets the dataObject attribute of the AbstractCanvasLink object
     *
     * @return   The dataObject value
     */
    public Object getDataObject() {
        return null;
    }

    /**
     * Return true if this link is not hidding from the screen, such as by
     * scroll panel.
     *
     * @param viewRect  the rectangle of the viewable area
     * @return          true if this link is not hidding from the screen, such
     *      as by scroll panel.
     */
    public boolean isDisplaying(Rectangle viewRect) {
        return true;// ???
    }

    
    public void setPosition(int pos, int collectionSize) {
        mPosition = pos;
        mCollectionSize = collectionSize;
    }

    int mPosition = 0;
    int mCollectionSize = 0;


    /**
     * Return the mid point of this orthoal link. Override getMidOrthoPosition in JGoLink.
     *
     * @param from      the from point related to the mid point
     * @param to        the to point related to the mid point
     * @param vertical  true if the mid point is related to a vertical segment, false otherwise.
     * @return          The mid orthoal position value
     */
    protected int getMidOrthoPosition(int from, int to, boolean vertical) {
        int result = super.getMidOrthoPosition(from, to, vertical);
        if (mPosition > 0) {
            int newPos = from + (mPosition*6);
            if (newPos < result) {
                result = newPos;
            }
        }
        return result;
    }

    protected class NodeYChangeListener
         implements PropertyChangeListener {
        // PENDING -- the y axis difference between the tree view and
        // the canvas. This need to be calculate dynamically.

        /**
         * the difference between tree view and the canvas in y coordination.
         */
        protected static final int Y_AXIS_DIFF = 1;

        /**
         * the port of the link.
         */
        private JGoPort mPort;

        /**
         * the view contains this link.
         */
        private JGoView mView;

        /**
         * Creates a new NodeYChangeListener object.
         *
         * @param view  the view contains this link.
         * @param port  the port of the link.
         */
        public NodeYChangeListener(JGoView view, JGoPort port) {
            mPort = port;
            mView = view;
        }

        /**
         * Change the port of this link to alone with the mapper node y
         * coordination.
         *
         * @param e  the PropertyChangeEvent
         */
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName()
                .equals(IMapperNode.Y_CHANGE)) {

                final int topLoc = ((IMapperNode) e.getSource()).getY()
                    + Y_AXIS_DIFF
                    + mView.getViewPosition().y;
                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            mPort.setTop(topLoc);
                        }
                    });
            }
        }
    }
    
    protected void gainedSelection(JGoSelection jgoselection)
    {
        mIsSelected = true;
        super.gainedSelection(jgoselection);
        startHighlighting();
        if (getMapperCanvas().isPathHighlightingEnabled()) {
            new NetworkHighlightTraverser(true).visit(this);
        }
    }

    protected void lostSelection(JGoSelection jgoselection)
    {
        mIsSelected = false;
        super.lostSelection(jgoselection);
        stopHighlighting();
        if (getMapperCanvas().isPathHighlightingEnabled()) {
            new NetworkHighlightTraverser(false).visit(this);
        }
    }
    
    /**
     * start highlighting this link
     */
    public void startHighlighting() {
        this.setPen(JGoPen.make(
                JGoPen.SOLID, 
                mIsSelected ? 2 : 1, 
                DEFAULT_LINK_SELECTED_COLOR));
        this.setBrush(JGoBrush.makeStockBrush(DEFAULT_LINK_SELECTED_COLOR));
        highlightSingleLink(mMapperLink, getMapperCanvas());
    }

    /**
     * stop highlighting this link
     */
    public void stopHighlighting() {
        if (!mIsSelected) {
            this.setPen(pen);
            this.setBrush(brush);
            unHighlightSingleLink(mMapperLink, getMapperCanvas());
        }
    }
    
    public JGoPort getStartPort() {
        return this.startPort;
    }
    
    public JGoPort getEndPort() {
        return this.endPort; 
    }
    
    public static void initializeArrowHeads(JGoLink link) {
        link.setArrowLength(7.5);
        link.setArrowShaftLength(6.5);
        link.setArrowWidth(4);
    }
    
    private void highlightSingleLink(IMapperLink link, ICanvasView canvasView) {
        IMapperNode startNode = link.getStartNode();
        if (startNode instanceof IMapperTreeNode) {
            IMapperTreeNode startTreeNode = (IMapperTreeNode) startNode;
            startTreeNode.setHighlightLink(true);
            startTreeNode.setSelectedLink(mIsSelected);
            canvasView.getParentView().getViewManager().getSourceView().getTree().scrollPathToVisible(startTreeNode.getPath());
            canvasView.getParentView().getViewManager().getSourceView().getTree().repaint();
        }
        
        IMapperNode endNode = link.getEndNode();
        if (endNode instanceof IMapperTreeNode) {
            IMapperTreeNode endTreeNode = (IMapperTreeNode) endNode;
            endTreeNode.setHighlightLink(true);
            endTreeNode.setSelectedLink(mIsSelected);
            canvasView.getParentView().getViewManager().getDestView().getTree().scrollPathToVisible(endTreeNode.getPath());
            canvasView.getParentView().getViewManager().getDestView().getTree().repaint();
        }
        
        setPortActivated(startPort, true);
        setPortActivated(endPort,   true);
    }
    
    private void unHighlightSingleLink(IMapperLink link, ICanvasView canvasView) {
        IMapperNode startNode = link.getStartNode();
        if (startNode instanceof IMapperTreeNode) {
            IMapperTreeNode startTreeNode = (IMapperTreeNode) startNode;
            startTreeNode.setHighlightLink(false);
            startTreeNode.setSelectedLink(false);
            canvasView.getParentView().getViewManager().getSourceView().getTree().repaint();
            
        }
        
        IMapperNode endNode = link.getEndNode();
        if (endNode instanceof IMapperTreeNode) {
            IMapperTreeNode endTreeNode = (IMapperTreeNode) endNode;
            endTreeNode.setHighlightLink(false);
            endTreeNode.setSelectedLink(false);
            canvasView.getParentView().getViewManager().getDestView().getTree().repaint();
        }
        
        setPortActivated(startPort, false);
        setPortActivated(endPort,   false);
    }
    
    private static void setPortActivated(JGoPort port, boolean isActivated) {
        if (port instanceof BasicCanvasPort) {
            DrawPort drawnPort = 
                    ((BasicCanvasPort) port).getDrawPort();
            if (drawnPort != null) {
                drawnPort.setIsActivated(isActivated);
            }
        }
    }
}
