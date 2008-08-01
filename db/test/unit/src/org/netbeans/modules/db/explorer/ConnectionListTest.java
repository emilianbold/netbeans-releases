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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.explorer;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;
import org.openide.loaders.DataObject;

/**
 *
 * @author Andrei Badea
 */
public class ConnectionListTest extends TestBase {

    public ConnectionListTest(String testName) {
        super(testName);
    }

    /**
     * Tests that ConnectionManager manages the same instance that was
     * added using the {@link ConnectionManager#addConnection} method.
     */
    public void testSameInstanceAfterAdd() throws Exception {
        Util.clearConnections();
        assertEquals(0, ConnectionList.getDefault().getConnections().length);

        DatabaseConnection dbconn = new DatabaseConnection("org.bar.BarDriver",
                "bar_driver", "jdbc:bar:localhost", "schema", "user", "password", true);
        // temporary: should actually call addDriver(), but that doesn't return a DataObject
        // ConnectionManager.getDefault().addConnection(dbconn);
        DataObject dbconnDO = DatabaseConnectionConvertor.create(dbconn);

        /* Commenting out until 75204 is fixed
        Reference dbconnDORef = new WeakReference(dbconnDO);
        dbconnDO = null;
        assertGC("Should not be able to GC dobj", dbconnDORef);
         */

        assertEquals(1, ConnectionList.getDefault().getConnections().length);
        /* Commenting out until 75204 is fixed
        assertSame(dbconn, ConnectionList.getDefault().getConnections()[0]);
         */
    }
}
