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
package org.netbeans.modules.sql.framework.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLGroupBy;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.utils.ConditionUtil;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderUtil;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderView;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class GroupByView extends JPanel implements EnhancedCustomPropertyEditor {

    private static transient final Logger mLogger = Logger.getLogger(GroupByView.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    class ButtonActionListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            String actionCmd = e.getActionCommand();

            if (actionCmd == null) {
                return;
            }

            if (actionCmd.equalsIgnoreCase("UP")) {
                moveUp();
            } else if (actionCmd.equalsIgnoreCase("DOWN")) {
                moveDown();
            } else if (actionCmd.equalsIgnoreCase("ADD")) {
                add();
            } else if (actionCmd.equalsIgnoreCase("REMOVE")) {
                remove();
            } else if (actionCmd.equalsIgnoreCase("HAVING")) {
                showCondBuilder();
            }
        }

        private void add() {
            int[] sel = table.getSelectedRows();
            if (sel.length == 0) {
                return;
            }
            ArrayList list = new ArrayList();
            for (int i = 0; i < sel.length; i++) {
                list.add(model.getColumn(sel[i]));
            }

            // now delete from top panel
            for (int i = 0; i < list.size(); i++) {
                model.removeColumn(list.get(i));
            }

            // now add to bottom panel
            groupPanel.addToList(list);
            table.clearSelection();
        }

        private void moveDown() {
            int[] sel = table.getSelectedRows();
            for (int i = 0; i < sel.length; i++) {
                int indx = sel[i];
                if (indx == model.getRowCount() - 1) {
                    return;
                }
            }

            for (int i = 0; i < sel.length; i++) {
                int indx = sel[i];
                Object obj = model.getColumn(indx);
                model.addColumn(indx + 1, obj);
                model.removeColumn(indx);

            }
        }

        private void moveUp() {
            int[] sel = table.getSelectedRows();
            for (int i = 0; i < sel.length; i++) {
                int indx = sel[i];
                if (indx == 0) {
                    return;
                }
            }

            for (int i = 0; i < sel.length; i++) {
                int indx = sel[i];
                Object obj = model.getColumn(indx);
                model.removeColumn(indx);
                model.addColumn(indx - 1, obj);
            }

        }

        private void remove() {
            List list = groupPanel.getSelectItems();
            for (int i = 0; i < list.size(); i++) {
                model.addColumn(list.get(i));
            }

            groupPanel.removeFromList(list);
        }

        private void showCondBuilder() {
            String text = havingText.getText();
            SQLGroupBy groupBy = targetTable.getSQLGroupBy();
            if (groupBy == null) {
                groupBy = SQLModelObjectFactory.getInstance().createGroupBy();
                targetTable.setSQLGroupBy(groupBy);
            }
            SQLCondition conditionContainer = groupBy.getHavingCondition();
            String oldText = null;
            if (conditionContainer != null) {
                oldText = conditionContainer.getConditionText();
            }

            if (conditionContainer != null && text != null && !text.equals(oldText)) {
                try {
                    conditionContainer = (SQLCondition) conditionContainer.cloneSQLObject();
                } catch (CloneNotSupportedException ex) {
                    mLogger.errorNoloc(mLoc.t("EDIT196: error cloning the condition {0}", LOG_CATEGORY), ex);
                    return;
                }

                conditionContainer.setConditionText(text);
                SQLDefinition def = SQLObjectUtil.getAncestralSQLDefinition((SQLObject) conditionContainer.getParent());
                try {
                    SQLObject obj = ConditionUtil.parseCondition(text, def);
                    conditionContainer.removeAllObjects();
                    ConditionUtil.populateCondition(conditionContainer, obj);
                    // if we do not get a predicate then the condition is invalid
                    // and if text is not empty string
                    if (!(obj instanceof SQLPredicate) && !text.trim().equals("")) {
                        warnForInvalidCondition();
                    }
                } catch (Exception ex) {
                    mLogger.errorNoloc(mLoc.t("EDIT201: Error finding root predicate from text condition{0}from joinview table.", text), ex);
                    warnForInvalidCondition();
                }

                // if user modified text then change the gui mode
                conditionContainer.setGuiMode(SQLCondition.GUIMODE_SQLCODE);
                groupBy.setHavingCondition(conditionContainer);
            }

            ConditionBuilderView builderView = ConditionBuilderUtil.getHavingConditionBuilderView(targetTable, editor);

            DialogDescriptor dd = new DialogDescriptor(builderView, "Having Condition", true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);
            
            if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
                SQLCondition cond = (SQLCondition) builderView.getPropertyValue();
                if (cond != null) {
                    groupBy.setHavingCondition(cond);
                    havingText.setText(cond.getConditionText());
                }
            }
        }

        private void warnForInvalidCondition() {
            String nbBundle1 = mLoc.t("BUND484: The condition is not valid.Make sure you correct it.");
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(nbBundle1.substring(15),
                    NotifyDescriptor.WARNING_MESSAGE));
        }
    }

    class GroupByTableModel extends AbstractTableModel {

        private String[] columnNames = {"Column Name"};
        private List rowList = new ArrayList();

        public GroupByTableModel() {
        }

        public GroupByTableModel(List list) {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                SQLObject expr = (SQLObject) it.next();
                rowList.add(expr);
            }
        }

        public void addColumn(int idx, Object expr) {
            rowList.add(idx, expr);
            this.fireTableRowsInserted(getRowCount(), getRowCount());
        }

        public void addColumn(Object expr) {
            rowList.add(expr);
            this.fireTableRowsInserted(getRowCount(), getRowCount());
        }

        public Object getColumn(int idx) {
            return rowList.get(idx);
        }

        /*
         * JTable uses this method to determine the default renderer/ editor for each
         * cell. If we didn't implement this method, then the last column would contain
         * text ("true"/"false"), rather than a check box.
         */
        public Class getColumnClass(int c) {
            if (c == 0) {
                return SourceColumn.class;
            }

            return String.class;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public int getRowCount() {
            return rowList.size();
        }

        public Object getValueAt(int row, int col) {
            Object obj = rowList.get(row);
            switch (col) {
                case 0:
                    return obj;
            }

            return String.valueOf(col + "?");
        }

        /*
         * Don't need to implement this method unless the table is editable.
         */
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public void removeColumn(int idx) {
            rowList.remove(idx);
            this.fireTableRowsInserted(idx, idx);
        }

        public void removeColumn(Object expr) {
            int idx = rowList.indexOf(expr);
            if (idx != -1) {
                rowList.remove(idx);
                this.fireTableRowsInserted(idx, idx);
            }
        }

        /*
         * Don't need to implement this method unless your table's data can change.
         */
        public void setValueAt(Object value, int row, int col) {
        }
    }

    class GroupTableCellRenderer extends DefaultTableCellRenderer {

        /**
         * Returns the default table cell renderer.
         * 
         * @param aTable the <code>JTable</code>
         * @param value the value to assign to the cell at <code>[row, column]</code>
         * @param isSelected true if cell is selected
         * @param isFocus true if cell has focus
         * @param row the row of the cell to render
         * @param column the column of the cell to render
         * @return the default table cell renderer
         */
        public Component getTableCellRendererComponent(JTable aTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return super.getTableCellRendererComponent(aTable, value.toString(), isSelected, hasFocus, row, column);
        }
    }
    private static final String LOG_CATEGORY = GroupByView.class.getName();
    private IGraphViewContainer editor;
    private GroupPanel groupPanel;
    private JTextField havingText;
    private GroupByTableModel model;
    private JTable table;
    private TargetTable targetTable;

    /**
     * New instance
     * 
     * @param groupByColumn - column
     */
    public GroupByView(IGraphViewContainer editor, TargetTable tTable, Collection groupByColumns) {
        this.targetTable = tTable;
        this.editor = editor;
        List allColumns = new ArrayList();

        try {
            List srcTables = targetTable.getSourceTableList();
            for (Iterator iter = srcTables.iterator(); iter.hasNext();) {
                allColumns.addAll(((SourceTable) iter.next()).getColumnList());
            }
            allColumns.addAll(this.targetTable.getColumnList());
        } catch (Exception e) {
            // ignore
        }

        initializeModel(allColumns, groupByColumns);
        initGui();
        ArrayList validColumns = new ArrayList();
        Iterator it = groupByColumns.iterator();
        while (it.hasNext()) {
            Object column = it.next();
            if (allColumns.contains(column)) {
                validColumns.add(column);
            }
        }
        groupPanel.setData(validColumns);
    }

    /**
     * Get ordered list
     * 
     * @return List
     */
    public List getOrderedList() {
        return groupPanel.getOrderedList();
    }

    /**
     * Get the customized property value.
     * 
     * @return the property value
     * @exception IllegalStateException when the custom property editor does not contain a
     *            valid property value (and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException {
        SQLGroupBy groupBy = SQLModelObjectFactory.getInstance().createGroupBy(this.getOrderedList(), targetTable);
        return groupBy;
    }

    private void initGui() {
        ButtonActionListener aListener = new ButtonActionListener();
        this.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        String nbBundle1 = mLoc.t("BUND491: Available Group By Columns");
        mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Available Group By Columns"),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        this.add(mainPanel, c);

        // List of source/target columns
        table = new JTable();
        table.setDefaultRenderer(SourceColumn.class, new GroupTableCellRenderer());
        table.setModel(model);
        JScrollPane sPane = new JScrollPane(table);
        mainPanel.add(sPane, BorderLayout.CENTER);

        // Add button panel
        JPanel addButtonPanel = new JPanel();
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
        addButtonPanel.setLayout(fl);
        mainPanel.add(addButtonPanel, BorderLayout.SOUTH);

        // add button
        String nbBundle30 = mLoc.t("BUND485: Add Column/Expression");
        JButton addButton = new JButton(nbBundle30.substring(15));
        addButton.getAccessibleContext().setAccessibleName(nbBundle30.substring(15));
        addButton.setMnemonic(nbBundle30.substring(15).charAt(0));
        addButton.setToolTipText("<html> <table border=0 cellspacing=0 cellpadding=0 ><tr> <td>When target column is added, <br>the mapped expression/column <br>will be used in group by clause</td></tr></table>");
        addButton.setActionCommand("ADD");
        addButton.addActionListener(aListener);
        addButtonPanel.add(addButton);

        // order or remove selected columns panel
        JPanel orderOrRemoveSelectedColumn = new JPanel();
        orderOrRemoveSelectedColumn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Selected Group By Columns"),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        orderOrRemoveSelectedColumn.setLayout(new BorderLayout());
        this.add(orderOrRemoveSelectedColumn, c);

        groupPanel = new GroupPanel("Group Column");
        orderOrRemoveSelectedColumn.add(groupPanel, BorderLayout.CENTER);

        // Add button panel
        JPanel removeButtonPanel = new JPanel();
        removeButtonPanel.setLayout(fl);
        orderOrRemoveSelectedColumn.add(removeButtonPanel, BorderLayout.SOUTH);

        // remove selected column button
        String nbBundle31 = mLoc.t("BUND152: Remove");
        JButton removeButton = new JButton(nbBundle31.substring(15));
        removeButton.setMnemonic(nbBundle31.substring(15).charAt(0));
        removeButton.getAccessibleContext().setAccessibleName(nbBundle31.substring(15));
        removeButton.setActionCommand("REMOVE");
        removeButton.addActionListener(aListener);
        removeButtonPanel.add(removeButton);

        // having panel
        JPanel havingPanel = new JPanel();
        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridwidth = GridBagConstraints.REMAINDER;
        c2.weightx = 1.0;
        c2.weighty = 1.0;
        c2.fill = GridBagConstraints.HORIZONTAL;
        this.add(havingPanel, c2);

        havingPanel.setLayout(fl);
        havingPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Define Having Clause"),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));

        String havingCondition = "";
        if (targetTable.getSQLGroupBy() != null && targetTable.getSQLGroupBy().getHavingCondition() != null) {
            havingCondition = targetTable.getSQLGroupBy().getHavingCondition().getConditionText();
        }
        havingText = new JTextField(havingCondition);
        havingText.setPreferredSize(new Dimension(230, 25));
        havingPanel.add(havingText);

        // having button
        String nbBundle32 = mLoc.t("BUND487: Having...");
        JButton havingButton = new JButton(nbBundle32.substring(15));
        havingButton.setMnemonic(nbBundle32.substring(15).charAt(0));
        havingButton.getAccessibleContext().setAccessibleName(nbBundle32.substring(15));
        havingButton.setActionCommand("Having");
        havingButton.addActionListener(aListener);
        havingPanel.add(havingButton);

    }

    private void initializeModel(List allColumns, Collection groupByColumns) {
        model = new GroupByTableModel();
        Iterator it = allColumns.iterator();
        while (it.hasNext()) {
            Object sColumn = it.next();
            if (!groupByColumns.contains(sColumn)) {
                model.addColumn(sColumn);
            }
        }
    }
}

