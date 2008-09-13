/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.ruby.railsprojects.ui.wizards;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.DatabaseServerManager;
import org.netbeans.modules.db.mysql.util.Utils;
import org.netbeans.modules.db.mysql.ui.CreateDatabasePanel;
import org.netbeans.modules.db.mysql.ui.PropertiesDialog;
import org.netbeans.modules.ruby.railsprojects.database.RailsAdapterFactory;
import org.netbeans.modules.ruby.railsprojects.database.RailsDatabaseConfiguration;
import org.netbeans.modules.ruby.railsprojects.database.RailsJdbcAsAdapterConnection;
import org.netbeans.modules.ruby.railsprojects.database.RailsJdbcConnection;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.NbBundle;

/**
 * A panel for JDBC connections.
 *
 * @author  Erno Mononen
 */
public class JdbcConnectionsPanel extends SettingsPanel {

    private static final Logger LOGGER = Logger.getLogger(JdbcConnectionsPanel.class.getName());
    
    /**
     * The DB connection manager for this panel.
     */
    private final ConnectionManager connectionManager;
    /**
     * The number of db connections the connection manager has.
     */
    private int connectionCount;

    /** Creates new form JdbcConnectionsPanel */
    public JdbcConnectionsPanel() {
        this.connectionManager = ConnectionManager.getDefault();
        this.connectionCount = connectionManager.getConnections().length;
        initComponents();
        DatabaseExplorerUIs.connect(developmentComboBox, connectionManager);
        DatabaseExplorerUIs.connect(productionComboBox, connectionManager);
        DatabaseExplorerUIs.connect(testComboBox, connectionManager);
    }

    /**
     * Refreshes all three connection combos, needed after creating databases.
     */
    void updateConnectionCombos() {
        int newConnectionCount = connectionManager.getConnections().length;
        if (newConnectionCount == connectionCount) {
            return;
        }
        this.connectionCount = newConnectionCount;
        developmentComboBox = replaceCombo(developmentComboBox);
        testComboBox = replaceCombo(testComboBox);
        productionComboBox = replaceCombo(productionComboBox);
    }

    private JComboBox replaceCombo(JComboBox comboBox) {
        // this is ugly, but needed for keeping the connection combos
        // up to date after creating a database
        Object selectedItem = comboBox.getSelectedItem();
        JComboBox newComboBox = new JComboBox();
        DatabaseExplorerUIs.connect(newComboBox, connectionManager);
        ((GroupLayout) this.getLayout()).replace(comboBox, newComboBox);
        newComboBox.setSelectedItem(selectedItem);
        return newComboBox;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        developmentLabel = new javax.swing.JLabel();
        productionLabel = new javax.swing.JLabel();
        testLabel = new javax.swing.JLabel();
        developmentComboBox = new javax.swing.JComboBox();
        productionComboBox = new javax.swing.JComboBox();
        testComboBox = new javax.swing.JComboBox();
        createDevelopmentDb = new javax.swing.JButton();
        createTestDb = new javax.swing.JButton();
        createProductionDb = new javax.swing.JButton();

        developmentLabel.setLabelFor(developmentComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(developmentLabel, org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "LBL_DevelopmentConnection")); // NOI18N

        productionLabel.setLabelFor(productionComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(productionLabel, org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "LBL_ProductionConnection")); // NOI18N

        testLabel.setLabelFor(testComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(testLabel, org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "LBL_TestConnection")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(createDevelopmentDb, org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "LBL_CreateMySqlDb_Devel")); // NOI18N
        createDevelopmentDb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createDevelopmentDbActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(createTestDb, org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "LBL_CreateMySqlDb_Test")); // NOI18N
        createTestDb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createTestDbActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(createProductionDb, org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "LBL_CreateMySqlDb_Production")); // NOI18N
        createProductionDb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createProductionDbActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(testLabel)
                    .add(productionLabel)
                    .add(developmentLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(testComboBox, 0, 313, Short.MAX_VALUE)
                    .add(productionComboBox, 0, 313, Short.MAX_VALUE)
                    .add(developmentComboBox, 0, 313, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(createTestDb, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(createProductionDb, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(createDevelopmentDb, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(developmentLabel)
                    .add(createDevelopmentDb)
                    .add(developmentComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(testLabel)
                    .add(testComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(createTestDb))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(productionLabel)
                    .add(productionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(createProductionDb))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        developmentLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "ACSD_DevelopmentConnection")); // NOI18N
        productionLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "ACSD_ProductionConnection")); // NOI18N
        testLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "ACSD_TestConnection")); // NOI18N
        createDevelopmentDb.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "ASCN_CreateMySqlDb")); // NOI18N
        createDevelopmentDb.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "ASCD_CreateMySqlDb")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "ASCN_JdbcPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(JdbcConnectionsPanel.class, "ASCD_JdbcPanel")); // NOI18N
        getAccessibleContext().setAccessibleParent(this);
    }// </editor-fold>//GEN-END:initComponents

