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
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author pfiala
 */
public abstract class QueryMethodsTableModel extends InnerTableModel {

    protected final FileObject ejbJarFile;
    protected final Entity entity;
    private List queries;
    private final HashMap queryMethodHelperMap = new HashMap();

    public QueryMethodsTableModel(String[] columnNames, int[] columnWidths, FileObject ejbJarFile,
            Entity entity) {
        super(columnNames, columnWidths);
        this.ejbJarFile = ejbJarFile;
        this.entity = entity;
        initMethods();
        ejbJarFile.addFileChangeListener(new FileChangeAdapter() {
            public void fileChanged(FileEvent fe) {
                initMethods();
            }
        });
    }

    protected void initMethods() {
        queries = getQueries();
    }

    public void removeRow(int row) {
        // TODO: implement Method removal
        initMethods();
        fireTableRowsDeleted(-1, -1);
    }

    public void dataFileChanged() {
        queryMethodHelperMap.clear();
        super.dataFileChanged();
    }

    public int getRowCount() {
        return queries.size();
    }

    protected QueryMethodHelper getQueryMethodHelper(Query query) {
        QueryMethodHelper queryMethodHelper = (QueryMethodHelper) queryMethodHelperMap.get(query);
        if (queryMethodHelper == null) {
            queryMethodHelper = new QueryMethodHelper(ejbJarFile, entity, query);
            queryMethodHelperMap.put(query, queryMethodHelper);
        }
        return queryMethodHelper;

    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    protected List getQueries() {
        List l = new LinkedList();
        Query[] queries = entity.getQuery();
        for (int i = 0; i < queries.length; i++) {
            Query query = queries[i];
            if (isSupportedMethod(query)) {
                l.add(query);
            }
        }
        return l;
    }

    protected abstract boolean isSupportedMethod(Query query);
}
