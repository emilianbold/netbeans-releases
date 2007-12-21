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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.compapp.projects.base.ui.customizer;

import java.awt.Component;
import java.awt.event.ItemEvent;
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
import org.netbeans.modules.compapp.projects.base.IcanproConstants;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/** Customizer for general project attributes.
 *
 * @author  phrebejk
 */
public class CustomizerGeneral extends JPanel implements IcanproCustomizer.Panel {

    private IcanproProjectProperties webProperties;
    private VisualPropertySupport vps;
    private boolean bValidation = true;

    /** Creates new form CustomizerCompile */
    public CustomizerGeneral(IcanproProjectProperties webProperties) {
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerGeneral.class, "ACS_CustomizeGeneral_A11YDesc")); //NOI18N

        this.webProperties = webProperties;
        vps = new VisualPropertySupport(webProperties);
        
        Object validationObject =webProperties.get(IcanproConstants.VALIDATION_FLAG);
        
        // BpelProjectHelper.getInstance().getProjectProperty(IcanproProjectProperties.VALIDATION_FLAG);
        if (validationObject != null ){
            boolean validation = ((Boolean)validationObject).booleanValue();
            if (validation) {
                jCheckBox1.setSelected(true);
            } else {
                jCheckBox1.setSelected(false);
            }
            
        }else {
            jCheckBox1.setSelected(false);
        }
        
    }

    public void initValues(  ) {
        FileObject projectFolder = webProperties.getProject().getProjectDirectory();
        File pf = FileUtil.toFile(projectFolder);
        jTextFieldProjectFolder.setText(pf == null ? "" : pf.getPath()); // NOI18N

        vps.register(jTextFieldProjectType, IcanproProjectProperties.JBI_SE_TYPE);
        vps.register(jTextFieldServiceUnitDescription, IcanproProjectProperties.SERVICE_UNIT_DESCRIPTION);

        Charset originalCharset = (Charset) webProperties.get(IcanproProjectProperties.SOURCE_ENCODING);
        String originalEncoding = (originalCharset != null) ?
            originalCharset.name() : Charset.defaultCharset().name();
        
        vps.register(jComboBoxEncoding, new EncodingModel(originalEncoding), 
                new EncodingRenderer(), IcanproProjectProperties.SOURCE_ENCODING,
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelProjectName = new javax.swing.JLabel();
        jTextFieldProjectFolder = new javax.swing.JTextField();
        jLabelProjectType = new javax.swing.JLabel();
        jTextFieldProjectType = new javax.swing.JTextField();
        jLabelServiceUnitDescription = new javax.swing.JLabel();
        jTextFieldServiceUnitDescription = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxEncoding = new javax.swing.JComboBox();
        jCheckBox1 = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabelProjectName.setLabelFor(jTextFieldProjectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelProjectName, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustomizeGeneral_ProjectFolder_JLabel")); // NOI18N

        jTextFieldProjectFolder.setEditable(false);

        jLabelProjectType.setLabelFor(jTextFieldProjectType);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelProjectType, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustomizeGeneral_ProjectType_JLabel")); // NOI18N

        jTextFieldProjectType.setEditable(false);

        jLabelServiceUnitDescription.setLabelFor(jLabelServiceUnitDescription);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelServiceUnitDescription, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustomizeGeneral_AssemblyUnit_JLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "TXT_Encoding")); // NOI18N

        jComboBoxEncoding.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.jCheckBox1.text")); // NOI18N
        jCheckBox1.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.jCheckBox1.toolTipText")); // NOI18N
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox1validationHandler(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabelProjectName)
                            .add(jLabelProjectType)
                            .add(jLabelServiceUnitDescription)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jTextFieldProjectFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
                            .add(jTextFieldServiceUnitDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextFieldProjectType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                            .add(jComboBoxEncoding, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 147, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jCheckBox1))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {jLabelProjectName, jLabelProjectType}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(12, 12, 12)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelProjectName)
                    .add(jTextFieldProjectFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelProjectType)
                    .add(jTextFieldProjectType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldServiceUnitDescription, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelServiceUnitDescription))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jComboBoxEncoding, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jCheckBox1)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jTextFieldProjectFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACS_CustomizeGeneral_ProjectFolder_A11YDesc")); // NOI18N
        jCheckBox1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "CustomizerGeneral.jCheckBox1.text")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBox1validationHandler(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1validationHandler
        // TODO add your handling code here:
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            //bValidation = false;
            webProperties.put(IcanproConstants.VALIDATION_FLAG, true);
            //   BpelProjectHelper.getInstance().setProjectProperty(IcanproProjectProperties.VALIDATION_FLAG, "false", false);
        } else {
            webProperties.put(IcanproConstants.VALIDATION_FLAG, false);
            //   bValidation = true;
            //   BpelProjectHelper.getInstance().setProjectProperty(IcanproProjectProperties.VALIDATION_FLAG, "true",false);
        }
    }//GEN-LAST:event_jCheckBox1validationHandler

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBoxEncoding;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelProjectName;
    private javax.swing.JLabel jLabelProjectType;
    private javax.swing.JLabel jLabelServiceUnitDescription;
    private javax.swing.JTextField jTextFieldProjectFolder;
    private javax.swing.JTextField jTextFieldProjectType;
    private javax.swing.JTextField jTextFieldServiceUnitDescription;
    // End of variables declaration//GEN-END:variables

}
