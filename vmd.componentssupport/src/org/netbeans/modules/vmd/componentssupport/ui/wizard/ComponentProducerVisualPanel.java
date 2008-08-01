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

package org.netbeans.modules.vmd.componentssupport.ui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.vmd.componentssupport.ui.IconUtils;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.CustomComponentHelper;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


final class ComponentProducerVisualPanel extends JPanel {
    
    private static final String MSG_ERR_CLASS_NAME_EMPTY 
                                              = "MSG_CP_EmptyClassName";        // NOI18N 
    private static final String MSG_ERR_CLASS_NAME_INVALID 
                                              = "MSG_CP_InvalidClassName";      // NOI18N 
    private static final String MSG_ERR_CLASS_NAME_EXISTS 
                                              = "MSG_CP_ExistingClassName";          // NOI18N 
    private static final String MSG_ERR_PALETTE_DISP_NAME_EMPTY 
                                              = "MSG_CP_EmptyPaletteDispName";  // NOI18N 
    private static final String MSG_ERR_LIB_NAME_EMPTY 
                                              = "MSG_CP_EmptyLibName";          // NOI18N 
    private static final String MSG_ERR_SMALL_NOT_EXIST 
                                              = "MSG_CP_SmallIconNotExist";          // NOI18N 
    private static final String MSG_ERR_LARGE_NOT_EXIST 
                                              = "MSG_CP_LargeIconNotExist";          // NOI18N 
    private static final String LBL_SELECT    = "LBL_Select";                   // NOI18N 
    private static final String TXT_NONE    = "TXT_NONE";                   // NOI18N 
    
    private static final String NONE    = getMessage(TXT_NONE);
    private static final int ICON_LARGE_W = 32;
    private static final int ICON_LARGE_H = 32;
    private static final int ICON_SMALL_W = 16;
    private static final int ICON_SMALL_H = 16;

