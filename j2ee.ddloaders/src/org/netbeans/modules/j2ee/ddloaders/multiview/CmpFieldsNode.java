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
class CmpFieldsNode extends EjbSectionNode {

    private EntityHelper.CmpFields cmpFields;

    CmpFieldsNode(SectionNodeView sectionNodeView, EntityHelper.CmpFields cmpFields) {
        super(sectionNodeView, true, cmpFields, Utils.getBundleMessage("LBL_CmpFields"), Utils.ICON_BASE_MISC_NODE);
        this.cmpFields = cmpFields;
    }

    protected SectionNodeInnerPanel createNodeInnerPanel() {
        final CmpFieldsTableModel model = new CmpFieldsTableModel(cmpFields);
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
        };
        return innerTablePanel;

    }

}
