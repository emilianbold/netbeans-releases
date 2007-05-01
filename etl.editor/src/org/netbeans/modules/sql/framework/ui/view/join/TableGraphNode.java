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
package org.netbeans.modules.sql.framework.ui.view.join;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.sql.framework.model.SQLJoinTable;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicCanvasArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicCellArea;

import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoText;

/**
 * This class represents a one cell level representation of a table
 * 
 * @author radval
 */
public class TableGraphNode extends BasicCanvasArea {
    protected JGoRectangle columnRect;
    private BasicCellArea numberArea;
    private BasicCellArea cellArea;

    /** Creates a new instance of ColumnGraphNode */
    public TableGraphNode() {
        super();
        this.setResizable(false);
        this.setSelectable(false);
        this.setGrabChildSelection(false);
        this.setUpdateGuiInfo(false);

    }

    public TableGraphNode(SQLJoinTable table) {
        this();
        this.setDataObject(table);
        SourceTable sTable = table.getSourceTable();

        this.setToolTipText(sTable.getQualifiedName());

        //set up the number area
        numberArea = new BasicCellArea("?");
        numberArea.setSelectable(false);
        numberArea.setResizable(false);

        numberArea.drawBoundingRect(true);
        numberArea.setTextAlignment(JGoText.ALIGN_CENTER);
        numberArea.setLeftGap(3);
        numberArea.setIconTextGap(0);
        numberArea.setInsets(new Insets(1, 0, 1, 1));
        numberArea.setSize(numberArea.getMinimumWidth(), numberArea.getMinimumHeight());
        numberArea.setBorder(JGoPen.black);
        numberArea.setBackGroundColor(new Color(241, 240, 227));
        this.addObjectAtTail(numberArea);

        cellArea = new BasicCellArea(BasicCellArea.RIGHT_PORT_AREA, sTable.getQualifiedName());
        cellArea.setSelectable(false);
        cellArea.setResizable(false);

        cellArea.drawBoundingRect(true);
        this.addObjectAtHead(cellArea);
        //this is a hack to add 1 in width to forec this to repaint and all its children
        //so that BasicCellArea can draw a bounding rect around it
        this.setSize(this.getMinimumWidth(), this.getMinimumHeight());
    }

    public TableGraphNode(String tableName) {
        this();
        this.setDataObject(null);

        this.setToolTipText(tableName);

        //set up the number area
        numberArea = new BasicCellArea("100");
        numberArea.setSelectable(true);
        numberArea.setResizable(true);

        numberArea.drawBoundingRect(true);
        numberArea.setTextAlignment(JGoText.ALIGN_CENTER);
        numberArea.setLeftGap(3);
        numberArea.setIconTextGap(0);
        numberArea.setInsets(new Insets(1, 0, 1, 1));
        numberArea.setSize(numberArea.getMinimumWidth(), numberArea.getMinimumHeight());
        numberArea.setBorder(JGoPen.black);
        numberArea.setBackGroundColor(new Color(241, 240, 227));
        this.addObjectAtTail(numberArea);

        cellArea = new BasicCellArea(BasicCellArea.RIGHT_PORT_AREA, tableName);
        cellArea.setSelectable(true);
        cellArea.setResizable(true);

        cellArea.drawBoundingRect(true);
        this.addObjectAtHead(cellArea);
        //this is a hack to add 1 in width to forec this to repaint and all its children
        //so that BasicCellArea can draw a bounding rect around it
        this.setSize(this.getMinimumWidth(), this.getMinimumHeight());

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
        return cellArea.getMinimumWidth() + numberArea.getMinimumWidth();
    }

    /**
     * Lays out this area's child objects.
     */
    public void layoutChildren() {
        Rectangle rect = this.getBoundingRect();
        numberArea.setBoundingRect(rect.x, rect.y, numberArea.getMinimumWidth(), rect.height);
        cellArea.setBoundingRect(rect.x + numberArea.getWidth(), rect.y, cellArea.getMinimumWidth() + 1, rect.height);
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
     * Gets field name associated with the given port
     * 
     * @param graphPort graph port
     * @return field name
     */
    public String getFieldName(IGraphPort graphPort) {
        SQLJoinTable joinTable = (SQLJoinTable) this.getDataObject();
        SourceTable sourceTable = joinTable.getSourceTable();

        if (sourceTable != null) {
            return sourceTable.toString();
        }

        return null;
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

    public void setNumber(String num) {
        this.numberArea.setText(num);
        this.setSize(this.getMinimumWidth(), this.getMinimumHeight());
    }
}

