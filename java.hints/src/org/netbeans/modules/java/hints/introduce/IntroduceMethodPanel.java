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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.element.Modifier;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import org.netbeans.modules.java.hints.introduce.IntroduceHint.TargetDescription;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Lahoda
 */
public class IntroduceMethodPanel extends CommonMembersPanel {
    
    public static final int INIT_METHOD = 1;
    public static final int INIT_FIELD = 2;
    public static final int INIT_CONSTRUCTORS = 4;
    
    private static final int ACCESS_PUBLIC = 1;
    private static final int ACCESS_PROTECTED = 2;
    private static final int ACCESS_DEFAULT = 3;
    private static final int ACCESS_PRIVATE = 4;
    
    private JButton btnOk;
    
    public IntroduceMethodPanel(String name, int duplicatesCount, Iterable<TargetDescription> targets) {
        super(targets);
        initComponents();
        
        this.name.setText(name);
        if ( name != null && name.trim().length() > 0 ) {
            this.name.setCaretPosition(name.length());
            this.name.setSelectionStart(0);
            this.name.setSelectionEnd(name.length());
        }
        
        Preferences pref = getPreferences();
        
        int accessModifier = pref.getInt( "accessModifier", ACCESS_PRIVATE ); //NOI18N
        switch( accessModifier ) {
        case ACCESS_PUBLIC:
            accessPublic.setSelected( true );
            break;
        case ACCESS_PROTECTED:
            accessProtected.setSelected( true );
            break;
        case ACCESS_DEFAULT:
            accessDefault.setSelected( true );
            break;
        case ACCESS_PRIVATE:
            accessPrivate.setSelected( true );
            break;
        }

        if (duplicatesCount == 0) {
            duplicates.setEnabled(false);
            duplicates.setSelected(false);
        } else {
            duplicates.setEnabled(true);
            duplicates.setSelected(true); //from pref
            duplicates.setText(duplicates.getText() + " (" + duplicatesCount + ")");
        }

        initialize(target, duplicates);
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule( IntroduceFieldPanel.class ).node( "introduceField" ); //NOI18N
    }
    
    public void setOkButton( JButton btn ) {
        this.btnOk = btn;
        btnOk.setEnabled(((ErrorLabel)errorLabel).isInputTextValid());
    }
    
