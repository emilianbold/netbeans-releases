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
package org.netbeans.modules.iep.editor.ps;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import org.netbeans.modules.iep.editor.designer.JTextFieldFilter;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.tbls.editor.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.tbls.editor.table.MoveableRowTable;
import org.netbeans.modules.tbls.editor.table.NoExpressionDefaultMoveableRowTableModel;
import org.openide.util.NbBundle;

/**
 * InvocationResponsePanel.java
 *
 * Created on November 1, 2006, 1:52 PM
 *
 * @author Bing Lu
 */
public class InvocationResponsePanel extends JPanel implements SharedConstants {

    private static final Logger mLog = Logger.getLogger(InvocationResponsePanel.class.getName());
    private static Set<String> QUANTITY_TYPES = new HashSet<String>();
    

    static {
//        QUANTITY_TYPES.add(SQL_TYPE_TINYINT);
//        QUANTITY_TYPES.add(SQL_TYPE_SMALLINT);
        QUANTITY_TYPES.add(SQL_TYPE_INTEGER);
        QUANTITY_TYPES.add(SQL_TYPE_BIGINT);
//        QUANTITY_TYPES.add(SQL_TYPE_REAL);
//        QUANTITY_TYPES.add(SQL_TYPE_FLOAT);
        QUANTITY_TYPES.add(SQL_TYPE_DOUBLE);
        //QUANTITY_TYPES.add(SQL_TYPE_DECIMAL);
//        QUANTITY_TYPES.add(SQL_TYPE_NUMERIC);
        QUANTITY_TYPES.add(SQL_TYPE_DATE);
        QUANTITY_TYPES.add(SQL_TYPE_TIMESTAMP);
    //removed time see http://www.netbeans.org/issues/show_bug.cgi?id=149295
//        QUANTITY_TYPES.add(SQL_TYPE_TIME);
    }
    private static DefaultCellEditor mCellEditorNumeric;
    private static DefaultCellEditor mCellEditorANU;
    private static DefaultCellEditor mCellEditorAny;
    private static DefaultCellEditor mCellEditorSqlType;
    private static JTextField mTextFieldNumeric;
    private static JTextField mTextFieldANU;
    private static JTextField mTextFieldAny;
    private static JComboBox mComboBoxSqlType;
    private static Vector<String> mSqlType;
    private OperatorComponent mComponent;
    private DefaultMoveableRowTableModel mTableModel;
    private MoveableRowTable mTable;
    private SelectPanelTableCellRenderer spTCRenderer;

    public InvocationResponsePanel(OperatorComponent component) {
        mComponent = component;

        mTextFieldANU = new JTextField();
//        if(Locale.getDefault() == Locale.ENGLISH) {
        mTextFieldANU.setDocument(JTextFieldFilter.newAlphaNumericUnderscore(mTextFieldANU));
//        } 

        mCellEditorANU = new DefaultCellEditor(mTextFieldANU);

        mTextFieldNumeric = new JTextField();
        mTextFieldNumeric.setDocument(JTextFieldFilter.newNumeric(mTextFieldNumeric));
        mCellEditorNumeric = new DefaultCellEditor(mTextFieldNumeric);

        mTextFieldAny = new JTextField();
        mCellEditorAny = new DefaultCellEditor(mTextFieldAny);

        mSqlType = new Vector<String>();
        mSqlType.add("");
        for (int i = 0; i < SQL_TYPE_NAMES.length; i++) {
            mSqlType.add(SQL_TYPE_NAMES[i]);
        }
        mComboBoxSqlType = new JComboBox(mSqlType);
        mComboBoxSqlType.addItemListener(new SQLTypesItemListener());
        mCellEditorSqlType = new DefaultCellEditor(mComboBoxSqlType);

        initComponents();
    }

    private DefaultMoveableRowTableModel createTableModel() {
        return new NoExpressionDefaultMoveableRowTableModel();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel topPane = new JPanel();
        topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPane.setLayout(new BorderLayout(5, 5));
        add(topPane, BorderLayout.CENTER);
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(5, 5));

