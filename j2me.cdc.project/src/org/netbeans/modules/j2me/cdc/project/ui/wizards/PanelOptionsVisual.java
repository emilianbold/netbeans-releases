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
import org.netbeans.modules.j2me.cdc.platform.spi.CDCProjectInformation;
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
    private JPanel additionalInfoPanel;
    private PropertyChangeListener pcl;
        
    private String PROP_PLATFROM_RESOLVED = "platfromResolved";
    
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
                appName.setVisible( false );                
                jTextFieldAppName.setVisible( false );
                break;
            case NewCDCProjectWizardIterator.TYPE_APP:
                createMainCheckBox.addActionListener( this );
                createMainCheckBox.setSelected( lastMainClassCheck );
                mainClassTextField.setEnabled( lastMainClassCheck );
                separator.setVisible( true );
                appName.setVisible( true );
                jTextFieldAppName.setVisible( true );
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
                appName.setVisible( false );
                jTextFieldAppName.setVisible( false );
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
        
        this.jTextFieldAppName.getDocument().addDocumentListener( new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                PanelOptionsVisual.this.panel.fireChangeEvent();
            }
            
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
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
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        setAsMainCheckBox = new javax.swing.JCheckBox();
        createMainCheckBox = new javax.swing.JCheckBox();
        mainClassTextField = new javax.swing.JTextField();
        appName = new javax.swing.JLabel();
        jTextFieldAppName = new javax.swing.JTextField();
        separator = new javax.swing.JSeparator();
        containerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_PanelOptionsVisual"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_PanelOptionsVisual"));
        setAsMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(setAsMainCheckBox, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_setAsMainCheckBox"));
        setAsMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        add(setAsMainCheckBox, gridBagConstraints);
        setAsMainCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSN_setAsMainCheckBox"));
        setAsMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSD_setAsMainCheckBox"));

        createMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createMainCheckBox, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_createMainCheckBox"));
        createMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(createMainCheckBox, gridBagConstraints);
        createMainCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSN_createMainCheckBox"));
        createMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSD_createMainCheckBox"));

        mainClassTextField.setText("com.myapp.Main");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(mainClassTextField, gridBagConstraints);
        mainClassTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ASCN_mainClassTextFiled"));
        mainClassTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ASCD_mainClassTextFiled"));

        appName.setLabelFor(jTextFieldAppName);
        org.openide.awt.Mnemonics.setLocalizedText(appName, java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/cdc/project/ui/wizards/Bundle").getString("LBL_ApplicationName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(appName, gridBagConstraints);
        appName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class,"ACSN_AppName"));
        appName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class,"ACSD_AppName"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 11, 0);
        add(jTextFieldAppName, gridBagConstraints);
        jTextFieldAppName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class,"ACSN_SpecAppName"));
        jTextFieldAppName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class,"ACSD_SpecAppName"));

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

    }// </editor-fold>//GEN-END:initComponents
           
    boolean valid(WizardDescriptor settings) {
        if (jTextFieldAppName.isVisible() && jTextFieldAppName.getText().trim().length() == 0){
            settings.putProperty( "WizardPanel_errorMessage", // NOI18N
                NbBundle.getMessage(PanelOptionsVisual.class,"ERR_ApplicationNameMissing")); //NOI18N
            return false;
        }

        
        String s = (String)settings.getProperty("activePlatform");
        JavaPlatform platforms[]=JavaPlatformManager.getDefault().getPlatforms (s, new Specification(CDCPlatform.PLATFORM_CDC,null));    //NOI18N
        if (platforms.length!=0 && platforms[0] instanceof CDCPlatform)
        {
            CDCPlatform plat=(CDCPlatform)platforms[0];
            CDCProjectInformation info = plat.getCDCProjectInformation();
            if (info == null)
                return false;
            settings.putProperty( "WizardPanel_errorMessage", info.getMessage());
            if (!info.isPanelValid())
                return false;
        }
        
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
        if (jTextFieldAppName.getText().length() == 0){
            jTextFieldAppName.setText((String) d.getProperty ("name"));
        }

        String s = (String)d.getProperty("activePlatform");
        assert s != null;
        JavaPlatform platforms[]=JavaPlatformManager.getDefault().getPlatforms (s, new Specification(CDCPlatform.PLATFORM_CDC,null));    //NOI18N
        if (platforms.length!=0 && platforms[0] instanceof CDCPlatform)
        {
            CDCPlatform platform=(CDCPlatform)platforms[0];
            assert platform != null;

            Properties p = (Properties) d.getProperty("additionalProperties");
            p.put(PROP_PLATFROM_RESOLVED, platform);
            CDCProjectInformation cpi=platform.getCDCProjectInformation();
            assert cpi != null;
            cpi.setProperties( p );
            additionalInfoPanel = platform.getCDCProjectInformation().getAdditionalPanel();
            this.setVisible(false);
            containerPanel.removeAll();
            containerPanel.add(additionalInfoPanel, BorderLayout.CENTER);
            additionalInfoPanel.addPropertyChangeListener(pcl);
            this.setVisible(true);
            this.invalidate();
        }
    }
    
    void validate (WizardDescriptor d) {
        // nothing to validate
    }

    void store( WizardDescriptor d ) {
        d.putProperty( /*XXX Define somewhere */ "setAsMain", setAsMainCheckBox.isSelected() && setAsMainCheckBox.isVisible() ? Boolean.TRUE : Boolean.FALSE ); // NOI18N
        d.putProperty( /*XXX Define somewhere */ "mainClass", createMainCheckBox.isSelected() && createMainCheckBox.isVisible() ? mainClassTextField.getText() : null ); // NOI18N
        if ( type != NewCDCProjectWizardIterator.TYPE_LIB){            
            d.putProperty( /*XXX Define somewhere */ "appName",  jTextFieldAppName.getText()); // NOI18N
        }
        if (additionalInfoPanel != null){
            additionalInfoPanel.removePropertyChangeListener(pcl);
        }
        String s = (String)d.getProperty("activePlatform");
        JavaPlatform platforms[]=JavaPlatformManager.getDefault().getPlatforms (s, new Specification(CDCPlatform.PLATFORM_CDC,null));    //NOI18N
        if (platforms.length!=0 && platforms[0] instanceof CDCPlatform)
        {
            CDCPlatform platform=(CDCPlatform)platforms[0];
            Properties p = platform.getCDCProjectInformation().getProperties();
            p.remove(PROP_PLATFROM_RESOLVED); //remove otherwise it is going to be stored in propejct.properties!!
            d.putProperty("additionalProperties", p);
        }
   }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel appName;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JCheckBox createMainCheckBox;
    private javax.swing.JTextField jTextFieldAppName;
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

