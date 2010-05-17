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
package org.netbeans.modules.dm.virtual.db.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.openide.util.NbBundle;

/**
 * 
 * @author Ahimanikya Satapathy
 */
public class VirtualDatabaseModel extends VirtualDBObject {

    private static Logger mLogger = Logger.getLogger(VirtualDatabaseModel.class.getName());

    private static final String FQ_TBL_NAME_SEPARATOR = ".";
    private static final String LOG_CATEGORY = VirtualDatabaseModel.class.getName();
    protected volatile String connectionName;
    protected volatile String name;
    protected Map<String, VirtualDBTable> tables;
    protected VirtualDBConnectionDefinition connectionDefinition;

    public VirtualDatabaseModel() {
        tables = new HashMap<String, VirtualDBTable>();
    }

    public VirtualDatabaseModel(String modelName, VirtualDBConnectionDefinition connDef) {
        this();

        if (connDef == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(VirtualDatabaseModel.class, "MSG_Null_connDef"));
        }

        String connName = connDef.getName();
        if (connName == null || connName.trim().length() == 0) {
            throw new IllegalArgumentException(NbBundle.getMessage(VirtualDatabaseModel.class, "MSG_connDef_Name"));
        }

        if (modelName == null || modelName.trim().length() == 0) {
            throw new IllegalArgumentException(NbBundle.getMessage(VirtualDatabaseModel.class, "MSG_Empty_modelName"));
        }

