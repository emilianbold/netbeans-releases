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

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.vmd.componentssupport.ui.UIUtils;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.CustomComponentHelper;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Represents <em>Name and Location</em> panel in J2ME Library Descriptor Wizard.
 *
 * @author ads
 */
final class ComponentDescriptorVisualPanel extends JPanel {
    
    public static final String TXT_DEFAULT_PREFIX
                                              = "TXT_DefaultClassNamePrefix";// NOI18N 

    private static final String MSG_ERR_PREFIX_EMPTY 
                                              = "MSG_CD_EmptyPrefix";       // NOI18N 
    private static final String MSG_ERR_PREFIX_WITH_DOT 
                                              = "MSG_CD_DotInPrefix";          // NOI18N 
    private static final String MSG_ERR_PREFIX_INVALID 
                                              = "MSG_CD_InvalidPrefix";          // NOI18N 
    private static final String MSG_ERR_CLASS_NAME_EMPTY 
                                              = "MSG_CD_EmptyClassName";       // NOI18N 
    private static final String MSG_ERR_CLASS_NAME_INVALID 
                                              = "MSG_CD_InvalidClassName";          // NOI18N 
    private static final String MSG_ERR_CLASS_NAME_EXISTS 
                                              = "MSG_CD_ExistingClassName";          // NOI18N 
    private static final String MSG_ERR_TYPE_ID_EMPTY 
                                              = "MSG_CD_EmptyTypeID";       // NOI18N 
    private static final String MSG_ERR_SUPER_CLASS_EMPTY 
                                              = "MSG_CD_EmptySuperClass";       // NOI18N 
    private static final String MSG_ERR_SUPER_CLASS_INVALID 
                                              = "MSG_CD_InvalidSuperClass";          // NOI18N 
    
    
            
