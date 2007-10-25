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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.compapp.projects.jbi.ui.customizer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;

import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.UIResource;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Customizer for general project attributes.
 *
 * @author  phrebejk
 */
public class CustomizerGeneral extends JPanel implements JbiJarCustomizer.Panel, HelpCtx.Provider {

    private JbiProjectProperties projProperties;
    private VisualPropertySupport vps;
    
//    private String originalEncoding;

    /** Creates new form CustomizerCompile */
    public CustomizerGeneral(JbiProjectProperties projProperties) {
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(CustomizerGeneral.class, "ACS_CustomizeGeneral_A11YDesc")); // NOI18N

        this.projProperties = projProperties;
        vps = new VisualPropertySupport(projProperties);
        
//        this.originalEncoding = this.projProperties.getProject().evaluator().
//                getProperty(JbiProjectProperties.SOURCE_ENCODING);
//        if (this.originalEncoding == null) {
//            this.originalEncoding = Charset.defaultCharset().name();
//        }
//        
//        this.encoding.setModel(new EncodingModel(this.originalEncoding));
//        this.encoding.setRenderer(new EncodingRenderer());        
//
//        this.encoding.addActionListener(new ActionListener () {
//            public void actionPerformed(ActionEvent arg0) {
//                handleEncodingChange();
//            }            
//        });
    }
    
//    private void handleEncodingChange() {
//        Charset enc = (Charset) encoding.getSelectedItem();
//        String encName;
//        if (enc != null) {
//            encName = enc.name();
//        } else {
//            encName = originalEncoding;
//        }
//        //this.projProperties.putAdditionalProperty(JbiProjectProperties.SOURCE_ENCODING, encName);
//        this.projProperties.put(JbiProjectProperties.SOURCE_ENCODING, encName);
//    }
    
    public void initValues() {        
        FileObject projectFolder = projProperties.getProject().getProjectDirectory();
        File pf = FileUtil.toFile(projectFolder);
        jTextFieldProjectFolder.setText(pf == null ? "" : pf.getPath()); // NOI18N

        vps.register(jTextFieldServiceAssemblyDescription, JbiProjectProperties.SERVICE_ASSEMBLY_DESCRIPTION);
        vps.register(jTextFieldServiceUnitDescription, JbiProjectProperties.SERVICE_UNIT_DESCRIPTION);
        
        String originalEncoding = projProperties.getProject().evaluator().
                getProperty(JbiProjectProperties.SOURCE_ENCODING);
        if (originalEncoding == null) {
            originalEncoding = Charset.defaultCharset().name();
        }
        vps.register(jComboBoxEncoding, new EncodingModel(originalEncoding), 
                new EncodingRenderer(), JbiProjectProperties.SOURCE_ENCODING,
                Charset.class);
    }

    
    private static class EncodingRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public EncodingRenderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            assert value instanceof Charset;
            setName("ComboBox.listRenderer"); // NOI18N
            setText(((Charset) value).displayName());
            setIcon(null);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
        
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name; // NOI18N
        }
        
    }
    
    private static class EncodingModel extends DefaultComboBoxModel {
        
        public EncodingModel(String originalEncoding) {
            Charset defEnc = null;
            for (Charset c : Charset.availableCharsets().values()) {
                if (c.name().equals(originalEncoding)) {
                    defEnc = c;
                }
                addElement(c);
            }
            if (defEnc == null) {
                //Create artificial Charset to keep the original value
                //May happen when the project was set up on the platform
                //which supports more encodings
                try {
                    defEnc = new UnknownCharset(originalEncoding);
                    addElement(defEnc);
                } catch (IllegalCharsetNameException e) {
                    //The source.encoding property is completely broken
                    Logger.getLogger(this.getClass().getName()).info(
                            "IllegalCharsetName: " + originalEncoding); // NOI18N
                }
            }
            if (defEnc == null) {
                defEnc = Charset.defaultCharset();
            }
            setSelectedItem(defEnc);
        }
    }
    
    private static class UnknownCharset extends Charset {
        
        UnknownCharset(String name) {
            super(name, new String[0]);
        }
        
        public boolean contains(Charset c) {
            throw new UnsupportedOperationException();
        }
        
        public CharsetDecoder newDecoder() {
            throw new UnsupportedOperationException();
        }
        
        public CharsetEncoder newEncoder() {
            throw new UnsupportedOperationException();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelProjectName = new javax.swing.JLabel();
        jLabelServiceAssemblyDescription = new javax.swing.JLabel();
        jTextFieldServiceAssemblyDescription = new javax.swing.JTextField();
        jLabelServiceUnitDescription = new javax.swing.JLabel();
        jTextFieldServiceUnitDescription = new javax.swing.JTextField();
        jTextFieldProjectFolder = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxEncoding = new javax.swing.JComboBox();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabelProjectName.setLabelFor(jTextFieldProjectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelProjectName, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustomizeGeneral_ProjectFolder_JLabel")); // NOI18N

        jLabelServiceAssemblyDescription.setLabelFor(jTextFieldServiceAssemblyDescription);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelServiceAssemblyDescription, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustomizeGeneral_AssemblyUnitDescription_JLabel")); // NOI18N

        jLabelServiceUnitDescription.setLabelFor(jTextFieldServiceUnitDescription);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelServiceUnitDescription, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustomizeGeneral_ApplicationSubAssemblyDescription_JLabel")); // NOI18N

        jTextFieldProjectFolder.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "jLabel1");

        jLabel2.setLabelFor(jComboBoxEncoding);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "TXT_Encoding")); // NOI18N

        jComboBoxEncoding.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabelServiceUnitDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabelServiceAssemblyDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabelProjectName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(4, 4, 4)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextFieldServiceUnitDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextFieldServiceAssemblyDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                            .add(jTextFieldProjectFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jComboBoxEncoding, 0, 393, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelProjectName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldProjectFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldServiceAssemblyDescription, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelServiceAssemblyDescription))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldServiceUnitDescription, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelServiceUnitDescription))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jComboBoxEncoding, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jLabelProjectName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACS_PROJECT_FOLDER")); // NOI18N
        jLabelServiceAssemblyDescription.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACS_SA_DESCRIPTION")); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/compapp/projects/jbi/ui/customizer/Bundle"); // NOI18N
        jTextFieldServiceAssemblyDescription.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeGeneral_AssemblyUnitDescription_A11YDesc")); // NOI18N
        jLabelServiceUnitDescription.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACS_DEFAULT_SU_DESCRIPTION")); // NOI18N
        jTextFieldServiceUnitDescription.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_CustomizeGeneral_ApplicationSubAssemblyDescription_A11YDesc")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACS_ENCODING")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxEncoding;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelProjectName;
    private javax.swing.JLabel jLabelServiceAssemblyDescription;
    private javax.swing.JLabel jLabelServiceUnitDescription;
    private javax.swing.JTextField jTextFieldProjectFolder;
    private javax.swing.JTextField jTextFieldServiceAssemblyDescription;
    private javax.swing.JTextField jTextFieldServiceUnitDescription;
    // End of variables declaration//GEN-END:variables

  /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerGeneral.class);
    } 
}
