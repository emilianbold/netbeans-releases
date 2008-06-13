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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.MapperModel;

/**
 *
 * @author anjeleevich
 */
public class MoveTool extends AbstractMapperDnDTool {

    //private Transferable transferable;
    private GraphSubset graphSubSet;
    
    public MoveTool(Mapper mapper) {
        super(mapper);
    }

    Transferable getMoveTransferable(GraphSubset graphSubSet, Point location) {
        this.graphSubSet = graphSubSet;
         Point delta;
         if (graphSubSet.getGraph() != null && graphSubSet.getVertexCount() > 0) {
                int dx = graphSubSet.getMinYVertex().getX();
                int dy = graphSubSet.getMinYVertex().getY();
                dx = getCanvas().toCanvas(dx * getCanvas().getStep());
                MapperNode node = getCanvas().getNodeAt(location.y);
                dy = node.yToView(dy * getCanvas().getStep());
                delta = new Point(location.x - dx, location.y - dy);
            } else {
                delta = new Point();
            }
        return new MoveTransferable(graphSubSet, delta);
    }

    private void reset() {
        graphSubSet = null;
    }

    public boolean drag(JComponent component, DropTargetDragEvent dtde) {
        if (component != getCanvas()) {
            return false;
        }

        Transferable transferable = dtde.getTransferable();

        if (transferable == null) {
            reset();
            return false;
        }

        this.graphSubSet = getGraphSubset(transferable);

        if (this.graphSubSet == null) {
            return false;
        }
        
        int y = dtde.getLocation().getLocation().y;
        MapperNode node = getNodeAt(y);
        MapperModel model = getMapperModel();
        
        Graph graph = (node == null) ? null : node.getGraph();
        if (graph != null && model != null) {
            if (dtde.getDropAction() == DnDConstants.ACTION_COPY 
                    && model.canCopy(node.getTreePath(), graphSubSet)) 
            {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
            } else if (dtde.getDropAction() == DnDConstants.ACTION_MOVE
                    && model.canCopy(node.getTreePath(), graphSubSet)) 
            {
                dtde.acceptDrag(DnDConstants.ACTION_MOVE);
            } else {
                dtde.rejectDrag();
            }
        } else {
            dtde.rejectDrag();
        }
        
        return true;
    }

    public void drawDnDImage() {
    }

    private GraphSubset getGraphSubset(Transferable transferable) {
        if (transferable.isDataFlavorSupported(MOVE_GRAPHSUBSET_FLAVOR)) {
            try {
                return (GraphSubset) transferable.getTransferData(
                        MOVE_GRAPHSUBSET_FLAVOR);
            } catch (IOException ex) {
                return null;
            } catch (UnsupportedFlavorException ex) {
                return null;
            }
        }

        MapperModel model = getMapperModel();
        return model.getGraphSubset(transferable);
    }
    
    private Point getDelta(Transferable transferable) {
        if (transferable.isDataFlavorSupported(MOVE_DELTA_FLAVOR)) {
            try {
                return (Point) transferable.getTransferData(MOVE_DELTA_FLAVOR);
            } catch (UnsupportedFlavorException ex) {
                return new Point();
            } catch (IOException ex) {
                return new Point();
            }
        }
        return new Point();
    }

    public boolean drop(JComponent component, DropTargetDropEvent dtde) {
        if (component != getCanvas()) {
            return false;
        }

        Point point = dtde.getLocation();

        MapperModel model = getMapperModel();

        Canvas canvas = getCanvas();

        MapperNode node = canvas.getNodeAt(point.y);
        
        Point delta = getDelta(dtde.getTransferable());
        
        if (node != null) {
            int step = getMapper().getStepSize();
            int graphY = node.yToNode(point.y - delta.y);
            int graphX = canvas.toGraph(point.x - delta.x);
            if (dtde.getDropAction() == DnDConstants.ACTION_COPY || graphSubSet.getGraph() == null) {
                graphSubSet = model.copy(node.getTreePath(), graphSubSet,
                        (int) (Math.round(((double) (graphX)) / step)),
                        Math.max(0, (int) (Math.round(((double) (graphY)) / step))));
            } else {
                model.move(node.getTreePath(), graphSubSet,
                        (int) (Math.round(((double) (graphX)) / step)),
                        Math.max(0, (int) (Math.round(((double) (graphY)) / step))));
            }
            
            getCanvas().requestFocusInWindow();
            SelectionModel selectionModel = getSelectionModel();
            TreePath treePath = node.getTreePath();
            if (graphSubSet != null && graphSubSet.getVertexCount() > 0 &&
                    !selectionModel.isSelected(treePath, graphSubSet.getVertex(0)))
            {
                //selectionModel.setSelected(treePath, graphSubSet.getVertex(0));
                selectionModel.setSelected(treePath, graphSubSet);
            }
         }

        reset();

        return true;
    }


    public void startDrag(Transferable transferable){
    
    }
    
    public void dragDone() {
    }

    private static class MoveTransferable implements Transferable {

        private Point delta;
        private GraphSubset group;

        public MoveTransferable(GraphSubset group) {
            this.group = group;
            this.delta = new Point();
        }
        
        public MoveTransferable(GraphSubset group, Point delta) {
            this.group = group;
            this.delta = delta;
        }

        public DataFlavor[] getTransferDataFlavors() {
             return new DataFlavor[] {MOVE_GRAPHSUBSET_FLAVOR, 
             MOVE_DELTA_FLAVOR
             };
         } 
  
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor == MOVE_GRAPHSUBSET_FLAVOR ||
                    flavor == MOVE_DELTA_FLAVOR;
        }

        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            if (flavor == MOVE_GRAPHSUBSET_FLAVOR) {
                return group;
            }
            if (flavor == MOVE_DELTA_FLAVOR) {
                return delta;
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }
//    private static class MoveTransferable implements Transferable {
//        private Long uid;
//        
//        public MoveTransferable(long uid) {
//            this.uid = new Long(uid);
//        }
//        
//        public DataFlavor[] getTransferDataFlavors() {
//            return new DataFlavor[] { MOVE_DATA_FLAVOR};
//        }
//
//        public boolean isDataFlavorSupported(DataFlavor flavor) {
//            return flavor == MOVE_DATA_FLAVOR;
//        }
//
//        public Object getTransferData(DataFlavor flavor) 
//                throws UnsupportedFlavorException, IOException 
//        {
//            if (flavor == MOVE_DATA_FLAVOR) return uid;
//            throw new UnsupportedFlavorException(flavor);
//        }
//    }
    private static final DataFlavor MOVE_GRAPHSUBSET_FLAVOR = new DataFlavor(
            MoveTransferable.class, "MapperDataFlavor");
    
    private static final DataFlavor MOVE_DELTA_FLAVOR = new DataFlavor(
            Point.class, "MapperDeltaFlavor");

    private static synchronized long createUID() {
        return UID++;
    }
    private static long UID = 0;
}
