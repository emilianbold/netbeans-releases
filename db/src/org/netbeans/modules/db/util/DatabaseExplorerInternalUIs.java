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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.db.explorer.driver.JDBCDriverSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public final class DatabaseExplorerInternalUIs {

    private DatabaseExplorerInternalUIs() {
    }

    public static void connect(JComboBox comboBox, JDBCDriverManager driverManager) {
        connect(comboBox, driverManager, null);
    }

    public static void connect(JComboBox comboBox, JDBCDriverManager driverManager, String driverClass) {
        DataComboBoxSupport.connect(comboBox, new DriverDataComboBoxModel(driverManager, driverClass), driverClass == null);
    }

    private static final class DriverDataComboBoxModel implements DataComboBoxModel {

        private final JDBCDriverManager driverManager;
        private final DriverComboBoxModel comboBoxModel;

        public DriverDataComboBoxModel(JDBCDriverManager driverManager, String driverClass) {
            this.driverManager = driverManager;
            this.comboBoxModel = new DriverComboBoxModel(driverManager, driverClass);
        }

        public String getItemTooltipText(Object item) {
            return ((JDBCDriver)item).toString();
        }

        public String getItemDisplayName(Object item) {
            return ((JDBCDriver)item).getDisplayName();
        }

        public void newItemActionPerformed() {
            Set oldDrivers = new HashSet(Arrays.asList(driverManager.getDrivers()));
            driverManager.showAddDriverDialog();

            // try to find the new driver
            JDBCDriver[] newDrivers = driverManager.getDrivers();
            if (newDrivers.length == oldDrivers.size()) {
                // no new driver, so...
                return;
            }
            for (int i = 0; i < newDrivers.length; i++) {
                if (!oldDrivers.contains(newDrivers[i])) {
                    comboBoxModel.addSelectedDriver(newDrivers[i]);
                    break;
                }
            }
        }

        public String getNewItemDisplayName() {
            return NbBundle.getMessage(DatabaseExplorerInternalUIs.class, "LBL_NewDriver");
        }

        public ComboBoxModel getListModel() {
            return comboBoxModel;
        }
    }

    private static final class DriverComboBoxModel extends AbstractListModel implements ComboBoxModel {

        private final JDBCDriverManager driverManager;
        private final List driverList; // must be ArrayList

        private Object selectedItem; // can be anything, not just a database driver

        public DriverComboBoxModel(JDBCDriverManager driverManager, String driverClass) {
            this.driverManager = driverManager;

            driverList = new ArrayList();
            JDBCDriver[] drivers;
            if (driverClass != null) {
                drivers = driverManager.getDrivers(driverClass);
            } else {
                drivers = driverManager.getDrivers();
            }
            for (int i = 0; i < drivers.length; i++) {
                if (JDBCDriverSupport.isAvailable(drivers[i])) {
                    driverList.add(drivers[i]);
                }
            }
            Collections.sort(driverList, new DriverComparator());
        }

        public void setSelectedItem(Object anItem) {
            selectedItem = anItem;
        }

        public Object getElementAt(int index) {
            return driverList.get(index);
        }

        public int getSize() {
            return driverList.size();
        }

        public Object getSelectedItem() {
            return selectedItem;
        }

        public void addSelectedDriver(JDBCDriver dbconn) {
            selectedItem = dbconn;
            driverList.add(dbconn);
            Collections.sort(driverList, new DriverComparator());
            fireContentsChanged(this, 0, driverList.size());
        }
    }

    private static final class DriverComparator implements Comparator {

        public boolean equals(Object that) {
            return that instanceof DriverComparator;
        }

        public int compare(Object driver1, Object driver2) {
            if (driver1 == null) {
                return driver2 == null ? 0 : -1;
            } else {
                if (driver2 == null) {
                    return 1;
                }
            }

            String dispName1 = ((JDBCDriver)driver1).getDisplayName();
            String dispName2 = ((JDBCDriver)driver2).getDisplayName();
            if (dispName1 == null) {
                return dispName2 == null ? 0 : -1;
            } else {
                return dispName2 == null ? 1 : dispName1.compareToIgnoreCase(dispName2);
            }
        }
    }
}
