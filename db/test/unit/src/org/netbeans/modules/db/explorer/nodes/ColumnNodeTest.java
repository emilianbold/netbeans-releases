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

package org.netbeans.modules.db.explorer.nodes;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DbMetaDataTransferProvider;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.test.TestBase;
import org.openide.util.Lookup;

/**
 *
 * @author Andrei Badea
 */
public class ColumnNodeTest extends TestBase {

    public ColumnNodeTest(String testName) {
        super(testName);
    }

    public void testClipboardCopy() throws Exception {
        assertNotNull("ColumnNode.clipboardCopy() needs an impl of DbMetaDataTransferProvider in the default lookup", Lookup.getDefault().lookup(DbMetaDataTransferProvider.class));

        JDBCDriver driver = JDBCDriver.create("foo", "Foo", "org.example.Foo", new URL[0]);
        JDBCDriverManager.getDefault().addDriver(driver);
        DatabaseConnection dbconn = DatabaseConnection.create(driver, "url", "user", "schema", "pwd", false);
        ConnectionManager.getDefault().addConnection(dbconn);

        ColumnNode columnNode = new ColumnNode();
        ConnectionNodeInfo connNodeInfo = (ConnectionNodeInfo)DatabaseNodeInfo.createNodeInfo(null, DatabaseNodeInfo.CONNECTION);
        connNodeInfo.setDatabaseConnection(ConnectionList.getDefault().getConnections()[0]);
        DatabaseNodeInfo tableNodeInfo = DatabaseNodeInfo.createNodeInfo(connNodeInfo, DatabaseNode.TABLE);
        DatabaseNodeInfo columnNodeInfo = DatabaseNodeInfo.createNodeInfo(tableNodeInfo, DatabaseNode.COLUMN);
        columnNode.setInfo(columnNodeInfo);

        assertTrue(columnNode.canCopy());

        Transferable transferable = (Transferable)columnNode.clipboardCopy();
        Set mimeTypes = new HashSet();
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            mimeTypes.add(flavors[i].getMimeType());
        }
        assertTrue(mimeTypes.contains("application/x-java-netbeans-dbexplorer-column; class=org.netbeans.modules.db.api.explorer.DatabaseMetaDataTransfer$Column"));
        assertTrue(mimeTypes.contains("application/x-java-openide-nodednd; mask=1; class=org.openide.nodes.Node"));
    }
}
