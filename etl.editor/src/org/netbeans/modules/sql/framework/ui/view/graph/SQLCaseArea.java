/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.netbeans.modules.sql.framework.model.SQLCaseOperator;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SQLWhen;
import org.netbeans.modules.sql.framework.ui.graph.ICommand;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.IHighlightConfigurator;
import org.netbeans.modules.sql.framework.ui.graph.IHighlightable;
import org.netbeans.modules.sql.framework.ui.graph.ListAreaCellRenderer;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicCanvasArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicCellArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicImageArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicListArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.CellArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.DefaultListAreaRenderer;
import org.netbeans.modules.sql.framework.ui.graph.impl.HighlightConfiguratorImpl;
import org.netbeans.modules.sql.framework.ui.graph.impl.ListArea;
import org.netbeans.modules.sql.framework.ui.model.SQLUIModel;
import org.netbeans.modules.sql.framework.ui.view.IGraphViewContainer;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderUtil;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderView;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoSelection;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;
import com.sun.etl.exception.BaseException;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * This class represents a case area build on top of list area
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SQLCaseArea extends BasicListArea {
    
    protected static final JGoBrush BRUSH_OUTPUT_REGULAR = JGoBrush.makeStockBrush(new Color(236, 215, 215)); // pink
    
    protected static final JGoBrush BRUSH_INPUT_REGULAR = JGoBrush.makeStockBrush(new Color(240, 240, 240)); // light gray
    
    protected static final JGoBrush BRUSH_INPUT_HIGHLIGHTED = JGoBrush.makeStockBrush(new Color(254, 254, 244)); // light beige    
    
    protected static final Color TEXT_COLOR_RESULT = Color.BLACK;
    
    protected static final Color TEXT_COLOR_INPUT = new Color(100, 100, 90); // gray
    
    protected static final Color TEXT_COLOR_LITERAL = new Color(30, 70, 230); // navy    

    private static final JGoPen PEN_DEFAULT = JGoPen.makeStockPen(Color.WHITE);
    
    private  final String nbBundle1 = mLoc.t("BUND432: condition");
    
    String nbBundle2 = mLoc.t("BUND433: invalid condition");
    
    private final String VALID_CONDITION = nbBundle1.substring(15);
    
    private  final String INVALID_CONDITION = nbBundle2.substring(15);
    
    private static final URL caseUrl = SQLCaseArea.class
        .getResource("/org/netbeans/modules/sql/framework/ui/resources/images/Case.png");

    private static final URL showSqlUrl = SQLCaseArea.class
        .getResource("/org/netbeans/modules/sql/framework/ui/resources/images/Show_Sql.png");

    private static final URL removeUrl = SQLCaseArea.class
        .getResource("/org/netbeans/modules/sql/framework/ui/resources/images/remove.png");

    private static final URL editUrl = SQLCaseArea.class
        .getResource("/org/netbeans/modules/sql/framework/ui/resources/images/Comparison.png");

    private static final URL invalidIconUrl = SQLCaseArea.class
        .getResource("/org/netbeans/modules/sql/framework/ui/resources/images/Error.png");

    private static final URL validIconUrl = SQLCaseArea.class
        .getResource("/org/netbeans/modules/sql/framework/ui/resources/images/validateField.png");
    
    private static ImageIcon titleImage;
    
    private BasicCellArea.Highlightable defaultArea;
    private BasicCellArea resultArea;
    private SQLCaseOperator caseObj;

    private Action removeWhenAction;
    
    private JMenuItem showSqlItem;
    private JMenuItem removeItem;
    
    private JPopupMenu whenPopup;
    
    private ImageIcon invalidIcon = null;
    private ImageIcon validIcon = null;
    
    private static transient final Logger mLogger = Logger.getLogger(SQLCaseArea.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    
    /** Creates a new instance of SQLCaseArea */
    public SQLCaseArea() {
        super("case");
        this.setSelectable(true);

        // This case area is no longer resizable but we may change it in future
        this.setResizable(false);

        // Case-when does not grab child selections; this is to allow case areas to be
        // selectable

        if (titleImage == null) {
            titleImage = new ImageIcon(caseUrl);
        }
        this.getTitleArea().setTitleImage(titleImage);
        titleArea.setPen(PEN_DEFAULT);
        titleArea.setBrush(BRUSH_TITLE);

        listArea = new ListArea();
        listArea.setDrawLines(false);
        listArea.setVerticalSpacing(0);
        listArea.setShowScrollBar(false);

        // Set the renderer for the list area
        listArea.setCellRenderer(new CaseWhenRenderer());

        CaseListModel listModel = new CaseListModel();
        RemoveWhenListener whenlistener = new RemoveWhenListener(listModel);
        whenlistener.update();

        listArea.setModel(listModel);
        this.addObjectAtTail(listArea);

        //add the default area for the case
        defaultArea = new BasicCellArea.Highlightable(BasicCellArea.LEFT_PORT_AREA, "default");
        defaultArea.setTextAlignment(JGoText.ALIGN_CENTER);
        defaultArea.drawBoundingRect(true);
        
        defaultArea.setBrush(BRUSH_INPUT_REGULAR);
        defaultArea.getHighlightConfigurator().setHoverBrush(BRUSH_INPUT_HIGHLIGHTED);
        defaultArea.setLinePen(PEN_DEFAULT);
        defaultArea.setTextColor(TEXT_COLOR_INPUT);
        this.addObjectAtTail(defaultArea);

        //add the result are for the case
        resultArea = new BasicCellArea(BasicCellArea.RIGHT_PORT_AREA, "result");
        resultArea.setTextAlignment(JGoText.ALIGN_CENTER);
        resultArea.drawBoundingRect(true);
        resultArea.setBrush(BRUSH_OUTPUT_REGULAR);
        resultArea.setLinePen(PEN_DEFAULT);
        resultArea.setTextColor(TEXT_COLOR_RESULT);
        this.addObjectAtTail(resultArea);

        initializePopUpMenus();
    }

    /**
     * Creates a new instance of SQLCaseArea using the given data array.
     * 
     * @param data array of objects to be represented in ListModel
     */
    public SQLCaseArea(Object[] data) {
        super("Case", data);

        resultArea = new BasicCellArea(BasicCellArea.RIGHT_PORT_AREA, "result");
        resultArea.setLeftGap(20);
        resultArea.drawBoundingRect(true);
        resultArea.setResizable(false);
        this.addObjectAtTail(resultArea);
    }

    /**
     * Adds a condition in this area
     * 
     * @param val object representing a condition
     */
    public void setCondition(Object val) {
        SQLPredicate sqlPredicate = (SQLPredicate) val;

        // If no condition is set then return
        if (sqlPredicate == null) {
            return;
        }

        try {
            SQLWhen sqlWhen = caseObj.getWhen(sqlPredicate.getDisplayName());
            if (sqlPredicate != null) {
                sqlWhen.addInput(SQLWhen.CONDITION, sqlPredicate);
            }
        } catch (BaseException ex) {
            ex.printStackTrace();
        }
    }

    private void addPredicateToModel(Object val, Point loc) {
        CaseListModel model = (CaseListModel) listArea.getModel();

        if (loc != null) {
            Object data = listArea.getValueAt(loc);
            model.add(model.indexOf(data), val);
        } else {
            model.add(val);
        }
    }

    /**
     * Gets the input port for the given fieldName
     * 
     * @param fieldName name of the field
     * @return port for the fieldName
     */
    public IGraphPort getInputGraphPort(String fieldName) {
        if (caseObj != null && fieldName.equals(SQLCaseOperator.DEFAULT)) {
            return defaultArea.getLeftGraphPort();
        }

        return null;
    }

    /**
     * Gets the output graph port for the given fieldName
     * 
     * @param fieldName the name of this field
     * @return port that belongs to fieldName
     */
    public IGraphPort getOutputGraphPort(String fieldName) {
        return resultArea.getRightGraphPort();
    }

    /**
     * Gets a list of all input and output links
     * 
     * @return list of input links
     */
    public List getAllLinks() {
        ArrayList list = new ArrayList();
        IGraphPort port = null;

        // Add the link of default area
        port = defaultArea.getLeftGraphPort();
        addLinks(port, list);

        // Add the link of the list area
        for (int i = 0; i < listArea.getModel().getSize(); i++) {
            WhenArea wArea = (WhenArea) listArea.getCellRendererComponent(i);
            list.addAll(wArea.getAllLinks());
        }

        // Add the link of the result area
        port = resultArea.getRightGraphPort();
        addLinks(port, list);

        return list;
    }

    /**
     * Gets the field name for the given port
     * 
     * @param graphPort port
     * @return field which has graphPort
     */
    public String getFieldName(IGraphPort graphPort) {
        if (graphPort.equals(resultArea.getRightGraphPort())) {
            return resultArea.getText();
        }

        //check if it is connected to default area
        IGraphPort port = defaultArea.getLeftGraphPort();
        if (graphPort.equals(port) && caseObj != null) {
            return SQLCaseOperator.DEFAULT;
        }

        return null;
    }

    /**
     * get the child graphNode
     * 
     * @param obj child data object
     * @return graph node
     */
    public IGraphNode getChildNode(Object obj) {
        for (int i = 0; i < listArea.getModel().getSize(); i++) {
            Object whenObj = listArea.getModel().getElementAt(i);
            if (whenObj.equals(obj)) {
                return (IGraphNode) listArea.getCellRendererComponent(i);
            }
        }

        return null;
    }

    /**
     * Remove a child object
     * 
     * @param child child object
     */
    public void removeChildNode(IGraphNode childNode) throws Exception {
        Object dataObj = childNode.getDataObject();
        if (dataObj != null) {
            CaseListModel model = (CaseListModel) listArea.getModel();
            if (model.remove(dataObj)) {
                //remove from case also
                caseObj.removeSQLWhen((SQLWhen) dataObj);

                //a case is removed so make model dirty
                ((SQLUIModel) this.getGraphView().getGraphModel()).setDirty(true);

                //layout children again
                this.setHeight(this.getMaximumHeight());
            } else {
                String nbBundle3 = mLoc.t("BUND434: Cannot remove selected when condition - at least one condition must be defined.");
                String msg = nbBundle3.substring(15);
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        }
    }

    // overridden to get the visible row heights
    // this not used as of now as this case area is no longer scrollable
    protected int getVisibleRowHeights() {
        int visHeight = super.getVisibleRowHeights();
        if (defaultArea != null) {
            visHeight += defaultArea.getHeight();
        }

        if (resultArea != null) {
            visHeight += resultArea.getHeight();
        }

        return visHeight;
    }

    /**
     * Sets the layout of this area's children
     */
    public void layoutChildren() {
        Insets insets1 = getInsets();

        //get the bounding rectangle of this table area
        int x = getLeft() + insets1.left;
        int y = getTop() + insets1.top;
        int width = getWidth() - insets1.left - insets1.right;
        int height = getHeight() - insets1.top - insets1.bottom;

        titleArea.setBoundingRect(x, y, width, titleArea.getMinimumHeight());

        if (defaultArea == null || resultArea == null) {
            return;
        }

        if (height - titleArea.getHeight() - defaultArea.getHeight() - resultArea.getHeight() > 0) {
            listArea.setVisible(true);
            listArea.setOutOfScrollCellBounds(titleArea.getBoundingRect());
            listArea.setBoundingRect(x, y + titleArea.getHeight(), width, height - titleArea.getHeight() - defaultArea.getHeight()
                - resultArea.getHeight());
        } else {
            listArea.setVisible(false);
            listArea.setOutOfScrollCellBounds(titleArea.getBoundingRect());
            listArea.setBoundingRect(titleArea.getLocation(), new Dimension(titleArea.getWidth(), 0));
        }

        if (height - titleArea.getHeight() - listArea.getHeight() > 0) {
            defaultArea.setVisible(true);
            defaultArea.setBoundingRect(x, y + titleArea.getHeight() + listArea.getHeight(), width, defaultArea.getHeight());

        } else {
            defaultArea.setVisible(false);
            defaultArea.setLocation(titleArea.getLocation());
        }

        if (height - titleArea.getHeight() - listArea.getHeight() - defaultArea.getHeight() > 0) {
            resultArea.setVisible(true);
            resultArea.setBoundingRect(x, y + titleArea.getHeight() + listArea.getHeight() + defaultArea.getHeight(), width, resultArea.getHeight());

        } else {
            resultArea.setVisible(false);
            resultArea.setLocation(titleArea.getLocation());
        }
    }

    /**
     * Gets the data object from this area.
     * 
     * @return data object
     */
    public Object getDataObject() {
        return caseObj;
    }

    /**
     * Sets the data object in this area
     * 
     * @param obj data object
     */
    public void setDataObject(Object obj) {
        try {
            this.caseObj = (SQLCaseOperator) obj;
            //create one when by default
            if (caseObj.getWhenCount() == 0) {
                SQLWhen when = caseObj.createSQLWhen();
                caseObj.addSQLWhen(when);
            }

            setWhens(caseObj);
        } catch (BaseException ex) {
            ex.printStackTrace();
        }
    }

    private void setWhens(SQLCaseOperator sqlCase) {
        List list = sqlCase.getWhenList();
        if (list == null) {
            return;
        }

        Iterator it = list.iterator();

        while (it.hasNext()) {
            SQLWhen when = (SQLWhen) it.next();
            addPredicateToModel(when, null);
        }
    }

    /**
     * Adds a model object holding the predicate
     * 
     * @param obj the object that represents a predicate
     * @param loc location of the new predicate
     */
    public void addPredicate(Object obj, Point loc) {
        //add(obj, loc);
    }

    class CaseListArea extends ListArea {
        public ListAreaCellRenderer getCellRenderer(int row) {
            return new CaseWhenRenderer();
        }
    }

    class DefaultCaseRenderer extends DefaultListAreaRenderer {
        public JGoObject getListAreaCellRenderer(ListArea list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            JGoObject aCellArea = super.getListAreaCellRenderer(list, value, index, isSelected, cellHasFocus);
            aCellArea.setResizable(false);

            return aCellArea;
        }
    }

    class CaseWhenRenderer implements ListAreaCellRenderer {
        public JGoObject getListAreaCellRenderer(JGoObject list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String ret = "return";

            SQLWhen when = (SQLWhen) value;
            WhenArea whenArea = new WhenArea(ret);
            whenArea.setDataObject(when);
            whenArea.setResizable(false);

            return whenArea;
        }
    }

    class ConditionCellArea extends BasicCellArea {
        private BasicImageArea bia;
        private CellArea invalidMsg;
        
        public ConditionCellArea(String text) {
            super(text);
            
            bia = new BasicImageArea();
            bia.setSelectable(false);
            bia.setResizable(false);
            
            this.addObjectAtTail(bia);             
        }
        
        public void setImageIcon(int iconType, Icon icon, String tooltip) {
            if (icon != null) {
                if (bia != null){
                    bia.setVisible(true);
                    ImageIcon imgIcon = (ImageIcon) icon;
                    bia.loadImage(imgIcon.getImage(), false);
                    bia.setSize(imgIcon.getImage().getWidth(null), imgIcon.getImage().getHeight(null));
                    if (tooltip != null){
                        bia.setToolTipText(tooltip);
                    }
                }
            } else {
                if (bia != null){
                    bia.setVisible(false);
                }
            }
            
            layoutChildren();
        }
        
        public void layoutChildren() {
            if (drawBoundingRect) {
                rect.setBoundingRect(this.getBoundingRect());
            }
            
            int rectwidth = getWidth();
            int rectheight = getHeight();

            int width = rectwidth - insets.left - insets.right;
            int height = rectheight - insets.top - insets.bottom;
            
            if ((bia != null) && (bia.isVisible())){
                bia.setSpotLocation(JGoObject.Left, this, JGoObject.Left);

                int topGap = this.getHeight() - bia.getHeight();
                if (topGap > 0) {
                    bia.setTop(this.getTop() + topGap / 2);
                }
                
                bia.setLeft(bia.getLeft() + leftGap);
            }
            
            if (cellArea != null) {
                int w = width - leftGap;
                
                if (bia != null && bia.isVisible()) {
                    cellArea.setSpotLocation(JGoObject.Left, bia, JGoObject.Right);
                    cellArea.setLeft(cellArea.getLeft() + iconTextGap);
                    w = w - bia.getWidth() - iconTextGap;
                } else {
                    cellArea.setSpotLocation(JGoObject.Left, this, JGoObject.Left);
                    cellArea.setLeft(cellArea.getLeft() + leftGap);
                }
                
                cellArea.setSize(w, height);
            }
        }
        
        /**
         * get the maximum width
         * 
         * @return max width
         */
        public int getMaximumWidth() {
            int minWidth = getInsets().left + getInsets().right;
            minWidth += leftGap;
            
            if (bia != null){
                minWidth += bia.getWidth() + iconTextGap;
            }

            if (cellArea != null) {
                minWidth += getInvalidMessageArea().getMaximumWidth();
            }

            return minWidth;
        }

        /**
         * get the minimum width of this cell area
         * 
         * @return min width
         */
        public int getMinimumWidth() {
            int minWidth = getInsets().left + getInsets().right;
            minWidth += leftGap;
            
            if (bia != null){
                minWidth += bia.getWidth() + iconTextGap;
            }

            if (cellArea != null) {
                minWidth += getInvalidMessageArea().getMinimumWidth();
            }
            
            return minWidth;
        }        
        
        public int getMinimumHeight() {
            int minHeight = getInsets().top + getInsets().bottom;

            int height = 0;
            if (cellArea != null) {
                height = cellArea.getHeight();
            }

            if (bia != null) {
                if ((bia != null) && (height < bia.getHeight())) {
                    height = bia.getHeight();
                }
            }
            
            minHeight += height;
            return minHeight;
        }        
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.sql.framework.ui.graph.impl.BasicCellArea#initImageAreas()
         */
        protected void initImageAreas() {
        }
        
        private CellArea getInvalidMessageArea() {
            if (invalidMsg == null) {
                invalidMsg = new CellArea(INVALID_CONDITION);
            }
            return invalidMsg;
        }
    }
    
    class WhenArea extends BasicCanvasArea implements IHighlightable {
        private JGoRectangle rect;

        private BasicCellArea conditionArea;
        private BasicCellArea returnArea;
        private SQLWhen dataObject1;

        private boolean selected = false;

        private IHighlightConfigurator hc;
        
        private boolean drawBoundingRect = false;

        public WhenArea(String returnName) {
            super();

            //make it selectable so that it can be deleted
            this.setSelectable(true);

            rect = new JGoRectangle();
            rect.setPen(PEN_DEFAULT);

            rect.setBrush(BRUSH_INPUT_REGULAR);
            rect.setSelectable(false);
            rect.setResizable(false);
            addObjectAtHead(rect);
            drawBoundingRect(true);
            
            conditionArea = new ConditionCellArea(INVALID_CONDITION);
            conditionArea.setTextAlignment(JGoText.ALIGN_CENTER);
            String nbBundle4 = mLoc.t("BUND435: Double-click to edit condition{0}" ,"");
            conditionArea.setToolTipText(nbBundle4.substring(15));
            conditionArea.setLinePen(PEN_DEFAULT);
            conditionArea.setTextColor(TEXT_COLOR_INPUT);
            conditionArea.setBrush(BRUSH_INPUT_REGULAR);
            
            SQLCaseArea.this.setDisplayAttributesFor(conditionArea, null);
            conditionArea.setResizable(false);
            addObjectAtTail(conditionArea);

            returnArea = new BasicCellArea(BasicCellArea.LEFT_PORT_AREA, returnName);
            returnArea.setBrush(BRUSH_INPUT_REGULAR);
            returnArea.setLinePen(PEN_DEFAULT);
            returnArea.setTextColor(TEXT_COLOR_INPUT);
            returnArea.setTextAlignment(JGoText.ALIGN_CENTER);
            returnArea.setResizable(false);
            addObjectAtTail(returnArea);

            hc = new HighlightConfiguratorImpl(BRUSH_INPUT_REGULAR, BRUSH_INPUT_HIGHLIGHTED);
            
            setSize(conditionArea.getWidth() + 20, conditionArea.getMinimumHeight() + returnArea.getMinimumHeight());
        }
        
        public void setBrush(JGoBrush newBrush) {
            rect.setBrush(newBrush);
            conditionArea.setBrush(newBrush);
            returnArea.setBrush(newBrush);
        }

        /**
         * Set if a bounding rectangle needs to be drawn
         * 
         * @param drawRect boolean
         */
        public void drawBoundingRect(boolean drawRect) {
            this.drawBoundingRect = drawRect;
        }

        //when this are gains selection set the flag
        //this will be used to delete this are if selected
        protected void gainedSelection(JGoSelection selection) {
            super.gainedSelection(selection);
            selected = true;
        }

        protected void lostSelection(JGoSelection selection) {
            super.lostSelection(selection);
            selected = false;
        }

        public boolean isSelected() {
            return this.selected;
        }

        public boolean doMouseClick(int modifiers, Point dc, Point vc, JGoView view1) {
            int popupMask = java.awt.event.InputEvent.BUTTON3_MASK;
            if ((modifiers & popupMask) != 0 && whenPopup != null) {
                whenPopup.show(view1, vc.x, vc.y);
                return true;
            }

            return false;
        }
        
        /**
         * Overrides default implementation to display condition builder dialog for this
         * when clause's associated condition.
         * 
         * @see com.nwoods.jgo.JGoObject#doMouseDblClick(int, java.awt.Point, java.awt.Point, com.nwoods.jgo.JGoView)
         */
        public boolean doMouseDblClick(int modifiers, Point dc, Point vc, JGoView myView) {
            editWhen_ActionPerformed(null);
            return false;
        }
        
        String getText() {
            return SQLWhen.RETURN;
        }

        IGraphPort getLeftGraphPort() {
            return returnArea.getLeftGraphPort();
        }

        BasicCellArea getConditionArea() {
            return conditionArea;
        }

        /**
         * handle geometry change we always want to layout if geometry changes this is to
         * make sure we layout this area if location (when we collapse or expand location
         * changes)
         * 
         * @param prevRect previous bounds rectangle
         */
        protected void geometryChange(Rectangle prevRect) {
            layoutChildren();
        }

        /**
         * Lays out the children of this area.
         */
        public void layoutChildren() {
            if (drawBoundingRect) {
                rect.setBoundingRect(this.getBoundingRect());
            }

            int rectleft = getLeft();
            int recttop = getTop();
            int rectwidth = getWidth();
            int rectheight = getHeight();

            int left = rectleft + insets.left;
            int top = recttop + insets.top;
            int width = rectwidth - insets.left - insets.right;
            int height = rectheight - insets.top - insets.bottom;

            if (height > 0 && this.isVisible()) {
                conditionArea.setVisible(true);
                conditionArea.setBoundingRect(left, top, width, conditionArea.getHeight());
            } else {
                conditionArea.setVisible(false);
                conditionArea.setLocation(left, top);
            }
            
            if (height - conditionArea.getHeight() > 0 && this.isVisible()) {
                returnArea.setVisible(true);
                returnArea.setBoundingRect(left, top + conditionArea.getHeight(), width, returnArea.getHeight());
            } else {
                returnArea.setVisible(false);
                returnArea.setLocation(left, top);
            }
        }

        /**
         * Expands this graph node
         * 
         * @param expand whether to expand or collapse this node
         */
        public void expand(boolean expand) {
        }

        /**
         * Gets the data object associated with graph node
         * 
         * @return data object
         */
        public Object getDataObject() {
            return dataObject1;
        }

        /**
         * Sets the data object associated with graph node
         * 
         * @param obj new data object
         */
        public void setDataObject(Object obj) {
            this.dataObject1 = (SQLWhen) obj;
            if (dataObject1 != null) {
                SQLCondition cond = dataObject1.getCondition();
                setDisplayAttributesFor(conditionArea, cond);
            }
        }

        /**
         * Gets field name given a port
         * 
         * @param graphPort graph port
         * @return field name
         */
        public String getFieldName(IGraphPort graphPort) {
            if (returnArea.getLeftGraphPort().equals(graphPort)) {
                return SQLWhen.RETURN;
            }

            return null;
        }

        /**
         * Gets input graph port, given a field name
         * 
         * @param fieldName field name
         * @return graph port
         */
        public IGraphPort getInputGraphPort(String fieldName) {
            if (fieldName.equals(SQLWhen.RETURN)) {
                return returnArea.getLeftGraphPort();
            }

            return null;
        }

        /**
         * Gets output graph port , given a field name
         * 
         * @param fieldName field name
         * @return graph port
         */
        public IGraphPort getOutputGraphPort(String fieldName) {
            return null;
        }

        /**
         * Gets List of all input and output links
         * 
         * @return list of input links
         */
        public List getAllLinks() {
            List list = new ArrayList();

            IGraphPort port = returnArea.getLeftGraphPort();
            addLinks(port, list);

            return list;
        }

        /**
         * get the child graphNode
         * 
         * @param obj child data object
         * @return graph node
         */
        public IGraphNode getChildNode(Object obj) {
            return null;
        }

        /**
         * Get the parent node
         * 
         * @return parent
         */
        public IGraphNode getParentGraphNode() {
            return SQLCaseArea.this;
        }

        /**
         * @see org.netbeans.modules.sql.framework.ui.graph.IHighlightable#setHighlighted(boolean)
         */
        public void setHighlighted(boolean shouldHighlight) {
            if (shouldHighlight) {
                this.setBrush(hc.getHoverBrush());
            } else {
                this.setBrush(hc.getNormalBrush());
            }
        }

        /**
         * @see org.netbeans.modules.sql.framework.ui.graph.IHighlightable#setHighlightEnabled(boolean)
         */
        public void setHighlightEnabled(boolean enabled) {
            // Do nothing - WhenArea is always highlightable
        }

        /**
         * @see org.netbeans.modules.sql.framework.ui.graph.IHighlightable#isHighlightEnabled()
         */
        public boolean isHighlightEnabled() {
            return true;
        }

        /**
         * @see org.netbeans.modules.sql.framework.ui.graph.IHighlightable#getHighlightConfigurator()
         */
        public IHighlightConfigurator getHighlightConfigurator() {
            return hc;
        }

        /**
         * @see org.netbeans.modules.sql.framework.ui.graph.IHighlightable#setHighlightConfigurator(org.netbeans.modules.sql.framework.ui.graph.IHighlightConfigurator)
         */
        public void setHighlightConfigurator(IHighlightConfigurator hc) {
            throw new UnsupportedOperationException("Cannot change HighlightConfigurator for WhenArea.");
        }
    }

    /**
     * get maximum width of this area
     * 
     * @return max width
     */
    public int getMaximumWidth() {
        int maxWidth = getInsets().left + getInsets().right;

        int w = 0;

        w = titleArea.getMaximumWidth();

        if (listArea.getMaximumWidth() > w) {
            w = listArea.getMaximumWidth();
        }

        if (defaultArea.getMaximumWidth() > w) {
            w = defaultArea.getMaximumWidth();
        }

        if (resultArea.getMaximumWidth() > w) {
            w = resultArea.getMaximumWidth();
        }

        maxWidth += w;

        return maxWidth;
    }

    /**
     * get the maximum height of this area
     * 
     * @return max height
     */
    public int getMaximumHeight() {
        int maxHeight = getInsets().top + getInsets().bottom;

        maxHeight += titleArea.getMaximumHeight();
        maxHeight += listArea.getMaximumHeight();
        maxHeight += defaultArea.getMaximumHeight();
        maxHeight += resultArea.getMaximumHeight();

        return maxHeight;
    }

    /**
     * get the minimum width of this area
     * 
     * @return minimum width
     */
    public int getMinimumWidth() {
        int insetWidth = getInsets().left + getInsets().right;

        int applicableWidth = 0;

        applicableWidth = Math.max(titleArea.getMinimumWidth(), listArea.getMaximumWidth());
        applicableWidth = Math.max(applicableWidth, defaultArea.getMaximumWidth());
        applicableWidth = Math.max(applicableWidth, resultArea.getMaximumWidth());

        return insetWidth + Math.max(0, applicableWidth);
    }

    private void initializePopUpMenus() {
        OperatorActionListener aListener = new OperatorActionListener();
        popUpMenu = new JPopupMenu();
        whenPopup = new JPopupMenu();

        // Show SQL
        String nbBundle5 = mLoc.t("BUND365: Show SQL");
        showSqlItem = new JMenuItem(nbBundle5.substring(15), new ImageIcon(showSqlUrl));
        showSqlItem.addActionListener(aListener);

        // Add new when. Use Action to allow inclusion in multiple menus but control via
        // only
        // one ActionListener.
        String nbBundle6 = mLoc.t("BUND437: Add New When");
        Action addWhenAction = new AbstractAction(nbBundle6.substring(15), new ImageIcon(caseUrl)) {
            public void actionPerformed(ActionEvent e) {
                addWhen_ActionPerformed(e);
            }
        };

        // Remove case-when
        String nbBundle7 = mLoc.t("BUND152: Remove");
        removeItem = new JMenuItem(nbBundle7.substring(15), new ImageIcon(removeUrl));
        removeItem.addActionListener(aListener);

        // Remove when
        String nbBundle8 = mLoc.t("BUND439: Remove When");
        removeWhenAction = new AbstractAction(nbBundle8.substring(15), new ImageIcon(removeUrl)) {
            public void actionPerformed(ActionEvent e) {
                removeWhen_ActionPerformed(e);
            }
        };

        // Build up main pop up menu.
        popUpMenu.add(showSqlItem);
        popUpMenu.add(new JMenuItem(addWhenAction));
        popUpMenu.addSeparator();
        popUpMenu.add(removeItem);

        // Build up when-specific pop up menu.
        String nbBundle9 = mLoc.t("BUND440: Edit Condition...");
        Action editWhenAction = new AbstractAction(nbBundle9.substring(15), new ImageIcon(editUrl)) {
            public void actionPerformed(ActionEvent e) {
                editWhen_ActionPerformed(e);
            }
        };
        whenPopup.add(new JMenuItem(editWhenAction));
        whenPopup.addSeparator();
        whenPopup.add(new JMenuItem(addWhenAction));
        whenPopup.add(new JMenuItem(removeWhenAction));
    }

    private class OperatorActionListener implements ActionListener {
        /**
         * Invoked when an action occurs.
         * 
         * @param e ActionEvent to handle
         */
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == showSqlItem) {
                showSql_actionPerformed(e);
            } else if (source == removeItem) {
                remove_ActionPerformed(e);
            }
        }
    }

    private void editWhen_ActionPerformed(ActionEvent e) {
        for (int i = 0; i < listArea.getModel().getSize(); i++) {
            WhenArea renderer = (WhenArea) listArea.getCellRendererComponent(i);
            if (renderer.isSelected()) {
                showConditionBuilderFor(renderer);
                break;
            }
        }
    }
    
    /**
     * @param renderer
     */
    private void showConditionBuilderFor(WhenArea renderer) {
        SQLWhen when = (SQLWhen) renderer.getDataObject();
        ConditionBuilderView builderView = ConditionBuilderUtil.getConditionBuilderView(when, (IGraphViewContainer) this.getGraphView().getGraphViewContainer());
        
        String nbBundle10 = mLoc.t("BUND441: Edit when condition");
        String dlgTitle = nbBundle10.substring(15);
        DialogDescriptor dd = new DialogDescriptor(builderView, dlgTitle,
            true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);
        builderView.doValidation();
        
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            SQLCondition cond = (SQLCondition) builderView.getPropertyValue();
            BasicCellArea conditionArea = renderer.getConditionArea();
            
            if (cond != null) {
                when.setCondition(cond);
            }
            
            if (null != conditionArea) {
                setDisplayAttributesFor(conditionArea, cond);
                conditionArea.layoutChildren();
            }
            ((SQLUIModel) this.getGraphView().getGraphModel()).setDirty(true);
        } 
    }

    /**
     * @param cellArea TODO
     * @return
     */
    private void setDisplayAttributesFor(BasicCellArea cellArea, SQLCondition cond) {
        String conditionDisplay = "";
        
        if (cond != null && cond.isConditionDefined() && cond.isValid()) {
            if (validIcon == null) {
                validIcon = new ImageIcon(validIconUrl);
            }
            
            cellArea.setText(VALID_CONDITION);
            cellArea.setImageIcon(BasicCellArea.IMAGE_VALIDATION, validIcon);
            conditionDisplay = ": " + cond.getConditionText();            
        } else {
            if (invalidIcon == null) {
                invalidIcon = new ImageIcon(invalidIconUrl);
            }
            
            cellArea.setText(INVALID_CONDITION);
            cellArea.setImageIcon(BasicCellArea.IMAGE_VALIDATION, invalidIcon);
        }
        String nbBundle11 = mLoc.t("BUND435: Double-click to edit condition{0}",conditionDisplay);
        cellArea.setToolTipText(nbBundle11.substring(15));        
    }

    private void removeWhen_ActionPerformed(ActionEvent e) {
        for (int i = 0; i < listArea.getModel().getSize(); i++) {
            WhenArea renderer = (WhenArea) listArea.getCellRendererComponent(i);
            if (renderer.isSelected()) {
                try {
                    removeChildNode(renderer);
                    ((SQLUIModel) this.getGraphView().getGraphModel()).setDirty(true);
                } catch (Exception ex) {
                    NotifyDescriptor d = new NotifyDescriptor.Message(ex.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
        }
    }

    private void addWhen_ActionPerformed(ActionEvent e) {
        SQLCaseOperator caseObj1 = (SQLCaseOperator) this.getDataObject();
        try {
            if (caseObj != null) {
                // Condition builder dialog call goes here.
                
                SQLWhen when = caseObj1.createSQLWhen();
                caseObj1.addSQLWhen(when);
                
                //a case is added so make model dirty
                ((SQLUIModel) this.getGraphView().getGraphModel()).setDirty(true);

                CaseListModel model = (CaseListModel) listArea.getModel();
                model.add(when);
                this.setHeight(this.getMaximumHeight());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Invoked when an action occurs.
     * 
     * @param e ActionEvent to handle
     */
    private void showSql_actionPerformed(ActionEvent e) {
        SQLObject sqlObject = (SQLObject) SQLCaseArea.this.getDataObject();
        this.getGraphView().execute(ICommand.SHOW_SQL_CMD, new Object[] { sqlObject });
    }

    private void remove_ActionPerformed(ActionEvent e) {
        this.getGraphView().deleteNode(this);
    }

    private class RemoveWhenListener implements ListDataListener {
        private CaseListModel model;

        public RemoveWhenListener(CaseListModel aModel) {
            model = aModel;
            aModel.addListDataListener(this);
        }

        protected void finalize() throws Throwable {
            if (model != null) {
                model.removeListDataListener(this);
            }
        }

        public void contentsChanged(ListDataEvent e) {
            setRemoveActionEnabled(e.getSource());
        }

        public void intervalAdded(ListDataEvent e) {
            setRemoveActionEnabled(e.getSource());
        }

        public void intervalRemoved(ListDataEvent e) {
            setRemoveActionEnabled(e.getSource());
        }

        public void update() {
            setRemoveActionEnabled(model);
        }

        private void setRemoveActionEnabled(Object source) {
            if (null != model && source == model) {
                if (removeWhenAction != null) {
                    removeWhenAction.setEnabled(model.getSize() > 1);
                }
            }
        }
    }
    
    protected void setExpanded(boolean isExpanded) {
        super.setExpanded(isExpanded);
        setResizable(false);
    }    
}

