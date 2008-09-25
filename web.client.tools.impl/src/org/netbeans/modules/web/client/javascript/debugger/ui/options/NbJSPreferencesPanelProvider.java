/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.web.client.javascript.debugger.ui.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.javascript.editing.spi.JSPreferencesPanel;
import org.netbeans.modules.javascript.editing.spi.JSPreferencesPanelProvider;
import org.netbeans.modules.web.client.javascript.debugger.models.NbJSPreferences;
import org.netbeans.modules.web.client.tools.api.FirefoxBrowserUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;

/**
 *
 * @author  quynguyen
 */
public class NbJSPreferencesPanelProvider extends JSPreferencesPanel implements JSPreferencesPanelProvider, ActionListener {
    
    private List<ChangeListener> listeners;
    private NbJSPreferencesPanelProvider INSTANCE = null;
    
    
    public JSPreferencesPanel getPanel () {
        if( INSTANCE == null )  {
            INSTANCE = new NbJSPreferencesPanelProvider();
        }
        return INSTANCE;
    }
    public static final NbJSPreferences preferences = NbJSPreferences.getInstance();
    
    /** Creates new form NbJSAdvancedOptionsPanel */
    public NbJSPreferencesPanelProvider() {
        initComponents();
        
        showFunctionsCheckBox.addActionListener(this);
        showFunctionsCheckBox.addActionListener(this);
        suspendOnErrorsCheckBox.addActionListener(this);
        suspendOnExceptionsCheckBox.addActionListener(this);
        suspendOnFirstLineCheckBox.addActionListener(this);
        
        errorLabel.setVisible(false);
        ffProfileTextField.getDocument().addDocumentListener(
                new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                validateProfileDir();
            }

            public void removeUpdate(DocumentEvent e) {
                validateProfileDir();
            }

            public void changedUpdate(DocumentEvent e) {
                validateProfileDir();
            }
            
        });
    }

    @Override
    public void load() {
        updateUIFromPreferences(preferences);
    }

    @Override
    public void store() {
        updatePreferencesFromUI(preferences);
    }

    void updateUIFromPreferences(NbJSPreferences preferences) {
        showConstantsCheckBox.setSelected(preferences.getShowConstants());
        showFunctionsCheckBox.setSelected(preferences.getShowFunctions());
        suspendOnDebuggerKeywordCheckBox.setSelected(preferences.getSuspendOnDebuggerKeyword());
        suspendOnErrorsCheckBox.setSelected(preferences.getSuspendOnErrors());
        suspendOnExceptionsCheckBox.setSelected(preferences.getSuspendOnExceptions());
        suspendOnFirstLineCheckBox.setSelected(preferences.getSuspendOnFirstLine());
        ignoreQueryStringsCheckBox.setSelected(preferences.getIgnoreQueryStrings());
        
        Preferences prefs = NbPreferences.forModule(FirefoxBrowserUtils.class);
        String defaultProfile = prefs.get(FirefoxBrowserUtils.PROFILE_PREF, "");
        
        ffProfileTextField.setText(defaultProfile);
        validateProfileDir();
    }
    
    void updatePreferencesFromUI(NbJSPreferences preferences) {
        preferences.setShowConstants(showConstantsCheckBox.isSelected());
        preferences.setShowFunctions(showFunctionsCheckBox.isSelected());
        
        preferences.setSuspendOnDebuggerKeyword(suspendOnDebuggerKeywordCheckBox.isSelected());
        preferences.setSuspendOnErrors(suspendOnErrorsCheckBox.isSelected());
        preferences.setSuspendOnExceptions(suspendOnExceptionsCheckBox.isSelected());
        preferences.setSuspendOnFirstLine(suspendOnFirstLineCheckBox.isSelected());

        preferences.setIgnoreQueryStrings(ignoreQueryStringsCheckBox.isSelected());
        
        Preferences prefs = NbPreferences.forModule(FirefoxBrowserUtils.class);
        String defaultProfile = prefs.get(FirefoxBrowserUtils.PROFILE_PREF, "");
        String newProfile = ffProfileTextField.getText();
        
        if (!defaultProfile.equals(newProfile) && validateProfileDir()) {
            prefs.put(FirefoxBrowserUtils.PROFILE_PREF, newProfile);
            try {
                prefs.sync();
            } catch (BackingStoreException ex) {
                Logger.getLogger(NbJSPreferencesPanelProvider.class.getName()).log(Level.INFO, "Could not save preferences", ex);
            }
        }
    }
    
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof javax.swing.JCheckBox) {
            ChangeEvent changeEvent = new ChangeEvent(source);
            
            if (listeners != null) {
                for (ChangeListener listener : listeners) {
                    listener.stateChanged(changeEvent);
                }
            }
        }
    }
    
    private boolean validateProfileDir() {
        String text = ffProfileTextField.getText();
        if (text.length() == 0) {
            errorLabel.setVisible(false);
            return true;
        }
        
        File f = new File(text);
        if (f.isDirectory()) {
            errorLabel.setVisible(false);
            return true;
        } else {
            errorLabel.setVisible(true);
            return false;
        }
    }
    
    public void addChangeListener(ChangeListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ChangeListener>();
        }
        
        listeners.add(listener);
    }
    
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        showFunctionsCheckBox = new javax.swing.JCheckBox();
        showConstantsCheckBox = new javax.swing.JCheckBox();
        suspendOnFirstLineCheckBox = new javax.swing.JCheckBox();
        suspendOnExceptionsCheckBox = new javax.swing.JCheckBox();
        suspendOnErrorsCheckBox = new javax.swing.JCheckBox();
        suspendOnDebuggerKeywordCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        ffProfileLabel = new javax.swing.JLabel();
        ffProfileTextField = new javax.swing.JTextField();
        ffProfileBrowseButton = new javax.swing.JButton();
        errorLabel = new javax.swing.JLabel();
        ignoreQueryStringsCheckBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(showFunctionsCheckBox, org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.showFunctionsCheckBox.text")); // NOI18N
        showFunctionsCheckBox.setMaximumSize(new java.awt.Dimension(122, 22));
        showFunctionsCheckBox.setMinimumSize(new java.awt.Dimension(122, 22));

        org.openide.awt.Mnemonics.setLocalizedText(showConstantsCheckBox, org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.showConstantsCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(suspendOnFirstLineCheckBox, org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.suspendOnFirstLineCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(suspendOnExceptionsCheckBox, org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.suspendOnExceptionsCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(suspendOnErrorsCheckBox, org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.suspendOnErrorsCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(suspendOnDebuggerKeywordCheckBox, org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.suspendOnDebuggerKeywordCheckBox.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.Debugging.text")); // NOI18N

        ffProfileLabel.setLabelFor(ffProfileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(ffProfileLabel, org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.ffProfileLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ffProfileBrowseButton, org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.ffProfileBrowseButton.text")); // NOI18N
        ffProfileBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ffProfileBrowseButtonActionPerformed(evt);
            }
        });

        errorLabel.setText(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.errorLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ignoreQueryStringsCheckBox, org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.ignoreQueryStringsCheckBox.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(suspendOnFirstLineCheckBox)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(suspendOnDebuggerKeywordCheckBox)
                                    .add(suspendOnErrorsCheckBox)
                                    .add(suspendOnExceptionsCheckBox))
                                .add(18, 18, 18)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(ignoreQueryStringsCheckBox)
                                    .add(showConstantsCheckBox)
                                    .add(showFunctionsCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                    .add(layout.createSequentialGroup()
                        .add(ffProfileLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(errorLabel)
                            .add(layout.createSequentialGroup()
                                .add(ffProfileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 302, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(ffProfileBrowseButton)))))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel1)
                            .add(suspendOnDebuggerKeywordCheckBox))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(suspendOnErrorsCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(suspendOnExceptionsCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(suspendOnFirstLineCheckBox))
                    .add(layout.createSequentialGroup()
                        .add(showConstantsCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showFunctionsCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ignoreQueryStringsCheckBox)))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ffProfileLabel)
                    .add(ffProfileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ffProfileBrowseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(errorLabel)
                .addContainerGap(76, Short.MAX_VALUE))
        );

        showFunctionsCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "A11Y_CKBX_ShowFunctions")); // NOI18N
        showFunctionsCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "A11Y_CKBX_ShowFunctions")); // NOI18N
        showConstantsCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "A11Y_CKBX_ShowConstants")); // NOI18N
        showConstantsCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "A11Y_CKBX_ShowConstants")); // NOI18N
        suspendOnFirstLineCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "A11Y_CKBX_SuspendOnFirstLine")); // NOI18N
        suspendOnFirstLineCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "A11Y_CKBX_SuspendOnFirstLine")); // NOI18N
        suspendOnExceptionsCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "A11Y_CKBX_SuspendOnExceptions")); // NOI18N
        suspendOnExceptionsCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "A11Y_CKBX_SuspendOnExceptions")); // NOI18N
        suspendOnErrorsCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "A11Y_CKBX_SuspendOnErrors")); // NOI18N
        suspendOnErrorsCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSAdvancedOptionsPanel.suspendOnErrorsCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        suspendOnDebuggerKeywordCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "A11Y_CKBX_ShowOnDebuggerKeyword")); // NOI18N
        suspendOnDebuggerKeywordCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "A11Y_CKBX_ShowOnDebuggerKeyword")); // NOI18N
        ffProfileTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.ffProfileTextField.AccessibleContext.accessibleDescription")); // NOI18N
        ffProfileBrowseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.ffProfileBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        ignoreQueryStringsCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.ignoreQueryStringsCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void ffProfileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ffProfileBrowseButtonActionPerformed

    JFileChooser chooser = new JFileChooser();
    FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    
    String text = ffProfileTextField.getText();
    if (text.length() > 0) {
        File f = new File(text);
        if (f.exists()) {
            chooser.setCurrentDirectory(f);
        }
    }
    
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        File selected = FileUtil.normalizeFile(chooser.getSelectedFile());
        ffProfileTextField.setText(selected.getAbsolutePath());
    }
}//GEN-LAST:event_ffProfileBrowseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorLabel;
    private javax.swing.JButton ffProfileBrowseButton;
    private javax.swing.JLabel ffProfileLabel;
    private javax.swing.JTextField ffProfileTextField;
    private javax.swing.JCheckBox ignoreQueryStringsCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JCheckBox showConstantsCheckBox;
    private javax.swing.JCheckBox showFunctionsCheckBox;
    private javax.swing.JCheckBox suspendOnDebuggerKeywordCheckBox;
    private javax.swing.JCheckBox suspendOnErrorsCheckBox;
    private javax.swing.JCheckBox suspendOnExceptionsCheckBox;
    private javax.swing.JCheckBox suspendOnFirstLineCheckBox;
    // End of variables declaration//GEN-END:variables

}
