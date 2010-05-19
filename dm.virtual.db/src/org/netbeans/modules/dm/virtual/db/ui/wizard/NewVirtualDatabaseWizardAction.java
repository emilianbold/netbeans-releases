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
import java.awt.Dialog;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JComponent;

import org.netbeans.modules.dm.virtual.db.ui.AxionDBConfiguration;
import org.netbeans.modules.dm.virtual.db.model.DBExplorerUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

public final class NewVirtualDatabaseWizardAction extends CallableSystemAction {

    private WizardDescriptor.Panel[] panels;
    public static final String DEFAULT_VIRTUAL_DB_URL_PREFIX = "jdbc:axiondb:"; // NOI18N

    public void performAction() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(NewVirtualDatabaseWizardAction.class, "LBL_wizardtitle"));
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(
                NewVirtualDatabaseWizardAction.class, "ACSD_Databasedialog"));
        dialog.setSize(630, 334);
        dialog.setVisible(true);
        dialog.toFront();
        
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            String dbName = (String) wizardDescriptor.getProperty("dbName"); // NOI18N
            boolean status = handle(dbName);
            if (status) {
                NotifyDescriptor d = new NotifyDescriptor.Message(
                        NbBundle.getMessage(NewVirtualDatabaseWizardAction.class, "MSG_database_created",dbName), 
                        NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        }
    }

    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                        new NewVirtualDatabaseWizardPanel()
                    };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) {
                    // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
                }
            }
        }
        return panels;
    }

    public String getName() {
        return NbBundle.getMessage(NewVirtualDatabaseWizardAction.class, "LBL_create_database");
    }

    @Override
    public String iconResource() {
        return null;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
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
        String loc = prop.getProperty(AxionDBConfiguration.PROP_DB_LOC);
        File db = new File(loc);
        if (!db.exists()) {
            db.mkdir();
        }
        return loc;
    }

    private boolean handle(String name) {
        String location = null;
        String dbName = null;
        if (CommonUtils.IS_PROJECT_CALL && CommonUtils.isETL) {
            location = CommonUtils.PRJ_PATH + File.separator + "nbproject" +
            File.separator + "private" + File.separator + "databases"; // NOI18N
            dbName = CommonUtils.PRJ_NAME + "_" + name; // NOI18N
        } else {
            location = getDefaultWorkingFolder();
            dbName = name;
        }

        boolean status = false;
        String url = DEFAULT_VIRTUAL_DB_URL_PREFIX + dbName + ":" + location + File.separator + name;

        File f = new File(location + name);
        char[] ch = name.toCharArray();
        if (ch == null) {            
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    NbBundle.getMessage(NewVirtualDatabaseWizardAction.class, "MSG_database_name"), 
                    NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        } else if (f.exists()) {
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    NbBundle.getMessage(NewVirtualDatabaseWizardAction.class, "MSG_database_exits",name), 
                    NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        } else {
            Connection conn = null;
            try {
                conn = DBExplorerUtil.createConnection("org.axiondb.jdbc.AxionDriver", url, "sa", "sa"); // NOI18N
                if (conn != null) {
                    status = true;
                }
            } catch (Exception ex) {
                NotifyDescriptor d = new NotifyDescriptor.Message(
                        NbBundle.getMessage(NewVirtualDatabaseWizardAction.class, "ERR_loading_driver"), 
                        NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            } finally {
                try {
                    if (conn != null) {
                        conn.createStatement().execute("shutdown"); // NOI18N
                        conn.close();
                    }
                } catch (SQLException ex) {
                    conn = null;
                }
            }
        }
        CommonUtils.IS_PROJECT_CALL = false;
        return status;
    }
}
