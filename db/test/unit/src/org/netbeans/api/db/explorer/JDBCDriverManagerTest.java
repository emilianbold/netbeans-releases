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

import java.net.URL;
import org.netbeans.modules.db.explorer.driver.JDBCDriverConvertor;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;
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
        DataObject driver1DO = JDBCDriverConvertor.create(driver1);

        // Probably cannot be GCed, becaue of I cannot get this GCed due to:
        //
        // private static org.netbeans.api.db.explorer.JDBCDriverManager org.netbeans.api.db.explorer.JDBCDriverManager.DEFAULT->
        // org.netbeans.api.db.explorer.JDBCDriverManager@63f6ea-result->
        // org.openide.util.lookup.ProxyLookup$R@10ad419-this$0->
        // org.netbeans.modules.settings.RecognizeInstanceObjects$OverObjects@527386-lookups->
        // [Lorg.openide.util.Lookup;@e08edd-[0]->
        // org.openide.loaders.FolderLookup$ProxyLkp@1e53a48-lookups->
        // org.openide.util.lookup.AbstractLookup@19c0bd6-tree->
        // org.openide.util.lookup.ArrayStorage@13e4f82-content->
        // [Ljava.lang.Object;@177bebe-[0]->
        // org.openide.loaders.FolderLookup$ICItem@b34076-obj->
        // org.openide.loaders.XMLDataObject@147917a
        //
        // /*
        // assertEquals(1, JDBCDriverManager.getDefault().getDrivers().length);
        //
        // WeakReference driver1DORef = new WeakReference(driver1DO);
        // driver1DO = null;
        // assertGC("Can GC the driver's DataObject", driver1DORef);
        // */

        // this used to fail as described in issue 75204
        assertEquals(1, JDBCDriverManager.getDefault().getDrivers().length);
        assertSame(driver1, JDBCDriverManager.getDefault().getDrivers("org.bar.BarDriver")[0]);
    }
}
