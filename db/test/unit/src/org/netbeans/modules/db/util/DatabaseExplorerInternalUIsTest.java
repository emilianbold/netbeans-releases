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

package org.netbeans.modules.db.util;

import java.net.URL;
import javax.swing.JComboBox;
import org.netbeans.api.db.explorer.*;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Andrei Badea
 */
public class DatabaseExplorerInternalUIsTest extends TestBase {

    private JDBCDriver driver1 = null;
    private JDBCDriver driver2 = null;

    public DatabaseExplorerInternalUIsTest(String testName) {
        super(testName);
    }

    private void setUpDrivers() throws Exception {
        removeDrivers();

        driver1 = JDBCDriver.create("foo_driver", "FooDriver", "org.foo.FooDriver", new URL[0]);
        JDBCDriverManager.getDefault().addDriver(driver1);
        driver2 = JDBCDriver.create("bar_driver", "BarDriver", "org.bar.BarDriver", new URL[0]);
        JDBCDriverManager.getDefault().addDriver(driver2);
        assertEquals(2, JDBCDriverManager.getDefault().getDrivers().length);
    }

    private void removeDrivers() throws Exception {
        FileObject driversFO = Util.getDriversFolder();
        FileObject[] children = driversFO.getChildren();
        for (int i = 0; i < children.length; i++) {
            children[i].delete();
        }
        assertEquals(0, JDBCDriverManager.getDefault().getDrivers().length);
    }

    public void testEmptyComboboxContent() throws Exception {
        removeDrivers();
        JComboBox combo = new JComboBox();
        DatabaseExplorerInternalUIs.connect(combo, JDBCDriverManager.getDefault());

        assertTrue("Wrong number of items in the empty combobox", combo.getItemCount() == 1);
    }

    public void testComboboxWithDrivers() throws Exception {
        setUpDrivers();
        JComboBox combo = new JComboBox();
        DatabaseExplorerInternalUIs.connect(combo, JDBCDriverManager.getDefault());

        assertTrue("Wrong number of items in the combobox", combo.getItemCount() == 4);
        assertSame(driver2, combo.getItemAt(0));
        assertSame(driver1, combo.getItemAt(1));
    }

    public void testComboBoxWithDriverClass() throws Exception {
        setUpDrivers();
        JComboBox combo = new JComboBox();
        DatabaseExplorerInternalUIs.connect(combo, JDBCDriverManager.getDefault(), "org.bar.BarDriver");

        assertTrue("Wrong number of items in the combobox", combo.getItemCount() == 1);
        assertSame(driver2, combo.getItemAt(0));
    }
}
