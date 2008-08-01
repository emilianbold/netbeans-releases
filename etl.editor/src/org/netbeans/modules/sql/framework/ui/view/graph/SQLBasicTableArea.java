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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.common.utils.XmlUtil;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.visitors.ColumnsUsedInSQLOperatorVisitor;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicCellArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicImageArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicTableArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.ColumnArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.ColumnPortArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.PortArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.TableConstants;
import org.netbeans.modules.sql.framework.ui.graph.impl.TitleArea;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;
import org.netbeans.modules.sql.framework.ui.view.TableColumnNode;
import org.netbeans.modules.sql.framework.ui.view.TableColumnTreePanel;
import org.openide.windows.WindowManager;
import net.java.hulp.i18n.Logger;
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoPort;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.StringUtil;
import java.awt.event.InputEvent;
import javax.swing.KeyStroke;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.ForeignKey;

/**
 * This class represents the table rendered on the canvas. It implements custom rendering
 * of a table similar to swing's JTable
 *
 * @author Ritesh Adval
 * @author Jonathan Giron
 */
public abstract class SQLBasicTableArea extends BasicTableArea implements IGraphNode {

    /* log4j logger category */
    private static final String LOG_CATEGORY = SQLBasicTableArea.class.getName();
    protected SQLDBTable table;
    private static URL filterUrl = SQLBasicTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/filter16.gif");
    private static URL columnValidationIconUrl = SQLBasicTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/validateField.png");
    private static URL selectColumnsUrl = SQLBasicTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/ColumnSelection.png");
    private static URL removeUrl = SQLBasicTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/remove.png");
    private int tableType;
    private JMenuItem selectColumnsItem;
    private JMenuItem removeItem;
    private static transient final Logger mLogger = Logger.getLogger(SQLBasicTableArea.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public SQLBasicTableArea() {
        super();
        this.setSelectable(true);
        this.setResizable(true);
        this.setPickableBackground(true);
        setTitleToolBarGap(0);
        setToolBarTableGap(0);
    }

    /**
     * Creates a new instance of SQLBasicTableArea
     *
     * @param table the table to render
     * @param icon icon for this table
     * @param tableType type of this table (input, output, both)
     */
    public SQLBasicTableArea(SQLDBTable table) {
        this();
        initialize(table);
    }

    public void initialize(Object obj) {
        this.table = (SQLDBTable) obj;

        init();

        this.tableType = table.getObjectType();

        String title = table.getQualifiedName();

        titleArea = new TableTitleArea(title);
        Icon icon = createIcon();
        titleArea.setTitleImage(icon);
        titleArea.setBrush(getDefaultTitleBrush());

        this.addObjectAtTail(titleArea);

        initializeTable();

        // Set initialized size to ensure everything is visible in this area.
        setSize(new Dimension(getMaximumWidth(), this.getMaximumHeight()));
    }

    abstract Icon createIcon();

    abstract void initializePopUpMenu();

    protected abstract JGoBrush getDefaultTitleBrush();

    protected abstract Color getDefaultBackgroundColor();

    /**
     * Sets the condition filter icon in the graph table.
     */
    public abstract void setConditionIcons();

    /**
     * set the table title icon
     *
     * @param tableIcon table title icon
     */
    public void setTableIcon(Icon tableIcon) {
        titleArea.setTitleImage(tableIcon);
    }

    private void init() {
        popUpMenu = new JPopupMenu();
        initializePopUpMenu();
        initializeProperties();
    }

    protected void addSelectVisibleColumnsPopUpMenu(ActionListener aListener) {
        String nbBundle1 = mLoc.t("BUND426: Select Columns...");
        String lbl = nbBundle1.substring(15);
        selectColumnsItem = new JMenuItem(lbl, new ImageIcon(selectColumnsUrl));
        selectColumnsItem.setAccelerator(KeyStroke.getKeyStroke('E',InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_MASK));
        selectColumnsItem.addActionListener(aListener);
        popUpMenu.add(selectColumnsItem);
    }

    protected void addRemovePopUpMenu(ActionListener aListener) {
        //remove menu
        String nbBundle2 = mLoc.t("BUND152: Remove");
        String lbl = nbBundle2.substring(15);
        removeItem = new JMenuItem(lbl, new ImageIcon(removeUrl));
        removeItem.addActionListener(aListener);
        popUpMenu.add(removeItem);
    }

    private void initializeTable() {
        int tType = TableConstants.INPUT_TABLE;

        if (this.tableType == SQLConstants.TARGET_TABLE) {
            tType = TableConstants.OUTPUT_TABLE;
        } else if (this.tableType == SQLConstants.RUNTIME_OUTPUT) {
            tType = TableConstants.NO_PORT_TABLE;
        }

        SQLTableArea sqlTableArea = new SQLTableArea(tType, table);
        sqlTableArea.setShowHeader(false);
        ColumnArea columnArea = sqlTableArea.getColumnArea(0);
        if (columnArea != null) {
            columnArea.setPreferredWidth(25);
        }

        columnArea = sqlTableArea.getColumnArea(2);
        columnArea = sqlTableArea.getColumnArea(3);

        tableArea = sqlTableArea;
        this.addObjectAtTail(tableArea);

        setBackgroundColor(getDefaultBackgroundColor());
    }

    void setFilterIcon(String toolTip) {
        SQLTableArea tabArea = (SQLTableArea) this.getTableArea();
        MetaTableModel model = (MetaTableModel) tabArea.getModel();
        int rowCount = model.getRowCount();
        ColumnArea column = tabArea.getColumnArea(0);

        for (int i = 0; i < rowCount; i++) {
            BasicCellArea cell = column.getCellAt(i);

            if (model.isFiltered(i)) {
                String columnToolTip = toolTip;
                ImageIcon icon = new ImageIcon(filterUrl);
                SQLDBColumn col = (SQLDBColumn) model.getColumn(i);
                String columnName = col.toString();
                if (toolTip != null && columnName != null) {
                    int idx = toolTip.indexOf(columnName);
                    if (idx == -1) {
                        columnName = columnName.toLowerCase();
                    }
                    columnToolTip = columnToolTip.replaceAll(columnName, "<b> " + columnName + "</b>");
                }
                cell.setDataExtractionImageIcon(icon, columnToolTip);
            } else {
                cell.setDataExtractionImageIcon(null);
            }
        }
    }

    /**
     * Sets Validation icons for each of the columns names which are part of Validation
     * condition.
     *
     * @param condition
     */
    void setValidationIcon(String toolTip) {
        SQLTableArea tabArea = (SQLTableArea) this.getTableArea();
        MetaTableModel model = (MetaTableModel) tabArea.getModel();
        int rowCount = model.getRowCount();
        ColumnArea column = tabArea.getColumnArea(0);

        for (int i = 0; i < rowCount; i++) {
            BasicCellArea cell = column.getCellAt(i);

            if (model.isValidationPresent(i)) {
                String columnToolTip = toolTip;
                ImageIcon icon = new ImageIcon(columnValidationIconUrl);
                SQLDBColumn col = (SQLDBColumn) model.getColumn(i);
                String columnName = col.toString();
                if (toolTip != null && columnName != null) {
                    int idx = toolTip.indexOf(columnName);
                    if (idx == -1) {
                        columnName = columnName.toLowerCase();
                    }
                    columnToolTip = columnToolTip.replaceAll(columnName, "<b> " + columnName + "</b>");
                }
                cell.setImageIcon(BasicCellArea.IMAGE_VALIDATION, icon, columnToolTip);
            } else {
                cell.setImageIcon(BasicCellArea.IMAGE_VALIDATION, null);
            }
        }
    }

    private String getFilterToolTip(SQLCondition condition) {
        SQLPredicate rootPredicate = condition.getRootPredicate();

        String toolTip = null;
        StringBuilder tBuffer = new StringBuilder("<html> <table border=0 cellspacing=0 cellpadding=0><tr><td>");

        if (rootPredicate != null) {
            toolTip = rootPredicate.toString();
            if (toolTip != null) {
                //TODO we should pass locale when we have to convert to upper case.
                toolTip = XmlUtil.escapeHTML(toolTip);
                String upperCaseToolTip = toolTip.toUpperCase();
                tBuffer.append(StringUtil.insertStringBeforeLogicalOperators(upperCaseToolTip, "</td></tr><tr><td>"));
            }
        }

        tBuffer.append("</td></tr></table> </html>");

        toolTip = tBuffer.toString();

        return toolTip;

    }

    /**
     * Sets the bounding rectangle and ensure it does not resize below a certain width and
     * beyond a fixed height, depending on expanded state.
     *
     * @param left new x-location of upper-left-hand corner of object
     * @param top new y-location of upper-left-hand corner of object
     * @param width width of object
     * @param height height of object
     */
    @Override
    public void setBoundingRect(int left, int top, int width, int height) {
        super.setBoundingRect(left, top, Math.max(width, 100), (isExpandedState() ? getMaximumHeight() : height));
    }

    /**
     * Lays out the children of this area.
     */
    @Override
    public void layoutChildren() {
        super.layoutChildren();
    }

    private DBColumn getColumn(JGoPort port) {
        PortArea ptArea = (PortArea) port.getParent();
        ColumnPortArea pAreaGroup = (ColumnPortArea) ptArea.getParent();
        int row = pAreaGroup.getIndexOf(ptArea);

        if (row == -1) {
            return null;
        }

        SQLTableArea tabArea = (SQLTableArea) this.getTableArea();
        MetaTableModel model = (MetaTableModel) tabArea.getModel();
        return model.getColumn(row);

    }

    /**
     * Gets the name for a given port.
     *
     * @param iGraphPort port
     * @return name which has a port iGraphPort attach to it
     */
    @Override
    public String getFieldName(IGraphPort iGraphPort) {
        DBColumn column = getColumn((JGoPort) iGraphPort);
        return column.getName();
    }

    /**
     * Gets column port group based on the column name.
     *
     * @param columnName name of the column
     * @return column port group
     */
    @Override
    public IGraphPort getInputGraphPort(String columnName) {
        SQLTableArea tabArea = (SQLTableArea) this.getTableArea();
        MetaTableModel model = (MetaTableModel) tabArea.getModel();
        int index = -1;

        for (int i = 0; i < model.getRowCount(); i++) {
            DBColumn column = model.getColumn(i);
            if (column != null && column.getName().equals(columnName)) {
                index = i;
                break;

            }
        }

        if (index != -1) {
            ColumnPortArea inputPortArea = tabArea.getInputPortArea();
            if (inputPortArea != null) {
                PortArea ptArea = inputPortArea.getPortAreaAt(index);
                return ptArea.getGraphPort();
            }

        }

        return null;
    }

    /**
     * Gets the output port which represents the field columnName.
     *
     * @param columnName name of the column
     * @return port which represents columnName
     */
    @Override
    public IGraphPort getOutputGraphPort(String columnName) {
        SQLTableArea tabArea = (SQLTableArea) this.getTableArea();
        MetaTableModel model = (MetaTableModel) tabArea.getModel();
        int index = -1;

        for (int i = 0; i < model.getRowCount(); i++) {
            DBColumn column = model.getColumn(i);
            if (column != null && column.getName().equals(columnName)) {
                index = i;
                break;

            }
        }

        if (index != -1) {
            ColumnPortArea outputPortArea = tabArea.getOutputPortArea();
            if (outputPortArea != null) {
                PortArea ptArea = outputPortArea.getPortAreaAt(index);
                return ptArea.getGraphPort();
            }
        }

        return null;

    }

    /**
     * Sets the data object
     *
     * @param obj - then object to be represented by this node
     */
    @Override
    public void setDataObject(Object obj) {
        table = (SQLDBTable) obj;
    }

    public void refreshDataObject(Object obj) {
        SQLTableArea tableArea1 = (SQLTableArea) this.getTableArea();
        tableArea1.setTableObject((DBTable) obj);
        SQLBasicTableArea.this.setHeight(this.getMaximumHeight());
        setDataObject(obj);
    }

    public void addColumns(List<SQLDBColumn> columnList) {
        boolean added = false;

        SQLTableArea tableArea1 = (SQLTableArea) this.getTableArea();
        MetaTableModel model = (MetaTableModel) tableArea1.getModel();

        Iterator<SQLDBColumn> it = columnList.iterator();
        while (it.hasNext()) {
            SQLDBColumn column = it.next();
            if (!model.containsColumn(column)) {
                model.addColumn(column);
                added = true;
            } else {
                makeColumnVisible(column);
            }
        }

        if (added) {
            SQLBasicTableArea.this.setHeight(this.getMaximumHeight());
        }
    }

    public void makeColumnVisible(SQLDBColumn column) {
        SQLTableArea tableArea1 = (SQLTableArea) this.getTableArea();
        tableArea1.makeColumnVisible(column);
    }

    public void makeColumnInVisible(SQLDBColumn column) throws BaseException {
        SQLTableArea tableArea1 = (SQLTableArea) this.getTableArea();
        tableArea1.makeColumnInVisible(column);
    }

    public void removeColumns(List columnList) {
        boolean removed = false;

        SQLTableArea tableArea1 = (SQLTableArea) this.getTableArea();
        MetaTableModel model = (MetaTableModel) tableArea1.getModel();

        Iterator it = columnList.iterator();
        while (it.hasNext()) {
            SQLDBColumn column = (SQLDBColumn) it.next();
            model.removeColumn(column);
            removed = true;
        }

        if (removed) {
            SQLBasicTableArea.this.setHeight(this.getMaximumHeight());
        }
    }

    /**
     * If column is visible then we remove the model.removeColumn actually remove the
     * column from its parent table otherwise it just make it invisible in the table node
     * so that later it can be made visible at the same position in the table graph node
     * if you just want to hide table in graph node make sure to set the visible attr in
     * column to false before calling this method otherwise that column will actually be
     * removed permanently from table
     */
    public void removeColumn(SQLDBColumn column) throws BaseException {

        SQLTableArea tableArea1 = (SQLTableArea) this.getTableArea();
        MetaTableModel model = (MetaTableModel) tableArea1.getModel();
        if (column instanceof SourceColumn) {
            removeColumnReference(column);
        }
        model.removeColumn(column);
    }

    public void updateColumn(DBColumn column) {
        SQLTableArea tableArea1 = (SQLTableArea) this.getTableArea();
        MetaTableModel model = (MetaTableModel) tableArea1.getModel();
        model.updateColumn(column);
    }

    /**
     * get the data object stored in this object
     *
     * @return data object
     */
    @Override
    public Object getDataObject() {
        return this.table;
    }

    protected void handleCommonActions(ActionEvent e) {
        Object source = e.getSource();
        if (source == selectColumnsItem) {
            selectVisibleColumnsActionPerformed(e);
        } else if (source == removeItem) {
            Remove_ActionPerformed(e);
        }
    }

    //keep it protected as some subclass overrides it
    protected boolean selectVisibleColumnsActionPerformed(ActionEvent e) {
        SQLDBTable dbTable = (SQLDBTable) SQLBasicTableArea.this.getDataObject();
        if (dbTable == null) {
            return false;
        }

        List tableList = new ArrayList();
        tableList.add(dbTable);

        TableColumnTreePanel columnPanel = new TableColumnTreePanel(tableList, true);
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        String nbBundle3 = mLoc.t("BUND428: Select columns to display for this table.");
        String dlgLabel = nbBundle3.substring(15);
        JLabel lbl = new JLabel(dlgLabel);
        lbl.getAccessibleContext().setAccessibleName(dlgLabel);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.bottom = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        panel.add(new JSeparator(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(columnPanel, gbc);

        String nbBundle4 = mLoc.t("BUND429: Select Columns");
        String dlgTitle = nbBundle4.substring(15);
        int response = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), panel, dlgTitle, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        boolean userClickedOk = (JOptionPane.OK_OPTION == response);
        if (userClickedOk) {
            List columns = dbTable.getColumnList();
            List tableNodes = columnPanel.getTableColumnNodes();

            Iterator iter = columns.iterator();
            while (iter.hasNext()) {
                SQLDBColumn column = (SQLDBColumn) iter.next();
                boolean userWantsVisible = TableColumnNode.isColumnVisible(column, tableNodes);
                if (column.isVisible() && !userWantsVisible) {
                    column.setVisible(false);
                    try {
                        removeColumn(column);
                    } catch (BaseException ex) {
                    }
                } else if (!column.isVisible() && userWantsVisible) {
                    column.setVisible(true);

                    MetaTableModel model = (MetaTableModel) (((SQLTableArea) getTableArea()).getModel());
                    if (!model.containsColumn(column)) {
                        model.addColumn(column);
                    } else {
                        makeColumnVisible(column);
                    }
                }
            }
            setHeight(getMaximumHeight());
            layoutChildren();

            // Mark collab as needing to be persisted.
            Object graphModel = getGraphView().getGraphModel();
            if (graphModel instanceof CollabSQLUIModel) {
                ((CollabSQLUIModel) graphModel).setDirty(true);
            }
            setConditionIcons();
        }

        return userClickedOk;
    }

    //keep it protected as some subclass overrides it
    protected void Remove_ActionPerformed(ActionEvent e) {
        this.getGraphView().deleteNode(this);
    }

    /**
     * Sets the Extraction condition icons.
     *
     * @param condition
     */
    protected void setTableConditionIcons(SQLCondition condition) {
        if (condition == null) {
            return;
        }
        setTableConditionIcons(condition, null);
    }

    /**
     * Sets the Extraction and Validation condition icons.
     *
     * @param extractionCondition
     * @param validationCondition
     */
    protected void setTableConditionIcons(SQLCondition extractionCondition, SQLCondition validationCondition) {
        if ((extractionCondition == null) && (validationCondition == null)) {
            return;
        }

        SQLTableArea tableArea1 = (SQLTableArea) SQLBasicTableArea.this.getTableArea();
        MetaTableModel metTabMod = (MetaTableModel) tableArea1.getModel();
        metTabMod.resetFilters();
        metTabMod.resetValidationFlag();

        SQLPredicate extractionPredicate = null;
        if (extractionCondition != null) {
            extractionPredicate = extractionCondition.getRootPredicate();
        }

        //Set extraction flag on model
        if (extractionPredicate != null) {
            try {
                setConditionFlag(BasicCellArea.IMAGE_EXTRACTION, metTabMod, extractionPredicate);
            } catch (BaseException ex) {
                mLogger.errorNoloc(mLoc.t("EDIT168: Error setting filter icon for ({0})", table.getDisplayName()), ex);

                return;
            }
        }

        SQLPredicate validationPredicate = null;
        if (validationCondition != null) {
            validationPredicate = validationCondition.getRootPredicate();
        }

        //Set validation flag on model
        if (validationPredicate != null) {
            try {
                setConditionFlag(BasicCellArea.IMAGE_VALIDATION, metTabMod, validationPredicate);
            } catch (BaseException ex) {
                mLogger.errorNoloc(mLoc.t("EDIT169: Error setting validation icon for ({0})", table.getDisplayName()), ex);

                return;
            }
        }

        if (extractionCondition != null) {
            setFilterIcon(getFilterToolTip(extractionCondition));
        }

        if (validationCondition != null) {
            setValidationIcon(getFilterToolTip(validationCondition));
        }
        layoutChildren();
    }

    /**
     * Sets data extraction flag on table model.
     *
     * @param metTabMod
     * @param predicate
     * @throws BaseException
     */
    private void setConditionFlag(int flagType, MetaTableModel metTabMod, SQLPredicate predicate) throws BaseException {

        Set columnNames = ColumnsUsedInSQLOperatorVisitor.visit(predicate);
        if (columnNames != null) {
            Iterator cNamesItr = columnNames.iterator();
            while (cNamesItr.hasNext()) {
                String rowName = (String) cNamesItr.next();
                if (rowName != null) {
                    if (flagType == BasicCellArea.IMAGE_EXTRACTION) {
                        metTabMod.setFilter(rowName, true);
                    } else if (flagType == BasicCellArea.IMAGE_VALIDATION) {
                        metTabMod.setValidationFlag(rowName, true);
                    }
                }
            }
        }
    }

    /**
     * Gets a list of all input and output links.
     *
     * @return list of input links
     */
    @Override
    public List<JGoLink> getAllLinks() {
        ArrayList<JGoLink> list = new ArrayList<JGoLink>();
        IGraphPort port = null;
        SQLTableArea tabArea = (SQLTableArea) this.getTableArea();
        MetaTableModel model = (MetaTableModel) tabArea.getModel();

        for (int i = 0; i < model.getRowCount(); i++) {
            ColumnPortArea inputPortArea = tabArea.getInputPortArea();
            if (inputPortArea != null) {
                PortArea ptArea = inputPortArea.getPortAreaAt(i);
                if (ptArea != null) {
                    port = ptArea.getGraphPort();
                    addLinks(port, list);
                }
            }

            ColumnPortArea outputPortArea = tabArea.getOutputPortArea();
            if (outputPortArea != null) {
                PortArea ptArea = outputPortArea.getPortAreaAt(i);
                if (ptArea != null) {
                    port = ptArea.getGraphPort();
                    addLinks(port, list);
                }
            }
        }

        return list;
    }

    private List getForeignKeyList(DBColumn column) {
        ArrayList optionList = new ArrayList();
        String nbBundle5 = mLoc.t("BUND430: -->");
        String refString = column.getName() + nbBundle5.substring(15);

        List list = table.getForeignKeys();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            ForeignKey fk = (ForeignKey) it.next();
            if (fk.contains(column)) {
                List pkColumnList = fk.getPKColumnNames();
                Iterator it1 = pkColumnList.iterator();
                while (it1.hasNext()) {
                    String pkColName = (String) it1.next();
                    String optStr = refString.toString() + pkColName;
                    optionList.add(optStr);
                }
            }
        }

        return optionList;
    }

    private void initializeProperties() {
        ArrayList pkList = new ArrayList();
        ArrayList fkList = new ArrayList();
        ArrayList idxList = new ArrayList();

        List columnList = table.getColumnList();
        Iterator it = columnList.iterator();
        while (it.hasNext()) {
            DBColumn column = (DBColumn) it.next();
            boolean pk = column.isPrimaryKey();
            boolean fk = column.isForeignKey();
            boolean indexed = column.isIndexed();

            //create pk option
            if (pk) {
                pkList.add(column.getName());
            }

            //get fk options
            if (fk) {
                List fkListForColumn = getForeignKeyList(column);
                if (fkListForColumn.size() > 0) {
                    fkList.addAll(fkListForColumn);
                }
            }

            //create idx option
            if (indexed) {
                idxList.add(column.getName());
            }
        }

        //sort options
        Collections.sort(pkList);
        Collections.sort(fkList);
        Collections.sort(idxList);
    }

    class ColumnPropertySupport {

        private Vector pkVec;

        ColumnPropertySupport(List list) {
            pkVec = new Vector(list);
        }

        public void add(String pk) {
            pkVec.add(pk);
        }

        public Vector getDisplayVector() {
            return pkVec;
        }

        public String getDisplayString() {
            if (pkVec.isEmpty()) {
                String nbBundle6 = mLoc.t("BUND431: None");
                return nbBundle6.substring(15);
            }

            StringBuilder strBuf = null;
            Iterator it = pkVec.iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                if (strBuf == null) {
                    strBuf = new StringBuilder(str);
                } else {
                    strBuf.append(str);
                }
            }

            return strBuf.toString();
        }
    }

