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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.share.SharedConstants;
import org.openide.util.NbBundle;

/**
 * InvocationRequestPanel.java
 *
 * Created on September 25, 2008, 1:52 PM
 *
 * @author Bing Lu
 */
public class InvocationRequestPanel extends JPanel implements SharedConstants {
    private static final Logger mLog = Logger.getLogger(InvocationRequestPanel.class.getName());

    private OperatorComponent mComponent;
    private Vector<String> mColTitle;
    private DefaultTableModel mTableModel;
    private JTable mTable;

    public InvocationRequestPanel(OperatorComponent component) {
        mComponent = component;
        initComponents();
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if (columnIndex == 0) {
                    return true;
                }
                return false;
            }
            
            @Override
            public Class getColumnClass(int columnIdex) {
                if (columnIdex == 0) {
                    return Boolean.class;
                }
                return String.class;
            }
        };
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
        mTable = new JTable(mTableModel);

        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        try {
            List<String> retainAttributeList = mComponent.getStringList(PROP_RETAIN_ATTRIBUTE_LIST);
            List<SchemaComponent> inputSchemaList = mComponent.getInputSchemaList();
            if (!inputSchemaList.isEmpty()) {
                SchemaComponent inputSchema = inputSchemaList.get(0);
                List<SchemaAttribute> inputAttributeList = inputSchema.getSchemaAttributes();
                for (int i = 0; i < inputAttributeList.size(); i++) {
                    Vector<Object> r = new Vector<Object>();
                    SchemaAttribute inputAttribute = inputAttributeList.get(i);
                    String attributeName = inputAttribute.getAttributeName();
                    String attributeType = inputAttribute.getAttributeType();
                    String attributeSize = inputAttribute.getAttributeSize();
                    String attributeScale = inputAttribute.getAttributeScale();
                    String attributeComment = inputAttribute.getAttributeComment();
                    if (retainAttributeList.contains(attributeName)) {
                        r.add(Boolean.TRUE);
                    } else {
                        r.add(Boolean.FALSE);
                    }
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

        mColTitle = new Vector<String>();
        mColTitle.add(NbBundle.getMessage(InvocationRequestPanel.class, "InvocationRequestPanel.Add_to_Output"));
        mColTitle.add(NbBundle.getMessage(InvocationAttributePanel.class, "InvocationAttributePanel.ATTRIBUTE_NAME"));
        mColTitle.add(NbBundle.getMessage(InvocationAttributePanel.class, "InvocationAttributePanel.DATA_TYPE"));
        mColTitle.add(NbBundle.getMessage(InvocationAttributePanel.class, "InvocationAttributePanel.SIZE"));
        mColTitle.add(NbBundle.getMessage(InvocationAttributePanel.class, "InvocationAttributePanel.SCALE"));
        mColTitle.add(NbBundle.getMessage(InvocationAttributePanel.class, "InvocationAttributePanel.COMMENT"));
        
        mTableModel.setDataVector(data, mColTitle);

        TableColumnModel tcm = mTable.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(100);
        mTable.setPreferredScrollableViewportSize(new Dimension(600, 200));
        mTable.setDefaultRenderer(String.class, new SelectPanelTableCellRenderer());
        pane.add(new JScrollPane(mTable), BorderLayout.CENTER);
        topPane.add(pane, BorderLayout.CENTER);
    }

    public void store() {
        List<String> nameList = new ArrayList<String>();
        Vector r = mTableModel.getDataVector();
        for (int i = 0,  I = r.size(); i < I; i++) {
            Vector c = (Vector) r.elementAt(i);
            Boolean selected = (Boolean)c.elementAt(0);
            if (selected) {
                nameList.add((String) c.elementAt(1));
            }    
        }
        IEPModel model = mComponent.getModel();
        model.startTransaction();
        mComponent.setStringList(PROP_RETAIN_ATTRIBUTE_LIST, nameList);
        model.endTransaction();
    }
    
    public DefaultTableModel getTableModel() {
        return mTableModel;
    }

}