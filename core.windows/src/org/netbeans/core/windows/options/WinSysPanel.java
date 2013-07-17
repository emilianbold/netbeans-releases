/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.core.windows.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.core.windows.FloatingWindowTransparencyManager;
import org.netbeans.core.windows.nativeaccess.NativeWindowSystem;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.LifecycleManager;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

@OptionsPanelController.Keywords(keywords={"#KW_WindowOptions"}, location=OptionsDisplayer.ADVANCED, tabTitle="#AdvancedOption_DisplayName_WinSys")
public class WinSysPanel extends javax.swing.JPanel {

    protected final WinSysOptionsPanelController controller;
    
    private final Preferences prefs = NbPreferences.forModule(WinSysPanel.class);
    
    private final boolean isAquaLaF = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N

    private boolean defMultiRow;
    private int defTabPlacement;
    private int defaultLookAndFeelIndex;
    private final ArrayList<LookAndFeelInfo> lafs = new ArrayList<LookAndFeelInfo>( 10 );
    
    protected WinSysPanel(final WinSysOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
        // TODO listen to changes in form fields and call controller.changed()
        boolean isMacJDK17 = isMacJDK7();
        this.isDragImage.setEnabled(!isMacJDK17);
        this.isDragImageAlpha.setEnabled(!isMacJDK17);
        this.isAlphaFloating.setEnabled(!isMacJDK17);
        checkMaximizeNativeLaF.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                controller.changed();
            }
        });
        initLookAndFeel();
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for( LookAndFeelInfo li : lafs ) {
            model.addElement( li.getName() );
        }
        comboLaf.setModel( model );
        comboLaf.addItemListener( new ItemListener() {

            @Override
            public void itemStateChanged( ItemEvent e ) {
                controller.changed();
            }
        });
        initTabsPanel( panelTabs );
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        isDragImage = new javax.swing.JCheckBox();
        isAlphaFloating = new javax.swing.JCheckBox();
        isSnapping = new javax.swing.JCheckBox();
        isDragImageAlpha = new javax.swing.JCheckBox();
        isSnapScreenEdges = new javax.swing.JCheckBox();
        panelDocTabs = new javax.swing.JPanel();
        isCloseActivatesMostRecentDocument = new javax.swing.JCheckBox();
        isNewDocumentOpensNextToActiveTab = new javax.swing.JCheckBox();
        panelTabs = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        radioTop = new javax.swing.JRadioButton();
        radioBottom = new javax.swing.JRadioButton();
        radioLeft = new javax.swing.JRadioButton();
        radioRight = new javax.swing.JRadioButton();
        checkMultiRow = new javax.swing.JCheckBox();
        panelSeparator = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        panelSeparator1 = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        panelLaF = new javax.swing.JPanel();
        checkMaximizeNativeLaF = new javax.swing.JCheckBox();
        panelLaFCombo = new javax.swing.JPanel();
        comboLaf = new javax.swing.JComboBox();
        lblLaf = new javax.swing.JLabel();
        lblRestart = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(isDragImage, org.openide.util.NbBundle.getMessage(WinSysPanel.class, "LBL_DragWindowImage")); // NOI18N
        isDragImage.setToolTipText(org.openide.util.NbBundle.getMessage(WinSysPanel.class, "IsDragWindowTooltip")); // NOI18N
        isDragImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isDragImageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(isDragImage, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(isAlphaFloating, org.openide.util.NbBundle.getMessage(WinSysPanel.class, "LBL_TransparentFloatingWindows")); // NOI18N
        isAlphaFloating.setToolTipText(org.openide.util.NbBundle.getMessage(WinSysPanel.class, "IsAlphaFloatingTooltip")); // NOI18N
        isAlphaFloating.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isAlphaFloatingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        add(isAlphaFloating, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(isSnapping, org.openide.util.NbBundle.getMessage(WinSysPanel.class, "LBL_SnapFloatingWindows")); // NOI18N
        isSnapping.setToolTipText(org.openide.util.NbBundle.getMessage(WinSysPanel.class, "IsSnappingTooltip")); // NOI18N
        isSnapping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isSnappingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        add(isSnapping, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(isDragImageAlpha, org.openide.util.NbBundle.getMessage(WinSysPanel.class, "LBL_TransparentDragWindow")); // NOI18N
        isDragImageAlpha.setToolTipText(org.openide.util.NbBundle.getMessage(WinSysPanel.class, "IsAlphaDragTooltip")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(isDragImageAlpha, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(isSnapScreenEdges, org.openide.util.NbBundle.getMessage(WinSysPanel.class, "LBL_SnapToScreenEdges")); // NOI18N
        isSnapScreenEdges.setToolTipText(org.openide.util.NbBundle.getMessage(WinSysPanel.class, "IsSnapScreenEdgesTooltip")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        add(isSnapScreenEdges, gridBagConstraints);

        panelDocTabs.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(isCloseActivatesMostRecentDocument, org.openide.util.NbBundle.getMessage(WinSysPanel.class, "LBL_CloseActivatesRecentDocument")); // NOI18N
        isCloseActivatesMostRecentDocument.setToolTipText(org.openide.util.NbBundle.getMessage(WinSysPanel.class, "TIP_CloseActivatesMostRecentDocument")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        panelDocTabs.add(isCloseActivatesMostRecentDocument, gridBagConstraints);
        isCloseActivatesMostRecentDocument.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WinSysPanel.class, "WinSysPanel.isCloseActivatesMostRecentDocument.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(isNewDocumentOpensNextToActiveTab, NbBundle.getMessage(WinSysPanel.class, "WinSysPanel.isNewDocumentOpensNextToActiveTab.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panelDocTabs.add(isNewDocumentOpensNextToActiveTab, gridBagConstraints);

        panelTabs.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(WinSysPanel.class, "WinSysPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        panelTabs.add(jLabel1, gridBagConstraints);

        buttonGroup1.add(radioTop);
        radioTop.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(radioTop, NbBundle.getMessage(WinSysPanel.class, "WinSysPanel.radioTop.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        panelTabs.add(radioTop, gridBagConstraints);

        buttonGroup1.add(radioBottom);
        org.openide.awt.Mnemonics.setLocalizedText(radioBottom, NbBundle.getMessage(WinSysPanel.class, "WinSysPanel.radioBottom.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        panelTabs.add(radioBottom, gridBagConstraints);

        buttonGroup1.add(radioLeft);
        org.openide.awt.Mnemonics.setLocalizedText(radioLeft, NbBundle.getMessage(WinSysPanel.class, "WinSysPanel.radioLeft.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        panelTabs.add(radioLeft, gridBagConstraints);

        buttonGroup1.add(radioRight);
        org.openide.awt.Mnemonics.setLocalizedText(radioRight, NbBundle.getMessage(WinSysPanel.class, "WinSysPanel.radioRight.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        panelTabs.add(radioRight, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(checkMultiRow, NbBundle.getMessage(WinSysPanel.class, "WinSysPanel.checkMultiRow.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        panelTabs.add(checkMultiRow, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panelDocTabs.add(panelTabs, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(panelDocTabs, gridBagConstraints);

        panelSeparator.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.weightx = 1.0;
        panelSeparator.add(jSeparator1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(WinSysPanel.class, "WinSysPanel.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        panelSeparator.add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        add(panelSeparator, gridBagConstraints);

        panelSeparator1.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.weightx = 1.0;
        panelSeparator1.add(jSeparator2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, NbBundle.getMessage(WinSysPanel.class, "WinSysPanel.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        panelSeparator1.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        add(panelSeparator1, gridBagConstraints);

        panelLaF.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(checkMaximizeNativeLaF, org.openide.util.NbBundle.getMessage(WinSysPanel.class, "WinSysPanel.checkMaximizeNativeLaF.text")); // NOI18N
        checkMaximizeNativeLaF.setToolTipText(org.openide.util.NbBundle.getMessage(WinSysPanel.class, "WinSysPanel.checkMaximizeNativeLaF.toolTipText")); // NOI18N
        panelLaF.add(checkMaximizeNativeLaF, java.awt.BorderLayout.WEST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(panelLaF, gridBagConstraints);

        panelLaFCombo.setLayout(new java.awt.BorderLayout(3, 0));
        panelLaFCombo.add(comboLaf, java.awt.BorderLayout.CENTER);

        lblLaf.setLabelFor(comboLaf);
        org.openide.awt.Mnemonics.setLocalizedText(lblLaf, NbBundle.getMessage(WinSysPanel.class, "WinSysPanel.lblLaf.text")); // NOI18N
        panelLaFCombo.add(lblLaf, java.awt.BorderLayout.WEST);

        org.openide.awt.Mnemonics.setLocalizedText(lblRestart, NbBundle.getMessage(WinSysPanel.class, "WinSysPanel.lblRestart.text")); // NOI18N
        panelLaFCombo.add(lblRestart, java.awt.BorderLayout.LINE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(panelLaFCombo, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void isDragImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isDragImageActionPerformed
        updateDragSection();
        controller.changed();
}//GEN-LAST:event_isDragImageActionPerformed

private void isAlphaFloatingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isAlphaFloatingActionPerformed
    controller.changed();
}//GEN-LAST:event_isAlphaFloatingActionPerformed

private void isSnappingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isSnappingActionPerformed
    updateSnapSection();
    controller.changed();
}//GEN-LAST:event_isSnappingActionPerformed

    protected void load() {
        boolean isNotSolaris = Utilities.getOperatingSystem() != Utilities.OS_SOLARIS;
        boolean isMacJDK17 = isMacJDK7();
        isDragImage.setSelected(prefs.getBoolean(WinSysPrefs.DND_DRAGIMAGE, isNotSolaris && !isMacJDK17));
        isDragImageAlpha.setSelected(prefs.getBoolean(WinSysPrefs.TRANSPARENCY_DRAGIMAGE, isNotSolaris && !isMacJDK17));

        isAlphaFloating.setSelected(prefs.getBoolean(WinSysPrefs.TRANSPARENCY_FLOATING,!isMacJDK17));
        
        isSnapping.setSelected(prefs.getBoolean(WinSysPrefs.SNAPPING, true));
        isSnapScreenEdges.setSelected(prefs.getBoolean(WinSysPrefs.SNAPPING_SCREENEDGES, true));
        
        isCloseActivatesMostRecentDocument.setSelected(prefs.getBoolean(WinSysPrefs.EDITOR_CLOSE_ACTIVATES_RECENT, true));
        isNewDocumentOpensNextToActiveTab.setSelected(prefs.getBoolean(WinSysPrefs.OPEN_DOCUMENTS_NEXT_TO_ACTIVE_TAB, false));

        updateDragSection();
        updateSnapSection();
        updateFloatingSection();

        defMultiRow = prefs.getBoolean( WinSysPrefs.DOCUMENT_TABS_MULTIROW, false );
        checkMultiRow.setSelected( defMultiRow );
        defTabPlacement = prefs.getInt( WinSysPrefs.DOCUMENT_TABS_PLACEMENT, JTabbedPane.TOP );
        switch( defTabPlacement ) {
            case JTabbedPane.BOTTOM:
                radioBottom.setSelected( true );
                break;
            case JTabbedPane.LEFT:
                radioLeft.setSelected( true );
                break;
            case JTabbedPane.RIGHT:
                radioRight.setSelected( true );
                break;
            default:
                radioTop.setSelected( true );
        }
        
        checkMaximizeNativeLaF.setSelected(prefs.getBoolean(WinSysPrefs.MAXIMIZE_NATIVE_LAF, false));
        
        if( isAquaLaF ) {
            checkMultiRow.setSelected(false);
            checkMultiRow.setEnabled(false);
            radioLeft.setEnabled(false);
            radioRight.setEnabled(false);
            if( radioLeft.isSelected() || radioRight.isSelected() ) {
                radioTop.setSelected(true);
            }
        }

        boolean isForcedLaF = isForcedLaF();
        defaultLookAndFeelIndex = lafs.indexOf( isForcedLaF ? getCurrentLaF() : getPreferredLaF() );
        comboLaf.setSelectedIndex( defaultLookAndFeelIndex );
        comboLaf.setEnabled( !isForcedLaF );
    }

    protected boolean store() {
        prefs.putBoolean(WinSysPrefs.DND_DRAGIMAGE, isDragImage.isSelected());
        prefs.putBoolean(WinSysPrefs.TRANSPARENCY_DRAGIMAGE, isDragImageAlpha.isSelected());
        
        prefs.putBoolean(WinSysPrefs.TRANSPARENCY_FLOATING, isAlphaFloating.isSelected());
        FloatingWindowTransparencyManager.getDefault().update();
        
        prefs.putBoolean(WinSysPrefs.SNAPPING, isSnapping.isSelected());
        prefs.putBoolean(WinSysPrefs.SNAPPING_SCREENEDGES, isSnapScreenEdges.isSelected());
        
        prefs.putBoolean(WinSysPrefs.EDITOR_CLOSE_ACTIVATES_RECENT, isCloseActivatesMostRecentDocument.isSelected());
        prefs.putBoolean(WinSysPrefs.OPEN_DOCUMENTS_NEXT_TO_ACTIVE_TAB, isNewDocumentOpensNextToActiveTab.isSelected());
        
        prefs.putBoolean(WinSysPrefs.MAXIMIZE_NATIVE_LAF, checkMaximizeNativeLaF.isSelected());
        System.setProperty("nb.native.filechooser", checkMaximizeNativeLaF.isSelected() ? "true" : "false"); //NOI18N

        boolean needsWinsysRefresh = false;
        needsWinsysRefresh = checkMultiRow.isSelected() != defMultiRow;
        prefs.putBoolean(WinSysPrefs.DOCUMENT_TABS_MULTIROW, checkMultiRow.isSelected());

        int tabPlacement = JTabbedPane.TOP;
        if( radioBottom.isSelected() )
            tabPlacement = JTabbedPane.BOTTOM;
        else if( radioLeft.isSelected() )
            tabPlacement = JTabbedPane.LEFT;
        else if( radioRight.isSelected() )
            tabPlacement = JTabbedPane.RIGHT;
        prefs.putInt( WinSysPrefs.DOCUMENT_TABS_PLACEMENT, tabPlacement );
        needsWinsysRefresh |= tabPlacement != defTabPlacement;

        int selLaFIndex = comboLaf.getSelectedIndex();
        if( selLaFIndex != defaultLookAndFeelIndex && !isForcedLaF() ) {
            LookAndFeelInfo li = lafs.get( comboLaf.getSelectedIndex() );
            NbPreferences.root().node( "laf" ).put( "laf", li.getClassName() ); //NOI18N
            NbPreferences.root().node( "laf" ).putBoolean( "theme.dark", li == DARK_METAL || li == DARK_NIMBUS ); //NOI18N
            askForRestart();
        }

        return needsWinsysRefresh;
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }

    protected void initTabsPanel( JPanel panel ) {

    }
    
    private void updateDragSection () {
        boolean isAlpha = NativeWindowSystem.getDefault().isWindowAlphaSupported();
        boolean isDrag = isDragImage.isSelected();

        isDragImageAlpha.setEnabled(isAlpha && isDrag);

        if (isAlpha) {
            isDragImageAlpha.setToolTipText(
                    NbBundle.getMessage(WinSysPanel.class, "IsAlphaDragTooltip")); // NOI18N
        } else {
            isDragImageAlpha.setToolTipText(
                    NbBundle.getMessage(WinSysPanel.class, "NoAlphaSupport")); // NOI18N
        }
    }
    
    private void updateSnapSection () {
        isSnapScreenEdges.setEnabled(isSnapping.isSelected());
    }
    
    private void updateFloatingSection () {
        boolean isAlpha = NativeWindowSystem.getDefault().isWindowAlphaSupported();

        isAlphaFloating.setEnabled(isAlpha);

        if (isAlpha) {
            isAlphaFloating.setToolTipText(
                    NbBundle.getMessage(WinSysPanel.class, "IsAlphaFloatingTooltip")); // NOI18N
        } else {
            isAlphaFloating.setToolTipText(
                    NbBundle.getMessage(WinSysPanel.class, "NoAlphaSupport")); // NOI18N
        }
    }
    
    private static boolean isMacJDK7() {
        if( Utilities.isMac() ) {
            String version = System.getProperty("java.version"); //NOI18N
            if( null != version && version.startsWith("1.7" ) ) //NOI18N
                return true;
        }
        return false;
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox checkMaximizeNativeLaF;
    private javax.swing.JCheckBox checkMultiRow;
    private javax.swing.JComboBox comboLaf;
    private javax.swing.JCheckBox isAlphaFloating;
    private javax.swing.JCheckBox isCloseActivatesMostRecentDocument;
    private javax.swing.JCheckBox isDragImage;
    private javax.swing.JCheckBox isDragImageAlpha;
    private javax.swing.JCheckBox isNewDocumentOpensNextToActiveTab;
    private javax.swing.JCheckBox isSnapScreenEdges;
    private javax.swing.JCheckBox isSnapping;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblLaf;
    private javax.swing.JLabel lblRestart;
    private javax.swing.JPanel panelDocTabs;
    private javax.swing.JPanel panelLaF;
    private javax.swing.JPanel panelLaFCombo;
    private javax.swing.JPanel panelSeparator;
    private javax.swing.JPanel panelSeparator1;
    private javax.swing.JPanel panelTabs;
    private javax.swing.JRadioButton radioBottom;
    private javax.swing.JRadioButton radioLeft;
    private javax.swing.JRadioButton radioRight;
    private javax.swing.JRadioButton radioTop;
    // End of variables declaration//GEN-END:variables


    private void initLookAndFeel() {
        lafs.clear();
        for( LookAndFeelInfo i : UIManager.getInstalledLookAndFeels() ) {
            lafs.add( i );
            if( MetalLookAndFeel.class.getName().equals( i.getClassName() ) ) {
                lafs.add( DARK_METAL );
            } else if( "Nimbus".equals( i.getName() ) ) { //NOI18N
                lafs.add( DARK_NIMBUS );
            }
        }
    }

    private boolean isForcedLaF() {
        return null != System.getProperty( "nb.laf.forced" ); //NOI18N
    }

    private LookAndFeelInfo getCurrentLaF() {
        boolean darkTheme = Boolean.getBoolean("netbeans.plaf.dark.theme"); //NOI18N
        LookAndFeelInfo currentLaf = null;
        String currentLAFClassName = UIManager.getLookAndFeel().getClass().getName();
        boolean isAqua = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N
        for( LookAndFeelInfo li : lafs ) {
            if( currentLAFClassName.equals( li.getClassName() ) 
                    || (isAqua && li.getClassName().contains("apple.laf.AquaLookAndFeel")) ) { //NOI18N
                currentLaf = li;
                if( darkTheme ) {
                    if( MetalLookAndFeel.class.getName().equals( currentLAFClassName ) ) {
                        currentLaf = DARK_METAL;
                    } else if( "Nimbus".equals( UIManager.getLookAndFeel().getID() ) ) { //NOI18N
                        currentLaf = DARK_NIMBUS;
                    }
                }
                break;
            }
        }
        return currentLaf;
    }

    private LookAndFeelInfo getPreferredLaF() {
        String lafClassName = NbPreferences.root().node( "laf" ).get( "laf", null ); //NOI18N
        if( null == lafClassName )
            return getCurrentLaF();
        LookAndFeelInfo currentLaf = null;
        boolean darkTheme = NbPreferences.root().node( "laf" ).getBoolean( "theme.dark", false ); //NOI18N
        boolean isAqua = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N
        for( LookAndFeelInfo li : lafs ) {
            if( lafClassName.equals( li.getClassName() )
                    || (isAqua && li.getClassName().contains("apple.laf.AquaLookAndFeel")) ) { //NOI18N
                currentLaf = li;
                if( darkTheme ) {
                    if( MetalLookAndFeel.class.getName().equals( lafClassName ) ) {
                        currentLaf = DARK_METAL;
                    } else if( NimbusLookAndFeel.class.getName().equals( lafClassName ) ) {
                        currentLaf = DARK_NIMBUS;
                    }
                }
                break;
            }
        }
        if( null == currentLaf
                && com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel.class.getName().equals( lafClassName )
                && darkTheme ) {
            currentLaf = DARK_NIMBUS;
    }
        return currentLaf;
    }

    static final LookAndFeelInfo DARK_METAL = new UIManager.LookAndFeelInfo( NbBundle.getMessage(WinSysPanel.class, "Laf_DARK_METAL"), MetalLookAndFeel.class.getName() );
    static final LookAndFeelInfo DARK_NIMBUS = new UIManager.LookAndFeelInfo( NbBundle.getMessage(WinSysPanel.class, "Laf_DARK_NIMBUS"), NimbusLookAndFeel.class.getName() );

    private static boolean askedAlready = false;
    private void askForRestart() {
        if( askedAlready )
            return;
        askedAlready = true;
        NotificationDisplayer.getDefault().notify( NbBundle.getMessage(WinSysPanel.class, "Hint_RESTART_IDE"),
                ImageUtilities.loadImageIcon( "org/netbeans/core/windows/resources/restart.png", true ), //NOI18N
                NbBundle.getMessage(WinSysPanel.class, "Descr_Restart"), new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                LifecycleManager.getDefault().markForRestart();
                LifecycleManager.getDefault().exit();
            }
        }, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    void selectDarkLookAndFeel() {
        comboLaf.setSelectedItem( DARK_METAL.getName() );
        comboLaf.requestFocusInWindow();
        SwingUtilities.invokeLater( new Runnable() {

            @Override
            public void run() {
                comboLaf.setPopupVisible( true );
            }
        });
    }
}
