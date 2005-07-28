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

package org.netbeans.modules.db.explorer.infos;

import java.io.IOException;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Vector;

import org.netbeans.lib.ddl.DBConnection;
import org.netbeans.lib.ddl.DatabaseProductNotFoundException;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.SpecificationFactory;

import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseOption;

//commented out for 3.6 release, need to solve for next Studio release
//import org.netbeans.modules.db.explorer.PointbasePlus;
//import org.openide.nodes.Node;
//import org.netbeans.modules.db.explorer.nodes.ConnectionNode;

import org.netbeans.modules.db.explorer.nodes.RootNode;

public class ConnectionNodeInfo extends DatabaseNodeInfo implements ConnectionOperations {
    
    static final long serialVersionUID =-8322295510950137669L;

    public void connect(String dbsys) throws DatabaseException {
        String drvurl = getDriver();
        String dburl = getDatabase();
        
        try {
//commented out for 3.6 release, need to solve for next Studio release
            // check if there is connected connection by Pointbase driver
            // Pointbase driver doesn't permit the concurrently connection
//            if (drvurl.startsWith(PointbasePlus.DRIVER)) {
//                Node n[] = getParent().getNode().getChildren().getNodes();
//                for (int i = 0; i < n.length; i++)
//                    if (n[i] instanceof ConnectionNode) {
//                        ConnectionNodeInfo cinfo = (ConnectionNodeInfo)((ConnectionNode)n[i]).getInfo();
//                        if (cinfo.getDriver().startsWith(PointbasePlus.DRIVER))
//                            if (!(cinfo.getDatabase().equals(dburl)&&cinfo.getUser().equals(getUser())))
//                                if ((cinfo.getConnection()!=null))
//                                    throw new Exception(bundle.getString("EXC_PBConcurrentConn")); // NOI18N
//                    }
//            }

            DatabaseConnection con = new DatabaseConnection(drvurl, dburl, getUser(), getPassword());
            Connection connection = con.createJDBCConnection();
            
            finishConnect(dbsys, con, connection);
        } catch (Exception e) {
            DatabaseException dbe = new DatabaseException(e.getMessage());
            dbe.initCause(e);
            throw dbe;
        }
    }

    public void connect() throws DatabaseException {
        connect((String) null);
    }

    public void connectReadOnly() throws DatabaseException {
        setReadOnly(true);
        connect("GenericDatabaseSystem"); //NOI18N
    }

    public void connect(DBConnection conn) throws DatabaseException {
        try {
            String dbsys = null;
            DatabaseConnection con = (DatabaseConnection) conn;
            
            Connection connection = con.getConnection();
            
            SpecificationFactory factory = (SpecificationFactory) getSpecificationFactory();
            Specification spec;
            DriverSpecification drvSpec;

            if (dbsys != null) {
                spec = (Specification) factory.createSpecification(con, dbsys, connection);
            } else {
                setReadOnly(false);
                spec = (Specification) factory.createSpecification(con, connection);
            }
            put(DBPRODUCT, spec.getProperties().get(DBPRODUCT));

            setSpecification(spec);

            drvSpec = factory.createDriverSpecification(spec.getMetaData().getDriverName().trim());
            if (spec.getMetaData().getDriverName().trim().equals("jConnect (TM) for JDBC (TM)")) //NOI18N
                //hack for Sybase ASE - I don't guess why spec.getMetaData doesn't work
                drvSpec.setMetaData(connection.getMetaData());
            else
                drvSpec.setMetaData(spec.getMetaData());
            drvSpec.setCatalog(connection.getCatalog());
            drvSpec.setSchema(getSchema());
            setDriverSpecification(drvSpec);
            setConnection(connection); // fires change
        } catch (DatabaseProductNotFoundException e) {
            setReadOnly(false);
            connect("GenericDatabaseSystem"); //NOI18N
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }
    
    public void finishConnect(String dbsys, DatabaseConnection con, Connection connection) throws DatabaseException {
        try {
            SpecificationFactory factory = (SpecificationFactory) getSpecificationFactory();
            Specification spec;
            DriverSpecification drvSpec;

            if (dbsys != null) {
                spec = (Specification) factory.createSpecification(con, dbsys, connection);
            } else {
                setReadOnly(false);
                spec = (Specification) factory.createSpecification(con, connection);
            }
            put(DBPRODUCT, spec.getProperties().get(DBPRODUCT));

            setSpecification(spec);

            drvSpec = factory.createDriverSpecification(spec.getMetaData().getDriverName().trim());
            if (spec.getMetaData().getDriverName().trim().equals("jConnect (TM) for JDBC (TM)")) //NOI18N
                //hack for Sybase ASE - I don't guess why spec.getMetaData doesn't work
                drvSpec.setMetaData(connection.getMetaData());
            else
                drvSpec.setMetaData(spec.getMetaData());
            drvSpec.setCatalog(connection.getCatalog());
            drvSpec.setSchema(getSchema());
            setDriverSpecification(drvSpec);
            setConnection(connection); // fires change
        } catch (DatabaseProductNotFoundException e) {
            setReadOnly(false);
            connect("GenericDatabaseSystem"); //NOI18N
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }
    
    public void disconnect() throws DatabaseException {
        Connection connection = getConnection();
        if (connection != null) {
            try {
                connection.close();
                setConnection(null); // fires change
            } catch (Exception exc) {
                // connection is broken, connection state has been changed
                setConnection(null); // fires change
                
                String message = MessageFormat.format(bundle().getString("EXC_ConnectionIsBroken"), new String[] {exc.getMessage()}); // NOI18N
                throw new DatabaseException(message);
            }
        }
    }

    public void delete() throws IOException {
        try {
            Vector cons = RootNode.getOption().getConnections();
            DatabaseConnection cinfo = (DatabaseConnection) getDatabaseConnection();
            if (cons.contains(cinfo)) {
                cons.remove(cinfo);
                RootNode.getOption().save(); //serialize connection
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public Object put(Object key, Object obj) {
        if (key.equals(USER) || key.equals(DRIVER) || key.equals(DATABASE) || key.equals(SCHEMA)) {
            String newVal = (String)obj;
            updateConnection((String)key, newVal);
        }
        return super.put(key, obj);
    }
    
    private void updateConnection(String key, String newVal) {
        DatabaseOption option = RootNode.getOption();
        Vector cons = option.getConnections();
        DBConnection infoConn = getDatabaseConnection();
        int idx = cons.indexOf(infoConn);
        if(idx>=0) {
            DatabaseConnection connFromList = (DatabaseConnection)cons.elementAt(idx);
            if (key.equals(SCHEMA))
                connFromList.setSchema(newVal);
            else if (key.equals(USER))
                connFromList.setUser(newVal);
            else if (key.equals(DRIVER)) {
                connFromList.setDriver(newVal);
            } else if (key.equals(DATABASE)) {
                connFromList.setDatabase(newVal);
            }
        }
        setName(infoConn.getName());
        
        RootNode.getOption().save();
    }
    
}