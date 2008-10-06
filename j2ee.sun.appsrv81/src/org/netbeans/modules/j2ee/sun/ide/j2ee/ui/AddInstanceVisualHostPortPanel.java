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
package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public final class AddInstanceVisualHostPortPanel extends JPanel {

    private SpinnerNumberModel valueModel = 
            new SpinnerNumberModel(4848,1,65535,1);
    /**
     * Creates new form AddInstanceVisualHostPortPanel
     */
    public AddInstanceVisualHostPortPanel() {
        initComponents();
        portValue.setModel(valueModel);
        portValue.setEditor(new javax.swing.JSpinner.NumberEditor(portValue, "#####"));
        portValue.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                fireChangeEvent();
            }
        });
        valueModel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                fireChangeEvent();
            }
        });
        hostName.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                fireChangeEvent();
            }

            public void removeUpdate(DocumentEvent e) {
                fireChangeEvent();
            }

            public void changedUpdate(DocumentEvent e) {
                fireChangeEvent();
            }
            
        });
    }

    public String getName() {
        return NbBundle.getMessage(AddInstanceVisualHostPortPanel.class, 
                "StepName_PickHostAndPort");                                // NOI18N
    }
    
    String getHost() {
        return hostName.getText().trim();
    }
    
    int getPort() {
        return valueModel.getNumber().intValue();
    }
    
    // Event Handling
    //
    private Set/*<ChangeListener.*/ listenrs = new HashSet/*<Changelisteners.*/();

    void addChangeListener(ChangeListener l) {
        synchronized (listenrs) {
            listenrs.add(l);
        }
    }
    
    void removeChangeListener(ChangeListener l ) {
        synchronized (listenrs) {
            listenrs.remove(l);
        }
    }

    RequestProcessor.Task changeEvent = null;
    
    private void fireChangeEvent() {
        // don't go so fast here, since this can get called a lot from the
        // document listener
        if (changeEvent == null) {
            changeEvent = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            Iterator it;
                            synchronized (listenrs) {
                                it = new HashSet(listenrs).iterator();
                            }
                            ChangeEvent ev = new ChangeEvent(this);
                            while (it.hasNext()) {
                                ((ChangeListener)it.next()).stateChanged(ev);
                            }
                        }
                    });
                    
                }
            }, 100);
        } else {
            changeEvent.schedule(100);
        }
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        description = new javax.swing.JLabel();
        hostNameLabel = new javax.swing.JLabel();
        hostName = new javax.swing.JTextField();
        portValueLabel = new javax.swing.JLabel();
        portValue = new javax.swing.JSpinner();
        spacingHack = new javax.swing.JLabel();

        setFocusable(false);
        setPreferredSize(new java.awt.Dimension(0, 0));
        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle"); // NOI18N
        description.setText(bundle.getString("TXT_hostPortDescription")); // NOI18N
        description.setFocusable(false);
        description.setMaximumSize(null);
        description.setMinimumSize(null);
        description.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(description, gridBagConstraints);

        hostNameLabel.setLabelFor(hostName);
        org.openide.awt.Mnemonics.setLocalizedText(hostNameLabel, org.openide.util.NbBundle.getMessage(AddInstanceVisualHostPortPanel.class, "LBL_hostNameLabel")); // NOI18N
        hostNameLabel.setMaximumSize(null);
        hostNameLabel.setMinimumSize(null);
        hostNameLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 5, 6);
        add(hostNameLabel, gridBagConstraints);

        hostName.setMaximumSize(null);
        hostName.setMinimumSize(null);
        hostName.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(hostName, gridBagConstraints);
        hostName.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_hostName")); // NOI18N

        portValueLabel.setLabelFor(portValue);
        org.openide.awt.Mnemonics.setLocalizedText(portValueLabel, org.openide.util.NbBundle.getMessage(AddInstanceVisualHostPortPanel.class, "LBL_portValueLabel")); // NOI18N
        portValueLabel.setMaximumSize(null);
        portValueLabel.setMinimumSize(null);
        portValueLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 5, 6);
        add(portValueLabel, gridBagConstraints);

        portValue.setEditor(new javax.swing.JSpinner.NumberEditor(portValue, "#####"));
        portValue.setMaximumSize(null);
        portValue.setMinimumSize(null);
        portValue.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(portValue, gridBagConstraints);
        portValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddInstanceVisualHostPortPanel.class, "ACSD_AdminPort_registered")); // NOI18N

        spacingHack.setEnabled(false);
        spacingHack.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weighty = 1.0;
        add(spacingHack, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel description;
    private javax.swing.JTextField hostName;
    private javax.swing.JLabel hostNameLabel;
    private javax.swing.JSpinner portValue;
    private javax.swing.JLabel portValueLabel;
    private javax.swing.JLabel spacingHack;
    // End of variables declaration//GEN-END:variables

}

