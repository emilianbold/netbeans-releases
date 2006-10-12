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

package org.netbeans.modules.dbapi;

import java.net.URL;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.api.explorer.MetaDataListener;
import org.netbeans.modules.db.explorer.DbMetaDataListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author Andrei Badea
 */
public class DbMetaDataListenerImplTest extends NbTestCase {

    public DbMetaDataListenerImplTest(String testName) {
        super(testName);
    }

    /**
     * Tests the registered listeners are invoked when the tableChanged and tablesChanged
     * methods of DbMetaDataListenerImpl are invoked.
     */
    public void testListenerFired() throws Exception {
        JDBCDriver driver = JDBCDriver.create("foo", "Foo", "org.example.Foo", new URL[0]);
        DatabaseConnection dbconn = DatabaseConnection.create(driver, "url", "user", "schema", "pwd", false);

        class TestListener implements MetaDataListener {

            DatabaseConnection dbconn;
            String tableName;

            public void tablesChanged(DatabaseConnection dbconn) {
                this.dbconn = dbconn;
            }

            public void tableChanged(DatabaseConnection dbconn, String tableName) {
                this.dbconn = dbconn;
                this.tableName = tableName;
            }
        }

        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        FileObject listenersFO = FileUtil.createFolder(sfs.getRoot(), DbMetaDataListenerImpl.REFRESH_LISTENERS_PATH);
        FileObject listenerFO = listenersFO.createData("TestListener", "instance");
        TestListener listener = new TestListener();
        listenerFO.setAttribute("instanceCreate", listener);

        DbMetaDataListener dbListener = new DbMetaDataListenerImpl();

        assertNull(listener.dbconn);
        dbListener.tablesChanged(dbconn);
        assertSame(dbconn, listener.dbconn);

        listener.dbconn = null;
        assertNull(listener.dbconn);
        assertNull(listener.tableName);
        dbListener.tableChanged(dbconn, "TABLE");
        assertSame(dbconn, listener.dbconn);
        assertEquals("TABLE", listener.tableName);
    }
}
