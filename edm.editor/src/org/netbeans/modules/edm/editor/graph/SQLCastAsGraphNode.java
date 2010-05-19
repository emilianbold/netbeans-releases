/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.edm.editor.graph;

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

import org.netbeans.modules.edm.model.SQLCastOperator;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphPort;
import org.netbeans.modules.edm.editor.graph.jgo.IHighlightConfigurator;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorField;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorXmlInfo;
import org.netbeans.modules.edm.editor.graph.jgo.BasicCellArea;
import org.netbeans.modules.edm.editor.graph.jgo.CanvasArea;
import org.netbeans.modules.edm.editor.graph.jgo.HighlightConfiguratorImpl;
import org.netbeans.modules.edm.editor.graph.jgo.OperatorGraphFieldNode;
import org.openide.windows.WindowManager;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;
import org.netbeans.modules.edm.editor.utils.SQLUtils;
import org.openide.util.NbBundle;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SQLCastAsGraphNode extends SQLOperatorGraphNode implements PropertyChangeListener {

    private static final URL propertiesUrl = SQLCastAsGraphNode.class.getResource("/org/netbeans/modules/edm/editor/resources/properties.png");
    
    
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


    public int getMaximumWidth() {
        // Establish max width of 'literal' label, other components in parent, versus type
        // label.
        return Math.max(super.getMaximumWidth(), valueNode.getMaximumWidth());
    }

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
                StringBuilder labelBuffer = new StringBuilder(sqlTypeStr);
                
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
                final String typeLabel = "Unknown";
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
            NbBundle.getMessage(SQLCastAsGraphNode.class, "TITLE_edit_castas"), true);
        
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

