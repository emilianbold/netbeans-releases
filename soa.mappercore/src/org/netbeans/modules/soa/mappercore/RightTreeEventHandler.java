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

import java.awt.Dimension;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;

/**
 *
 * @author anjeleevich
 */
public class RightTreeEventHandler extends AbstractMapperEventHandler {

    private MouseEvent initialEvent = null;
    private TreePath initialPath = null;
    private Graph initialTargetGraph = null;

    /** Creates a new instance of RightTreeEventHandler */
    public RightTreeEventHandler(RightTree rightTree) {
        super(rightTree.getMapper(), rightTree);
        new AutoScrollSelectionRightTree(rightTree);
    }

    private void reset() {
        initialEvent = null;
        initialPath = null;
        initialTargetGraph = null;
    }

    public void mousePressed(MouseEvent e) {
        reset();

        initialEvent = e;

        Mapper mapper = getMapper();
        RightTree rightTree = getRightTree();

        if (!rightTree.hasFocus()) {
            rightTree.requestFocusInWindow();
        }

        int y = e.getY();
        int x = e.getX();

        MapperNode node = getNodeAt(y);

        boolean canInitLink = true;

        if (node != null) {
            y = node.yToNode(y);

            int width = rightTree.getWidth();

            int nodeIndent = node.getIndent();
            int nodeContentHeight = node.getContentHeight();
            int nodeHeight = node.getHeight();

            int cy = node.getContentCenterY();
            int cx = width - nodeIndent + mapper.getRightIndent();

            boolean switchCollapsedExpandedState = false;

            if (!node.isLeaf()) {
                if (Math.abs(cx - x) <= 8 && Math.abs(cy - y) <= 8) {
                    switchCollapsedExpandedState = true;
                } else if (node.isCollapsed() && nodeContentHeight < nodeHeight) {
                    int x2 = width - nodeIndent - mapper.getTotalIndent();

                    Dimension size = rightTree.getChildrenLabel().getPreferredSize();

                    int x1 = x2 - size.width;
                    int y2 = nodeHeight - 2;
                    int y1 = nodeContentHeight + 1;

                    switchCollapsedExpandedState = x1 <= x && x < x2 && y1 <= y && y < y2;
                }
            }

            if (switchCollapsedExpandedState) {
                mapper.switchCollapsedExpandedState(node);
                canInitLink = false;
            } else if (x < width - nodeIndent) {
                mapper.setSelectedNode(node);
                if (e.isPopupTrigger()) {
                    canInitLink = false;
                    showPopupMenu(e);
                }
            }

            initialTargetGraph = node.getGraph();
            initialPath = node.getTreePath();
        }

        if (!canInitLink || initialTargetGraph == null || initialPath == null) {
            reset();
        }
    }

    public void mouseReleased(MouseEvent e) {
// Was:
//        reset();
//        if (e.isPopupTrigger() && getMapper().getNodeAt(e.getY()) != null) {
//            showPopupMenu(e);
//        }

        reset();

        Mapper mapper = getMapper();
        RightTree rightTree = getRightTree();

        if (!rightTree.hasFocus()) {
            rightTree.requestFocusInWindow();
        }

        int y = e.getY();
        int x = e.getX();

        MapperNode node = getNodeAt(y);

        if (node != null) {
            y = node.yToNode(y);

            int width = rightTree.getWidth();

            int nodeIndent = node.getIndent();
            int nodeContentHeight = node.getContentHeight();
            int nodeHeight = node.getHeight();

            int cy = node.getContentCenterY();
            int cx = width - nodeIndent + mapper.getRightIndent();

            boolean switchCollapsedExpandedState = false;

            if (!node.isLeaf()) {
                if (Math.abs(cx - x) <= 8 && Math.abs(cy - y) <= 8) {
                    switchCollapsedExpandedState = true;
                } else if (node.isCollapsed() && nodeContentHeight < nodeHeight) {
                    int x2 = width - nodeIndent - mapper.getTotalIndent();

                    Dimension size = rightTree.getChildrenLabel().getPreferredSize();

                    int x1 = x2 - size.width;
                    int y2 = nodeHeight - 2;
                    int y1 = nodeContentHeight + 1;

                    switchCollapsedExpandedState = x1 <= x && x < x2 && y1 <= y && y < y2;
                }
            }

            if (!switchCollapsedExpandedState && (x < width - nodeIndent)) {
                mapper.setSelectedNode(node);
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        if ((initialEvent != null) && (initialEvent.getPoint().distance(e.getPoint()) >= 5)) {
            
            LinkTool linkTool = getMapper().getLinkTool();
            Transferable transferable = null;
            Link link = null;
            for (int i = initialTargetGraph.getLinkCount() - 1; i >= 0; i--) {
                if (initialTargetGraph.getLink(i).getTarget() instanceof Graph) {
                    link = initialTargetGraph.getLink(i);
                }
            }
            if (link != null) {
                if (link.getSource() instanceof TreeSourcePin) {
                    TreeSourcePin source = (TreeSourcePin) link.getSource();
                    transferable = linkTool.activateOutgoing(source, 
                            link, initialPath);
                }
                if (link.getSource() instanceof Vertex  && 
                        getMapper().getNode(initialPath, true).isGraphExpanded()) 
                {
                    Vertex vertex = (Vertex) link.getSource();
                    transferable = linkTool.activateOutgoing(initialPath, vertex);
                }
                    
            } else {
                transferable = linkTool.activateIngoing(
                    initialPath, initialTargetGraph, null);
            }
            if (transferable != null) {
                startDrag(initialEvent, transferable, MOVE);
            }
            reset();
        }
    }
    
    
    @Override
    public void mouseClicked(MouseEvent e) {
        int y = e.getY();
        Mapper mapper = getMapper();
        MapperNode node = mapper.getNodeAt(y);
        if (node != null && e.getClickCount() == 2) {
            if (!node.isLeaf()) {
                mapper.setExpandedState(node.getTreePath(), node.isCollapsed());
            }
        }
    }

    
    private void showPopupMenu(MouseEvent event) {
        MapperContext context = getMapper().getContext();
        MapperModel model = getMapper().getModel();
        
        if (context == null || model == null) {
            return;
        }

        TreePath treePath = getSelectionModel().getSelectedPath();
        if (treePath == null) {
            return;
        }
        
        Object value = treePath.getLastPathComponent();
        if (value == null) {
            return;
        }

        JPopupMenu menu = context.getRightPopupMenu(model, value);
        if (menu != null) {
            menu.show(getRightTree(), event.getX(), event.getY());
        }
    }
}