    private JLabel createErrorLabel() {
        ErrorLabel.Validator validator = new ErrorLabel.Validator() {

            public String validate(String text) {
                if( null == text 
                    || text.length() == 0 ) return "";
                if (!Utilities.isJavaIdentifier(text))
                    return getDefaultErrorMessage( text );
                return null;
            }
        };
        
        final ErrorLabel eLabel = new ErrorLabel( name.getDocument(), validator );
        eLabel.addPropertyChangeListener(  ErrorLabel.PROP_IS_VALID, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                btnOk.setEnabled(eLabel.isInputTextValid());
            }
        });
        return eLabel;
    }
    
    String getDefaultErrorMessage( String inputText ) {
        return "'" + inputText +"' is not a valid identifier";
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        initilizeIn = new javax.swing.ButtonGroup();
        accessGroup = new javax.swing.ButtonGroup();
        lblName = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        lblAccess = new javax.swing.JLabel();
        accessPublic = new javax.swing.JRadioButton();
        accessProtected = new javax.swing.JRadioButton();
        accessDefault = new javax.swing.JRadioButton();
        accessPrivate = new javax.swing.JRadioButton();
        errorLabel = createErrorLabel();
        duplicates = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        target = new javax.swing.JComboBox();

        lblName.setLabelFor(name);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getBundle(IntroduceMethodPanel.class).getString("LBL_Name")); // NOI18N

        name.setColumns(20);

        lblAccess.setLabelFor(accessPublic);
        org.openide.awt.Mnemonics.setLocalizedText(lblAccess, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "LBL_Access")); // NOI18N

        accessGroup.add(accessPublic);
        org.openide.awt.Mnemonics.setLocalizedText(accessPublic, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "LBL_public")); // NOI18N
        accessPublic.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessPublic.setMargin(new java.awt.Insets(0, 0, 0, 0));

        accessGroup.add(accessProtected);
        org.openide.awt.Mnemonics.setLocalizedText(accessProtected, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "LBL_protected")); // NOI18N
        accessProtected.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessProtected.setMargin(new java.awt.Insets(0, 0, 0, 0));

        accessGroup.add(accessDefault);
        org.openide.awt.Mnemonics.setLocalizedText(accessDefault, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "LBL_Default")); // NOI18N
        accessDefault.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessDefault.setMargin(new java.awt.Insets(0, 0, 0, 0));

        accessGroup.add(accessPrivate);
        accessPrivate.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(accessPrivate, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "LBL_private")); // NOI18N
        accessPrivate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessPrivate.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(duplicates, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "IntroduceMethodPanel.duplicates.text")); // NOI18N

        jLabel1.setLabelFor(target);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "IntroduceMethodPanel.jLabel1.text")); // NOI18N

        target.setModel(new DefaultComboBoxModel());
        target.setRenderer(new TargetsRendererImpl());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAccess)
                            .addComponent(lblName))
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(name, javax.swing.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(accessPublic)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(accessProtected)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(accessDefault)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(accessPrivate))))
                    .addComponent(errorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(duplicates)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(target, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAccess)
                    .addComponent(accessPublic)
                    .addComponent(accessProtected)
                    .addComponent(accessDefault)
                    .addComponent(accessPrivate))
                .addGap(18, 18, 18)
                .addComponent(duplicates)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(target, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 54, Short.MAX_VALUE)
                .addComponent(errorLabel)
                .addContainerGap())
        );

        name.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AN_IntrMethod_Name")); // NOI18N
        name.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Name")); // NOI18N
        accessPublic.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Public")); // NOI18N
        accessProtected.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Protected")); // NOI18N
        accessDefault.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Default")); // NOI18N
        accessPrivate.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Private")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IntroduceMethodPanel.class, "AD_IntrMethod_Dialog")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton accessDefault;
    private javax.swing.ButtonGroup accessGroup;
    private javax.swing.JRadioButton accessPrivate;
    private javax.swing.JRadioButton accessProtected;
    private javax.swing.JRadioButton accessPublic;
    private javax.swing.JCheckBox duplicates;
    private javax.swing.JLabel errorLabel;
    private javax.swing.ButtonGroup initilizeIn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblAccess;
    private javax.swing.JLabel lblName;
    private javax.swing.JTextField name;
    private javax.swing.JComboBox target;
    // End of variables declaration//GEN-END:variables
    
    public String getMethodName() {
        if (methodNameTest != null) return methodNameTest;
        return this.name.getText();
    }
    
    public Set<Modifier> getAccess() {
        if (accessTest != null) return accessTest;
        Set<Modifier> set;
        int val;
        if( accessPublic.isSelected() ) {
            val = ACCESS_PUBLIC;
            set = EnumSet.of(Modifier.PUBLIC);
        } else if( accessProtected.isSelected() ) {
            val = ACCESS_PROTECTED;
            set = EnumSet.of(Modifier.PROTECTED);
        } else if( accessDefault.isSelected() ) {
            val = ACCESS_DEFAULT;
            set = Collections.emptySet();
        } else {
            val = ACCESS_PRIVATE;
            set = EnumSet.of(Modifier.PRIVATE);
        }
        getPreferences().putInt( "accessModifier", val ); //NOI18N
        return set;
    }

    public boolean getReplaceOther() {
        return replaceOtherTest != null ? replaceOtherTest : duplicates.isSelected();
    }

    //For tests:
    private String methodNameTest;
    private Set<Modifier> accessTest;
    private Boolean replaceOtherTest;
    
    void setAccess(Set<Modifier> access) {
        this.accessTest = access;
    }

    void setMethodName(String methodName) {
        this.methodNameTest = methodName;
    }

    void setReplaceOther(boolean v) {
        this.replaceOtherTest = v;
    }
}
