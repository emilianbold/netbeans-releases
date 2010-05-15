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
package org.netbeans.modules.dm.virtual.db.ui.wizard;

import java.awt.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.dm.virtual.db.model.VirtualDBMetaDataFactory;
import org.netbeans.modules.dm.virtual.db.api.Property;
import org.netbeans.modules.dm.virtual.db.model.VirtualDatabaseModel;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBConnectionDefinition;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  ks161616
 */
public class VirtualDBViewerTreePanel implements WizardDescriptor.Panel {

    private PreviewDatabaseVisualPanel component;

    public Component getComponent() {
        if (component == null) {
            component = new PreviewDatabaseVisualPanel();
        }
        return component;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public boolean isValid() {
        return true;
    }
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);

    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    public void readSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;

            VirtualDatabaseModel folder = (VirtualDatabaseModel) wd.getProperty(VirtualDBTableWizardIterator.PROP_VIRTUALDBMODEL);
            folder = addDBTables(folder);
            if (folder == null || folder.getTables().size() == 0) {
                throw new IllegalStateException(NbBundle.getMessage(VirtualDBViewerTreePanel.class, "MSG_context"));
            }

            component.setModel(folder);
            fireChangeEvent();
        }
    }

    public void storeSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;
            wd.putProperty(VirtualDBTableWizardIterator.PROP_VIRTUALDBMODEL, null);
            fireChangeEvent();
        }
    }

    private VirtualDatabaseModel addDBTables(VirtualDatabaseModel model) {
        VirtualDBMetaDataFactory dbMeta = new VirtualDBMetaDataFactory();
        Connection conn = null;
        String catalog = null;
        String[][] tableList = null;
        try {
            conn = model.getJDBCConnection();
            catalog = (conn.getCatalog() == null) ? "" : conn.getCatalog();
            dbMeta.connectDB(conn);
            tableList = dbMeta.getTablesAndViews(catalog, "", "", false);
            model = createTable(tableList, model, dbMeta);
            conn.createStatement().execute("shutdown"); // NOI18N
        } catch (Exception ex) {
            //ignore
        } finally {
            dbMeta.disconnectDB();
        }
        return model;
    }

    private VirtualDatabaseModel createTable(String[][] tableList, VirtualDatabaseModel model, VirtualDBMetaDataFactory meta) throws Exception {
        String[] currTable = null;
        VirtualDBTable dbTable = null;
        VirtualDBConnectionDefinition def = model.getVirtualDBConnectionDefinition();
        if (tableList != null) {
            for (int i = 0; i < tableList.length; i++) {
                currTable = tableList[i];
                dbTable = new VirtualDBTable(currTable[VirtualDBMetaDataFactory.NAME],
                        currTable[VirtualDBMetaDataFactory.SCHEMA], currTable[VirtualDBMetaDataFactory.CATALOG]);
                meta.populateColumns(dbTable);
                HashMap map = getTableMetaData(def, dbTable);
                dbTable.setProperties(map);
                model.addTable(dbTable);
            }
        }
        return model;
    }

    private HashMap getTableMetaData(VirtualDBConnectionDefinition condef, VirtualDBTable element) {
        HashMap<String, Property> map = new HashMap<String, Property>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(condef.getConnectionURL());
            stmt = conn.createStatement();
            String query = "select PROPERTY_NAME, PROPERTY_VALUE from AXION_TABLE_PROPERTIES " + "where TABLE_NAME = '" + element.getName() + "' ORDER BY PROPERTY_NAME"; // NOI18N
            stmt.execute(query);
            rs = stmt.getResultSet();
            while (rs.next()) {
                rs.getMetaData().getColumnCount();
                String value1 = rs.getString(1);
                String value2 = rs.getString(2);

                Property prop = new Property();
                if (value2.equals("true") || value2.equals("false")) { // NOI18N
                    prop = new Property(value1, Boolean.class, true);
                    prop.setValue(Boolean.valueOf(value2));
                    map.put(value1, prop);
                } else {
                    try {
                        Integer.parseInt(value2);
                        prop = new Property(value1, Integer.class, true);
                        prop.setValue(Integer.valueOf(value2));
                        map.put(value1, prop);

                    } catch (NumberFormatException e) {
                        prop = new Property(value1, String.class, true);
                        prop.setValue(value2);
                        map.put(value1, prop);
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.getDefault().log(e.getMessage());
        } finally {
            try {
                rs.close();
            } catch (Exception ex) {
                //ignore
            }

            try {
                stmt.close();
            } catch (Exception ex) {
                //ignore
            }

            try {
                conn.close();
            } catch (Exception ex) {
                //ignore
            }
        }
        return map;
    }
}

