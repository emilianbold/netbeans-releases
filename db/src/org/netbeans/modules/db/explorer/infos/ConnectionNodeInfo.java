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

package org.netbeans.modules.db.explorer.infos;

import java.io.IOException;
import java.sql.Connection;
import java.text.MessageFormat;

import org.netbeans.lib.ddl.DBConnection;
import org.netbeans.lib.ddl.DatabaseProductNotFoundException;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.SpecificationFactory;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DerbyConectionEventListener;

//commented out for 3.6 release, need to solve for next Studio release
//import org.netbeans.modules.db.explorer.PointbasePlus;
//import org.openide.nodes.Node;
//import org.netbeans.modules.db.explorer.nodes.ConnectionNode;

import org.openide.nodes.Children;
import org.openide.nodes.Node;

public class ConnectionNodeInfo extends DatabaseNodeInfo implements ConnectionOperations {
    
    static final long serialVersionUID =-8322295510950137669L;

    private void connect(String dbsys) throws DatabaseException {
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

    /*
     * Connects this connection node to the database.
     */
    public void connect() throws DatabaseException {
        connect((String)null);
    }

    /*
     * Connect to this node a DBConnection which is already connected to the
     * database. Used when adding a new connection: the newly added DBConnection is already
     * connected to the database, so this methods helps avoiding connecting to the
     * database once more.
     */
    public void connect(DBConnection conn) throws DatabaseException {
        try {
            DatabaseConnection con = (DatabaseConnection) conn;
            
            Connection connection = con.getConnection();
            
            SpecificationFactory factory = (SpecificationFactory) getSpecificationFactory();
            Specification spec;
            DriverSpecification drvSpec;

            setReadOnly(false);
            spec = (Specification) factory.createSpecification(con, connection);
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
            String message = null;
            try {
                connection.close();
                setConnection(null); // fires change
            } catch (Exception exc) {
                // connection is broken, connection state has been changed
                setConnection(null); // fires change
                
                message = MessageFormat.format(bundle().getString("EXC_ConnectionError"), new String[] {exc.getMessage()}); // NOI18N
            }
            
            // XXX hack for Derby
            DerbyConectionEventListener.getDefault().afterDisconnect(getDatabaseConnection(), connection);
            
            if (message != null) {
                throw new DatabaseException(message);
            }
        }
    }

    public void delete() throws IOException {
        try {
            DatabaseConnection cinfo = (DatabaseConnection) getDatabaseConnection();
            ConnectionList.getDefault().remove(cinfo);
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
        DatabaseConnection infoConn = getDatabaseConnection();
        DatabaseConnection connFromList = ConnectionList.getDefault().getConnection(infoConn);
        if (connFromList != null) {
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
    }

    public void refreshChildren() throws DatabaseException {
        Children children = getNode().getChildren();
        Node[] nodes = children.getNodes();
        for (int i = 0; i < nodes.length; i++) {
            DatabaseNodeInfo info = (DatabaseNodeInfo)nodes[i].getCookie(DatabaseNodeInfo.class);
            info.refreshChildren();
        }
    }
}
