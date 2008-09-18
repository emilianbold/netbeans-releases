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

package org.netbeans.modules.db.explorer.infos;

import java.util.Iterator;
import java.util.Vector;
import junit.framework.TestCase;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.test.Util;

/**
 *
 * @author David Van Couvering
 */
public class RootNodeInfoTest extends TestCase {
    public void testAddRemoveConnections() throws Exception {
        // Initialize the tree with a driver and a connection
        JDBCDriver driver = Util.createDummyDriver();
        JDBCDriverManager.getDefault().addDriver(driver);
        RootNodeInfo rootInfo = RootNodeInfo.getInstance();
        assertEquals(1, rootInfo.getChildren().size());
        validateConn2InfoCache(rootInfo);

        DatabaseConnection conn = DatabaseConnection.create(
                driver, "jdbc:mark//twain", "tomsawyer", null, "whitewash", true);
        ConnectionManager.getDefault().addConnection(conn);

        rootInfo.refreshChildren();
        assertEquals(2, rootInfo.getChildren().size());
        validateConn2InfoCache(rootInfo);


        DatabaseConnection conn2 = DatabaseConnection.create(
                driver, "jdbc:bob//dylan", "rolling", null, "stone", true);
        ConnectionManager.getDefault().addConnection(conn2);

        rootInfo.refreshChildren();
        assertEquals(3, rootInfo.getChildren().size());
        validateConn2InfoCache(rootInfo);


        ConnectionManager.getDefault().removeConnection(conn);

        rootInfo.refreshChildren();
        validateConn2InfoCache(rootInfo);
        Vector children = rootInfo.getChildren();
        assertEquals(2, children.size());
        checkConnection(rootInfo, conn2);

        ConnectionManager.getDefault().removeConnection(conn2);
        rootInfo.refreshChildren();
        assertEquals(1, rootInfo.getChildren().size());
        validateConn2InfoCache(rootInfo);
    }

    private void checkConnection(RootNodeInfo rootInfo,
            DatabaseConnection expected) throws Exception {

        Vector children = rootInfo.getChildren();
        for (Iterator it = children.iterator() ; it.hasNext() ; ) {
            Object next = it.next();
            if (next instanceof ConnectionNodeInfo) {
                ConnectionNodeInfo connInfo = (ConnectionNodeInfo)next;
                DatabaseConnection conn = connInfo.getDatabaseConnection().getDatabaseConnection();
                assertTrue(conn != null);
                assertTrue(conn.getDatabaseURL().equals(expected.getDatabaseURL()));
                assertTrue(conn.getUser().equals(expected.getUser()));
                assertTrue(conn.getPassword().equals(expected.getPassword()));
                assertTrue(conn.getDriverClass().equals(expected.getDriverClass()));
                return;
            }
        }
    }

    private void validateConn2InfoCache(RootNodeInfo rootInfo) throws Exception {
        org.netbeans.modules.db.explorer.DatabaseConnection[] connections = ConnectionList.getDefault().getConnections();

        assertEquals(connections.length, rootInfo.getConn2InfoCache().size());
        Vector<DatabaseNodeInfo> children = (Vector<DatabaseNodeInfo>)rootInfo.getChildren();
        assertEquals(children.size() - 1, connections.length);

        for (org.netbeans.modules.db.explorer.DatabaseConnection dbconn : connections) {
            DatabaseNodeInfo info = rootInfo.getConn2InfoCache().get(dbconn);
            assertNotNull(info);
            assertTrue(children.contains(info));
        }

        for (DatabaseNodeInfo child : children) {
            if (! (child instanceof ConnectionNodeInfo)) {
                continue;
            }

            assertTrue(rootInfo.getConn2InfoCache().containsValue(child));
        }
    }
}
