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

import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.QueryCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.src.MethodElement;

/**
 * @author pfiala
 */
class FinderMethodsTableModel extends QueryMethodsTableModel {

    protected static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_Method"),
                                                    Utils.getBundleMessage("LBL_ReturnsCollection"),
                                                    Utils.getBundleMessage("LBL_ResultInterface"),
                                                    Utils.getBundleMessage("LBL_Query"),
                                                    Utils.getBundleMessage("LBL_Description")};
    protected static final int[] COLUMN_WIDTHS = new int[]{200, 100, 120, 200, 100};

    public FinderMethodsTableModel(FileObject ejbJarFile, Entity entity, EntityHelper entityHelper) {
        super(COLUMN_NAMES, COLUMN_WIDTHS, ejbJarFile, entity, entityHelper);
    }

    public void editRow(int row) {
        Query query = (Query) getQueries().get(row);
        QueryMethodHelper helper = getQueryMethodHelper(query);

        boolean hasLocal = entityHelper.getLocal() != null;
        boolean hasRemote = entityHelper.getRemote() != null;
        boolean hasLocalMethod = helper.localMethod != null;
        boolean hasRemoteMethod = helper.remoteMethod != null;
        boolean returnsCollection = helper.returnsCollection();
        QueryCustomizer customizer = new QueryCustomizer();
        MethodElement methodElement = (MethodElement) helper.getPrototypeMethod().clone();
        query = (Query) query.clone();
        boolean result = customizer.showFinderCustomizer(methodElement, query, returnsCollection,
                hasLocal, hasRemote, hasLocalMethod, hasRemoteMethod);
        if (result) {
            helper.updateFinderMethod(methodElement, query, customizer.finderReturnIsSingle(), customizer.publishToLocal(),
                    customizer.publishToRemote());
            fireTableRowsUpdated(row, row);
        }
    }

    public int addRow() {
        entityHelper.addFinderMethod();
        initMethods();
        fireTableRowsInserted(-1, -1);
        return getRowCount() - 1;
    }

    protected boolean isSupportedMethod(Query query) {
        return query.getQueryMethod().getMethodName().startsWith("findBy");//NOI18N
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Query query = (Query) getQueries().get(rowIndex);
        switch (columnIndex) {
            case 0:
                return query.getQueryMethod().getMethodName();
            case 1:
                return new Boolean(getQueryMethodHelper(query).returnsCollection());
            case 2:
                return getQueryMethodHelper(query).getResultInterface();
            case 3:
                return query.getEjbQl();
            case 4:
                return query.getDefaultDescription();
        }
        return null;
    }

    public Class getColumnClass(int columnIndex) {
        return columnIndex == 1 ? Boolean.class : String.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return super.isCellEditable(rowIndex, columnIndex);
    }
}
