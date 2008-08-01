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

package org.netbeans.modules.apisupport.project.ui.customizer;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.NbBundle;

/**
 * Represents <em>Splash branding parameters</em> panel in Suite customizer.
 *
 * @author Radek Matous
 */
public class SuiteCustomizerWindowSystemBranding extends NbPropertyPanel.Suite {
    
    /**
     * Creates new form SuiteCustomizerLibraries
     */
    public SuiteCustomizerWindowSystemBranding(final SuiteProperties suiteProps, ProjectCustomizer.Category cat) {
        super(suiteProps, SuiteCustomizerWindowSystemBranding.class, cat);
        BasicBrandingModel branding = getBrandingModel();
        branding.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                enableDisableComponents();
            }
        });
        
        initComponents();
        refresh();
        cat.setValid(true);
        cat.setErrorMessage(null);
    }
    
    
    @Override
    public void store() {
        BasicBrandingModel branding = getBrandingModel();
        
        branding.getWsEnableClosingEditors().setValue( Boolean.toString(cbEnableEditorClosing.isSelected() ) );
        branding.getWsEnableClosingViews().setValue( Boolean.toString(cbEnableViewClosing.isSelected() ) );
        branding.getWsEnableDragAndDrop().setValue( Boolean.toString(cbEnableDnd.isSelected() ) );
        branding.getWsEnableFloating().setValue( Boolean.toString(cbEnableFloating.isSelected() ) );
        branding.getWsEnableMaximization().setValue( Boolean.toString(cbEnableMaximization.isSelected() ) );
        branding.getWsEnableMinimumSize().setValue( Boolean.toString(cbEnableMinimumSize.isSelected() ) );
        branding.getWsEnableResizing().setValue( Boolean.toString(cbEnableResizing.isSelected() ) );
        branding.getWsEnableSliding().setValue( Boolean.toString(cbEnableSliding.isSelected() ) );
    }
    
    
    void refresh() {
        BasicBrandingModel branding = getBrandingModel();
        
        cbEnableDnd.setSelected(Boolean.parseBoolean(branding.getWsEnableDragAndDrop().getValue()));
        cbEnableEditorClosing.setSelected(Boolean.parseBoolean(branding.getWsEnableClosingEditors().getValue()));
        cbEnableFloating.setSelected(Boolean.parseBoolean(branding.getWsEnableFloating().getValue()));
        cbEnableMaximization.setSelected(Boolean.parseBoolean(branding.getWsEnableMaximization().getValue()));
        cbEnableMinimumSize.setSelected(Boolean.parseBoolean(branding.getWsEnableMinimumSize().getValue()));
        cbEnableResizing.setSelected(Boolean.parseBoolean(branding.getWsEnableResizing().getValue()));
        cbEnableSliding.setSelected(Boolean.parseBoolean(branding.getWsEnableSliding().getValue()));
        cbEnableViewClosing.setSelected(Boolean.parseBoolean(branding.getWsEnableClosingViews().getValue()));
        
        enableDisableComponents();
        
    }
    
    private void enableDisableComponents() {
        final BasicBrandingModel branding = getBrandingModel();
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
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableDnd, org.openide.util.NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "LBL_EnableDnD")); // NOI18N

        cbEnableFloating.setMnemonic('F');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableFloating, org.openide.util.NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "LBL_EnableFloating")); // NOI18N

        cbEnableSliding.setMnemonic('S');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableSliding, org.openide.util.NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "LBL_EnableSliding")); // NOI18N

        cbEnableViewClosing.setMnemonic('N');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableViewClosing, org.openide.util.NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "LBL_EnableViewClosing")); // NOI18N

        cbEnableEditorClosing.setMnemonic('C');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableEditorClosing, org.openide.util.NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "LBL_EnableEditorClosing")); // NOI18N

        cbEnableResizing.setMnemonic('R');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableResizing, org.openide.util.NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "LBL_EnableResizing")); // NOI18N

        cbEnableMinimumSize.setMnemonic('E');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableMinimumSize, org.openide.util.NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "LBL_EnableMinimumSize")); // NOI18N

        cbEnableMaximization.setMnemonic('M');
        org.openide.awt.Mnemonics.setLocalizedText(cbEnableMaximization, org.openide.util.NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "LBL_EnableMaximization")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "SuiteCustomizerWindowSystemBranding.jLabel1.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cbEnableEditorClosing)
                    .add(cbEnableMinimumSize)
                    .add(cbEnableFloating)
                    .add(cbEnableSliding)
                    .add(cbEnableMaximization)
                    .add(cbEnableViewClosing)
                    .add(cbEnableResizing)
                    .add(cbEnableDnd))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(cbEnableDnd))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbEnableFloating)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbEnableSliding)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbEnableMaximization)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbEnableViewClosing)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbEnableEditorClosing, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbEnableResizing)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbEnableMinimumSize)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        cbEnableDnd.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "ACSD_EnableDnD")); // NOI18N
        cbEnableFloating.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "ACSD_EnableFloating")); // NOI18N
        cbEnableSliding.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "SuiteCustomizerWindowSystemBranding.cbEnableSliding.AccessibleContext.accessibleName")); // NOI18N
        cbEnableSliding.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "ACSD_EnableSliding")); // NOI18N
        cbEnableViewClosing.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "ACSD_EnableViewClosing")); // NOI18N
        cbEnableEditorClosing.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "ACSD_EnableEditorClosing")); // NOI18N
        cbEnableResizing.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "ACSD_EnableResizing")); // NOI18N
        cbEnableMinimumSize.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "ACSD_EnableMinimumSize")); // NOI18N
        cbEnableMaximization.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "SuiteCustomizerWindowSystemBranding.cbEnableMaximization.AccessibleContext.accessibleName")); // NOI18N
        cbEnableMaximization.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "ACSD_EnableMaximization")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "SuiteCustomizerWindowSystemBranding.jLabel1.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SuiteCustomizerWindowSystemBranding.class, "SuiteCustomizerWindowSystemBranding.AccessibleContext.accessibleName")); // NOI18N
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
    
    private BasicBrandingModel getBrandingModel() {
        return getProperties().getBrandingModel();
    }
}
