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
package org.netbeans.modules.edm.editor.ui.view.property;

import org.netbeans.modules.edm.editor.utils.Attribute;
import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java.util.Vector;
import org.netbeans.modules.edm.model.DBColumn;
import org.netbeans.modules.edm.model.DBTable;
import org.netbeans.modules.edm.model.ForeignKey;
import org.netbeans.modules.edm.model.Index;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.editor.property.impl.DefaultPropertyEditor;
import org.netbeans.modules.edm.editor.ui.model.CollabSQLUIModel;
import org.openide.nodes.Node;


/**
 * @author Ritesh Adval
 */
public class TableProperties {
    protected ColumnPropertySupport fKeys;

    protected ColumnPropertySupport indices;

    protected ColumnPropertySupport uniqueCols;

    protected ColumnPropertySupport pKeys;
    
    protected CollabSQLUIModel model;

    private SQLDBTable table;
    
    public String getAliasName() {
        return table.getAliasName();
    }

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

    public String getForeignKeys() {
        return fKeys.getDisplayString();
    }

    public String getIndices() {
        return indices.getDisplayString();
    }

    public String getModelName() {
        return table.getParent().getModelName();
    }

    public String getPrimaryKeys() {
        return pKeys.getDisplayString();
    }

    public String getSchema() {
        return table.getSchema();
    }

    public String getCatalog() {
        return table.getCatalog();
    }

    public String getTablePrefix() {
        return table.getTablePrefix();
    }

    public String getUserDefinedTableName() {
        return table.getUserDefinedTableName();
    }

    public String getUserDefinedSchemaName() {
        return table.getUserDefinedSchemaName();
    }

    public String getUserDefinedCatalogName() {
        return table.getUserDefinedCatalogName();
    }

    public boolean isUseFullyQualifiedName() {
        return table.isUsingFullyQualifiedName();
    }

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

    public void setAliasName(String aName) {
        this.table.setAliasName(aName);
    }

    public void setTablePrefix(String tPrefix) {
        table.setTablePrefix(tPrefix);
        setDirty(true);
    }


    public void setUserDefinedTableName(String newName) {
        table.setUserDefinedTableName(newName);
        setDirty(true);
    }


    public void setUserDefinedSchemaName(String newName) {
        table.setUserDefinedSchemaName(newName);
        setDirty(true);
    }


    public void setUserDefinedCatalogName(String newName) {
        table.setUserDefinedCatalogName(newName);
        setDirty(true);
    }

    public void setUseFullyQualifiedName(boolean useFullName) {
        table.setUsingFullyQualifiedName(useFullName);
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
        
    }
}
