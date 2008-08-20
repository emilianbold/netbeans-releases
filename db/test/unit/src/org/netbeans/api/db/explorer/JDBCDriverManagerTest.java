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

package org.netbeans.api.db.explorer;

import java.net.URL;
import java.sql.Driver;
import org.netbeans.modules.db.explorer.driver.JDBCDriverConvertor;
import org.netbeans.modules.db.test.Util;
import org.netbeans.modules.db.test.DBTestBase;
import org.openide.loaders.DataObject;

/**
 *
 * @author Andrei Badea
 */
public class JDBCDriverManagerTest extends DBTestBase {
    public JDBCDriverManagerTest(String testName) {
        super(testName);
    }

    /**
     * Tests that JDBCDriverManager manages the same instance that was
     * added using the {@link JDBCDriverManager#addDriver} method.
     */
    public void testSameInstanceAfterAdd() throws Exception {
        Util.deleteDriverFiles();
        assertEquals(0, JDBCDriverManager.getDefault().getDrivers().length);

        JDBCDriver driver = JDBCDriver.create("bar_driver", "Bar Driver", "org.bar.BarDriver", new URL[0]);
        // temporary: should actually call addDriver(), but that doesn't return a DataObject
        // JDBCDriverManager.getDefault().addDriver(driver1);
        DataObject driverDO = JDBCDriverConvertor.create(driver);

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
        /* Commenting out until 75204 is fixed
        // assertEquals(1, JDBCDriverManager.getDefault().getDrivers().length);
        WeakReference driverDORef = new WeakReference(driverDO);
        driverDO = null;
        assertGC("Can GC the driver's DataObject", driverDORef);
        */

        // this used to fail as described in issue 75204
        assertEquals(1, JDBCDriverManager.getDefault().getDrivers().length);
        /* Still failing, commenting out until 75204 is fixed
        assertSame(driver, JDBCDriverManager.getDefault().getDrivers("org.bar.BarDriver")[0]);
         */
    }

    public void testGetDriver() throws Exception {
        JDBCDriver jdbcDriver = getJDBCDriver();
        Driver driver = jdbcDriver.getDriver();

        assertNotNull(driver);

    }
}