        mTableModel = createTableModel();
        mTable = new MoveableRowTable(mTableModel);
        mTable.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InvocationResponsePanel.class, "ACSN_InvocationResponsePanel_Table"));
        mTable.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InvocationResponsePanel.class, "ACSD_InvocationResponsePanel_Table"));


        Vector<Vector<String>> data = new Vector<Vector<String>>();
        try {
            SchemaComponent outputSchema = mComponent.getOutputSchema();

            if (outputSchema != null) {
                List<SchemaAttribute> attrs = outputSchema.getSchemaAttributes();
                List<String> responseAttributeList = mComponent.getStringList(PROP_RESPONSE_ATTRIBUTE_LIST);
                for (int i = 0; i < attrs.size(); i++) {
                    Vector<String> r = new Vector<String>();
                    SchemaAttribute sa = attrs.get(i);
                    String attributeName = sa.getAttributeName();
                    if (!responseAttributeList.contains(attributeName)) {
                        continue;
                    }
                    String attributeType = sa.getAttributeType();
                    String attributeSize = sa.getAttributeSize();
                    String attributeScale = sa.getAttributeScale();
                    String attributeComment = sa.getAttributeComment();

                    if (attributeName != null) {
                        r.add(attributeName);
                    } else {
                        r.add("");
                    }

                    if (attributeType != null) {
                        r.add(attributeType);
                    } else {
                        r.add("");
                    }

                    if (attributeSize != null) {
                        r.add(attributeSize);
                    } else {
                        r.add("");
                    }

                    if (attributeScale != null) {
                        r.add(attributeScale);
                    } else {
                        r.add("");
                    }

                    if (attributeComment != null) {
                        r.add(attributeComment);
                    } else {
                        r.add("");
                    }

                    data.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Vector<String> colTitle = new Vector<String>();
        colTitle.add(NbBundle.getMessage(InvocationAttributePanel.class, "InvocationAttributePanel.ATTRIBUTE_NAME"));
        colTitle.add(NbBundle.getMessage(InvocationAttributePanel.class, "InvocationAttributePanel.DATA_TYPE"));
        colTitle.add(NbBundle.getMessage(InvocationAttributePanel.class, "InvocationAttributePanel.SIZE"));
        colTitle.add(NbBundle.getMessage(InvocationAttributePanel.class, "InvocationAttributePanel.SCALE"));
        colTitle.add(NbBundle.getMessage(InvocationAttributePanel.class, "InvocationAttributePanel.COMMENT"));
        mTableModel.setDataVector(data, colTitle);
        TableColumnModel tcm = mTable.getColumnModel();
        spTCRenderer = new SelectPanelTableCellRenderer();
        try {
            tcm.getColumn(0).setCellEditor(mCellEditorANU);
            tcm.getColumn(1).setCellEditor(mCellEditorSqlType);
            tcm.getColumn(2).setCellEditor(mCellEditorNumeric);
            tcm.getColumn(3).setCellEditor(mCellEditorNumeric);
            tcm.getColumn(4).setCellEditor(mCellEditorAny);

            // setting up renderer
            tcm.getColumn(0).setCellRenderer(spTCRenderer);
            tcm.getColumn(1).setCellRenderer(spTCRenderer);
            tcm.getColumn(2).setCellRenderer(spTCRenderer);
            tcm.getColumn(3).setCellRenderer(spTCRenderer);
            tcm.getColumn(4).setCellRenderer(spTCRenderer);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        mTable.setPreferredScrollableViewportSize(new Dimension(600, 200));
        pane.add(new JScrollPane(mTable), BorderLayout.CENTER);
        JPanel cp = new JPanel();
        cp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        cp.setLayout(new GridLayout(1, 4, 10, 10));
        String lbl = NbBundle.getMessage(InvocationResponsePanel.class, "InvocationResponsePanel.ADD_ATTRIBUTE");
        JButton btnAdd = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(btnAdd, lbl);
        btnAdd.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InvocationResponsePanel.class, "ACSD_InvocationResponsePanel.ADD_ATTRIBUTE"));
        btnAdd.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                TableCellEditor editor = mTable.getCellEditor();
                if (editor != null) {
                    editor.stopCellEditing();
                }

                mTableModel.addRow(new Object[]{"", "", "", "", "", ""});
                int rcount = mTable.getRowCount();
                mTable.setRowSelectionInterval(rcount - 1, rcount - 1);
                //Table request's focus, so the selection is visible.
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        mTable.requestFocus();
                    }
                });
            }
        });
        lbl = NbBundle.getMessage(InvocationResponsePanel.class, "InvocationResponsePanel.DELETE");
        JButton btnDel = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(btnDel, lbl);
        btnDel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InvocationResponsePanel.class, "ACSD_InvocationResponsePanel.DELETE"));
        btnDel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                TableCellEditor editor = mTable.getCellEditor();
                if (editor != null) {
                    editor.stopCellEditing();
                }

                int r[] = mTable.getSelectedRows();
                int firstSelectedRow = 0;
                if (r != null && r.length > 0) {
                    Arrays.sort(r);
                    firstSelectedRow = r[0];
                    for (int i = r.length - 1; i >= 0; i--) {
                        mTableModel.removeRow(r[i]);
                    }
                }
                int rcount = mTable.getRowCount();
                if (rcount > 0) {
                    if (firstSelectedRow < rcount) {
                        mTable.setRowSelectionInterval(firstSelectedRow, firstSelectedRow);
                    } else {
                        if (firstSelectedRow == rcount) {
                            mTable.setRowSelectionInterval(firstSelectedRow - 1, firstSelectedRow - 1);
                        }
                    }
                    // Table request's focus, so the selection is visible.
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            mTable.requestFocus();
                        }
                    });
                }
            }
        });
        lbl = NbBundle.getMessage(InvocationResponsePanel.class, "InvocationResponsePanel.MOVE_UP");
        JButton btnMoveUp = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(btnMoveUp, lbl);
        btnMoveUp.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InvocationResponsePanel.class, "ACSD_InvocationResponsePanel.MOVE_UP"));
        btnMoveUp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                TableCellEditor editor = mTable.getCellEditor();
                if (editor != null) {
                    editor.stopCellEditing();
                }

                int r = mTable.getSelectedRow();
                if (r > 0) {
                    mTableModel.moveRow(r, r, r - 1);
                    mTable.setRowSelectionInterval(r - 1, r - 1);
                    // Table request's focus, so the selection is visible.
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            mTable.requestFocus();
                        }
                    });

                }
            }
        });
        lbl = NbBundle.getMessage(InvocationResponsePanel.class, "InvocationResponsePanel.MOVE_DOWN");
        JButton btnMoveDown = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(btnMoveDown, lbl);
        btnMoveDown.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InvocationResponsePanel.class, "ACSD_InvocationResponsePanel.MOVE_DOWN"));
        btnMoveDown.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                TableCellEditor editor = mTable.getCellEditor();
                if (editor != null) {
                    editor.stopCellEditing();
                }

                int r = mTable.getSelectedRow();
                int rcount = mTable.getRowCount();
                if (r >= 0 && rcount - 1 > r) {
                    mTableModel.moveRow(r, r, r + 1);
                    mTable.setRowSelectionInterval(r + 1, r + 1);
                    // Table request's focus, so the selection is visible.
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            mTable.requestFocus();
                        }
                    });
                }
            }
        });
        Dimension s = btnAdd.getPreferredSize();
        int maxW = s.width;
        int maxH = s.height;
        s = btnDel.getPreferredSize();
        maxW = Math.max(s.width, maxW);
        maxH = Math.max(s.height, maxH);
        s = btnMoveUp.getPreferredSize();
        maxW = Math.max(s.width, maxW);
        maxH = Math.max(s.height, maxH);
        s = btnMoveDown.getPreferredSize();
        maxW = Math.max(s.width, maxW);
        maxH = Math.max(s.height, maxH);
        s = new Dimension(maxW, maxH);
        btnAdd.setPreferredSize(s);
        btnDel.setPreferredSize(s);
        btnMoveUp.setPreferredSize(s);
        btnMoveDown.setPreferredSize(s);
        cp.add(btnAdd);
        cp.add(btnDel);
        cp.add(btnMoveUp);
        cp.add(btnMoveDown);
