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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.share.SharedConstants;
import org.openide.util.NbBundle;

/**
 * InvocationAttributePanel.java
 *
 * Created on November 1, 2006, 1:52 PM
 *
 * @author Bing Lu
 */
public class InvocationAttributePanel extends JPanel implements SharedConstants {

    private static final Logger mLog = Logger.getLogger(InvocationAttributePanel.class.getName());
    private OperatorComponent mComponent;
    private Vector<String> mColTitle;
    private DefaultTableModel mTableModel;
    private JTable mTable;
    private SelectPanelTableCellRenderer spTCRenderer;

    public InvocationAttributePanel(OperatorComponent component) {
        mComponent = component;
        initComponents();
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class getColumnClass(int columnIdex) {
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

        mTableModel = createTableModel();
        mTable = new JTable(mTableModel);

        Vector<Vector<String>> data = new Vector<Vector<String>>();
        try {
            SchemaComponent outputSchema = mComponent.getOutputSchema();

            if (outputSchema != null) {
                List<SchemaAttribute> attrs = outputSchema.getSchemaAttributes();
                for (int i = 0; i < attrs.size(); i++) {
                    Vector<String> r = new Vector<String>();
                    SchemaAttribute sa = attrs.get(i);
                    String attributeName = sa.getAttributeName();
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
        mColTitle = new Vector<String>();
        mColTitle.add(NbBundle.getMessage(InvocationAttributePanel.class, "InvocationAttributePanel.ATTRIBUTE_NAME"));
        mColTitle.add(NbBundle.getMessage(InvocationAttributePanel.class, "InvocationAttributePanel.DATA_TYPE"));
        mColTitle.add(NbBundle.getMessage(InvocationAttributePanel.class, "InvocationAttributePanel.SIZE"));
        mColTitle.add(NbBundle.getMessage(InvocationAttributePanel.class, "InvocationAttributePanel.SCALE"));
        mColTitle.add(NbBundle.getMessage(InvocationAttributePanel.class, "InvocationAttributePanel.COMMENT"));
        mTableModel.setDataVector(data, mColTitle);
        TableColumnModel tcm = mTable.getColumnModel();
        spTCRenderer = new SelectPanelTableCellRenderer();
        mTable.setDefaultRenderer(String.class, spTCRenderer);
        mTable.setPreferredScrollableViewportSize(new Dimension(600, 200));
        topPane.add(new JScrollPane(mTable), BorderLayout.CENTER);
    }
    
    public void setDataVector(Vector<Vector<String>> dataVector) {
        mTableModel.setDataVector(dataVector, mColTitle);
    }

    public List<SchemaAttribute> getAttributes() {
        IEPModel model = mComponent.getModel();
        List<SchemaAttribute> attributeList = new ArrayList<SchemaAttribute>();
        Vector r = mTableModel.getDataVector();
        for (int i = 0, I = r.size(); i < I; i++) {
            Vector c = (Vector) r.elementAt(i);
            if (!(c.elementAt(1) == null) && !(c.elementAt(1).equals(""))) {
                SchemaAttribute sa = model.getFactory().createSchemaAttribute(model);
                String name = (String) c.elementAt(0);
                sa.setName(name);
                sa.setTitle(name);

                //name
                sa.setAttributeName(name);
                //type
                sa.setAttributeType((String) c.elementAt(1));
                //size
                sa.setAttributeSize((String) c.elementAt(2));
                //scale
                sa.setAttributeScale((String) c.elementAt(3));
                //comment
                sa.setAttributeComment((String) c.elementAt(4));

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
        // attribute name must be unique
        Set nameSet = new HashSet();
        for (int i = 0; i < rowCount; i++) {
            String colName = (String) mTableModel.getValueAt(i, 0);
            if (nameSet.contains(colName)) {
                String msg = NbBundle.getMessage(InvocationAttributePanel.class,
                        "InvocationAttributePanel.Attribute_name_must_be_unique");
                throw new PropertyVetoException(msg, evt);
            }
            nameSet.add(colName);
        }
    }
}