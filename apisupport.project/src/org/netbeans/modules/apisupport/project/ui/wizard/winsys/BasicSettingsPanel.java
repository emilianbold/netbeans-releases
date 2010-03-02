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

package org.netbeans.modules.apisupport.project.ui.wizard.winsys;

import java.awt.Cursor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.AsyncGUIJob;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * the first panel in TopComponent wizard
 *
 * @author Milos Kleint
 */
final class BasicSettingsPanel extends BasicWizardIterator.Panel {
    
    private NewTCIterator.DataModel data;
    private static final String[] DEFAULT_MODES = 
            new String[] {
                "editor" //NOI18N
            };
    private boolean loadedComboBox = false;

    /**
     * Creates new form BasicSettingsPanel
     */
    public BasicSettingsPanel(WizardDescriptor setting, NewTCIterator.DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        initAccessibility();
        setupCombo();
        putClientProperty("NewFileWizard_Title", getMessage("LBL_TCWizardTitle"));
    }
    
    private void checkValidity() {
        //TODO: probably nothing...
        if (loadedComboBox) {
            markValid();
        } else {
            markInvalid();
        }
    }
    
//    public void addNotify() {
//        super.addNotify();
//        attachDocumentListeners();
//        checkValidity();
//    }
//
//    public void removeNotify() {
//        // prevent checking when the panel is not "active"
//        removeDocumentListeners();
//        super.removeNotify();
//    }
//
//    private void attachDocumentListeners() {
//        if (!listenersAttached) {
//            listenersAttached = true;
//        }
//    }
//
//    private void removeDocumentListeners() {
//        if (listenersAttached) {
//            listenersAttached = false;
//        }
//    }
    
    private void setupCombo() {
        //TODO get dynamically from layers??
        final Cursor currentCursor = getCursor();
        setCursor(Utilities.createProgressCursor(this));

        Utilities.attachInitJob(comMode, new AsyncGUIJob() {

            String[] modes = null;

            @Override
            public void construct() {
                try {
                    FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(data.getProject());
                    FileObject foRoot = fs.getRoot().getFileObject("Windows2/Modes"); //NOI18N
                    if (foRoot != null) {
                        FileObject[] fos = foRoot.getChildren();
                        Collection<String> col = new ArrayList<String>();
                        for (FileObject fo : fos) {
                            if (fo.isData() && "wsmode".equals(fo.getExt())) { //NOI18N
                                col.add(fo.getName());
                            }
                        }
                        modes = col.toArray(new String[col.size()]);
                    } else {
                        modes = DEFAULT_MODES;
                    }
                } catch (IOException exc) {
                    modes = DEFAULT_MODES;

                }
            }

            @Override
            public void finished() {
                comMode.setModel(new DefaultComboBoxModel(modes));
                setComModeSelectedItem();
                windowPosChanged(null);
                setCursor(currentCursor);
                loadedComboBox = true;
                checkValidity();
            }
        });
    }
    
    protected void storeToDataModel() {
        data.setOpened(cbOpenedOnStart.isSelected());
        data.setKeepPrefSize(cbKeepPrefSize.isSelected());
        data.setClosingNotAllowed(cbClosingNotAllowed.isSelected());
        data.setDraggingNotAllowed(cbDraggingNotAllowed.isSelected());
        data.setMaximizationNotAllowed(cbMaximizationNotAllowed.isSelected());
        data.setSlidingNotAllowed(cbSlidingNotAllowed.isSelected());
        data.setUndockingNotAllowed(cbUndockingNotAllowed.isSelected());
        data.setMode((String)comMode.getSelectedItem());
    }
    
    protected void readFromDataModel() {
        cbOpenedOnStart.setSelected(data.isOpened());
        cbKeepPrefSize.setSelected(data.isKeepPrefSize());
        cbClosingNotAllowed.setSelected(data.isClosingNotAllowed());
        cbDraggingNotAllowed.setSelected(data.isDraggingNotAllowed());
        cbMaximizationNotAllowed.setSelected(data.isMaximizationNotAllowed());
        cbSlidingNotAllowed.setSelected(data.isSlidingNotAllowed());
        cbUndockingNotAllowed.setSelected(data.isUndockingNotAllowed());
        setComModeSelectedItem();
        windowPosChanged(null);
        checkValidity();
    }

