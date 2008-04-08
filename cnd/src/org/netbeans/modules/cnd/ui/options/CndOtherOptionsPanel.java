/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.cnd.ui.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.modules.cnd.editor.filecreation.CndExtensionList;
import org.netbeans.modules.cnd.editor.filecreation.CndHandlableExtensions;
import org.netbeans.modules.cnd.editor.filecreation.ExtensionsSettings;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author sg155630
 */
public class CndOtherOptionsPanel extends javax.swing.JPanel implements ActionListener {

    private final Collection<? extends CndHandlableExtensions> res;
    
    public CndOtherOptionsPanel() {
        res = Lookup.getDefault().lookupResult(CndHandlableExtensions.class).allInstances();
        setName("TAB_CndOtherOptionsTab"); // NOI18N (used as a pattern...)
        initComponents();
        initGeneratedComponents();
        if( "Windows".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            setOpaque( false );
        }
    }

    void applyChanges() {
        for (ExtensionsElements ee : eeList) {
            ee.apply();
        }

        isChanged = false;
    }

    void update() {
        for (ExtensionsElements ee : eeList) {
            ee.update();
        }
    }

    // for OptionsPanelSupport
    private boolean isChanged = false;

    void cancel() {
        isChanged = false;
    }

    boolean isChanged() {
        return isChanged;
    }

    public void actionPerformed(ActionEvent e) {
        isChanged = true;
    }
    
    private void editExtensionsButtonActionPerformed(ExtensionsElements ee) {
        StringArrayCustomEditor editor = new StringArrayCustomEditor(
                ee.getValues(), ee.defaultValue,
                getMessage("EE_ItemLabel"), getMessage("EE_ItemLabel_Mnemonic").charAt(0),  // NOI18N
                getMessage("EE_ItemListLabel"), getMessage("EE_ItemListLabel_Mnemonic").charAt(0),  // NOI18N
                false);
        
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        outerPanel.add(editor, gridBagConstraints);
        
        Object[] options = new Object[] {NotifyDescriptor.OK_OPTION};
        DialogDescriptor dd = new DialogDescriptor(outerPanel, getMessage("ExtensionsListEditorTitle"), true, options, NotifyDescriptor.OK_OPTION, 0, null, null);
        
        DialogDisplayer dialogDisplayer = DialogDisplayer.getDefault();
        java.awt.Dialog dl = dialogDisplayer.createDialog(dd);
        dl.getAccessibleContext().setAccessibleDescription(getMessage("ExtensionsListEditorTitle_AD"));
        dl.pack();
        dl.setSize(new java.awt.Dimension(300, (int)dl.getPreferredSize().getHeight()));
        dl.setVisible(true);

        ee.defaultValue = editor.getDefaultValue();
        ee.setValues( editor.getItemList() );
    }

    private final List<ExtensionsElements> eeList = new ArrayList<ExtensionsElements>();
    
    private void initGeneratedComponents() {
        for (Iterator<? extends CndHandlableExtensions> it = res.iterator(); it.hasNext();) {
            CndHandlableExtensions che = it.next();
            ExtensionsSettings es = ExtensionsSettings.getInstance(che);
            final ExtensionsElements ee = new ExtensionsElements(es);

            ee.label.setText(NbBundle.getMessage(CndOtherOptionsPanel.class, "EE_ExtensionListTitle", che.getDisplayNameForExtensionList()));
            ee.button.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    editExtensionsButtonActionPerformed(ee);
                }
            });

            eeList.add(ee);
         }

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(layout);
        GroupLayout.SequentialGroup horizontalGroup = layout.createSequentialGroup();
        horizontalGroup.add(12, 12, 12);

        GroupLayout.ParallelGroup labelsGroup = layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING);
        for (int i = 0; i < eeList.size(); i++) {
            labelsGroup.add(eeList.get(i).label);
        }

        horizontalGroup.add(labelsGroup);
        horizontalGroup.add(4, 4, 4);

        GroupLayout.ParallelGroup textfieldsGroup = layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false);
        for (int i = 0; i <  eeList.size(); i++) {
            textfieldsGroup.add(eeList.get(i).textfield, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE);
        }
        horizontalGroup.add(textfieldsGroup);
        horizontalGroup.add(6, 6, 6);
        
        GroupLayout.ParallelGroup buttonsGroup = layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING);
        for (int i = 0; i < eeList.size(); i++) {
            buttonsGroup.add(eeList.get(i).button);
        }
        horizontalGroup.add(buttonsGroup);

        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                    .add(horizontalGroup)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup()
                .addContainerGap();
        
        for (int i = 0; i < eeList.size(); i++) {
            ExtensionsElements ee = eeList.get(i);
            verticalGroup.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(ee.label)
                        .add(ee.textfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(ee.button, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE));
            if (i !=  eeList.size() - 1) {
                verticalGroup.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED);
            } else {
                verticalGroup.addContainerGap(20, Short.MAX_VALUE);
            }
                
        }

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(verticalGroup)
        );
        
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();

        jPanel1.setOpaque(false);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 399, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        jPanel2.setOpaque(false);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 399, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 276, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables

    private static String getMessage(String resourceName) {
        return NbBundle.getMessage(CndOtherOptionsPanel.class, resourceName);
    }
    
    private static class ExtensionsElements {

        public ExtensionsElements(ExtensionsSettings es) {
            this.es = es;
            update();
            textfield.setContentType("text/html");  // NOI18N
            textfield.setEditable(false);
            updateTextField();
            button.setText(getMessage("CndOtherOptionsPanel.Extensions.EditButton"));
        }

        private final String DELIMITER = ", "; // NOI18N
    
        public void updateTextField() {
            String text = "";
            for (String elem : list) {
                if (text.length() > 0) {
                    text += DELIMITER;
                }
                if (elem.equals(defaultValue)) {
                    elem = "<b>" + elem + "</b>"; // NOI18N
                }
                text += elem;
            }

            textfield.setText(text);
        }

        String[] getValues() {
            return list.toArray(new String[list.size()]);
        }
        
        void setValues(String[] values) {
            list = Arrays.asList(values);
            updateTextField();
        }

        public void apply() {
            CndExtensionList el = new CndExtensionList(getValues());

            es.setExtensionList(el);
            es.setDefaultExtension(defaultValue);
        }

        public void update() {
            list = new ArrayList<String>();
            this.defaultValue = es.getDefaultExtension();
            for (Enumeration<String> enList = es.getExtensionList().extensions(); enList != null && enList.hasMoreElements();) {
                list.add(enList.nextElement());
            }
            updateTextField();
        }
        
        private final ExtensionsSettings es;
        public final JLabel label = new JLabel();
        public final JEditorPane textfield = new JEditorPane();
        public final JButton button = new JButton();
        public List<String> list;
        public String defaultValue;
        
    }
}
