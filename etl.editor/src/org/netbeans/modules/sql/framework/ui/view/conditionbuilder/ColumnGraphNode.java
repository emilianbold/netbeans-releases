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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.sql.framework.model.ColumnRef;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicCanvasArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicCellArea;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ColumnGraphNode extends BasicCanvasArea {
    protected JGoRectangle columnRect;
    private BasicCellArea cellArea;

    /** Creates a new instance of ColumnGraphNode */
    public ColumnGraphNode() {
        super();

        columnRect = new JGoRectangle();
        columnRect.setSelectable(true);
        columnRect.setPen(JGoPen.lightGray);
        columnRect.setBrush(JGoBrush.makeStockBrush(new Color(254, 253, 235)));

        this.setResizable(false);
        this.setSelectable(true);
        this.setGrabChildSelection(true);

    }

    public ColumnGraphNode(ColumnRef column) {
        this();
        this.setDataObject(column);
        SQLObject col = column.getColumn();
        if (col != null) {
            SQLDBColumn origColumn = (SQLDBColumn) col;
            String toolTip = origColumn.getQualifiedName();
            this.setToolTipText(toolTip);
        }

        cellArea = new BasicCellArea(BasicCellArea.RIGHT_PORT_AREA, column.toString());
        cellArea.drawBoundingRect(true);
        this.addObjectAtTail(cellArea);

        // This is a hack to add 1 in width to forec this to repaint and all its children
        // so that BasicCellArea can draw a bounding rect around it
        this.setSize(this.getMinimumWidth() + 2, this.getMinimumHeight());
    }

    /**
     * Gets a list of all input and output links
     * 
     * @return list of input links
     */
    public List getAllLinks() {
        ArrayList list = new ArrayList();
        if (cellArea != null) {
            IGraphPort port = cellArea.getRightGraphPort();
            addLinks(port, list);
        }

        return list;
    }

    /**
     * Gets field name associated with the given port
     * 
     * @param graphPort graph port
     * @return field name
     */
    public String getFieldName(IGraphPort graphPort) {
        ColumnRef columnRef = (ColumnRef) this.getDataObject();
        SQLDBColumn column = (SQLDBColumn) columnRef.getColumn();
        if (column != null) {
            return column.toString();
        }

        return null;
    }

    /**
     * get the minimum height
     * 
     * @return min height
     */
    public int getMinimumHeight() {
        return cellArea.getMinimumHeight();
    }

    /**
     * get the minimum width
     * 
     * @return min width
     */
    public int getMinimumWidth() {
        return cellArea.getMinimumWidth();
    }

    /**
     * get output graph port , given a field name
     * 
     * @param fieldName field name
     * @return graph port
     */
    public IGraphPort getOutputGraphPort(String fieldName) {
        if (cellArea != null) {
            return cellArea.getRightGraphPort();
        }
        return null;
    }

    /**
     * Lays out this area's child objects.
     */
    public void layoutChildren() {
        cellArea.setBoundingRect(this.getBoundingRect());
    }
}

