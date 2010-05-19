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
package org.netbeans.modules.edm.editor.ui.view.join;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.netbeans.modules.edm.model.SQLCondition;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLJoinOperator;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphPort;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphView;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorXmlInfoModel;
import org.netbeans.modules.edm.editor.graph.jgo.BasicCanvasArea;
import org.netbeans.modules.edm.editor.graph.jgo.BasicCellArea;
import org.netbeans.modules.edm.editor.graph.jgo.BasicComboBoxArea;
import org.netbeans.modules.edm.editor.graph.jgo.CanvasArea;
import org.netbeans.modules.edm.editor.graph.jgo.CellArea;
import org.netbeans.modules.edm.editor.graph.jgo.ColumnPortArea;
import org.netbeans.modules.edm.editor.graph.jgo.GraphPort;
import org.netbeans.modules.edm.editor.graph.jgo.TitleArea;
import org.netbeans.modules.edm.editor.ui.view.conditionbuilder.ConditionBuilderView;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoImage;
import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoView;
import org.netbeans.modules.edm.editor.ui.model.SQLUIModel;
import org.openide.util.NbBundle;

/**
 * This is the join representation in preview panel.
 * 
 * @author Ritesh Adval
 */
public class JoinPreviewGraphNode extends BasicCanvasArea {

    private JoinBottomArea bottomArea;

    private ColumnPortArea outputArea;

    private boolean showOutput = false;

    private int cbAreaHeight;

    private Vector<JoinType> joinTypes;

    private BasicComboBoxArea cbArea;

    private IGraphView mainSQLGraphView;

    private static URL no_condition_url = JoinPreviewGraphNode.class.getResource("/org/netbeans/modules/edm/editor/resources/no_condition.png");

    private static URL no_condition_hover_url = JoinPreviewGraphNode.class.getResource("/org/netbeans/modules/edm/editor/resources/no_condition_hover.png");

    private static URL system_condition_url = JoinPreviewGraphNode.class.getResource("/org/netbeans/modules/edm/editor/resources/system_condition.png");

    private static URL system_condition_hover_url = JoinPreviewGraphNode.class.getResource("/org/netbeans/modules/edm/editor/resources/system_condition_hover.png");

    private static URL modified_condition_url = JoinPreviewGraphNode.class.getResource("/org/netbeans/modules/edm/editor/resources/modified_condition.png");

    private static URL modified_condition_hover_url = JoinPreviewGraphNode.class.getResource("/org/netbeans/modules/edm/editor/resources/modified_condition_hover.png");

    /** Creates a new instance of JoinPreviewGraphNode */
    public JoinPreviewGraphNode() {
        initGUI();
    }

    private void initGUI() {
        this.setSelectable(true);
        this.setResizable(false);
        this.setPickableBackground(false);
        this.setUpdateGuiInfo(false);

        //add bounding rectangle
        //rect = new JGoRectangle();

        //add title
        titleArea = new TitleArea("join");
        titleArea.setInsets(new Insets(1, 3, 0, 3));

        URL url = JoinPreviewGraphNode.class.getResource("/org/netbeans/modules/edm/editor/resources/Join.png");

        ImageIcon joinTitleIcon = new ImageIcon(url);
        titleArea.setTitleImage(joinTitleIcon);
        titleArea.showExpansionImage(false);
        addObjectAtTail(titleArea);

        //add join bottom area
        bottomArea = new JoinBottomArea();
        bottomArea.setSelectable(false);
        bottomArea.setResizable(false);
        addObjectAtHead(bottomArea);

        //add output port if any
        outputArea = new ColumnPortArea(ColumnPortArea.RIGHT_PORT_AREA, 1);
        outputArea.setVisible(false);
        outputArea.setInsets(new Insets(0, 0, 0, 0));
        addObjectAtTail(outputArea);

        this.setSize(this.getMaximumWidth(), this.getMaximumHeight());
    }

