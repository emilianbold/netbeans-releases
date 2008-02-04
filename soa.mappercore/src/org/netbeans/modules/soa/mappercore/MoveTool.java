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
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.GraphItem;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.MapperModel;

/**
 *
 * @author anjeleevich
 */
public class MoveTool extends AbstractMapperDnDTool {

    private Transferable transferable;
    private GraphSubset graphSubSet;
    private Point startPoint;

    public MoveTool(Mapper mapper) {
        super(mapper);
    }

    Transferable getMoveTransferable(GraphSubset graphSubSet) {
        this.graphSubSet = graphSubSet;
        return new MoveTransferable(graphSubSet);
    }

    private void reset() {
        transferable = null;
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

        if (transferable != this.transferable) {
            this.transferable = transferable;
            this.graphSubSet = getGraphSubset(transferable);
            this.startPoint = (graphSubSet.getGraph() != null) ? 
                    dtde.getLocation() : new Point(0, 0);
                
        
        }

        if (this.transferable != null && this.graphSubSet == null) {
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
        if (transferable != null) {

        }
    }

    private GraphSubset getGraphSubset(Transferable transferable) {
        if (transferable.isDataFlavorSupported(MOVE_DATA_FLAVOR)) {
            try {
                return (GraphSubset) transferable.getTransferData(
                        MOVE_DATA_FLAVOR);
            } catch (IOException ex) {
                return null;
            } catch (UnsupportedFlavorException ex) {
                return null;
            }
        }

        MapperModel model = getMapperModel();
        return model.getGraphSubset(transferable);
    }

    public boolean drop(JComponent component, DropTargetDropEvent dtde) {
        if (component != getCanvas()) {
            return false;
        }

        Point point = dtde.getLocation();

        MapperModel model = getMapperModel();

        Canvas canvas = getCanvas();

        MapperNode node = canvas.getNodeAt(point.y);
        
        if (node != null) {
            int step = getMapper().getStepSize();
            if (dtde.getDropAction() == DnDConstants.ACTION_COPY  || graphSubSet.getGraph() == null) {
                int graphY = node.yToNode(point.y) - step;
                int graphX = canvas.toGraph(point.x);


    
                model.copy(node.getTreePath(), graphSubSet, 
                        (graphX + step / 2) / step,
                        Math.max(0, (graphY + step / 2) / step));
        } else {
                model.move(node.getTreePath(), graphSubSet, 
                        (point.x - startPoint.x) / step, 
                        (point.y - startPoint.y) / step);
            }
        }

        reset();

        return true;
    }



    public void dragDone() {
    }

    private static class MoveTransferable implements Transferable {

        private GraphSubset group;

        public MoveTransferable(GraphSubset group) {
            this.group = group;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{MOVE_DATA_FLAVOR};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor == MOVE_DATA_FLAVOR;
        }

        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            if (flavor == MOVE_DATA_FLAVOR) {
                return group;
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
    private static final DataFlavor MOVE_DATA_FLAVOR = new DataFlavor(
            MoveTransferable.class, "MapperDataFlavor");

    private static synchronized long createUID() {
        return UID++;
    }
    private static long UID = 0;
}
