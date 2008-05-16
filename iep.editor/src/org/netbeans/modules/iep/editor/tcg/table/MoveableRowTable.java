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


package org.netbeans.modules.iep.editor.tcg.table;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JTable;

import org.netbeans.modules.iep.editor.tcg.exception.ErrorDisplay;


/**
 * MoveableRowTable is a JTable with added ability to allow dragging and
 * dropping of rows within itself.<br>
 * MoveableRowTable requires table model that ether implements
 * MoveableRowTableModel interface or extends DefaultMoveableTableModel
 *
 * @author Bing Lu
 *
 * @since July 8, 2002
 */
public class MoveableRowTable
    extends JTable
    implements DragGestureListener, DropTargetListener {

    /**
     * 
     */
    private static final long serialVersionUID = -9061291731143514732L;
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(MoveableRowTable.class.getName());

    /**
     * Construct MoveableRowTable with a default empty MoveableRowTbleModel
     */
    public MoveableRowTable() {

        super(new DefaultMoveableRowTableModel());

        // Install a dragsource
        DragSource dragSource = DragSource.getDefaultDragSource();

        dragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_MOVE, (DragGestureListener) this);

        // and a drag target
        new DropTarget(this, (DropTargetListener) this);
    }

    /**
     * Construct MoveableRowTable with the given MoveableRowTbleModel
     *
     * @param m The table model to use
     */
    public MoveableRowTable(MoveableRowTableModel m) {

        super(m);

        // Install a dragsource
        DragSource dragSource = DragSource.getDefaultDragSource();

        dragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_MOVE, (DragGestureListener) this);

        // and a drag target
        new DropTarget(this, (DropTargetListener) this);
    }

    // ------------------------------------------------------------------------------//
    // END Interface DropTargetListener                         //
    // ------------------------------------------------------------------------------//

    /**
     * Test method
     *
     * @param args Description of the Parameter
     */
    public static void main(String[] args) {

        Vector<String> colNames = new Vector<String>();

        colNames.add("Variable");
        colNames.add("Label");
        colNames.add("Value");

        Vector<Vector<String>> data = new Vector<Vector<String>>();

        for (int i = 1; i <= 30; i++) {
            Vector<String> aRow = new Vector<String>();

            aRow.add("variable " + i);
            aRow.add("label " + i);
            aRow.add("value " + i);
            data.add(aRow);
        }

        javax.swing.JFrame f = new javax.swing.JFrame("Test");

        f.getContentPane().add(
            new javax.swing.JScrollPane(
                new MoveableRowTable(
                    new DefaultMoveableRowTableModel(data, colNames))));
        f.pack();
        f.setVisible(true);
    }

    // ------------------------------------------------------------------------------//
    // END Interface DragGesturetListener                       //
    // ------------------------------------------------------------------------------//
    // ******************************************************************************//
    // BEGIN Interface DropTargetListener                       //
    // ******************************************************************************//

    /**
     * Called while a drag operation is ongoing, when the mouse pointer enters
     * the operable part of the drop site for the <code>DropTarget</code>
     * registered with this listener.
     *
     * @param dtde the <code>DropTargetDragEvent</code>
     */
    public void dragEnter(DropTargetDragEvent dtde) {

        // do nothing
    }

    /**
     * Called while a drag operation is ongoing, when the mouse pointer has
     * exited the operable part of the drop site for the
     * <code>DropTarget</code> registered with this listener.
     *
     * @param dte the <code>DropTargetEvent</code>
     */
    public void dragExit(DropTargetEvent dte) {

        // do nothing
    }

    // ******************************************************************************//
    // BEGIN Interface DragGestureListener                      //
    // ******************************************************************************//

    /**
     * A <code>DragGestureRecognizer</code> has detected a platform-dependent
     * drag initiating gesture and is notifying this listener in order for it
     * to initiate the action for the user.
     *
     * <P></p>
     *
     * @param dge the <code>DragGestureEvent</code> describing the gesture that
     *        has just occurred
     */
    public void dragGestureRecognized(DragGestureEvent dge) {

        JTable tbl = (JTable) dge.getComponent();
        int[] rowIdx = tbl.getSelectedRows();

        dge.startDrag(null, new TransferableRowIndices(rowIdx));
    }

    /**
     * Called when a drag operation is ongoing, while the mouse pointer is
     * still over the operable part of the drop site for the
     * <code>DropTarget</code> registered with this listener.
     *
     * @param dtde the <code>DropTargetDragEvent</code>
     */
    public void dragOver(DropTargetDragEvent dtde) {

        // do nothing
    }

    /**
     * Called when the drag operation has terminated with a drop on the
     * operable part of the drop site for the <code>DropTarget</code>
     * registered with this listener.
     *
     * <p>
     * This method is responsible for undertaking the transfer of the data
     * associated with the gesture. The <code>DropTargetDropEvent</code>
     * provides a means to obtain a <code>Transferable</code> object that
     * represents the data object(s) to be transfered.
     * </p>
     *
     * <P>
     * From this method, the <code>DropTargetListener</code> shall accept or
     * reject the drop via the acceptDrop(int dropAction) or rejectDrop()
     * methods of the <code>DropTargetDropEvent</code> parameter.
     * </p>
     *
     * <P>
     * Subsequent to acceptDrop(), but not before,
     * <code>DropTargetDropEvent</code> 's getTransferable() method may be
     * invoked, and data transfer may be performed via the returned
     * <code>Transferable</code>'s getTransferData() method.
     * </p>
     *
     * <P></p>
     *
     * @param dtde the <code>DropTargetDropEvent</code>
     */
    public void drop(DropTargetDropEvent dtde) {

        try {
            int toIdx = -1;
            int fromIdx = -1;
            int destIdx = -1;
            Transferable t = dtde.getTransferable();
            DataFlavor[] flavors = t.getTransferDataFlavors();

            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i]
                        .equals(TransferableRowIndices.ROW_INDICES_FLAVOR)) {
                    ArrayList indices =
                        (ArrayList) t.getTransferData(flavors[i]);

                    toIdx = ((Integer) indices.get(indices.size()
                                                   - 1)).intValue();
                    fromIdx = ((Integer) indices.get(0)).intValue();
                }
            }

            destIdx = this.rowAtPoint(dtde.getLocation());

            MoveableRowTableModel dtm = (MoveableRowTableModel) this.getModel();

            dtm.moveRow(fromIdx, toIdx, destIdx);
            dtde.dropComplete(true);
            mLog.info("Move indices [" + fromIdx + ", " + toIdx + "] to: "
                       + destIdx);
        } catch (java.io.IOException e) {
            mLog.warning(e.getMessage());
            new ErrorDisplay(this, e.getMessage());
        } catch (java.awt.datatransfer.UnsupportedFlavorException e) {
            mLog.warning(e.getMessage());
            new ErrorDisplay(this, e.getMessage());
        }
    }

    /**
     * Called if the user has modified the current drop gesture.
     *
     * <P></p>
     *
     * @param dtde the <code>DropTargetDragEvent</code>
     */
    public void dropActionChanged(DropTargetDragEvent dtde) {

        // do nothing
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
