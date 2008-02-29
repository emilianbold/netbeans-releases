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

package org.netbeans.modules.j2me.cdc.project;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.modules.j2me.cdc.project.CDCPropertiesDescriptor;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.MouseUtils;
import org.openide.util.NbBundle;


/**
 *
 * @author  Adam Sotona
 */
public class CustomizerRun extends JPanel implements CustomizerPanel, VisualPropertyGroup  {
    
    final private javax.swing.JTextField jTextHidden;
    
    private static String[] PROPERTY_NAMES = new String[] {
        CDCPropertiesDescriptor.MAIN_CLASS,
        CDCPropertiesDescriptor.MAIN_CLASS_CLASS,
        CDCPropertiesDescriptor.APPLICATION_ARGS,
        DefaultPropertiesDescriptor.RUN_CMD_OPTIONS
    };
    
    private VisualPropertySupport vps;
    private ProjectProperties prop;
    
    public CustomizerRun() {
        initComponents();
        jTextHidden=new javax.swing.JTextField();        
    }
    public void initValues(ProjectProperties props, String configuration) {
        vps = VisualPropertySupport.getDefault(props);
        vps.register(jCheckBox1, configuration, this);
        prop=props;        
    }
    
    public String[] getGroupPropertyNames() {
        return PROPERTY_NAMES;
    }
    
    public void initGroupValues(boolean useDefault) {
        vps.register(jTextFieldMainClass, CDCPropertiesDescriptor.MAIN_CLASS, useDefault);
        vps.register(jTextFieldArgs, CDCPropertiesDescriptor.APPLICATION_ARGS, useDefault);
        vps.register(jTextVMOptions, DefaultPropertiesDescriptor.RUN_CMD_OPTIONS, useDefault);
        vps.register(jTextHidden, CDCPropertiesDescriptor.MAIN_CLASS_CLASS, useDefault);
        
        jButtonMainClass.addActionListener( new MainClassListener( jTextFieldMainClass));
        
        jLabelArgs.setEnabled(!useDefault);
        jLabelMainClass.setEnabled(!useDefault);
        jLabelVMOptions.setEnabled(!useDefault);
        jLabelVMOptionsExample.setEnabled(!useDefault);
        jButtonMainClass.setEnabled(!useDefault);
        jTextFieldMainClass.setEditable(false);
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
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabelMainClass = new javax.swing.JLabel();
        jTextFieldMainClass = new javax.swing.JTextField();
        jButtonMainClass = new javax.swing.JButton();
        jLabelArgs = new javax.swing.JLabel();
        jTextFieldArgs = new javax.swing.JTextField();
        jLabelVMOptions = new javax.swing.JLabel();
        jTextVMOptions = new javax.swing.JTextField();
        jLabelVMOptionsExample = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, NbBundle.getMessage(CustomizerRun.class, "LBL_UseDefault")); // NOI18N
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jCheckBox1, gridBagConstraints);
        jCheckBox1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACSN_UseDefault")); // NOI18N
        jCheckBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACSD_UseDefault")); // NOI18N

        jLabelMainClass.setLabelFor(jTextFieldMainClass);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabelMainClass, gridBagConstraints);
        jLabelMainClass.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "ASCN_MAINCLASS")); // NOI18N
        jLabelMainClass.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "ASCD_MAINCLASS")); // NOI18N

        jTextFieldMainClass.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(jTextFieldMainClass, gridBagConstraints);
        jTextFieldMainClass.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "ASCN_MAINCLASS")); // NOI18N
        jTextFieldMainClass.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "ASCD_MAINCLASS")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(jButtonMainClass, gridBagConstraints);
        jButtonMainClass.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "ASCN_BROWSE")); // NOI18N
        jButtonMainClass.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "ASCD_BROWSE")); // NOI18N

        jLabelArgs.setLabelFor(jTextFieldArgs);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelArgs, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Args_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabelArgs, gridBagConstraints);
        jLabelArgs.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "ASCN_ARGUMENTS")); // NOI18N
        jLabelArgs.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "ASCD_ARGUMENTS")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(jTextFieldArgs, gridBagConstraints);
        jTextFieldArgs.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "ASCN_ARGUMENTS")); // NOI18N
        jTextFieldArgs.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "ASCD_ARGUMENTS")); // NOI18N

        jLabelVMOptions.setLabelFor(jTextVMOptions);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptions, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jLabelVMOptions, gridBagConstraints);
        jLabelVMOptions.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "ACSN_VMOPTIONS")); // NOI18N
        jLabelVMOptions.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "ASCD_VMOPTIONS")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(jTextVMOptions, gridBagConstraints);
        jTextVMOptions.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "ACSN_VMOPTIONS")); // NOI18N
        jTextVMOptions.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "ASCD_VMOPTIONS")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptionsExample, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options_Example")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(jLabelVMOptionsExample, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonMainClass;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabelArgs;
    private javax.swing.JLabel jLabelMainClass;
    private javax.swing.JLabel jLabelVMOptions;
    private javax.swing.JLabel jLabelVMOptionsExample;
    private javax.swing.JTextField jTextFieldArgs;
    private javax.swing.JTextField jTextFieldMainClass;
    private javax.swing.JTextField jTextVMOptions;
    // End of variables declaration//GEN-END:variables
    
    // Innercasses -------------------------------------------------------------
    
    private class MainClassListener implements ActionListener  {
        
        protected final JButton okButton;
        private JTextField mainClassTextField;
        
        MainClassListener( JTextField mainClassTextField ) {            
            this.mainClassTextField = mainClassTextField;
            this.okButton  = new JButton (NbBundle.getMessage (CustomizerRun.class, "LBL_ChooseMainClass_OK"));
            this.okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (CustomizerRun.class, "AD_ChooseMainClass_OK"));
        }
        
        // Implementation of ActionListener ------------------------------------
        
        /** Handles button events
         */        
        public void actionPerformed( ActionEvent e ) { 
            // only chooseMainClassButton can be performed
            final MainClassChooser panel = new MainClassChooser (prop.getSourceRoot(),
                    CDCProjectUtil.getExecutionModes(prop),(String)prop.get("platform.bootclasspath"));

            Object[] options = new Object[] {
                okButton,
                DialogDescriptor.CANCEL_OPTION
            };

            panel.setSelectedMainClass(mainClassTextField.getText());
            panel.addChangeListener (new ChangeListener () {
               public void stateChanged(ChangeEvent e) {
                   if ((e.getSource () instanceof MouseEvent) && 
                           MouseUtils.isDoubleClick (((MouseEvent)e.getSource ())) ) {
                       // click button and finish the dialog with selected class
                       okButton.doClick ();
                   } else {
                       okButton.setEnabled (panel.getSelectedMainClass () != null);
                   }
               }
            });
            okButton.setEnabled (false);
            DialogDescriptor desc = new DialogDescriptor (
                panel,
                NbBundle.getMessage (CustomizerRun.class, "LBL_ChooseMainClass_Title" ),
                true, 
                options, 
                options[0], 
                DialogDescriptor.BOTTOM_ALIGN, 
                null, 
                null);
            //desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
            Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
            dlg.setVisible (true);
            if (desc.getValue() == options[0]) {
               mainClassTextField.setText (panel.getSelectedMainClass ());
               if (panel.isAppletExecution())
               {
                   jTextHidden.setText("applet");
               }
               else if (panel.isXletExecution())
               {
                   jTextHidden.setText("xlet");
               }
               else
               {
                   jTextHidden.setText("main");
               }
            } 
            dlg.dispose();
            
        }                
    }

    
}
