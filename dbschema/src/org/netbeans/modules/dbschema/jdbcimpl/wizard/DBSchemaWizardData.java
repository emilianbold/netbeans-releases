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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.jdbcimpl.wizard;

import java.beans.*;
import java.util.LinkedList;
import java.util.Vector;
import org.netbeans.api.db.explorer.DatabaseConnection;

import org.openide.loaders.DataFolder;

import org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider;

public class DBSchemaWizardData {

    private DataFolder destinationPackage;
    private String name;
    private String driver;
    private DatabaseConnection dbconn;
    private boolean existingConn;
    private ConnectionProvider cp;
    private LinkedList tables;
    private LinkedList views;
    private boolean connected;
    private String schema;
    private Vector schemas;
    private boolean all;

    private PropertyChangeSupport propertySupport;

    /** Creates new DBSchemaWizardData */
    public DBSchemaWizardData() {
        schemas = new Vector();
        propertySupport = new PropertyChangeSupport(this);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDestinationPackage(DataFolder destinationPackage) {
        this.destinationPackage = destinationPackage;
    }

    public DataFolder getDestinationPackage() {
        return destinationPackage;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getDriver() {
        return driver;
    }
    
    public void setDatabaseConnection(DatabaseConnection dbconn) {
        this.dbconn = dbconn;
    }
    
    public DatabaseConnection getDatabaseConnection() {
        return dbconn;
    }

    public void setExistingConn(boolean existingConn) {
        this.existingConn = existingConn;
    }

    public boolean isExistingConn() {
        return existingConn;
    }

    public void setConnectionProvider(ConnectionProvider cp) {
        this.cp = cp;
    }

    public ConnectionProvider getConnectionProvider() {
        return cp;
    }

    public void setTables(LinkedList tables) {
        this.tables = tables;
    }

    public LinkedList getTables() {
        return tables;
    }

    public void setViews(LinkedList views) {
        this.views = views;
    }

    public LinkedList getViews() {
        return views;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setSchemas(Vector schemas) {
        this.schemas = schemas;
    }

    public Vector getSchemas() {
        return schemas;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getSchema() {
        return schema;
    }

    public void setAllTables(boolean all) {
        this.all = all;
    }

    public boolean isAllTables() {
        return all;
    }

    //==== property change support needed for schemas ====
    public PropertyChangeSupport getPropertySupport() {
        return propertySupport;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertySupport.addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertySupport.removePropertyChangeListener (l);
    }
}
