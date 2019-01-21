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

package org.netbeans.modules.mobility.project.ui.customizer;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.spi.mobility.project.ui.customizer.ComposedCustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.HelpCtxCallback;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class CustomizerPlatform extends JPanel implements ComposedCustomizerPanel, VisualPropertyGroup, ActionListener {
    
    private final ArrayList<TypeComboElement> typeElements = new ArrayList();
    private ProjectProperties props;
    private boolean useDefault;
    
    private HelpCtxCallback callback;
    
    /** Creates new form CustomizerPlatform */
    public CustomizerPlatform() {
        initComponents();
        FileObject fo = FileUtil.getConfigFile("Customizer/org.netbeans.modules.kjava.j2meproject/Platform"); //NOI18N
        DataFolder df = DataFolder.findFolder(fo);
        DataObject dob[] = df.getChildren();
        for (int i=0; i<dob.length; i++) {
            typeElements.add(new TypeComboElement(dob[i].getPrimaryFile().getName(), dob[i].getNodeDelegate().getDisplayName(), (JComponent)dob[i].getPrimaryFile().getAttribute("customizerPanelClass"))); //NOI18N
        }
        jComboBox1.setModel(new DefaultComboBoxModel(typeElements.toArray()));
        final ListCellRenderer lcr = jComboBox1.getRenderer();
        jComboBox1.setRenderer(new ListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return lcr.getListCellRendererComponent(list, value instanceof TypeComboElement ? ((TypeComboElement)value).getDisplayName() : value, index, isSelected, cellHasFocus);
            }
        });
        jComboBox1.addActionListener(this);
        for (int i=0; i<typeElements.size(); i++) {
            JComponent c = typeElements.get(i).getCustomizer();
            jPanel1.add(c == null ? new JPanel() : c, typeElements.get(i).toString());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        defaultCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(defaultCheckBox, NbBundle.getMessage(CustomizerPlatform.class, "LBL_Use_Default")); // NOI18N
        defaultCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(defaultCheckBox, gridBagConstraints);
        defaultCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPlatform.class, "ACSN_UseDefault")); // NOI18N
        defaultCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPlatform.class, "ACSD_UseDefault")); // NOI18N

        jLabel1.setLabelFor(jComboBox1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(CustomizerPlatform.class, "CustomizerPlatform.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(jComboBox1, gridBagConstraints);
        jComboBox1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerPlatform.class, "ACSN_SelectPlatform")); // NOI18N
        jComboBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerPlatform.class, "ACSD_SelectPlatform")); // NOI18N

        jPanel1.setLayout(new java.awt.CardLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(CustomizerPlatform.class, "LBL_CustPlatform_NoCustomizer")); // NOI18N
        jPanel1.add(jLabel2, "none");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    public void initValues(ProjectProperties props, String configuration) {
        this.props = props;
        for (int i=0; i<typeElements.size(); i++) {
            JComponent c = typeElements.get(i).getCustomizer();
            if (c instanceof CustomizerPanel) ((CustomizerPanel)c).initValues(props, configuration);
        }
        VisualPropertySupport vps = VisualPropertySupport.getDefault(props);
        vps.register(defaultCheckBox, configuration, this);
    }

    public void initGroupValues(boolean useDefault) {
        this.useDefault = useDefault;
        jComboBox1.removeActionListener(this);
        jComboBox1.setEditable(true);
        VisualPropertySupport.getDefault(props).register(jComboBox1, null, DefaultPropertiesDescriptor.PLATFORM_TRIGGER, useDefault);
        jComboBox1.setEditable(false);
        jComboBox1.addActionListener(this);
        boolean showCombo = jComboBox1.getItemCount() > 1 || jComboBox1.getSelectedIndex() < 0;
        jLabel1.setVisible(showCombo);
        jComboBox1.setVisible(showCombo);
        actionPerformed(null);
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = jComboBox1.getSelectedItem();
        JComponent c = null;
        if (o instanceof TypeComboElement) {
            TypeComboElement tce = (TypeComboElement)o;
            ((CardLayout)jPanel1.getLayout()).show(jPanel1, tce.toString());
            c = tce.getCustomizer();
            if (c instanceof VisualPropertyGroup) ((VisualPropertyGroup)c).initGroupValues(useDefault);
        } else {
            ((CardLayout)jPanel1.getLayout()).show(jPanel1, "none"); //NOI18N
        }
        if (callback != null) callback.updateHelpCtx(new HelpCtx(c == null ? this.getClass() : c.getClass()));
    }

    public String[] getGroupPropertyNames() {
        ArrayList<String> names = new ArrayList();
        names.add(DefaultPropertiesDescriptor.PLATFORM_TRIGGER);
        for (int i=0; i<typeElements.size(); i++) {
            JComponent c = typeElements.get(i).getCustomizer();
            if (c instanceof VisualPropertyGroup) names.addAll(Arrays.asList(((VisualPropertyGroup)c).getGroupPropertyNames()));
        }
        return names.toArray(new String[names.size()]);
    }

    public void setHelpContextCallback(HelpCtxCallback callback) {
        this.callback = callback;
        if (typeElements.size() == 1) callback.updateHelpCtx(new HelpCtx(typeElements.get(0).getCustomizer().getClass()));
    }

    private class TypeComboElement {
        
        private String name, displayName;
        JComponent customizer;
        
        public TypeComboElement(String name, String displayName, JComponent customizer) {
            this.name = name;
            this.displayName = displayName;
            this.customizer = customizer;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public JComponent getCustomizer() {
            return customizer;
        }
        
        public String toString() {
            return name;
        }

        public int hashCode() {
            return name.hashCode();
        }

        public boolean equals(Object obj) {
            return obj != null && name.equals(obj.toString());
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox defaultCheckBox;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
    
}