    /**
     * Updates this node with changes in data object.
     */
    @Override
    public void updateUI() {
        SQLBasicTableArea.this.setHeight(this.getMaximumHeight());
    }

    /**
     * Removes the child data object.
     *
     * @param obj child data object
     */
    @Override
    public void removeChildObject(Object obj) {
        if (obj instanceof DBColumn) {
            ArrayList list = new ArrayList();
            list.add(obj);
            this.addColumns(list);
        }
    }

    /**
     * Adds a new child data object.
     *
     * @param obj child data object
     */
    @Override
    public void addChildObject(Object obj) {
        if (obj instanceof DBColumn) {
            ArrayList list = new ArrayList();
            list.add(obj);
            this.removeColumns(list);
        }
    }

    public void showExpansionImage(boolean show) {
        this.titleArea.showExpansionImage(show);
    }

    public boolean isColumnMapped(SQLDBColumn column) {
        SQLTableArea tblArea = (SQLTableArea) this.tableArea;
        return tblArea.isColumnMapped(column);
    }

    public void removeColumnReference(SQLDBColumn column) throws BaseException {
        SQLTableArea tblArea = (SQLTableArea) this.tableArea;
        tblArea.removeColumnReference(column);
    }

    class TableTitleArea extends TitleArea {

        TableTitleArea(String titleStr) {
            super(titleStr);
        }

        @Override
        public String getToolTipText() {
            return UIUtil.getTableToolTip((SQLDBTable) SQLBasicTableArea.this.getDataObject());
        }
    }

    class FilterImage extends BasicImageArea {

        private SQLCondition condition;

        FilterImage(SQLCondition cond) {
            super();
            this.condition = cond;
        }

        @Override
        public String getToolTipText() {
            if (condition == null) {
                return null;
            }

            SQLPredicate rootPredicate = condition.getRootPredicate();

            if (rootPredicate == null) {
                return null;
            }

            String filterStr = rootPredicate.toString();

            return filterStr;
        }
    }
}

