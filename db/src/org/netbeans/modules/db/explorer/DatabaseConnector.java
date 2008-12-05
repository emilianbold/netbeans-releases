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

package org.netbeans.modules.db.explorer;

import java.sql.Connection;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.SpecificationFactory;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Rob Englander
 */
public class DatabaseConnector {

    // Thread-safe, no synchronization
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private DatabaseConnection databaseConnection;
    private Connection connection = null;
    private SpecificationFactory factory;
    private boolean readOnly = false;
    private Specification spec;
    private DriverSpecification drvSpec;
    private boolean connected = false;


    public DatabaseConnector(DatabaseConnection conn) {
        databaseConnection = conn;

        try {
            factory = new SpecificationFactory();
        } catch (DDLException e) {
            // throw a runtime exception?
        }
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    public void setRememberPassword(boolean val) {

    }

    public boolean getRememberPassword() {
        return true;
    }

    public Specification getDatabaseSpecification() {
        return spec;
    }

    public DriverSpecification getDriverSpecification() {
        return drvSpec;
    }
    
    public void connect() throws DatabaseException {
        try {
            Connection theConnection = databaseConnection.getConnection();
            databaseConnection.connectAsync();

            readOnly = false;

            spec = (Specification) factory.createSpecification(databaseConnection, theConnection);
            //put(DBPRODUCT, spec.getProperties().get(DBPRODUCT));

            drvSpec = factory.createDriverSpecification(spec.getMetaData().getDriverName().trim());
            if (spec.getMetaData().getDriverName().trim().equals("jConnect (TM) for JDBC (TM)")) //NOI18N
                //hack for Sybase ASE - I don't guess why spec.getMetaData doesn't work
                drvSpec.setMetaData(theConnection.getMetaData());
            else
                drvSpec.setMetaData(spec.getMetaData());
            drvSpec.setCatalog(theConnection.getCatalog());
            drvSpec.setSchema(databaseConnection.getSchema());
            setConnection(theConnection); // fires change
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }
    
    public void finishConnect(String dbsys, DatabaseConnection con, Connection connection) throws DatabaseException {
        try {
            if (dbsys != null) {
                spec = (Specification) factory.createSpecification(con, dbsys, connection);
                readOnly = true;
            } else {
                readOnly = false;
                spec = (Specification) factory.createSpecification(con, connection);
            }
            //put(DBPRODUCT, spec.getProperties().get(DBPRODUCT));

            drvSpec = factory.createDriverSpecification(spec.getMetaData().getDriverName().trim());
            if (spec.getMetaData().getDriverName().trim().equals("jConnect (TM) for JDBC (TM)")) //NOI18N
                //hack for Sybase ASE - I don't guess why spec.getMetaData doesn't work
                drvSpec.setMetaData(connection.getMetaData());
            else
                drvSpec.setMetaData(spec.getMetaData());
            drvSpec.setCatalog(connection.getCatalog());
            drvSpec.setSchema(con.getSchema());
            setConnection(connection); // fires change
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void disconnect() throws DatabaseException {
        if (connection != null) {
            String message = null;
            try {
                Connection con = connection;
                setConnection(null); // fires change
                con.close();
            } catch (Exception exc) {
                // connection is broken, connection state has been changed
                setConnection(null); // fires change

                //message = MessageFormat.format(bundle().getString("EXC_ConnectionError"), exc.getMessage()); // NOI18N
            }

            // XXX hack for Derby
            DerbyConectionEventListener.getDefault().afterDisconnect(getDatabaseConnection(), connection);

            if (message != null) {
                throw new DatabaseException(message);
            }

            databaseConnection.disconnect();
        }
    }

    public void setConnection(Connection con) throws DatabaseException
    {
        Connection oldval = connection;
        if (con != null) {
            if (oldval != null && oldval.equals(con)) return;
            connection = con;
            connected = true;
        } else {
            connection = null;
            connected = false;
        }

        //databaseConnection.getConnectionPCS().firePropertyChange(CONNECTION, oldval, databaseConnection);
        notifyChange();
    }

    public boolean supportsCommand(String cmd) {
        boolean supported = true;

        if (spec.getCommandProperties(cmd).containsKey("Supported")) {
            supported = spec.getCommandProperties(cmd).get("Supported").toString().equals("true");
        }

        return supported;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void notifyChange() {
        changeSupport.fireChange();
    }
}