    @Override
    public void setDataObject(Object obj) {
        super.setDataObject(obj);
        SQLJoinOperator op = (SQLJoinOperator) obj;
        int jConditionType = op.getJoinConditionType();

        if (jConditionType == SQLJoinOperator.SYSTEM_DEFINED_CONDITION) {
            this.bottomArea.setImage(new ImageIcon(system_condition_url));
            this.bottomArea.setConditionToolTip(NbBundle.getMessage(JoinPreviewGraphNode.class, "TOOLTIP_Click_To_Edit_System_Discovered_Condition"));
        } else if (jConditionType == SQLJoinOperator.USER_DEFINED_CONDITION) {
            this.bottomArea.setImage(new ImageIcon(modified_condition_url));
            this.bottomArea.setConditionToolTip(NbBundle.getMessage(JoinPreviewGraphNode.class, "TOOLTIP_Click_To_Edit_User_Modified_Condition"));
        } else {
            this.bottomArea.setImage(new ImageIcon(no_condition_url));
            this.bottomArea.setConditionToolTip(NbBundle.getMessage(JoinPreviewGraphNode.class, "TOOLTIP_Click_To_Edit_Condition"));
        }

        if (cbArea != null) {
            //set join type in combo box
            setJoinType(op.getJoinType());
        }
    }
    public void setModifiable(boolean b) {
        this.cbArea.setComboBoxEnabled(b);
    }
    private void setJoinType(int joinType) {
        Iterator it = joinTypes.iterator();
        while (it.hasNext()) {
            JoinType jt = (JoinType) it.next();
            if (jt.getJoinType() == joinType) {
                cbArea.setSelectedItem(jt);
            }
        }
    }

    /**
     * layout the children of this cell area
     */
    @Override
    public void layoutChildren() {
        int rectleft = getLeft();
        int recttop = getTop();
        int rectwidth = getWidth();

        int left = rectleft + insets.left;
        int top = recttop + insets.top;
        int width = rectwidth - insets.left - insets.right;

        titleArea.setBoundingRect(left, top, width, titleArea.getMinimumHeight());

        if (cbArea != null) {
            cbArea.setBoundingRect(left, top + titleArea.getHeight(), width, cbAreaHeight);

            cbAreaHeight = cbArea.getHeight();
        }

        if (cbArea != null) {
            bottomArea.setSpotLocation(JGoObject.TopLeft, cbArea, JGoObject.BottomLeft);
        } else {
            bottomArea.setSpotLocation(JGoObject.TopLeft, titleArea, JGoObject.BottomLeft);
        }

        if (showOutput) {
            this.outputArea.setVisible(true);
            this.outputArea.setSpotLocation(JGoObject.TopRight, bottomArea, JGoObject.BottomRight);
        } else {
            this.outputArea.setVisible(false);
        }
    }

    /**
     * get the minimum height of the area
     * 
     * @return minimum height
     */
    @Override
    public int getMaximumHeight() {
        int minHeight = 0;

        minHeight = getInsets().top + getInsets().bottom;

        minHeight += titleArea.getMinimumHeight();
        if (cbArea != null) {
            minHeight += cbArea.getMinimumHeight();
        }
        minHeight += bottomArea.getHeight();
        if (showOutput) {
            minHeight += outputArea.getMinimumHeight();
        }
        return minHeight;
    }

    @Override
    public int getMaximumWidth() {
        int maxWidth = 0;

        maxWidth = getInsets().left + getInsets().right;

        int width = 0;

        if (titleArea.getMinimumWidth() > width) {
            width = titleArea.getMinimumWidth();
        }

        if (bottomArea.getWidth() > width) {
            width = bottomArea.getWidth();
        }

        maxWidth += width;

        return maxWidth;
    }

    class JoinBottomArea extends JGoArea {

        private JoinCellArea leftArea;
        private JoinCellArea rightArea;
        private JoinConditionArea conditionArea;

        JoinBottomArea() {
            leftArea = new JoinCellArea("(L)");
            rightArea = new JoinCellArea("(R)");
            JGoArea leftRightArea = new JGoArea();
            leftRightArea.setSelectable(false);
            leftRightArea.setResizable(false);

            leftArea.setSpotLocation(JGoObject.Left, leftRightArea, JGoObject.Left);
            rightArea.setSpotLocation(JGoObject.TopLeft, leftArea, JGoObject.BottomLeft);

            leftRightArea.addObjectAtTail(leftArea);
            leftRightArea.addObjectAtTail(rightArea);

            addObjectAtTail(leftRightArea);

            //add condition Area
            conditionArea = new JoinConditionArea();
            conditionArea.setSpotLocation(JGoObject.TopLeft, leftRightArea, JGoObject.TopRight);
            conditionArea.setHeight(40);

            addObjectAtHead(conditionArea);
        }

