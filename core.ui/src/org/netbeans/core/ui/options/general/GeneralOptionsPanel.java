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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.core.ui.options.general;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.beaninfo.editors.HtmlBrowser;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public class GeneralOptionsPanel extends JPanel implements ActionListener {
    
    private boolean                 changed = false;
    private GeneralOptionsModel     model;
    private HtmlBrowser.FactoryEditor editor;
    private AdvancedProxyPanel advancedPanel;
//    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private boolean valid = true;

    
    /** 
     * Creates new form GeneralOptionsPanel. 
     */
    public GeneralOptionsPanel () {
        initComponents ();

        Color nbErrorForeground = UIManager.getColor("nb.errorForeground");
        if (nbErrorForeground == null) {
            nbErrorForeground = new Color(255, 0, 0);
        }
        errorLabel.setForeground(nbErrorForeground);
        Image img = ImageUtilities.loadImage("org/netbeans/core/ui/resources/error.gif"); //NOI18N
        errorLabel.setIcon(new ImageIcon(img));
        errorLabel.setVisible(false);
        
        loc (lWebBrowser, "Web_Browser");
        loc (lWebProxy, "Web_Proxy");
        loc (lProxyHost, "Proxy_Host");
        loc (lProxyPort, "Proxy_Port");
            
            
        cbWebBrowser.getAccessibleContext ().setAccessibleName (loc ("AN_Web_Browser"));
        cbWebBrowser.getAccessibleContext ().setAccessibleDescription (loc ("AD_Web_Browser"));
        tfProxyHost.getAccessibleContext ().setAccessibleName (loc ("AN_Host"));
        tfProxyHost.getAccessibleContext ().setAccessibleDescription (loc ("AD_Host"));
        tfProxyPort.getAccessibleContext ().setAccessibleName (loc ("AN_Port"));
        tfProxyPort.getAccessibleContext ().setAccessibleDescription (loc ("AD_Port"));
        rbNoProxy.addActionListener (this);
        rbUseSystemProxy.addActionListener (this);
        rbHTTPProxy.addActionListener (this);
        cbWebBrowser.addActionListener (this);
        tfProxyHost.addActionListener (this);
        tfProxyPort.addActionListener (this);
        tfProxyPort.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                validatePortValue();
            }

            public void removeUpdate(DocumentEvent e) {
                validatePortValue();
            }

            public void changedUpdate(DocumentEvent e) {
                validatePortValue();
            }
        });
        
        ButtonGroup bgProxy = new ButtonGroup ();
        bgProxy.add (rbNoProxy);
        bgProxy.add (rbUseSystemProxy);
        bgProxy.add (rbHTTPProxy);
        loc (rbNoProxy, "No_Proxy");
        loc (rbUseSystemProxy, "Use_System_Proxy_Settings");
        loc (rbHTTPProxy, "Use_HTTP_Proxy");
        
        loc (lUsage, "Usage_Statistics");
        lUsage.getAccessibleContext ().setAccessibleDescription (loc ("AD_Usage_Statistics"));
        lUsage.getAccessibleContext ().setAccessibleName (loc ("AN_Usage_Statistics"));

        loc (jUsageCheck, "Usage_Check");
        jUsageCheck.getAccessibleContext ().setAccessibleDescription (loc ("AD_Usage_Check"));
        jUsageCheck.getAccessibleContext ().setAccessibleName (loc ("AN_Usage_Check"));

        lblUsageInfo.setText(loc("CTL_Usage_Info"));
        lblUsageInfo.getAccessibleContext ().setAccessibleDescription (loc ("AD_Usage_Info"));
        lblUsageInfo.getAccessibleContext ().setAccessibleName (loc ("AN_Usage_Info"));
        
        lblLearnMore.setText(loc("CTL_Learn_More"));
        lblLearnMore.getAccessibleContext ().setAccessibleDescription (loc ("AD_Learn_More"));
        lblLearnMore.getAccessibleContext ().setAccessibleName (loc ("AN_Learn_More"));
        
        rbUseSystemProxy.setToolTipText (getUseSystemProxyToolTip ());

        //#144853: Show statistics ui only in IDE not in Platform.
        if (System.getProperty("nb.show.statistics.ui") == null) {
            jSeparator3.setVisible(false);
            lUsage.setVisible(false);
            jUsageCheck.setVisible(false);
            lblUsageInfo.setVisible(false);
            lblLearnMore.setVisible(false);
        }
        
        // if system proxy setting is not detectable, disable this radio
        // button
        // do not disable this radio button at all
        // it could use JDK detection sometime
        //if (System.getProperty("netbeans.system_http_proxy") == null) // NOI18N
            //rbUseSystemProxy.setEnabled(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lWebBrowser = new javax.swing.JLabel();
        cbWebBrowser = new javax.swing.JComboBox();
        jSeparator2 = new javax.swing.JSeparator();
        lWebProxy = new javax.swing.JLabel();
        rbNoProxy = new javax.swing.JRadioButton();
        rbUseSystemProxy = new javax.swing.JRadioButton();
        rbHTTPProxy = new javax.swing.JRadioButton();
        lProxyHost = new javax.swing.JLabel();
        tfProxyHost = new javax.swing.JTextField();
        lProxyPort = new javax.swing.JLabel();
        tfProxyPort = new javax.swing.JTextField();
        bMoreProxy = new javax.swing.JButton();
        editBrowserButton = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        lUsage = new javax.swing.JLabel();
        jUsageCheck = new javax.swing.JCheckBox();
        lblUsageInfo = new javax.swing.JLabel();
        lblLearnMore = new javax.swing.JLabel();
        errorLabel = new javax.swing.JLabel();

        lWebBrowser.setLabelFor(cbWebBrowser);
        org.openide.awt.Mnemonics.setLocalizedText(lWebBrowser, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.lWebBrowser.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lWebProxy, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "LBL_GeneralOptionsPanel_lWebProxy")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(rbNoProxy, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.rbNoProxy.text")); // NOI18N
        rbNoProxy.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(rbUseSystemProxy, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.rbUseSystemProxy.text")); // NOI18N
        rbUseSystemProxy.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(rbHTTPProxy, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "CTL_Use_HTTP_Proxy", new Object[] {})); // NOI18N
        rbHTTPProxy.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        lProxyHost.setLabelFor(tfProxyHost);
        org.openide.awt.Mnemonics.setLocalizedText(lProxyHost, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "CTL_Proxy_Host", new Object[] {})); // NOI18N

        tfProxyHost.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfProxyHostFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfProxyHostFocusLost(evt);
            }
        });

        lProxyPort.setLabelFor(tfProxyPort);
        org.openide.awt.Mnemonics.setLocalizedText(lProxyPort, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "CTL_Proxy_Port", new Object[] {})); // NOI18N

        tfProxyPort.setColumns(4);
        tfProxyPort.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfProxyPortFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfProxyPortFocusLost(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bMoreProxy, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "LBL_GeneralOptionsPanel_bMoreProxy")); // NOI18N
        bMoreProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bMoreProxyActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(editBrowserButton, org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.editBrowserButton.text")); // NOI18N
        editBrowserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editBrowserButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lUsage, "Usage Statistics:"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jUsageCheck, "Help us improve the NetBeans IDE by providing anonymous usage data"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblUsageInfo, "<html>The usage statistics help us better understand user\nrequirements and prioritize improvements in future releases. We will never\nreverse-engineer the collected data to find specific details about your projects.</html>"); // NOI18N
        lblUsageInfo.setFocusable(false);

        org.openide.awt.Mnemonics.setLocalizedText(lblLearnMore, "<html><font color=\"#0000FF\" <u>Learn more</u></font></html>"); // NOI18N
        lblLearnMore.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblLearnMoreMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                lblLearnMoreMousePressed(evt);
            }
        });

        errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lWebBrowser)
                        .addGap(18, 18, 18)
                        .addComponent(cbWebBrowser, 0, 1294, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editBrowserButton))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 1494, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lWebProxy)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rbNoProxy)
                            .addComponent(rbUseSystemProxy)
                            .addComponent(rbHTTPProxy)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(bMoreProxy)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(errorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 1254, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lProxyHost)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(tfProxyHost, javax.swing.GroupLayout.DEFAULT_SIZE, 1137, Short.MAX_VALUE)
                                        .addGap(12, 12, 12)
                                        .addComponent(lProxyPort)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(tfProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, 1494, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lUsage)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblUsageInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 1365, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jUsageCheck)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 838, Short.MAX_VALUE))
                            .addComponent(lblLearnMore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lWebBrowser)
                    .addComponent(cbWebBrowser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editBrowserButton))
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rbNoProxy)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rbUseSystemProxy)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rbHTTPProxy))
                    .addComponent(lWebProxy))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lProxyHost)
                    .addComponent(tfProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfProxyHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lProxyPort))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bMoreProxy)
                    .addComponent(errorLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lUsage)
                    .addComponent(jUsageCheck))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblUsageInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblLearnMore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        bMoreProxy.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "LBL_GeneralOptionsPanel_bMoreProxy.AN")); // NOI18N
        bMoreProxy.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "LBL_GeneralOptionsPanel_bMoreProxy.AD")); // NOI18N
        editBrowserButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.editBrowserButton.AN")); // NOI18N
        editBrowserButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneralOptionsPanel.class, "GeneralOptionsPanel.editBrowserButton.AD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void editBrowserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editBrowserButtonActionPerformed
    final WebBrowsersOptionsModel wbModel = new WebBrowsersOptionsModel();
    WebBrowsersOptionsPanel wbPanel = new WebBrowsersOptionsPanel(wbModel, cbWebBrowser.getSelectedItem().toString());
    DialogDescriptor dialogDesc = new DialogDescriptor (wbPanel, loc("LBL_WebBrowsersPanel_Title"), true, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (DialogDescriptor.OK_OPTION.equals(e.getSource())) {
                    wbModel.applyChanges();
                } else {
                    wbModel.discardChanges();
                }
            }
        });
    DialogDisplayer.getDefault().createDialog(dialogDesc).setVisible(true);
    if (dialogDesc.getValue().equals(DialogDescriptor.OK_OPTION)) {
        updateWebBrowsers();
        for (int i = 0, items = cbWebBrowser.getItemCount(); i < items; i++) {
            Object item = cbWebBrowser.getItemAt(i);
            if (item.equals(wbModel.getSelectedValue())) {
                cbWebBrowser.setSelectedItem(item);
                break;
            }
        }
    }
}//GEN-LAST:event_editBrowserButtonActionPerformed

