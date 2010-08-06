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
package org.netbeans.modules.db.explorer.oracle;

import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.NoSuchElementException;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.dlg.ConnectionDialogMediator;
import org.netbeans.modules.db.explorer.dlg.SchemaPanel;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class PredefinedWizard extends ConnectionDialogMediator implements WizardDescriptor.Iterator<PredefinedWizard> {
    
    private String driverLocation;
    private String driverName;
    private String[] steps;
    private WizardDescriptor.Panel<PredefinedWizard>[] panels;
    private Type type;
    private int index;
    private LookingForDriverPanel driverPanel;
    private boolean found = false;
    private String pwd;
    private String driverDN;
    private String driverClass;
    private String databaseUrl;
    private String user;
    

    private PredefinedWizard(Type type) {
        this.type = type;
        switch (type) {
            case ORACLE:
                driverName = NbBundle.getMessage(PredefinedWizard.class, "OracleThinDriverName");
                driverDN = NbBundle.getMessage(PredefinedWizard.class, "OracleThinDriverDisplayName");
                driverClass = NbBundle.getMessage(PredefinedWizard.class, "OracleThinDriverClass");
                databaseUrl = NbBundle.getMessage(PredefinedWizard.class, "OracleSampleDatabaseUrl");
                user = NbBundle.getMessage(PredefinedWizard.class, "OracleSampleUser");
                pwd = NbBundle.getMessage(PredefinedWizard.class, "OracleSamplePassword");
                break;
            case MYSQL:
                driverName = NbBundle.getMessage(PredefinedWizard.class, "MySQLDriverName");
                driverDN = NbBundle.getMessage(PredefinedWizard.class, "MySQLDriverDisplayName");
                driverClass = NbBundle.getMessage(PredefinedWizard.class, "MySQLDriverClass");
                databaseUrl = NbBundle.getMessage(PredefinedWizard.class, "MySQLSampleDatabaseUrl");
                user = NbBundle.getMessage(PredefinedWizard.class, "MySQLSampleUser");
                pwd = NbBundle.getMessage(PredefinedWizard.class, "MySQLSamplePassword");
                break;
            default:
                assert false;
        }
    }

    @Override
    protected boolean retrieveSchemas(SchemaPanel schemaPanel, DatabaseConnection dbcon, String defaultSchema) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public enum Type {
        ORACLE,
        MYSQL
    }
    
    public static void showWizard(PredefinedConnectionProvider.PredefinedConnection conn) {
        if (conn instanceof PredefinedConnectionProvider.OracleConnection) {
            PredefinedWizard wiz = new PredefinedWizard(Type.ORACLE);
            wiz.openWizard();
        } else if (conn instanceof PredefinedConnectionProvider.MySQLConnection) {
            PredefinedWizard wiz = new PredefinedWizard(Type.MYSQL);
            wiz.openWizard();
        } else {
            assert false : "No PredefinedConnection found in lookup.";
        }
    }
    
    private void openWizard() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(this, this);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(PredefinedWizard.class, "PredefinedWizard.WizardTitle")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            // do something
        }
    }
    
    public static interface Panel extends WizardDescriptor.Panel<PredefinedWizard>{}
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel<PredefinedWizard>[] getPanels() {
        if (panels == null) {
            driverPanel = new LookingForDriverPanel(type);
            panels = new Panel[] {
                driverPanel,
                new ConnectionPanel(),
                new ChoosingSchemaPanel(),
            };
            steps = new String[panels.length];
            steps = new String[] {
                NbBundle.getMessage(PredefinedWizard.class, "LookingForDriverUI.Name"), // NOI18N
                NbBundle.getMessage(PredefinedWizard.class, "ConnectionPanel.Name"), // NOI18N
                NbBundle.getMessage(PredefinedWizard.class, "ChoosingSchemaPanel.Name"), // NOI18N
            };
        }
        return panels;
    }
    
    @Override
    public WizardDescriptor.Panel<PredefinedWizard> current() {
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
    
    Type getType() {
        return type;
    }
    
    String[] getSteps() {
        return steps;
    }
    
}