        public IGraphPort getLeftGraphPort() {
            return leftArea.getGraphPort();
        }

        public IGraphPort getRightGraphPort() {
            return rightArea.getGraphPort();
        }

        public void setImage(Icon icon) {
            this.conditionArea.setImage(icon);
        }

        public void setConditionToolTip(String tTip) {
            this.conditionArea.setConditionToolTip(tTip);
        }
    }

    class JoinConditionArea extends CanvasArea {
        private JGoRectangle rect;

        //this area has one image also
        private ImageArea imageArea;

        private CellArea cell;

        JoinConditionArea() {
            this.setSelectable(true);
            this.setResizable(false);
            this.setDraggable(true);
            this.setPickableBackground(false);

            rect = new JGoRectangle();
            rect.setPen(JGoPen.makeStockPen(Color.lightGray));
            rect.setBrush(JGoBrush.makeStockBrush(Color.white));
            rect.setSelectable(false);
            rect.setResizable(false);
            addObjectAtHead(rect);

            //add image area
            imageArea = new ImageArea();
            addObjectAtTail(imageArea);

            //add condition cell
            cell = new CellArea(" Condition");
            this.addObjectAtTail(cell);

            //add image for testing
            this.setImage(new ImageIcon(no_condition_url));
        }

        public void setImage(Icon icon) {
            this.imageArea.setImage(icon);
            this.layoutChildren();
        }

        public void setConditionToolTip(String tTip) {
            this.setToolTipText(tTip);
        }

        /**
         * layout the children of this cell area
         */
        @Override
        public void layoutChildren() {
            Rectangle rectangle = this.getBoundingRect();
            rect.setBoundingRect(rectangle);
            cell.setBoundingRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height / 3);
            imageArea.setBoundingRect(rectangle.x, rectangle.y, rectangle.width, (rectangle.height * 2) / 3);
            cell.setSpotLocation(JGoObject.TopLeft, imageArea, JGoObject.BottomLeft);
        }

        @Override
        public boolean doMouseClick(int modifiers, java.awt.Point dc, java.awt.Point vc, JGoView aView) {
            return doMouseDblClick(modifiers, dc, vc, aView);
        }

        @Override
        public boolean doMouseDblClick(int modifiers, java.awt.Point dc, java.awt.Point vc, JGoView aView) {
            SQLJoinOperator join = (SQLJoinOperator) JoinPreviewGraphNode.this.getDataObject();
            if (join != null && mainSQLGraphView != null) {
                ConditionBuilderView conditionView = new ConditionBuilderView( (SQLUIModel)mainSQLGraphView,
                    join.getAllSourceTables(), join.getJoinCondition(), IOperatorXmlInfoModel.CATEGORY_FILTER);
                DialogDescriptor dd = new DialogDescriptor(conditionView, NbBundle.getMessage(JoinPreviewGraphNode.class, "LBL_Edit_Join_Condition"), true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);

                if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
                    SQLCondition cond = (SQLCondition) conditionView.getPropertyValue();
                    if (cond != null) {
                        SQLCondition oldCondition = join.getJoinCondition();
                        if (join != null && !cond.equals(oldCondition)) {
                            join.setJoinCondition(cond);
                            join.setJoinConditionType(SQLJoinOperator.USER_DEFINED_CONDITION);
                            this.setImage(new ImageIcon(modified_condition_url));
                        }
                    }
                }
                return true;
            }

