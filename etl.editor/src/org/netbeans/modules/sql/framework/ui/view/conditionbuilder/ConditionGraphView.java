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

package org.netbeans.modules.sql.framework.ui.view.conditionbuilder;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;

import org.netbeans.modules.sql.framework.model.ColumnRef;
import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.ui.event.SQLDataEvent;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLGraphView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Logger;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ConditionGraphView extends SQLGraphView {

    /** Creates a new instance of ConditionGraphView */
    public ConditionGraphView() {
        super();
    }

    public void drop(java.awt.dnd.DropTargetDropEvent e) {
        try {

            if (e.isDataFlavorSupported(mDataFlavorArray[0])) {
                Transferable tr = e.getTransferable();

                if (!(tr.getTransferData(mDataFlavorArray[0]) instanceof SQLDBColumn)) {
                    super.drop(e);
                    return;
                }
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                SQLDBColumn column = (SQLDBColumn) tr.getTransferData(mDataFlavorArray[0]);

                Point viewCoord = e.getLocation();
                Point docCoord = viewToDocCoords(viewCoord);
                ColumnRef columnRef = SQLModelObjectFactory.getInstance().createColumnRef(column);

                GUIInfo gInfo = columnRef.getGUIInfo();
                gInfo.setX(docCoord.x);
                gInfo.setY(docCoord.y);

                ((ConditionGraphController) this.getGraphController()).handleNodeAdded(columnRef);
            } else {
                e.rejectDrop();
            }

        } catch (Exception ex) {
            Logger.printThrowable(Logger.ERROR, ConditionGraphView.class.getName(), "drop", "error in doing reload", ex);

            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message("Can not create Node in the canvas " + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        } finally {
            e.dropComplete(true);
        }

    }

    protected void createGraphNode(SQLDataEvent event) throws BaseException {
        SQLCanvasObject canvasObj = event.getCanvasObject();
        if (!(canvasObj instanceof ColumnRef)) {
            super.createGraphNode(event);
            return;
        }

        IGraphNode canvasNode = this.findGraphNode(canvasObj);
        // If graph node already exists then simply return
        if (canvasNode != null) {
            return;
        }

        GUIInfo gInfo = canvasObj.getGUIInfo();
        Point location = new Point(gInfo.getX(), gInfo.getY());

        ColumnGraphNode columnNode = new ColumnGraphNode((ColumnRef) canvasObj);
        columnNode.setLocation(location);
        this.addNode(columnNode);

    }
}

