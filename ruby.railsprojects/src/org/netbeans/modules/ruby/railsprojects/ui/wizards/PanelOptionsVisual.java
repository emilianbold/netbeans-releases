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

package org.netbeans.modules.ruby.railsprojects.ui.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.NbBundle;

public class PanelOptionsVisual extends SettingsPanel implements ActionListener, PropertyChangeListener {
    
    private static boolean lastMainClassCheck = true; // XXX Store somewhere
    
    private PanelConfigureProject panel;
    private boolean valid;
    private String javaDb;
    private String otherJdbc;
    public static final String JAVA_DB = "javadb"; // NOI18N
    public static final String JDBC = "jdbc"; // NOI18N
    
    /** Creates new form PanelOptionsVisual */
    public PanelOptionsVisual( PanelConfigureProject panel, int type ) {
        initComponents();
        interpreterDesc.setToolTipText(NbBundle.getMessage(PanelOptionsVisual.class, "RubyIsGlobal"));

        javaDb = NbBundle.getMessage(PanelOptionsVisual.class, "JavaDB");
        otherJdbc = NbBundle.getMessage(PanelOptionsVisual.class, "OtherJDBC");

        dbCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "mysql", javaDb, "oracle", "postgresql", "sqlite2", "sqlite3",
            otherJdbc}));
        dbCombo.addActionListener(this);
        
        this.panel = panel;
        interpreterChanged();

        switch (type) {
//            case NewRailsProjectWizardIterator.TYPE_LIB:
//                setAsMainCheckBox.setVisible( false );
//                createMainCheckBox.setVisible( false );
//                mainClassTextField.setVisible( false );
//                break;
            case NewRailsProjectWizardIterator.TYPE_APP:
                //createMainCheckBox.addActionListener( this );
                //createMainCheckBox.setSelected( lastMainClassCheck );
                //mainClassTextField.setEnabled( lastMainClassCheck );
                break;
            case NewRailsProjectWizardIterator.TYPE_EXT:
                setAsMainCheckBox.setVisible( true );
                //createMainCheckBox.setVisible( false );
                //mainClassTextField.setVisible( false );
                break;
        }
        
        jdbcCheckBox.addActionListener(this);
        warCheckBox.addActionListener(this);
        
        //this.mainClassTextField.getDocument().addDocumentListener( new DocumentListener () {
        //   
        //    public void insertUpdate(DocumentEvent e) {
        //        mainClassChanged ();
        //    }
        //    
        //    public void removeUpdate(DocumentEvent e) {
        //        mainClassChanged ();
        //    }
        //    
        //    public void changedUpdate(DocumentEvent e) {
        //        mainClassChanged ();
        //    }
        //    
        //});
    }

    public void actionPerformed( ActionEvent e ) {  
        if (e.getSource() == jdbcCheckBox) {
            this.panel.fireChangeEvent();
        } else if (e.getSource() == warCheckBox) {
            this.panel.fireChangeEvent();
        } else if (e.getSource() == dbCombo) {
            String db = (String)dbCombo.getSelectedItem();
            if (db.equals(javaDb) || db.equals(otherJdbc)) {
                jdbcCheckBox.setSelected(true);
            }
            this.panel.fireChangeEvent();
        }
        //if ( e.getSource() == createMainCheckBox ) {
        //    lastMainClassCheck = createMainCheckBox.isSelected();
        //    mainClassTextField.setEnabled( lastMainClassCheck );        
        //    this.panel.fireChangeEvent();
        //}                
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        if ("roots".equals(event.getPropertyName())) {
            interpreterChanged();
        }
        //if (PanelProjectLocationVisual.PROP_PROJECT_NAME.equals(event.getPropertyName())) {
        //    String newProjectName = NewRailsProjectWizardIterator.getPackageName((String) event.getNewValue());
        //    if (!Utilities.isJavaIdentifier(newProjectName)) {
        //        newProjectName = NbBundle.getMessage (PanelOptionsVisual.class, "TXT_PackageNameSuffix", newProjectName); 
        //    }
        //    this.mainClassTextField.setText (MessageFormat.format(
        //        NbBundle.getMessage (PanelOptionsVisual.class,"TXT_ClassName"), new Object[] {newProjectName}
        //    ));
        //}
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setAsMainCheckBox = new javax.swing.JCheckBox();
        dbLabel = new javax.swing.JLabel();
        dbCombo = new javax.swing.JComboBox();
        jdbcCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        warCheckBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        interpreterDesc = new javax.swing.JLabel();
        changeRubyButton = new javax.swing.JButton();
        rubyField = new javax.swing.JTextField();

        setAsMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(setAsMainCheckBox, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_setAsMainCheckBox")); // NOI18N
        setAsMainCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        dbLabel.setLabelFor(dbCombo);
        org.openide.awt.Mnemonics.setLocalizedText(dbLabel, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "Database")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jdbcCheckBox, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "UseJdbc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "UsingRuby")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(warCheckBox, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "WarFile")); // NOI18N

        interpreterDesc.setLabelFor(changeRubyButton);
        org.openide.awt.Mnemonics.setLocalizedText(interpreterDesc, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "RubyInterpreter")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(changeRubyButton, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "Change")); // NOI18N
        changeRubyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeRuby(evt);
            }
        });

        rubyField.setEditable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(setAsMainCheckBox)
                    .add(layout.createSequentialGroup()
                        .add(dbLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(dbCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel1)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jdbcCheckBox))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(warCheckBox)))
                .addContainerGap(35, Short.MAX_VALUE))
            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(interpreterDesc)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rubyField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(changeRubyButton))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(setAsMainCheckBox)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dbLabel)
                    .add(dbCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jdbcCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(warCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(interpreterDesc)
                    .add(changeRubyButton)
                    .add(rubyField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        setAsMainCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSN_setAsMainCheckBox")); // NOI18N
        setAsMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSD_setAsMainCheckBox")); // NOI18N
        rubyField.getAccessibleContext().setAccessibleName("Ruby Interpreter");

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_PanelOptionsVisual")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_PanelOptionsVisual")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void changeRuby(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeRuby
        String title = null;
        String desc = NbBundle.getMessage(PanelOptionsVisual.class, "RubyIsGlobal");
        String[] continueOptions = new String[] { NbBundle.getMessage(PanelOptionsVisual.class, "Continue") };
        DialogDescriptor descriptor =
                new DialogDescriptor(desc, title, true, continueOptions, continueOptions[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
        DialogDisplayer.getDefault().notify(descriptor);
    
        OptionsDisplayer.getDefault().open("RubyOptions"); // NOI18N
    }//GEN-LAST:event_changeRuby
    
    boolean valid(WizardDescriptor settings) {
        if ((warCheckBox.isSelected() || jdbcCheckBox.isSelected()) && !RubyInstallation.getInstance().isJRubySet()) {
            settings.putProperty( "WizardPanel_errorMessage", 
                    NbBundle.getMessage(PanelOptionsVisual.class, "JRubyRequired") ); //NOI18N
            return false;
        }
        String db = (String)dbCombo.getSelectedItem();
        if ((!jdbcCheckBox.isSelected()) && (db.equals(javaDb) || db.equals(otherJdbc))) {
            settings.putProperty( "WizardPanel_errorMessage", 
                    NbBundle.getMessage(PanelOptionsVisual.class, "JdbcRequired", db) ); //NOI18N
            return false;
        }
        //if (mainClassTextField.isVisible () && mainClassTextField.isEnabled ()) {
        //    if (!valid) {
        //        settings.putProperty( "WizardPanel_errorMessage", // NOI18N
        //            NbBundle.getMessage(PanelOptionsVisual.class,"ERROR_IllegalMainClassName")); //NOI18N
        //    }
        //    return this.valid;
        //}
        //else {
            return true;
        //}
    }
    
    void read (WizardDescriptor d) {
        RubyInstallation.getInstance().addPropertyChangeListener(this);
    }
    
    void validate (WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }

    void store( WizardDescriptor d ) {
        d.putProperty( /*XXX Define somewhere */ "setAsMain", setAsMainCheckBox.isSelected() && setAsMainCheckBox.isVisible() ? Boolean.TRUE : Boolean.FALSE ); // NOI18N
        //d.putProperty( /*XXX Define somewhere */ "mainClass", createMainCheckBox.isSelected() && createMainCheckBox.isVisible() ? mainClassTextField.getText() : null ); // NOI18N
        String db = (String) dbCombo.getSelectedItem();
        if (db.equals(javaDb)) {
            db = JAVA_DB;
        } else if (db.equals(otherJdbc)) {
            db = JDBC;
        }
        d.putProperty(NewRailsProjectWizardIterator.RAILS_DB_WN, db);
        d.putProperty(NewRailsProjectWizardIterator.JDBC_WN, jdbcCheckBox.isSelected() && jdbcCheckBox.isVisible() ? Boolean.TRUE : Boolean.FALSE ); // NOI18N
        d.putProperty(NewRailsProjectWizardIterator.GOLDSPIKE_WN, warCheckBox.isSelected() && warCheckBox.isVisible() ? Boolean.TRUE : Boolean.FALSE ); // NOI18N
        RubyInstallation.getInstance().removePropertyChangeListener(this);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton changeRubyButton;
    private javax.swing.JComboBox dbCombo;
    private javax.swing.JLabel dbLabel;
    private javax.swing.JLabel interpreterDesc;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox jdbcCheckBox;
    private javax.swing.JTextField rubyField;
    private javax.swing.JCheckBox setAsMainCheckBox;
    private javax.swing.JCheckBox warCheckBox;
    // End of variables declaration//GEN-END:variables
    
    //private void mainClassChanged () {
    //    
    //    String mainClassName = this.mainClassTextField.getText ();
    //    StringTokenizer tk = new StringTokenizer (mainClassName, "."); //NOI18N
    //    boolean valid = true;
    //    while (tk.hasMoreTokens()) {
    //        String token = tk.nextToken();
    //        if (token.length() == 0 || /* !Utilities.isJavaIdentifier(token)*/ token.equals(" ")) {
    //            valid = false;
    //            break;
    //        }            
    //    }
    //    this.valid = valid;
    //    this.panel.fireChangeEvent();
    //}
    
    public void interpreterChanged() {
        rubyField.setText(RubyInstallation.getInstance().getShortName());
        this.panel.fireChangeEvent();
    }
}
