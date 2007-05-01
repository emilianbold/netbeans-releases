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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.ui.view.property;

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.model.database.DBColumn;
import org.netbeans.modules.model.database.DBTable;
import org.netbeans.modules.model.database.ForeignKey;
import org.netbeans.modules.model.database.Index;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.ui.editor.property.impl.DefaultPropertyEditor;
import org.netbeans.modules.sql.framework.ui.view.BasicTopView;
import org.netbeans.modules.sql.framework.ui.view.IGraphViewContainer;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea;
import org.openide.nodes.Node;


/**
 * @author Ritesh Adval
 */
public class TableProperties {
    /** ColumnPropertySupport instance for foreign keys. */
    protected ColumnPropertySupport fKeys;

    /** ColumnPropertySupport instance for indices. */
    protected ColumnPropertySupport indices;

    /** ColumnPropertySupport instance for unique columns. */
    protected ColumnPropertySupport uniqueCols;

    /** ColumnPropertySupport instance for primary keys. */
    protected ColumnPropertySupport pKeys;
    
    protected IGraphViewContainer editor;
    protected SQLBasicTableArea gNode;

    private SQLDBTable table;

    /**
     * Gets the alias name for this table.
     * 
     * @return alias name
     */
    public String getAliasName() {
        return table.getAliasName();
    }

    /**
     * Gets the batch size for this table.
     * 
     * @return Integer representing batch size for the table
     */
    public Integer getBatchSize() {
        return new Integer(this.table.getBatchSize());
    }

    /**
     * Gets appropriate custom editor, if any, for the given property.
     * 
     * @param property Node.Property whose custom editor is sought
     * @return PropertyEditor associated with <code>property</code>, or null if none
     *         exists.
     */
    public PropertyEditor getCustomEditor(Node.Property property) {
        if (property.getName().equals("primaryKeys")) {
            return new DefaultPropertyEditor.ListEditor(pKeys.getDisplayVector());
        } else if (property.getName().equals("foreignKeys")) {
            return new DefaultPropertyEditor.ListEditor(fKeys.getDisplayVector());
        } else if (property.getName().equals("indices")) {
            return new DefaultPropertyEditor.ListEditor(indices.getDisplayVector());
        }

        return null;
    }

    /**
     * Gets display name of this table.
     * 
     * @return table disply name.
     */
    public String getDisplayName() {
        return table.getDisplayName();
    }

    private List getForeignKeyList(DBTable tbl, DBColumn column) {
        ArrayList optionList = new ArrayList();
        String refString = column.getName() + " --> ";

        List list = tbl.getForeignKeys();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            ForeignKey fk = (ForeignKey) it.next();
            if (fk.contains(column)) {
                List pkColumnList = fk.getPKColumnNames();
                Iterator it1 = pkColumnList.iterator();
                while (it1.hasNext()) {
                    String pkColName = (String) it1.next();
                    String optStr = refString.toString() + pkColName;
                    optionList.add(optStr);
                }
            }
        }

