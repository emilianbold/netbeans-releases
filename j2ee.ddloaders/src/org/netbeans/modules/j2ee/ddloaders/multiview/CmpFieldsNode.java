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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * @author pfiala
 */
public class CmpFieldsNode extends EjbSectionNode {

    private EntityHelper.CmpFields cmpFields;

    CmpFieldsNode(SectionNodeView sectionNodeView, EntityHelper.CmpFields cmpFields) {
        super(sectionNodeView, true, cmpFields, Utils.getBundleMessage("LBL_CmpFields"), Utils.ICON_BASE_MISC_NODE);
        this.cmpFields = cmpFields;
    }

    protected SectionNodeInnerPanel createNodeInnerPanel() {
        final CmpFieldsTableModel model = cmpFields.getCmpFieldsTableModel();
        final InnerTablePanel innerTablePanel = new InnerTablePanel(getSectionNodeView(), model) {
            protected void editCell(final int row, final int column) {
                model.editRow(row);
            }

            public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
                if (source == key) {
                    model.refreshView();
                    scheduleRefreshView();
                }
            }

            public void focusData(Object element) {
                if (element instanceof CmpField) {
                    final int row = cmpFields.getFieldRow((CmpField) element);
                    if (row >= 0) {
                        getTable().getSelectionModel().setSelectionInterval(row, row);
                    }
                }
            }
        };
        cmpFields.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt != null && CmpFieldHelper.PROPERTY_FIELD_ROW_CHANGED.equals(evt.getPropertyName())) {
                    final ListSelectionModel selectionModel = innerTablePanel.getTable().getSelectionModel();
                    final int selectedRow = selectionModel.getLeadSelectionIndex();
                    model.refreshView();
                    final int oldRow = ((Integer)evt.getOldValue()).intValue();
                    final int newRow = ((Integer)evt.getNewValue()).intValue();
                    if (selectedRow == oldRow) {
                        selectionModel.setSelectionInterval(newRow, newRow);
                    }
                }
            }
        });
        return innerTablePanel;

    }

    public SectionNode getNodeForElement(Object element) {
        if (element instanceof CmpField) {
            if (cmpFields.getFieldRow((CmpField) element) >= 0) {
                return this;
            }
        } else if (element instanceof CmpField[]) {
            final List list1 = Arrays.asList(cmpFields.getCmpFields());
            final List list2 = new LinkedList(Arrays.asList((CmpField[]) element));
            if (list1.size() == list2.size()) {
                list2.removeAll(list1);
                if (list2.size() == 0) {
                    return this;
                }
            }
        }
        return super.getNodeForElement(element);
    }
}