            return false;
        }

        public boolean doMouseEntered(int modifiers, Point dc, Point vc, JGoView aView) {
            SQLJoinOperator join = (SQLJoinOperator) JoinPreviewGraphNode.this.getDataObject();
            if (join != null) {
                int conditonType = join.getJoinConditionType();
                ImageIcon imageIcon = null;
                switch (conditonType) {
                    case SQLJoinOperator.SYSTEM_DEFINED_CONDITION:
                        imageIcon = new ImageIcon(JoinPreviewGraphNode.system_condition_hover_url);
                        this.setImage(imageIcon);
                        break;
                    case SQLJoinOperator.USER_DEFINED_CONDITION:
                        imageIcon = new ImageIcon(JoinPreviewGraphNode.modified_condition_hover_url);
                        this.setImage(imageIcon);
                        break;
                    case SQLJoinOperator.NO_CONDITION:
                        imageIcon = new ImageIcon(JoinPreviewGraphNode.no_condition_hover_url);
                        this.setImage(imageIcon);
                        break;
                }

                return true;
            }

            return false;
        }

        public boolean doMouseExited(int modifiers, Point dc, Point vc, JGoView aView) {
            SQLJoinOperator join = (SQLJoinOperator) JoinPreviewGraphNode.this.getDataObject();
            if (join != null) {
                int conditonType = join.getJoinConditionType();
                ImageIcon imageIcon = null;
                switch (conditonType) {
                    case SQLJoinOperator.SYSTEM_DEFINED_CONDITION:
                        imageIcon = new ImageIcon(JoinPreviewGraphNode.system_condition_url);
                        this.setImage(imageIcon);
                        break;
                    case SQLJoinOperator.USER_DEFINED_CONDITION:
                        imageIcon = new ImageIcon(JoinPreviewGraphNode.modified_condition_url);
                        this.setImage(imageIcon);
                        break;
                    case SQLJoinOperator.NO_CONDITION:
                        imageIcon = new ImageIcon(JoinPreviewGraphNode.no_condition_url);
                        this.setImage(imageIcon);
                        break;
                }
                return true;
            }

            return false;
        }
    }

    class ImageArea extends CanvasArea {
        //image
        private JGoImage img;

        ImageArea() {
            this.setSelectable(false);
            this.setResizable(false);

            img = new JGoImage();
            img.setSelectable(false);
            img.setResizable(false);
            addObjectAtTail(img);
        }

        /**
         * set he image in this cell area
         * 
         * @param icon icon
         */
        public void setImage(Icon icon) {
            if (icon != null) {
                img.setVisible(true);
                ImageIcon imgIcon = (ImageIcon) icon;
                img.loadImage(imgIcon.getImage(), false);
                img.setSize(imgIcon.getImage().getWidth(null), imgIcon.getImage().getHeight(null));

                layoutChildren();
            } else {
                img.setVisible(false);
            }

        }

        /**
         * layout the children of this cell area
         */
        @Override
        public void layoutChildren() {
            Rectangle rectangle = this.getBoundingRect();
            img.setSpotLocation(JGoObject.Center, this, JGoObject.Center);
            img.setTop(rectangle.y + 2);
        }
    }

    //this class is used to render left and right port areas
    class JoinCellArea extends CanvasArea {

        private GraphPort port;
        private BasicCellArea cell;

        JoinCellArea(String text) {
            this.setSelectable(false);
            this.setResizable(false);
            this.setDraggable(true);

            //add port which will be hidden
            port = new GraphPort();
            port.setStyle(JGoPort.StyleHidden);

            //default port can be a source and destination of a link
            port.setValidSource(true);
            port.setValidDestination(true);
            this.addObjectAtTail(port);

            cell = new BasicCellArea(text);
            cell.setLeftGap(3);
            cell.setIconTextGap(0);
            cell.setInsets(new Insets(1, 0, 1, 0));
            cell.drawBoundingRect(true);
            cell.setBackGroundColor(new Color(254, 253, 235));

            this.addObjectAtTail(cell);

            this.setSize(22, 20);
        }

        /**
         * layout the children of this cell area
         */
        @Override
        public void layoutChildren() {
            Rectangle rectangle = this.getBoundingRect();
            //rect.setBoundingRect(rectangle);
            port.setBoundingRect(rectangle);
            cell.setBoundingRect(rectangle);
        }

        public IGraphPort getGraphPort() {
            return this.port;
        }
    }

    /**
     * get the field name for a port
     * 
     * @param iGraphPort port
     * @return field name
     */
    @Override
    public String getFieldName(IGraphPort iGraphPort) {
        IGraphPort graphPort;
        String fieldName = null;

        //check at left area
        graphPort = this.bottomArea.getLeftGraphPort();
        if (iGraphPort.equals(graphPort)) {
            fieldName = SQLJoinOperator.LEFT;
        }

        //check at right area of join
        graphPort = this.bottomArea.getRightGraphPort();
        if (iGraphPort.equals(graphPort)) {
            fieldName = SQLJoinOperator.RIGHT;
        }

        //check result area of join
        if (this.showOutput) {
            graphPort = outputArea.getPortAreaAt(0).getGraphPort();
            if (iGraphPort.equals(graphPort)) {
                SQLJoinOperator join = (SQLJoinOperator) this.getDataObject();
                if (join != null) {
                    fieldName = join.getDisplayName();
                }
            }
        }

        return fieldName;
    }

    /**
     * get the input port for a field name
     * 
     * @param str name of the field name
     * @return port
     */
    @Override
    public IGraphPort getInputGraphPort(String str) {
        IGraphPort graphPort = null;

        if (SQLJoinOperator.LEFT.equals(str)) {
            graphPort = this.bottomArea.getLeftGraphPort();
        } else if (SQLJoinOperator.RIGHT.equals(str)) {
            graphPort = this.bottomArea.getRightGraphPort();
        }

        return graphPort;
    }

    /**
     * get the output port for a field name
     * 
     * @param str field name
     * @return port
     */
    @Override
    public IGraphPort getOutputGraphPort(String str) {
        IGraphPort graphPort = null;
        //there is only one output graph port so no need to look on str
        graphPort = outputArea.getPortAreaAt(0).getGraphPort();

        return graphPort;
    }

    /**
     * get a list of all input and output links
     * 
     * @return list of input links
     */
    @Override
    public List getAllLinks() {
        ArrayList<JGoLink> list = new ArrayList<JGoLink>();
        IGraphPort port = null;
        port = this.bottomArea.getLeftGraphPort();
        addLinks(port, list);
        port = this.bottomArea.getRightGraphPort();
        addLinks(port, list);
        port = outputArea.getPortAreaAt(0).getGraphPort();
        addLinks(port, list);

        return list;
    }

    public void showOutputPort(boolean show) {
        this.showOutput = show;
        this.setSize(this.getMaximumWidth(), this.getMaximumHeight());
    }

    private class JoinType {
        int joinType;
        String strJoinType;

        public JoinType(int jType, String strJType) {
            this.joinType = jType;
            this.strJoinType = strJType;
        }

        public int getJoinType() {
            return this.joinType;
        }

        @Override
        public String toString() {
            return this.strJoinType;
        }
    }

    class CBItemListener implements ItemListener {

        /**
         * Invoked when an item has been selected or deselected by the user. The code
         * written for this method performs the operations that need to occur when an item
         * is selected (or deselected).
         */
        public void itemStateChanged(ItemEvent e) {
            JoinType jt = (JoinType) e.getItem();
            SQLJoinOperator join = (SQLJoinOperator) JoinPreviewGraphNode.this.getDataObject();
            if (join != null) {
                join.setJoinType(jt.getJoinType());
            }
        }

    }

    public void addJoinTypeComboBox() {
        //add join type
        joinTypes = new Vector<JoinType>();
        JoinType jt1 = new JoinType(SQLConstants.INNER_JOIN, "Inner");
        joinTypes.add(jt1);
        JoinType jt2 = new JoinType(SQLConstants.LEFT_OUTER_JOIN, "Left Outer");
        joinTypes.add(jt2);
        JoinType jt3 = new JoinType(SQLConstants.RIGHT_OUTER_JOIN, "Right Outer");
        joinTypes.add(jt3);
        JoinType jt4 = new JoinType(SQLConstants.FULL_OUTER_JOIN, "Full Outer");
        joinTypes.add(jt4);

        cbArea = new BasicComboBoxArea(null, joinTypes);
        cbArea.addItemListener(new CBItemListener());
        this.addObjectAtTail(cbArea);
        cbAreaHeight = cbArea.getMinimumHeight();
        this.setSize(this.getMaximumWidth(), this.getMaximumHeight());
    }

    public void setMainSQLGraphView(IGraphView gView) {
        this.mainSQLGraphView = gView;
    }

}