        name = modelName;
        connectionDefinition = new VirtualDBConnectionDefinition(connDef);
    }

    public boolean deleteTable(String tableName) {
        return tables.remove(tableName) != null;
    }

    public void setModelName(String name) {
        this.name = name;
    }

    public void addTable(VirtualDBTable table) {
        if (table != null) {
            table.setParent(this);
            tables.put(getFullyQualifiedTableName(table), table);
        }
    }

    @Override
    public VirtualDatabaseModel clone() {
        return this.clone();
    }

    public VirtualDBTable createTable(String tableName, String schemaName, String catalogName) {
        VirtualDBTable table = null;

        if (tableName == null || tableName.length() == 0) {
            throw new IllegalArgumentException(NbBundle.getMessage(VirtualDatabaseModel.class, "MSG_Null_tableName"));
        }

        table = new VirtualDBTable(tableName, schemaName, catalogName);
        addTable(table);

        return table;
    }

    @Override
    public boolean equals(Object refObj) {
        // Check for reflexivity.
        if (this == refObj) {
            return true;
        }

        boolean result = false;

        // Ensure castability (also checks for null refObj)
        if (refObj instanceof VirtualDatabaseModel) {
            VirtualDatabaseModel aSrc = (VirtualDatabaseModel) refObj;

            result = ((aSrc.name != null) ? aSrc.name.equals(name) : (name == null));
            mLogger.log(Level.INFO, NbBundle.getMessage(VirtualDatabaseModel.class, "LOG_ModelNames", LOG_CATEGORY) + result);
            boolean connCheck = (aSrc.connectionName != null) ? aSrc.connectionName.equals(connectionName) : (connectionName == null);
            mLogger.log(Level.INFO, NbBundle.getMessage(VirtualDatabaseModel.class, "LOG_ConnectionNames", LOG_CATEGORY) + connCheck);
            result &= connCheck;

            connCheck = ((aSrc.connectionDefinition != null) ? aSrc.connectionDefinition.equals(connectionDefinition) : (connectionDefinition == null));
            mLogger.log(Level.INFO, NbBundle.getMessage(VirtualDatabaseModel.class, "LOG_ConnectionDefs", LOG_CATEGORY) + connCheck);
            result &= connCheck;

            if (tables != null && aSrc.tables != null) {
                Set objTbls = aSrc.tables.keySet();
                Set myTbls = tables.keySet();

                // Must be identical (no subsetting), hence the pair of tests.
                boolean tblCheck = myTbls.containsAll(objTbls) && objTbls.containsAll(myTbls);
                mLogger.log(Level.INFO, NbBundle.getMessage(VirtualDatabaseModel.class, "LOG_TableNames", LOG_CATEGORY) + tblCheck);
                result &= tblCheck;
            }
        }

        mLogger.log(Level.INFO, NbBundle.getMessage(VirtualDatabaseModel.class, "LOG_refObj", LOG_CATEGORY) + result);
        return result;
    }

    public VirtualDBConnectionDefinition getVirtualDBConnectionDefinition() {
        return connectionDefinition;
    }

    public String getFullyQualifiedTableName(VirtualDBTable tbl) {
        return (tbl != null) ? getFullyQualifiedTableName(tbl.getName(), tbl.getSchema(), tbl.getCatalog()) : "";
    }

    public String getFullyQualifiedTableName(String tblName, String schName, String catName) {
        if (tblName == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(VirtualDatabaseModel.class, "MSG_Empty_tblName"));
        }

        StringBuilder buf = new StringBuilder(50);

        if (catName != null && catName.trim().length() != 0) {
            buf.append(catName.trim());
            buf.append(FQ_TBL_NAME_SEPARATOR);
        }

        if (schName != null && schName.trim().length() != 0) {
            buf.append(schName.trim());
            buf.append(FQ_TBL_NAME_SEPARATOR);
        }

        buf.append(tblName.trim());

        return buf.toString();
    }

    public Connection getJDBCConnection() throws Exception {
        Connection conn = null;
        String url = null;
        String pswd = null;

        try {
            VirtualDBConnectionDefinition cd = getVirtualDBConnectionDefinition();
            url = cd.getConnectionURL();
            id = cd.getUserName();
            pswd = cd.getPassword();

            if ((id != null) && (!"".equals(id))) {
                conn = VirtualDBConnectionFactory.getInstance().getConnection(url, id, pswd);
            } else {
                conn = VirtualDBConnectionFactory.getInstance().getConnection(url, null);
            }
        } catch (VirtualDBException ex) {
            Throwable cause = ex.getCause();
            if (cause == null) {
                cause = ex;
            }
            throw new Exception(cause);
        }

        return conn;
    }

    @Override
    public int hashCode() {
        int myHash = (name != null) ? name.hashCode() : 0;

        myHash += (connectionDefinition != null) ? connectionDefinition.hashCode() : 0;

        if (tables != null) {
            myHash += tables.keySet().hashCode();
        }

        return myHash;
    }

    public void setConnectionName(String theConName) {
        this.connectionName = theConName;
    }

    public void setTables(Map theTables) {
        this.tables = theTables;
    }

    public void setVirtualDBConnectionDefinition(VirtualDBConnectionDefinition def) {
        connectionDefinition = def;
    }

    @Override
    public String toString() {
        return getFullyQualifiedName();
    }

    private String getFullyQualifiedName() {
        return this.getModelName();
    }

    public VirtualDBTable getFileMatchingTableName(String tableName) {
        if (tableName == null) {
            return null;
        }

        Iterator iter = getTables().iterator();
        while (iter.hasNext()) {
            VirtualDBTable file = (VirtualDBTable) iter.next();
            if (tableName.equals(file.getTableName())) {
                return file;
            }
        }

        return null;
    }

    public VirtualDBTable getFileMatchingFileName(String aName) {
        if (aName == null) {
            return null;
        }

        Iterator iter = getTables().iterator();
        while (iter.hasNext()) {
            VirtualDBTable file = (VirtualDBTable) iter.next();
            if (aName.equals(file.getFileName())) {
                return file;
            }
        }

        return null;
    }

    public String getModelName() {
        return this.name;
    }

    public List<VirtualDBTable> getTables() {
        List list = Collections.EMPTY_LIST;
        Collection tableColl = tables.values();

        if (tableColl.size() != 0) {
            list = new ArrayList(tableColl.size());
            list.addAll(tableColl);
        }

        return Collections.unmodifiableList(list);
    }

    public VirtualDBTable getTable(String fqTableName) {
        return (VirtualDBTable) this.tables.get(fqTableName);
    }

    public VirtualDBTable getTable(String tableName, String schemaName, String catalogName) {
        Iterator it = this.tables.values().iterator();
        while (it.hasNext()) {
            VirtualDBTable table = (VirtualDBTable) it.next();
            String tName = table.getName();
            String tSchemaName = table.getSchema();
            String tCatalogName = table.getCatalog();

            boolean found = true;
            found = tName != null ? tName.equals(tableName) : tableName == null;
            found &= tSchemaName != null ? tSchemaName.equals(schemaName) : schemaName == null;
            found &= tCatalogName != null ? tCatalogName.equals(catalogName)
                    : (catalogName == null || catalogName.trim().equals(""));

            if (found) {
                return table;
            }
        }

        return null;
    }
}

