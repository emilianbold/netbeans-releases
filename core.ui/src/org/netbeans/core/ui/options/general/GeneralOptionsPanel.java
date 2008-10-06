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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.beaninfo.editors.HtmlBrowser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
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

    
    /** 
     * Creates new form GeneralOptionsPanel. 
     */
    public GeneralOptionsPanel () {
        initComponents ();
        
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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(lWebBrowser)
                        .add(18, 18, 18)
                        .add(cbWebBrowser, 0, 1131, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(editBrowserButton))
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1291, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(lWebProxy)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rbNoProxy)
                            .add(rbUseSystemProxy)
                            .add(rbHTTPProxy)
                            .add(layout.createSequentialGroup()
                                .add(17, 17, 17)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(bMoreProxy)
                                    .add(layout.createSequentialGroup()
                                        .add(lProxyHost)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(tfProxyHost, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 957, Short.MAX_VALUE)
                                        .add(12, 12, 12)
                                        .add(lProxyPort)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(tfProxyPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))))
                    .add(jSeparator3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1291, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(lUsage)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblUsageInfo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1171, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(jUsageCheck)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 690, Short.MAX_VALUE))
                            .add(lblLearnMore))))
                .add(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lWebBrowser)
                    .add(cbWebBrowser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(editBrowserButton))
                .add(18, 18, 18)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(rbNoProxy)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(rbUseSystemProxy)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(rbHTTPProxy))
                    .add(lWebProxy))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lProxyHost)
                    .add(tfProxyPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(tfProxyHost, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lProxyPort))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bMoreProxy)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lUsage)
                    .add(jUsageCheck))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblUsageInfo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 52, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblLearnMore)
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
        int i, k = tags.length;
        for (i = 0; i < k; i++) {
            cbWebBrowser.addItem (tags [i]);
        }
        cbWebBrowser.setSelectedItem (editor.getAsText ());
    }
    
    void applyChanges () {
        // listening on JTextFields dont work!
        // if (!changed) return; 
        
        if (model == null) return;
        
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
        return true;
    }
    
    boolean isChanged () {
        if (model == null) return false;
        if (!tfProxyHost.getText ().equals (model.getHttpProxyHost ())) return true;
        if (!tfProxyPort.getText ().equals (model.getHttpProxyPort ())) return true;
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
