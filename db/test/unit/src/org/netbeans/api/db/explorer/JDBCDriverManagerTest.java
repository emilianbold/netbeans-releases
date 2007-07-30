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

package org.netbeans.api.db.explorer;

import java.lang.ref.WeakReference;
import java.net.URL;
import org.netbeans.modules.db.explorer.driver.JDBCDriverConvertor;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/**
 *
 * @author Andrei Badea
 */
public class JDBCDriverManagerTest extends TestBase {

    public JDBCDriverManagerTest(String testName) {
        super(testName);
    }

    /**
     * Tests that JDBCDriverManager manages the same instance that was
     * added using the {@link JDBCDriverManager#addDriver} method.
     */
    public void testSameInstanceAfterAdd() throws Exception {
        Util.deleteDriverFiles();

        JDBCDriver driver1 = JDBCDriver.create("bar_driver", "Bar Driver", "org.bar.BarDriver", new URL[0]);
        // JDBCDriverManager.getDefault().addDriver(driver1);
        DataObject driver1DO = JDBCDriverConvertor.create(driver1);

        // must recognize another XMLDataObject first, since the last one
        // is held in XMLDataObject.sharedParserImpl and can't be GC'd
        DataObject dobj = DataObject.find(Repository.getDefault().getDefaultFileSystem().getRoot().createData("foo.xml"));
        dobj.getCookie(OpenCookie.class);

        WeakReference driver1DORef = new WeakReference(driver1DO);
        driver1DO = null;
        assertGC("Can GC the driver's DataObject", driver1DORef);

        // this used to fail as described in issue 75204
        assertEquals(1, JDBCDriverManager.getDefault().getDrivers().length);
        assertSame(driver1, JDBCDriverManager.getDefault().getDrivers("org.bar.BarDriver")[0]);
    }
}