        return optionList;
    }

    /**
     * Gets delimited String list of foreign keys associated with this table.
     * 
     * @return delimited String list of foreign keys
     */
    public String getForeignKeys() {
        return fKeys.getDisplayString();
    }

    /**
     * Gets delimited String list of indices associated with this table.
     * 
     * @return delimited String list of indices
     */
    public String getIndices() {
        return indices.getDisplayString();
    }

    /**
     * Gets name of parent DatabaseModel for this table
     * 
     * @return name of parent DatabaseModel
     */
    public String getModelName() {
        return table.getParent().getModelName();
    }

    /**
     * Gets delimited String list of primary key columns associated with this table.
     * 
     * @return delimited String list of primary key columns
     */
    public String getPrimaryKeys() {
        return pKeys.getDisplayString();
    }

    /**
     * Gets the current OTD-derived schema name.
     * 
     * @return current schema name as supplied by OTD.
     */
    public String getSchema() {
        return table.getSchema();
    }

    /**
     * Gets the current OTD-derived catalog name.
     * 
     * @return current catalog name as supplied by OTD.
     */
    public String getCatalog() {
        return table.getCatalog();
    }

    /**
     * Gets the table prefix, if any, associated with this table.
     * 
     * @return table prefix
     */
    public String getTablePrefix() {
        return table.getTablePrefix();
    }

    /**
     * Gets the user defined table name.
     * 
     * @return user defined table
     */
    public String getUserDefinedTableName() {
        return table.getUserDefinedTableName();
    }

    /**
     * Gets the user defined schema name, if any.
     * 
     * @return user defined schema name
     */
    public String getUserDefinedSchemaName() {
        return table.getUserDefinedSchemaName();
    }

    /**
     * Gets the user defined catalog name, if any.
     * 
     * @return user defined catalog name
     */
    public String getUserDefinedCatalogName() {
        return table.getUserDefinedCatalogName();
    }

    // RFE-102428
    /**
     * Gets the Staging Table Name
     * 
     * @return Staging Table Name
     */
    public String getStagingTableName() {
        return table.getStagingTableName();
    }
    /**
     * Indicates whether to use fully-qualified form in resolving table name.
     * 
     * @return true to use fully-qualified form, false otherwise.
     */
    public boolean isUseFullyQualifiedName() {
        return table.isUsingFullyQualifiedName();
    }

    /**
     * Initializes display properties using values from the given SQLDBTable.
     * 
     * @param tbl SQLDBTable from which to initialize values
     */
    protected void initializeProperties(SQLDBTable tbl) {
        this.table = tbl;
        ArrayList pkList = new ArrayList();
        ArrayList fkList = new ArrayList();
        ArrayList idxList = new ArrayList();
        Set indexedUniqueCols = new HashSet();

        List columnList = table.getColumnList();
        Iterator it = columnList.iterator();
        while (it.hasNext()) {
            DBColumn column = (DBColumn) it.next();
            boolean pk = column.isPrimaryKey();
            boolean fk = column.isForeignKey();
            boolean indexed = column.isIndexed();

            //create pk option
            if (pk) {
                pkList.add(column.getName());
            }

            //get fk options
            if (fk) {
                List fkListForColumn = getForeignKeyList(table, column);
                if (fkListForColumn.size() > 0) {
                    fkList.addAll(fkListForColumn);
                }
            }

            //create idx option
            if (indexed) {
                idxList.add(column.getName());
            }
        }

        List indexList = table.getIndexes();
        it = indexList.iterator();
        while (it.hasNext()) {
            Index idx = (Index) it.next();
            if (idx.isUnique()) {
                indexedUniqueCols.addAll(idx.getColumnNames());
            }
        }

        //sort options
        Collections.sort(pkList);
        Collections.sort(fkList);
        Collections.sort(idxList);

        //create objects
        pKeys = new ColumnPropertySupport(pkList);
        fKeys = new ColumnPropertySupport(fkList);
        indices = new ColumnPropertySupport(idxList);
        uniqueCols = new ColumnPropertySupport(new ArrayList(indexedUniqueCols));
        
    }

    /**
     * Sets the alias name for this table
     * 
     * @param aName alias name
     */
    public void setAliasName(String aName) {
        this.table.setAliasName(aName);
    }

    /**
     * Sets the batch size for this table
     * 
     * @param newSize new value for batch size
     */
    public void setBatchSize(Integer newSize) {
        table.setBatchSize(newSize.intValue());
        setDirty(true);
    }

    /**
     * Sets the table prefix.
     * 
     * @param tPrefix new table prefix
     */
    public void setTablePrefix(String tPrefix) {
        table.setTablePrefix(tPrefix);
        setDirty(true);
    }

    /**
     * Sets the user defined table name.
     * 
     * @param newName new user defined table name
     */
    public void setUserDefinedTableName(String newName) {
        table.setUserDefinedTableName(newName);
        setDirty(true);
    }

    /**
     * Sets the user defined schema name.
     * 
     * @param newName user defined schema name
     */
    public void setUserDefinedSchemaName(String newName) {
        table.setUserDefinedSchemaName(newName);
        setDirty(true);
    }

    /**
     * Sets the user defined catalog name.
     * 
     * @param newName user defined catalog name
     */
    public void setUserDefinedCatalogName(String newName) {
        table.setUserDefinedCatalogName(newName);
        setDirty(true);
    }

    /**
     * Sets whether to use fully-qualified form in resolving table name.
     * 
     * @param useFullName true to use fully-qualified form, false otherwise.
     */
    public void setUseFullyQualifiedName(boolean useFullName) {
        table.setUsingFullyQualifiedName(useFullName);
        setDirty(true);
    }
    
    // RFE-102428
    /**
     * Sets the Staging Table Name.
     * 
     * @param stgTbleName user defined catalog name
     */
    public void setStagingTableName(String stgTbleName) {
        table.setStagingTableName(stgTbleName);
        setDirty(true);
    }
    
    protected void setDirty(boolean dirty) {
        if(editor instanceof BasicTopView) {
            BasicTopView topView = (BasicTopView)editor;
            topView.setDirty(dirty);
        }
    }
}
