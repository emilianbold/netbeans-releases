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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import org.openide.util.NbBundle;

/**
 * Represents <em>Window System parameters</em> panel in branding editor.
 *
 * @author Radek Matous, S. Aubrecht
 */
public class WindowSystemBrandingPanel extends AbstractBrandingPanel {
    
    public WindowSystemBrandingPanel(BasicBrandingModel model) {
        super(NbBundle.getMessage(BasicBrandingPanel.class, "LBL_WindowSystemTab"), model); //NOI18N
        
        initComponents();
        refresh();
        enableDisableComponents();
        
        ItemListener listener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setModified();
            }
        };
        cbEnableDnd.addItemListener(listener);
        cbEnableEditorClosing.addItemListener(listener);
        cbEnableFloating.addItemListener(listener);
        cbEnableMaximization.addItemListener(listener);
        cbEnableMinimumSize.addItemListener(listener);
        cbEnableResizing.addItemListener(listener);
        cbEnableSliding.addItemListener(listener);
        cbEnableViewClosing.addItemListener(listener);
    }
    
    
    @Override
    public void store() {
        BasicBrandingModel branding = getBranding();
        
        SplashUISupport.setValue(branding.getWsEnableClosingEditors(), Boolean.toString(cbEnableEditorClosing.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableClosingViews(), Boolean.toString(cbEnableViewClosing.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableDragAndDrop(), Boolean.toString(cbEnableDnd.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableFloating(), Boolean.toString(cbEnableFloating.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableMaximization(), Boolean.toString(cbEnableMaximization.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableMinimumSize(), Boolean.toString(cbEnableMinimumSize.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableResizing(), Boolean.toString(cbEnableResizing.isSelected()));
        SplashUISupport.setValue(branding.getWsEnableSliding(), Boolean.toString(cbEnableSliding.isSelected()));
    }
    
    
    void refresh() {
        BasicBrandingModel branding = getBranding();
        
        cbEnableDnd.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableDragAndDrop()));
        cbEnableEditorClosing.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableClosingEditors()));
        cbEnableFloating.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableFloating()));
        cbEnableMaximization.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableMaximization()));
        cbEnableMinimumSize.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableMinimumSize()));
        cbEnableResizing.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableResizing()));
        cbEnableSliding.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableSliding()));
        cbEnableViewClosing.setSelected(SplashUISupport.bundleKeyToBoolean(branding.getWsEnableClosingViews()));
        
        enableDisableComponents();
        
    }
    
    private void enableDisableComponents() {
        final BasicBrandingModel branding = getBranding();
        cbEnableDnd.setEnabled(branding.isBrandingEnabled());
        cbEnableEditorClosing.setEnabled(branding.isBrandingEnabled());
        cbEnableFloating.setEnabled(branding.isBrandingEnabled());
        cbEnableMinimumSize.setEnabled(branding.isBrandingEnabled());
        cbEnableResizing.setEnabled(branding.isBrandingEnabled());
        cbEnableSliding.setEnabled(branding.isBrandingEnabled());
        cbEnableViewClosing.setEnabled(branding.isBrandingEnabled());
        cbEnableMaximization.setEnabled(branding.isBrandingEnabled());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbEnableDnd = new javax.swing.JCheckBox();
        cbEnableFloating = new javax.swing.JCheckBox();
        cbEnableSliding = new javax.swing.JCheckBox();
        cbEnableViewClosing = new javax.swing.JCheckBox();
        cbEnableEditorClosing = new javax.swing.JCheckBox();
        cbEnableResizing = new javax.swing.JCheckBox();
        cbEnableMinimumSize = new javax.swing.JCheckBox();
        cbEnableMaximization = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();

        cbEnableDnd.setMnemonic('D');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableDnd, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "LBL_EnableDnD")); // NOI18N

        cbEnableFloating.setMnemonic('F');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableFloating, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "LBL_EnableFloating")); // NOI18N

        cbEnableSliding.setMnemonic('S');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableSliding, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "LBL_EnableSliding")); // NOI18N

        cbEnableViewClosing.setMnemonic('N');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableViewClosing, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "LBL_EnableViewClosing")); // NOI18N

        cbEnableEditorClosing.setMnemonic('C');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableEditorClosing, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "LBL_EnableEditorClosing")); // NOI18N

        cbEnableResizing.setMnemonic('R');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableResizing, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "LBL_EnableResizing")); // NOI18N

        cbEnableMinimumSize.setMnemonic('E');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableMinimumSize, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "LBL_EnableMinimumSize")); // NOI18N

        cbEnableMaximization.setMnemonic('M');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableMaximization, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "LBL_EnableMaximization")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(WindowSystemBrandingPanel.class, "SuiteCustomizerWindowSystemBranding.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbEnableEditorClosing)
                    .addComponent(cbEnableMinimumSize)
                    .addComponent(cbEnableFloating)
                    .addComponent(cbEnableSliding)
                    .addComponent(cbEnableMaximization)
                    .addComponent(cbEnableViewClosing)
                    .addComponent(cbEnableResizing)
                    .addComponent(cbEnableDnd))
                .addContainerGap(137, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cbEnableDnd))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableFloating)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableSliding)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableMaximization)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableViewClosing)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableEditorClosing, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableResizing)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableMinimumSize)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        cbEnableDnd.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "ACSD_EnableDnD")); // NOI18N
        cbEnableFloating.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "ACSD_EnableFloating")); // NOI18N
        cbEnableSliding.getAccessibleContext().setAccessibleName(NbBundle.getMessage(WindowSystemBrandingPanel.class, "SuiteCustomizerWindowSystemBranding.cbEnableSliding.AccessibleContext.accessibleName")); // NOI18N
        cbEnableSliding.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "ACSD_EnableSliding")); // NOI18N
        cbEnableViewClosing.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "ACSD_EnableViewClosing")); // NOI18N
        cbEnableEditorClosing.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "ACSD_EnableEditorClosing")); // NOI18N
        cbEnableResizing.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "ACSD_EnableResizing")); // NOI18N
        cbEnableMinimumSize.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "ACSD_EnableMinimumSize")); // NOI18N
        cbEnableMaximization.getAccessibleContext().setAccessibleName(NbBundle.getMessage(WindowSystemBrandingPanel.class, "SuiteCustomizerWindowSystemBranding.cbEnableMaximization.AccessibleContext.accessibleName")); // NOI18N
        cbEnableMaximization.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "ACSD_EnableMaximization")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WindowSystemBrandingPanel.class, "SuiteCustomizerWindowSystemBranding.jLabel1.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(WindowSystemBrandingPanel.class, "SuiteCustomizerWindowSystemBranding.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
            
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbEnableDnd;
    private javax.swing.JCheckBox cbEnableEditorClosing;
    private javax.swing.JCheckBox cbEnableFloating;
    private javax.swing.JCheckBox cbEnableMaximization;
    private javax.swing.JCheckBox cbEnableMinimumSize;
    private javax.swing.JCheckBox cbEnableResizing;
    private javax.swing.JCheckBox cbEnableSliding;
    private javax.swing.JCheckBox cbEnableViewClosing;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
