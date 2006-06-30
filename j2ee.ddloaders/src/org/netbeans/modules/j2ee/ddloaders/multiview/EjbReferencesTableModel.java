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

import org.netbeans.modules.j2ee.ejbcore.api.ui.CallEjb;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.JavaClass;

/**
 * @author pfiala
 */
public class EjbReferencesTableModel extends InnerTableModel {

    private XmlMultiViewDataObject dataObject;
    Ejb ejb;
    private static final String[] COLUMN_NAMES = {
        NbBundle.getBundle(EjbReferencesTableModel.class).getString("LBL_ReferenceName"),
        NbBundle.getBundle(EjbReferencesTableModel.class).getString("LBL_LinkedEjb"),
        NbBundle.getBundle(EjbReferencesTableModel.class).getString("LBL_Interface"),
        NbBundle.getBundle(EjbReferencesTableModel.class).getString("LBL_Description")};
    private static final int[] COLUMN_WIDTHS = new int[]{170, 260, 70, 250};

    public EjbReferencesTableModel(XmlMultiViewDataObject dataObject, Ejb ejb) {
        super(((EjbJarMultiViewDataObject)dataObject).getModelSynchronizer(), COLUMN_NAMES, COLUMN_WIDTHS);
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
        JavaClass beanClass = (JavaClass) JMIUtils.resolveType(ejb.getEjbClass());
        if (CallEjb.showCallEjbDialog(beanClass, NbBundle.getMessage(EjbReferencesTableModel.class, "LBL_AddEjbReference"))) { // NOI18N
            modelUpdatedFromUI();
        }
        return -1;
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
