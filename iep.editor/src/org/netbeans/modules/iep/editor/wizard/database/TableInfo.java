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
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author radval
 */
public class TableInfo implements Comparable {

    public static final String TABLE_CAT = "TABLE_CAT";
    
    public static final String TABLE_SCHEM = "TABLE_SCHEM";
    
    public static final String TABLE_NAME = "TABLE_NAME";
    
    public static final String TABLE_TYPE = "TABLE_TYPE";
    
    // String used in java.sql.DatabaseMetaData to indicate system tables.
    public static final String SYSTEM_TABLE = "SYSTEM TABLE"; // NOI18N

    // String used in java.sql.DatabaseMetaData to indicate system tables.
    public static final String TABLE = "TABLE"; // NOI18N

    // String used in java.sql.DatabaseMetaData to indicate system tables.
    public static final String VIEW = "VIEW"; // NOI18N

    private String mCatalogName;
    
    private String mSchemaName;
    
    private String mTableName;
    
    private String mTableType;
    
    private List<ColumnInfo> mColumns = new ArrayList<ColumnInfo>();
    
    private List<PrimaryKeyInfo> mPrimaryKeys = new ArrayList<PrimaryKeyInfo>();
    
    private List<ForeignKeyInfo> mForeignKeys = new ArrayList<ForeignKeyInfo>();
    
    public TableInfo(String catalogName,
                     String schemaName,
                     String tableName,
                     String tableType) {
        
        this.mCatalogName = catalogName;
        this.mSchemaName = schemaName;
        this.mTableName = tableName;
        this.mTableType = tableType;
    }
    
    public String getTableName() {
        return this.mTableName;
    }
    
    public String getSchemaName() {
        return this.mSchemaName;
    }
    
    public String getCatalogName() {
        return this.mCatalogName;
    }
    
    public String getTableType() {
        return this.mTableType;
    }
    
    public List<ColumnInfo> getColumns() {
        return this.mColumns;
    }
    
    public ColumnInfo findColumn(String columnName) {
        if(columnName == null) {
            return null;
        }
        
        ColumnInfo column = null;
        Iterator<ColumnInfo> it = this.mColumns.iterator();
        while(it.hasNext()) {
            ColumnInfo c = it.next();
            if(columnName.equals(c.getColumnName())) {
                column = c;
            }
        }
        
        return column;
    }
    
    public void addColumn(ColumnInfo column) {
    	if(column != null) {
    		column.setTable(this);
    		this.mColumns.add(column);
    	}
    }
    
    public void addPrimaryKey(PrimaryKeyInfo key) {
        this.mPrimaryKeys.add(key);
    }
    
    public List<PrimaryKeyInfo> getPrimaryKeys() {
        return this.mPrimaryKeys;
    }
    
    public void addForeignKey(ForeignKeyInfo key) {
        this.mForeignKeys.add(key);
    }
    
    public List<ForeignKeyInfo> getForeignKeys() {
        return this.mForeignKeys;
    }
    
    public PrimaryKeyInfo findPrimaryKey(ColumnInfo column) {
    	PrimaryKeyInfo pk = null;
    	
    	if(column == null) {
    		return null;
    	}
    	
    	if(this.mPrimaryKeys != null) {
    		Iterator<PrimaryKeyInfo> it = this.mPrimaryKeys.iterator();
    		while(it.hasNext()) {
    			PrimaryKeyInfo key = it.next();
    			ColumnInfo c = key.getColumn();
    			if(column.equals(c)) {
    				pk = key;
    				break;
    			}
    		}
    	}
    	return pk;
    }
    
    
    public PrimaryKeyInfo findPrimaryKey(String pkName) {
    	PrimaryKeyInfo pk = null;
    	
    	if(pkName == null) {
    		return null;
    	}
    	
    	if(this.mPrimaryKeys != null) {
    		Iterator<PrimaryKeyInfo> it = this.mPrimaryKeys.iterator();
    		while(it.hasNext()) {
    			PrimaryKeyInfo key = it.next();
    			
    			if(pkName.equals(key.getKeyName())) {
    				pk = key;
    				break;
    			}
    		}
    	}
    	return pk;
    }
    
    public ForeignKeyInfo findForeignKey(ColumnInfo column) {
    	ForeignKeyInfo fk = null;
    	
    	if(column == null) {
    		return null;
    	}
    	
    	if(this.mForeignKeys != null) {
    		Iterator<ForeignKeyInfo> it = this.mForeignKeys.iterator();
    		while(it.hasNext()) {
    			ForeignKeyInfo key = it.next();
    			ColumnInfo c = key.getForeignKeyColumn();
    			if(column.equals(c)) {
    				fk = key;
    				break;
    			}
    		}
    	}
    	return fk;
    }
    
    public void cleanUp() {
    	this.mColumns.clear();
    	this.mPrimaryKeys.clear();
    	this.mForeignKeys.clear();
    }
    
    public String toString() {
        StringBuffer str = new StringBuffer();
        
        boolean hasCatalog = false;
        boolean hasSchema = false;
        
        if(this.mCatalogName != null && !this.mCatalogName.equals("")) {
            str.append(this.mCatalogName);
            hasCatalog = true;
        }
        
        if(this.mSchemaName != null && !this.mSchemaName.equals("")) {
        	hasSchema = true;
            if(hasCatalog) {
                str.append(".");
            }
            str.append(this.mSchemaName);
        }
        
        if(this.mTableName != null && !this.mTableName.equals("")) {
            if(hasSchema) {
                str.append(".");
            }
            
            str.append(this.mTableName);
        }
        
        return str.toString();
    }

    public String getQualifiedName() {
    	StringBuffer str = new StringBuffer();
        String catalogName = getCatalogName();
        String schemaName = getSchemaName();
        String tableName = getTableName();
        
        boolean hasCatalog = false;
        boolean hasSchema = false;
                
        if(catalogName != null && !catalogName.equals("")) {
            hasCatalog = true;
            str.append("\"");
            str.append(catalogName);
            str.append("\"");
        }
        
        if(schemaName != null && !schemaName.equals("")) {
            if(hasCatalog) {
                str.append(".");
            }
            hasSchema = true;
            str.append("\"");
            str.append(schemaName);
            str.append("\"");
        }
        
        if(hasCatalog || hasSchema) {
            str.append(".");
        }
        
        str.append(tableName);
        
    	
    	return str.toString();
    }
    
    public int compareTo(Object o) {
        int result = this.toString().compareTo(o.toString());
        
        return result;
    }
    
    
    @Override
    public boolean equals(Object other) {
    	boolean result = true;
    	if(!(other instanceof TableInfo)) {
    		result = false;
    	}
    	
    	TableInfo otherTable = (TableInfo) other;
    	
    	result &= this.getQualifiedName().equals(otherTable.getQualifiedName()); 
    	return result;
    }
    
    
    
    @Override
    public int hashCode() {
    	int hashCode = getQualifiedName().hashCode();
    	return hashCode;
    	
    }
}

