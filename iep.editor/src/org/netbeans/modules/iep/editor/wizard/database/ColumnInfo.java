/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.iep.editor.wizard.database;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author radval
 */
public class ColumnInfo {

    private String mColumnName;
    
    private String mColumnDataType;
    
    private int mPrecision;
    
    private int mScale;
    
    private TableInfo mTable;
    
    public ColumnInfo(String columnName, String dataType, int precision, int scale) {
        this.mColumnName = columnName;
        this.mColumnDataType = dataType;
        this.mPrecision = precision;
        this.mScale = scale;
    }
    
    public String getColumnName() {
        return this.mColumnName;
    }
    
    public String getColumnDataType() {
        return this.mColumnDataType;
    }
    
    public int getPrecision() {
        return this.mPrecision;
    }
    
    public int getScale() {
        return this.mScale;
    }
    
    public void setTable(TableInfo table) {
        this.mTable = table;
    }
    
    public TableInfo getTable() {
        return this.mTable;
    }
    
    public String toString() {
        StringBuffer str = new StringBuffer();
        if(mColumnName != null) {
            str.append(mColumnName);
            
            if(mTable != null) {
                PrimaryKeyInfo pk = mTable.findPrimaryKey(this);
                ForeignKeyInfo fk = mTable.findForeignKey(this);
                boolean isColumnPK = false;
                boolean isColumnFK = false;
                if(pk != null && this.equals(pk.getColumn())) {
                    str.append("   [PK");
                    isColumnPK = true;
                }
                
                
                if(fk != null && this.equals(fk.getForeignKeyColumn())) {
                    isColumnFK = true;
                    if(isColumnPK) {
                        str.append("  FK]");
                    } else {
                        str.append("   [FK]");
                    }
                } 
                
                if(isColumnPK && !isColumnFK) {
                    str.append("]");
                }
            }

        }
        return str.toString();
    }
    
    public String getQualifiedName() {
        StringBuffer str = new StringBuffer();
        if(mColumnName != null) {
            boolean hasCatalog = false;
            boolean hasSchema = false;
            if(mTable != null) {
                String catalogName = mTable.getCatalogName();
                String schemaName = mTable.getSchemaName();
                String tableName = mTable.getTableName();
                if(catalogName != null && !catalogName.equals("")) {
                    str.append("\"");
                    str.append(catalogName);
                    str.append("\"");
                    hasCatalog = true;
                }
                
                if(schemaName != null && !schemaName.equals("")) {
                    if(hasCatalog) {
                        str.append(".");
                    }
                    str.append("\"");
                    str.append(schemaName);
                    str.append("\"");
                    hasSchema = true;
                }
                
                if(tableName != null && !tableName.equals("")) {
                    if(hasSchema) {
                        str.append(".");
                    }
                    str.append("\"");
                    str.append(tableName);
                    str.append("\"");
                    str.append(".");
                }
            }
            str.append("\"");
            str.append(mColumnName);
            str.append("\"");
        }
        return str.toString();
    }
    
    
    @Override
    public boolean equals(Object other) {
        boolean result = true;
        if(!(other instanceof ColumnInfo)) {
            result = false;
        }
        
        ColumnInfo otherColumn = (ColumnInfo) other;
        
        result &= this.getQualifiedName().equals(otherColumn.getQualifiedName()); 
        return result;
    }
    
    
    
    @Override
    public int hashCode() {
        int hashCode = getQualifiedName().hashCode();
        return hashCode;
        
    }
}
