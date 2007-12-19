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
package org.netbeans.modules.mashup.db.model;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.netbeans.modules.etl.model.ETLObject;
import org.netbeans.modules.etl.model.impl.ETLObjectImpl;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDatabaseModelImpl;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.JDBCConnectionProvider;


/**
 * @author Jonathan Giron
 * @version $Revision$
 */
public class FlatfileDefinition extends ETLObjectImpl implements ETLObject, JDBCConnectionProvider   {

    /* Database metadata model storing record/column formatting for underlying Flatfile DB */
    private FlatfileDatabaseModelImpl modelImpl;

    /**
     * Constructs a default instance of FlatfileDefinition.
     */
    public FlatfileDefinition()  {
        super(null);
    }

    /**
     * Constructs a new instance of FlatfileDefinition with the given name.
     * 
     * @param name the name
     */
    public FlatfileDefinition(String name) {
    	super(name);
    }

    /**
     * @see org.netbeans.modules.mashup.db.model.FlatfileDefinition#getMetadataSourceOID
     */
    public String getMetadataSourceOID() {
        return UUID.randomUUID().toString();
    }

    public String getInstanceName() {
        return null;
    }

    public void setInstanceName(String newName) {
    }

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getModelName
     */
    public String getModelName() {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        if (modelImpl != null) {
            return modelImpl.getModelName();
        }
        String name = null;

        return name;
    }

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getModelName
     */
    public String getModelDescription() {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        return (modelImpl != null) ? modelImpl.getModelDescription() : "";
    }

    /**
     * Gets associated DBConnectionDefinition
     * 
     * @return DBConnectionDefinition
     */
    public DBConnectionDefinition getConnectionDefinition() {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        return (modelImpl != null) ? modelImpl.getConnectionDefinition() : null;
    }

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getFullyQualifiedTableName(DBTable)
     */
    public String getFullyQualifiedTableName(DBTable tbl) {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        return (modelImpl != null) ? modelImpl.getFullyQualifiedTableName(tbl) : "";
    }

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getFullyQualifiedTableName(String,String,String)
     */
    public String getFullyQualifiedTableName(String table, String schema, String catalog) {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        return (modelImpl != null) ? modelImpl.getFullyQualifiedTableName(table, schema, catalog) : "";
    }

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getSource()
     */
    public Object getSource() {
        return this;
    }

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getTable(String)
     */
    public DBTable getTable(String fqTableName) {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        return (modelImpl != null) ? modelImpl.getTable(fqTableName) : null;
    }

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getTable
     */
    public DBTable getTable(String tableName, String schemaName, String catalogName) {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        return (modelImpl != null) ? modelImpl.getTable(tableName, schemaName, catalogName) : null;
    }

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getTables
     */
    public List getTables() {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        return (modelImpl != null) ? modelImpl.getTables() : Collections.EMPTY_LIST;
    }

    private void createDatabaseModel() {
        
    }

    /**
     * @return Returns the modelImpl.
     */
    public FlatfileDatabaseModel getFlatfileDatabaseModel() {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        return modelImpl;
    }

    /**
     * @param modelImpl The modelImpl to set.
     */
    public void setFlatfileDatabaseModel(FlatfileDatabaseModel theModelImpl) {
        this.modelImpl = (FlatfileDatabaseModelImpl) theModelImpl;
        this.modelImpl.setSource(this);
    }

    /**
     * @see org.netbeans.modules.model.database.JDBCConnectionProvider#getJDBCDriverClassNames()
     */
    public List getJDBCDriverClassNames() throws Exception {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        if (modelImpl != null) {
            return modelImpl.getJDBCDriverClassNames();
        }

        throw new Exception("Could not create DatabaseModel implementation for FlatfileDB Database");
    }

