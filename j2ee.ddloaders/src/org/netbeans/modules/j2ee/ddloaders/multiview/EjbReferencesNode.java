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

import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRef;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.filesystems.FileObject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

/**
 * @author pfiala
 */
public class EjbReferencesNode extends SectionNode {
    public EjbReferencesNode(SectionNodeView sectionNodeView, Ejb ejb) {
        super(sectionNodeView, ejb, "Enterprise Bean References", Utils.ICON_BASE_MISC_NODE);
    }

    protected SectionInnerPanel createNodeInnerPanel() {
        final Ejb ejb = (Ejb) getKey();
        final InnerTablePanel innerTablePanel = new InnerTablePanel(getSectionNodeView(),
                new EjbReferencesTableModel(ejb));
        innerTablePanel.getEditButton().setVisible(false);
        innerTablePanel.getRemoveButton().setVisible(false);
        innerTablePanel.getAddButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FileObject ejbJarFile =
                        ((SectionNodeView) innerTablePanel.getSectionView()).getDataObject().getPrimaryFile();
                if (new OpenAddReferenceDialog(ejb, createRefNameSet(ejb), ejbJarFile).openDialog()) {
                    EjbReferencesTableModel model = ((EjbReferencesTableModel) innerTablePanel.getTable().getModel());
                    model.fireTableRowsInserted(0, 0);
                    model.fireTableDataChanged();
                    innerTablePanel.adjustHeight();
                }
            }
        });
        return innerTablePanel;
    }

    private Set createRefNameSet(Ejb ejb) {
        Set refNameSet = new HashSet();
        EjbLocalRef[] ejbLocalRef = ejb.getEjbLocalRef();
        for (int i = 0; i < ejbLocalRef.length; i++) {
            refNameSet.add(ejbLocalRef[i].getEjbRefName());
        }
        EjbRef[] ejbRef = ejb.getEjbRef();
        for (int i = 0; i < ejbRef.length; i++) {
            refNameSet.add(ejbRef[i].getEjbRefName());
        }
        return refNameSet;
    }
}