    /** Creates new NameAndLocationPanel */
    ComponentProducerVisualPanel(ComponentProducerWizardPanel panel) {
        myPanel = panel;
        initComponents();
        
        myCPPaletteCategoryCombo.setModel(PaletteCategory.getComboBoxModel());
        myCPValidAlwaysRadio.setSelected(true);
        
        myDocListener = new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                checkValidity();
            }
        };
        myClassNameListener = new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                isCPClassNameUpdated = true;
                checkValidity();
            }
        };
        mySmallIconPathListener = new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                isSmallIconUpdated = true;
                checkValidity();
            }
        };
        myLargeIconPathListener = new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                isLargeIconUpdated = true;
                checkValidity();
            }
        };
        myAddLibDependencyListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkValidity();
            }
        };
        
    }
    
    public @Override void addNotify() {
        super.addNotify();
        attachDocumentListeners();
        checkValidity();
    }
    
    public @Override void removeNotify() {
        // prevent checking when the panel is not "active"
        removeDocumentListeners();
        super.removeNotify();
    }

    void storeData(WizardDescriptor descriptor) {
        descriptor.putProperty(NewComponentDescriptor.CP_CLASS_NAME, 
                getClassNameValue());
        descriptor.putProperty(NewComponentDescriptor.CP_PALETTE_DISP_NAME, 
                getPaletteDispNameValue());
        descriptor.putProperty(NewComponentDescriptor.CP_PALETTE_TIP, 
                getPaletteTooltipValue());
        descriptor.putProperty(NewComponentDescriptor.CP_PALETTE_CATEGORY, 
                getPaletteCategoryValue());
        descriptor.putProperty(NewComponentDescriptor.CP_SMALL_ICON, 
                getSmallIconValue());
        descriptor.putProperty(NewComponentDescriptor.CP_LARGE_ICON, 
                getLargeIconValue());
        descriptor.putProperty(NewComponentDescriptor.CP_ADD_LIB, 
                getAddLibraryValue());
        descriptor.putProperty(NewComponentDescriptor.CP_LIB_NAME, 
                getLibraryNameValue());
        descriptor.putProperty(NewComponentDescriptor.CP_VALID_ALWAYS, 
                getValidAlwaysValue());
        descriptor.putProperty(NewComponentDescriptor.CP_VALID_PLATFORM, 
                getValidPlatformValue());
        descriptor.putProperty(NewComponentDescriptor.CP_VALID_CUSTOM, 
                getValidCustomValue());
    }
    
    void readData( WizardDescriptor descriptor) {
        mySettings = descriptor;
        
        readClassNameValue();
        myCPPaletteDispName.setText(getPaletteDispName());
        myCPPaletteTooltip.setText(getPaletteTooltip());
        myCPPaletteCategoryCombo.setSelectedItem(getPaletteCategory());
        myCPSmallIconPath.setText(getSmallIcon());
        myCPLargeIconPath.setText(getLargeIcon());
        myCPAddLibDependencyChk.setSelected(getAddLib());
        myCPLibName.setText((String)mySettings.getProperty(
                NewComponentDescriptor.CP_LIB_NAME));
        if (getValidAlways() != null){
            myCPValidAlwaysRadio.setSelected(getValidAlways());
        }
        if (getValidPlatform() != null){
            myCPValidPlatformRadio.setSelected(getValidPlatform());
        }
        if (getValidCustom() != null){
            myCPValidCustomRadio.setSelected(getValidCustom());
        }
        
        checkValidity();
    }

    private CustomComponentHelper getHelper(){
        return (CustomComponentHelper)mySettings.getProperty( 
                NewComponentDescriptor.HELPER);
    }
    
    private String getPaletteDispName() {
        String value = (String) mySettings.getProperty(
                NewComponentDescriptor.CP_PALETTE_DISP_NAME);
        return (value == null) ? getPrefix() : value;
    }
    private String getPaletteTooltip() {
        String value = (String) mySettings.getProperty(
                NewComponentDescriptor.CP_PALETTE_TIP);
        return (value == null) ? getPrefix() : value;
    }

    private PaletteCategory getPaletteCategory(){
        Object value = mySettings.getProperty(
                NewComponentDescriptor.CP_PALETTE_CATEGORY);
        if (value == null || !(value instanceof PaletteCategory)){
            return PaletteCategory.CATEGORY_DISPLAYABLES;
        }
        return (PaletteCategory)value;
    }

    private String getPrefix(){
        String prefix = (String) mySettings.getProperty(
                NewComponentDescriptor.CC_PREFIX);
        assert prefix != null;
        return prefix;
    }
    
    private void readClassNameValue() {
        String name = (String) mySettings.getProperty(
                NewComponentDescriptor.CP_CLASS_NAME);
        if (name == null || !isCPClassNameUpdated){
            name = NewComponentDescriptor.createDefaultProducerClass(getPrefix());
            myCPClassName.setText(name);
            isCPClassNameUpdated = false;
        } else {
            myCPClassName.setText(name);
        }
    }

    private Boolean getAddLib() {
        Object value = mySettings.getProperty(
                NewComponentDescriptor.CP_ADD_LIB);
        if (value == null) {
            return Boolean.FALSE;
        }
        return (Boolean)value;
    }

    private String getSmallIcon(){
        String value = (String)mySettings.getProperty(
                NewComponentDescriptor.CP_SMALL_ICON);
        return (value == null || value.length() == 0) ? NONE : value;
    }

    private String getLargeIcon(){
        String value = (String)mySettings.getProperty(
                NewComponentDescriptor.CP_LARGE_ICON);
        return (value == null || value.length() == 0) ? NONE : value;
    }

    private Boolean getValidAlways() {
        return (Boolean)mySettings.getProperty(
                NewComponentDescriptor.CP_VALID_ALWAYS);
    }

    private Boolean getValidPlatform() {
        return (Boolean)mySettings.getProperty(
                NewComponentDescriptor.CP_VALID_PLATFORM);
    }

    private Boolean getValidCustom() {
        return (Boolean)mySettings.getProperty(
                NewComponentDescriptor.CP_VALID_CUSTOM);
    }
    
    private boolean checkValidity(){
        if (!isCPClassNameValid()){
            return false;
        }
        if (!isCPPaletteDispNameValid()){
            return false;
        }
        if (!isCPSmallIconValid()){
            return false;
        }
        if (!isCPLargeIconValid()){
            return false;
        }
        if (!isCPLibNameValid()){
            return false;
        }
        markValid();
        return true;
    }
    
    private boolean isCPSmallIconValid(){
        String path = getSmallIconValue();
        if (path.length() == 0) {
            setWarning(IconUtils.getNoIconMessage(ICON_SMALL_W, ICON_SMALL_H));
        } else if (!isFileExist(path)){
            setError(getMessage(MSG_ERR_SMALL_NOT_EXIST));
            return false;
        } else if (!IconUtils.isValidIcon(new File(path),ICON_SMALL_W, ICON_SMALL_H)) {
            setWarning(IconUtils.getIconDimensionMessage(
                    new File(path), ICON_SMALL_W, ICON_SMALL_H));
        }
        return true;
    }
    
    private boolean isCPLargeIconValid(){
        String path = getLargeIconValue();
        if (path.length() == 0) {
            setWarning(IconUtils.getNoIconMessage(ICON_LARGE_W, ICON_LARGE_H));
        } else if (!isFileExist(path)){
            setError(getMessage(MSG_ERR_LARGE_NOT_EXIST));
            return false;
        } else if (!IconUtils.isValidIcon(new File(path), ICON_LARGE_W, ICON_LARGE_H)) {
            setWarning(IconUtils.getIconDimensionMessage(
                    new File(path), ICON_LARGE_W, ICON_LARGE_H));
        }
        return true;
    }

    private static boolean isFileExist(String path){
        File file = new File(path);
        return file.exists();
    }
    
    // TODO add unique validation
    private boolean isCPClassNameValid(){
        String name = getClassNameValue();
        if (name.length() == 0) {
            setError(getMessage(MSG_ERR_CLASS_NAME_EMPTY));
            return false;
        } else if (!Utilities.isJavaIdentifier(name)){
            setError(getMessage(MSG_ERR_CLASS_NAME_INVALID));
            return false;
        } else if (getHelper().isProducerClassNameExist(name)){
            setError(getMessage(MSG_ERR_CLASS_NAME_EXISTS));
            return false;
        }
        return true;
    }

    private boolean isCPPaletteDispNameValid(){
        String name = getPaletteDispNameValue();
        if (name.length() == 0) {
            setError(getMessage(MSG_ERR_PALETTE_DISP_NAME_EMPTY));
            return false;
        }
        return true;
    }

    private boolean isCPLibNameValid(){
        if (!getAddLibraryValue()){
            return true;
        }
        String name = getLibraryNameValue();
        if (name.length() == 0) {
            setError(getMessage(MSG_ERR_LIB_NAME_EMPTY));
            return false;
        }
        return true;
    }

    /**
     * Set an error message and mark the panel as invalid.
     */
    protected final void setError(String message) {
        assert message != null;
        setMessage(message);
        setValid(false);
    }

    /**
     * Set an warning message but mark the panel as valid.
     */
    protected final void setWarning(String message) {
        assert message != null;
        setMessage(message);
        setValid(true);
    }

    /**
     * Mark the panel as valid and clear any error or warning message.
     */
    protected final void markValid() {
        setMessage(null);
        setValid(true);
    }
    
    private final void setMessage(String message) {
        mySettings.putProperty(
                CustomComponentWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, 
                message);
    }

    private final void setValid(boolean valid) {
        myPanel.setValid(valid);
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(ComponentProducerVisualPanel.class);
    }
    
    private static String getMessage(String key, Object... args) {
        return NbBundle.getMessage(ComponentProducerVisualPanel.class, key, args);
    }
    
    private String getClassNameValue(){
        return myCPClassName.getText().trim();
    }
    
    private String getPaletteDispNameValue(){
        return myCPPaletteDispName.getText().trim();
    }
    
    private String getPaletteTooltipValue(){
        return myCPPaletteTooltip.getText().trim();
    }
    
    private PaletteCategory getPaletteCategoryValue(){
        return (PaletteCategory)myCPPaletteCategoryCombo.getSelectedItem();
    }
    
    private String getSmallIconValue(){
        String value = myCPSmallIconPath.getText().trim();
        return (value.equals(NONE)) ? "" : value;
    }
    
    private String getLargeIconValue(){
        String value = myCPLargeIconPath.getText().trim();
        return (value.equals(NONE)) ? "" : value;
    }
    
    private Boolean getAddLibraryValue(){
        return myCPAddLibDependencyChk.isSelected();
    }
    
    private String getLibraryNameValue(){
        return myCPLibName.getText().trim();
    }
    
    private Boolean getValidAlwaysValue(){
        return myCPValidAlwaysRadio.isSelected();
    }
    
    private Boolean getValidPlatformValue(){
        return myCPValidPlatformRadio.isSelected();
    }
    
    private Boolean getValidCustomValue(){
        return myCPValidCustomRadio.isSelected();
    }
    
    private void attachDocumentListeners() {
        if (!listenersAttached) {
            myCPClassName.getDocument().addDocumentListener(myClassNameListener);
            myCPPaletteDispName.getDocument().addDocumentListener(myDocListener);
            myCPPaletteTooltip.getDocument().addDocumentListener(myDocListener);
            myCPSmallIconPath.getDocument().addDocumentListener(mySmallIconPathListener);
            myCPLargeIconPath.getDocument().addDocumentListener(myLargeIconPathListener);
            myCPLibName.getDocument().addDocumentListener(myDocListener);
            myCPAddLibDependencyChk.addActionListener(myAddLibDependencyListener);
            listenersAttached = true;
        }
    }

    private void removeDocumentListeners() {
        if (listenersAttached) {
            myCPClassName.getDocument().removeDocumentListener(myClassNameListener);
            myCPPaletteDispName.getDocument().removeDocumentListener(myDocListener);
            myCPPaletteTooltip.getDocument().removeDocumentListener(myDocListener);
            myCPSmallIconPath.getDocument().removeDocumentListener(mySmallIconPathListener);
            myCPLargeIconPath.getDocument().removeDocumentListener(myLargeIconPathListener);
            myCPLibName.getDocument().removeDocumentListener(myDocListener);
            myCPAddLibDependencyChk.removeActionListener(myAddLibDependencyListener);
            listenersAttached = false;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        myValidButtonGroup = new javax.swing.ButtonGroup();
        myCompProducerPanel = new javax.swing.JPanel();
        myCPClassNameLabel = new javax.swing.JLabel();
        myCPClassName = new javax.swing.JTextField();
        myCPPaletteDispNameLabel = new javax.swing.JLabel();
        myCPPaletteDispName = new javax.swing.JTextField();
        myCPPaletteTooltipLabel = new javax.swing.JLabel();
        myCPPaletteTooltip = new javax.swing.JTextField();
        myCPPaletteCategoryLabel = new javax.swing.JLabel();
        myCPPaletteCategoryCombo = new javax.swing.JComboBox();
        myCPSmallIconPathLabel = new javax.swing.JLabel();
        myCPSmallIconPath = new javax.swing.JTextField();
        myCPSmallIconPathButton = new javax.swing.JButton();
        myCPLargeIconPathLabel = new javax.swing.JLabel();
        myCPLargeIconPath = new javax.swing.JTextField();
        myCPLargeIconPathButton = new javax.swing.JButton();
        myCPAddLibDependencyChk = new javax.swing.JCheckBox();
        myLibNamePanel = new javax.swing.JPanel();
        myCPLibNameLabel = new javax.swing.JLabel();
        myCPLibName = new javax.swing.JTextField();
        myValidityPanel = new javax.swing.JPanel();
        myCPValidAlwaysRadio = new javax.swing.JRadioButton();
        myCPValidPlatformRadio = new javax.swing.JRadioButton();
        myCPValidCustomRadio = new javax.swing.JRadioButton();

        myCompProducerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "LBL_CP_ComponentProducer"))); // NOI18N

        myCPClassNameLabel.setLabelFor(myCPClassName);
        org.openide.awt.Mnemonics.setLocalizedText(myCPClassNameLabel, org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "LBL_CP_ClassNameLabel")); // NOI18N

        myCPClassName.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_ClassNameLabel")); // NOI18N

        myCPPaletteDispNameLabel.setLabelFor(myCPPaletteDispName);
        org.openide.awt.Mnemonics.setLocalizedText(myCPPaletteDispNameLabel, org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "LBL_CP_PaletteDisplayName")); // NOI18N

        myCPPaletteDispName.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_PaletteDisplayName")); // NOI18N

        myCPPaletteTooltipLabel.setLabelFor(myCPPaletteTooltip);
        org.openide.awt.Mnemonics.setLocalizedText(myCPPaletteTooltipLabel, org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "LBL_CP_PaletteTooltip")); // NOI18N

        myCPPaletteTooltip.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_PaletteTooltip")); // NOI18N

        myCPPaletteCategoryLabel.setLabelFor(myCPPaletteCategoryCombo);
        org.openide.awt.Mnemonics.setLocalizedText(myCPPaletteCategoryLabel, org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "LBL_CP_PaletteCategory")); // NOI18N

        myCPPaletteCategoryCombo.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_PaletteCategory")); // NOI18N

        myCPSmallIconPathLabel.setLabelFor(myCPSmallIconPath);
        org.openide.awt.Mnemonics.setLocalizedText(myCPSmallIconPathLabel, org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "LBL_CP_SmallIconPath")); // NOI18N

        myCPSmallIconPath.setText(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "TXT_NONE")); // NOI18N
        myCPSmallIconPath.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_SmallIconPath")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myCPSmallIconPathButton, org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "LBL_CP_SmallIconButton")); // NOI18N
        myCPSmallIconPathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myCPSmallIconPathButtonActionPerformed(evt);
            }
        });

        myCPLargeIconPathLabel.setLabelFor(myCPLargeIconPath);
        org.openide.awt.Mnemonics.setLocalizedText(myCPLargeIconPathLabel, org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "LBL_CP_LargeIconPath")); // NOI18N

        myCPLargeIconPath.setText(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "TXT_NONE")); // NOI18N
        myCPLargeIconPath.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_LargeIconPath")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myCPLargeIconPathButton, org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "LBL_CP_LargeIconButton")); // NOI18N
        myCPLargeIconPathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myCPLargeIconPathButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(myCPAddLibDependencyChk, org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "LBL_CP_AddLibraryChk")); // NOI18N

        myCPLibNameLabel.setLabelFor(myCPLibName);
        org.openide.awt.Mnemonics.setLocalizedText(myCPLibNameLabel, org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "LBL_CP_LibName")); // NOI18N

        myCPLibName.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_LibName")); // NOI18N

        org.jdesktop.layout.GroupLayout myLibNamePanelLayout = new org.jdesktop.layout.GroupLayout(myLibNamePanel);
        myLibNamePanel.setLayout(myLibNamePanelLayout);
        myLibNamePanelLayout.setHorizontalGroup(
            myLibNamePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myLibNamePanelLayout.createSequentialGroup()
                .add(40, 40, 40)
                .add(myCPLibNameLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(myCPLibName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE))
        );
        myLibNamePanelLayout.setVerticalGroup(
            myLibNamePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myLibNamePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(myCPLibNameLabel)
                .add(myCPLibName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        myCPLibNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSN_CP_LibName")); // NOI18N
        myCPLibNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_LibName")); // NOI18N

        myValidityPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "LBL_CP_Validity"))); // NOI18N

        myValidButtonGroup.add(myCPValidAlwaysRadio);
        org.openide.awt.Mnemonics.setLocalizedText(myCPValidAlwaysRadio, org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "LBL_CP_ValidAlways")); // NOI18N
        myCPValidAlwaysRadio.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_ValidAlways")); // NOI18N

        myValidButtonGroup.add(myCPValidPlatformRadio);
        org.openide.awt.Mnemonics.setLocalizedText(myCPValidPlatformRadio, org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "LBL_CP_ValidPlatform")); // NOI18N
        myCPValidPlatformRadio.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_ValidPlatform")); // NOI18N

        myValidButtonGroup.add(myCPValidCustomRadio);
        org.openide.awt.Mnemonics.setLocalizedText(myCPValidCustomRadio, org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "LBL_CP_ValidCustom")); // NOI18N
        myCPValidCustomRadio.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_ValidCustom")); // NOI18N

        org.jdesktop.layout.GroupLayout myValidityPanelLayout = new org.jdesktop.layout.GroupLayout(myValidityPanel);
        myValidityPanel.setLayout(myValidityPanelLayout);
        myValidityPanelLayout.setHorizontalGroup(
            myValidityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myValidityPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(myValidityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myCPValidAlwaysRadio)
                    .add(myCPValidPlatformRadio)
                    .add(myCPValidCustomRadio))
                .addContainerGap(241, Short.MAX_VALUE))
        );
        myValidityPanelLayout.setVerticalGroup(
            myValidityPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myValidityPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(myCPValidAlwaysRadio)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myCPValidPlatformRadio)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myCPValidCustomRadio)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        myCPValidAlwaysRadio.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSN_CP_ValidAlways")); // NOI18N
        myCPValidAlwaysRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_ValidAlways")); // NOI18N
        myCPValidPlatformRadio.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSN_CP_ValidPlatform")); // NOI18N
        myCPValidPlatformRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_ValidPlatform")); // NOI18N
        myCPValidCustomRadio.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSN_CP_ValidCustom")); // NOI18N
        myCPValidCustomRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_ValidCustom")); // NOI18N

        org.jdesktop.layout.GroupLayout myCompProducerPanelLayout = new org.jdesktop.layout.GroupLayout(myCompProducerPanel);
        myCompProducerPanel.setLayout(myCompProducerPanelLayout);
        myCompProducerPanelLayout.setHorizontalGroup(
            myCompProducerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myCompProducerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(myCompProducerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myValidityPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(myCompProducerPanelLayout.createSequentialGroup()
                        .add(myCompProducerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, myCPAddLibDependencyChk)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, myLibNamePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, myCompProducerPanelLayout.createSequentialGroup()
                                .add(myCompProducerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(myCPPaletteDispNameLabel)
                                    .add(myCPPaletteTooltipLabel)
                                    .add(myCPPaletteCategoryLabel)
                                    .add(myCPSmallIconPathLabel)
                                    .add(myCPLargeIconPathLabel)
                                    .add(myCPClassNameLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(myCompProducerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(myCPClassName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                                    .add(myCPPaletteTooltip, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                                    .add(myCPPaletteDispName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                                    .add(myCPPaletteCategoryCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 182, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, myCompProducerPanelLayout.createSequentialGroup()
                                        .add(myCompProducerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                            .add(myCPLargeIconPath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                                            .add(myCPSmallIconPath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(myCompProducerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, myCPSmallIconPathButton)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, myCPLargeIconPathButton))))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addContainerGap())
        );
        myCompProducerPanelLayout.setVerticalGroup(
            myCompProducerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myCompProducerPanelLayout.createSequentialGroup()
                .add(myCompProducerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myCPClassNameLabel)
                    .add(myCPClassName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myCompProducerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myCPPaletteDispNameLabel)
                    .add(myCPPaletteDispName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myCompProducerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myCPPaletteTooltipLabel)
                    .add(myCPPaletteTooltip, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myCompProducerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myCPPaletteCategoryLabel)
                    .add(myCPPaletteCategoryCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myCompProducerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myCPSmallIconPathLabel)
                    .add(myCPSmallIconPathButton)
                    .add(myCPSmallIconPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myCompProducerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myCPLargeIconPathLabel)
                    .add(myCPLargeIconPathButton)
                    .add(myCPLargeIconPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myCPAddLibDependencyChk)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myLibNamePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myValidityPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        myCPClassNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSN_CP_ClassNameLabel")); // NOI18N
        myCPClassNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_ClassNameLabel")); // NOI18N
        myCPPaletteDispNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSN_CP_PaletteDisplayName")); // NOI18N
        myCPPaletteDispNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_PaletteDisplayName")); // NOI18N
        myCPPaletteTooltipLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSN_CP_PaletteTooltip")); // NOI18N
        myCPPaletteTooltipLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_PaletteTooltip")); // NOI18N
        myCPPaletteCategoryLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSN_CP_PaletteCategory")); // NOI18N
        myCPPaletteCategoryLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_PaletteCategory")); // NOI18N
        myCPSmallIconPathLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSN_CP_SmallIconPath")); // NOI18N
        myCPSmallIconPathLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_SmallIconPath")); // NOI18N
        myCPSmallIconPathButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSN_CP_SmallIconButton")); // NOI18N
        myCPSmallIconPathButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_SmallIconButton")); // NOI18N
        myCPLargeIconPathLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSN_CP_LargeIconPath")); // NOI18N
        myCPLargeIconPathLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_LargeIconPath")); // NOI18N
        myCPLargeIconPathButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSN_CP_LargeIconButton")); // NOI18N
        myCPLargeIconPathButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_LargeIconButton")); // NOI18N
        myCPAddLibDependencyChk.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSN_CP_AddLibraryChk")); // NOI18N
        myCPAddLibDependencyChk.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_AddLibraryChk")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myCompProducerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myCompProducerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSN_CP_ComponentProducerPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentProducerVisualPanel.class, "ACSD_CP_ComponentProducerPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void myCPSmallIconPathButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myCPSmallIconPathButtonActionPerformed
    JFileChooser chooser = IconUtils.getIconFileChooser(myCPSmallIconPath.getText());
    int ret = chooser.showDialog(this, getMessage(LBL_SELECT));
    if (ret == JFileChooser.APPROVE_OPTION) {//GEN-HEADEREND:event_myCPSmallIconPathButtonActionPerformed
        File iconFile = chooser.getSelectedFile();

        File secondIcon = getAnotherIconPath(iconFile);
        if (secondIcon != null) {
            boolean isIconSmall = IconUtils.isValidIcon(iconFile, 
                                                    ICON_SMALL_W, ICON_SMALL_H);
            String small = (isIconSmall) ? iconFile.getAbsolutePath() 
                    : secondIcon.getAbsolutePath();
            String large = (isIconSmall) ? secondIcon.getAbsolutePath() 
                    : iconFile.getAbsolutePath();
            
            myCPSmallIconPath.setText(small);
            if (!isLargeIconUpdated) {
                myCPLargeIconPath.setText(large);
                isLargeIconUpdated = false;
            }
        } else {
            myCPSmallIconPath.setText(iconFile.getAbsolutePath());
        }
    }


}                                                       

private void myCPLargeIconPathButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-LAST:event_myCPSmallIconPathButtonActionPerformed
    JFileChooser chooser = IconUtils.getIconFileChooser(myCPLargeIconPath.getText());//GEN-FIRST:event_myCPLargeIconPathButtonActionPerformed
    int ret = chooser.showDialog(this, getMessage(LBL_SELECT));
    if (ret == JFileChooser.APPROVE_OPTION) {//GEN-HEADEREND:event_myCPLargeIconPathButtonActionPerformed
        File iconFile = chooser.getSelectedFile();

        File secondIcon = getAnotherIconPath(iconFile);
        if (secondIcon != null) {
            boolean isIconSmall = IconUtils.isValidIcon(iconFile, 
                                                    ICON_SMALL_W, ICON_SMALL_H);
            String small = (isIconSmall) ? iconFile.getAbsolutePath() 
                    : secondIcon.getAbsolutePath();
            String large = (isIconSmall) ? secondIcon.getAbsolutePath() 
                    : iconFile.getAbsolutePath();
            
            myCPLargeIconPath.setText(large);
            if (!isSmallIconUpdated) {
                myCPSmallIconPath.setText(small);
                isSmallIconUpdated = false;
            }
        } else {
            myCPLargeIconPath.setText(iconFile.getAbsolutePath());
        }
    }

    
}                                                       

    private static File getAnotherIconPath(File iconFile) {
        Set<File> allFiles = IconUtils.getPossibleIcons(iconFile.getAbsolutePath());//GEN-LAST:event_myCPLargeIconPathButtonActionPerformed
        assert allFiles.contains(iconFile);
        
        allFiles.remove(iconFile);
        
        boolean isIconSmall = IconUtils.isValidIcon(iconFile, 16, 16);

        File secondIcon = null;
        boolean isSecondIconSmall = false;
        for (Iterator<File> it = allFiles.iterator(); it.hasNext() && !isSecondIconSmall;) {
            File f = it.next();
            isSecondIconSmall = (isIconSmall) 
                    ? IconUtils.isValidIcon(f, ICON_LARGE_W, ICON_LARGE_H) 
                    : IconUtils.isValidIcon(f, ICON_SMALL_W, ICON_SMALL_H);
            if (isSecondIconSmall) {
                secondIcon = f;
                break;
            }
        }
        return secondIcon;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox myCPAddLibDependencyChk;
    private javax.swing.JTextField myCPClassName;
    private javax.swing.JLabel myCPClassNameLabel;
    private javax.swing.JTextField myCPLargeIconPath;
    private javax.swing.JButton myCPLargeIconPathButton;
    private javax.swing.JLabel myCPLargeIconPathLabel;
    private javax.swing.JTextField myCPLibName;
    private javax.swing.JLabel myCPLibNameLabel;
    private javax.swing.JComboBox myCPPaletteCategoryCombo;
    private javax.swing.JLabel myCPPaletteCategoryLabel;
    private javax.swing.JTextField myCPPaletteDispName;
    private javax.swing.JLabel myCPPaletteDispNameLabel;
    private javax.swing.JTextField myCPPaletteTooltip;
    private javax.swing.JLabel myCPPaletteTooltipLabel;
    private javax.swing.JTextField myCPSmallIconPath;
    private javax.swing.JButton myCPSmallIconPathButton;
    private javax.swing.JLabel myCPSmallIconPathLabel;
    private javax.swing.JRadioButton myCPValidAlwaysRadio;
    private javax.swing.JRadioButton myCPValidCustomRadio;
    private javax.swing.JRadioButton myCPValidPlatformRadio;
    private javax.swing.JPanel myCompProducerPanel;
    private javax.swing.JPanel myLibNamePanel;
    private javax.swing.ButtonGroup myValidButtonGroup;
    private javax.swing.JPanel myValidityPanel;
    // End of variables declaration//GEN-END:variables
    
    private WizardDescriptor mySettings;
    private ComponentProducerWizardPanel myPanel;
    private boolean isSmallIconUpdated;
    private boolean isLargeIconUpdated;
    private boolean isCPClassNameUpdated;
    private boolean listenersAttached;

    DocumentListener myDocListener;
    DocumentListener myClassNameListener;
    DocumentListener mySmallIconPathListener;
    DocumentListener myLargeIconPathListener;
    ActionListener myAddLibDependencyListener;
}
