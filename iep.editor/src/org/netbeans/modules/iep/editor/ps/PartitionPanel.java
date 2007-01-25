/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.iep.editor.ps;

import org.netbeans.modules.iep.editor.model.Plan;
import org.netbeans.modules.iep.editor.model.Schema;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.iep.editor.tcg.table.MoveableRowTable;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.dnd.DragGestureEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
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
    
    private Plan mPlan;
    private TcgComponent mComponent;
    private DefaultMoveableRowTableModel mTableModel;
    private MoveableRowTable mTable;

    public PartitionPanel(Plan plan, TcgComponent component) {
        mPlan = plan;
        mComponent = component;
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
            String schemaId = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue();
            java.util.List partitionKey = mComponent.getProperty(ATTRIBUTE_LIST_KEY).getListValue();
            if(!schemaId.trim().equals("")) {
                Schema schema = mPlan.getSchema(schemaId);
                java.util.List attributeMetadataList = new ArrayList(schema.getAttributeMetadataAsList());
                for(int i = 0; i < attributeMetadataList.size(); i+=5) {
                    Vector r = new Vector();
                    String name = (String)attributeMetadataList.get(i);
                    if (partitionKey.contains(name)) {
                        r.add(Boolean.TRUE);
                    } else {
                        r.add(Boolean.FALSE);
                    }
                    r.add(name);
                    if(i + 1 < attributeMetadataList.size()) {
                        r.add(attributeMetadataList.get(i + 1));
                    } else {
                        r.add("");
                    }
                    if(i + 2 < attributeMetadataList.size()) {
                        r.add(attributeMetadataList.get(i + 2));
                    } else {
                        r.add("");
                    }
                    if(i + 3 < attributeMetadataList.size()) {
                        r.add(attributeMetadataList.get(i + 3));
                    } else {
                        r.add("");
                    }
                    if(i + 4 < attributeMetadataList.size()) {
                        r.add(attributeMetadataList.get(i + 4));
                    } else {
                        r.add("");
                    }
                    data.add(r);
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
        if (r.size() > 0 && getPartitionKey().size() == 0) {
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
            TcgProperty prop = mComponent.getProperty(ATTRIBUTE_LIST_KEY);
            if (!sb.toString().equals(prop.getStringValue())) {
                prop.setValue(partitionKey);
            }
        } catch (Exception e) {
            mLog.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}