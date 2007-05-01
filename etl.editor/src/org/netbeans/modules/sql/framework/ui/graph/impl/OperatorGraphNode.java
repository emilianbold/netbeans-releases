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
package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.netbeans.modules.sql.framework.ui.graph.IGraphFieldNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorField;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoText;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class OperatorGraphNode extends BasicCanvasArea {

    /* RCS id */
    static final String RCS_ID = "$Id$";

    private static final GradientBrush BRUSH_TITLE = new GradientBrush(new Color(221, 221, 255), // light
                                                                                                    // magenta
        new Color(160, 186, 213)); // navy

    private static final Color DEFAULT_BG_COLOR = new Color(254, 253, 235);

    private static final JGoPen PEN_DEFAULT = JGoPen.makeStockPen(Color.WHITE);

    protected ArrayList fieldList  = new ArrayList();;
    protected int verticalGap = 0;
    protected JGoRectangle columnRect;

    protected OperatorGraphFieldNode resultField;

    public OperatorGraphNode(String displayName, String toolTip, Icon icon) {
        super();
        columnRect = new JGoRectangle();
        columnRect.setSelectable(true);
        columnRect.setPen(PEN_DEFAULT);
        columnRect.setBrush(JGoBrush.makeStockBrush(DEFAULT_BG_COLOR));
        addObjectAtHead(columnRect);

        titleArea = new TitleArea(displayName);
        titleArea.setShowDot(false);
        titleArea.setTextAndExpandedImgGap(10);
        titleArea.setToolTipText(toolTip);
        titleArea.setTitleImage(icon);
        titleArea.setPen(PEN_DEFAULT);
        titleArea.setBrush(BRUSH_TITLE);
        this.addObjectAtTail(titleArea);

        this.setResizable(false);
        this.setSelectable(true);
        this.setGrabChildSelection(true);

        // RJR - remove setting of fieldlist which was causing a NPE
    }

    /**
     * Creates a new instance of OperatorGraphNode using GUI info contained in the given
     * configuration object.
     * 
     * @param info IOperatorXmlInfo object containing operator layout info.
     */
    public OperatorGraphNode(IOperatorXmlInfo info) {
        this(info.getDisplayName(), info.getToolTip(), info.getIcon());
    }

    /**
     * Initialize this instance using GUI info contained in the given configuration
     * object.
     * 
     * @param info IOperatorXmlInfo object containing operator layout info.
     */
    protected void initialize(IOperatorXmlInfo info) {
        // add input graph field
        for (int i = 0; i < info.getInputCount(); i++) {
            IOperatorField field = (IOperatorField) info.getInputFields().get(i);

            OperatorGraphFieldNode fieldNode = new OperatorGraphFieldNode(BasicCellArea.LEFT_PORT_AREA, field);

            this.addObjectAtTail(fieldNode);
            fieldList.add(fieldNode);
        }

        // add output graph field
        for (int i = 0; i < info.getOutputCount(); i++) {
            IOperatorField field = (IOperatorField) info.getOutputFields().get(i);

            OperatorGraphFieldNode fieldNode = new OperatorGraphFieldNode(BasicCellArea.RIGHT_PORT_AREA, field, JGoText.ALIGN_CENTER);

            // there is only one result field for operator
            resultField = fieldNode;
            this.addObjectAtTail(fieldNode);
            fieldList.add(fieldNode);
        }
    }

    /**
     * Gets the minimum height of this area
     * 
     * @return minimum height
     */
    public int getMinimumHeight() {
        int minHeight = 0;
        minHeight = getInsets().top + getInsets().bottom;
        minHeight += titleArea.getMinimumHeight();

        return minHeight;
    }

    /**
     * Gets maximum height
     * 
     * @return max height
     */
    public int getMaximumHeight() {
        int maxHeight = 0;
        maxHeight = getInsets().top + getInsets().bottom;
        maxHeight += titleArea.getMaximumHeight();

        for (int i = 0; i < fieldList.size(); i++) {
            CanvasArea fieldNode = (CanvasArea) fieldList.get(i);
            maxHeight += fieldNode.getMaximumHeight() + verticalGap;
        }

        return maxHeight;
    }

    /**
     * Gets the maximum width
     * 
     * @return max width
     */
    public int getMinimumWidth() {
        int minWidth = titleArea.getMinimumWidth();

        for (int i = 0; i < fieldList.size(); i++) {
            CanvasArea fieldNode = (CanvasArea) fieldList.get(i);

            int w = fieldNode.getMinimumWidth();
            if (w > minWidth) {
                minWidth = w;
            }
        }

        minWidth += getInsets().left + getInsets().right;
        return minWidth;
    }

    public int getMaximumWidth() {
        int maxWidth = titleArea.getMaximumWidth();

        for (int i = 0; i < fieldList.size(); i++) {
            CanvasArea fieldNode = (CanvasArea) fieldList.get(i);

            int w = fieldNode.getMaximumWidth();
            if (w > maxWidth) {
                maxWidth = w;
            }
        }

        maxWidth += getInsets().left + getInsets().right;
        return maxWidth;
    }

    /**
     * Constrains instance to the maximum height; do not allow resize in height. Also,
     * enforces minimum width for the operator.
     * 
     * @param left left
     * @param top top
     * @param width width
     * @param height height
     */
    public void setBoundingRect(int left, int top, int width, int height) {
        super.setBoundingRect(left, top, width, Math.min(height, getMaximumHeight()));
    }

    protected boolean geometryChangeChild(JGoObject child, Rectangle prevRect) {
        // do nothing as we do not want to listen to changes in children
        return true;
    }

    /**
     * Handles geometry change
     * 
     * @param prevRect previous bounds rectangle
     */
    protected void geometryChange(Rectangle prevRect) {
        // handle any size changes by repositioning all the items
        if (prevRect.width != getWidth() || prevRect.height != getHeight()) {
            layoutChildren();
        } else {
            super.geometryChange(prevRect);
        }
    }

    /**
     * Lays out this area's child objects.
     */
    public void layoutChildren() {
        columnRect.setBoundingRect(this.getBoundingRect());

        int rectleft = getLeft();
        int recttop = getTop();
        int rectwidth = getWidth();
        int rectheight = getHeight();

        int left = rectleft + insets.left;
        int top = recttop + insets.top;
        int width = rectwidth - insets.left - insets.right;
        int height = rectheight - insets.top - insets.bottom;

        titleArea.setBoundingRect(left, top, width, titleArea.getMinimumHeight());

        int aggrHeight = top + titleArea.getMinimumHeight();

        for (int i = 0; i < fieldList.size(); i++) {
            CanvasArea fieldNode = (CanvasArea) fieldList.get(i);

            if (aggrHeight < top + height) {
                fieldNode.setVisible(true);
                fieldNode.setBoundingRect(left, aggrHeight, width, fieldNode.getHeight() + verticalGap);
            } else {
                fieldNode.setVisible(false);
                fieldNode.setBoundingRect(left, top, width, fieldNode.getHeight() + verticalGap);
            }
            aggrHeight += fieldNode.getHeight() + verticalGap;
        }
    }

    /**
     * Gets field name associated with the given port
     * 
     * @param graphPort graph port
     * @return field name
     */
    public String getFieldName(IGraphPort graphPort) {
        for (int i = 0; i < fieldList.size(); i++) {
            IGraphFieldNode fieldNode = (IGraphFieldNode) fieldList.get(i);

            if (graphPort.equals(fieldNode.getLeftGraphPort()) || graphPort.equals(fieldNode.getRightGraphPort())) {
                return fieldNode.getName();
            }
        }
        return null;
    }

    /**
     * Gets input graph port, if any, associated with the given field name
     * 
     * @param fieldName field name
     * @return graph port
     */
    public IGraphPort getInputGraphPort(String fieldName) {

        if (fieldName == null) {
            return null;
        }

        for (int i = 0; i < fieldList.size(); i++) {
            IGraphFieldNode fieldNode = (IGraphFieldNode) fieldList.get(i);

            if (fieldName.equals(fieldNode.getName())) {
                return fieldNode.getLeftGraphPort();
            }
        }
        return null;
    }

    /**
     * Gets output graph port, if any, associated with the given field name
     * 
     * @param fieldName field name
     * @return graph port
     */
    public IGraphPort getOutputGraphPort(String fieldName) {
        return resultField.getRightGraphPort();
    }

    /**
     * Gets object, if any, associated with the given port
     * 
     * @param graphPort graph port
     * @return object for the given port
     */
    Object getObject(IGraphPort graphPort) {
        return null;
    }

    /**
     * Gets port, if any, associated with the given object
     * 
     * @param graphPort graph port
     * @return object for the given port
     */
    IGraphPort getGraphPort(Object obj) {
        return null;
    }

    /**
     * Gets a list of all input and output links
     * 
     * @return list of input links
     */
    public List getAllLinks() {
        ArrayList list = new ArrayList();

        for (int i = 0; i < fieldList.size(); i++) {
            IGraphFieldNode fieldNode = (IGraphFieldNode) fieldList.get(i);
            IGraphPort port = fieldNode.getLeftGraphPort();
            if (port != null) {
                addLinks(port, list);
            }
            port = fieldNode.getRightGraphPort();
            if (port != null) {
                addLinks(port, list);
            }
        }

        return list;
    }
}

