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

import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.entres.CallEjbAction;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.entres.CallEjbDialog;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

import java.util.Set;
import java.util.HashSet;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.src.ClassElement;

/**
 * @author pfiala
 */
class EjbReferencesTableModel extends InnerTableModel {

    private XmlMultiViewDataObject dataObject;
    Ejb ejb;
    private static final String[] COLUMN_NAMES = {
        NbBundle.getBundle(EjbReferencesTableModel.class).getString("LBL_ReferenceName"),
        NbBundle.getBundle(EjbReferencesTableModel.class).getString("LBL_LinkedEjb"),
        NbBundle.getBundle(EjbReferencesTableModel.class).getString("LBL_Interface"),
        NbBundle.getBundle(EjbReferencesTableModel.class).getString("LBL_Description")};
    private static final int[] COLUMN_WIDTHS = new int[]{170, 260, 70, 250};

    public EjbReferencesTableModel(XmlMultiViewDataObject dataObject, Ejb ejb) {
        super(dataObject, COLUMN_NAMES, COLUMN_WIDTHS);
        this.dataObject = dataObject;
        this.ejb = ejb;
    }

    public int getRowCount() {
        return ejb.getEjbLocalRef().length + ejb.getEjbRef().length;
    }

    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public int addRow() {
        FileObject ejbJarFile = dataObject.getPrimaryFile();
        Project project = FileOwnerQuery.getOwner(ejbJarFile);
        ClassElement beanClass = Utils.getClassElement(Utils.getSourceClassPath(ejbJarFile), ejb.getEjbClass());
        CallEjbDialog callEjbDialog = new CallEjbDialog();
        if (callEjbDialog.open(ejb, project, beanClass)) {
            modelUpdatedFromUI();
        }
        return -1;
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
    public void removeRow(int selectedRow) {
        // TODO: implement removal of reference
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        int n = ejb.getEjbLocalRef().length;
        if (rowIndex < n) {
            EjbLocalRef ref = ejb.getEjbLocalRef(rowIndex);
            switch (columnIndex) {
                case 0:
                    return ref.getEjbRefName();
                case 1:
                    return ref.getEjbLink();
                case 2:
                    return "local"; //NOI18N
                case 3:
                    return ref.getDefaultDescription();
            }
        } else {
            EjbRef ref = ejb.getEjbRef(rowIndex - n);
            switch (columnIndex) {
                case 0:
                    return ref.getEjbRefName();
                case 1:
                    return ref.getEjbLink();
                case 2:
                    return "remote"; //NOI18N
                case 3:
                    return ref.getDefaultDescription();
            }
        }
        return null;
    }
}
