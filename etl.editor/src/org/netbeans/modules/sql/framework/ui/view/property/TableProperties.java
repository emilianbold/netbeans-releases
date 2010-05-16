/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.sql.framework.ui.view.property;

import com.sun.etl.utils.Attribute;
import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java.util.Vector;
import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.ForeignKey;
import org.netbeans.modules.sql.framework.model.Index;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
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
    
    private SQLDefinition def;

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
        }else if (property.getName().equals("modelName")) {
            Vector str = new Vector();
            str.add(table.getParent().getModelName());
            return new DefaultPropertyEditor.ListEditor(str);
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

    private List<String> getForeignKeyList(DBTable tbl, DBColumn column) {
        ArrayList<String> optionList = new ArrayList<String>();
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
     * Gets the current database-derived schema name.
     * 
     * @return current schema name as supplied by Database.
     */
    public String getSchema() {
        return table.getSchema();
    }

    /**
     * Gets the current Database-derived catalog name.
     * 
     * @return current catalog name as supplied by Database.
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
        ArrayList<String> pkList = new ArrayList<String>();
        ArrayList<String> fkList = new ArrayList<String>();
        ArrayList<String> idxList = new ArrayList<String>();
        Set<String> indexedUniqueCols = new HashSet<String>();

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
                List<String> fkListForColumn = getForeignKeyList(table, column);
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
        List<String> ndxCols = new ArrayList<String>(indexedUniqueCols);
        uniqueCols = new ColumnPropertySupport(ndxCols);
        
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

    public void setOrgProperty(String attrName, String newFileType) {
        table.setAttribute("ORGPROP_" + attrName , newFileType);
        setDirty(true);
    }
    
    public Attribute getOrgProperty(String attrName) {
        return table.getAttribute("ORGPROP_" + attrName);
    }
    
    protected void setDirty(boolean dirty) {
        if(editor instanceof BasicTopView) {
            BasicTopView topView = (BasicTopView)editor;
            topView.setDirty(dirty);
        }
    }
}
