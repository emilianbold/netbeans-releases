/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.designview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.wlm.model.api.TDeadlineExpr;
import org.netbeans.modules.wlm.model.api.TDurationExpr;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.TTimeout;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.editor.designview.components.ExUtils;
import org.netbeans.modules.worklist.editor.designview.components.LinkButton;
import org.netbeans.modules.worklist.editor.designview.components.TextFieldEditor;
import org.netbeans.modules.worklist.editor.designview.components.TitledPanel;
import org.netbeans.modules.worklist.editor.nodes.TimeoutNode;
import org.netbeans.modules.worklist.editor.nodes.TimeoutsNode;
import org.netbeans.modules.worklist.editor.nodes.WLMNodeType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class TimeoutsPanel extends DesignViewPanel  implements Widget,
        FocusListener, ListSelectionListener
{
    private JScrollPane timeoutsScrollPane;
    private JComponent timeoutsHeader;
    
    private LinkButton addDurationButton;
    private LinkButton addDeadlineButton;
    private LinkButton removeButton;
    
    private Action addDurationAction;
    private Action addDeadlineAction;
    private Action removeTimeoutAction;
    
    private TimeoutsModel timeoutsModel;
    private JTable timeoutsTable;
    
    private TitledPanel titledPanel;

    private Widget widgetParent;

    public TimeoutsPanel(Widget widgetParent, DesignView designView) {
        super(designView);

        ExUtils.setA11Y(this, "TimeoutsPanel"); // NOI18N

        setLayout(new BorderLayout());
        //setBackground(Color.WHITE);
        setBorder(null);
        setOpaque(false);

        this.widgetParent = widgetParent;

        JComboBox typeEditorComponent = new JComboBox(new String[] {
            DURATION_TYPE, 
            DEADLINE_TYPE
        });
        
        timeoutsModel = new TimeoutsModel();
        timeoutsTable = new JTable(timeoutsModel);
        timeoutsTable.setRowHeight(20);
        timeoutsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timeoutsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        timeoutsTable.getSelectionModel().addListSelectionListener(this);
        timeoutsTable.addFocusListener(this);
        ExUtils.setA11Y(timeoutsTable, TimeoutsPanel.class,
                "TimeoutsTable"); // NOI18N
        
        TableColumnModel columnModel = timeoutsTable.getColumnModel();
        TableColumn typeColumn = columnModel.getColumn(0);
        typeColumn.setCellEditor(new DefaultCellEditor(typeEditorComponent));
        typeColumn.setMinWidth(120);
        typeColumn.setMaxWidth(120);
        typeColumn.setResizable(false);
        
        timeoutsScrollPane = new JScrollPane(timeoutsTable);
        timeoutsScrollPane.getViewport().setBackground(Color.WHITE);
                
        addDeadlineAction = new AddDeadlineAction();
        addDurationAction = new AddDurationAction();
        removeTimeoutAction = new RemoveTimeoutAction();
        
        addDurationButton = new LinkButton(addDurationAction);
        addDurationButton.addFocusListener(this);
        ExUtils.setA11Y(addDurationButton, TimeoutsPanel.class,
                "AddDurationButton"); // NOI18N

        addDeadlineButton = new LinkButton(addDeadlineAction);
        addDeadlineButton.addFocusListener(this);
        ExUtils.setA11Y(addDeadlineButton, TimeoutsPanel.class,
                "AddDeadlineButton"); // NOI18N

        removeButton = new LinkButton(removeTimeoutAction);
        removeButton.addFocusListener(this);
        ExUtils.setA11Y(addDeadlineButton, TimeoutsPanel.class,
                "RemoveTimeoutButton"); // NOI18N
        
        timeoutsHeader = Box.createHorizontalBox();
        timeoutsHeader.setOpaque(false);
        timeoutsHeader.add(Box.createHorizontalGlue()).setFocusable(false);
        timeoutsHeader.add(addDurationButton);
        timeoutsHeader.add(addDeadlineButton);
        timeoutsHeader.add(removeButton);
        ExUtils.setA11Y(timeoutsHeader, TimeoutsPanel.class,
                "TimeoutsTableHeader"); // NOI18N
        
        add(timeoutsHeader, BorderLayout.NORTH);
        add(timeoutsScrollPane, BorderLayout.CENTER);
        
        processWLMModelChanged();
    }
    
    public void processWLMModelChanged() {
        timeoutsModel.processWLMModelChanged();
    }
    
    public JComponent getView() {
        if (titledPanel == null) {
            titledPanel = new TitledPanel(getMessage("LBL_TIMEOUTS"), // NOI18N
                    this, 0);
        }
        return titledPanel;
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension size = timeoutsHeader.getPreferredSize();
        size.height += 70;
        return size;
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    public Widget getWidget(int index) {
        return timeoutsModel.get(index);
    }

    public int getWidgetCount() {
        return timeoutsModel.getRowCount();
    }

    public Node getWidgetNode() {
        return new TimeoutsNode(getTask(), Children.LEAF, getNodeLookup());
    }

    public void requestFocusToWidget() {
        getDesignView().showBasicPropertiesTab();
    }

    public Widget getWidgetParent() {
        return widgetParent;
    }

    public WLMComponent getWidgetWLMComponent() {
        return getTask();
    }

    public WLMNodeType getWidgetNodeType() {
        return WLMNodeType.TIMEOUTS;
    }

    public void focusGained(FocusEvent e) {
        Object source = e.getSource();
        if (source == addDeadlineButton
                || source == addDurationButton
                || source == removeButton
                || source == timeoutsHeader
                || source == timeoutsScrollPane)
        {
            selectWidget(this);
        } else if (source == timeoutsTable) {
            int row = timeoutsTable.getSelectedRow();
            if (row >= 0) {
                selectWidget(timeoutsModel.get(row));
            } else {
                selectWidget(this);
            }
        }
    }

    public void focusLost(FocusEvent e) {
        // do nothing
    }

    // List selection listener
    public void valueChanged(ListSelectionEvent e) {
        if (timeoutsTable.hasFocus() || timeoutsTable.isEditing()) {
            int row = timeoutsTable.getSelectedRow();
            if (row >= 0) {
                selectWidget(timeoutsModel.get(row));
            } else {
                selectWidget(this);
            }
        }
    }

    
    private class TimeoutsModel extends AbstractTableModel {
        private List<TimeoutRow> rows;

        public TimeoutsModel() {
            rows = new ArrayList<TimeoutRow>();
        }
        
        public int getRowCount() {
            return rows.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            TimeoutRow row = rows.get(rowIndex);
            
            if (columnIndex == 0) {
                return (row.isDuration()) 
                        ? DURATION_TYPE 
                        : DEADLINE_TYPE;
            } 
            
            return row.getExpression();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex < 0 || rowIndex >= getRowCount()) {
                return;
            }
            
            TimeoutRow row = rows.get(rowIndex);
            TTimeout timeout = row.getTimeoutElement();
            
            boolean durationFlag;
            String expression;
            
            if (columnIndex == 0) {
                durationFlag = !DEADLINE_TYPE.equals(aValue);
                expression = TextFieldEditor.textToXPath(row.getExpression());
            } else {
                durationFlag = row.isDuration();
                expression = TextFieldEditor.textToXPath(aValue.toString());
            }
            
            WLMModel model = getModel();
            if (model.startTransaction()) {
                try {
                    TDurationExpr duration = timeout.getDuration();
                    TDeadlineExpr deadline = timeout.getDeadline();
                    
                    if (durationFlag) {
                        if (deadline != null) {
                            timeout.removeDeadline(deadline);
                            deadline = null;
                        }
                        
                        if (duration == null) {
                            duration = model.getFactory().createDuration(model);
                            timeout.setDuration(duration);
                        }
                        
                        duration.setContent(expression);
                    } else {
                        if (duration != null) {
                            timeout.removeDuration(duration);
                            duration = null;
                        }
                        
                        if (deadline == null) {
                            deadline = model.getFactory().createDeadline(model);
                            timeout.setDeadline(deadline);
                        }
                        
                        deadline.setContent(expression);
                    }
                } finally {
                    model.endTransaction();
                }
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return getMessage("CLM_TYPE"); // NOI18N
            }
            return getMessage("CLM_TIMEOUT_EXPRESSION"); // NOI18N
        }
        
        public void clear() {
            rows.clear();
        }
        
        public void add(TimeoutRow row) {
            rows.add(row);
        }
        
        public void remove(TimeoutRow row) {
            rows.remove(row);
        }
   
        public TimeoutRow get(int row) {
            return rows.get(row);
        }
        
        void processWLMModelChanged() {
            TTask task = getTask();
            List<TTimeout> timeoutsList = (task == null) ? null :
                    task.getTimeouts();
            
            if (timeoutsList != null) {
                Set<TTimeout> newTimeouts = new HashSet<TTimeout>(timeoutsList);
                
                for (int i = rows.size() - 1; i >= 0; i--) {
                    TimeoutRow timeoutRow = rows.get(i);
                    TTimeout timeout = timeoutRow.getTimeoutElement();
                    
                    newTimeouts.remove(timeout);
                    
                    if (timeoutsList.contains(timeout)) {
                        timeoutRow.update();
                        fireTableRowsUpdated(i, i);
                    } else {
                        rows.remove(i);
                        fireTableRowsDeleted(i, i);
                    }
                }
                
                for (int i = 0; i < timeoutsList.size(); i++) {
                    TTimeout timeout = timeoutsList.get(i);
                    if (newTimeouts.contains(timeout)) {
                        rows.add(i, new TimeoutRow(timeout));
                        fireTableRowsInserted(i, i);
                    }
                }
            } else {
                int rowCount = rows.size();
                rows.clear();
                
                if (rowCount > 0) {
                    fireTableRowsDeleted(0, rowCount - 1);
                }
            }            
        }
    }
    
    private class TimeoutRow implements Widget {
        private TTimeout timeout;
        private boolean durationFlag;
        private String expression;
        
        TimeoutRow(TTimeout timeout) {
            this.timeout = timeout;
            update();
        }
        
        public boolean isDuration() {
            return durationFlag;
        }
        
        public TTimeout getTimeoutElement() {
            return timeout;
        }
        
        public String getExpression() {
            return expression;
        }
    
        public void update() {
            TDeadlineExpr deadline = timeout.getDeadline();
            TDurationExpr duration = timeout.getDuration();
            
            if (duration != null) {
                expression = TextFieldEditor.xPathToText(duration.getContent());
                durationFlag = true;
            } else if (deadline != null) {
                expression = TextFieldEditor.xPathToText(deadline.getContent());
                durationFlag = false;
            } else {
                durationFlag = true;
                expression = ""; // NOI18N
            }
        }

        public Widget getWidget(int index) {
            throw new IndexOutOfBoundsException("Leaf widget has no children");
        }

        public int getWidgetCount() {
            return 0;
        }

        public Node getWidgetNode() {
            return new TimeoutNode(timeout, Children.LEAF, getNodeLookup());
        }

        public void requestFocusToWidget() {
            getDesignView().showBasicPropertiesTab();

            int index = timeoutsModel.rows.indexOf(this);

            if (index >= 0) {
                timeoutsTable.getSelectionModel().setSelectionInterval(index,
                        index);
            }

            timeoutsTable.requestFocusInWindow();
        }

        public Widget getWidgetParent() {
            return TimeoutsPanel.this;
        }

        public WLMComponent getWidgetWLMComponent() {
            return timeout;
        }

        public WLMNodeType getWidgetNodeType() {
            return WLMNodeType.TIMEOUT;
        }
    }
    
    private static final String DURATION_TYPE 
            = getMessage("RDB_DURATION"); // NOI18N
    private static final String DEADLINE_TYPE 
            = getMessage("RDB_DEADLINE"); // NOI18N
    
    private class AddDurationAction extends AbstractAction {
        public AddDurationAction() {
            super(getMessage("LBL_ADD_DURATION")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent e) {
            WLMModel model = getModel();
            TTask task = getTask();
            
            if (model.startTransaction()) {
                TTimeout timeout = null;
                
                try {
                    TDurationExpr duration = model.getFactory()
                            .createDuration(model);
                    timeout = model.getFactory().createTimeout(model);
                    timeout.setDuration(duration);
                    task.addTimeOut(timeout);
                } finally {
                    model.endTransaction();
                }
            }
        }
    }
    
    private class AddDeadlineAction extends AbstractAction {
        public AddDeadlineAction() {
            super(getMessage("LBL_ADD_DEADLINE")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent e) {
            WLMModel model = getModel();
            TTask task = getTask();
            
            if (model.startTransaction()) {
                TTimeout timeout = null;
                
                try {
                    TDeadlineExpr deadline = model.getFactory()
                            .createDeadline(model);
                    timeout = model.getFactory().createTimeout(model);
                    timeout.setDeadline(deadline);
                    task.addTimeOut(timeout);
                } finally {
                    model.endTransaction();
                }
            }
        }
    }

    private class RemoveTimeoutAction extends AbstractAction {
        RemoveTimeoutAction() {
            super(getMessage("LBL_REMOVE_TIMEOUT")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            int rowIndex = timeoutsTable.getSelectedRow();
            if (rowIndex < 0 || rowIndex >= timeoutsModel.getRowCount()) {
                return;
            }
            
            TimeoutRow row = timeoutsModel.get(rowIndex);
            
            TTimeout timeout = row.getTimeoutElement();
            
            WLMModel model = getModel();
            TTask task = model.getTask();
            
            if (model.startTransaction()) {
                try {
                    task.removeTimeOut(timeout);
                } finally {
                    model.endTransaction();
                }
            }
        }
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(TimeoutsPanel.class, key);
    }               
}
