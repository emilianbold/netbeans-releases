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

package org.netbeans.modules.db.explorer.dlg;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import org.openide.util.NbBundle;

import org.netbeans.modules.db.explorer.DatabaseConnection;

public class SchemaPanel extends javax.swing.JPanel {

    private ConnectionDialogMediator mediator;
    private DatabaseConnection dbcon;
    private ProgressHandle progressHandle;
    private JComponent progressComponent;

    /**
     * Creates a new form SchemaPanel
     * 
     * @param dbcon instance of DatabaseConnection object
     */
    public SchemaPanel(ConnectionDialogMediator mediator, DatabaseConnection dbcon) {
        this.mediator = mediator;
        this.dbcon = dbcon;
        initComponents();
        initAccessibility();

        ConnectionProgressListener progressListener = new ConnectionProgressListener() {
            public void connectionStarted() {
                startProgress();
            }
            
            public void connectionStep(String step) {
                setProgressMessage(step);
            }

            public void connectionFinished() {
                stopProgress(true);
            }

            public void connectionFailed() {
                stopProgress(false);
            }
        };
        mediator.addConnectionProgressListener(progressListener);
    }

    private void initAccessibility() {
        schemaLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_SchemaDialogTextA11yDesc")); //NOI18N
        schemaComboBox.getAccessibleContext().setAccessibleName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_SchemaDialogTextComboBoxA11yName")); //NOI18N
        commentTextArea.getAccessibleContext().setAccessibleName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_SchemaPanelCommentA11yName")); //NOI18N
        commentTextArea.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_SchemaPanelCommentA11yDesc")); //NOI18N
        connectProgressPanel.getAccessibleContext().setAccessibleName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectionProgressBarA11yName")); //NOI18N
        connectProgressPanel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectionProgressBarA11yDesc")); //NOI18N
        this.getAccessibleContext().setAccessibleName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_GetSchemasPanelA11yName")); //NOI18N
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_GetSchemasPanelA11yDesc")); //NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        commentTextArea = new javax.swing.JTextArea();
        schemaLabel = new javax.swing.JLabel();
        schemaComboBox = new javax.swing.JComboBox();
        connectProgressPanel = new javax.swing.JPanel();
        progressMessageLabel = new javax.swing.JLabel();
        progressContainerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        commentTextArea.setEditable(false);
        commentTextArea.setFont(javax.swing.UIManager.getFont("Label.font"));
        commentTextArea.setLineWrap(true);
        commentTextArea.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("MSG_SchemaPanelComment")); // NOI18N
        commentTextArea.setWrapStyleWord(true);
        commentTextArea.setDisabledTextColor(javax.swing.UIManager.getColor("Label.foreground"));
        commentTextArea.setEnabled(false);
        commentTextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(commentTextArea, gridBagConstraints);

        schemaLabel.setLabelFor(schemaComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(schemaLabel, NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("SchemaDialogText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(schemaLabel, gridBagConstraints);

        schemaComboBox.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_SchemaDialogTextComboBoxA11yDesc")); // NOI18N
        schemaComboBox.setEnabled(false);
        schemaComboBox.setPrototypeDisplayValue("wwwwwwwwww");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(schemaComboBox, gridBagConstraints);

        connectProgressPanel.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectionProgressBarA11yDesc")); // NOI18N
        connectProgressPanel.setLayout(new java.awt.BorderLayout(0, 5));

        org.openide.awt.Mnemonics.setLocalizedText(progressMessageLabel, " ");
        connectProgressPanel.add(progressMessageLabel, java.awt.BorderLayout.NORTH);

        progressContainerPanel.setMinimumSize(new java.awt.Dimension(20, 20));
        progressContainerPanel.setPreferredSize(new java.awt.Dimension(20, 20));
        progressContainerPanel.setLayout(new java.awt.BorderLayout());
        connectProgressPanel.add(progressContainerPanel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 11);
        add(connectProgressPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea commentTextArea;
    private javax.swing.JPanel connectProgressPanel;
    private javax.swing.JPanel progressContainerPanel;
    private javax.swing.JLabel progressMessageLabel;
    private javax.swing.JComboBox schemaComboBox;
    private javax.swing.JLabel schemaLabel;
    // End of variables declaration//GEN-END:variables

    public String getSchema() {
        Object schema = schemaComboBox.getSelectedItem();
        if (schema != null && !schema.toString().equals(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("TXT_NoSchema"))) //NOI18N
            return schema.toString();
        else
            return null;
    }

    /**
     * Determine if there are any schemas available
     * 
     * @return true if there are schemas, false otherwise
     */
    public boolean schemasAvailable()
    {
        // the schema combo box is enabled if there are schemas
        boolean available = schemaComboBox.isEnabled();
        
        return available;
    }
    
    public boolean setSchemas(Vector items, String schema) {
        schemaComboBox.removeAllItems();
        for (int i = 0; i < items.size(); i++)
            schemaComboBox.addItem(items.elementAt(i));

        if (items.size() == 0) {
            schemaComboBox.addItem(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("TXT_NoSchema")); //NOI18N
            schemaComboBox.setEnabled(false);
        } else
            schemaComboBox.setEnabled(true);

        if (items.size() == 1)
            //no or only one schema in the items
            return true;

        int idx = items.indexOf(schema);
        if (idx == -1)
            idx = items.indexOf(schema.toLowerCase());
        if (idx == -1)
            idx = items.indexOf(schema.toUpperCase());
        if (idx != -1) {
            schemaComboBox.setSelectedIndex(idx);
            // schema has been found in the items
            return true;
        }

        // schema has not been found in the items; index is set to the first item
        return false;
    }

    public void setComment(String msg) {
        commentTextArea.setText(msg);
    }
    
    private void startProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressHandle = ProgressHandleFactory.createHandle(null);
                progressComponent = ProgressHandleFactory.createProgressComponent(progressHandle);
                progressContainerPanel.add(progressComponent, BorderLayout.CENTER);
                progressHandle.start();
                progressMessageLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectionProgress_Connecting"));
            }
        });
    }
    
    private void setProgressMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressMessageLabel.setText(message);
            }
        });
    }

    /**
     * Terminates the use of the progress bar.
     */
    public void terminateProgress()
    {
        stopProgress(false);
    }
    
    private void stopProgress(final boolean connected) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (progressHandle != null)
                {
                    progressHandle.finish();
                    progressContainerPanel.remove(progressComponent);
                    // without this, the removed progress component remains painted on its parent... why?
                    progressContainerPanel.repaint();
                    if (connected) {
                        progressMessageLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectionProgress_Established"));
                    } else {
                        progressMessageLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectionProgress_Failed"));
                    }
                }
            }
        });
    }
    
    public void resetProgress() {
        progressMessageLabel.setText(" "); // NOI18N
    }
}
