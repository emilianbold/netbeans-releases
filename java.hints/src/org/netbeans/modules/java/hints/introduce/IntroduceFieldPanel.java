/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import org.netbeans.modules.java.hints.introduce.IntroduceFieldPanel;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.element.Modifier;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Lahoda
 */
public class IntroduceFieldPanel extends javax.swing.JPanel {
    
    public static final int INIT_METHOD = 1;
    public static final int INIT_FIELD = 2;
    public static final int INIT_CONSTRUCTORS = 4;
    
    private static final int ACCESS_PUBLIC = 1;
    private static final int ACCESS_PROTECTED = 2;
    private static final int ACCESS_DEFAULT = 3;
    private static final int ACCESS_PRIVATE = 4;
    
    private int[] allowInitMethods;
    private boolean allowFinalInCurrentMethod;
    
    public IntroduceFieldPanel(String name, int[] allowInitMethods, int numOccurrences, boolean allowFinalInCurrentMethod) {
        initComponents();
        
        this.name.setText(name);
        this.allowInitMethods = allowInitMethods;
        this.replaceAll.setEnabled(numOccurrences > 1);
        this.allowFinalInCurrentMethod = allowFinalInCurrentMethod;
        
        Preferences pref = getPreferences();
        if( numOccurrences == 1 ) {
            replaceAll.setEnabled( false );
            replaceAll.setSelected( false );
        } else {
            replaceAll.setEnabled( true );
            replaceAll.setText( replaceAll.getText() + " (" + numOccurrences + ")" );
            replaceAll.setSelected( pref.getBoolean("replaceAll", true) ); //NOI18N
        }
        
        declareFinal.setSelected( pref.getBoolean("declareFinal", true) ); //NOI18N
        
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

        int init = pref.getInt( "initMethod", INIT_METHOD ); //NOI18N
        switch( init ) {
        case INIT_FIELD:
            initField.setSelected( true );
            break;
        case INIT_CONSTRUCTORS:
            initConstructors.setSelected( true );
            break;
        case INIT_METHOD:
            initMethod.setSelected( true );
            break;
        }
        
        adjustInitializeIn();
        adjustFinal();
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule( IntroduceFieldPanel.class ).node( "introduceField" ); //NOI18N
    }

    private void adjustInitializeIn() {
        int allowInitMethods = this.allowInitMethods[this.replaceAll.isSelected() ? 1 : 0];
        
        initMethod.setEnabled((allowInitMethods & INIT_METHOD) != 0);
        initField.setEnabled((allowInitMethods & INIT_FIELD) != 0);
        initConstructors.setEnabled((allowInitMethods & INIT_CONSTRUCTORS) != 0);
        
        if( !initMethod.isEnabled() && initMethod.isSelected() ) {
            if( initField.isEnabled() )
                initField.setSelected(true);
            else
                initConstructors.setSelected(true);
        } else if( !initField.isEnabled() && initField.isSelected() ) {
            if( initMethod.isEnabled() )
                initMethod.setSelected(true);
            else
                initConstructors.setSelected(true);
        } else if( !initConstructors.isEnabled() && initConstructors.isSelected() ) {
            if( initMethod.isEnabled() )
                initMethod.setSelected(true);
            else
                initField.setSelected(true);
        }
    }
    