    private void setComModeSelectedItem() {
        if (data.getMode() != null) {
            comMode.setSelectedItem(data.getMode());
        } else {
            comMode.setSelectedItem("output");//NOI18N
        }
    }
    
    protected String getPanelName() {
        return getMessage("LBL_BasicSettings_Title");
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(BasicSettingsPanel.class);
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(BasicSettingsPanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblMode = new javax.swing.JLabel();
        comMode = new javax.swing.JComboBox();
        cbOpenedOnStart = new javax.swing.JCheckBox();
        cbKeepPrefSize = new javax.swing.JCheckBox();
        cbSlidingNotAllowed = new javax.swing.JCheckBox();
        cbClosingNotAllowed = new javax.swing.JCheckBox();
        cbUndockingNotAllowed = new javax.swing.JCheckBox();
        cbDraggingNotAllowed = new javax.swing.JCheckBox();
        cbMaximizationNotAllowed = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        lblMode.setLabelFor(comMode);
        org.openide.awt.Mnemonics.setLocalizedText(lblMode, org.openide.util.NbBundle.getMessage(BasicSettingsPanel.class, "LBL_Mode")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(lblMode, gridBagConstraints);

        comMode.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                windowPosChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 6);
        add(comMode, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cbOpenedOnStart, org.openide.util.NbBundle.getMessage(BasicSettingsPanel.class, "LBL_OpenOnStart")); // NOI18N
        cbOpenedOnStart.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbOpenedOnStart.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(cbOpenedOnStart, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cbKeepPrefSize, org.openide.util.NbBundle.getMessage(BasicSettingsPanel.class, "LBL_KeepPrefSize")); // NOI18N
        cbKeepPrefSize.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbKeepPrefSize.setEnabled(false);
        cbKeepPrefSize.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(cbKeepPrefSize, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cbSlidingNotAllowed, org.openide.util.NbBundle.getMessage(BasicSettingsPanel.class, "CTL_SlidingNotAllowed")); // NOI18N
        cbSlidingNotAllowed.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbSlidingNotAllowed.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(cbSlidingNotAllowed, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cbClosingNotAllowed, org.openide.util.NbBundle.getMessage(BasicSettingsPanel.class, "CTL_ClosingNotAllowed")); // NOI18N
        cbClosingNotAllowed.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(cbClosingNotAllowed, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cbUndockingNotAllowed, org.openide.util.NbBundle.getMessage(BasicSettingsPanel.class, "CTL_UndockingNotAllowed")); // NOI18N
        cbUndockingNotAllowed.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(cbUndockingNotAllowed, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cbDraggingNotAllowed, org.openide.util.NbBundle.getMessage(BasicSettingsPanel.class, "CTL_DraggingNotAllowed")); // NOI18N
        cbDraggingNotAllowed.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(cbDraggingNotAllowed, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cbMaximizationNotAllowed, org.openide.util.NbBundle.getMessage(BasicSettingsPanel.class, "CTL_MaximizationNotAllowed")); // NOI18N
        cbMaximizationNotAllowed.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(cbMaximizationNotAllowed, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void windowPosChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_windowPosChanged
    cbKeepPrefSize.setEnabled( !("editor".equals( comMode.getSelectedItem()) ) );
    if( !cbKeepPrefSize.isEnabled() )
        cbKeepPrefSize.setSelected( false );
    cbSlidingNotAllowed.setEnabled( !("editor".equals( comMode.getSelectedItem()) ) );
    if( !cbSlidingNotAllowed.isEnabled() )
        cbSlidingNotAllowed.setSelected( false );
}//GEN-LAST:event_windowPosChanged
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbClosingNotAllowed;
    private javax.swing.JCheckBox cbDraggingNotAllowed;
    private javax.swing.JCheckBox cbKeepPrefSize;
    private javax.swing.JCheckBox cbMaximizationNotAllowed;
    private javax.swing.JCheckBox cbOpenedOnStart;
    private javax.swing.JCheckBox cbSlidingNotAllowed;
    private javax.swing.JCheckBox cbUndockingNotAllowed;
    private javax.swing.JComboBox comMode;
    private javax.swing.JLabel lblMode;
    // End of variables declaration//GEN-END:variables
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_BasicSettingsPanel"));
        cbOpenedOnStart.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_OpenOnStart"));
        cbKeepPrefSize.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_KeepPrefSize"));
        comMode.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_Mode"));
    }
    
}
