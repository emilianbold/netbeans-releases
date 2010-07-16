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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.netbeans.modules.iep.editor.designer.JTextFieldFilter;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.tbls.editor.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.tbls.editor.table.ExpressionDefaultMoveableRowTableModel;
import org.netbeans.modules.tbls.editor.table.MoveableRowTable;
import org.netbeans.modules.tbls.editor.table.NoExpressionDefaultMoveableRowTableModel;
import org.netbeans.modules.tbls.model.ArrayHashMap;
import org.netbeans.modules.tbls.model.ListMap;
import org.openide.util.NbBundle;

/**
 * SelectPanel.java
 *
 * Created on November 1, 2006, 1:52 PM
 *
 * @author Bing Lu
 */
public class SelectPanel extends JPanel implements SharedConstants {
    private static final Logger mLog = Logger.getLogger(SelectPanel.class.getName());
    
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
    
    
    private static int mAcceptableActions = DnDConstants.ACTION_COPY;
    private static DefaultCellEditor mCellEditorNumeric;
    private static DefaultCellEditor mCellEditorANU;
    private static DefaultCellEditor mCellEditorAny;
    private static DefaultCellEditor mCellEditorSqlType;
    private static JTextField mTextFieldNumeric;
    private static JTextField mTextFieldANU;
    private static JTextField mTextFieldAny;
    private static SmartTextField mTextFieldExpression;
    private static AttributeDropNotificationListener mTextFieldExpressionDropHandler;
    private static JComboBox mComboBoxSqlType;
    private static Vector<String> mSqlType;
   
    private OperatorComponent mComponent;
    private DefaultMoveableRowTableModel mTableModel;
    private MoveableRowTable mTable;
    private SmartCellEditor mCellEditorExpression;
    private ListMap mColumnMetadataTable;
    private DropTarget mDropTarget;

    private String mTitle;
    private boolean mReadOnly;
    private boolean mHasExpressionColumn;
    
    private SelectPanelTableCellRenderer  spTCRenderer;
    
    private EventListenerList mListernerList = new EventListenerList();
    
    private void initialize() {
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
        
        boolean truncateColumn = false;
        mTextFieldExpressionDropHandler = new TextFieldExpressionDropNotificationHandler();
        
        mTextFieldExpression = new SmartTextField(truncateColumn, mTextFieldExpressionDropHandler);
        
        mSqlType = new Vector<String>();
        mSqlType.add("");
        for(int i = 0; i < SQL_TYPE_NAMES.length; i++)
            mSqlType.add(SQL_TYPE_NAMES[i]);
        
        mComboBoxSqlType = new JComboBox(mSqlType);
        mComboBoxSqlType.addItemListener(new SQLTypesItemListener());
        mCellEditorSqlType = new DefaultCellEditor(mComboBoxSqlType);
        
        initAttributeMetadataTables();
        mCellEditorExpression = new SmartCellEditor(mTextFieldExpression);
        initComponents();        
    }
    
    public SelectPanel(String title, OperatorComponent component, boolean hasExpressionColumn, boolean readOnly) {
        mTitle = title;
        mComponent = component;
        mHasExpressionColumn = hasExpressionColumn;
        mReadOnly = readOnly;
        initialize();
    }
    
    public SelectPanel(OperatorComponent component) {
        mComponent = component;
        try {
            boolean isSchemaOwner = component.getBoolean(PROP_IS_SCHEMA_OWNER);
            String inputType = component.getInputType().getType();
            mReadOnly = !isSchemaOwner;
            mHasExpressionColumn = isSchemaOwner && !inputType.equals(IO_TYPE_NONE);
            if (mHasExpressionColumn) {
                mTitle = NbBundle.getMessage(DefaultCustomEditor.class, "DefaultCustomEditor.SELECT");
            } else {
                mTitle = null;
            }
        } catch (Exception e) {
            mHasExpressionColumn = false;
        }
        initialize();
    }
    
    public void addAttributeDropNotificationListener(AttributeDropNotificationListener listener) {
        this.mListernerList.add(AttributeDropNotificationListener.class, listener);
    }
    
    public void removeAttributeDropNotificationListener(AttributeDropNotificationListener listener) {
        this.mListernerList.remove(AttributeDropNotificationListener.class, listener);
    }
    
