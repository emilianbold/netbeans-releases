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

import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * @author pfiala
 */
public class FinderMethodsNode extends EjbSectionNode {

    private EntityHelper.Queries queries;

    FinderMethodsNode(SectionNodeView sectionNodeView, EntityHelper.Queries queries) {
        super(sectionNodeView, true, queries, Utils.getBundleMessage("LBL_CmpFinders"), Utils.ICON_BASE_MISC_NODE);
        this.queries = queries;
    }

    protected SectionNodeInnerPanel createNodeInnerPanel() {
        final FinderMethodsTableModel model = queries.getFinderMethodsTableModel();
        InnerTablePanel innerTablePanel = new InnerTablePanel(getSectionNodeView(), model) {
            protected void editCell(final int row, final int column) {
                model.editRow(row);
            }

            public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
                //super.dataModelPropertyChange(source, propertyName, oldValue, newValue);    
            }

            public void focusData(Object element) {
                if (element instanceof Query) {
                    final int row = queries.getFinderMethodRow((Query) element);
                    if (row >= 0) {
                        getTable().getSelectionModel().setSelectionInterval(row, row);
                    }
                }
            }
        };
        return innerTablePanel;
    }

    public SectionNode getNodeForElement(Object element) {
        if (element instanceof Query) {
            if (queries.getFinderMethodRow((Query) element) >= 0) {
                return this;
            }
        }
        return super.getNodeForElement(element);
    }
}
