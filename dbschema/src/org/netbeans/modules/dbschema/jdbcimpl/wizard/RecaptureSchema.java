/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.jdbcimpl.wizard;

import java.beans.*;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;

import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.jdbcimpl.*;

public class RecaptureSchema {
    
    private static final boolean debug = Boolean.getBoolean("org.netbeans.modules.dbschema.recapture.debug");

    ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.dbschema.jdbcimpl.resources.Bundle"); //NOI18N
    ResourceBundle bundleDB = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

    private DBSchemaWizardData data; 
    private Node dbSchemaNode;
    

    public RecaptureSchema(Node dbSchemaNode) {
        this.dbSchemaNode = dbSchemaNode;
        data = new DBSchemaWizardData();
        data.setExistingConn(true);
    }

    public void start() throws ClassNotFoundException, SQLException {
        final DBschemaDataObject dobj = (DBschemaDataObject)dbSchemaNode.getCookie(DBschemaDataObject.class);
        final SchemaElement elem = dobj.getSchema();
        //elem.
        //ConnectionProvider cp = new ConnectionProvider(elem.getDriver(), elem.getUrl(), elem.getUsername(), null);
        if (debug) {
            System.out.println("[dbschema] url='" + elem.getUrl() + "'");
        }
        final FileObject fo1 = dobj.getPrimaryFile();
        
        TableElement tableAndViewElements[] = elem.getTables();
        // now break down to tables and views
        final LinkedList tables = new LinkedList();
        final LinkedList views = new LinkedList();
        for (int i = 0; i < tableAndViewElements.length; i++) {
            TableElement te = tableAndViewElements[i];
            if (te.isTable()) {
                if (debug) {
                    System.out.println("[dbschema] adding table='" + te.getName() + "'");
                }
                tables.add(te.getName().getName());
            }
            else {
                if (debug) {
                    System.out.println("[dbschema] adding view='" + te.getName() + "'");
                }
                views.add(te.getName().getName());
            }
        }
        
        final boolean conned = data.isConnected();
        final boolean ec = data.isExistingConn();
        final DatabaseConnection dbconn = data.getDatabaseConnection();
//            final String target1 = target;
        final String dbIdentName = elem.getUrl();
            //dbconn.getName();
        if (debug) {
            System.out.println("[dbschema] conned='" + conned+ "'");
            System.out.println("[dbschema] ec='" + ec + "'");
            System.out.println("[dbschema] NEW dbIdentName='" + dbIdentName + "'");
        }
        final ConnectionProvider cp = createConnectionProvider(data, elem.getUrl());
        try {
            final ConnectionProvider c = cp;
            if (c == null) {
                throw new SQLException(bundle.getString("EXC_ConnectionNotEstablished"));
            }
            if (debug) {
                System.out.println("[dbschema] c.getConnection()='" + c.getConnection() + "'");
            }
            
            RequestProcessor.getDefault().post(new Runnable() {
                public void run () {
                    try {
                        StatusDisplayer.getDefault().setStatusText(bundle.getString("CreatingDatabaseSchema")); //NOI18N
                        
                        final ProgressFrame pf = new ProgressFrame();
                        final SchemaElementImpl sei = new SchemaElementImpl(c);
                        
                        PropertyChangeListener listener = new PropertyChangeListener() {
                            public void propertyChange(PropertyChangeEvent event) {
                                String message;
                                
                                if (event.getPropertyName().equals("totalCount")) { //NOI18N
                                    pf.setMaximum(((Integer)event.getNewValue()).intValue());
                                    return;
                                }

                                if (event.getPropertyName().equals("progress")) { //NOI18N
                                    pf.setValue(((Integer)event.getNewValue()).intValue());
                                    return;
                                }
                                
                                if (event.getPropertyName().equals("tableName")) { //NOI18N
                                    message = MessageFormat.format(bundle.getString("CapturingTable"), new String[] {((String) event.getNewValue()).toUpperCase()}); //NOI18N
                                    pf.setMessage(message);
                                    return;
                                }
                                
                                if (event.getPropertyName().equals("FKt")) { //NOI18N
                                    message = MessageFormat.format(bundle.getString("CaptureFK"), new String[] {((String) event.getNewValue()).toUpperCase(), bundle.getString("CaptureFKtable")}); //NOI18N
                                    pf.setMessage(message);
                                    return;
                                }
                                
                                if (event.getPropertyName().equals("FKv")) { //NOI18N
                                    message = MessageFormat.format(bundle.getString("CaptureFK"), new String[] {((String) event.getNewValue()).toUpperCase(), bundle.getString("CaptureFKview")}); //NOI18N
                                    pf.setMessage(message);
                                    return;
                                }
                                
                                if (event.getPropertyName().equals("viewName")) { //NOI18N
                                    message = MessageFormat.format(bundle.getString("CapturingView"), new String[] {((String) event.getNewValue()).toUpperCase()}); //NOI18N
                                    pf.setMessage(message);
                                    return;
                                }
                                
                                if (event.getPropertyName().equals("cancel")) { //NOI18N
                                    sei.setStop(true);
                                    StatusDisplayer.getDefault().setStatusText(""); //NOI18N
                                    return;
                                }
                            }
                        };
                        
                        pf.propertySupport.addPropertyChangeListener(listener);
                        pf.setVisible(true);
                        
                        sei.propertySupport.addPropertyChangeListener(listener);
                        final SchemaElement se = new SchemaElement(sei);
                        //se.setName(DBIdentifier.create(dbIdentName));
                        se.setName(elem.getName());
                        
                        sei.initTables(c, tables, views, false);

                        if (! sei.isStop()) {
                            fo1.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                                public void run() throws java.io.IOException {
                                    //FileObject fo1 = fo.createData(target1, "dbschema"); //NOI18N
                                    if (debug) {
                                        System.out.println("SchemaElement: " + dumpSe(se));
                                    }
                                    FileLock fl = fo1.lock();
                                    java.io.OutputStream out = fo1.getOutputStream(fl);
                                    if (out == null)
                                        throw new java.io.IOException("Unable to open output stream");

                                    pf.setMessage(bundle.getString("SavingDatabaseSchema")); //NOI18N
                                    StatusDisplayer.getDefault().setStatusText(bundle.getString("SavingDatabaseSchema")); //NOI18N

                                    se.save(out);
                                    fl.releaseLock();
                                }
                            });
                            
                            // refresh the node
                            SchemaElement.addToCache(se);
                            dobj.setSchemaElementImpl(sei);
                            dobj.setSchema(se);

                            pf.setMessage(bundle.getString("SchemaSaved")); //NOI18N
                            StatusDisplayer.getDefault().setStatusText(bundle.getString("SchemaSaved")); //NOI18N
                            
                            pf.setVisible(false);
                            pf.dispose();                        
                        }
                        
                        //c.closeConnection();
                        if (conned)
                            if (ec)
                                ConnectionManager.getDefault().disconnect(dbconn);
                            else
                                c.closeConnection();
                    } catch (Exception exc) {
                        ErrorManager.getDefault().notify(exc);
                    }
                }
            }, 0);
        } catch (Exception exc) {
            String message = MessageFormat.format(bundle.getString("UnableToCreateSchema"), new String[] {exc.getMessage()}); //NOI18N
            StatusDisplayer.getDefault().setStatusText(message);
            if (debug) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            }
            
            try {
                if (cp != null)
                    cp.closeConnection();
                    if (data.isConnected())
                        if (data.isExistingConn())
                            ConnectionManager.getDefault().disconnect(data.getDatabaseConnection());
                        else
                            cp.closeConnection();
            } catch (Exception exc1) {
                //unable to disconnect
            }
        }
    }
    
    private String dumpSe(SchemaElement se) {
        StringBuffer s = new StringBuffer();
        s.append("name " + se.getName());
        s.append("\n");
        s.append("driver " + se.getDriverName());
        s.append("\n");
        s.append("username " + se.getUsername());
        s.append("\n");
        TableElement tables[] = se.getTables();
        s.append("tables count " + tables.length);
        s.append("\n");
        for (int i = 0; i < tables.length; i++) {
            s.append("    table " + tables[i].getName());
            s.append("\n");
            ColumnElement columns[] = tables[i].getColumns();
            for (int j = 0; j < columns.length; j++) {
                s.append("        column " + columns[j].getName());
                s.append("\n");
            }
        }
        return s.toString();
    }
    
    public ConnectionProvider createConnectionProvider(DBSchemaWizardData data, String url) throws SQLException {
        
        DatabaseConnection dbconn = findDatabaseConnection(url);
        if (dbconn == null) {
            if (debug) {
                System.out.println("[dbschema-ccp] not found dbconn='" + dbconn + "'");
            }
            return null;
        }
        if (debug) {
            System.out.println("[dbschema-ccp] found dbconn='" + dbconn.getConnectionUrl() + "'");
        }
        data.setDatabaseConnection(dbconn);
        ConnectionHandler ch = new ConnectionHandler(data);
        if (ch.ensureConnection()) {
            dbconn = data.getDatabaseConnection();
            if (debug) {
                System.out.println("[dbschema-ccp] connection ensured ='" + dbconn.getConnectionUrl() + "'"); 
            }
            ConnectionProvider connectionProvider = 
                new ConnectionProvider(dbconn.getJDBCConnection(), dbconn.getDriverClass());
            connectionProvider.setSchema(dbconn.getSchema());
            //String schemaName = cni.getName();
            //schemaElementImpl.setName(DBIdentifier.create(schemaName));
            return connectionProvider;
        }
        if (debug) {
            System.out.println("[dbschema-ccp] connection not ensured, returning null");
        }
        return null;
    }
    
    private DatabaseConnection findDatabaseConnection(String url) {
        DatabaseConnection dbconns[] = ConnectionManager.getDefault().getConnections();
        for (int i = 0; i < dbconns.length; i++) {
            if (url.equals(dbconns[i].getConnectionUrl())) {
                return dbconns[i];
            }
        }
        return null;
    }
    
    private static class ConnectionHandler extends DBSchemaTablesPanel {
        public ConnectionHandler(DBSchemaWizardData data) {
            super(data, new ArrayList());
        }
        
        public boolean ensureConnection() {
            return init();
        }
    }
    
}
