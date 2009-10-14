/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.mashup.db.ui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComboBox;

import net.java.hulp.i18n.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.ETLEditorSupport;
import org.netbeans.modules.mashup.tables.wizard.MashupTableWizardIterator;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.netbeans.modules.sql.framework.common.utils.DBURL;

/**
 * Visual panel which displays the available mashup databases.
 *
 * @author Karthik S
 * @author Ahimanikya Satapathy
 */
public class SelectDatabaseVisualPanel extends javax.swing.JPanel {
    private static transient final Logger mLogger = Logger.getLogger(SelectDatabaseVisualPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private boolean populated;
    private SelectDatabasePanel owner;
    private String selectedDB;
    Set<String> urls = new HashSet<String>();

    /** Creates new form SelectDatabaseVisualPanel */
    public SelectDatabaseVisualPanel() {
        initComponents();
        databasesCombo.removeAllItems();
        populateDBList();
    }

    public SelectDatabaseVisualPanel(SelectDatabasePanel panel) {
        this();
        this.owner = panel;
        databasesCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JComboBox combo = (JComboBox) e.getSource();
                DBURL selectedUrl = (DBURL) combo.getSelectedItem();
                selectedDB = selectedUrl.getURL();
                dbPathTextField.setText(selectedUrl.getWorkDir());
                if (selectedDB != null) {
                    SelectDatabaseVisualPanel.this.owner.fireChangeEvent();
                }
            }
        });
        if (databasesCombo.getItemCount() != 0) {
            databasesCombo.setSelectedIndex(0);
        }
    }

    String nbBundle1 = mLoc.t("BUND228: Choose Mashup Database");
    @Override
    public String getName() {
        return nbBundle1.substring(15);
    }

    public String getSelectedDatabase() {
        return selectedDB;
    }

    private void populateDBList() {
        List<DatabaseConnection> models = new ArrayList<DatabaseConnection>();
        DBExplorerUtil.recreateMissingFlatfileConnectionInDBExplorer();
        if(MashupTableWizardIterator.IS_PROJECT_CALL) {
            models.addAll(DBExplorerUtil.getDatabasesForCurrentProject());
        }
        
        // Here we either display project level data base or top level
        if (!MashupTableWizardIterator.IS_PROJECT_CALL) {
            DatabaseConnection[] dbconns = ConnectionManager.getDefault().getConnections();
            for (int i = 0; i < dbconns.length; i++) {
                if (dbconns[i].getDriverClass().equals(DBExplorerUtil.AXION_DRIVER)) {
                    models.add(dbconns[i]);
                }
            }
        }
        
        for (DatabaseConnection model : models) {
            //java.util.logging.Logger.getLogger(SelectDatabaseVisualPanel.class.getName()).info("SelectDatabaseVisualPanel "+model.getDatabaseURL());
            if (MashupTableWizardIterator.IS_PROJECT_CALL) {               
                String url = model.getDatabaseURL();
                 //java.util.logging.Logger.getLogger(SelectDatabaseVisualPanel.class.getName()).info("SelectDatabaseVisualPanel url "+url);
                if (url.contains(ETLEditorSupport.PRJ_NAME.trim())) {
                   //  java.util.logging.Logger.getLogger(SelectDatabaseVisualPanel.class.getName()).info("SelectDatabaseVisualPanel inside if ");
                    databasesCombo.addItem(new DBURL(url, true));
                }
            } else {
                databasesCombo.addItem(new DBURL(model.getDatabaseURL(), false));
            }
        }
        
        MashupTableWizardIterator.IS_PROJECT_CALL = false;
        if (databasesCombo.getItemCount() != 0) {
            this.populated = true;
        } else {
            String nbBundle2 = mLoc.t("BUND229: No Mashup Database found.");
            error.setText(nbBundle2.substring(15));
            this.populated = false;
        }
    }
    
    public boolean isPopulated() {
        return this.populated;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        databasesCombo = new javax.swing.JComboBox();
        dbPathTextField = new javax.swing.JTextField();
        error = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(500, 200));
        setMinimumSize(new java.awt.Dimension(50, 50));
        setPreferredSize(new java.awt.Dimension(390, 160));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Available Databases"));

        databasesCombo.setToolTipText("Available Databases");
        databasesCombo.setAutoscrolls(true);
        databasesCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                databasesComboActionPerformed(evt);
            }
        });

        dbPathTextField.setEnabled(false);

        error.setForeground(new java.awt.Color(255, 0, 0));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, databasesCombo, 0, 347, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, dbPathTextField))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(122, 122, 122)
                        .add(error)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(databasesCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dbPathTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(error, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(101, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 109, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void databasesComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_databasesComboActionPerformed
    // TODO add your handling code here:
        
    }//GEN-LAST:event_databasesComboActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox databasesCombo;
    private javax.swing.JTextField dbPathTextField;
    private javax.swing.JLabel error;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
