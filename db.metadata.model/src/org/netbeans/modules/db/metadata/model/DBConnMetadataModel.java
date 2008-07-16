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

package org.netbeans.modules.db.metadata.model;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCMetadata;
import org.netbeans.modules.db.metadata.model.spi.MetadataFactory;
import org.openide.util.Mutex;

/**
 *
 * @author Andrei Badea
 */
public class DBConnMetadataModel implements MetadataModelImplementation {

    private final ReentrantLock lock = new ReentrantLock();
    private final WeakReference<DatabaseConnection> dbconnRef;

    private JDBCMetadata metadataImpl;
    private Metadata metadata;

    public DBConnMetadataModel(DatabaseConnection dbconn) {
        this.dbconnRef = new WeakReference<DatabaseConnection>(dbconn);
    }

    public void runReadAction(Action<Metadata> action) throws MetadataModelException {
        lock.lock();
        try {
            // Prevent dbconn from being GC'd while under read access
            // by holding it in a variable.
            DatabaseConnection dbconn = dbconnRef.get();
            if (dbconn == null) {
                return;
            }
            try {
                enterReadAccess(dbconn);
                if (metadata != null) {
                    action.run(metadata);
                }
            } catch (SQLException e) {
                throw new MetadataModelException(e);
            } catch (MetadataException e) {
                throw new MetadataModelException(e);
            }
        } finally {
            lock.unlock();
        }
    }

    private void enterReadAccess(final DatabaseConnection dbconn) throws SQLException {
        Connection conn = dbconn.getJDBCConnection();
        if (conn == null) {
            conn = Mutex.EVENT.readAccess(new org.openide.util.Mutex.Action<Connection>() {
                public Connection run() {
                    ConnectionManager.getDefault().showConnectionDialog(dbconn);
                    return dbconn.getJDBCConnection();
                }
            });
        }
        Connection oldConn = (metadata != null) ? metadataImpl.getConnection() : null;
        if (oldConn != conn) {
            // If the connection has been reconnected, reinit the metadata.
            String defaultSchemaName = dbconn.getSchema();
            if (defaultSchemaName.trim().length() == 0) {
                defaultSchemaName = null;
            }
            if (conn != null) {
                metadataImpl = new JDBCMetadata(conn, defaultSchemaName);
                metadata = MetadataFactory.createMetadata(metadataImpl);
            } else {
                metadataImpl = null;
                metadata = null;
            }
        }
    }
}