    protected DefaultMoveableRowTableModel createTableModel() {
        if(mHasExpressionColumn) {
            return new ExpressionDefaultMoveableRowTableModel();
        } else {
            return new NoExpressionDefaultMoveableRowTableModel();
        }
        
    }
    
    protected boolean isAddEmptyRow() {
        return true;
    }
    
    private void initAttributeMetadataTables() {
        String opName = "";
        mColumnMetadataTable = new ArrayHashMap();
        try {
            opName = mComponent.getString(PROP_NAME);
            List<OperatorComponent> inputOperators = mComponent.getInputOperatorList();
            Iterator<OperatorComponent> it = inputOperators.iterator();
            
            while(it.hasNext()) {
                OperatorComponent input = it.next();
                if(input != null) {
                    String inputName = input.getString(PROP_NAME);
                    ListMap lm = new ArrayHashMap();
                    mColumnMetadataTable.put(inputName, lm);
                    SchemaComponent outputSchema = input.getOutputSchema();
                    if(outputSchema != null) {
                        List<SchemaAttribute> attrs = outputSchema.getSchemaAttributes();
                        Iterator<SchemaAttribute> attrsIt = attrs.iterator();
                        while(attrsIt.hasNext()) {
                            SchemaAttribute sa = attrsIt.next();
                            lm.put(sa.getAttributeName(), sa);
                        }
                    }
                }
            }
            
            List<OperatorComponent> tableOperators = mComponent.getStaticInputList();
            Iterator<OperatorComponent> itTable = tableOperators.iterator();
            
            while(itTable.hasNext()) {
                OperatorComponent tableInput = itTable.next();
                    if(tableInput != null) {
                        String inputName = tableInput.getString(PROP_NAME);
                        ListMap lm = new ArrayHashMap();
                        mColumnMetadataTable.put(inputName, lm);
                        SchemaComponent outputSchema = tableInput.getOutputSchema();
                        if(outputSchema != null) {
                            List<SchemaAttribute> attrs = outputSchema.getSchemaAttributes();
                            Iterator<SchemaAttribute> attrsIt = attrs.iterator();
                            while(attrsIt.hasNext()) {
                                SchemaAttribute sa = attrsIt.next();
                                lm.put(sa.getAttributeName(), sa);
                            }
                        }
                    }
            }
        } catch(Exception e) {
            mLog.log(Level.SEVERE,
                    NbBundle.getMessage(InputSchemaTreeModel.class,
                    "SelectPanel.FAIL_TO_BUILD_COLUMN_TABLES_FOR_OPERATOR",
                    opName),
                    e);
        }
        
    }
    
    protected void setCustomTableHeader(JTable table, TableModel tableModel) {
	// sub classes will over ride to have specific behavior to have a 
	// custom TableHeader
    }
    
    protected boolean needCustomHeader() {
	// if a sub class needs to have specific table header 
	// will return true and subsequently setCustomTableHeader() method
	// will be called.
	return false;
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        if (mTitle != null) {
            setBorder(BorderFactory.createTitledBorder(mTitle));
        }
        JPanel topPane = new JPanel();
        topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPane.setLayout(new BorderLayout(5, 5));
        add(topPane, BorderLayout.CENTER);
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(5, 5));
        