private void bMoreProxyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bMoreProxyActionPerformed
    assert model != null : "Model found when AdvancedProxyPanel is created";
    if (advancedPanel == null) {
        advancedPanel = new AdvancedProxyPanel (model);
    }
    DialogDescriptor dd = new DialogDescriptor (advancedPanel, loc ("LBL_AdvancedProxyPanel_Title"));
    advancedPanel.setDialogDescriptor(dd);
    dd.createNotificationLineSupport();
    advancedPanel.update (tfProxyHost.getText (), tfProxyPort.getText ());
    DialogDisplayer.getDefault ().createDialog (dd).setVisible (true);
    if (DialogDescriptor.OK_OPTION.equals (dd.getValue ())) {
        advancedPanel.applyChanges ();
        tfProxyHost.setText (model.getHttpProxyHost ());
        tfProxyPort.setText (model.getHttpProxyPort ());
        isChanged ();
    }    
}//GEN-LAST:event_bMoreProxyActionPerformed

    private void tfProxyPortFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfProxyPortFocusLost
        tfProxyPort.select (0, 0);
    }//GEN-LAST:event_tfProxyPortFocusLost

    private void tfProxyHostFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfProxyHostFocusLost
        tfProxyHost.select (0, 0);
    }//GEN-LAST:event_tfProxyHostFocusLost

    private void tfProxyPortFocusGained (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfProxyPortFocusGained
        tfProxyPort.setCaretPosition (0);
        tfProxyPort.selectAll ();        
    }//GEN-LAST:event_tfProxyPortFocusGained

    private void tfProxyHostFocusGained (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfProxyHostFocusGained
        tfProxyHost.setCaretPosition (0);
        tfProxyHost.selectAll ();
    }//GEN-LAST:event_tfProxyHostFocusGained

    private void lblLearnMoreMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblLearnMoreMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_lblLearnMoreMouseEntered

    private void lblLearnMoreMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblLearnMoreMousePressed
        URL u = null;
        try {
            u = new URL(loc("METRICS_INFO_URL"));
        } catch (MalformedURLException exc) {
        }
        if (u != null) {
            org.openide.awt.HtmlBrowser.URLDisplayer.getDefault().showURL(u);
        }

    }//GEN-LAST:event_lblLearnMoreMousePressed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bMoreProxy;
    private javax.swing.JComboBox cbWebBrowser;
    private javax.swing.JButton editBrowserButton;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JCheckBox jUsageCheck;
    private javax.swing.JLabel lProxyHost;
    private javax.swing.JLabel lProxyPort;
    private javax.swing.JLabel lUsage;
    private javax.swing.JLabel lWebBrowser;
    private javax.swing.JLabel lWebProxy;
    private javax.swing.JLabel lblLearnMore;
    private javax.swing.JLabel lblUsageInfo;
    private javax.swing.JRadioButton rbHTTPProxy;
    private javax.swing.JRadioButton rbNoProxy;
    private javax.swing.JRadioButton rbUseSystemProxy;
    private javax.swing.JTextField tfProxyHost;
    private javax.swing.JTextField tfProxyPort;
    // End of variables declaration//GEN-END:variables
    
    private void validatePortValue() {
        clearError();

        boolean oldValid = valid;
        valid = isPortValid();
        if (!valid) {
            showError(loc("LBL_GeneralOptionsPanel_PortError")); // NOI18N
        }

        if (oldValid != valid) {
            firePropertyChange(OptionsPanelController.PROP_VALID, oldValid, valid);
        }
    }

    private boolean isPortValid() {
        String port = tfProxyPort.getText();
        boolean portStatus = true;
        if (port != null && port.length() > 0) {
            try {
                Integer.parseInt(port);
            } catch (NumberFormatException nfex) {
                portStatus = false;
            }
        }

        return portStatus;
    }

    private void showError(String message) {
        errorLabel.setVisible(true);
        errorLabel.setText(message);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    private static String loc (String key, String... params) {
        return NbBundle.getMessage (GeneralOptionsPanel.class, key, params);
    }
    
    private String getUseSystemProxyToolTip () {
        if (rbUseSystemProxy.isSelected ()) {
            String toolTip;
            String sHost = System.getProperty ("http.proxyHost"); // NOI18N
            if (sHost == null || sHost.trim ().length () == 0) {
                toolTip = loc ("GeneralOptionsPanel_rbUseSystemProxy_Direct"); // NOI18N
            } else {
                String sPort = System.getProperty ("http.proxyPort"); // NOI18N
                toolTip = loc ("GeneralOptionsPanel_rbUseSystemProxy_Format", sHost, sPort);
            }
            return toolTip;
        } else {
            return null;
        }
    }
    
    private static void loc (Component c, String key) {
        if (!(c instanceof JLabel)) {
            c.getAccessibleContext ().setAccessibleName (loc ("AN_" + key));
            c.getAccessibleContext ().setAccessibleDescription (loc ("AD_" + key));
        }
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc ("CTL_" + key)
            );
        } else {
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc ("CTL_" + key)
            );
        }
    }
    
    void update () {
        model = new GeneralOptionsModel ();
        
        // proxy settings
        switch (model.getProxyType ()) {
            case 0:
                rbNoProxy.setSelected (true);
                tfProxyHost.setEnabled (false);
                tfProxyPort.setEnabled (false);
                bMoreProxy.setEnabled (false);
                break;
            case 1:
                rbUseSystemProxy.setSelected (true);
                tfProxyHost.setEnabled (false);
                tfProxyPort.setEnabled (false);
                bMoreProxy.setEnabled (false);
                break;
            default:
                rbHTTPProxy.setSelected (true);
                tfProxyHost.setEnabled (true);
                tfProxyPort.setEnabled (true);
                bMoreProxy.setEnabled (true);
                break;
        }
        tfProxyHost.setText (model.getHttpProxyHost ());
        tfProxyPort.setText (model.getHttpProxyPort ());
        rbUseSystemProxy.setToolTipText (getUseSystemProxyToolTip ());

        jUsageCheck.setSelected(model.getUsageStatistics());
        
        updateWebBrowsers();
        
        changed = false;
    }
    
    private void updateWebBrowsers() {
        if (editor == null) {
            editor = Lookup.getDefault().lookup(HtmlBrowser.FactoryEditor.class);
        }
        cbWebBrowser.removeAllItems ();
        String[] tags = editor.getTags ();
        if (tags.length > 0) {
            for (String tag : tags) {
                cbWebBrowser.addItem(tag);
            }
            cbWebBrowser.setSelectedItem(editor.getAsText());
            lWebBrowser.setVisible(true);
            cbWebBrowser.setVisible(true);
            editBrowserButton.setVisible(true);
            jSeparator2.setVisible(true);
        } else {
            // #153747 hide web browser settings for platform
            lWebBrowser.setVisible(false);
            cbWebBrowser.setVisible(false);
            editBrowserButton.setVisible(false);
            jSeparator2.setVisible(false);
        }
    }
    
    void applyChanges () {
        // listening on JTextFields dont work!
        // if (!changed) return; 
        
        if (model == null) {
            return;
        }
        
        // proxy settings
        if (rbNoProxy.isSelected ()) {
            model.setProxyType (0);
        } else
        if (rbUseSystemProxy.isSelected ()) {
            model.setProxyType (1);
        } else {
            model.setProxyType (2);
        }
        
        model.setHttpProxyHost (tfProxyHost.getText ());
        model.setHttpProxyPort (tfProxyPort.getText ());
        if (model.useProxyAllProtocols ()) {
            model.setHttpsProxyHost (tfProxyHost.getText ());
            model.setHttpsProxyPort (tfProxyPort.getText ());
            model.setSocksHost (tfProxyHost.getText ());
            model.setSocksPort (tfProxyPort.getText ());
        }

        // web browser settings
        if (editor == null) {
            editor = Lookup.getDefault().lookup(HtmlBrowser.FactoryEditor.class);
        }
        editor.setAsText ((String) cbWebBrowser.getSelectedItem ());

        model.setUsageStatistics(jUsageCheck.isSelected());
    }
    
    void cancel () {
    }
    
    boolean dataValid () {
        return isPortValid();
    }
    
    boolean isChanged () {
        if (model == null) {
            return false;
        }
        if (!tfProxyHost.getText().equals(model.getHttpProxyHost())) {
            return true;
        }
        if (!tfProxyPort.getText().equals(model.getHttpProxyPort())) {
            return true;
        }
        return changed;
    }

    public void actionPerformed (ActionEvent e) {
        changed = true;
        tfProxyHost.setEnabled (rbHTTPProxy.isSelected ());
        tfProxyPort.setEnabled (rbHTTPProxy.isSelected ());
        bMoreProxy.setEnabled (rbHTTPProxy.isSelected ());
        rbUseSystemProxy.setToolTipText (getUseSystemProxyToolTip ());
    }
}
