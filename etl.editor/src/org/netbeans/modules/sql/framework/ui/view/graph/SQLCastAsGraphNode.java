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
package org.netbeans.modules.sql.framework.ui.view.graph;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.model.SQLCastOperator;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.IHighlightConfigurator;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorField;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicCellArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.CanvasArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.HighlightConfiguratorImpl;
import org.netbeans.modules.sql.framework.ui.graph.impl.OperatorGraphFieldNode;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SQLCastAsGraphNode extends SQLOperatorGraphNode implements PropertyChangeListener {

    private static final URL propertiesUrl = SQLCastAsGraphNode.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/properties.png");
    
    /* contains literal value + link node */
    private OperatorGraphFieldNode valueNode;
    
    private JMenuItem propsItem;

    private static final JGoBrush BRUSH_OUTPUT_HIGHLIGHTED = SQLOperatorGraphNode.BRUSH_INPUT_HIGHLIGHTED;

    /**
     * Creates a new instance of SQLLiteralGraphNode, using the given visual configuration
     * information.
     * 
     * @param info GUI configuration information
     */
    public SQLCastAsGraphNode(IOperatorXmlInfo info) {
        super(info);
    }

    /**
     * Initializes the UI look and feel based on the given visual configuration
     * information.
     * 
     * @param info GUI configuration information
     */
    protected void initialize(IOperatorXmlInfo info) {
        // Collect lists of input graph and parameter fields.
        for (int i = 0; i < info.getInputCount(); i++) {
            IOperatorField field = (IOperatorField) info.getInputFields().get(i);
            
            // Ignore static field input - type input and precision/scale attributes are set via dialog.
            if (!field.isStatic()) {
                OperatorGraphFieldNode fieldNode = new OperatorGraphFieldNode(BasicCellArea.LEFT_PORT_AREA, field);
                fieldNode.setBrush(BRUSH_INPUT_REGULAR);
                fieldNode.setTextColor(TEXT_COLOR_INPUT);
                fieldNode.setLinePen(PEN_DEFAULT);
                fieldNode.setHighlightEnabled(true);
                fieldNode.getHighlightConfigurator().setHoverBrush(BRUSH_INPUT_HIGHLIGHTED);
                
                fieldList.add(fieldNode);
            }
        }

        Iterator iter = fieldList.iterator();
        while (iter.hasNext()) {
            CanvasArea fieldNode = (CanvasArea) iter.next();
            this.addObjectAtTail(fieldNode);
        }        
        
        //add one output graph field
        for (int i = 0; i < info.getOutputCount(); i++) {
            IOperatorField field = (IOperatorField) info.getOutputFields().get(i);

            valueNode = new CastTypeFieldNode(BasicCellArea.RIGHT_PORT_AREA, field);
            valueNode.addPropertyChangeListener(this);
            valueNode.setTextAlignment(JGoText.ALIGN_CENTER);
            valueNode.setLinePen(JGoPen.makeStockPen(Color.WHITE));
            
            valueNode.setTextColor(TEXT_COLOR_LITERAL);
            valueNode.setTextEditable(false);
            valueNode.setHighlightEnabled(true);

            this.addObjectAtTail(valueNode);
            fieldList.add(valueNode);
        }

        this.setResizable(true);
    }

    /**
     * Extends OperatorGraphFieldNode to allow for activation of properties dialog via
     * double-click mouse gesture.
     * 
     * @author Jonathan Giron
     * @version $Revision$
     */
    class CastTypeFieldNode extends OperatorGraphFieldNode {
        public CastTypeFieldNode(int type, IOperatorField field) {
            super(type, field);
        }

        /**
         * Overrides parent implementation to display CastAsDialog.
         * 
         * @see com.nwoods.jgo.JGoObject#doMouseDblClick(int, java.awt.Point, java.awt.Point, com.nwoods.jgo.JGoView)
         */
        public boolean doMouseDblClick(int modifiers, Point dc, Point vc, JGoView aView) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    SQLCastOperator castOp = (SQLCastOperator) dataObject;
                    if (castOp != null && isEditable(castOp.getJdbcType())) {
                        SQLCastAsGraphNode.this.showCastAsDialog();
                    }                    
                }
            });
            return true;
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.sql.framework.ui.view.graph.SQLOperatorGraphNode#initializePopUpMenu()
     */
    protected void initializePopUpMenu() {
        super.initializePopUpMenu();
        
        // Properties menu
        popUpMenu.addSeparator();
        
        propsItem = new JMenuItem(NbBundle.getMessage(SQLCastAsGraphNode.class, "LBL_properties"), new ImageIcon(propertiesUrl));
        propsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SQLCastAsGraphNode.this.showCastAsDialog();
            }
        });
        popUpMenu.add(propsItem);
    }
    
    /**
     * @param jdbcType
     * @return
     */
    private boolean isEditable(int jdbcType) {
        return SQLUtils.isPrecisionRequired(jdbcType);
    }

    /**
     * Gets maximum height
     * 
     * @return max height
     */
    public int getMaximumHeight() {
        return super.getMaximumHeight();
    }

    /**
     * @see org.netbeans.modules.sql.framework.ui.graph.ICanvasInterface#getMaximumWidth()
     */
    public int getMaximumWidth() {
        // Establish max width of 'literal' label, other components in parent, versus type
        // label.
        return Math.max(super.getMaximumWidth(), valueNode.getMaximumWidth());
    }
    
    /**
     * @see org.netbeans.modules.sql.framework.ui.graph.ICanvasInterface#getMinimumWidth()
     */
    public int getMinimumWidth() {
        return Math.max(super.getMinimumWidth(), valueNode.getMinimumWidth());
    }    

    /**
     * Gets output graph port, given its field name.
     * 
     * @param fieldName field name
     * @return graph port
     */
    public IGraphPort getOutputGraphPort(String fieldName) {
        return valueNode.getRightGraphPort();
    }

    /**
     * Sets data object for which this UI element provides a view.
     * 
     * @param obj data object
     */
    public void setDataObject(Object obj) {
        this.dataObject = obj;
        
        updateValueNode();
    }

    /**
     * layout the children
     */
    public void layoutChildren() {
        Rectangle boundingRect = new Rectangle(this.getBoundingRect());
        
        int minLayoutWidth = getMinimumWidth();
        if (boundingRect.width < minLayoutWidth || !isExpandedState()) {
            boundingRect.width = minLayoutWidth;
            setBoundingRect(boundingRect);
        }
        
        columnRect.setBoundingRect(boundingRect);
        
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
            OperatorGraphFieldNode fieldNode = (OperatorGraphFieldNode) fieldList.get(i);

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
     * Handles change to a bound property as indicated by the given event.
     * 
     * @param evt A PropertyChangeEvent object describing the event source and the
     *        property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
    }
    
    private SQLCastOperator getCastOperator() {
        return (SQLCastOperator) dataObject;
    }
    
    private void updateValueNode() {
        SQLCastOperator castOp = getCastOperator();
        IHighlightConfigurator hc = new HighlightConfiguratorImpl(BRUSH_OUTPUT_REGULAR, BRUSH_OUTPUT_HIGHLIGHTED);
        Color textColor = TEXT_COLOR_LITERAL;
        
        if (castOp != null) {
            try {
                int jdbcType = castOp.getJdbcType();
                int precision = castOp.getPrecision();
                int scale = castOp.getScale(); 
                    
                String sqlTypeStr = SQLUtils.getStdSqlType(jdbcType);
                StringBuffer labelBuffer = new StringBuffer(sqlTypeStr);
                
                if (SQLUtils.isPrecisionRequired(jdbcType)) {
                    labelBuffer.append("(");
                    labelBuffer.append(precision);
                    if (SQLUtils.isScaleRequired(jdbcType) && scale > 0) {
                        labelBuffer.append(",").append(scale);
                    }
                    labelBuffer.append(")");
                } else {
                    hc.setHoverBrush(BRUSH_OUTPUT_REGULAR);
                    textColor = TEXT_COLOR_RESULT;
                }
                
                valueNode.setText(labelBuffer.toString());
                valueNode.setTextColor(textColor);
                valueNode.setHighlightConfigurator(hc);
            } catch (IllegalArgumentException ignore) {
                final String typeLabel = NbBundle.getMessage(SQLCastAsGraphNode.class, "LBL_unknown_type");
                valueNode.setText(typeLabel);
            }
            
            if (propsItem != null) {
                propsItem.setEnabled(isEditable(castOp.getJdbcType()));
            }
        }

        this.layoutChildren();
    }
    
    public void showCastAsDialog() {
        CastAsDialog castDlg = new CastAsDialog(WindowManager.getDefault().getMainWindow(), 
            NbBundle.getMessage(SQLGraphController.class, "TITLE_edit_castas"), true);
        
        SQLCastOperator castOp = (SQLCastOperator) dataObject;
        
        castDlg.setJdbcType(castOp.getJdbcType());
        castDlg.setJdbcTypeEditable(false);
        castDlg.setPrecision(castOp.getPrecision());
        castDlg.setScale(castOp.getScale());
        
        castDlg.show();
        if (castDlg.isCanceled()) {
            return;
        }
    
        castOp.setJdbcType(castDlg.getJdbcType());
        castOp.setPrecision(castDlg.getPrecision());
        castOp.setScale(castDlg.getScale());
        
        updateValueNode();
    }
}

