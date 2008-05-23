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
        
        myPrefix.getDocument().addDocumentListener(new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                updateValuesOnPrefixUpdate();
                checkValidity();
            }
        });
        myCDClassName.getDocument().addDocumentListener(new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                isCDClassNameUpdated = true;
                updateValuesOnClassNameUpdate();
                checkValidity();
            }
        });
        myCDTypeId.getDocument().addDocumentListener(new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                isCDTypeIdUpdated = true;
                checkValidity();
            }
        });
        myCDSuperClass.getDocument().addDocumentListener(new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                checkValidity();
            }
        });
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
        myCDClassName.setText((String)mySettings.getProperty(
                NewComponentDescriptor.CD_CLASS_NAME));
        myCDTypeId.setText((String)mySettings.getProperty(
                NewComponentDescriptor.CD_TYPE_ID));
        myCDSuperClass.setText(getSuperDescriptor());
        myCDVersionCombo.setSelectedItem(getVersion());
        myCanInstantiateChk.setSelected(getCanInstantiate());
        myCanBeSuperChk.setSelected(getCanBeSuper());

        isCDClassNameUpdated = false;
        isCDTypeIdUpdated = false;
        updateValuesOnPrefixUpdate();
        checkValidity();
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
        return extractBoolean(value, false);
    }

    private boolean getCanBeSuper(){
        Object value = mySettings.getProperty(
                NewComponentDescriptor.CD_CAN_BE_SUPER);
        return extractBoolean(value, false);
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
        return NbBundle.getMessage(ComponentDescriptorVisualPanel.class,
                    TXT_DEFAULT_PREFIX);
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
                myCDClassName.setText(getPrefixValue() + 
                        NewComponentDescriptor.COMPONENT_DESCRIPTOR_POSTFIX);
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
        return (String)mySettings.getProperty( 
                NewComponentDescriptor.CODE_NAME_BASE);
    }

    // TODO add unique validation
    private boolean isCCClassNameValid(){
        String name = getClassNameValue();
        if (name.length() == 0) {
            setError(NbBundle.getMessage(ComponentDescriptorVisualPanel.class, 
                    MSG_ERR_CLASS_NAME_EMPTY));
            return false;
        } else if (!Utilities.isJavaIdentifier(name)){
            setError(NbBundle.getMessage(ComponentDescriptorVisualPanel.class, 
                    MSG_ERR_CLASS_NAME_INVALID));
            return false;
        }
        return true;
    }
    
    private boolean isCCTypeIDValid(){
        String typeId = getTypeIdValue();
        if (typeId.length() == 0) {
            setError(NbBundle.getMessage(ComponentDescriptorVisualPanel.class, 
                    MSG_ERR_TYPE_ID_EMPTY));
            return false;
        }
        return true;
    }
    
    private boolean isCCSuperClassValid(){
        String name = getSuperDescrValue();
        if (name.length() == 0) {
            setError(NbBundle.getMessage(ComponentDescriptorVisualPanel.class, 
                    MSG_ERR_SUPER_CLASS_EMPTY));
            return false;
        } else if (!Utilities.isJavaIdentifier(name)){
            setError(NbBundle.getMessage(ComponentDescriptorVisualPanel.class, 
                    MSG_ERR_SUPER_CLASS_INVALID));
            return false;
        }
        return true;
    }
    
    private boolean isCCPrefixValid(){
        String prefix = getPrefixValue();
        if (prefix.length() == 0) {
            setError(NbBundle.getMessage(ComponentDescriptorVisualPanel.class, 
                    MSG_ERR_PREFIX_EMPTY));
            return false;
        } else if (prefix.contains(".")){
            setError(NbBundle.getMessage(ComponentDescriptorVisualPanel.class, 
                    MSG_ERR_PREFIX_WITH_DOT));
            return false;
        } else if (!Utilities.isJavaIdentifier(prefix)){
            setError(NbBundle.getMessage(ComponentDescriptorVisualPanel.class, 
                    MSG_ERR_PREFIX_INVALID));
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
    
    public void addNotify() {
        super.addNotify();
        checkValidity();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        CustCompPanel = new javax.swing.JPanel();
        myCustCompLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        myPrefixLabel = new javax.swing.JLabel();
        myPrefix = new javax.swing.JTextField();
        CompDescrPanel = new javax.swing.JPanel();
        myCompDescrLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
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

        org.openide.awt.Mnemonics.setLocalizedText(myCustCompLabel, org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "LBL_CD_CustomComponentArea")); // NOI18N

        myPrefixLabel.setLabelFor(myPrefix);
        org.openide.awt.Mnemonics.setLocalizedText(myPrefixLabel, org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "LBL_CD_Prefix")); // NOI18N

        myPrefix.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_Prefix")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(myPrefixLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myPrefix, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myPrefixLabel)
                    .add(myPrefix, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        myPrefixLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSN_CD_Prefix")); // NOI18N
        myPrefixLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_Prefix")); // NOI18N
        myPrefix.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSN_CD_Prefix")); // NOI18N
        myPrefix.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_Prefix")); // NOI18N

        org.jdesktop.layout.GroupLayout CustCompPanelLayout = new org.jdesktop.layout.GroupLayout(CustCompPanel);
        CustCompPanel.setLayout(CustCompPanelLayout);
        CustCompPanelLayout.setHorizontalGroup(
            CustCompPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(CustCompPanelLayout.createSequentialGroup()
                .add(myCustCompLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        CustCompPanelLayout.setVerticalGroup(
            CustCompPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(CustCompPanelLayout.createSequentialGroup()
                .add(CustCompPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
                    .add(myCustCompLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        myCustCompLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSN_CD_CustomComponentArea")); // NOI18N
        myCustCompLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_CustomComponentArea")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myCompDescrLabel, org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "LBL_CD_ComponentDescriptor")); // NOI18N

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

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(myCDClassNameLabel)
                            .add(myCDTypeIdLabel)
                            .add(myCDSuperClassLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, myCDTypeId, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                            .add(myCDClassName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                            .add(jPanel2Layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(myCDVersionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 122, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(myCDSuperClass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)))))
                    .add(myCDVersionLabel)
                    .add(myCanInstantiateChk)
                    .add(myCanBeSuperChk))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myCDClassNameLabel)
                    .add(myCDClassName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myCDTypeIdLabel)
                    .add(myCDTypeId, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myCDSuperClass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(myCDSuperClassLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
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

        org.jdesktop.layout.GroupLayout CompDescrPanelLayout = new org.jdesktop.layout.GroupLayout(CompDescrPanel);
        CompDescrPanel.setLayout(CompDescrPanelLayout);
        CompDescrPanelLayout.setHorizontalGroup(
            CompDescrPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(CompDescrPanelLayout.createSequentialGroup()
                .add(myCompDescrLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        CompDescrPanelLayout.setVerticalGroup(
            CompDescrPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(CompDescrPanelLayout.createSequentialGroup()
                .add(CompDescrPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, CompDescrPanelLayout.createSequentialGroup()
                        .add(myCompDescrLabel)
                        .add(1, 1, 1))
                    .add(CompDescrPanelLayout.createSequentialGroup()
                        .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(13, 13, 13)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        myCompDescrLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSN_CD_ComponentDescriptor")); // NOI18N
        myCompDescrLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_ComponentDescriptor")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, CompDescrPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, CustCompPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(CustCompPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(CompDescrPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSN_CD_Panel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentDescriptorVisualPanel.class, "ACSD_CD_Panel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel CompDescrPanel;
    private javax.swing.JPanel CustCompPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
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
    private javax.swing.JLabel myCompDescrLabel;
    private javax.swing.JLabel myCustCompLabel;
    private javax.swing.JTextField myPrefix;
    private javax.swing.JLabel myPrefixLabel;
    // End of variables declaration//GEN-END:variables
    
    private WizardDescriptor mySettings;
    private ComponentDescriptorWizardPanel myPanel;
    private boolean isCDClassNameUpdated;
    private boolean isCDTypeIdUpdated;
    
}
