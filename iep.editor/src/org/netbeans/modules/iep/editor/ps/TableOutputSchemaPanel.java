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
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;

import java.awt.BorderLayout;
import java.awt.dnd.DragGestureEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Iterator;
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
 * Created on November 1, 2PartitionPanel_1 *
 * @author Bing Lu
 */
public class TableOutputSchemaPanel extends JPanel implements SharedConstants {
    private static final Logger mLog = Logger.getLogger(TableOutputSchemaPanel.class.getName());
    
    private static final String COL_SEQID = "ems_seqid"; //com.sun.jbi.engine.iep.core.share.SharedConstants    
    
    private IEPModel mModel;
    private OperatorComponent mComponent;
    private DefaultMoveableRowTableModel mTableModel;
    private MoveableRowTable mTable;

    public TableOutputSchemaPanel(IEPModel model, OperatorComponent component) {
        mModel = model;
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
                return false;
            }
            public void dragGestureRecognized(DragGestureEvent dge) {
                return;
            }
        };
        Vector data = new Vector();
        try {
            SchemaComponent outputSchema = mComponent.getOutputSchemaId();
            if(outputSchema != null) {
                List<SchemaAttribute> attrs = outputSchema.getSchemaAttributes();
                Iterator<SchemaAttribute> attrIt = attrs.iterator();
                //ritjava.util.List fromColumnList = mComponent.getProperty(FROM_COLUMN_LIST_KEY).getListValue();
                
                while(attrIt.hasNext()) {
                    Vector r = new Vector();
                    
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
                
                Vector r = new Vector();
                r.add(COL_SEQID);
                r.add(SQL_TYPE_BIGINT);
                r.add("");
                r.add("");
                r.add("");
                data.add(r);
            }
            
//            String schemaId = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue();
//            if(!schemaId.trim().equals("")) {
//                Schema schema = mPlan.getSchema(schemaId);
//                java.util.List attributeMetadataList = new ArrayList(schema.getAttributeMetadataAsList());
//                for(int i = 0; i < attributeMetadataList.size(); i+=5) {
//                    Vector r = new Vector();
//                    String name = (String)attributeMetadataList.get(i);
//                    r.add(name);
//                    if(i + 1 < attributeMetadataList.size()) {
//                        r.add(attributeMetadataList.get(i + 1));
//                    } else {
//                        r.add("");
//                    }
//                    if(i + 2 < attributeMetadataList.size()) {
//                        r.add(attributeMetadataList.get(i + 2));
//                    } else {
//                        r.add("");
//                    }
//                    if(i + 3 < attributeMetadataList.size()) {
//                        r.add(attributeMetadataList.get(i + 3));
//                    } else {
//                        r.add("");
//                    }
//                    if(i + 4 < attributeMetadataList.size()) {
//                        r.add(attributeMetadataList.get(i + 4));
//                    } else {
//                        r.add("");
//                    }
//                    data.add(r);
//                }
//                Vector r = new Vector();
//                r.add(COL_SEQID);
//                r.add(SQL_TYPE_BIGINT);
//                r.add("");
//                r.add("");
//                r.add("");
//                data.add(r);
//            } 
        } catch(Exception e) {
            e.printStackTrace();
        }
        Vector colTitle = new Vector();
        colTitle.add(NbBundle.getMessage(TableOutputSchemaPanel.class, "SelectPanel.ATTRIBUTE_NAME"));
        colTitle.add(NbBundle.getMessage(TableOutputSchemaPanel.class, "SelectPanel.DATA_TYPE"));
        colTitle.add(NbBundle.getMessage(TableOutputSchemaPanel.class, "SelectPanel.SIZE"));
        colTitle.add(NbBundle.getMessage(TableOutputSchemaPanel.class, "SelectPanel.SCALE"));
        colTitle.add(NbBundle.getMessage(TableOutputSchemaPanel.class, "SelectPanel.COMMENT"));
        mTableModel.setDataVector(data, colTitle);
        pane.add(new JScrollPane(mTable), BorderLayout.CENTER);
        topPane.add(pane, BorderLayout.CENTER);
    }
    
    public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
    }
            
    public void store() {
    }
}
