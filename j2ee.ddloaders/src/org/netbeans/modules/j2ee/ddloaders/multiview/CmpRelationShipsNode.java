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

import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author pfiala
 */
class CmpRelationShipsNode extends EjbSectionNode {

    CmpRelationShipsNode(SectionNodeView sectionNodeView, EjbJar ejbJar) {
        super(sectionNodeView, true, ejbJar, Utils.getBundleMessage("LBL_CmpRelationships"), Utils.ICON_BASE_MISC_NODE);
        setExpanded(true);
    }

    protected SectionInnerPanel createNodeInnerPanel() {
        final EjbJar ejbJar = (EjbJar) key;
        EjbJarMultiViewDataObject dataObject = (EjbJarMultiViewDataObject) getSectionNodeView().getDataObject();
        final CmpRelationshipsDialogHelper dialogHelper = new CmpRelationshipsDialogHelper(dataObject, ejbJar);
        final CmpRelationshipsTableModel model = new CmpRelationshipsTableModel(ejbJar);
        final InnerTablePanel innerTablePanel = new InnerTablePanel(getSectionNodeView(), model) {
            public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
                if (source == key) {
                    scheduleRefreshView();
                }
            }

            protected void editCell(int row, int column) {
                EjbRelation ejbRelation = ejbJar.getSingleRelationships().getEjbRelation(row);
                dialogHelper.showCmpRelationshipsDialog(Utils.getBundleMessage("LBL_Edit_CMP_Relationship"),
                        ejbRelation);
            }
        };
        innerTablePanel.getAddButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialogHelper.showCmpRelationshipsDialog(Utils.getBundleMessage("LBL_AddCMPRelationship"), null);
            }
        });
        return innerTablePanel;

    }

}
