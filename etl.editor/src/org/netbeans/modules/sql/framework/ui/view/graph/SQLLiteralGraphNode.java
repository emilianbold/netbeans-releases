/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.sql.framework.ui.view.graph;

import java.awt.Color;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.sql.Types;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.netbeans.modules.sql.framework.model.VisibleSQLLiteral;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorField;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicCellArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.CanvasArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.OperatorGraphFieldNode;


import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoText;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * Graphical representation of literal element.
 * 
 * @author Ritesh Adval
 * @author Jonathan Giron
 * @version $Revision$
 */
public class SQLLiteralGraphNode extends SQLOperatorGraphNode implements PropertyChangeListener {

    /* contains literal value + link node */
    private OperatorGraphFieldNode valueNode;

    private static final URL URL_NUMBER_ICON = SQLLiteralGraphNode.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/Math.png");
    private static final URL URL_DATE_ICON = SQLLiteralGraphNode.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/NOW2.png");
    private static final URL URL_TEXT_ICON = SQLLiteralGraphNode.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/literal.png");
    private static transient final Logger mLogger = Logger.getLogger(SQLLiteralGraphNode.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private static Icon numberIcon;

    private static Icon dateIcon;

    private static Icon textIcon;

    /**
     * Creates a new instance of SQLLiteralGraphNode, using the given visual configuration
     * information.
     * 
     * @param info GUI configuration information
     */
    public SQLLiteralGraphNode(IOperatorXmlInfo info) {
        super(info);
    }

    /**
     * Initializes the UI look and feel based on the given visual configuration
     * information.
     * 
     * @param info GUI configuration information
     */
    protected void initialize(IOperatorXmlInfo info) {
        //add one output graph field
        for (int i = 0; i < info.getOutputCount(); i++) {
            IOperatorField field = (IOperatorField) info.getOutputFields().get(i);

            valueNode = new OperatorGraphFieldNode(BasicCellArea.RIGHT_PORT_AREA, field);
            valueNode.addPropertyChangeListener(this);
            valueNode.setTextAlignment(JGoText.ALIGN_CENTER);
            valueNode.setLinePen(JGoPen.makeStockPen(Color.WHITE));
            valueNode.setBrush(BRUSH_OUTPUT_REGULAR);
            valueNode.setTextColor(TEXT_COLOR_LITERAL);

            this.addObjectAtTail(valueNode);
            fieldList.add(valueNode);
        }

        this.setResizable(true);
    }

    /**
     * get maximum height
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
        valueNode.setText(((VisibleSQLLiteral) obj).getValue());

        SQLLiteral lit = (SQLLiteral) obj;

        if (lit != null) {
            try {
                int jdbcType = lit.getJdbcType();
                switch (jdbcType) {
                    case Types.NUMERIC:
                    case Types.FLOAT:
                    case Types.DOUBLE:
                    case Types.INTEGER:
                        titleArea.setTitleImage(getNumberIcon());
                        break;
                        
                    case Types.TIME:
                    case Types.TIMESTAMP:
                        titleArea.setTitleImage(getDateIcon());
                        break;
                        
                    case Types.VARCHAR:
                    case Types.CHAR:
                    default:
                        titleArea.setTitleImage(getTextIcon());
                        break;
                }
                
                titleArea.setTitle(SQLUtils.getStdSqlType(jdbcType));
            } catch (IllegalArgumentException ignore) {
                String nbBundle1 = mLoc.t("BUND447: SQL type:");
                final String typeLabel = nbBundle1.substring(15);
                titleArea.setTitle(typeLabel);
            }
        }

        this.layoutChildren();
    }
    
    private static Icon getNumberIcon() {
        if (numberIcon == null) {
            numberIcon = new ImageIcon(URL_NUMBER_ICON);
        }
        
        return numberIcon;
    }
    
    private static Icon getDateIcon() {
        if (dateIcon == null) {
            dateIcon = new ImageIcon(URL_DATE_ICON);
        }
        
        return dateIcon;
    }
    
    private static Icon getTextIcon() {
        if (textIcon == null) {
            textIcon = new ImageIcon(URL_TEXT_ICON);            
        }
        
        return textIcon;
    }

    /**
     * layout the children
     */
    public void layoutChildren() {
        Rectangle boundingRect = new Rectangle(this.getBoundingRect());
        
        int minLayoutWidth = getMinimumWidth();
        if (boundingRect.width < minLayoutWidth || !isExpandedState()) {
            boundingRect.width = minLayoutWidth;
            this.setBoundingRect(boundingRect);
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
        if (evt.getPropertyName().equals(BasicCellArea.TEXT)) {
            VisibleSQLLiteral lit = (VisibleSQLLiteral) this.getDataObject();
            String newValue = (String) evt.getNewValue();
            
            if (lit != null) { 
                final String errorMsg = LiteralDialog.evaluateIfLiteralValid(newValue, lit.getJdbcType());
                if (errorMsg == null) {
                    lit.setValue((String) evt.getNewValue());
                } else {
                    valueNode.setText((String) evt.getOldValue());
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            LiteralDialog.showMessage(errorMsg);
                        }
                    });
                }
                
                setSize(getMaximumWidth(), getMaximumHeight());
            } else {
                valueNode.setText((String) evt.getOldValue());
            }
        }
    }

    class BasicTypeArea extends CanvasArea {
        private BasicCellArea typeArea1;
        private BasicCellArea typeValueArea;
        private JGoRectangle rect;

        BasicTypeArea(String type, String typeValue) {
            super();
            this.setSelectable(false);
            this.setResizable(false);

            rect = new JGoRectangle();
            rect.setPen(SQLOperatorGraphNode.PEN_DEFAULT);
            rect.setBrush(SQLOperatorGraphNode.BRUSH_INPUT_REGULAR);
            rect.setSelectable(false);
            rect.setResizable(false);
            addObjectAtHead(rect);

            typeArea1 = new BasicCellArea(type);
            typeArea1.setTextAlignment(JGoText.ALIGN_CENTER);
            typeArea1.setLinePen(SQLOperatorGraphNode.PEN_DEFAULT);
            typeArea1.setBrush(SQLOperatorGraphNode.BRUSH_INPUT_REGULAR);
            typeArea1.setTextColor(TEXT_COLOR_INPUT);
            this.addObjectAtTail(typeArea1);

            typeValueArea = new BasicCellArea(typeValue);
            typeValueArea.setTextAlignment(JGoText.ALIGN_CENTER);
            typeValueArea.setLinePen(SQLOperatorGraphNode.PEN_DEFAULT);
            typeValueArea.setBrush(SQLOperatorGraphNode.BRUSH_INPUT_REGULAR);
            typeValueArea.setTextColor(TEXT_COLOR_INPUT);
            this.addObjectAtTail(typeValueArea);

            this.setSize(getMaximumWidth(), getMaximumHeight());
        }

        public int getMaximumHeight() {
            int h = this.insets.top + this.insets.bottom;

            if (typeArea1 != null) {
                h += typeArea1.getMaximumHeight();
            }

            if (typeValueArea != null) {
                h += typeValueArea.getHeight();
            }

            return h;
        }

        public int getMaximumWidth() {
            int w = this.insets.left + this.insets.right;

            int width = 0;

            if (typeArea1 != null) {
                width = typeArea1.getMaximumWidth();
            }

            if (typeValueArea != null && typeValueArea.getWidth() > width) {
                width = typeValueArea.getWidth();
            }

            w += width;

            return w;
        }

        public void layoutChildren() {
            rect.setBoundingRect(this.getBoundingRect());

            int rectleft = getLeft();
            int recttop = getTop();
            int rectwidth = getWidth();

            int left = rectleft + insets.left;
            int top = recttop + insets.top;
            int width = rectwidth - insets.left - insets.right;

            typeArea1.setBoundingRect(left, top, width, typeArea1.getHeight());

            typeValueArea.setBoundingRect(left, top + typeArea1.getHeight(), width, typeValueArea.getHeight());

        }

        public void setValueType(String vType) {
            typeValueArea.setText(vType);
        }
    }
}