private void createDevelopmentDbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createDevelopmentDbActionPerformed
    DatabaseConnection created = createMySqlDb();
    if (created != null) {
        developmentComboBox.setSelectedItem(created);
    }
}//GEN-LAST:event_createDevelopmentDbActionPerformed

private void createTestDbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createTestDbActionPerformed
    DatabaseConnection created = createMySqlDb();
    if (created != null) {
        testComboBox.setSelectedItem(created);
    }
}//GEN-LAST:event_createTestDbActionPerformed

private void createProductionDbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createProductionDbActionPerformed
    DatabaseConnection created = createMySqlDb();
    if (created != null) {
        productionComboBox.setSelectedItem(created);
    }
}//GEN-LAST:event_createProductionDbActionPerformed

    private DatabaseConnection createMySqlDb() {
        DatabaseServer mysql = DatabaseServerManager.getDatabaseServer();
        if (!mysql.isConnected()) {
            tryToConnect(mysql);
        }
        DatabaseConnection result = null;
        if (mysql.isConnected()) {
            try {
                CreateDatabasePanel panel = new CreateDatabasePanel(mysql);
                result = panel.showCreateDatabaseDialog();
                updateConnectionCombos();
            } catch (DatabaseException dbe) {
                Utils.displayErrorMessage(dbe.getMessage());
            }
        }
        return result;
    }

    private void tryToConnect(DatabaseServer mysql) {
        doConnect(mysql, true);
        if (!mysql.isConnected()) {
            if (new PropertiesDialog(mysql).displayDialog()) {
                doConnect(mysql, false);
            }
        }
    }
    
    private void doConnect(DatabaseServer mysql, boolean quiet) {
        try {
            mysql.reconnect();
        } catch (DatabaseException ex) {
            if (quiet) {
                LOGGER.log(Level.FINE, "Could not connect to the MySQL server instance", ex);
            } else {
                String message = NbBundle.getMessage(JdbcConnectionsPanel.class, "MSG_UnableToConnect");
                Utils.displayError(message, ex);
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createDevelopmentDb;
    private javax.swing.JButton createProductionDb;
    private javax.swing.JButton createTestDb;
    private javax.swing.JComboBox developmentComboBox;
    private javax.swing.JLabel developmentLabel;
    private javax.swing.JComboBox productionComboBox;
    private javax.swing.JLabel productionLabel;
    private javax.swing.JComboBox testComboBox;
    private javax.swing.JLabel testLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    void store( WizardDescriptor settings) {
        DatabaseConnection devel = (DatabaseConnection) developmentComboBox.getSelectedItem();
        DatabaseConnection production = (DatabaseConnection) productionComboBox.getSelectedItem();
        DatabaseConnection test = (DatabaseConnection) testComboBox.getSelectedItem();

        RailsDatabaseConfiguration databaseConfiguration = null;
        Boolean jdbc = (Boolean) settings.getProperty(NewRailsProjectWizardIterator.JDBC_WN);
        boolean useJdbc = jdbc != null ? jdbc.booleanValue() : false;
        // see #129332 - if nothing is specified, use the default adapter
        boolean useDefault = !jdbc && devel == null && production == null && test == null;

        if (useDefault) {
            databaseConfiguration = RailsAdapterFactory.getDefaultAdapter();
        } else if (useJdbc) {
            databaseConfiguration = new RailsJdbcConnection(devel, test, production);
        } else {
            databaseConfiguration = new RailsJdbcAsAdapterConnection(devel, test, production);
        }
        settings.putProperty(NewRailsProjectWizardIterator.RAILS_DEVELOPMENT_DB, databaseConfiguration);
    }

    @Override
    void read( WizardDescriptor settings) {
    }

    @Override
    boolean valid( WizardDescriptor settings) {
        return true;
    }

    @Override
    void validate( WizardDescriptor settings) throws WizardValidationException {
    }
}
