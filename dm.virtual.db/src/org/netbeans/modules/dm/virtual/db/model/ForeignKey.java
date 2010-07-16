/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.dm.virtual.db.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/**
 * @author Ahimanikya Satapathy
 */
public class ForeignKey implements Cloneable {

    private static final String RS_PK_NAME = "PK_NAME"; // NOI18N
    private static final String RS_PKCATALOG_NAME = "PKTABLE_CAT"; // NOI18N
    private static final String RS_PKSCHEMA_NAME = "PKTABLE_SCHEM"; // NOI18N
    private static final String RS_PKTABLE_NAME = "PKTABLE_NAME"; // NOI18N
    private static final String RS_PKCOLUMN_NAME = "PKCOLUMN_NAME"; // NOI18N
    private static final String RS_FK_NAME = "FK_NAME"; // NOI18N
    private static final String RS_FKCOLUMN_NAME = "FKCOLUMN_NAME"; // NOI18N
    private static final String RS_UPDATE_RULE = "UPDATE_RULE"; // NOI18N
    private static final String RS_DELETE_RULE = "DELETE_RULE"; // NOI18N
    private static final String RS_DEFERRABILITY = "DEFERRABILITY"; // NOI18N
    private int deferrability;
    private int deleteRule;
    private List<String> fkColumnNames = new ArrayList<String>();
    private String fkName;
    private VirtualDBTable parent;
    private String pkCatalog;
    private List<String> pkColumnNames = new ArrayList<String>();
    private String pkName;
    private String pkSchema;
    private String pkTable;
    private int updateRule;

    public static Map<String, ForeignKey> createForeignKeyColumnMap(VirtualDBTable table, ResultSet rs)
            throws SQLException {
        if (rs == null) {
            Locale locale = Locale.getDefault();
            ResourceBundle cMessages = ResourceBundle.getBundle("org/netbeans/modules/dm/virtual/db/model/impl/Bundle", locale); // NO i18n
            throw new IllegalArgumentException(
                    cMessages.getString("ERROR_NULL_RS") + "(ERROR_NULL_RS)");
        }

        Map<String, ForeignKey> fkColumns = new HashMap<String, ForeignKey>();
        while (rs.next()) {
            ForeignKey fk = (ForeignKey) fkColumns.get(rs.getString(RS_FK_NAME));
            if (fk != null) {
                fk.addColumnNames(rs);
            } else {
                fk = new ForeignKey(rs);
                fk.setParent(table);
                fkColumns.put(fk.getName(), fk);
            }
        }
        return fkColumns;
    }

    private ForeignKey(ResultSet rs) throws SQLException {
        if (rs == null) {
            Locale locale = Locale.getDefault();
            ResourceBundle cMessages = ResourceBundle.getBundle("org/netbeans/modules/dm/virtual/db/model/impl/Bundle", locale); // NO i18n            
            throw new IllegalArgumentException(
                    cMessages.getString("ERROR_VALID_RS") + "(ERROR_VALID_RS)");
        }
        //parent = fkTable;
        fkName = rs.getString(RS_FK_NAME);
        pkName = rs.getString(RS_PK_NAME);

        pkTable = rs.getString(RS_PKTABLE_NAME);
        pkSchema = rs.getString(RS_PKSCHEMA_NAME);

        pkCatalog = rs.getString(RS_PKCATALOG_NAME);
        addColumnNames(rs);

        //rs.getShort(RS_SEQUENCE_NUM)

        updateRule = rs.getShort(RS_UPDATE_RULE);
        deleteRule = rs.getShort(RS_DELETE_RULE);
        deferrability = rs.getShort(RS_DEFERRABILITY);
    }

    public ForeignKey(VirtualDBTable fkTable, String foreignKeyName, String primaryKeyName, String primaryKeyTable, String primaryKeySchema,
            String primaryKeyCatalog, int updateFlag, int deleteFlag, int deferFlag) {
        parent = fkTable;
        fkName = foreignKeyName;
        pkName = primaryKeyName;

        pkTable = primaryKeyTable;
        pkSchema = primaryKeySchema;
        pkCatalog = primaryKeyCatalog;

        updateRule = updateFlag;
        deleteRule = deleteFlag;
        deferrability = deferFlag;
    }

    public ForeignKey(ForeignKey src) {
        if (src == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(ForeignKey.class, "MSG_Null_ForeignKey"));
        }

