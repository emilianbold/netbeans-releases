/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * @author pfiala
 */
class FinderMethodsNode extends EjbSectionNode {

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
        };
        return innerTablePanel;
    }
}
