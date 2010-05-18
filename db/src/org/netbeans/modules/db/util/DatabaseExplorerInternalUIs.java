/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import org.netbeans.modules.db.util.JdbcUrl;
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
            JdbcUrl url = (JdbcUrl)item;
            if (url.getDriver() == null) {
                return "";
            } else {
                return url.getDriver().toString();
            }
        }

        public String getItemDisplayName(Object item) {
            return ((JdbcUrl)item).getDisplayName();
        }

        public void newItemActionPerformed() {
            Set<JDBCDriver> oldDrivers = new HashSet<JDBCDriver>(Arrays.asList(driverManager.getDrivers()));
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
        private final ArrayList<JdbcUrl> driverList; 

        private Object selectedItem; // can be anything, not just a database driver

        public DriverComboBoxModel(JDBCDriverManager driverManager, String driverClass) {
            this.driverManager = driverManager;

            driverList = new ArrayList<JdbcUrl>();
            JDBCDriver[] drivers;
            if (driverClass != null) {
                drivers = driverManager.getDrivers(driverClass);
            } else {
                drivers = driverManager.getDrivers();
            }
            for (int i = 0; i < drivers.length; i++) {
                JDBCDriver driver = drivers[i];
                if (JDBCDriverSupport.isAvailable(driver)) {
                    driverList.addAll(DriverListUtil.getJdbcUrls(driver));
                }
            }
            
            Collections.sort(driverList, new DriverTypeComparator());
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

        public void addSelectedDriver(JDBCDriver driver) {
            List<JdbcUrl> types = DriverListUtil.getJdbcUrls(driver);
            
            assert(! types.isEmpty());
            driverList.addAll(types);
            setSelectedItem(types.get(0));
            
            Collections.sort(driverList, new DriverTypeComparator());
            fireContentsChanged(this, 0, driverList.size());
        }
        
    }

    private static final class DriverTypeComparator implements Comparator {
        public int compare(Object type1, Object type2) {
            if (type1 == null) {
                return type2 == null ? 0 : -1;
            } else {
                if (type2 == null) {
                    return 1;
                }
            }
            
            String dispName1 = ((JdbcUrl)type1).getName();
            String dispName2 = ((JdbcUrl)type2).getName();
            
            if (dispName1 == null) {
                return dispName2 == null ? 0 : -1;
            } else {
                return dispName2 == null ? 1 : dispName1.compareToIgnoreCase(dispName2);
            }
        }
    }
}
