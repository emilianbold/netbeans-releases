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

import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.CmpRelationshipsForm;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * @author pfiala
 */
class CmpRelationShipsNode extends SectionNode {

    CmpRelationShipsNode(SectionNodeView sectionNodeView, EjbJar ejbJar) {
        super(sectionNodeView, true, ejbJar, Utils.getBundleMessage("LBL_CmpRelationships"), Utils.ICON_BASE_MISC_NODE);
        setExpanded(true);
    }

    protected SectionInnerPanel createNodeInnerPanel() {
        final EjbJar ejbJar = (EjbJar) key;
        final FileObject ejbJarFile = getSectionNodeView().getDataObject().getPrimaryFile();
        final CmpRelationshipsTableModel model = new CmpRelationshipsTableModel(ejbJarFile, ejbJar);
        final InnerTablePanel innerTablePanel = new InnerTablePanel(getSectionNodeView(), model);
        final CmpRelationshipsDialogHelper dialogHelper = new CmpRelationshipsDialogHelper(ejbJarFile,  ejbJar);
        innerTablePanel.getAddButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (dialogHelper.showCmpRelationshipsDialog(Utils.getBundleMessage("LBL_AddCMPRelationship"), null)) {
                    int row = model.getRowCount() - 1;
                    model.fireTableRowsInserted(row, row);
                }
            }
        });
        innerTablePanel.getEditButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = innerTablePanel.getTable().getSelectedRow();
                EjbRelation ejbRelation = ejbJar.getSingleRelationships().getEjbRelation(row);
                if (dialogHelper.showCmpRelationshipsDialog(Utils.getBundleMessage("LBL_Edit_CMP_Relationship"), ejbRelation)) {
                    model.fireTableRowsUpdated(row, row);
                }
            }
        });
        return innerTablePanel;

    }

}
