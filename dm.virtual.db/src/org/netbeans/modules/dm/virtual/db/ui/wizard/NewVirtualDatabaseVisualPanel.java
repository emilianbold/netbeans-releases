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

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JPanel;
import org.axiondb.util.StringIdentifierGenerator;
import org.netbeans.modules.dm.virtual.db.ui.AxionDBConfiguration;
import org.openide.util.NbBundle;

public final class NewVirtualDatabaseVisualPanel extends JPanel {

    class NameFieldKeyAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {
            checkDBName();
            NewVirtualDatabaseVisualPanel.this.owner.fireChangeEvent();
        }
    }
    private boolean canProceed = false;
    private NewVirtualDatabaseWizardPanel owner;

    public NewVirtualDatabaseVisualPanel() {
        initComponents();
        errorMsg.setForeground(Color.RED);
        jLabel1.setForeground(Color.BLACK);
        dbLoc.setForeground(Color.BLACK);
        driverClass.setForeground(Color.BLACK);
        dbName.setText("");
        errorMsg.setText("");
        dbName.addKeyListener(new NameFieldKeyAdapter());
    }

    public NewVirtualDatabaseVisualPanel(NewVirtualDatabaseWizardPanel panel) {
        this();
        this.owner = panel;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "LBL_wizardtitle");
    }

    public void clearText() {
        String name = NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "DEFAULT_databaseName");
        String id = StringIdentifierGenerator.INSTANCE.nextIdentifier().substring(0, 4);
        dbName.setText(name.toUpperCase() + id);
    }

    public boolean canProceed() {
        checkDBName();
        return this.canProceed;
    }

    private String getDefaultWorkingFolder() {
        File conf = AxionDBConfiguration.getConfigFile();
        Properties prop = new Properties();
        try {
            FileInputStream in = new FileInputStream(conf);
            prop.load(in);
        } catch (FileNotFoundException ex) {
            //ignore
        } catch (IOException ex) {
            //ignore
        }
        String defaultDir = prop.getProperty(AxionDBConfiguration.PROP_DB_LOC);
        if (!defaultDir.endsWith(File.separator)) {
            defaultDir = defaultDir + File.separator;
        }
        return defaultDir;
    }

    public String getDBName() {
        return dbName.getText().trim();
    }

    public void setDBName(String name) {
        dbName.setText(name.trim());
    }

    public void setErrorMsg(String msg) {
        errorMsg.setText(msg);
    }

    public String getErrorMsg() {
        return errorMsg.getText();
    }

    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jLabel1.setText(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "LBL_databaseName"));
        dbName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        dbLoc = new javax.swing.JTextField();
        dbLoc.setEditable(false);
        driver = new javax.swing.JLabel();
        driverClass = new javax.swing.JTextField();
        driverClass.setText("org.axiondb.jdbc.AxionDriver"); // NOI18N
        driverClass.setEditable(false);
        errorMsg = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(10000, 4000));
        setPreferredSize(new java.awt.Dimension(10, 4));
        jLabel1.setDisplayedMnemonic(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "ACSD_databaseName").charAt(0));
        jLabel1.setLabelFor(dbName);
        jLabel1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "ACSD_databaseName"));

        dbName.setToolTipText(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "TOOLTIP_databaseName"));
        dbName.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "ACSD_databaseName"));
        dbName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "ACSD_databaseName"));
        dbName.addKeyListener(new NameFieldKeyAdapter());

        dbLoc.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "LBL_dbLocation"));
        dbLoc.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "LBL_dbLocation"));

        driverClass.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "LBL_driver"));
        driverClass.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "LBL_driver"));

        jLabel2.setLabelFor(dbLoc);
        jLabel2.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "LBL_dbLocation"));
        jLabel2.setText(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "LBL_dbLocation"));
        jLabel2.setDisplayedMnemonic(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "LBL_dbLocation").charAt(0));

        driver.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "LBL_driver"));
        driver.setLabelFor(driverClass);
        driver.setText(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "LBL_driver"));
        driver.setDisplayedMnemonic(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "LBL_driver").charAt(7));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).add(jLabel1).add(layout.createSequentialGroup().add(driver).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(driverClass).addContainerGap()).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(dbName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 161, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(190, 190, 190)).add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(org.jdesktop.layout.GroupLayout.LEADING, dbLoc, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE).add(errorMsg, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)).addContainerGap())))));
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(40, 40, 40).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(dbName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(errorMsg, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(14, 14, 14).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(dbLoc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(32, 32, 32).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(driver).add(driverClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    }

    private void checkDBName() {
        String name = dbName.getText().trim();
        String location = null;
        if (CommonUtils.IS_PROJECT_CALL && CommonUtils.isETL) {
            location = CommonUtils.PRJ_PATH + File.separator + "nbproject" +
                    File.separator + "private" + File.separator + "databases"; // NOI18N
            dbLoc.setText("${project.home}" + File.separator + "nbproject" +
                    File.separator + "private" + File.separator + "databases"); // NOI18N
        } else {
            location = getDefaultWorkingFolder();
            dbLoc.setText(location);
        }

        File f = new File(location + File.separator + name);
        char[] ch = name.toCharArray();
        if (ch.length != 0) {
            if (f.exists()) {
                errorMsg.setText(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "MSG_database_exits", name));
                canProceed = false;
            } else if (Character.isDigit(ch[0])) {
                errorMsg.setText(NbBundle.getMessage(NewVirtualDatabaseVisualPanel.class, "TOOLTIP_databaseName"));
                canProceed = false;
            } else {
                errorMsg.setText("");
                canProceed = true;
            }
        } else {
            errorMsg.setText("");
            canProceed = false;
        }
    }
    private javax.swing.JTextField dbLoc;
    private javax.swing.JTextField dbName;
    private javax.swing.JLabel driver;
    private javax.swing.JTextField driverClass;
    private javax.swing.JLabel errorMsg;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
}