        mTableModel = createTableModel();
        if (mHasExpressionColumn && needCustomHeader()) {
            // condition for InvokeStream operator as this needs to 
            // add a custom MultiRowTableHeader. Model is set after
            // the TableColumnModel is added.
            mTable = new MoveableRowTable();
        } else if (mReadOnly) {
            mTable = new MoveableRowTable(mTableModel) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
                @Override
                public void dragGestureRecognized(DragGestureEvent dge) {
                    return;
                }
            };
        } else {
            mTable = new MoveableRowTable(mTableModel);
        }
        
        mTable.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SelectPanel.class, "ACSN_SelectPanel_Table"));
        mTable.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SelectPanel.class, "ACSD_SelectPanel_Table"));
        
        mDropTarget = new DropTarget(mTable, new MyDropTargetAdapter());
        Vector<Vector<String>> data = new Vector<Vector<String>>();
        try {
            SchemaComponent outputSchema = mComponent.getOutputSchema();
            
            if(outputSchema != null) {
                if (mHasExpressionColumn) {
                    List<SchemaAttribute> attrs = outputSchema.getSchemaAttributes();
                    Iterator<SchemaAttribute> attrIt = attrs.iterator();
                    List<String> fromColumnList = mComponent.getStringList(PROP_FROM_COLUMN_LIST);
                    int j = 0;
                    while(attrIt.hasNext()) {
                        Vector<String> r = new Vector<String>();
                        r.add(fromColumnList.get(j));
                        
                        SchemaAttribute sa = attrIt.next();
                        String attributeName = sa.getAttributeName();
                        String attributeType = sa.getAttributeType();
                        String attributeSize = sa.getAttributeSize();
                        String attributeScale = sa.getAttributeScale();
                        String attributeComment = sa.getAttributeComment();
                         
                         if(attributeName != null) {
                             r.add(attributeName);
                         } else {
                             r.add("");
                         }
    
                        if(attributeType != null) {
                             r.add(attributeType);                         
                         } else {
                             r.add("");
                         }

                        if(attributeSize != null) {
                            r.add(attributeSize);
                        } else {
                             r.add("");
                        }

                        if(attributeScale != null) {
                            r.add(attributeScale);
                        } else {
                             r.add("");
                        }

                        if(attributeComment != null) {
                            r.add(attributeComment);
                        } else {
                             r.add("");
                        }
                        
                        data.add(r);
                        j++;
                    }
                } else {
                    List<SchemaAttribute> attrs = outputSchema.getSchemaAttributes();
                    Iterator<SchemaAttribute> attrIt = attrs.iterator();
                    while(attrIt.hasNext()) {
                        Vector<String> r = new Vector<String>();
                        
                        SchemaAttribute sa = attrIt.next();
                        String attributeName = sa.getAttributeName();
                        String attributeType = sa.getAttributeType();
                        String attributeSize = sa.getAttributeSize();
                        String attributeScale = sa.getAttributeScale();
                        String attributeComment = sa.getAttributeComment();
                         
                         if(attributeName != null) {
                             r.add(attributeName);
                         } else {
                             r.add("");
                         }
    
                        if(attributeType != null) {
                             r.add(attributeType);                         
                         } else {
                             r.add("");
                         }

                        if(attributeSize != null) {
                            r.add(attributeSize);
                        } else {
                             r.add("");
                        }

                        if(attributeScale != null) {
                            r.add(attributeScale);
                        } else {
                             r.add("");
                        }

                        if(attributeComment != null) {
                            r.add(attributeComment);
                        } else {
                             r.add("");
                        }
                        
                        data.add(r);
                    }
                }
                
                if(data.size() == 0 && isAddEmptyRow()) {
                    Vector<String> r = new Vector<String>();
                    if (mHasExpressionColumn) {
                        r.add("");
                    }
                    r.add("");
                    r.add("");
                    r.add("");
                    r.add("");
                    r.add("");
                    data.add(r);
                }
            } else {
                if(isAddEmptyRow()) {
                    Vector<String> r = new Vector<String>();
                    if (mHasExpressionColumn) {
                        r.add("");
                    }
                    r.add("");
                    r.add("");
                    r.add("");
                    r.add("");
                    r.add("");
                    data.add(r);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        Vector<String> colTitle = new Vector<String>();
        if (mHasExpressionColumn) {
            colTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.EXPRESSION"));
        }
        colTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.ATTRIBUTE_NAME"));
        colTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.DATA_TYPE"));
        colTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.SIZE"));
        colTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.SCALE"));
        colTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.COMMENT"));
        mTableModel.setDataVector(data, colTitle);
        if (mHasExpressionColumn && needCustomHeader()) {
            // give chance to sub class to set custom headers.
            setCustomTableHeader(mTable, mTableModel);
        }
        TableColumnModel tcm = mTable.getColumnModel();
        spTCRenderer = new SelectPanelTableCellRenderer();
        try {
            if (mHasExpressionColumn) {
                mCellEditorExpression.addCellEditorListener(new ExpressionCellEditorListener());
                tcm.getColumn(0).setCellEditor(mCellEditorExpression);
                tcm.getColumn(0).setPreferredWidth(180);
                tcm.getColumn(0).setCellRenderer(spTCRenderer);
            }
            int nameCol = mHasExpressionColumn? 1 : 0;
            tcm.getColumn(nameCol).setCellEditor(mCellEditorANU);
            tcm.getColumn(nameCol + 1).setCellEditor(mCellEditorSqlType);
            //mComboBoxSqlType.addActionListener(new SQLTypeComboBoxActionListener());
            tcm.getColumn(nameCol + 2).setCellEditor(mCellEditorNumeric);
            tcm.getColumn(nameCol + 3).setCellEditor(mCellEditorNumeric);
            tcm.getColumn(nameCol + 4).setCellEditor(mCellEditorAny);
            
            // setting up renderer
            tcm.getColumn(nameCol).setCellRenderer(spTCRenderer);
            tcm.getColumn(nameCol + 1).setCellRenderer(spTCRenderer);
            tcm.getColumn(nameCol + 2).setCellRenderer(spTCRenderer);
            tcm.getColumn(nameCol + 3).setCellRenderer(spTCRenderer);
            tcm.getColumn(nameCol + 4).setCellRenderer(spTCRenderer);
            
            
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        pane.add(new JScrollPane(mTable), BorderLayout.CENTER);
        if (!mReadOnly && isShowButtons()) {
            JPanel cp = new JPanel();
            cp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            cp.setLayout(new GridLayout(1, 4, 10, 10));
            String lbl = NbBundle.getMessage(SelectPanel.class, "SelectPanel.ADD_ATTRIBUTE");
            JButton btnAdd = new JButton();
            org.openide.awt.Mnemonics.setLocalizedText(btnAdd, lbl);
            btnAdd.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SelectPanel.class, "ACSD_SelectPanel.ADD_ATTRIBUTE"));
            
            btnAdd.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    
                    TableCellEditor editor = mTable.getCellEditor();
                    if(editor != null) {
                        editor.stopCellEditing();
                    }
                    
                    mTableModel.addRow(mHasExpressionColumn?
                        new Object[] {"", "", "", "", "", ""} :
                        new Object[] {"", "", "", "", ""});
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
            lbl = NbBundle.getMessage(SelectPanel.class, "SelectPanel.DELETE");
            JButton btnDel = new JButton();
            org.openide.awt.Mnemonics.setLocalizedText(btnDel, lbl);
            btnDel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SelectPanel.class, "ACSD_SelectPanel.DELETE"));
            
            btnDel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    TableCellEditor editor = mTable.getCellEditor();
                    if(editor != null) {
                        editor.stopCellEditing();
                    }
                    
                    int r[] = mTable.getSelectedRows();
                    int firstSelectedRow = 0;
                    if(r != null && r.length > 0) {
                        Arrays.sort(r);
                        firstSelectedRow = r[0];
                        for(int i = r.length - 1; i >= 0; i--)
                            mTableModel.removeRow(r[i]);
                        
                    }
                    int rcount = mTable.getRowCount();
                    if(rcount > 0) {
                        if(firstSelectedRow < rcount) {
                            mTable.setRowSelectionInterval(firstSelectedRow, firstSelectedRow);
                        } else {
                            if(firstSelectedRow == rcount) {
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
            lbl = NbBundle.getMessage(SelectPanel.class, "SelectPanel.MOVE_UP");
            JButton btnMoveUp = new JButton();
            org.openide.awt.Mnemonics.setLocalizedText(btnMoveUp, lbl);
            btnMoveUp.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SelectPanel.class, "ACSD_SelectPanel.MOVE_UP"));
            
            btnMoveUp.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    TableCellEditor editor = mTable.getCellEditor();
                    if(editor != null) {
                        editor.stopCellEditing();
                    }
                    
                    int r = mTable.getSelectedRow();
                    if(r > 0) {
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
            lbl = NbBundle.getMessage(SelectPanel.class, "SelectPanel.MOVE_DOWN");
            JButton btnMoveDown = new JButton();
            org.openide.awt.Mnemonics.setLocalizedText(btnMoveDown, lbl);
            btnMoveDown.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SelectPanel.class, "ACSD_SelectPanel.MOVE_DOWN"));
            
            btnMoveDown.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    
                    TableCellEditor editor = mTable.getCellEditor();
                    if(editor != null) {
                        editor.stopCellEditing();
                    }
                    
                    int r = mTable.getSelectedRow();
                    int rcount = mTable.getRowCount();
                    if(r >= 0 && rcount - 1 > r) {
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
//            cp.add(Box.createHorizontalGlue());
            pane.add(cp, BorderLayout.SOUTH);
        }
        topPane.add(pane, BorderLayout.CENTER);
    }
    
    private void initAccessibility() {
        
    }
    
    public DefaultMoveableRowTableModel getTableModel() {
	return mTableModel;
    }
    
    public MoveableRowTable getTable() {
	return mTable;
    }
   
    public List<SchemaAttribute> getAttributes() {
        List<SchemaAttribute> attributeList = new ArrayList<SchemaAttribute>();
        Vector r = mTableModel.getDataVector();
        IEPModel model = mComponent.getModel();
        int nameCol = mHasExpressionColumn? 1 : 0;
        for (int i = 0, I = r.size(); i < I; i++) {
            Vector c = (Vector) r.elementAt(i);
            if (!(c.elementAt(nameCol) == null) && !(c.elementAt(nameCol).equals(""))) {
                SchemaAttribute sa = model.getFactory().createSchemaAttribute(model);
                String name = (String) c.elementAt(nameCol);
                sa.setName(name);
                sa.setTitle(name);
                
                //name
                sa.setAttributeName(name);
                //type
                sa.setAttributeType((String) c.elementAt(nameCol + 1));
                //size
                sa.setAttributeSize((String) c.elementAt(nameCol + 2));
                //scale
                sa.setAttributeScale((String) c.elementAt(nameCol + 3));
                //comment
                sa.setAttributeComment((String) c.elementAt(nameCol + 4));
                
                attributeList.add(sa);
                
            }
        }
        return attributeList;
    }
    
    public void setAttributes(List<SchemaAttribute> attributes) {
        if(attributes == null) {
            return;
        }
        
        Iterator<SchemaAttribute> it = attributes.iterator();
        while(it.hasNext()) {
            SchemaAttribute sa = it.next();
            
            String name = sa.getAttributeName();
            String type = sa.getAttributeType();
            String size = sa.getAttributeSize();
            String scale = sa.getAttributeScale();
            String comment = sa.getAttributeComment();
            
            mTableModel.addRow(mHasExpressionColumn?
                    new Object[] {"", name, type, size, scale, comment} :
                    new Object[] {name, type, size, scale, comment});
                int rcount = mTable.getRowCount();
                mTable.setRowSelectionInterval(rcount - 1, rcount - 1);
        }
    }
    
    public void clearTable() {
        
        while(mTableModel.getRowCount() != 0) {
            mTableModel.removeRow(0);
        }
    }
    public boolean hasExpressionList() {
        return mHasExpressionColumn;
    }
    
    public List<String> getExpressionList() {
        List<String> expList = new ArrayList<String>();
        if (!mHasExpressionColumn) {
            return expList;
        }
        Vector r = mTableModel.getDataVector();
        for (int i = 0, I = r.size(); i < I; i++) {
            Vector c = (Vector) r.elementAt(i);
            if (!(c.elementAt(1) == null) && !(c.elementAt(1).equals(""))) {
                expList.add((String) c.elementAt(0));
            }
        }
        return expList;
    }
    
    public void setExpressions(List<String> expressions) {
        if (!mHasExpressionColumn) {
            return;
        }
        
        Vector<Vector<String>> r = mTableModel.getDataVector();
        if(r.size() != expressions.size()) {
            return;
        }
        for (int i = 0, I = r.size(); i < I; i++) {
            Vector<String> c = r.elementAt(i);
            c.set(0, expressions.get(i));
        }
    }
    
    public List<String> getToColumnList() {
        List<String> toList = new ArrayList<String>();
        if (!mHasExpressionColumn) {
            return toList;
        }
        Vector r = mTableModel.getDataVector();
        for (int i = 0, I = r.size(); i < I; i++) {
            Vector c = (Vector) r.elementAt(i);
            if (!(c.elementAt(1) == null) && !(c.elementAt(1).equals(""))) {
                toList.add((String)c.elementAt(1));
            }
        }
        return toList;
    }
    
    public boolean hasInput(String inputName) {
        return mColumnMetadataTable.containsKey(inputName);
    }
    
    public boolean hasInputAttribute(String inputAttributeName) {
        return getAttribute(inputAttributeName) != null;
    }
    
    public SchemaAttribute findSchemaAttribute(String toColumn) {
        SchemaAttribute sa = null;
        
        if(toColumn == null) {
            return null;
        }
        
        List<SchemaAttribute> list = getAttributes();
        Iterator<SchemaAttribute> it = list.iterator();
        while(it.hasNext()) {
            SchemaAttribute a = it.next();
            if(toColumn.equals(a.getAttributeName())) {
                sa = a;
                break;
            }
        }
        
        return sa;
    }
    
    /**
     * @param inputAttributeName input.attribute or attribute (when one and only one input)
     *
     * @return null if not found
     */
    public SchemaAttribute getAttribute(String inputAttributeName) {
        int index = inputAttributeName.indexOf(".");
        if (index < 1) {
            String attributeName = inputAttributeName;
            if (mColumnMetadataTable.size() != 1) {
                return null;
            }
            ListMap lm = (ListMap)mColumnMetadataTable.get(0);
            if (!lm.containsKey(attributeName)) {
                return null;
            }
            return (SchemaAttribute)lm.get(attributeName);
        }
        String inputName = inputAttributeName.substring(0, index);
        if (!mColumnMetadataTable.containsKey(inputName)) {
            return null;
        }
        String attributeName = inputAttributeName.substring(index + 1);
        ListMap lm = (ListMap)mColumnMetadataTable.get(inputName);
        if (!lm.containsKey(attributeName)) {
            return null;
        }
        return (SchemaAttribute)lm.get(attributeName);
    }
    
    public List<String> getQuantityAttributeList() {
        List<String> attributeList = new ArrayList<String>();
        Vector r = mTableModel.getDataVector();
        int nameCol = mHasExpressionColumn? 1 : 0;
        for (int i = 0, I = r.size(); i < I; i++) {
            Vector c = (Vector) r.elementAt(i);
            String name = (String)c.elementAt(nameCol);
            String type = (String)c.elementAt(nameCol + 1);
            if ( type != null && QUANTITY_TYPES.contains(type)) {
                attributeList.add(name);
            }
        }
        return attributeList;
    }
    
    public List<String> getAttributeNameList() {
	List<String> nameList = new ArrayList<String>();
	Vector r = mTableModel.getDataVector();
        int nameCol = mHasExpressionColumn? 1 : 0;
	for (int i = 0, I = r.size(); i < I; i++) {
	    Vector c = (Vector) r.elementAt(i);
	    String name = (String)c.elementAt(nameCol);
	    nameList.add(name);
	}
	return nameList;
    }
    
    protected boolean isShowButtons() {
        return true;
    }
    
    public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
        if (mReadOnly) {
            return;
        }
        
        //stop any cell editing IZ129687
        TableCellEditor editor = mTable.getCellEditor();
        if(editor != null) {
            editor.stopCellEditing();
        }
        
        int rowCount = mTableModel.getRowCount();
        // at least one attribute must be defined
        if (rowCount < 1) {
            String msg = NbBundle.getMessage(SelectPanel.class,
                    "SelectPanel.AT_LEAST_ONE_ATTRIBUTE_MUST_BE_DEFINED");
            throw new PropertyVetoException(msg, evt);
        }
        
        if (mHasExpressionColumn) {
            // for each attribute: expression must be defined
            for (int i = 0; i < rowCount; i++) {
                String exp = (String)mTableModel.getValueAt(i, 0);
                if (exp == null || exp.trim().equals("")) {
                    String msg = NbBundle.getMessage(SelectPanel.class,
                            "SelectPanel.EXPRESSION_MUST_BE_SPECIFIED");
                    throw new PropertyVetoException(msg, evt);
                }
            }
        }
        
        // for each attribute: name and type must be defined
        int nameCol = mHasExpressionColumn? 1 : 0;
        for (int i = 0; i < rowCount; i++) {
            String colName = (String)mTableModel.getValueAt(i, nameCol);
            if (colName == null || colName.trim().equals("")) {
                String msg = NbBundle.getMessage(SelectPanel.class,
                        "SelectPanel.ATTRIBUTE_NAME_MUST_BE_DEFINED");
                throw new PropertyVetoException(msg, evt);
            }
            String colType = (String)mTableModel.getValueAt(i, nameCol + 1);
            if (colType == null || colType.trim().equals("")) {
                String msg = NbBundle.getMessage(SelectPanel.class,
                        "SelectPanel.ATTRIBUTE_TYPE_MUST_BE_DEFINED");
                throw new PropertyVetoException(msg, evt);
            }
        }
        
        // attribute name must be unique
        Set<String> nameSet = new HashSet<String>();
        for (int i = 0; i < rowCount; i++) {
            String colName = (String)mTableModel.getValueAt(i, nameCol);
            if (nameSet.contains(colName)) {
                String msg = NbBundle.getMessage(SelectPanel.class,
                        "SelectPanel.ATTRIBUTE_NAME_MUST_BE_UNIQUE");
                throw new PropertyVetoException(msg, evt);
            }
            nameSet.add(colName);
        }
    }
    
    private void updateWhenExpressionChanges() {
        String opName = "";
        String columnName = "";
        try {
            opName = SelectPanel.this.mComponent.getString(PROP_NAME);
            int row = SelectPanel.this.mCellEditorExpression.mRow;
            int column = SelectPanel.this.mCellEditorExpression.mColumn;
            String exp = (String)SelectPanel.this.mTable.getValueAt(row, 0);
            String name = (String)SelectPanel.this.mTable.getValueAt(row, 1);
            String type = (String)SelectPanel.this.mTable.getValueAt(row, 2);
            String size = (String)SelectPanel.this.mTable.getValueAt(row, 3);
            String scale = (String)SelectPanel.this.mTable.getValueAt(row, 4);
            if (!(name.trim().equals("") && type.trim().equals("") && size.trim().equals("") && scale.trim().equals(""))) {
                return;
            }
            exp = exp.trim();
            if (exp.equals("")) {
                return;
            }
            SchemaAttribute sa = SelectPanel.this.getAttribute(exp);
            if (sa == null) {
                return;
            }
            String attrName = generateUniqueAttributeName(sa.getAttributeName());
            
            SelectPanel.this.mTable.setValueAt(attrName, row, 1);
            SelectPanel.this.mTable.setValueAt(sa.getAttributeType(), row, 2);
            SelectPanel.this.mTable.setValueAt(sa.getAttributeSize(), row, 3);
            SelectPanel.this.mTable.setValueAt(sa.getAttributeScale(), row, 4);
        } catch (Exception ex) {
            mLog.log(Level.WARNING,
                    NbBundle.getMessage(SelectPanel.class,
                    "SelectPanel.FAIL_TO_AUTOFILL_COLUMN_METADATA_FOR_COLUMN_OF_OPERATOR",
                    columnName,
                    opName),
                    ex);
        }
    }
    
   
    private void updateWhenExpressionChanges(AttributeInfo info) {
        String opName = "";
        String columnName = "";
        try {
            opName = SelectPanel.this.mComponent.getString(PROP_NAME);
            int row = SelectPanel.this.mCellEditorExpression.mRow;
            int column = SelectPanel.this.mCellEditorExpression.mColumn;
            String exp = (String)SelectPanel.this.mTable.getValueAt(row, 0);
            String name = (String)SelectPanel.this.mTable.getValueAt(row, 1);
            String type = (String)SelectPanel.this.mTable.getValueAt(row, 2);
            String size = (String)SelectPanel.this.mTable.getValueAt(row, 3);
            String scale = (String)SelectPanel.this.mTable.getValueAt(row, 4);
            if (!(name.trim().equals("") && type.trim().equals("") && size.trim().equals("") && scale.trim().equals(""))) {
                return;
            }
            exp = exp.trim();
            if (exp.equals("")) {
                exp = info.getEntityAndColumnName();
            
                SchemaAttribute sa = SelectPanel.this.getAttribute(exp);
                if (sa == null) {
                    return;
                }
                
                String attrName = generateUniqueAttributeName(sa.getAttributeName());
                
                SelectPanel.this.mTable.setValueAt(attrName, row, 1);
                SelectPanel.this.mTable.setValueAt(sa.getAttributeType(), row, 2);
                SelectPanel.this.mTable.setValueAt(sa.getAttributeSize(), row, 3);
                SelectPanel.this.mTable.setValueAt(sa.getAttributeScale(), row, 4);
            
            }
        } catch (Exception ex) {
            mLog.log(Level.WARNING,
                    NbBundle.getMessage(SelectPanel.class,
                    "SelectPanel.FAIL_TO_AUTOFILL_COLUMN_METADATA_FOR_COLUMN_OF_OPERATOR",
                    columnName,
                    opName),
                    ex);
        }
    }
    
    public String generateUniqueAttributeName(String baseName) {
        int rowCount = mTableModel.getRowCount();
        String newAttrName = baseName;
        Set nameSet = new HashSet();
        int nameCol = mHasExpressionColumn? 1 : 0;
        for (int i = 0; i < rowCount; i++) {
            String colName = (String)mTableModel.getValueAt(i, nameCol);
            nameSet.add(colName);
        }
        
        int counter = 0;
        while(nameSet.contains(newAttrName)) {
            newAttrName = baseName + "_" + counter;
            counter++;
        }
        
        return newAttrName;
    }
    
    
    private void fireDropNotification(AttributeInfo info) {
        Object[] listeners = mListernerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==AttributeDropNotificationListener.class) {
                AttributeDropNotificationEvent evt = new AttributeDropNotificationEvent(info);
                ((AttributeDropNotificationListener)listeners[i+1]).onDropComplete(evt);
            }
        }
    }
    
    class MyDropTargetAdapter extends DropTargetAdapter {
        @Override
        public void dragEnter(DropTargetDragEvent e) {
            if(!isDragAcceptable(e)) {
                e.rejectDrag();
                return;
            }
            SelectPanel.this.mTable.grabFocus();
            int row = SelectPanel.this.mTable.rowAtPoint(e.getLocation());
            int column =  SelectPanel.this.mTable.columnAtPoint(e.getLocation());
            if (column == 0 && row >= 0) {
                SelectPanel.this.mTable.editCellAt(row, column);
                e.acceptDrag(mAcceptableActions);
                return;
            }
            e.rejectDrag();
        }
        
        @Override
        public void dragOver(DropTargetDragEvent e) {
            // When dragOver is called, the mouse is not in the previous
            // activated smart text-field
            if(!isDragAcceptable(e)) {
                e.rejectDrag();
                return;
            }
            SelectPanel.this.mTable.grabFocus();
            // Retrieve the working cell editor.
            TableCellEditor editor = SelectPanel.this.mTable.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }
            int row = SelectPanel.this.mTable.rowAtPoint(e.getLocation());
            int column =  SelectPanel.this.mTable.columnAtPoint(e.getLocation());
            if (column == 0 && row >= 0) {
                SelectPanel.this.mTable.editCellAt(row, column);
                e.acceptDrag(mAcceptableActions);
                return;
            }
            e.rejectDrag();
        }
        
        public void drop(DropTargetDropEvent e) {
            // Never happen
        }
        
        @Override
        public void dropActionChanged(DropTargetDragEvent e) {
            if(!isDragAcceptable(e)) {
                e.rejectDrag();
                return;
            } else {
                e.acceptDrag(mAcceptableActions);
                return;
            }
        }
        
        private boolean isDragAcceptable(DropTargetDragEvent e) {
            return e.isDataFlavorSupported(AttributeInfoDataFlavor.ATTRIBUTE_INFO_FLAVOR);
        }
    }
    
    class SmartCellEditor extends DefaultCellEditor {
        int mRow;
        int mColumn;
        
        public SmartCellEditor(SmartTextField stf) {
            super(stf);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            mRow = row;
            mColumn = column;
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
    }
    
    class ExpressionCellEditorListener implements CellEditorListener {
        // This tells the listeners the editor has canceled editing
        public void editingCanceled(ChangeEvent e) {
            return;
        }
        // This tells the listeners the editor has ended editing
        public void editingStopped(ChangeEvent e) {
            updateWhenExpressionChanges();
        }
        
    }
    
    class TextFieldExpressionDropNotificationHandler implements AttributeDropNotificationListener {
        public void onDropComplete(AttributeDropNotificationEvent evt) {
            AttributeInfo info = evt.getAttributeInfo();
            if(info != null) {
                updateWhenExpressionChanges(info);
                fireDropNotification(info);
            }
        }
    }
    
    class SQLTypesItemListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
                String item = (String) e.getItem();
                int row = mTable.getEditingRow();
                if(row == -1) {
                    return;
                }
                
                if(item.equals(SQL_TYPE_VARCHAR)) {
                    if(row != -1) {
                        String size = "";
                        if(mHasExpressionColumn) {
                            size = (String) mTableModel.getValueAt(row, 3);
                        } else {
                            size = (String) mTableModel.getValueAt(row, 2);
                        }
                        
                        if(size == null || size.trim().equals("")) {
                            //by default set varchar data types size to 100
                            if(mHasExpressionColumn) {
                                mTableModel.setValueAt("100", row, 3);
                            } else {
                                mTableModel.setValueAt("100", row, 2);
                            }
                        }
                    }
                } else {
                    //by default remove size and let user type in if
                    //required.
                    if(mHasExpressionColumn) {
                        mTableModel.setValueAt("", row, 3);
                    } else {
                        mTableModel.setValueAt("", row, 2);
                    }
                }
                
                mTableModel.fireTableDataChanged();
        }
        
    }
    
}