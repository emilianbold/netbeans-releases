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

/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.impl;

import java.util.ResourceBundle;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBConnectionDefinition;
import org.openide.util.NbBundle;

/**
 * Implements DBConnectionDefinition interface for JDBC Database OTD.
 * 
 * @author
 */
public class DBConnectionDefinitionImpl implements DBConnectionDefinition {

    private String name;

    private String driverClass;

    private String url;

    private String userName;

    private String password;

    private String description;

    private String dbType;

    /**
     * Creates a new instance of DBConnectionDefinitionImpl with the given attributes.
     * 
     * @param connName connection name
     * @param driverName driver name
     * @param connUrl JDBC URL for this connection
     * @param uname username used to establish connection
     * @param passwd password used to establish connection
     * @param desc description of connection
     * @param type String concatenation of vendor and version number of source DB (e.g., "Oracle9",
     *            "JDBC11")
     */
    public DBConnectionDefinitionImpl(final String connName, final String driverName, final String connUrl, final String uname, final String passwd,
            final String desc, final String type) {
        this.name = connName;

        this.driverClass = driverName;
        this.url = connUrl;
        this.userName = uname;
        this.password = passwd;
        this.description = desc;
        this.dbType = type;
    }

    /**
     * Creates a new instance of DBConnectionDefinitionImpl using the values in the given
     * DBConnectionDefinition.
     * 
     * @param connectionDefn DBConnectionDefinition to be copied
     */
    public DBConnectionDefinitionImpl(final DBConnectionDefinition connectionDefn) {
        if (connectionDefn == null) {
            final ResourceBundle cMessages = NbBundle.getBundle(DBConnectionDefinitionImpl.class);
            throw new IllegalArgumentException(cMessages.getString("ERROR_NULL_DBCONNDEF") + "ERROR_NULL_DBCONNDEF");// NO
                                                                                                                        // i18n
        }

        this.copyFrom(connectionDefn);
    }

    /**
     * @see com.stc.model.database.DBConnectionDefinition#getConnectionURL()
     */
    public String getConnectionURL() {
        return this.url;
    }

    /**
     * Sets URL used to reference and establish a connection to the data source referenced in this
     * object.
     * 
     * @param newUrl URL pointing to the data source
     */
    public void setConnectionURL(final String newUrl) {
        this.url = newUrl;
    }

    /**
     * Gets user-defined description, if any, for this DBConnectionDefinition.
     * 
     * @return user-defined description, possibly null if none was defined
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @see com.stc.model.database.DatabaseModel#getDriverClass
     */
    public String getDriverClass() {
        return this.driverClass;
    }

    /**
     * Sets fully-qualified class name of driver used to establish a connection to the data source
     * referenced in this object
     * 
     * @param newClass new fully-qualified driver class name
     */
    public void setDriverClass(final String newClass) {
        this.driverClass = newClass;
    }

    /**
     * @see com.stc.model.database.DatabaseModel#getName
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets new name for this DBConnectionDefinition.
     * 
     * @param newName new name for DBConnectionDefinition
     */
    public void setName(final String newName) {
        this.name = newName;
    }

    /**
     * @see com.stc.model.database.DatabaseModel#getUserName
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Sets username used in authenticating a connection to the data source referenced in this
     * object.
     * 
     * @param newName new username, if any, to use for authentication purposes; may be null
     */
    public void setUserName(final String newName) {
        this.userName = newName;
    }

    /**
     * @see com.stc.model.database.DatabaseModel#getPassword
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets password used in authenticating a connection to the data source referenced in this
     * object.
     * 
     * @param newPw new password, if any, used for authentication purposes
     */
    public void setPassword(final String newPw) {
        this.password = newPw;
    }

    /**
     * @see com.stc.model.database.DBConnectionDefinition#getDBType
     */
    public String getDBType() {
        return this.dbType;
    }

    /**
     * Sets descriptive name of dbType of DB data source from which this metadata content was
     * derived, e.g., "Oracle9" for an Oracle 9i database, etc. Returns null if content was derived
     * from a non-DB source, such such as a flatfile.
     * 
     * @param newType vendor name of source database; null if derived from non-DB source
     */
    public void setType(final String newType) {
        this.dbType = newType;
    }

    /**
     * Copies member values to those contained in the given DBConnectionDefinition instance. Does
     * shallow copy of properties and flatfiles collections.
     * 
     * @param source DBConnectionDefinition whose contents are to be copied into this instance
     */
    public synchronized void copyFrom(final DBConnectionDefinition source) {
        if (source == null) {
            final ResourceBundle cMessages = NbBundle.getBundle(DBConnectionDefinitionImpl.class);
            throw new IllegalArgumentException(cMessages.getString("ERROR_NULL_REF") + "(ERROR_NULL_REF)");
        } else if (source == this) {
            return;
        }

        // classRef = source.getClassRef();
        this.description = source.getDescription();
        this.name = source.getName();
        this.dbType = source.getDBType();
        this.driverClass = source.getDriverClass();
        this.url = source.getConnectionURL();
        this.userName = source.getUserName();
        this.password = source.getPassword();
    }

}
