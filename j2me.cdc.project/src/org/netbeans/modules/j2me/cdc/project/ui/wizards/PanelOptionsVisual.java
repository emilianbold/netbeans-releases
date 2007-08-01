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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2me.cdc.project.ui.wizards;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author  phrebejk
 */
public class PanelOptionsVisual extends SettingsPanel implements ActionListener, PropertyChangeListener {
    
    private static boolean lastMainClassCheck = true; // XXX Store somewhere
    
    protected PanelConfigureProject panel;
    private boolean valid;
    private int type;

    //private ProjectTypeProvider provider;
    private PropertyChangeListener pcl;
    
    /** Creates new form PanelOptionsVisual */
    public PanelOptionsVisual( final PanelConfigureProject panel, int type ) {
        initComponents();
        this.type = type;
        this.panel = panel;
                        
        pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                panel.fireChangeEvent();
            }
        };

        switch (type) {
            case NewCDCProjectWizardIterator.TYPE_LIB:
                setAsMainCheckBox.setVisible( false );
                createMainCheckBox.setVisible( false );
                mainClassTextField.setVisible( false );
                separator.setVisible( false );
                break;
            case NewCDCProjectWizardIterator.TYPE_APP:
                createMainCheckBox.addActionListener( this );
                createMainCheckBox.setSelected( lastMainClassCheck );
                mainClassTextField.setEnabled( lastMainClassCheck );
                separator.setVisible( true );
                break;
            case NewCDCProjectWizardIterator.TYPE_EXT:
                setAsMainCheckBox.setVisible( true );
                createMainCheckBox.setVisible( false );
                mainClassTextField.setVisible( false );
                break;
            case NewCDCProjectWizardIterator.TYPE_SAMPLE:
                createMainCheckBox.setVisible( false );
                mainClassTextField.setVisible( false );
                separator.setVisible( true );
                break;
        }
        
        this.mainClassTextField.getDocument().addDocumentListener( new DocumentListener () {
            
            public void insertUpdate(DocumentEvent e) {
                mainClassChanged ();
            }
            
            public void removeUpdate(DocumentEvent e) {
                mainClassChanged ();
            }
            
            public void changedUpdate(DocumentEvent e) {
                mainClassChanged ();
            }
            
        });
        
     }

    public void actionPerformed( ActionEvent e ) {        
        if ( e.getSource() == createMainCheckBox ) {
            lastMainClassCheck = createMainCheckBox.isSelected();
            mainClassTextField.setEnabled( lastMainClassCheck );        
            this.panel.fireChangeEvent();
        }                
    }
    
    public void propertyChange (PropertyChangeEvent event) {
        if (PanelProjectLocationVisual.PROP_PROJECT_NAME.equals(event.getPropertyName())) {
            String newProjectName = NewCDCProjectWizardIterator.getPackageName((String) event.getNewValue());
            this.mainClassTextField.setText (MessageFormat.format(
                NbBundle.getMessage (PanelOptionsVisual.class,"TXT_ClassName"), new Object[] {newProjectName}
            ));
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setAsMainCheckBox = new javax.swing.JCheckBox();
        createMainCheckBox = new javax.swing.JCheckBox();
        mainClassTextField = new javax.swing.JTextField();
        separator = new javax.swing.JSeparator();
        containerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setAsMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(setAsMainCheckBox, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_setAsMainCheckBox")); // NOI18N
        setAsMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        add(setAsMainCheckBox, gridBagConstraints);
        setAsMainCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSN_setAsMainCheckBox")); // NOI18N
        setAsMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSD_setAsMainCheckBox")); // NOI18N

        createMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createMainCheckBox, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_createMainCheckBox")); // NOI18N
        createMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(createMainCheckBox, gridBagConstraints);
        createMainCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSN_createMainCheckBox")); // NOI18N
        createMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSD_createMainCheckBox")); // NOI18N

        mainClassTextField.setText("com.myapp.Main");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(mainClassTextField, gridBagConstraints);
        mainClassTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ASCN_mainClassTextFiled")); // NOI18N
        mainClassTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ASCD_mainClassTextFiled")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(separator, gridBagConstraints);

        containerPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(containerPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_PanelOptionsVisual")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_PanelOptionsVisual")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
           
    boolean valid(WizardDescriptor settings) {
        if (mainClassTextField.isVisible () && mainClassTextField.isEnabled ()) {
            if (!valid) {
                settings.putProperty( "WizardPanel_errorMessage", // NOI18N
                    NbBundle.getMessage(PanelOptionsVisual.class,"ERROR_IllegalMainClassName")); //NOI18N
            }
            return this.valid;
        }
        return true;
    }
    
    void read (WizardDescriptor d) {
 
        JavaPlatform platforms[]=JavaPlatformManager.getDefault().getPlatforms (null, new Specification(CDCPlatform.PLATFORM_CDC,null));    //NOI18N
        if (platforms.length!=0 && platforms[0] instanceof CDCPlatform)
        {
            CDCPlatform platform=(CDCPlatform)platforms[0];
            assert platform != null;
        }
    }
    
    void validate (WizardDescriptor d) {
        // nothing to validate
    }

    void store( WizardDescriptor d ) {
        d.putProperty( /*XXX Define somewhere */ "setAsMain", setAsMainCheckBox.isSelected() && setAsMainCheckBox.isVisible() ? Boolean.TRUE : Boolean.FALSE ); // NOI18N
        d.putProperty( /*XXX Define somewhere */ "mainClass", createMainCheckBox.isSelected() && createMainCheckBox.isVisible() ? mainClassTextField.getText() : null ); // NOI18N
   }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel containerPanel;
    private javax.swing.JCheckBox createMainCheckBox;
    private javax.swing.JTextField mainClassTextField;
    private javax.swing.JSeparator separator;
    private javax.swing.JCheckBox setAsMainCheckBox;
    // End of variables declaration//GEN-END:variables
    
    protected void mainClassChanged () {
        String mainClassName = this.mainClassTextField.getText ();
        StringTokenizer tk = new StringTokenizer (mainClassName, "."); //NOI18N
        boolean valid = true;
out:        while (tk.hasMoreTokens()) {
            String token = tk.nextToken();
            if (token.length() == 0) {
                valid = false;
                break out;
            }
            for (int i=0; i< token.length();i++) {
                if ((i == 0 && !Character.isJavaIdentifierStart(token.charAt(0)))
                    || (i != 0 && !Character.isJavaIdentifierPart(token.charAt(i)))) {
                        valid = false;                        
                        break out;
                }
            }
        }
        this.valid = valid;
        this.panel.fireChangeEvent();
    }
}

