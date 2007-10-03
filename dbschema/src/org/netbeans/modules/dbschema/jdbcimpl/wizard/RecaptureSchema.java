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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.dbschema.jdbcimpl.wizard;

import java.beans.*;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;

import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.jdbcimpl.*;
import org.openide.util.Exceptions;

public class RecaptureSchema {
    
    private static final boolean debug = Boolean.getBoolean("org.netbeans.modules.dbschema.recapture.debug");

    ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.dbschema.jdbcimpl.resources.Bundle"); //NOI18N

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
        try {
            SchemaElement.removeFromCache(elem.getName().getFullName() + "#" + fo1.getURL().toString()); //NOI18N
        } catch (FileStateInvalidException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } 
        
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
                        pf.finishProgress();

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
                        Exceptions.printStackTrace(exc);
                    }
                }
            }, 0);
        } catch (Exception exc) {
            String message = MessageFormat.format(bundle.getString("UnableToCreateSchema"), new String[] {exc.getMessage()}); //NOI18N
            StatusDisplayer.getDefault().setStatusText(message);
            if (debug) {
                Logger.getLogger("global").log(Level.INFO, null, exc);
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
            System.out.println("[dbschema-ccp] found dbconn='" + dbconn.getDatabaseURL() + "'");
        }
        data.setDatabaseConnection(dbconn);
        ConnectionHandler ch = new ConnectionHandler(data);
        if (ch.ensureConnection()) {
            dbconn = data.getDatabaseConnection();
            if (debug) {
                System.out.println("[dbschema-ccp] connection ensured ='" + dbconn.getDatabaseURL() + "'"); 
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
            if (url.equals(dbconns[i].getDatabaseURL())) {
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
