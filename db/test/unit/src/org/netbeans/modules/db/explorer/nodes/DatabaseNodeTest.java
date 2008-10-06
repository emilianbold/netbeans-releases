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

package org.netbeans.modules.db.explorer.nodes;

import java.util.Iterator;
import java.util.Vector;
import junit.framework.TestCase;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.DriverListNodeInfo;
import org.netbeans.modules.db.explorer.infos.RootNodeInfo;
import org.netbeans.modules.db.test.Util;
import org.openide.nodes.Node;

/**
 *
 * @author David
 */
public class DatabaseNodeTest extends TestCase {
    
    public DatabaseNodeTest(String testName) {
        super(testName);
    }        

    @Override
    public void setUp() throws Exception {
        Util.clearConnections();
        Util.deleteDriverFiles();
    }
    /**
     * Use case: create the root node, and verify that the expected
     * hierarchy of nodes and infos are created 
     */
    public void testRootHierarchy() throws Exception {
        // Initialize the tree with a driver and a connection
        JDBCDriver driver = Util.createDummyDriver();
        JDBCDriverManager.getDefault().addDriver(driver);
        
        DatabaseConnection conn = DatabaseConnection.create(
                driver, "jdbc:mark//twain", "tomsawyer", null, "whitewash", true);
        ConnectionManager.getDefault().addConnection(conn);
        
        // Need to force a refresh because otherwise it happens asynchronously
        // and this test does not pass reliably
        RootNodeInfo.getInstance().refreshChildren();
        
        checkConnection(RootNodeInfo.getInstance(), conn);

        checkInfoChildren(RootNodeInfo.getInstance());
        checkNodeChildren(RootNode.getInstance());
    }
    
    private void checkNodeChildren(final RootNode root) throws Exception {
        Node[] children = root.getChildren().getNodes(true);

        // The Driver List Node and the connection node should be the two
        // children
        assertEquals(2, children.length);
        assertTrue(children[0] instanceof DriverListNode);
        assertTrue(children[1] instanceof ConnectionNode); 
    }
    
    private void checkInfoChildren(DatabaseNodeInfo rootInfo) throws Exception {
        Vector children = rootInfo.getChildren();
        assertTrue(children.size() == 2);

        // These aren't sorted, and this is the order they come in
        assertTrue(children.get(0) instanceof ConnectionNodeInfo);
        assertTrue(children.get(1) instanceof DriverListNodeInfo);
    }
    
    private void checkConnection(DatabaseNodeInfo rootInfo, 
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
}
