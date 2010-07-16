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

import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.tbls.editor.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.tbls.editor.table.MoveableRowTable;
import org.netbeans.modules.tbls.editor.table.ReadOnlyNoExpressionDefaultMoveableRowTableModel;
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
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
    private OperatorComponent mComponent;
    private DefaultMoveableRowTableModel mTableModel;
    private MoveableRowTable mTable;
    private SelectPanelTableCellRenderer spTCRenderer;

    public TableOutputSchemaPanel(OperatorComponent component) {
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
        mTableModel = new ReadOnlyNoExpressionDefaultMoveableRowTableModel();
        mTable = new MoveableRowTable(mTableModel) {

            public void dragGestureRecognized(DragGestureEvent dge) {
                return;
            }
        };
        Vector<Vector<String>> data = new Vector<Vector<String>>();
        try {
            SchemaComponent outputSchema = mComponent.getOutputSchema();
            if (outputSchema != null) {
                List<SchemaAttribute> attrs = outputSchema.getSchemaAttributes();
                Iterator<SchemaAttribute> attrIt = attrs.iterator();
                //ritjava.util.List fromColumnList = mComponent.getProperty(PROP_FROM_COLUMN_LIST).getListValue();

                while (attrIt.hasNext()) {
                    Vector<String> r = new Vector<String>();

                    SchemaAttribute sa = attrIt.next();

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

                Vector<String> r = new Vector<String>();
                r.add(COL_SEQID);
                r.add(SQL_TYPE_BIGINT);
                r.add("");
                r.add("");
                r.add("");
                data.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Vector<String> colTitle = new Vector<String>();
        colTitle.add(NbBundle.getMessage(TableOutputSchemaPanel.class, "SelectPanel.ATTRIBUTE_NAME"));
        colTitle.add(NbBundle.getMessage(TableOutputSchemaPanel.class, "SelectPanel.DATA_TYPE"));
        colTitle.add(NbBundle.getMessage(TableOutputSchemaPanel.class, "SelectPanel.SIZE"));
        colTitle.add(NbBundle.getMessage(TableOutputSchemaPanel.class, "SelectPanel.SCALE"));
        colTitle.add(NbBundle.getMessage(TableOutputSchemaPanel.class, "SelectPanel.COMMENT"));
        mTableModel.setDataVector(data, colTitle);

        TableColumnModel tcm = mTable.getColumnModel();
        spTCRenderer = new SelectPanelTableCellRenderer();

//      setting up renderer
        tcm.getColumn(0).setCellRenderer(spTCRenderer);
        tcm.getColumn(1).setCellRenderer(spTCRenderer);
        tcm.getColumn(2).setCellRenderer(spTCRenderer);
        tcm.getColumn(3).setCellRenderer(spTCRenderer);
        tcm.getColumn(4).setCellRenderer(spTCRenderer);

        pane.add(new JScrollPane(mTable), BorderLayout.CENTER);
        topPane.add(pane, BorderLayout.CENTER);
    }

    public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
    }

    public void store() {
    }
}