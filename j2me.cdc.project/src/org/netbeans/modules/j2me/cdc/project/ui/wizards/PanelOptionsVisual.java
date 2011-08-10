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

package org.netbeans.modules.j2me.cdc.project.ui.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.StringTokenizer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  phrebejk
 */
public class PanelOptionsVisual extends SettingsPanel implements ActionListener, PropertyChangeListener {
    
    private static boolean lastMainClassCheck = true; // XXX Store somewhere

    private static final String SET_AS_MAIN = "setAsMain"; //NOI18N
    private static final String MAIN_CLASS = "mainClass"; //NOI18N
    
    
    protected PanelConfigureProject panel;
    private boolean valid;
    private int type;

    @SuppressWarnings("LeakingThisInConstructor") //NOI18N
    public PanelOptionsVisual(final PanelConfigureProject panel, int type) {
        initComponents();
        this.type = type;
        this.panel = panel;

        switch (type) {
            case NewCDCProjectWizardIterator.TYPE_LIB:
                setAsMainCheckBox.setVisible(false);
                createMainCheckBox.setVisible(false);
                mainClassTextField.setVisible(false);
                separator.setVisible(false);
                break;
            case NewCDCProjectWizardIterator.TYPE_APP:
                createMainCheckBox.addActionListener(this);
                createMainCheckBox.setSelected(lastMainClassCheck);
                mainClassTextField.setEnabled(lastMainClassCheck);
                separator.setVisible(true);
                break;
            case NewCDCProjectWizardIterator.TYPE_EXT:
                setAsMainCheckBox.setVisible(true);
                createMainCheckBox.setVisible(false);
                mainClassTextField.setVisible(false);
                break;
            case NewCDCProjectWizardIterator.TYPE_SAMPLE:
                createMainCheckBox.setVisible(false);
                mainClassTextField.setVisible(false);
                separator.setVisible(true);
                break;
        }
        
        this.mainClassTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                mainClassChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                mainClassChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                mainClassChanged();
            }
        });
     }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createMainCheckBox) {
            lastMainClassCheck = createMainCheckBox.isSelected();
            mainClassTextField.setEnabled(lastMainClassCheck);
            this.panel.fireChangeEvent();
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (PanelProjectLocationVisual.PROP_PROJECT_NAME.equals(event.getPropertyName())) {
            String newProjectName = NewCDCProjectWizardIterator.getPackageName((String) event.getNewValue());
            this.mainClassTextField.setText(MessageFormat.format(
                    NbBundle.getMessage(PanelOptionsVisual.class, "TXT_ClassName"), new Object[]{newProjectName} //NOI18N
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
           
    @Override
    boolean valid(WizardDescriptor settings) {
        if (mainClassTextField.isVisible () && mainClassTextField.isEnabled ()) {
            if (!valid) {
                settings.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(PanelOptionsVisual.class,"ERROR_IllegalMainClassName")); //NOI18N
            }
            return this.valid;
        }
        return true;
    }
    
    @Override
    void read (WizardDescriptor d) {
        JavaPlatform platforms[] = JavaPlatformManager.getDefault().getPlatforms(null, new Specification(CDCPlatform.PLATFORM_CDC, null));
        if (platforms.length != 0 && platforms[0] instanceof CDCPlatform) {
            CDCPlatform platform = (CDCPlatform) platforms[0];
            assert platform != null;
        }
    }
    
    @Override
    void validate (WizardDescriptor d) {
        // nothing to validate
    }

    @Override
    void store( WizardDescriptor d ) {
        d.putProperty(SET_AS_MAIN, setAsMainCheckBox.isSelected() && setAsMainCheckBox.isVisible() ? Boolean.TRUE : Boolean.FALSE );
        d.putProperty(MAIN_CLASS, createMainCheckBox.isSelected() && createMainCheckBox.isVisible() ? mainClassTextField.getText() : null );
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
        boolean _valid = true;
        while (tk.hasMoreTokens()) {
            String token = tk.nextToken();
            if (token.length() == 0 || !Utilities.isJavaIdentifier(token)) {
                _valid = false;
                break;
            }            
        }
        this.valid = _valid;
        this.panel.fireChangeEvent();
    }
}