    /** Creates new NameAndLocationPanel */
    ComponentDescriptorVisualPanel(ComponentDescriptorWizardPanel panel) {
        myPanel = panel;
        initComponents();
        
        myCDVersionCombo.setModel(Version.getComboBoxModel());
        
        
        myPrefixListener = new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                updateValuesOnPrefixUpdate();
                checkValidity();
            }
        };
        myClassNAmeListener = new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                isCDClassNameUpdated = true;
                updateValuesOnClassNameUpdate();
                checkValidity();
            }
        };
        myTypeIdListener = new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                isCDTypeIdUpdated = true;
                checkValidity();
            }
        };
        
        mySuperClassListener = new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                checkValidity();
            }
        };
    }
    

    void storeData(WizardDescriptor descriptor) {
        descriptor.putProperty(NewComponentDescriptor.CC_PREFIX, 
                getPrefixValue());
        descriptor.putProperty(NewComponentDescriptor.CD_CLASS_NAME, 
                getClassNameValue());
        descriptor.putProperty(NewComponentDescriptor.CD_TYPE_ID, 
                getTypeIdValue());
        descriptor.putProperty(NewComponentDescriptor.CD_SUPER_DESCR_CLASS, 
                getSuperDescrValue());
        descriptor.putProperty(NewComponentDescriptor.CD_VERSION, 
                getVersionValue());
        descriptor.putProperty(NewComponentDescriptor.CD_CAN_INSTANTIATE, 
                getCanInstantiateValue());
        descriptor.putProperty(NewComponentDescriptor.CD_CAN_BE_SUPER, 
                getCanBeSuperValue());
    }
    
    void readData( WizardDescriptor descriptor) {
        mySettings = descriptor;

        myPrefix.setText(getPrefix());
        if (getClassName() != null){
            myCDClassName.setText(getClassName());
        }
        if (getTypeId() != null){
            myCDTypeId.setText(getTypeId());
        }
        myCDSuperClass.setText(getSuperDescriptor());
        myCDVersionCombo.setSelectedItem(getVersion());
        myCanInstantiateChk.setSelected(getCanInstantiate());
        myCanBeSuperChk.setSelected(getCanBeSuper());

        updateValuesOnPrefixUpdate();
        checkValidity();
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
    
    private void attachDocumentListeners() {
        if (!listenersAttached) {
            myPrefix.getDocument().addDocumentListener(myPrefixListener);
            myCDClassName.getDocument().addDocumentListener(myClassNAmeListener);
            myCDTypeId.getDocument().addDocumentListener(myTypeIdListener);
            myCDSuperClass.getDocument().addDocumentListener(mySuperClassListener);
            listenersAttached = true;
        }
    }

    private void removeDocumentListeners() {
        if (listenersAttached) {
            myPrefix.getDocument().removeDocumentListener(myPrefixListener);
            myCDClassName.getDocument().removeDocumentListener(myClassNAmeListener);
            myCDTypeId.getDocument().removeDocumentListener(myTypeIdListener);
            myCDSuperClass.getDocument().removeDocumentListener(mySuperClassListener);
            listenersAttached = false;
        }
    }

    private String getTypeId(){
        return (String)mySettings.getProperty(
                NewComponentDescriptor.CD_TYPE_ID);
    }
    
    private String getClassName(){
        return (String)mySettings.getProperty(
                NewComponentDescriptor.CD_CLASS_NAME);
    }
    
    private String getSuperDescriptor(){
        String superDescr = (String)mySettings.getProperty(
                NewComponentDescriptor.CD_SUPER_DESCR_CLASS);
        if (superDescr == null){
            superDescr = NewComponentDescriptor.COMPONENT_DECRIPTOR_DEFAULT_PARENT;
        }
        return superDescr;
    }

    private boolean getCanInstantiate(){
        Object value = mySettings.getProperty(
                NewComponentDescriptor.CD_CAN_INSTANTIATE);
        return extractBoolean(value, 
                NewComponentDescriptor.COMPONENT_DECRIPTOR_DEFAULT_CAN_INSTANTIATE);
    }

    private boolean getCanBeSuper(){
        Object value = mySettings.getProperty(
                NewComponentDescriptor.CD_CAN_BE_SUPER);
        return extractBoolean(value, 
                NewComponentDescriptor.COMPONENT_DECRIPTOR_DEFAULT_CAN_BE_SUPER);
    }
    
    private static boolean extractBoolean(Object value, boolean defaultValue){
        if (value == null || !(value instanceof Boolean)){
            return defaultValue;
        }
        return (Boolean)value;
    }
    
    private String getPrefix(){
        String prefix = (String)mySettings.getProperty(
                NewComponentDescriptor.CC_PREFIX);
        if (prefix == null){
            prefix = getDefaultPrefix();
        }
        return prefix;
    }

    private String getDefaultPrefix(){
        String prefixProposal = getMessage(TXT_DEFAULT_PREFIX);
        int index = 1;
        String prefix = getDefaultFreePrefix(prefixProposal, "");
        while (prefix == null){
            prefix = getDefaultFreePrefix(prefixProposal, ""+index);
            index++;
        }
        return prefix;
    }
    
    private String getDefaultFreePrefix(String prefix, String index){
        String proposal = prefix + index;
        String cdClass = NewComponentDescriptor.createDefaultCDClass(proposal);
        String producerClass = NewComponentDescriptor.createDefaultProducerClass(proposal);
        if (   getHelper().isCDClassNameExist(cdClass) 
            || getHelper().isProducerClassNameExist(producerClass))
        {
            proposal = null;
        }
        return proposal;
    }
    
    private Version getVersion(){
        Object value = mySettings.getProperty(
                NewComponentDescriptor.CD_VERSION);
        if (value == null || !(value instanceof Version)){
            return Version.MIDP;
        }
        return (Version)value;
    }
    
    private boolean checkValidity(){
        if (!isCCPrefixValid()){
            return false;
        }
        if (!isCCClassNameValid()){
            return false;
        }
        if (!isCCTypeIDValid()){
            return false;
        }
        if (!isCCSuperClassValid()){
            return false;
        }
        markValid();
        return true;
    }

    private void updateValuesOnPrefixUpdate(){
        if (isCCPrefixValid()){
            if (!isCDClassNameUpdated){
                myCDClassName.setText( 
                        NewComponentDescriptor.createDefaultCDClass(getPrefixValue()));
                isCDClassNameUpdated = false;
            }
            if (!isCDTypeIdUpdated){
                myCDTypeId.setText(getCodeNameBase() + "." + getPrefixValue());
                isCDTypeIdUpdated = false;
            }
        }
    }
    
    private void updateValuesOnClassNameUpdate(){
        if (isCCClassNameValid()){
            if (!isCDTypeIdUpdated){
                myCDTypeId.setText(getCodeNameBase() + "." + getPrefixValue());
                isCDTypeIdUpdated = false;
            }
        }
    }
    
    private String getCodeNameBase(){
        return getHelper().getCodeNameBase();
    }

    private CustomComponentHelper getHelper(){
        return (CustomComponentHelper)mySettings.getProperty( 
                NewComponentDescriptor.HELPER);
    }
    
    // TODO add unique validation
    private boolean isCCClassNameValid(){
        String name = getClassNameValue();
        if (name.length() == 0) {
            setError(getMessage(MSG_ERR_CLASS_NAME_EMPTY));
            return false;
        } else if (!Utilities.isJavaIdentifier(name)){
            setError(getMessage(MSG_ERR_CLASS_NAME_INVALID));
            return false;
        } else if (getHelper().isCDClassNameExist(name)){
            setError(getMessage(MSG_ERR_CLASS_NAME_EXISTS));
            return false;
        }
        return true;
    }
    
    private boolean isCCTypeIDValid(){
        String typeId = getTypeIdValue();
        if (typeId.length() == 0) {
            setError(getMessage(MSG_ERR_TYPE_ID_EMPTY));
            return false;
        }
        return true;
    }
    
    private boolean isCCSuperClassValid(){
        String name = getSuperDescrValue();
        if (name.length() == 0) {
            setError(getMessage(MSG_ERR_SUPER_CLASS_EMPTY));
            return false;
        } else if (!UIUtils.isValidJavaFQN(name)){
            setError(getMessage(MSG_ERR_SUPER_CLASS_INVALID));
            return false;
        }
        return true;
    }
    
    private boolean isCCPrefixValid(){
        String prefix = getPrefixValue();
        if (prefix.length() == 0) {
            setError(getMessage(MSG_ERR_PREFIX_EMPTY));
            return false;
        } else if (prefix.contains(".")){
            setError(getMessage(MSG_ERR_PREFIX_WITH_DOT));
            return false;
        } else if (!Utilities.isJavaIdentifier(prefix)){
            setError(getMessage(MSG_ERR_PREFIX_INVALID));
            return false;
        }
        return true;
    }
    
    private String getPrefixValue(){
        return myPrefix.getText().trim();
    }
    
    private String getClassNameValue(){
        return myCDClassName.getText().trim();
    }
    
    private String getTypeIdValue(){
        return myCDTypeId.getText().trim();
    }
    
    private String getSuperDescrValue(){
        return myCDSuperClass.getText().trim();
    }
    
    private Version getVersionValue(){
        return (Version)myCDVersionCombo.getSelectedItem();
    }
    
    private Boolean getCanInstantiateValue(){
        return myCanInstantiateChk.isSelected();
    }
    
    private Boolean getCanBeSuperValue(){
        return myCanBeSuperChk.isSelected();
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
        return new HelpCtx(ComponentDescriptorVisualPanel.class);
    }
    
    private static String getMessage(String key, Object... args) {
        return NbBundle.getMessage(ComponentDescriptorVisualPanel.class, key, args);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        myCustCompPanel = new javax.swing.JPanel();
        myCustCompPrefixPanel = new javax.swing.JPanel();
        myPrefixLabel = new javax.swing.JLabel();
        myPrefix = new javax.swing.JTextField();
        myCompDescrPanel = new javax.swing.JPanel();
        myCDVersionLabel = new javax.swing.JLabel();
        myCDVersionCombo = new javax.swing.JComboBox();
        myCDClassNameLabel = new javax.swing.JLabel();
        myCDTypeIdLabel = new javax.swing.JLabel();
        myCDSuperClassLabel = new javax.swing.JLabel();
        myCDSuperClass = new javax.swing.JTextField();
        myCDTypeId = new javax.swing.JTextField();
        myCDClassName = new javax.swing.JTextField();
        myCanInstantiateChk = new javax.swing.JCheckBox();
        myCanBeSuperChk = new javax.swing.JCheckBox();

        myCustCompPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "LBL_CD_CustomComponentArea"))); // NOI18N

        myPrefixLabel.setLabelFor(myPrefix);
        org.openide.awt.Mnemonics.setLocalizedText(myPrefixLabel, org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "LBL_CD_Prefix")); // NOI18N

        myPrefix.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_Prefix")); // NOI18N

        org.jdesktop.layout.GroupLayout myCustCompPrefixPanelLayout = new org.jdesktop.layout.GroupLayout(myCustCompPrefixPanel);
        myCustCompPrefixPanel.setLayout(myCustCompPrefixPanelLayout);
        myCustCompPrefixPanelLayout.setHorizontalGroup(
            myCustCompPrefixPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myCustCompPrefixPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(myPrefixLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myPrefix, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                .addContainerGap())
        );
        myCustCompPrefixPanelLayout.setVerticalGroup(
            myCustCompPrefixPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myCustCompPrefixPanelLayout.createSequentialGroup()
                .add(myCustCompPrefixPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myPrefixLabel)
                    .add(myPrefix, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        myPrefixLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSN_CD_Prefix")); // NOI18N
        myPrefixLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_Prefix")); // NOI18N
        myPrefix.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSN_CD_Prefix")); // NOI18N
        myPrefix.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_Prefix")); // NOI18N

        myCompDescrPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "LBL_CD_ComponentDescriptor"))); // NOI18N

        myCDVersionLabel.setLabelFor(myCDVersionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(myCDVersionLabel, org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "LBL_CD_Version")); // NOI18N

        myCDClassNameLabel.setLabelFor(myCDClassName);
        org.openide.awt.Mnemonics.setLocalizedText(myCDClassNameLabel, org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "LBL_CD_ClassName")); // NOI18N

        myCDTypeIdLabel.setLabelFor(myCDTypeId);
        org.openide.awt.Mnemonics.setLocalizedText(myCDTypeIdLabel, org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "LBL_CD_TypeId")); // NOI18N

        myCDSuperClassLabel.setLabelFor(myCDSuperClass);
        org.openide.awt.Mnemonics.setLocalizedText(myCDSuperClassLabel, org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "LBL_CD_SuperDescriptorClass")); // NOI18N

        myCDSuperClass.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_SuperDescriptorClass")); // NOI18N

        myCDTypeId.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_TypeId")); // NOI18N

        myCDClassName.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_ClassName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myCanInstantiateChk, org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "LBL_CD_CanInstantiate")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myCanBeSuperChk, org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "LBL_CD_CanBeSuperType")); // NOI18N

        org.jdesktop.layout.GroupLayout myCompDescrPanelLayout = new org.jdesktop.layout.GroupLayout(myCompDescrPanel);
        myCompDescrPanel.setLayout(myCompDescrPanelLayout);
        myCompDescrPanelLayout.setHorizontalGroup(
            myCompDescrPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myCompDescrPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(myCompDescrPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myCompDescrPanelLayout.createSequentialGroup()
                        .add(myCompDescrPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(myCDClassNameLabel)
                            .add(myCDTypeIdLabel)
                            .add(myCDSuperClassLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(myCompDescrPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, myCDTypeId, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                            .add(myCDClassName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                            .add(myCompDescrPanelLayout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(myCompDescrPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(myCDVersionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(myCDSuperClass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)))))
                    .add(myCDVersionLabel)
                    .add(myCanInstantiateChk)
                    .add(myCanBeSuperChk))
                .addContainerGap())
        );
        myCompDescrPanelLayout.setVerticalGroup(
            myCompDescrPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myCompDescrPanelLayout.createSequentialGroup()
                .add(myCompDescrPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myCDClassNameLabel)
                    .add(myCDClassName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myCompDescrPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myCDTypeIdLabel)
                    .add(myCDTypeId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myCompDescrPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myCDSuperClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(myCDSuperClassLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myCompDescrPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myCDVersionLabel)
                    .add(myCDVersionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myCanInstantiateChk)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myCanBeSuperChk)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        myCDVersionLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSN_CD_Version")); // NOI18N
        myCDVersionLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_Version")); // NOI18N
        myCDClassNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSN_CD_ClassName")); // NOI18N
        myCDClassNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_ClassName")); // NOI18N
        myCDTypeIdLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSN_CD_TypeId")); // NOI18N
        myCDTypeIdLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_TypeId")); // NOI18N
        myCDSuperClassLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSN_CD_SuperDescriptorClass")); // NOI18N
        myCDSuperClassLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_SuperDescriptorClass")); // NOI18N
        myCanInstantiateChk.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSN_CD_CanInstantiate")); // NOI18N
        myCanInstantiateChk.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_CanInstantiate")); // NOI18N
        myCanBeSuperChk.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSN_CD_CanBeSuperType")); // NOI18N
        myCanBeSuperChk.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_CanBeSuperType")); // NOI18N

        org.jdesktop.layout.GroupLayout myCustCompPanelLayout = new org.jdesktop.layout.GroupLayout(myCustCompPanel);
        myCustCompPanel.setLayout(myCustCompPanelLayout);
        myCustCompPanelLayout.setHorizontalGroup(
            myCustCompPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, myCustCompPrefixPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, myCompDescrPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        myCustCompPanelLayout.setVerticalGroup(
            myCustCompPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myCustCompPanelLayout.createSequentialGroup()
                .add(myCustCompPrefixPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myCompDescrPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, myCustCompPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(myCustCompPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(102, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSN_CD_Panel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_Panel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField myCDClassName;
    private javax.swing.JLabel myCDClassNameLabel;
    private javax.swing.JTextField myCDSuperClass;
    private javax.swing.JLabel myCDSuperClassLabel;
    private javax.swing.JTextField myCDTypeId;
    private javax.swing.JLabel myCDTypeIdLabel;
    private javax.swing.JComboBox myCDVersionCombo;
    private javax.swing.JLabel myCDVersionLabel;
    private javax.swing.JCheckBox myCanBeSuperChk;
    private javax.swing.JCheckBox myCanInstantiateChk;
    private javax.swing.JPanel myCompDescrPanel;
    private javax.swing.JPanel myCustCompPanel;
    private javax.swing.JPanel myCustCompPrefixPanel;
    private javax.swing.JTextField myPrefix;
    private javax.swing.JLabel myPrefixLabel;
    // End of variables declaration//GEN-END:variables
    
    private WizardDescriptor mySettings;
    private ComponentDescriptorWizardPanel myPanel;
    private boolean isCDClassNameUpdated;
    private boolean isCDTypeIdUpdated;
    private boolean listenersAttached;

    private DocumentListener myPrefixListener;
    private DocumentListener myClassNAmeListener;
    private DocumentListener myTypeIdListener;
    private DocumentListener mySuperClassListener;
}