    private void adjustFinal() {
        declareFinal.setEnabled( !(initMethod.isSelected() && !allowFinalInCurrentMethod) );
        if (initMethod.isSelected() && !allowFinalInCurrentMethod) {
            declareFinal.setSelected(false);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        initilizeIn = new javax.swing.ButtonGroup();
        accessGroup = new javax.swing.ButtonGroup();
        lblName = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        replaceAll = new javax.swing.JCheckBox();
        declareFinal = new javax.swing.JCheckBox();
        lblInitializeIn = new javax.swing.JLabel();
        initMethod = new javax.swing.JRadioButton();
        initField = new javax.swing.JRadioButton();
        initConstructors = new javax.swing.JRadioButton();
        lblAccess = new javax.swing.JLabel();
        accessPublic = new javax.swing.JRadioButton();
        accessProtected = new javax.swing.JRadioButton();
        accessDefault = new javax.swing.JRadioButton();
        accessPrivate = new javax.swing.JRadioButton();

        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_Name")); // NOI18N

        name.setColumns(20);
        name.setText(org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("IntroduceFieldPanel.name.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(replaceAll, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_ReplaceAll")); // NOI18N
        replaceAll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        replaceAll.setMargin(new java.awt.Insets(0, 0, 0, 0));
        replaceAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceAllActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(declareFinal, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_DeclareFinal")); // NOI18N
        declareFinal.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        declareFinal.setMargin(new java.awt.Insets(0, 0, 0, 0));
        declareFinal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                declareFinalActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblInitializeIn, org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "IntroduceFieldPanel.lblInitializeIn.text")); // NOI18N

        initilizeIn.add(initMethod);
        initMethod.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(initMethod, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_CurrentMethod")); // NOI18N
        initMethod.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        initMethod.setMargin(new java.awt.Insets(0, 0, 0, 0));
        initMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initMethodActionPerformed(evt);
            }
        });

        initilizeIn.add(initField);
        org.openide.awt.Mnemonics.setLocalizedText(initField, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_Field")); // NOI18N
        initField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        initField.setMargin(new java.awt.Insets(0, 0, 0, 0));
        initField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initFieldActionPerformed(evt);
            }
        });

        initilizeIn.add(initConstructors);
        org.openide.awt.Mnemonics.setLocalizedText(initConstructors, org.openide.util.NbBundle.getBundle(IntroduceFieldPanel.class).getString("LBL_Constructors")); // NOI18N
        initConstructors.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        initConstructors.setMargin(new java.awt.Insets(0, 0, 0, 0));
        initConstructors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initConstructorsActionPerformed(evt);
            }
        });

        lblAccess.setLabelFor(accessPublic);
        org.openide.awt.Mnemonics.setLocalizedText(lblAccess, org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "LBL_Access")); // NOI18N

        accessGroup.add(accessPublic);
        org.openide.awt.Mnemonics.setLocalizedText(accessPublic, org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "LBL_public")); // NOI18N
        accessPublic.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessPublic.setMargin(new java.awt.Insets(0, 0, 0, 0));

        accessGroup.add(accessProtected);
        org.openide.awt.Mnemonics.setLocalizedText(accessProtected, org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "LBL_protected")); // NOI18N
        accessProtected.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessProtected.setMargin(new java.awt.Insets(0, 0, 0, 0));

        accessGroup.add(accessDefault);
        org.openide.awt.Mnemonics.setLocalizedText(accessDefault, org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "LBL_Default")); // NOI18N
        accessDefault.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessDefault.setMargin(new java.awt.Insets(0, 0, 0, 0));

        accessGroup.add(accessPrivate);
        accessPrivate.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(accessPrivate, org.openide.util.NbBundle.getMessage(IntroduceFieldPanel.class, "LBL_private")); // NOI18N
        accessPrivate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        accessPrivate.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblAccess)
                            .add(lblName))
                        .add(21, 21, 21)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(name, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(accessPublic)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(accessProtected)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(accessDefault)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(accessPrivate))))
                    .add(declareFinal)
                    .add(replaceAll)
                    .add(layout.createSequentialGroup()
                        .add(lblInitializeIn)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(initConstructors)
                            .add(initField)
                            .add(initMethod))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblName))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblAccess)
                    .add(accessPublic)
                    .add(accessProtected)
                    .add(accessDefault)
                    .add(accessPrivate))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(declareFinal)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(replaceAll)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblInitializeIn)
                    .add(layout.createSequentialGroup()
                        .add(initMethod)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(initField)
                        .add(7, 7, 7)
                        .add(initConstructors)))
                .addContainerGap(43, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void declareFinalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_declareFinalActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_declareFinalActionPerformed

private void initConstructorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initConstructorsActionPerformed
    adjustFinal();
}//GEN-LAST:event_initConstructorsActionPerformed

private void initFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initFieldActionPerformed
    adjustFinal();
}//GEN-LAST:event_initFieldActionPerformed

private void initMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initMethodActionPerformed
    adjustFinal();
}//GEN-LAST:event_initMethodActionPerformed

private void replaceAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceAllActionPerformed
        adjustInitializeIn();
}//GEN-LAST:event_replaceAllActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton accessDefault;
    private javax.swing.ButtonGroup accessGroup;
    private javax.swing.JRadioButton accessPrivate;
    private javax.swing.JRadioButton accessProtected;
    private javax.swing.JRadioButton accessPublic;
    private javax.swing.JCheckBox declareFinal;
    private javax.swing.JRadioButton initConstructors;
    private javax.swing.JRadioButton initField;
    private javax.swing.JRadioButton initMethod;
    private javax.swing.ButtonGroup initilizeIn;
    private javax.swing.JLabel lblAccess;
    private javax.swing.JLabel lblInitializeIn;
    private javax.swing.JLabel lblName;
    private javax.swing.JTextField name;
    private javax.swing.JCheckBox replaceAll;
    // End of variables declaration//GEN-END:variables
    
    public String getFieldName() {
        if (fieldNameTest != null) return fieldNameTest;
        return this.name.getText();
    }
    
    public int getInitializeIn() {
        if (initializeInTest != null) return initializeInTest;
        int ret;
        if (initMethod.isSelected())
            ret = INIT_METHOD;
        else if (initField.isSelected())
            ret = INIT_FIELD;
        else if (initConstructors.isSelected())
            ret = INIT_CONSTRUCTORS;
        else
            throw new IllegalStateException();
        getPreferences().putInt( "initMethod", ret ); //NOI18N
        return ret;
    }
    
    public boolean isReplaceAll() {
        if (replaceAllTest != null) return replaceAllTest;
        boolean ret = replaceAll.isSelected();
        getPreferences().putBoolean( "replaceAll", ret ); //NOI18N
        return ret;
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
    
    public boolean isDeclareFinal() {
        if (declareFinalTest != null) return declareFinalTest;
        boolean ret = declareFinal.isSelected();
        getPreferences().putBoolean( "declareFinal", ret ); //NOI18N
        return ret;
    }
    
    //For tests:
    private String fieldNameTest;
    private Integer initializeInTest;
    private Boolean replaceAllTest;
    private Set<Modifier> accessTest;
    private Boolean declareFinalTest;
    
    void setAccess(Set<Modifier> access) {
        this.accessTest = access;
    }

    void setDeclareFinal(Boolean declareFinal) {
        this.declareFinalTest = declareFinal;
    }

    void setFieldName(String fieldName) {
        this.fieldNameTest = fieldName;
    }

    void setInitializeIn(Integer initializeIn) {
        this.initializeInTest = initializeIn;
    }

    void setReplaceAll(Boolean replaceAll) {
        this.replaceAllTest = replaceAll;
    }
    
}
