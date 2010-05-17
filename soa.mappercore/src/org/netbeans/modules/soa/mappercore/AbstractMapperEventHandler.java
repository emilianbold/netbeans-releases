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
package org.netbeans.modules.soa.mappercore;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.dnd.*;
import org.netbeans.modules.soa.mappercore.model.GraphItem;
import org.netbeans.modules.soa.mappercore.utils.Utils;

/**
 *
 * @author anjeleevich
 */
public abstract class AbstractMapperEventHandler extends MapperPropertyAccess
        implements MouseMotionListener, MouseListener,
        DnDHandler, DnDConstants 
{

    private JComponent component;
    private DnDSupport dndSupport;
    /** Swing Timer for expand node's parent with delay time. */
    private Timer timer;
    private Point oldPoint;

    public AbstractMapperEventHandler(Mapper mapper, JComponent component) {
        super(mapper);
        this.component = component;
        dndSupport = new DnDSupport(component, this);
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
    }

    public JComponent getComponent() {
        return component;
    }

    public abstract void mousePressed(MouseEvent e);

    public abstract void mouseReleased(MouseEvent e);

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public abstract void mouseDragged(MouseEvent e);

    public void mouseMoved(MouseEvent e) {
    }

    public void drag(JComponent component, DropTargetDragEvent dtde) {
        autoExpand(component, dtde);
        setSelected(component, dtde);
        setMoveOnTop(component, dtde);
        boolean processed = false;
        if (!processed) {
            processed = getLinkTool().drag(component, dtde);
        }
        if (!processed) {
            processed = getMoveTool().drag(component, dtde);
        }
        
        if (!processed) {
            dtde.rejectDrag();
        }
    }

    public void drop(JComponent component, DropTargetDropEvent dtde) {
        boolean processed = false;
        if (!processed) {
            processed = getLinkTool().drop(component, dtde);
        }
        if (!processed) {
            processed = getMoveTool().drop(component, dtde);
        }
        getMapper().setSelectedDndPath(null);
      
        removeTimer();
    }

    public void dragDone(JComponent component, DragSourceDropEvent dsde) {
        getLinkTool().done();
        getMoveTool().dragDone();
        getMapper().setSelectedDndPath(null);
        removeTimer();
    }

    protected void startDrag(MouseEvent event, Transferable transferable,
            int action) {
        dndSupport.startDrag(event, transferable, action);
    }

    public void dragExit(JComponent component) {
        removeTimer();
        Canvas canvas = getCanvas();
        RightTree rightTree = getRightTree();
        if (component == canvas || component == rightTree) {
            canvas.getMapper().setSelectedDndPath(null);
            canvas.getMapper().repaint();
        }
        
    }

    private void autoExpand(JComponent component, DropTargetDragEvent dtde) {
        LeftTree leftTree = getLeftTree();
        RightTree rightTree = getRightTree();
        Canvas canvas = getCanvas();
        Point point = dtde.getLocation();

        if (component == leftTree) {
            TreePath treePath = leftTree.getPathForLocation(point.x, point.y);
            if (!Utils.equal(point, oldPoint)) {
                removeTimer();
            }
            // expand with a delay
            if (((timer == null) || !timer.isRunning()) && (treePath != null) &&
                    //      !tree.isLeaf(treePath) &&
                    !leftTree.isExpanded(treePath)) {
                // node is candidate for expand
                final TreePath fTreePath = treePath;
                final JTree fTree = leftTree;
                // remove old timer
                removeTimer();
                // create new timer                
                timer = new Timer(DELAY_TIME_FOR_EXPAND,
                        new ActionListener() {

                            final public void actionPerformed(ActionEvent e) {
                                fTree.expandPath(fTreePath);
                            }
                        });
                timer.setRepeats(false);
                timer.start();
            }
            oldPoint = point;
        }

        if (component == rightTree) {
            TreePath treePath = rightTree.getTreePath(point.y);
            MapperNode dropNode = rightTree.getNodeAt(point.y);
            if (!Utils.equal(point, oldPoint)) {
                removeTimer();
            }
            // expand with a delay
            if (((timer == null) || !timer.isRunning()) && (treePath != null) &&
                    !dropNode.isLeaf() &&
                    !dropNode.isExpanded()) {
                // node is candidate for expand
                final TreePath fTreePath = treePath;
                final MapperNode node = dropNode;
                // remove old timer
                removeTimer();
                // create new timer                
                timer = new Timer(DELAY_TIME_FOR_EXPAND,
                        new ActionListener() {

                            final public void actionPerformed(ActionEvent e) {
                                getMapper().setExpandedState(fTreePath, true);
                            }
                        });
                timer.setRepeats(false);
                timer.start();
            }
            oldPoint = point;
        }

        if (component == canvas) {
            TreePath treePath = rightTree.getTreePath(point.y);
            MapperNode dropNode = rightTree.getNodeAt(point.y);
            if (!Utils.equal(point, oldPoint)) {
                removeTimer();
            }
            // expand with a delay
            if (((timer == null) || !timer.isRunning()) && (treePath != null) 
                    && !dropNode.isGraphExpanded()) 
            {
                // node is candidate for expand
                final TreePath fTreePath = treePath;
                final Mapper maper = rightTree.getMapper();
                // remove old timer
                removeTimer();
                // create new timer                
                timer = new Timer(DELAY_TIME_FOR_EXPAND,
                        new ActionListener() {

                            final public void actionPerformed(ActionEvent e) {
                                maper.setExpandedGraphState(fTreePath, true);
                            }
                        });
                timer.setRepeats(false);
                timer.start();
            }
            oldPoint = point;
        }
    }

    /** Removes timer and all listeners. */
    private void removeTimer() {
        if (timer != null) {
            ActionListener[] l = timer.getListeners(ActionListener.class);
            for (int i = 0; i < l.length; i++) {
                timer.removeActionListener(l[i]);
            }
            timer.stop();
            timer = null;
        }
    }

    private void setMoveOnTop(JComponent component, DropTargetDragEvent dtde) {
        Canvas canvas = getCanvas();

        if (component == canvas) {
            Point p = dtde.getLocation();
            CanvasSearchResult searchResult = getCanvas().find(p.x, p.y);
            if (searchResult == null) return; 
            
            GraphItem selectedItem = searchResult.getGraphItem();
            if (selectedItem == null) return; 
            
             selectedItem.moveOnTop();
        }
    }
    
     private void setSelected(JComponent component, DropTargetDragEvent dtde) {
        Canvas canvas = getCanvas();
        RightTree rightTree = getRightTree();
        
        if (component == canvas || component == rightTree) {
            Point point = dtde.getLocation();
            TreePath treePath = rightTree.getTreePath(point.y);
            canvas.getMapper().setSelectedDndPath(treePath);
            canvas.getMapper().repaint();
        }        
     }
     
     final static private int DELAY_TIME_FOR_EXPAND = 600;
}