    /**
     * @see org.netbeans.modules.model.database.JDBCConnectionProvider#getJDBCDriverClassName()
     */
    public String getJDBCDriverClassName() throws Exception {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        if (modelImpl != null) {
            return modelImpl.getJDBCDriverClassName();
        }

        throw new Exception("Could not create DatabaseModel implementation for FlatfileDB Database");
    }

    /**
     * @see org.netbeans.modules.model.database.JDBCConnectionProvider#getJDBCDriverType()
     */
    public int getJDBCDriverType() throws Exception {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        if (modelImpl != null) {
            return modelImpl.getJDBCDriverType();
        }

        throw new Exception("Could not create DatabaseModel implementation for FlatfileDB Database");
    }

    /**
     * @see org.netbeans.modules.model.database.JDBCConnectionProvider#getJDBCDriverTypes(java.lang.String)
     */
    public int getJDBCDriverTypes(String className) throws Exception {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        if (modelImpl != null) {
            return modelImpl.getJDBCDriverTypes(className);
        }

        throw new Exception("Could not create DatabaseModel implementation for FlatfileDB Database");
    }

    /**
     * @see org.netbeans.modules.model.database.JDBCConnectionProvider#getJDBCConnection()
     */
    public Connection getJDBCConnection() throws Exception {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        if (modelImpl != null) {
            return modelImpl.getJDBCConnection();
        }

        throw new Exception("Could not create DatabaseModel implementation for FlatfileDB Database");

    }

    /**
     * @see org.netbeans.modules.model.database.JDBCConnectionProvider#getJDBCConnection(java.lang.ClassLoader)
     */
    public Connection getJDBCConnection(ClassLoader cl) throws Exception {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        if (modelImpl != null) {
            return modelImpl.getJDBCConnection(cl);
        }

        throw new Exception("Could not create DatabaseModel implementation for FlatfileDB Database");
    }

    /**
     * @see org.netbeans.modules.model.database.JDBCConnectionProvider#getJDBCConnection(java.util.Properties)
     */
    public Connection getJDBCConnection(Properties connProps) throws Exception {
        if (connProps == null) {
            throw new IllegalArgumentException("connProps argument is null!");
        }

        if (modelImpl == null) {
            createDatabaseModel();
        }

        if (modelImpl != null) {
            return modelImpl.getJDBCConnection(connProps);
        }

        throw new Exception("Could not create DatabaseModel implementation for FlatfileDB Database");
    }

    /**
     * @see org.netbeans.modules.model.database.JDBCConnectionProvider#getJDBCConnection(java.util.Properties,
     *      java.lang.ClassLoader)
     */
    public Connection getJDBCConnection(Properties connProps, ClassLoader cl) throws Exception {
        if (connProps == null) {
            throw new IllegalArgumentException("connProps argument is null!");
        }

        if (modelImpl == null) {
            createDatabaseModel();
        }

        if (modelImpl != null) {
            return modelImpl.getJDBCConnection(connProps, cl);
        }

        throw new Exception("Could not create DatabaseModel implementation for FlatfileDB Database");
    }

    /**
     * @see org.netbeans.modules.model.database.JDBCConnectionProvider#getJDBCConnection(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public Connection getJDBCConnection(String jdbcUrl, String uid, String passwd) throws Exception {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        if (modelImpl != null) {
            return modelImpl.getJDBCConnection(jdbcUrl, uid, passwd);
        }

        throw new Exception("Could not create DatabaseModel implementation for FlatfileDB Database");
    }

    /**
     * @see org.netbeans.modules.model.database.JDBCConnectionProvider#getJDBCConnection(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.ClassLoader)
     */
    public Connection getJDBCConnection(String jdbcUrl, String uid, String passwd, ClassLoader cl) throws Exception {
        if (modelImpl == null) {
            createDatabaseModel();
        }

        if (modelImpl != null) {
            return modelImpl.getJDBCConnection(jdbcUrl, uid, passwd, cl);
        }

        throw new Exception("Could not create DatabaseModel implementation for FlatfileDB Database");
    }
}

