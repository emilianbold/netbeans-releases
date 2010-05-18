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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.dm.virtual.db.ui.wizard;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import org.axiondb.ExternalConnectionProvider;
import org.netbeans.modules.dm.virtual.db.api.AxionExternalConnectionProvider;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBConnectionFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

public final class NewJDBCTableAction extends CallableSystemAction {
    private WizardDescriptor.Panel[] panels;
    public static final String DEFAULT_FLATFILE_JDBC_URL_PREFIX = "jdbc:axiondb:";

    public void performAction() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle("Add JDBC Table(s)");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        //dialog.setSize(new Dimension(650, 300));
        dialog.getAccessibleContext().setAccessibleDescription("This dialog lets user to create flatfile tables from jdbc sources");
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            boolean status = false;
            System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME,
                        AxionExternalConnectionProvider.class.getName());
            String jdbcUrl = (String) wizardDescriptor.getProperty("url");
            List<String> dblinks = (List<String>) wizardDescriptor.getProperty("dblinks");
            List<String> statements = (List<String>) wizardDescriptor.getProperty("statements");
            Connection conn = null;
            Statement stmt = null;
            try {
                conn = VirtualDBConnectionFactory.getInstance().getConnection(jdbcUrl);
                if (conn != null) {
                    conn.setAutoCommit(true);
                    stmt = conn.createStatement();
                }

                Iterator it = dblinks.iterator();
                while (it.hasNext()) {
                    String sql = (String) it.next();
                    stmt.execute(sql);
                }

                it = statements.iterator();
                while (it.hasNext()) {
                    String sql = (String) it.next();
                    stmt.execute(sql);
                }
                status = true;
            } catch (Exception ex) {

            } finally {
                if (conn != null) {
                    try {
                        if (stmt != null) {
                            stmt.execute("shutdown");
                        }
                        conn.close();
                    } catch (SQLException ex) {
                        conn = null;
                    }
                }
                System.setProperty(ExternalConnectionProvider.EXTERNAL_CONNECTION_PROVIDER_PROPERTY_NAME,"");
            }
            if (status) {
                String nbBundle2 = "Tables successfully created.";
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(nbBundle2, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            } else {
                String nbBundle3 = "Tables creation failed.";
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(nbBundle3, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                new SelectDatabasePanel(),
                new JDBCTablePanel()
            };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
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
        return "Add JDBC Table(s)";
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
}
