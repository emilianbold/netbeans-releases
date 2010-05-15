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
import java.sql.SQLException;
import java.util.HashMap;

import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBDefinition;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBConnectionDefinition;
import org.netbeans.modules.dm.virtual.db.model.VirtualDatabaseModel;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class SelectDatabasePanel extends AbstractWizardPanel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SelectDatabaseVisualPanel component;
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public synchronized Component getComponent() {
        if (component == null) {
            component = new SelectDatabaseVisualPanel(this);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        return someCondition();
    }
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;
            Connection conn = (Connection) wd.getProperty(VirtualDBTableWizardIterator.CONNECTION);
            if (conn != null) {
                try {
                    conn.createStatement().execute(NbBundle.getMessage(SelectDatabasePanel.class, "CMD_shutdown"));
                    conn.close();
                } catch (SQLException ex) {
                    //ignore
                }
            }
            wd.putProperty(VirtualDBTableWizardIterator.CONNECTION, null);
        }
    }

    @Override
    public void storeSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;
            SelectDatabaseVisualPanel panel = (SelectDatabaseVisualPanel) getComponent();
            String database = panel.getSelectedDatabase();
            if (database != null) {
                database = database.trim();
                int end = database.indexOf(":", NbBundle.getMessage(SelectDatabasePanel.class, "LBL_jdbc_axiondb").length());
                if (end == -1) {
                    end = database.trim().length();
                }
                String dbName = database.substring(NbBundle.getMessage(SelectDatabasePanel.class, "LBL_jdbc_axiondb").length(), end);
                wd.putProperty("url", database);

                VirtualDatabaseModel model = (VirtualDatabaseModel) wd.getProperty(VirtualDBTableWizardIterator.PROP_VIRTUALDBMODEL);
                if (model == null) {
                    VirtualDBConnectionDefinition def = new VirtualDBConnectionDefinition(dbName);
                    def.setConnectionURL(database);
                    model = new VirtualDatabaseModel(database, def);
                    model.setConnectionName(dbName);
                    VirtualDBDefinition ffDefn;
                    try {
                        ffDefn = new VirtualDBDefinition(dbName);
                        ffDefn.setInstanceName(dbName);
                        ffDefn.setVirtualDatabaseModel(model);
                    } catch (Exception ex) {
                        ErrorManager.getDefault().log(ErrorManager.ERROR, ex.getMessage());
                        ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);

                    }
                    wd.putProperty(VirtualDBTableWizardIterator.PROP_VIRTUALDBMODEL, model);
                    wd.putProperty(VirtualDBTableWizardIterator.TABLE_MAP,
                            new HashMap<String, VirtualDBTable>());
                }
            } else {
                ErrorManager.getDefault().log(NbBundle.getMessage(SelectDatabasePanel.class, "LOG_BeforeAddingTable"));
            }
        }
    }

    private boolean someCondition() {
        SelectDatabaseVisualPanel panel = (SelectDatabaseVisualPanel) getComponent();
        String dbUrl = panel.getSelectedDatabase();
        if (dbUrl == null) {
            return false;
        } else if (panel.isPopulated()) {
            return true;
        }
        return true;
    }

    public String getStepLabel() {
        return NbBundle.getMessage(SelectDatabasePanel.class, "LBL_SelectDatabase");
    }

    public String getTitle() {
        return NbBundle.getMessage(SelectDatabasePanel.class, "LBL_SelectDatabase");
    }
}
