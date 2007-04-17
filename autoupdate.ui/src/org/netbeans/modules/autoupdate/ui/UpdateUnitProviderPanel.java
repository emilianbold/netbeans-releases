/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.ui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;

/**
 * @author  Radek Matous
 */
public class UpdateUnitProviderPanel extends javax.swing.JPanel {
    private DocumentListener listener;
    private FocusListener focusNameListener;
    private FocusListener focusUrlListener;
    private JButton bOK = new JButton(NbBundle.getMessage(UpdateUnitProviderPanel.class, "UpdateUnitProviderPanel_OK"));//NOI18N        
    /** Creates new form UpdateUnitProviderPanel */
    public UpdateUnitProviderPanel(boolean isActive, String name, String url) {
        initComponents();
        addListeners ();
        tfURL.setText(url);
        tfName.setText(name);
        cbActive.setSelected(isActive);
    }
    
    JButton getOKButton() {
        return bOK;
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        addListeners ();
    }
    
    private void addListeners () {
        if (listener == null) {
            listener = new DocumentListener() {
                public void insertUpdate(DocumentEvent arg0) {
                    update();
                }

                public void removeUpdate(DocumentEvent arg0) {
                    update();
                }

                public void changedUpdate(DocumentEvent arg0) {
                    update();
                }   
                
                private void update() {
                    bOK.setEnabled(isValid());
                }
                
                private boolean isValid() {
                    boolean isOk = getProviderName().length() > 0 && getProviderURL ().length () > 0;
                    if (isOk) {
                        String s = getProviderURL ();
                        try {
                            new URI (s).toURL ();
                        } catch (MalformedURLException x) {
                            isOk = false;
                        } catch (URISyntaxException x) {
                            isOk = false;
                        } catch (IllegalArgumentException x) {
                            isOk = false;
                        }
                    }
                    return isOk;
                }
            };
            focusNameListener = new FocusListener () {
                public void focusGained(FocusEvent e) {
                    tfName.selectAll ();
                }
                public void focusLost(FocusEvent e) {
                    tfName.select (0, 0);
                }
            };
            tfName.addFocusListener (focusNameListener);
            
            focusUrlListener = new FocusListener () {
                public void focusGained(FocusEvent e) {
                    tfURL.selectAll ();
                }
                public void focusLost(FocusEvent e) {
                    tfURL.select (0, 0);
                }
            };
            tfURL.addFocusListener (focusUrlListener);
            
            tfName.getDocument().addDocumentListener(listener);
            tfURL.getDocument().addDocumentListener(listener);
        }        
    }

    private void removeListener() {
        if (listener != null) {
            tfName.getDocument().removeDocumentListener(listener);
            tfURL.getDocument().removeDocumentListener(listener); 
            tfName.removeFocusListener (focusNameListener);
            tfURL.removeFocusListener (focusUrlListener);
            listener = null;
        }        
    }   
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        removeListener();
    }

    
    public String getDisplayName() {
        return NbBundle.getMessage(SettingsTab.class, "UpdateUnitProviderPanel_displayName");//NOI18N
    }
    
    public boolean isActive() {
        return cbActive.isSelected();
    }
    
    public String getProviderName() {
        return tfName.getText().trim();
    }
    
    public String getProviderURL() {
        return tfURL.getText().trim();
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        lName = new javax.swing.JLabel();
        tfName = new javax.swing.JTextField();
        lURL = new javax.swing.JLabel();
        tfURL = new javax.swing.JTextField();
        cbActive = new javax.swing.JCheckBox();

        lName.setLabelFor(tfName);
        org.openide.awt.Mnemonics.setLocalizedText(lName, org.openide.util.NbBundle.getMessage(UpdateUnitProviderPanel.class, "UpdateUnitProviderPanel.lName.text")); // NOI18N

        lURL.setLabelFor(tfURL);
        org.openide.awt.Mnemonics.setLocalizedText(lURL, org.openide.util.NbBundle.getMessage(UpdateUnitProviderPanel.class, "UpdateUnitProviderPanel.lURL.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbActive, org.openide.util.NbBundle.getMessage(UpdateUnitProviderPanel.class, "UpdateUnitProviderPanel.cbActive.text")); // NOI18N
        cbActive.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lName)
                    .add(lURL))
                .add(15, 15, 15)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(tfURL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .add(tfName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .add(cbActive))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lName)
                    .add(tfName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbActive)
                .add(24, 24, 24)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lURL)
                    .add(tfURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        lName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateUnitProviderPanel.class, "UpdateUnitProviderPanel.lName.adesc")); // NOI18N
        lURL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateUnitProviderPanel.class, "UpdateUnitProviderPanel.lURL.adesc")); // NOI18N
        cbActive.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateUnitProviderPanel.class, "UpdateUnitProviderPanel.cbActive.adesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbActive;
    private javax.swing.JLabel lName;
    private javax.swing.JLabel lURL;
    private javax.swing.JTextField tfName;
    private javax.swing.JTextField tfURL;
    // End of variables declaration//GEN-END:variables
    
}
