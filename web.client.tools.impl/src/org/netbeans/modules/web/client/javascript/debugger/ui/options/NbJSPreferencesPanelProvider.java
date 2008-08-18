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
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.javascript.editing.spi.JSPreferencesPanel;
import org.netbeans.modules.javascript.editing.spi.JSPreferencesPanelProvider;
import org.netbeans.modules.web.client.javascript.debugger.models.NbJSPreferences;

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
    }

    public void load() {
        updateUIFromPreferences(preferences);
    }
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
    }
    
    void updatePreferencesFromUI(NbJSPreferences preferences) {
        preferences.setShowConstants(showConstantsCheckBox.isSelected());
        preferences.setShowFunctions(showFunctionsCheckBox.isSelected());
        
        preferences.setSuspendOnDebuggerKeyword(suspendOnDebuggerKeywordCheckBox.isSelected());
        preferences.setSuspendOnErrors(suspendOnErrorsCheckBox.isSelected());
        preferences.setSuspendOnExceptions(suspendOnExceptionsCheckBox.isSelected());
        preferences.setSuspendOnFirstLine(suspendOnFirstLineCheckBox.isSelected());
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

        org.openide.awt.Mnemonics.setLocalizedText(showFunctionsCheckBox, org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.showFunctionsCheckBox.text")); // NOI18N
        showFunctionsCheckBox.setMaximumSize(new java.awt.Dimension(122, 22));
        showFunctionsCheckBox.setMinimumSize(new java.awt.Dimension(122, 22));

        org.openide.awt.Mnemonics.setLocalizedText(showConstantsCheckBox, org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.showConstantsCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(suspendOnFirstLineCheckBox, org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.suspendOnFirstLineCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(suspendOnExceptionsCheckBox, org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.suspendOnExceptionsCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(suspendOnErrorsCheckBox, org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.suspendOnErrorsCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(suspendOnDebuggerKeywordCheckBox, org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.suspendOnDebuggerKeywordCheckBox.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(NbJSPreferencesPanelProvider.class, "NbJSPreferencesPanelProvider.Debugging.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(suspendOnExceptionsCheckBox)
                    .add(suspendOnFirstLineCheckBox)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(suspendOnDebuggerKeywordCheckBox)
                            .add(suspendOnErrorsCheckBox))
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(showFunctionsCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(showConstantsCheckBox))))
                .addContainerGap(107, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(suspendOnDebuggerKeywordCheckBox)
                    .add(showConstantsCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(suspendOnErrorsCheckBox)
                    .add(showFunctionsCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(suspendOnExceptionsCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(suspendOnFirstLineCheckBox)
                .addContainerGap(155, Short.MAX_VALUE))
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
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JCheckBox showConstantsCheckBox;
    private javax.swing.JCheckBox showFunctionsCheckBox;
    private javax.swing.JCheckBox suspendOnDebuggerKeywordCheckBox;
    private javax.swing.JCheckBox suspendOnErrorsCheckBox;
    private javax.swing.JCheckBox suspendOnExceptionsCheckBox;
    private javax.swing.JCheckBox suspendOnFirstLineCheckBox;
    // End of variables declaration//GEN-END:variables

}