        copyFrom(src);
    }

    public void addColumnNames(ResultSet rs) throws SQLException {

        String pkColName = rs.getString(RS_PKCOLUMN_NAME);
        if (!VirtualDBUtil.isNullString(pkColName)) {
            pkColumnNames.add(pkColName);
        }

        String fkColName = rs.getString(RS_FKCOLUMN_NAME);
        if (!VirtualDBUtil.isNullString(pkColName)) {
            fkColumnNames.add(fkColName);
        }
    }

    @Override
    public Object clone() {
        try {
            ForeignKey impl = (ForeignKey) super.clone();
            impl.pkColumnNames = new ArrayList<String>(this.pkColumnNames);
            impl.fkColumnNames = new ArrayList<String>(this.fkColumnNames);

            return impl;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    public boolean contains(VirtualDBColumn fkCol) {
        return contains(fkCol.getName());
    }

    public boolean contains(String fkColumnName) {
        return fkColumnNames.contains(fkColumnName);
    }

    @Override
    public boolean equals(Object refObj) {
        if (this == refObj) {
            return true;
        }

        if (!(refObj instanceof ForeignKey)) {
            return false;
        }

        ForeignKey ref = (ForeignKey) refObj;

        boolean result = (fkName != null) ? fkName.equals(ref.fkName) : (ref.fkName == null);
        result &= (pkName != null) ? pkName.equals(ref.pkName) : (ref.pkName == null);
        result &= (pkTable != null) ? pkTable.equals(ref.pkTable) : (ref.pkTable == null);
        result &= (pkSchema != null) ? pkSchema.equals(ref.pkSchema) : (ref.pkSchema == null);
        result &= (pkCatalog != null) ? pkCatalog.equals(ref.pkCatalog) : (ref.pkCatalog == null);
        result &= (updateRule == ref.updateRule) && (deleteRule == ref.deleteRule) && (deferrability == ref.deferrability);
        result &= (pkColumnNames != null) ? pkColumnNames.equals(ref.pkColumnNames) : (ref.pkColumnNames != null);
        result &= (fkColumnNames != null) ? fkColumnNames.equals(ref.fkColumnNames) : (ref.fkColumnNames != null);

        return result;
    }

    public int getColumnCount() {
        return fkColumnNames.size();
    }

    public String getColumnName(int iColumn) {
        return fkColumnNames.get(iColumn);
    }

    public List<String> getColumnNames() {
        return Collections.unmodifiableList(fkColumnNames);
    }

    public int getDeferrability() {
        return deferrability;
    }

    public int getDeleteRule() {
        return deleteRule;
    }

    public String getMatchingPKColumn(String fkColumnName) {
        ListIterator it = fkColumnNames.listIterator();
        while (it.hasNext()) {
            String colName = (String) it.next();
            if (colName.equals(fkColumnName.trim())) {
                return pkColumnNames.get(it.previousIndex());
            }
        }

        return null;
    }

    public String getName() {
        return fkName;
    }

    public VirtualDBTable getParent() {
        return parent;
    }

    public String getPKCatalog() {
        return pkCatalog;
    }

    public List<String> getPKColumnNames() {
        return Collections.unmodifiableList(pkColumnNames);
    }

    public String getPKName() {
        return pkName;
    }

    public String getPKSchema() {
        return pkSchema;
    }

    public String getPKTable() {
        return pkTable;
    }

    public int getSequence(VirtualDBColumn col) {
        if (col == null || col.getName() == null) {
            return -1;
        }

        return fkColumnNames.indexOf(col.getName().trim());
    }

    public int getUpdateRule() {
        return updateRule;
    }

    @Override
    public int hashCode() {
        int myHash = (fkName != null) ? fkName.hashCode() : 0;

        myHash += (pkName != null) ? pkName.hashCode() : 0;
        myHash += (pkTable != null) ? pkTable.hashCode() : 0;
        myHash += (pkSchema != null) ? pkSchema.hashCode() : 0;
        myHash += (pkCatalog != null) ? pkCatalog.hashCode() : 0;
        myHash += updateRule + deleteRule + deferrability;
        myHash += (fkColumnNames != null) ? fkColumnNames.hashCode() : 0;
        myHash += (pkColumnNames != null) ? pkColumnNames.hashCode() : 0;

        return myHash;
    }

    public boolean references(VirtualDBTable aTable) {
        return (aTable != null) ? references(aTable.getName(), aTable.getSchema(), aTable.getCatalog()) : false;
    }

    public boolean references(PrimaryKey pk) {
        if (pk == null) {
            return false;
        }

        List<String> targetColNames = pk.getColumnNames();
        VirtualDBTable targetTable = pk.getParent();

        return references(targetTable) && targetColNames.containsAll(pkColumnNames) && pkColumnNames.containsAll(targetColNames);
    }

    public boolean references(String pkTableName, String pkSchemaName, String pkCatalogName) {
        if (pkCatalogName.equals("")) {
            pkCatalogName = null;
        }
        if (pkSchemaName.equals("")) {
            pkSchemaName = null;
        }
        if (pkTableName.equals("")) {
            pkTableName = null;
        }

        boolean tableMatches = (pkTableName != null) ? pkTableName.equals(pkTable) : (pkTable == null);
        boolean schemaMatches = (pkSchemaName != null) ? pkSchemaName.equals(pkSchema) : (pkSchema == null);
        boolean catalogMatches = (pkCatalogName != null) ? pkCatalogName.equals(pkCatalog) : (pkCatalog == null);
        return tableMatches && schemaMatches && catalogMatches;
    }

    public void setColumnNames(List fkColumns, List pkColumns) {
        fkColumnNames.clear();
        pkColumnNames.clear();

        if (fkColumns == null || pkColumns == null) {
            return;
        }

        if (fkColumns.size() != pkColumns.size()) {
            throw new IllegalArgumentException(NbBundle.getMessage(ForeignKey.class, "MSG_Size_Unidentical"));
        }

        for (ListIterator it = fkColumns.listIterator(); it.hasNext();) {
            String myFkName = (String) it.next();
            String myPkName = (String) pkColumns.get(it.previousIndex());

            if (myFkName != null && myPkName != null) {
                fkColumnNames.add(myFkName);
                pkColumnNames.add(myPkName);
            }
        }
    }

    public void setParent(VirtualDBTable newParent) {
        parent = newParent;
    }

    private void copyFrom(ForeignKey src) {
        parent = src.getParent();

        fkName = src.getName();
        fkColumnNames.clear();
        fkColumnNames.addAll(src.getColumnNames());

        pkName = src.getPKName();
        pkCatalog = src.getPKCatalog();
        pkSchema = src.getPKSchema();
        pkTable = src.getPKTable();
        pkColumnNames.clear();
        pkColumnNames.addAll(src.getPKColumnNames());

        // Set cascade attributes
        updateRule = src.getUpdateRule();
        deleteRule = src.getDeleteRule();
        deferrability = src.getDeferrability();
    }
}