//        cp.add(Box.createHorizontalGlue());
        pane.add(cp, BorderLayout.SOUTH);

        topPane.add(pane, BorderLayout.CENTER);
    }

    public DefaultMoveableRowTableModel getTableModel() {
        return mTableModel;
    }

    public List<SchemaAttribute> getResponseAttributes() {
        IEPModel model = mComponent.getModel();
        List<SchemaAttribute> attributeList = new ArrayList<SchemaAttribute>();
        Vector r = mTableModel.getDataVector();
        for (int i = 0, I = r.size(); i < I; i++) {
            Vector c = (Vector) r.elementAt(i);
            if (!(c.elementAt(1) == null) && !(c.elementAt(1).equals(""))) {
                SchemaAttribute sa = model.getFactory().createSchemaAttribute(model);
                String name = (String) c.elementAt(1);
                sa.setName(name);
                sa.setTitle(name);

                //name
                sa.setAttributeName(name);
                //type
                sa.setAttributeType((String) c.elementAt(2));
                //size
                sa.setAttributeSize((String) c.elementAt(3));
                //scale
                sa.setAttributeScale((String) c.elementAt(4));
                //comment
                sa.setAttributeComment((String) c.elementAt(5));

                attributeList.add(sa);

            }
        }
        return attributeList;
    }

    public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
        //stop any cell editing IZ129687
        TableCellEditor editor = mTable.getCellEditor();
        if (editor != null) {
            editor.stopCellEditing();
        }

        int rowCount = mTableModel.getRowCount();
        // at least one attribute must be defined
        if (rowCount < 1) {
            String msg = NbBundle.getMessage(InvocationResponsePanel.class,
                    "InvocationResponsePanel.At_least_one_attribute_must_be_defined");
            throw new PropertyVetoException(msg, evt);
        }

        // for each attribute: name and type must be defined
        for (int i = 0; i < rowCount; i++) {
            String colName = (String) mTableModel.getValueAt(i, 1);
            if (colName == null || colName.trim().equals("")) {
                String msg = NbBundle.getMessage(InvocationResponsePanel.class,
                        "InvocationResponsePanel.Attribute_name_must_be_defined");
                throw new PropertyVetoException(msg, evt);
            }
            String colType = (String) mTableModel.getValueAt(i, 2);
            if (colType == null || colType.trim().equals("")) {
                String msg = NbBundle.getMessage(InvocationResponsePanel.class,
                        "InvocationResponsePanel.Attribute_type_must_be_defined");
                throw new PropertyVetoException(msg, evt);
            }
        }
    }

    public void store() {
        IEPModel model = mComponent.getModel();
        List<String> nameList = new ArrayList<String>();
        Vector r = mTableModel.getDataVector();
        for (int i = 0, I = r.size(); i < I; i++) {
            Vector c = (Vector) r.elementAt(i);
            nameList.add((String)c.elementAt(0));
        }
        model.startTransaction();
        mComponent.setStringList(PROP_RESPONSE_ATTRIBUTE_LIST, nameList);
        model.endTransaction();
    }

    class SQLTypesItemListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            String item = (String) e.getItem();
            int row = mTable.getEditingRow();
            if (row == -1) {
                return;
            }

            if (item.equals(SQL_TYPE_VARCHAR)) {
                if (row != -1) {
                    String size = "";
                    size = (String) mTableModel.getValueAt(row, 2);
                    if (size == null || size.trim().equals("")) {
                        //by default set varchar data types size to 100
                        mTableModel.setValueAt("100", row, 2);
                    }
                }
            } else {
                //by default remove size and let user type in if
                //required.
                mTableModel.setValueAt("", row, 2);
            }

            mTableModel.fireTableDataChanged();
        }
    }
}