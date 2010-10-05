/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.explorer.dlg;

import java.awt.Dialog;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.action.ConnectUsingDriverAction;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class AddConnectionWizard extends ConnectionDialogMediator implements WizardDescriptor.Iterator<AddConnectionWizard> {
    
    private String driverLocation;
    private final String driverName;
    private final String downloadFrom;
    private final String driverFileName;
    private String[] steps;
    private WizardDescriptor.Panel<AddConnectionWizard>[] panels;
    private int index;
    private ChoosingDriverPanel driverPanel;
    private boolean found = false;
    private String pwd;
    private String driverDN;
    private final String driverClass;
    private String databaseUrl;
    private String user;
    private String defaultSchema;
    private DatabaseConnection connection;
    private List<String> schemas = null;
    private String currentSchema;

    private AddConnectionWizard(String driverName, String driverClass, String databaseUrl, String user, String password) {
        if (true /* oracle */) {
            this.driverName = NbBundle.getMessage(AddConnectionWizard.class, "OracleThinDriverName");
            this.driverDN = NbBundle.getMessage(AddConnectionWizard.class, "OracleThinDriverDisplayName");
            this.driverClass = NbBundle.getMessage(AddConnectionWizard.class, "OracleThinDriverClass");
            this.databaseUrl = NbBundle.getMessage(AddConnectionWizard.class, "OracleSampleDatabaseUrl");
            this.user = NbBundle.getMessage(AddConnectionWizard.class, "OracleSampleUser");
            this.pwd = NbBundle.getMessage(AddConnectionWizard.class, "OracleSamplePassword");
            this.defaultSchema = NbBundle.getMessage(AddConnectionWizard.class, "OracleSampleSchema");
            this.downloadFrom = NbBundle.getMessage(AddConnectionWizard.class, "oracle.from");
            this.driverFileName = NbBundle.getMessage(AddConnectionWizard.class, "oracle.driver.name");
        } else if (true /* mysql */) {
            this.driverName = NbBundle.getMessage(AddConnectionWizard.class, "MySQLDriverName");
            this.driverDN = NbBundle.getMessage(AddConnectionWizard.class, "MySQLDriverDisplayName");
            this.driverClass = NbBundle.getMessage(AddConnectionWizard.class, "MySQLDriverClass");
            this.databaseUrl = NbBundle.getMessage(AddConnectionWizard.class, "MySQLSampleDatabaseUrl");
            this.user = NbBundle.getMessage(AddConnectionWizard.class, "MySQLSampleUser");
            this.pwd = NbBundle.getMessage(AddConnectionWizard.class, "MySQLSamplePassword");
            this.defaultSchema = NbBundle.getMessage(AddConnectionWizard.class, "MySQLSampleSchema");
            this.downloadFrom = NbBundle.getMessage(AddConnectionWizard.class, "mysql.from");
            this.driverFileName = NbBundle.getMessage(AddConnectionWizard.class, "mysql.driver.name");
        } else {
            // others
        }
    }

    @Override
    protected boolean retrieveSchemas(SchemaPanel schemaPanel, DatabaseConnection dbcon, String defaultSchema) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static void showWizard(String driverName, String driverClass, String databaseUrl, String user, String password) {
        AddConnectionWizard wiz = new AddConnectionWizard(driverName, driverClass, databaseUrl, user, password);
        wiz.openWizard();
    }
    
    private void openWizard() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(this, this);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(AddConnectionWizard.class, "PredefinedWizard.WizardTitle")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            assert getDatabaseConnection() != null : "DatabaseConnection found.";
            DatabaseConnection conn = getDatabaseConnection();
            if (getSchemas() != null) {
                conn.setSchema(getCurrentSchema());
            }
            try {
                ConnectionList.getDefault().add(getDatabaseConnection());
            } catch (DatabaseException ex) {
                Logger.getLogger(AddConnectionWizard.class.getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
                DbUtilities.reportError(NbBundle.getMessage (ConnectUsingDriverAction.class, "ERR_UnableToAddConnection"), ex.getMessage()); // NOI18N
                closeConnection();
            }
        }
    }
    
    public static interface Panel extends WizardDescriptor.Panel<AddConnectionWizard>{}
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel<AddConnectionWizard>[] getPanels() {
        if (panels == null) {
            driverPanel = new ChoosingDriverPanel(driverFileName, downloadFrom);
            panels = new Panel[] {
                driverPanel,
                new ConnectionPanel(),
                new ChoosingSchemaPanel(),
            };
            steps = new String[panels.length];
            steps = new String[] {
                NbBundle.getMessage(AddConnectionWizard.class, "ChoosingDriverUI.Name"), // NOI18N
                NbBundle.getMessage(AddConnectionWizard.class, "ConnectionPanel.Name"), // NOI18N
                NbBundle.getMessage(AddConnectionWizard.class, "ChoosingSchemaPanel.Name"), // NOI18N
            };
        }
        return panels;
    }
    
    @Override
    public WizardDescriptor.Panel<AddConnectionWizard> current() {
        // init panels first
        getPanels();
        if (driverPanel.getDriverLocation() != null && ! found) {
            found = true;
            index++;
            setDriverLocation(driverPanel.getDriverLocation());
        }
                
        return getPanels()[index];
    }

    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    void setDriverLocation(String location) {
        this.driverLocation = location;
    }
    
    String getDriverLocation() {
        return driverLocation;
    }
    
    String getDriverName() {
        return driverName;
    }
    
    String getDriverDisplayName() {
        return driverDN;
    }
    
    String getDriverClass() {
        return driverClass;
    }
    
    String getDatabaseUrl() {
        return databaseUrl;
    }
    
    String getUser() {
        return user;
    }
    
    String getPassword() {
        return pwd;
    }
    
    String[] getSteps() {
        return steps;
    }
    
    void setDatabaseConnection(DatabaseConnection conn) {
        this.connection = conn;
    }
    
    DatabaseConnection getDatabaseConnection() {
        return this.connection;
    }

    void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }
    
    List<String> getSchemas() {
        return schemas;
    }
    
    String getDefaultSchema() {
        return defaultSchema;
    }

    void setCurrentSchema(String schema) {
        this.currentSchema = schema;
    }
    
    String getCurrentSchema() {
        if (currentSchema == null) {
            return defaultSchema;
        }
        return currentSchema;
    }

    @Override
    public void closeConnection()
    {
        if (connection != null)
        {
            Connection conn = connection.getConnection();
            if (conn != null)
            {
                try 
                {
                    conn.close();
                    connection.setConnection(null);
                } 
                catch (SQLException e) 
                {
                    //unable to close db connection
                    connection.setConnection(null);
                }
            }
        }
    }
}
