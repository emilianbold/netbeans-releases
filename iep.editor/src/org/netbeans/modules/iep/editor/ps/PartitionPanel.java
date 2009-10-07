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

import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.iep.editor.tcg.table.MoveableRowTable;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.dnd.DragGestureEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.iep.model.lib.TcgProperty;
import org.openide.util.NbBundle;

/**
 * PartitionPanel.java
 *
 * Created on November 1, 2006, 1:52 PM
 *
 * @author Bing Lu
 */
public class PartitionPanel extends JPanel implements SharedConstants {
    private static final Logger mLog = Logger.getLogger(PartitionPanel.class.getName());
    
    private static DefaultCellEditor mCellEditor = new DefaultCellEditor(new JCheckBox());
    
    private IEPModel mModel;
    private OperatorComponent mComponent;
    private DefaultMoveableRowTableModel mTableModel;
    private MoveableRowTable mTable;
    private boolean mAllowEmptySelection;

    public PartitionPanel(OperatorComponent component, IEPModel model,  boolean allowEmptySelection) {
        mComponent = component;
        mModel = model;
        mAllowEmptySelection = allowEmptySelection;
        initComponents();
    }
    

    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel topPane = new JPanel();
        topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPane.setLayout(new BorderLayout(5, 5));
        add(topPane, BorderLayout.CENTER);
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(5, 5));
        mTableModel = new DefaultMoveableRowTableModel();
        mTable = new MoveableRowTable(mTableModel) {
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
            public void dragGestureRecognized(DragGestureEvent dge) {
                return;
            }
        };
        Vector data = new Vector();
        try {
            String schemaId = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).getValue();
            Property partionKeyProp = mComponent.getProperty(ATTRIBUTE_LIST_KEY);
            String partionKeyPropStringVal =  partionKeyProp.getValue();
            
            java.util.List partitionKey = (List) partionKeyProp.getPropertyType().getType().parse(partionKeyPropStringVal);
            if(!schemaId.trim().equals("")) {
                SchemaComponentContainer scContainer = mModel.getPlanComponent().getSchemaComponentContainer();
                
                SchemaComponent schema = scContainer.findSchema(schemaId);
                if(schema != null) {
                    
                    List<SchemaAttribute> attrs = schema.getSchemaAttributes();
                    Iterator<SchemaAttribute> it = attrs.iterator();
                    
                    while(it.hasNext()) {
                        Vector r = new Vector();
                        
                        SchemaAttribute sa = it.next();
                        String attributeName = sa.getAttributeName();
                        String attributeType = sa.getAttributeType();
                        String attributeSize = sa.getAttributeSize();
                        String attributeScale = sa.getAttributeScale();
                        String attributeComment = sa.getAttributeComment();
                         
                         
                        if (partitionKey.contains(attributeName)) {
                            r.add(Boolean.TRUE);
                        } else {
                            r.add(Boolean.FALSE);
                        }
                        
                        r.add(attributeName);
                        r.add(attributeType);
                        r.add(attributeSize);
                        r.add(attributeScale);
                        r.add(attributeComment);
                        
                        data.add(r);
                    }
                }
            } 
        } catch(Exception e) {
            e.printStackTrace();
        }
        Vector colTitle = new Vector();
        colTitle.add(NbBundle.getMessage(PartitionPanel.class, "PartitionPanel.PARTITION_KEY"));
        colTitle.add(NbBundle.getMessage(PartitionPanel.class, "SelectPanel.ATTRIBUTE_NAME"));
        colTitle.add(NbBundle.getMessage(PartitionPanel.class, "SelectPanel.DATA_TYPE"));
        colTitle.add(NbBundle.getMessage(PartitionPanel.class, "SelectPanel.SIZE"));
        colTitle.add(NbBundle.getMessage(PartitionPanel.class, "SelectPanel.SCALE"));
        colTitle.add(NbBundle.getMessage(PartitionPanel.class, "SelectPanel.COMMENT"));
        mTableModel.setDataVector(data, colTitle);
        TableColumnModel tcm = mTable.getColumnModel();
        try {
            tcm.getColumn(0).setCellEditor(mCellEditor);
            tcm.getColumn(0).sizeWidthToFit();
            tcm.getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                JCheckBox mCB = new JCheckBox();
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    if (value instanceof Boolean) {
                        mCB.setSelected(((Boolean)value).booleanValue());
                        return mCB;
                    }
                    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }
            });
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        pane.add(new JScrollPane(mTable), BorderLayout.CENTER);
        topPane.add(pane, BorderLayout.CENTER);
    }
    
    public List getAttributeList(Set types) {
        List attributeList = new ArrayList();
        Vector r = mTableModel.getDataVector();
        for (int i = 0, I = r.size(); i < I; i++) {
            Vector c = (Vector) r.elementAt(i);
            String name = (String)c.elementAt(1);
            String type = (String)c.elementAt(2);
            if ( type != null && types.contains(type)) {
                attributeList.add(name);
            }
        }
        return attributeList;
    }
    
    public List getPartitionKey() {
        List partitionKey = new ArrayList();
        Vector r = mTableModel.getDataVector();
        for (int i = 0, I = r.size(); i < I; i++) {
            Vector c = (Vector) r.elementAt(i);
            if (Boolean.TRUE.equals(c.elementAt(0))) {
                partitionKey.add(c.elementAt(1));
            }
        }
        return partitionKey;
    }
    
    public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
        List nameList = new ArrayList();
        Vector r = mTableModel.getDataVector();
        if (!mAllowEmptySelection && r.size() > 0 && getPartitionKey().size() == 0) {
            String msg = NbBundle.getMessage(PartitionPanel.class,
                    "PartitionPanel.PARTITION_KEY_MUST_HAVE_AT_LEAST_ONE_ATTRIBUTE");
            throw new PropertyVetoException(msg, evt);
        }
    }
            
    public void store() {
        List partitionKey = getPartitionKey();
        try {
            StringBuffer sb = new StringBuffer();            
            for (int i = 0, I = partitionKey.size(); i < I; i++) {
                if (0 < i) {
                    sb.append("\\");
                }
                sb.append((String)partitionKey.get(i));
            }
            Property partionKeyProp = mComponent.getProperty(ATTRIBUTE_LIST_KEY);
            String oldValue = partionKeyProp.getValue();
            if (!sb.toString().equals(oldValue)) {
                mComponent.getModel().startTransaction();
                partionKeyProp.setValue(sb.toString());
                mComponent.getModel().endTransaction();
            }
            
        } catch (Exception e) {
            mLog.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
